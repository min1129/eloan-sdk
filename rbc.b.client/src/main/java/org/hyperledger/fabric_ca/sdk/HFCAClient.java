/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.hyperledger.fabric_ca.sdk;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.hyperledger.fabric.protos.common.Rbc.RBCHttpRequest;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;
import org.hyperledger.fabric_ca.sdk.exception.RevocationException;
import org.hyperledger.fabric_ca.sdk.helper.Config;

import com.google.protobuf.ByteString;
import com.rongzer.blockchain.client.ChannelClient;

/**
 * HFCAClient Hyperledger Fabric Certificate Authority Client.
 */
public class HFCAClient {
    private static final Log logger = LogFactory.getLog(HFCAClient.class);
    Config config = Config.getConfig(); //Load config so enable logging setting.
    private static final String HFCA_CONTEXT_ROOT = "/api/v1/";
    private static final String HFCA_ENROLL = HFCA_CONTEXT_ROOT + "enroll";
    private static final String HFCA_REGISTER = HFCA_CONTEXT_ROOT + "register";
    private static final String HFCA_REENROLL = HFCA_CONTEXT_ROOT + "reenroll";
    private static final String HFCA_REVOKE = HFCA_CONTEXT_ROOT + "revoke";

    static final String FABRIC_CA_REQPROP = "caname";

    private final Properties properties;

    // TODO require use of CryptoPrimitives since we need the generateCertificateRequests methods
    // clean this up when we do have multiple implementations of CryptoSuite
    // see FAB-2628
    private CryptoSuite cryptoSuite;

    /**
     * HFCAClient constructor
     *
     * @param url        Http URL for the Fabric's certificate authority services endpoint
     * @param properties PEM used for SSL .. not implemented.
     *                   <p>
     *                   Supported properties
     *                   <ul>
     *                   <li>pemFile - File location for x509 pem certificate for SSL.</li>
     *                   <li>allowAllHostNames - boolen(true/false) override certificates CN Host matching -- for development only.</li>
     *                   </ul>
     * @throws MalformedURLException
     */
    HFCAClient(Properties properties) throws MalformedURLException {
     
        if (properties != null) {
            this.properties = (Properties) properties.clone(); //keep our own copy.
        } else {
            this.properties = null;
        }
    }

    public static HFCAClient createNewInstance(Properties properties) throws MalformedURLException {

        return new HFCAClient(properties);
    }


