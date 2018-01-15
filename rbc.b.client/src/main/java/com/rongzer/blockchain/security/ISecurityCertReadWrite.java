package com.rongzer.blockchain.security;

import java.util.List;

/**
 * 证书读取对象
 * @author Administrator
 *
 */
public interface ISecurityCertReadWrite {
	
	public SecurityCert readCert(String securityType,String customerNo);

	public boolean writeCert(SecurityCert securityCert);
	
	public List<String> lisCert();	
	
}
