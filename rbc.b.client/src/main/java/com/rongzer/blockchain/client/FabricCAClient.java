package com.rongzer.blockchain.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import com.rongzer.blockchain.orguser.RBCPeerOrg;
import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.rdp.common.util.StringUtil;
import com.rongzer.utils.RBCUtils;

/**
 * Fabric CA操作类
 * @author Administrator
 *
 */
public class FabricCAClient {
	
    private static final Log logger = LogFactory.getLog(FabricCAClient.class);
    
    /**
     * 创建管理用户adminUser/adminPwd由启动fabricca时指定
     * @param ca
     * @param adminUser
     * @param adminPwd
     * @return
     */
	public static RBCUser createAdminUser(HFCAClient ca , String adminUser , String adminPwd)
	{		
		 RBCPeerOrg rbcPeerOrg = RBCUtils.getRBCPeerOrg();
		 RBCUser userReturn = null;
		 final String orgName = rbcPeerOrg.getName();
		 final String mspid = rbcPeerOrg.getMSPID();
		 try
		 {
			 RBCUser admin = new RBCUser(adminUser,orgName);
		     if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
		
		         admin.setEnrollment(ca.enroll(admin.getName(), adminPwd));
		         admin.setMspId(mspid);
		     }
		     userReturn = admin;
		 }catch(Exception e)
		 {
			 e.printStackTrace();
			 logger.error("create admin user error", e);
		 }
		 return userReturn;
	}
	
	/**
	 * 创建普通用户
	 * @param ca
	 * @param userName
	 * @param adminUser
	 * @param csrPem
	 * @return
	 */
	public static RBCUser createUser(HFCAClient ca ,String userName , RBCUser adminUser,String csrPem)
	{
		RBCUser userReturn = null;
		RBCPeerOrg rbcPeerOrg = RBCUtils.getRBCPeerOrg();		
		try
		{
	        RBCUser user = new RBCUser(userName,rbcPeerOrg.getName());
	        if (!user.isRegistered()) {  // users need to be registered AND enrolled
	            RegistrationRequest rr = new RegistrationRequest(userName, "org1.department1");
	            user.setEnrollmentSecret(ca.register(rr, adminUser));
	        }
	        if (!user.isEnrolled()) {
	        	EnrollmentRequest req = new EnrollmentRequest();
	        	//代表存在证书请求并且格式正确
	        	if (StringUtil.isNotEmpty(csrPem) && csrPem.indexOf("CERTIFICATE REQUEST") >0){
	        		req.setCsr(csrPem);
	        	}
	        	Enrollment enrollment = ca.enroll(user.getName(), user.getEnrollmentSecret(),req);

	            user.setEnrollment(enrollment);
	            user.setMspId(rbcPeerOrg.getMSPID());
	        }
	        userReturn = user;
		}catch(Exception e)
		{
			 logger.error("create user error", e);
			 RBCUtils.exception(e.getMessage());
		}
		return userReturn;
	}
	
	/**
	 * reenroll用户证书
	 * @param ca
	 * @param userName
	 * @param adminUser
	 * @param csrPem
	 * @return
	 */
	public static RBCUser reenroll(HFCAClient ca ,String userName , RBCUser adminUser,String csrPem)
	{
		RBCUser userReturn = null;
		RBCPeerOrg rbcPeerOrg = RBCUtils.getRBCPeerOrg();		
		try
		{
			RBCUser user = new RBCUser(userName,rbcPeerOrg.getName());
        	EnrollmentRequest req = new EnrollmentRequest();
        	//代表存在证书请求并且格式正确
        	if (StringUtil.isNotEmpty(csrPem) && csrPem.indexOf("CERTIFICATE REQUEST") >0){
        		req.setCsr(csrPem);
        	}
        	Enrollment enrollment = ca.reenroll(adminUser, userName, req);
            user.setEnrollment(enrollment);
            user.setMspId(rbcPeerOrg.getMSPID());
	      
	        userReturn = user;
		}catch(Exception e)
		{
			 logger.error("reenroll user error", e);
			 RBCUtils.exception(e.getMessage());
		}
		return userReturn;
	}
}
