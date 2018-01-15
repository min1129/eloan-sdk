package com.rongzer.blockchain.security;

import java.io.Serializable;

import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.utils.ByteUtils;

/**
 * 证书数据对象
 * 
 * @author Administrator
 *
 */
public class SecurityCert implements Serializable {

	private String securityType = "ecdh";// ecdh,sm2,usbkey

	// USBKEY专用
	private String devicePath = "";

	private String customerId = "";

	private String customerNo = "";

	private RBCUser rbcUser = null;

	public SecurityCert() {

	}

	public SecurityCert(String securityType, RBCUser rbcUser) {
		this.securityType = securityType;
		customerId = rbcUser.getEnrollmentSecret();
		customerNo = rbcUser.getName();
		this.rbcUser = rbcUser;
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

	public String getDevicePath() {
		return devicePath;
	}

	public void setDevicePath(String devicePath) {
		this.devicePath = devicePath;
	}

	public RBCUser getRbcUser() {
		return rbcUser;
	}

	public void setRbcUser(RBCUser rbcUser) {
		this.rbcUser = rbcUser;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (securityType == null) {
			securityType = "ecdh";
		}
		if (customerNo == null) {
			customerNo = "";
		}
		if (customerId == null) {
			customerId = "";
		}
		
		sb.append("type = ").append(securityType).append("\n");
		sb.append("customerId = ").append(customerId).append("\n");
		sb.append("customerNo = ").append(customerNo).append("\n");
		if (rbcUser != null) {
			sb.append("rbcUser = ").append(ByteUtils.toHex(rbcUser.toByte())).append("\n");
		}

		return sb.toString();
	}

}