    public void setCryptoSuite(CryptoSuite cryptoSuite) {
        this.cryptoSuite =  cryptoSuite;
        try {
        	cryptoSuite.init();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    /**
     * Register a user.
     *
     * @param request   Registration request with the following fields: name, role.
     * @param registrar The identity of the registrar (i.e. who is performing the registration).
     * @return the enrollment secret.
     * @throws RegistrationException    if registration fails.
     * @throws InvalidArgumentException
     */

    public String register(RegistrationRequest request, User registrar) throws RegistrationException, InvalidArgumentException {

        if (Utils.isNullOrEmpty(request.getEnrollmentID())) {
            throw new InvalidArgumentException("EntrollmentID cannot be null or empty");
        }

        if (registrar == null) {
            throw new InvalidArgumentException("Registrar should be a valid member");
        }
        logger.debug(format("registrar: %s", registrar.getName()));
        
        try {
            String body = request.toJson();
            String authHdr = getHTTPAuthCertificate(registrar.getName(),registrar.getEnrollment(), body);
            JsonObject resp = httpPost(HFCA_REGISTER, body, authHdr);
            String secret = resp.getString("secret");
            if (secret == null) {
                throw new Exception("secret was not found in response");
            }
            logger.debug(format("registrar: %s done.", registrar));
            return secret;
        } catch (Exception e) {
            RegistrationException registrationException = new RegistrationException(format("Error while registering the user %s %s ", registrar,e.getMessage()), e);
            logger.error(registrar);
            throw registrationException;
        }
    }

    /**
     * Enroll the user with member service
     *
     * @param user   Identity name to enroll
     * @param secret Secret returned via registration
     * @return enrollment
     * @throws EnrollmentException
     * @throws InvalidArgumentException
     */
    public Enrollment enroll(String user, String secret) throws EnrollmentException, InvalidArgumentException {
        return enroll(user, secret, new EnrollmentRequest());
    }

    /**
     * Enroll the user with member service
     *
     * @param user   Identity name to enroll
     * @param secret Secret returned via registration
     * @param req    Enrollment request with the following fields: hosts, profile, csr, label, keypair
     * @return enrollment
     * @throws EnrollmentException
     * @throws InvalidArgumentException
     */

    public Enrollment enroll(String user, String secret, EnrollmentRequest req) throws EnrollmentException, InvalidArgumentException {

        logger.debug(format("enroll user: %s",user));

        if (Utils.isNullOrEmpty(user)) {
            throw new InvalidArgumentException("enrollment user is not set");
        }
        if (Utils.isNullOrEmpty(secret)) {
            throw new InvalidArgumentException("enrollment secret is not set");
        }

        try {
            String pem = req.getCsr();
            KeyPair keypair = null;
            //如果pem是空，则自动创建pem
            if (pem == null) {
            	keypair = req.getKeyPair();
            	//如果keypair是空，则自动创建kepair
				if (keypair == null) {
					keypair = cryptoSuite.keyGen();
				}
 
                PKCS10CertificationRequest csr = cryptoSuite.generateCertificationRequest(user, keypair);
                pem = cryptoSuite.certificationRequestToPEM(csr);
                req.setCSR(pem);
            }

            String body = req.toJson();

            String responseBody = httpPost(HFCA_ENROLL, body,
                    new UsernamePasswordCredentials(user, secret));

            logger.debug("response:" + responseBody);

            JsonReader reader = Json.createReader(new StringReader(responseBody));
            JsonObject jsonst = (JsonObject) reader.read();

            boolean success = jsonst.getBoolean("success");
            logger.debug(format("[HFCAClient] enroll success:[%s]", success));

            if (!success) {
                throw new EnrollmentException(format("FabricCA failed enrollment for user %s response success is false.", user));
            }

            JsonObject result = jsonst.getJsonObject("result");
            if (result == null) {
                throw new EnrollmentException(format("FabricCA failed enrollment for user %s - response did not contain a result", user));
            }

            Base64.Decoder b64dec = Base64.getDecoder();

            String signedPem = new String(b64dec.decode(result.getString("Cert").getBytes(UTF_8)));
            logger.debug(format("[HFCAClient] enroll returned pem:[%s]", signedPem));

            JsonArray messages = jsonst.getJsonArray("messages");
            if (messages != null && !messages.isEmpty()) {
                JsonObject jo = messages.getJsonObject(0);
                String message = format("Enroll request response message [code %d]: %s", jo.getInt("code"), jo.getString("message"));
                logger.info(message);
            }
            logger.debug("Enrollment done.");
            
            PrivateKey privateKey = null ;
            if (keypair != null){
            	privateKey = keypair.getPrivate();
            }
            
            return new RBCEnrollment(privateKey, signedPem);

        } catch (EnrollmentException ee) {
            logger.error(format("user:%s  error:%s", user, ee.getMessage()), ee);
            throw ee;
        } catch (Exception e) {
            EnrollmentException ee = new EnrollmentException(format("Failed to enroll user %s ", user), e);
            logger.error(e.getMessage(), e);
            throw ee;
        }

    }

    /**
     * Re-Enroll the user with member service
     *
     * @param user User to be re-enrolled
     * @param userName
     * @return enrollment
     * @throws EnrollmentException
     * @throws InvalidArgumentException
     */
    public Enrollment reenroll(User user,String userName) throws EnrollmentException, InvalidArgumentException {
        return reenroll(user,userName, new EnrollmentRequest());
    }

    /**
     * Re-Enroll the user with member service
     *
     * @param user User to be re-enrolled
     * @param userName
     * @param req  Enrollment request with the following fields: hosts, profile, csr, label
     * @return enrollment
     * @throws EnrollmentException
     * @throws InvalidArgumentException
     */

    public Enrollment reenroll(User user,String userName, EnrollmentRequest req) throws EnrollmentException, InvalidArgumentException {

        if (user == null) {
            throw new InvalidArgumentException("reenrollment user is missing");
        }
        if (user.getEnrollment() == null) {
            throw new InvalidArgumentException("reenrollment user is not a valid user object");
        }

        logger.debug(format("re-enroll user: %s", user.getName()));

        try {
            String pem = req.getCsr();
            KeyPair keypair = null;
            //如果pem是空，则自动创建pem
            if (pem == null) {
            	//如果keypair是空，则自动创建kepair
				if (keypair == null) {
					keypair = cryptoSuite.keyGen();
				}
	            PKCS10CertificationRequest csr = cryptoSuite.generateCertificationRequest(userName, keypair);
	            pem = cryptoSuite.certificationRequestToPEM(csr);
                req.setCSR(pem);

            }       
            String body = req.toJson();

            // build authentication header
            String authHdr = getHTTPAuthCertificate(user.getName(),user.getEnrollment(), body);
            JsonObject result = httpPost(HFCA_REENROLL, body, authHdr);

            // get new cert from response
            Base64.Decoder b64dec = Base64.getDecoder();
            String signedPem = new String(b64dec.decode(result.getString("Cert").getBytes(UTF_8)));
            logger.debug(format("[HFCAClient] re-enroll returned pem:[%s]", signedPem));

            logger.debug(format("reenroll user %s done.", user.getName()));
            PrivateKey privateKey = null ;
            if (keypair != null){
            	privateKey = keypair.getPrivate();
            }
            
            return new RBCEnrollment(privateKey, signedPem);

        } catch (EnrollmentException ee) {
            logger.error(ee.getMessage(), ee);
            throw ee;
        } catch (Exception e) {
            EnrollmentException ee = new EnrollmentException(format("Failed to re-enroll user %s", user), e);
            logger.error(e.getMessage(), e);
            throw ee;
        }
    }

    /**
     * revoke one enrollment of user
     *
     * @param revoker    admin user who has revoker attribute configured in CA-server
     * @param enrollment the user enrollment to be revoked
     * @param reason     revoke reason, see RFC 5280
     * @throws RevocationException
     * @throws InvalidArgumentException
     */

    public void revoke(User revoker, Enrollment enrollment, String reason) throws RevocationException, InvalidArgumentException {

        if (enrollment == null) {
            throw new InvalidArgumentException("revokee enrollment is not set");
        }
        if (revoker == null) {
            throw new InvalidArgumentException("revoker is not set");
        }

        logger.debug(format("revoke revoker: %s, reason: %s", revoker.getName(), reason));

        try {

            // get cert from to-be-revoked enrollment
            BufferedInputStream pem = new BufferedInputStream(new ByteArrayInputStream(enrollment.getCert().getBytes()));
            CertificateFactory certFactory = CertificateFactory.getInstance(Config.getConfig().getCertificateFormat());
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(pem);

            // get its serial number
            String serial = DatatypeConverter.printHexBinary(certificate.getSerialNumber().toByteArray());

            // get its aki
            // 2.5.29.35 : AuthorityKeyIdentifier
            byte[] extensionValue = certificate.getExtensionValue(Extension.authorityKeyIdentifier.getId());
            ASN1OctetString akiOc = ASN1OctetString.getInstance(extensionValue);
            String aki = DatatypeConverter.printHexBinary(AuthorityKeyIdentifier.getInstance(akiOc.getOctets()).getKeyIdentifier());

            // build request body
            RevocationRequest req = new RevocationRequest("", null, serial, aki, reason);
            String body = req.toJson();

            String authHdr = getHTTPAuthCertificate(revoker.getName(),revoker.getEnrollment(), body);

            // send revoke request
            httpPost(HFCA_REVOKE, body, authHdr);
            logger.debug("revoke done");
        } catch (CertificateException e) {
            logger.error("Cannot validate certificate. Error is: " + e.getMessage());
            throw new RevocationException("Error while revoking cert. " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RevocationException("Error while revoking the user. " + e.getMessage(), e);

        }
    }

    /**
     * revoke one user (including his all enrollments)
     *
     * @param revoker admin user who has revoker attribute configured in CA-server
     * @param revokee user who is to be revoked
     * @param reason  revoke reason, see RFC 5280
     * @throws RevocationException
     * @throws InvalidArgumentException
     */

    public void revoke(User revoker, String revokee, String reason) throws RevocationException, InvalidArgumentException {

        logger.debug(format("revoke revoker: %s, revokee: %s, reason: %s", revoker, revokee, reason));

        if (Utils.isNullOrEmpty(revokee)) {
            throw new InvalidArgumentException("revokee user is not set");
        }
        if (revoker == null) {
            throw new InvalidArgumentException("revoker is not set");
        }

        try {
            // build request body
            RevocationRequest req = new RevocationRequest("", revokee, null, null, reason);
            String body = req.toJson();

            // build auth header
            String authHdr = getHTTPAuthCertificate(revoker.getName(),revoker.getEnrollment(), body);

            // send revoke request
            httpPost( HFCA_REVOKE, body, authHdr);
            logger.debug(format("revoke revokee: %s done.", revokee));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RevocationException("Error while revoking the user. " + e.getMessage(), e);
        }
    }
    
    /**
     * Http Post Request.
     *
     * @param url         Target URL to POST to.
     * @param body        Body to be sent with the post.
     * @param credentials Credentials to use for basic auth.
     * @return Body of post returned.
     * @throws Exception
     */
    String httpPost(String url, String body, UsernamePasswordCredentials credentials) throws Exception {
    	RBCHttpRequest httpRequest = RBCHttpRequest.newBuilder()
    			.setType("CA")
    			.setMethod("POST")
    			.setEndpoint(url)
    			.setBody(ByteString.copyFromUtf8(body))
    			.putHeaders("user", credentials.getUserName())
    			.putHeaders("secret", credentials.getPassword())
    			.build();
    			
    	byte[] bReturn = ChannelClient.httpRequest(httpRequest) ;
    	String strReturn = new String(bReturn,"UTF-8");    			

    	return strReturn;
    }

    JsonObject httpPost(String url, String body, String authHTTPCert) throws Exception {

    	RBCHttpRequest httpRequest = RBCHttpRequest.newBuilder()
    			.setType("CA")
    			.setMethod("POST")
    			.setEndpoint(url)
    			.setBody(ByteString.copyFromUtf8(body))
    			.putHeaders("Authorization", authHTTPCert)
    			.build();
    			
    	byte[] bReturn = ChannelClient.httpRequest(httpRequest) ;
    	
    	String strReturn= new String(bReturn,"UTF-8");
    	
        JsonReader reader = Json.createReader(new StringReader(strReturn));
        JsonObject jobj = (JsonObject) reader.read();
        boolean success = jobj.getBoolean("success");
        if (!success) {
            EnrollmentException e = new EnrollmentException(
                    format("POST request to %s failed request body %s Body of response did not contain success", url, body),
                    new Exception());
            logger.error(e.getMessage());
            throw e;
        }
        JsonObject result = jobj.getJsonObject("result");
        if (result == null) {
            EnrollmentException e = new EnrollmentException(format("POST request to %s failed request body %s " +
                    "Body of response did not contain result", url, body), new Exception());
            logger.error(e.getMessage());
            throw e;
        }
        JsonArray messages = jobj.getJsonArray("messages");
        if (messages != null && !messages.isEmpty()) {
            JsonObject jo = messages.getJsonObject(0);
            String message = format("POST request to %s failed request body %s response message [code %d]: %s",
                    url, body, jo.getInt("code"), jo.getString("message"));
            logger.info(message);
        }

        logger.debug(format("httpPost %s, body:%s result: %s", url, body, "" + result));
        return result;
    }

    private String getHTTPAuthCertificate(String userId,Enrollment enrollment, String body) throws Exception {
        if (userId == null){
        	userId = "";
        }
    	Base64.Encoder b64 = Base64.getEncoder();
        String cert = b64.encodeToString(enrollment.getCert().getBytes(UTF_8));
        body = b64.encodeToString(body.getBytes(UTF_8));
        String signString = body + "." + cert;
        byte[] signature = cryptoSuite.sign(enrollment.getKey(),userId.getBytes(), signString.getBytes(UTF_8));
        return cert + "." + b64.encodeToString(signature);
    }


}

