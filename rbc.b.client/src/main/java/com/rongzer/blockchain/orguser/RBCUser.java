/*
 *  Copyright 2016 DTCC, Fujitsu Australia Software Technology - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rongzer.blockchain.orguser;

import io.netty.util.internal.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.List;
import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class RBCUser implements User, Serializable {
    private static final long serialVersionUID = 8077132186383604355L;
        
    //密码算法类型
	private String securityType = "ecdh";//ecdh,sm2,usbkey
	
	//USBKEY专用
	private String devicePath = "";

    //用户名
    private String name;
    //用户角色 
    private Set<String> roles;
    //帐号名
    private String account;
    //用户附属信息
    private String affiliation;
    //用户归属组织
    private String organization;
    //用户ID
    private String enrollmentSecret;
    //用户公私钥信息
    Enrollment enrollment = null; //need access in test env.

    public RBCUser(String name, String org) {
        this.name = name;

        this.organization = org;
    }

    public String getSecurityType() {
		return securityType;
	}

	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	public String getDevicePath() {
		return devicePath;
	}

	public void setDevicePath(String devicePath) {
		this.devicePath = devicePath;
	}

	/**
     * Get the user name.
     *
     * @return {string} The user name.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Get the roles.
     *
     * @return {string[]} The roles.
     */
    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    /**
     * Set the roles.
     *
     * @param roles {string[]} The roles.
     */
    public void setRoles(Set<String> roles) {

        this.roles = roles;
    }

    /**
     * Get the account.
     *
     * @return {String} The account.
     */
    @Override
    public String getAccount() {
        return this.account;
    }

    /**
     * Set the account.
     *
     * @param account The account.
     */
    public void setAccount(String account) {

        this.account = account;
    }

    /**
     * Get the affiliation.
     *
     * @return {string} The affiliation.
     */
    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    /**
     * Set the affiliation.
     *
     * @param affiliation The affiliation.
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * Get the enrollment logger.info.
     *
     * @return {Enrollment} The enrollment.
     */
    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    /**
     * Determine if this name has been registered.
     *
     * @return {boolean} True if registered; otherwise, false.
     */
    public boolean isRegistered() {
        return !StringUtil.isNullOrEmpty(enrollmentSecret);
    }

    /**
     * Determine if this name has been enrolled.
     *
     * @return {boolean} True if enrolled; otherwise, false.
     */
    public boolean isEnrolled() {
        return this.enrollment != null;
    }


    private String getAttrsKey(List<String> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return null;
        }
        return String.join(",", attrs);
    }


    /**
     * Make sure we're not dependent of HFCAEnrollment class.
     */
    class AfakeEnrollmentImplementation implements Enrollment{

        private final PrivateKey privateKey;
        private final String certificate;

        public AfakeEnrollmentImplementation(PrivateKey privateKey, String certificate ) {
            this.privateKey = privateKey;
            this.certificate = certificate;
        }

        @Override
        public PrivateKey getKey() {
            return privateKey;
        }

        @Override
        public String getCert() {
            return certificate;
        }

    }
    

    public String getEnrollmentSecret() {
        return enrollmentSecret;
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
    }


    public void setEnrollment(Enrollment enrollment) {

        this.enrollment = enrollment;

    }
    
    public void setEnrollment(PrivateKey privateKey,String cert)
    {
    	this.enrollment = new AfakeEnrollmentImplementation(privateKey,cert);
    }


    public static String toKeyValStoreName(String name, String org) {
        return "user." + name + org;
    }


    @Override
    public String getMspId() {
        return mspID;
    }

    String mspID;

    public void setMspId(String mspID) {
        this.mspID = mspID;
    }
    
    public byte[] toByte()
    {
    	byte[] bUser = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();
			bos.close();
			bUser = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bUser;
    }
}
