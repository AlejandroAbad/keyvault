package es.hefame.keyvault.dao.mariadb;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;


//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DelegationDAO;
import es.hefame.keyvault.datastructure.model.Delegation;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class MariaDbDelegationDAO implements DelegationDAO
{
	//private static Logger L = LogManager.getLogger();

	@Override
	public List<Delegation> getPersonDelegations(String personId) throws HException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Delegation> getKeypairDelegations(String keypairId) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT delegate_name, delegate_domain_id, not_before_timestamp, not_after_timestamp FROM delegation WHERE keypair_id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, keypairId);
			rs = st.executeQuery();

			List<Delegation> delegations = new LinkedList<Delegation>();
			
			while (rs.next())
			{
				
				String delegateName = rs.getString("delegate_name");
				String delegateDomainId = rs.getString("delegate_domain_id");
				Long notBeforeTimestamp = rs.getLong("not_before_timestamp");
				Long notAfterTimestamp = rs.getLong("not_after_timestamp");
				
				
				String delegateId = Domain.generateFQDN(delegateName, delegateDomainId);
				delegations.add(new Delegation(keypairId, delegateId, notBeforeTimestamp, notAfterTimestamp));
			}

			return delegations;
		}
		catch (SQLException e)
		{
			throw new HException("Error durante la operación con la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, rs, conn);
		}

	}

	@Override
	public boolean insert(Delegation delegation) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "INSERT INTO delegation (keypair_id, delegate_name, delegate_domain_id, not_before_timestamp, not_after_timestamp) VALUES (?, ?, ?, ?, ?)";
			st = conn.prepareStatement(selectSQL);

			String delegateName = Domain.getPersonNameFromFQDN(delegation.getDelegateId());
			String delegateDomainId = Domain.getDomainIdFromFQDN(delegation.getDelegateId());
			
			Long notBeforeTimestamp = delegation.getNotBefore() == null ? null : delegation.getNotBefore().getTime();
			Long notAfterTimestamp = delegation.getNotAfter() == null ? null : delegation.getNotAfter().getTime();
			
			st.setString(1, delegation.getKeypairId());
			st.setString(2, delegateName);
			st.setString(3, delegateDomainId);
			st.setLong(4, notBeforeTimestamp);
			st.setLong(5, notAfterTimestamp);
			int result = st.executeUpdate();

			if (result == 1)
			{
				conn.commit();
				return true;
			}
			else
			{
				conn.rollback();
				return false;
			}
		}
		catch (SQLException e)
		{
			MariaDbConnection.rollback(conn);
			throw new HException("Error al realizar la operación la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean update(Delegation delegation) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "UPDATE delegation SET not_before_timestamp = ?, not_after_timestamp = ? WHERE keypair_id = ? AND delegate_name = ? AND delegate_domain_id = ?";
			st = conn.prepareStatement(selectSQL);

			String delegateName = Domain.getPersonNameFromFQDN(delegation.getDelegateId());
			String delegateDomainId = Domain.getDomainIdFromFQDN(delegation.getDelegateId());
			
			Long notBeforeTimestamp = delegation.getNotBefore() == null ? null : delegation.getNotBefore().getTime();
			Long notAfterTimestamp = delegation.getNotAfter() == null ? null : delegation.getNotAfter().getTime();
			
			st.setLong(1, notBeforeTimestamp);
			st.setLong(2, notAfterTimestamp);
			st.setString(3, delegation.getKeypairId());
			st.setString(4, delegateName);
			st.setString(5, delegateDomainId);
			
			int result = st.executeUpdate();

			if (result == 1)
			{
				conn.commit();
				return true;
			}
			else
			{
				conn.rollback();
				return false;
			}
		}
		catch (SQLException e)
		{
			MariaDbConnection.rollback(conn);
			throw new HException("Error al realizar la operación en la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean delete(Delegation delegation) throws HException {
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();

			String selectSQL = "DELETE FROM delegation WHERE keypair_id = ? AND delegate_name = ? AND delegate_domain_id = ?";
			st = conn.prepareStatement(selectSQL);
			
			String delegateName = Domain.getPersonNameFromFQDN(delegation.getDelegateId());
			String delegateDomainId = Domain.getDomainIdFromFQDN(delegation.getDelegateId());
			
			st.setString(1, delegation.getKeypairId());
			st.setString(2, delegateName);
			st.setString(3, delegateDomainId);

			int result = st.executeUpdate();
			if (result == 1)
			{
				conn.commit();
				return true;
			}
			else
			{
				conn.rollback();
				return false;
			}
		}
		catch (SQLException e)
		{
			MariaDbConnection.rollback(conn);
			throw new HException("Error al consultar la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}
	
}
