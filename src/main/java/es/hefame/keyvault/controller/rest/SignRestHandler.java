package es.hefame.keyvault.controller.rest;

import java.io.IOException;

import es.hefame.hcore.HException;
import es.hefame.keyvault.crypto.SignatureFactory;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.message.InfoMessage;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.SignatureFormat;
import es.hefame.keyvault.datastructure.message.sign.SignRequestMessageParser;
import es.hefame.keyvault.datastructure.message.sign.SignResponseMessage;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class SignRestHandler extends HttpController
{

	@Override
	protected void post(HttpConnection t) throws IOException, HException
	{
		byte[] requestBody = t.request.getBodyAsByteArray();
		SignRequestMessageParser incomingMessage = new SignRequestMessageParser(requestBody);

		KeypairDAO keypairDAO = DAO.keypair();
		Keypair signingKeypair = keypairDAO.get_by_id(incomingMessage.get_cert_id());

		if (signingKeypair == null)
		{
			t.response.send(new InfoMessage(15, "Key pair not found"), 400);
			return;
		}

		byte[] payload = incomingMessage.get_payload();
		HashAlgorithm hashAlgo = incomingMessage.get_hash_algorithm();
		SignatureFormat signatureFormat = incomingMessage.get_signature_format();
		byte[] signedMessage = null;
		boolean outputBase64 = true;

		switch (signatureFormat)
		{
			case PKCS1:
				signedMessage = SignatureFactory.sign_pkcs1(payload, signingKeypair, hashAlgo);
				break;
			case PKCS7:
				boolean attachPayload = incomingMessage.attach_payload();
				boolean includeCAChain = incomingMessage.append_chain();
				signedMessage = SignatureFactory.sign_pkcs7(payload, signingKeypair, hashAlgo, attachPayload, includeCAChain);
				break;
			case PADES_CMS:
			case PADES_BES:
				signedMessage = SignatureFactory.sign_pades(payload, signingKeypair, hashAlgo, signatureFormat);
				break;
			case XMLDSIG:
				signedMessage = SignatureFactory.sign_xmldsig(payload, signingKeypair, incomingMessage.xmldsig_node_id(), hashAlgo, signatureFormat);
				outputBase64 = false;
				break;
			default:
				t.response.send(new InfoMessage(14, "Unknown signature type specified"), 400);
				return;
		}

		SignResponseMessage response = new SignResponseMessage(signedMessage, incomingMessage.get_signature_format(), signingKeypair.getIdentifier(), outputBase64);
		t.response.send(response, 200);
	}

}
