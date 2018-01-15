package com.rongzer.blockchain.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.math.ec.ECPoint;

public class SecurityCommon{

	
	static final String curveName = "secp256r1";
    static final X9ECParameters curve = SECNamedCurves.getByName(curveName);
    static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
    static final BigInteger HALF_CURVE_ORDER = curve.getN().shiftRight(1);
    static final String CERTIFICATE_FORMAT = "";
    
    /**
     * 用私钥进行签名
     * @param hash
     * @param privateKey
     * @return
     */
    private byte[] signMsg(byte[] msg, SecurityCert securityCert) {
    	
    	
    	  X9ECParameters params = NISTNamedCurves.getByName("P-256");
          BigInteger curve_N = params.getN();

          ECDomainParameters ecParams = new ECDomainParameters(params.getCurve(), params.getG(), curve_N,
                  params.getH());
    	PrivateKey  privateKey = securityCert.getRbcUser().getEnrollment().getKey();
    	ECPrivateKey ecPrivateKey = (ECPrivateKey)privateKey;
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, new ECPrivateKeyParameters(ecPrivateKey.getS(), ecParams));
        BigInteger[] signature = signer.generateSignature(msg);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DERSequenceGenerator seq = new DERSequenceGenerator(baos);
            ASN1Integer R = new ASN1Integer(signature[0]);
            ASN1Integer S = new ASN1Integer(toCanonicalS(signature[1]));
            seq.addObject(R);
            seq.addObject(S);
            seq.close();
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
    
    /**
     * 用公钥进行校验签名校验
     * @param hash
     * @param signature
     * @param publicKey
     * @return
     */
    private boolean verifyMsg(byte[] msg, byte[] signature, String cert) {
        ASN1InputStream asn1 = new ASN1InputStream(signature);
        try {
            BufferedInputStream pem = new BufferedInputStream(new ByteArrayInputStream(cert.getBytes()));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(pem);
            ECPublicKey ecPublicKey = (ECPublicKey)certificate.getPublicKey();  
            byte[] x = ecPublicKey.getW().getAffineX().toByteArray();
            byte[] y = ecPublicKey.getW().getAffineY().toByteArray();
            //ECPoint point = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
            ECPoint point  = curve.getCurve().createPoint(new BigInteger(1, x), new BigInteger(1, y));
            //ECPublicKeyParameters eckp = new ECPublicKeyParameters(curve.getCurve().decodePoint(certificate.getPublicKey().getEncoded()), domain);
            ECDSASigner signer = new ECDSASigner();
            
            signer.init(false, new ECPublicKeyParameters(point, domain));

            DLSequence seq = (DLSequence) asn1.readObject();
            BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getPositiveValue();
            BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getPositiveValue();
            return signer.verifySignature(msg, r, s);
        } catch (Exception e) {
            return false;
        } finally {
            try {
                asn1.close();
            } catch (IOException ignored) {
            }
        }
    }
    
    
    /**
     * 用私钥进行签名 for fabric 1.0-0430
     * @param hash
     * @param privateKey
     * @return
     */
    public byte[] sign(byte[] msg, SecurityCert securityCert) {
    	return signMsg(hash(msg),securityCert);
    }
    
  
    
    /**
     * 验签 for fabric 1.0-0430
     * @param msg
     * @param signature
     * @param cert
     * @return
     */
    public boolean verify(byte[] msg, byte[] signature, String cert) {
    	 return verifyMsg(hash(msg),signature,cert);
    }
  

    
    
 	/***
 	 * MD5加码 生成32位md5码
 	 */
 	public byte[] hash(byte[] md5Bytes) {
 		byte[] b = null;
 		try {
 			SHA3.DigestSHA3 md = new SHA3.DigestSHA3(256);
 			md.update(md5Bytes);

 			b = md.digest();

 		} catch (Exception e) {
 		}

 		return b;

 	}
     
     private static BigInteger toCanonicalS(BigInteger s) {
         if (s.compareTo(HALF_CURVE_ORDER) <= 0) {
             return s;
         } else {
             return curve.getN().subtract(s);
         }
     }

	

}
