package es.hefame.keyvault.dao.mariadb;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.message.acronym.CertificateType;
import es.hefame.keyvault.datastructure.message.acronym.KeyAlgorithm;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class MariaDbKeypairDAO implements KeypairDAO {

	private static Logger logger = LogManager.getLogger();

	@Override
	public Keypair get_by_id(String pair_id) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT id, owner_name, owner_domain_id, certificate, privatekey, certificate_type, privatekey_algorithm FROM keypair WHERE id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, pair_id);
			rs = st.executeQuery();

			if (rs.next()) {
				String id = rs.getString("id");
				String owner_name = rs.getString("owner_name");
				String owner_domain_id = rs.getString("owner_domain_id");
				Blob certificate_bytes = rs.getBlob("certificate");
				Blob privatekey_bytes = rs.getBlob("privatekey");
				CertificateType certificate_type = CertificateType.build(rs.getString("certificate_type"));
				KeyAlgorithm privatekey_algorithm = KeyAlgorithm.build(rs.getString("privatekey_algorithm"));
				MariaDbConnection.clearResources(st, rs);

				// Building the Certificate
				CertificateFactory cert_fact = CertificateFactory.getInstance(certificate_type.certificateFactoryName);
				X509Certificate certificate = (X509Certificate) cert_fact
						.generateCertificate(certificate_bytes.getBinaryStream());

				// Building the PrivateKey
				KeyFactory kf = KeyFactory.getInstance(privatekey_algorithm.name());
				PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(
						privatekey_bytes.getBytes(1, (int) privatekey_bytes.length()));
				PrivateKey privatekey = kf.generatePrivate(keysp);

				// Building the Certificate Chain
				selectSQL = "SELECT certificate, certificate_type FROM keypair_cert_chain WHERE keypair_id = ?";
				st = conn.prepareStatement(selectSQL);
				st.setString(1, pair_id);
				rs = st.executeQuery();

				List<X509Certificate> cert_chain = new LinkedList<X509Certificate>();
				while (rs.next()) {
					Blob chain_cert = rs.getBlob("certificate");
					certificate_type = CertificateType.build(rs.getString("certificate_type"));
					cert_fact = CertificateFactory.getInstance(certificate_type.certificateFactoryName);
					cert_chain.add((X509Certificate) cert_fact.generateCertificate(chain_cert.getBinaryStream()));
				}

				return new Keypair(id, Domain.generateFQDN(owner_name, owner_domain_id), certificate, privatekey,
						cert_chain);

			}
		} catch (SQLException | CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new HException("Error during database operation", e);
		} finally {
			MariaDbConnection.clearResources(st, rs, conn);
		}

		return null;
	}

	@Override
	public List<Keypair> get_list() throws HException {
		Connection conn = null;
		PreparedStatement st = null, st2 = null;
		ResultSet rs = null, rs2 = null;
		List<Keypair> certs = new LinkedList<Keypair>();

		try {
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT id, owner_name, owner_domain_id, certificate, privatekey, certificate_type, privatekey_algorithm FROM keypair";
			st = conn.prepareStatement(selectSQL);
			rs = st.executeQuery();
			MariaDbConnection.clearResources(st);

			while (rs.next()) {
				String id = rs.getString("id");
				String owner_name = rs.getString("owner_name");
				String owner_domain_id = rs.getString("owner_domain_id");
				Blob certificate_bytes = rs.getBlob("certificate");
				Blob privatekey_bytes = rs.getBlob("privatekey");
				CertificateType certificate_type = CertificateType.build(rs.getString("certificate_type"));
				KeyAlgorithm privatekey_algorithm = KeyAlgorithm.build(rs.getString("privatekey_algorithm"));

				// Building the Certificate
				CertificateFactory cert_fact = CertificateFactory.getInstance(certificate_type.certificateFactoryName);
				X509Certificate certificate = (X509Certificate) cert_fact
						.generateCertificate(certificate_bytes.getBinaryStream());

				// Building the PrivateKey
				KeyFactory kf = KeyFactory.getInstance(privatekey_algorithm.name());
				PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(
						privatekey_bytes.getBytes(1, (int) privatekey_bytes.length()));
				PrivateKey privatekey = kf.generatePrivate(keysp);

				// Building the Certificate Chain
				selectSQL = "SELECT certificate, certificate_type FROM keypair_cert_chain WHERE keypair_id = ?";
				st = conn.prepareStatement(selectSQL);
				st.setString(1, id);
				rs2 = st.executeQuery();
				MariaDbConnection.clearResources(st);

				List<X509Certificate> cert_chain = new LinkedList<X509Certificate>();
				while (rs2.next()) {
					Blob chain_cert = rs2.getBlob("certificate");
					certificate_type = CertificateType.build(rs2.getString("certificate_type"));
					cert_fact = CertificateFactory.getInstance(certificate_type.certificateFactoryName);
					cert_chain.add((X509Certificate) cert_fact.generateCertificate(chain_cert.getBinaryStream()));
				}
				MariaDbConnection.clearResources(rs2);
				certs.add(new Keypair(id, Domain.generateFQDN(owner_name, owner_domain_id), certificate, privatekey,
						cert_chain));

			}
		} catch (SQLException | CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new HException("Error during database operation", e);
		} finally {
			MariaDbConnection.clearResources(st, rs, st2, rs2, conn);
		}

		return certs;
	}

	@Override
	public List<Keypair> get_owned_by(Person owner) throws HException {
		return this.get_owned_by_person_id(owner.get_identifier());
	}

	@Override
	public List<Keypair> get_owned_by_person_id(String owner_id) throws HException {
		Connection conn = null;
		PreparedStatement st = null, st2 = null;
		ResultSet rs = null, rs2 = null;

		String owner_name = Domain.getPersonNameFromFQDN(owner_id);
		String owner_domain_id = Domain.getDomainIdFromFQDN(owner_id);

		List<Keypair> certs = new LinkedList<Keypair>();

		try {
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT id, owner_name, owner_domain_id, certificate, privatekey, certificate_type, privatekey_algorithm FROM keypair WHERE owner_name = ? AND owner_domain_id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, owner_name);
			st.setString(2, owner_domain_id);
			rs = st.executeQuery();
			MariaDbConnection.clearResources(st);

			while (rs.next()) {
				String id = rs.getString("id");
				owner_name = rs.getString("owner_name");
				owner_domain_id = rs.getString("owner_domain_id");
				Blob certificate_bytes = rs.getBlob("certificate");
				Blob privatekey_bytes = rs.getBlob("privatekey");
				CertificateType certificate_type = CertificateType.build(rs.getString("certificate_type"));
				KeyAlgorithm privatekey_algorithm = KeyAlgorithm.build(rs.getString("privatekey_algorithm"));

				// Building the Certificate
				CertificateFactory cert_fact = CertificateFactory.getInstance(certificate_type.certificateFactoryName);
				X509Certificate certificate = (X509Certificate) cert_fact
						.generateCertificate(certificate_bytes.getBinaryStream());

				// Building the PrivateKey
				KeyFactory kf = KeyFactory.getInstance(privatekey_algorithm.name());
				PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(
						privatekey_bytes.getBytes(1, (int) privatekey_bytes.length()));
				PrivateKey privatekey = kf.generatePrivate(keysp);

				// Building the Certificate Chain
				selectSQL = "SELECT certificate, certificate_type FROM keypair_cert_chain WHERE keypair_id = ?";
				st = conn.prepareStatement(selectSQL);
				st.setString(1, id);
				rs2 = st.executeQuery();
				MariaDbConnection.clearResources(st);

				List<X509Certificate> cert_chain = new LinkedList<X509Certificate>();
				while (rs2.next()) {
					Blob chain_cert = rs2.getBlob("certificate");
					certificate_type = CertificateType.build(rs2.getString("certificate_type"));
					cert_fact = CertificateFactory.getInstance(certificate_type.certificateFactoryName);
					cert_chain.add((X509Certificate) cert_fact.generateCertificate(chain_cert.getBinaryStream()));
				}
				MariaDbConnection.clearResources(rs2);
				certs.add(new Keypair(id, Domain.generateFQDN(owner_name, owner_domain_id), certificate, privatekey,
						cert_chain));

			}
		} catch (SQLException | CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new HException("Error during database operation", e);
		} finally {
			MariaDbConnection.clearResources(st, rs, st2, rs2, conn);
		}

		logger.debug("Se retornaron [{}] pares de claves de la bbdd", certs.size());
		return certs;
	}

	@Override
	public boolean insert(Keypair new_keypair) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		try {
			// Creamos el usuario dueño de la clave si no existe de antes
			Person p = DAO.person().get_by_fqdn(new_keypair.getOwnerId());
			if (p == null) {
				p = new Person(new_keypair.getOwnerId());
				logger.trace("Persona creada como requisito para insertar la clave: {}", p.jsonEncode());
				DAO.person().insert(p);
			}

			conn = MariaDbConnection.getConnection();

			String selectSQL = "INSERT INTO keypair (id, owner_name, owner_domain_id, certificate, privatekey, certificate_type, privatekey_algorithm) VALUES (?, ?, ?, ?, ?, ?, ?)";
			st = conn.prepareStatement(selectSQL);

			st.setString(1, new_keypair.getIdentifier());
			st.setString(2, Domain.getPersonNameFromFQDN(new_keypair.getOwnerId()));
			st.setString(3, Domain.getDomainIdFromFQDN(new_keypair.getOwnerId()));
			st.setBlob(4, new SerialBlob(new_keypair.getCertificate().getEncoded()));
			st.setBlob(5, new SerialBlob(new_keypair.getPrivateKey().getEncoded()));
			st.setString(6, new_keypair.getCertificate().getType());
			st.setString(7, new_keypair.getPrivateKey().getAlgorithm());
			int result = st.executeUpdate();

			MariaDbConnection.clearResources(st);

			int i = 1;
			if (result != i) {
				return false;
			}

			for (X509Certificate cert_in_chain : new_keypair.getCertificateChain()) {
				selectSQL = "INSERT INTO keypair_cert_chain (keypair_id, chain_number, certificate, certificate_type) VALUES (?, ?, ?, ?)";
				st = conn.prepareStatement(selectSQL);
				st.setString(1, new_keypair.getIdentifier());
				st.setInt(2, i++);
				st.setBlob(3, new SerialBlob(cert_in_chain.getEncoded()));
				st.setString(4, cert_in_chain.getType());
				result += st.executeUpdate();
				MariaDbConnection.clearResources(st);
			}

			if (result == i) {
				conn.commit();
				return true;
			} else {
				conn.rollback();
				return false;
			}
		} catch (SQLException | CertificateEncodingException e) {
			MariaDbConnection.rollback(conn);
			throw new HException("Error during database operation", e);
		} finally {
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean update(Keypair modified_keypair) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		try {
			// Creamos el usuario due�o de la clave si no existe de antes
			Person p = DAO.person().get_by_fqdn(modified_keypair.getOwnerId());
			if (p == null) {
				p = new Person(modified_keypair.getOwnerId());
				logger.trace("Persona creada como requisito para actualizar la clave: {}", p.jsonEncode());
				DAO.person().insert(p);
			}

			conn = MariaDbConnection.getConnection();
			String selectSQL = "UPDATE keypair SET owner_name = ?, owner_domain_id = ?, certificate = ?, privatekey = ?, certificate_type = ?, privatekey_algorithm = ? WHERE id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, Domain.getPersonNameFromFQDN(modified_keypair.getOwnerId()));
			st.setString(2, Domain.getDomainIdFromFQDN(modified_keypair.getOwnerId()));
			st.setBlob(3, new SerialBlob(modified_keypair.getCertificate().getEncoded()));
			st.setBlob(4, new SerialBlob(modified_keypair.getPrivateKey().getEncoded()));
			st.setString(5, modified_keypair.getCertificate().getType());
			st.setString(6, modified_keypair.getPrivateKey().getAlgorithm());
			st.setString(7, modified_keypair.getIdentifier());

			int result = st.executeUpdate();
			MariaDbConnection.clearResources(st);

			int i = 1;
			if (result != i) {
				return false;
			}

			selectSQL = "DELETE FROM keypair_cert_chain WHERE keypair_id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, modified_keypair.getIdentifier());
			st.executeUpdate();
			MariaDbConnection.clearResources(st);

			for (X509Certificate cert_in_chain : modified_keypair.getCertificateChain()) {
				selectSQL = "INSERT INTO keypair_cert_chain (keypair_id, chain_number, certificate, certificate_type) VALUES (?, ?, ?, ?)";
				st = conn.prepareStatement(selectSQL);
				st.setString(1, modified_keypair.getIdentifier());
				st.setInt(2, i++);
				st.setBlob(3, new SerialBlob(cert_in_chain.getEncoded()));
				st.setString(4, cert_in_chain.getType());
				result += st.executeUpdate();
				MariaDbConnection.clearResources(st);
			}

			if (result == i) {
				conn.commit();
				return true;
			} else {
				conn.rollback();
				return false;
			}
		} catch (SQLException | CertificateEncodingException e) {
			MariaDbConnection.rollback(conn);
			throw new HException("Error during database operation", e);
		} finally {
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean delete(String keypair_id) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		try {
			conn = MariaDbConnection.getConnection();

			String selectSQL = "DELETE FROM keypair WHERE id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, keypair_id);
			int result = st.executeUpdate();

			if (result == 1) {
				conn.commit();
				return true;
			} else {
				conn.rollback();
				return false;
			}
		} catch (SQLException e) {
			MariaDbConnection.rollback(conn);
			throw new HException("Error during database operation", e);
		} finally {
			MariaDbConnection.clearResources(st, conn);
		}
	}

	public boolean delete(Keypair keypair) throws HException {
		if (keypair == null)
			return false;
		return this.delete(keypair.getIdentifier());
	}

}
