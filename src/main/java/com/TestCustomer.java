package com;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DLSequence;
import org.hyperledger.common.ByteUtils;

import com.adobe.xmp.impl.Base64;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.utils.RBCUtils;

public class TestCustomer {
	public static void main(String[] args) {
		
		//SecurityCert securityCert = RBCUtils.getSecurityCommon("sm2").genKeyPair("test001");
		SecurityCert securityCert = new SecurityCert();
		securityCert.setSecurityType("sm2");
		securityCert.setPublicKey(ByteUtils.fromHex("04f25250f79de2b36d0963a0b34bd3e09b774b5e9afa07fb83f55ddf39ece41a68774f8d3d1b23762f0170449b2923ab679a17abcfc81f071af9ec8d3f9a412b6b"));
		securityCert.setPrivateKey(ByteUtils.fromHex("60c716042c609ac80c676594f5e1325a59ffe8a86c6968f38135fd4bff09f586"));
		
		
		try
		{
			System.out.println("PKIX:"+ByteUtils.toHex(securityCert.MarshalPKIXPublicKey()));
			byte[] bMsg = ByteUtils.fromHex("0a0608d3a39bc505120e0a0c52424341646d696e557365721a0e0a0c755a78714945626274696746225d125b3059301306072a8648ce3d020106082a811ccf5501822d03420004f25250f79de2b36d0963a0b34bd3e09b774b5e9afa07fb83f55ddf39ece41a68774f8d3d1b23762f0170449b2923ab679a17abcfc81f071af9ec8d3f9a412b6b2a5d125b3059301306072a8648ce3d020106082a811ccf5501822d03420004f25250f79de2b36d0963a0b34bd3e09b774b5e9afa07fb83f55ddf39ece41a68774f8d3d1b23762f0170449b2923ab679a17abcfc81f071af9ec8d3f9a412b6b");
			System.out.println("MSG:"+ByteUtils.toHex(bMsg));

			byte[] bSign = RBCUtils.getSecurityCommon("sm2").sign(bMsg, securityCert);
			bSign = ByteUtils.fromHex("3045022100c2bbb37c1c3d7d39ff560a9537ccb3008e7679d3e038628ab9e6a2bb40acbe6902200fe76dd2b607b5ed394c3bae7ff699672cf9ae02d6d673036f22e9b9b358305d");
			
			System.out.println("SIGN:"+ByteUtils.toHex(bSign));

			boolean bVer = RBCUtils.getSecurityCommon("sm2").verify(bMsg, bSign, securityCert);
			System.out.println("verify:"+bVer);

		
			String certValue = "308201b83082015ea003020102020101300a06082a8648ce3d0403033031310b300906035504061302555331143012060355040a130b48797065726c6564676572310c300a06035504031303656361301e170d3137303232313038333032385a170d3137303532323038333032385a3048310b300906035504061302555331143012060355040a130b48797065726c65646765723123302106035504030c1a52424341646d696e557365725c696e737469747574696f6e5f613059301306072a8648ce3d020106082a8648ce3d03010703420004f25250f79de2b36d0963a0b34bd3e09b774b5e9afa07fb83f55ddf39ece41a68774f8d3d1b23762f0170449b2923ab679a17abcfc81f071af9ec8d3f9a412b6ba350304e300e0603551d0f0101ff040403020780300c0603551d130101ff04023000300d0603551d0e0406040401020304300f0603551d2304083006800401020304300e06065103040506080101ff040131300a06082a8648ce3d0403030348003045022100c20bfe3921742626b0051efb980ea5e47e428f1668009d099c19bb88e1bcd2f102205944b33fa23044dd7e74e2fced6f5fa28fef10f6e3d9ca7894376d7a85d39ec1";
			ByteArrayInputStream bis2 = new ByteArrayInputStream(ByteUtils.fromHex(certValue));
			ASN1InputStream dis2 = new ASN1InputStream(bis2);
		    DLSequence seq1 = (DLSequence) dis2.readObject();
		    
		    ByteArrayInputStream bis1 = new ByteArrayInputStream(ByteUtils.fromHex(certValue));
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		    X509Certificate cer = (X509Certificate) fact.generateCertificate(bis1);
		    
		    System.out.println(seq1.toString());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		/*
		String customerNo = StringUtil.getUuid32();
		int nMemberServicePort = RBCUtils.str2Int(RBCUtils.getRBCProperties("memberServicePort"));
		if (nMemberServicePort <0)
		{
			nMemberServicePort = 50051;
		}
		MemberServiceClient memberServiceClient = new MemberServiceClient(RBCUtils.getRBCProperties("memberServiceAddress"));
		memberServiceClient.registAdminUser("RBCAdminUser");*/
	}
}
