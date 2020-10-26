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
		Keypair signingKeypair = keypairDAO.getById(incomingMessage.getCertId());

		if (signingKeypair == null)
		{
			t.response.send(new InfoMessage(15, "Key pair not found"), 400);
			return;
		}

		byte[] payload = incomingMessage.getPayload();
		HashAlgorithm hashAlgo = incomingMessage.getHashAlgorithm();
		SignatureFormat signatureFormat = incomingMessage.getSignatureFormat();
		byte[] signedMessage = null;
		boolean outputBase64 = true;

		switch (signatureFormat)
		{
			case PKCS1:
				signedMessage = SignatureFactory.signPKCS1(payload, signingKeypair, hashAlgo);
				break;
			case PKCS7:
				boolean attachPayload = incomingMessage.attachPayload();
				boolean includeCAChain = incomingMessage.appendChain();
				signedMessage = SignatureFactory.signPKCS7(payload, signingKeypair, hashAlgo, attachPayload, includeCAChain);
				break;
			case PADES_CMS:
			case PADES_BES:
				signedMessage = SignatureFactory.signPAdES(payload, signingKeypair, hashAlgo, signatureFormat);
				break;
			case XMLDSIG:
				signedMessage = SignatureFactory.signXmlDSig(payload, signingKeypair, incomingMessage.xmldsigNodeId(), hashAlgo, signatureFormat);
				outputBase64 = false;
				break;
			default:
				t.response.send(new InfoMessage(14, "Unknown signature type specified"), 400);
				return;
		}

		SignResponseMessage response = new SignResponseMessage(signedMessage, incomingMessage.getSignatureFormat(), signingKeypair.getIdentifier(), outputBase64);
		t.response.send(response, 200);
	}

}
