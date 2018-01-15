package com.rongzer.blockchain.security;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface ISecurityCommon {
	
	/**
	 * 创建公私钥串
	 * @return
	 */
	public OSecurityCert genKeyPair(String customerNo);	
    
    public PublicKey getPublicKey(byte[] key);

    public PrivateKey getPrivateKey(byte[] key);

    
    /**
     * 私钥签名
     * @param hash
     * @param securityCert
     * @return
     */
    public byte[] sign(byte[] msg, OSecurityCert securityCert);

    
    /**
     * 私钥签名
     * @param hash
     * @param securityCert
     * @return
     */
    public byte[] signMsg(byte[] msg, OSecurityCert securityCert);

    /**
     * 公钥验证
     * @param hash
     * @param signature
     * @param securityCert
     * @return
     */
    public boolean verifyMsg(byte[] msg, byte[] signature, OSecurityCert securityCert);

    
    /**
     * 公钥验证
     * @param hash
     * @param signature
     * @param securityCert
     * @return
     */
    public boolean verify(byte[] msg, byte[] signature, OSecurityCert securityCert);

    /**
     * 公钥加密
     * @param data
     * @param securityCert
     * @return
     */
    public byte[] encryptByPublic( byte[] data,OSecurityCert securityCert);
    
    
    /**
     * 用私钥的D值对数据进行解密
     * @param data
     * @param securityCert
     * @return
     */
    public byte[] decryptByPrivate( byte[] data,OSecurityCert securityCert);
    
	/***
	 * MD5加码 生成32位md5码
	 */
	public byte[] hash(byte[] md5Bytes);

}
