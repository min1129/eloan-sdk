package com.rongzer.blockchain.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;

import com.rongzer.blockchain.common.ByteUtils;
import com.rongzer.utils.StringUtil;
import com.security.cipher.sm.SM2;
import com.security.cipher.sm.SM2Utils;

public class SM2SecurityCommon implements ISecurityCommon{

	static final SM2 sm2 = SM2.Instance();

    /**
     * 获取私值的D值
     * @return
     */
	public OSecurityCert genKeyPair(String customerNo)
	{
        ECKeyPairGenerator generator = sm2.ecc_key_pair_generator;
        //ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(sm2.ecc_bc_spec, secureRandom);
        //generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        
        byte[] privateKey = privParams.getD().toByteArray();
        byte[] publicKey = sm2.ecc_point_g.multiply(new BigInteger(privateKey)) .getEncoded(false);
        OSecurityCert securityCert = new OSecurityCert();
        securityCert.setCustomerId(StringUtil.getUuid32());

        securityCert.setSecurityType("sm2");
        securityCert.setPrivateKey(privateKey);
        securityCert.setPublicKey(publicKey);
        securityCert.setCustomerNo(customerNo);
        
        return securityCert;
	}

    /**
     * 用私钥进行签名
     * @param hash
     * @param privateKey
     * @return
     */
    public byte[] signMsg(byte[] msg, OSecurityCert securityCert) {
       
        try {
        	return SM2Utils.sign(securityCert.getCustomerNo().getBytes(), securityCert.getPrivateKey(), msg);            
        } catch (IOException e) {
            return new byte[0];
        }
    }
    
    /**
     * 用私钥进行签名
     * @param hash
     * @param privateKey
     * @return
     */
    public byte[] sign(byte[] msg, OSecurityCert securityCert) {
    	System.out.println("msg hash:"+ByteUtils.toHex(hash(msg)));
    	return signMsg(hash(msg),securityCert);
    }

    /**
     * 用公钥进行校验签名校验
     * @param hash
     * @param signature
     * @param publicKey
     * @return
     */
    public boolean verifyMsg(byte[] msg, byte[] signature, OSecurityCert securityCert) {
        try {
        	
        	return SM2Utils.verifySign(securityCert.getCustomerNo().getBytes(), securityCert.getPublicKey(), msg, signature);          
        } catch (Exception e) {
            return false;
        } 
    }
    
    /**
     * 用公钥进行校验签名校验
     * @param hash
     * @param signature
     * @param publicKey
     * @return
     */
    public boolean verify(byte[] msg, byte[] signature, OSecurityCert securityCert) {
    	return verifyMsg(hash(msg),signature,securityCert);

    }
    
  
    
    /**
     * 用公钥Encode对数据进行加密
     * @param data
     * @param key
     * @return
     */
    public byte[] encryptByPublic( byte[] data,OSecurityCert securityCert)
    {
    	byte[] bReturn = null;
    	try{
    		bReturn = SM2Utils.encrypt(securityCert.getPublicKey(), data);    		
    	}catch(Exception e)
    	{
    		
    	}
    	return bReturn;
	}
    
    
    /**
     * 用私钥的D值对数据进行解密
     * @param data
     * @param key
     * @return
     */
    public byte[] decryptByPrivate( byte[] data,OSecurityCert securityCert)
    {
    	byte[] bReturn = null;
    	try{
    		bReturn = SM2Utils.decrypt(securityCert.getPrivateKey(), data);    		
    	}catch(Exception e)
    	{
    		
    	}
    	return bReturn;    
    }
    
    
	/***
	 * MD5加码 生成32位md5码
	 */
	public byte[] hash(byte[] md5Bytes) {
		byte[] b = null;
		try {
			
			b= SM2Utils.getMsgHash(md5Bytes);

		} catch (Exception e) {
		}

		return b;

	}

	@Override
	public PublicKey getPublicKey(byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
