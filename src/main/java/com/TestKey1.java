package com;

import org.hyperledger.common.ByteUtils;

import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.blockchain.security.USBKeySecurityCommon;

public class TestKey1 {

	public static void main(String[] args) {
		
		String customerNo = "wangjian";
		USBKeySecurityCommon sc = new USBKeySecurityCommon();
		SecurityCert securityCert = sc.genKeyPair(customerNo);
		if (securityCert == null || securityCert.getPublicKey() == null)
		{
			System.out.println("key 初始化出错");
			return;
		}
		String msg = "abc";

		System.out.println("SecurityCommon public key info:"+ ByteUtils.toHex(securityCert.getPublicKey()));
		
		byte[] scSign = sc.sign(msg.getBytes(),securityCert);
		System.out.println("SecurityCommon sign:"+ ByteUtils.toHex(scSign));
		
		boolean bVer1 = sc.verify(msg.getBytes(), scSign, securityCert);
		System.out.println("SecurityCommon sign verify:"+bVer1);


	}

	
}
