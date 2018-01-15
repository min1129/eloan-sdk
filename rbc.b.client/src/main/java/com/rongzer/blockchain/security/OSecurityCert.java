package com.rongzer.blockchain.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

import com.rongzer.blockchain.common.ByteUtils;

/**
 * 证书数据对象
 * @author Administrator
 *
 */
public class OSecurityCert implements Serializable {
	
	private String securityType = "ecdh";//ecdh,sm2,usbkey
	
	//USBKEY专用
	private String devicePath = "";
	
	private String customerId = "";

	private String customerNo = "";
	
	private byte[] privateKey = null;
	
	private byte[] publicKey = null;
	
	private byte[] publicCert = null;

	public OSecurityCert()
	{
		
	}

	public String getSecurityType() {
		return securityType;
	}
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getCustomerNo() {
		return customerNo;
	}
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}
	public byte[] getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}
	public byte[] getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	} 		
	public byte[] getPublicCert() {
		return publicCert;
	}
	
	public String getDevicePath() {
		return devicePath;
	}

	public void setDevicePath(String devicePath) {
		this.devicePath = devicePath;
	}
	
    /**
     * 拼装公钥CERT
     * @param key
     * @return
     */
    public byte[] MarshalPKIXPublicKey()
    {
    	byte[] b = null;
    	
    	try
    	{
    		if ("ecdh".equals(securityType))
    		{
	    		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	             DERSequenceGenerator seq = new DERSequenceGenerator(baos);
	             //org.bouncycastle.asn1.ASN1ObjectIdentifier
	             ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier("1.2.840.10045.2.1");
	             ASN1ObjectIdentifier pid = new ASN1ObjectIdentifier("1.2.840.10045.3.1.7");	
	
	             AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier( oid,pid);
	             DERBitString derBitString = new DERBitString(publicKey);
	
	             seq.addObject(algorithmIdentifier);
	             seq.addObject(derBitString);
	             seq.close();
	             b = baos.toByteArray();
    		}else
    		{
	       		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	             DERSequenceGenerator seq = new DERSequenceGenerator(baos);
	             //org.bouncycastle.asn1.ASN1ObjectIdentifier
	             ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier("1.2.840.10045.2.1");
	             ASN1ObjectIdentifier pid = new ASN1ObjectIdentifier("1.2.156.10197.1.301");	
	
	             AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier( oid,pid);
	             DERBitString derBitString = new DERBitString(publicKey);
	
	             seq.addObject(algorithmIdentifier);
	             seq.addObject(derBitString);
	             seq.close();
	             
	             b = baos.toByteArray();
    		}
             
    	}catch(Exception e)
    	{
    		
    	}
    	
    	return b;
    	
    }

	public void setPublicCert(byte[] publicCert) {
		/*
		ASN1InputStream dis1 = null;
		try
		{
			ByteArrayInputStream bis1 = new ByteArrayInputStream(publicCert);
		    dis1 = new ASN1InputStream(bis1);
		    DLSequence seq1 = (DLSequence) dis1.readObject();
		    System.out.println(seq1.toString());
		    if (seq1.size()>0 && seq1.getObjectAt(0) instanceof DLSequence)
		    {
		    	DLSequence seq2 = (DLSequence)seq1.getObjectAt(0);
		    	if (seq2.size()>6 && seq2.getObjectAt(6) instanceof DLSequence)
		    	{
		    		DLSequence seq3 = (DLSequence)seq2.getObjectAt(6);
		    		if (seq3.size()>1 && seq3.getObjectAt(1) instanceof DERBitString)
		    		{
		    			DERBitString derBitString = (DERBitString)seq3.getObjectAt(1);
		    			this.publicKey = derBitString.getOctets();
		    		}
		    	}
		    }
			this.publicCert = publicCert;

		}catch(Exception e)
		{
			
		}finally
		{
			if (dis1 != null)
			{
				dis1.close();
				dis1 = null;
			}
		}*/
		try
		{
			ByteArrayInputStream bis1 = new ByteArrayInputStream(publicCert);
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		    X509Certificate cer = (X509Certificate) fact.generateCertificate(bis1);
		    PublicKey key = cer.getPublicKey();
		    bis1.close();
		    this.publicCert = publicCert;
			ByteArrayInputStream bis2 = new ByteArrayInputStream(key.getEncoded());
			ASN1InputStream dis1 = new ASN1InputStream(bis2);
		    DLSequence seq1 = (DLSequence) dis1.readObject();
		    bis2.close();
		    dis1.close();
		    this.publicKey = ((DERBitString)seq1.getObjectAt(1)).getOctets();
		}catch(Exception e)
		{
			
		}  
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (securityType == null)
		{
			securityType = "ecdh";
		}
		if (customerNo == null)
		{
			customerNo = "";
		}
		sb.append("type = ").append(securityType).append("\n");
		sb.append("customerId = ").append(customerId).append("\n");
		sb.append("customerNo = ").append(customerNo).append("\n");
		if (privateKey != null)
		{
			sb.append("privateKey = ").append(ByteUtils.toHex(privateKey)).append("\n");
		}
		
		if (publicCert!= null)
		{
			sb.append("publicCert = ").append(ByteUtils.toHex(publicCert)).append("\n");
		}

		return sb.toString();
	}

}
