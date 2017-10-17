package com.rongzer.rbc.init;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import protos.Fabric;

import com.rongzer.blockchain.client.BlockChainApiClient;
import com.rongzer.blockchain.client.DevopsClient;
import com.rongzer.blockchain.client.MemberServiceClient;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.chaincode.entity.CustomerEntity;
import com.rongzer.rdp.common.service.RDPUtil;
import com.rongzer.rdp.common.util.StringUtil;
import com.rongzer.utils.JSONUtil;
import com.rongzer.utils.RBCUtils;

public class RBCPlatformInit {

	static int nPeerPort;
	static String peerAddress;
	static int nMemberServicePort;
	static String memberServiceAddress;

	static String AdminUserId = "RBCAdminUser";
	
	public static void main(String[] args) {
		//1、检测系统连接情
		//2、
		nPeerPort = RBCUtils.str2Int(RBCUtils.getRBCProperties("peerPort"));
		if (nPeerPort <0)
		{
			nPeerPort = 30303;
		}
		
		peerAddress = RBCUtils.getRBCProperties("peerAddress");
		
		nMemberServicePort = RBCUtils.str2Int(RBCUtils.getRBCProperties("memberServicePort"));
		if (nMemberServicePort <0)
		{
			nMemberServicePort = 50051;
		}
		
		memberServiceAddress =  RBCUtils.getRBCProperties("memberServiceAddress");
		if (StringUtil.isEmpty(peerAddress))
		{
			System.out.println("rbc.properties中peerAddress未指定！");
			return;
		}
		
		if (StringUtil.isEmpty(memberServiceAddress))
		{
			System.out.println("rbc.properties中memberServiceAddress未指定！");
			return;
		}
		
		String controllerName = queryControllerName();
		if (StringUtil.isEmpty(controllerName))
		{
			System.out.println("controlerName未获取，请检测节点的core.yaml配置！");
			return;
		}
		System.out.println("controlerName = "+controllerName);
		
		//查询管理用户
		String adminUserId = queryRBCAdminUser(controllerName);
		if (adminUserId == null)
		{
			System.out.println("rbccontroller或rbccustomer未运行");
			return;
		}
		
		if (StringUtil.isNotEmpty(adminUserId))
		{
			System.out.println("rbcAdminUser己经被创建，系统不能够再次初始化！");
			return;
		}
		
		//创建rbcAdminUser
		SecurityCert securityCert = createRBCAdminUser(controllerName);
		
		if (securityCert == null)
		{
			System.out.println("创建rbcAdminUser用户失败！");
			return;
		}
		
		System.out.println("创建并注册rbcAdminUser用户成功！");

		 System.out.println(securityCert.toString());

	}
	
	
	public static String queryControllerName()
	{
		String strReturn = "";
			
		BlockChainApiClient blockChainApiClient = new BlockChainApiClient(peerAddress,nPeerPort);
				
		strReturn = blockChainApiClient.getControllerName();
		
		return strReturn;
	}
	
	/**
	 * 通过rbccontroller检测rbccustomer是否否在
	 * @param conrollerName
	 * @return
	 */
	public static String queryRBCAdminUser(String conrollerName)
	{		
		
		String adminUser = "";
		DevopsClient devopsClient = new DevopsClient(peerAddress,nPeerPort);
		
		String customerChainCode = "";
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add("rbccustomer");
		
		Fabric.Response resp = devopsClient.query("jim", conrollerName,"getChainCodeName", lisArgs);
		if (resp == null ||  resp.getStatusValue() != protos.Fabric.Response.StatusCode.SUCCESS_VALUE)
		{
			RDPUtil.exception("rbccontroller运行失败");
		}
		
		if (resp != null && resp.getMsg() != null && StringUtil.isNotEmpty(resp.getMsg().toStringUtf8()))
		{
			customerChainCode = resp.getMsg().toStringUtf8();
			
		}
		
		if (StringUtil.isEmpty(customerChainCode))
		{
			customerChainCode = RBCUtils.getRBCProperties("rbc.rbccustomer");
		}
		
		if (StringUtil.isEmpty(customerChainCode))
		{
			RDPUtil.exception("未找到rbccustomer合约");
		}
		
		//查询是否有RBCAdminUser存在
		List<String> lisArgs1 = new ArrayList<String>();
		lisArgs1.add("{\"customerNo\":\""+AdminUserId+"\"}");

		Fabric.Response resp1 = devopsClient.query("jim", customerChainCode,"queryOne", lisArgs1);
		
		if (resp1 == null || resp1.getStatusValue() != protos.Fabric.Response.StatusCode.SUCCESS_VALUE)
		{
			RDPUtil.exception("rbccustomer合约不存在");
		}
		
		if (resp1 != null && resp1.getMsg() != null && StringUtil.isNotEmpty(resp1.getMsg().toStringUtf8()))
		{
			JSONObject jData = JSONUtil.getJSONObjectFromStr(resp.getMsg().toStringUtf8());
			if (jData != null && jData.get("customerId") != null)
			{
				adminUser = jData.get("customerId").toString();
			}			
		}
		
		return adminUser;

	}
	
	/**
	 * 创建RBCAdminUser
	 * @param conrollerName
	 * @return
	 */
	public static SecurityCert createRBCAdminUser(String conrollerName)
	{	
		String customerChainCode = "";
		SecurityCert securityCert = null;
		DevopsClient devopsClient = new DevopsClient(peerAddress,nPeerPort);
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add("rbccustomer");
		
		Fabric.Response resp = devopsClient.query("jim", conrollerName,"getChainCodeName", lisArgs);
		if (resp != null && resp.getMsg() != null && StringUtil.isNotEmpty(resp.getMsg().toStringUtf8()))
		{
			customerChainCode = resp.getMsg().toStringUtf8();
			
		}
		
		if (StringUtil.isEmpty(customerChainCode))
		{
			customerChainCode = RBCUtils.getRBCProperties("rbc.rbccustomer");
		}
		
		if (StringUtil.isEmpty(customerChainCode))
		{
			RDPUtil.exception("未找到rbccustomer合约");
		}
		
		String userId = AdminUserId;

		MemberServiceClient memberServiceClient = new MemberServiceClient(memberServiceAddress,nMemberServicePort);
		
		protos.Ca.Token token = memberServiceClient.registAdminUser(userId);
		if (token == null || token.getTok() == null)
		{
			RBCUtils.exception("regist user can't get token,please check memberservice or user is exist ");
		}
		
		System.out.println("userId:"+userId +" token:"+token.toString());
		//检查新的key
		securityCert = RBCUtils.getSecurityCert("usbkey");
		if (securityCert != null && securityCert.getCustomerNo().length()<1)
		{
			securityCert.setCustomerNo(userId);
		}
		
		if (securityCert == null || !securityCert.getCustomerNo().equals(userId) || securityCert.getPublicKey() != null)
		{
			String securityType = RBCUtils.getRBCProperties("security.type");
			if (StringUtil.isEmpty(securityType))
			{
				securityType = "ecdh";
			}
			
			//取默认的文件型
			securityCert = RBCUtils.getSecurityCommon(securityType).genKeyPair(userId);
		}
		
		securityCert = memberServiceClient.enrollUser(token,securityCert);
		
		if (securityCert == null || securityCert.getPublicCert() == null)
		{
			RBCUtils.exception("enroll User error");
		}

		//注册用户信息
		CustomerEntity customerEntity = new CustomerEntity();
		customerEntity.setCustomerId(StringUtil.getUuid32());
		customerEntity.setCustomerNo(userId);
		customerEntity.setCustomerName("默认超级管理用户");
		customerEntity.setCustomerType("1");//创建超级用户
		customerEntity.setCustomerStatus("3");
		customerEntity.setCustomerSignCert(ByteUtils.toHexString(securityCert.getPublicCert()));
		
		//2、注册用户,设置公钥
		List<String> lisArgs1 = new ArrayList<String>();
		lisArgs1.add(customerEntity.toJSON().toString());
		
		Fabric.Response resp1 = devopsClient.invoke("jim", customerChainCode,"register", lisArgs1);

		if (resp1 == null)
		{
			RBCUtils.exception("regist RBCAdminUser to rbccustomer error");
		}
	

		return securityCert;

	}
	
	private static void testRegist()
	{
		DevopsClient devopsClient = new DevopsClient(peerAddress,nPeerPort);

		String customerId = StringUtil.getUuid32();
		String jData = "{\"customerId\":\"\",\"customerNo\":\"RBCAdminUser\",\"customerName\":\"默认超级管理用户\",\"customerStatus\":\"3\",\"customerAuth\":\"0\",\"customerType\":\"1\",\"customerSignCert\":\"308201b83082015ea003020102020101300a06082a8648ce3d0403033031310b300906035504061302555331143012060355040a130b48797065726c6564676572310c300a06035504031303656361301e170d3136313232343032353032325a170d3137303332343032353032325a3048310b300906035504061302555331143012060355040a130b48797065726c65646765723123302106035504030c1a52424341646d696e557365725c696e737469747574696f6e5f613059301306072a8648ce3d020106082a8648ce3d03010703420004ee5b750ecd44e9fa9cb07f88ecced49c742de680c6205e36e85ce0f360bedbc6957bc524a8fd24be7d471d5fe12644be05251205663df66bf77e8028e5e10df7a350304e300e0603551d0f0101ff040403020780300c0603551d130101ff04023000300d0603551d0e0406040401020304300f0603551d2304083006800401020304300e06065103040506070101ff040131300a06082a8648ce3d0403030348003045022100fb0f017d4d2f29d050eb286e8bbc3fa5b780fdb32c26090118683b777e1d9a9e02200787cf439a105caab8e88e7a41260ea573e71c6615de7dd76d49ebb9c3cf57eb\",\"registTime\":\"\"}";
		
		CustomerEntity customerEntity = new CustomerEntity(JSONUtil.getJSONObjectFromStr(jData));
		customerEntity.setCustomerId(customerId);
		

		//2、注册用户,设置公钥
		List<String> lisArgs1 = new ArrayList<String>();
		lisArgs1.add(customerEntity.toJSON().toString());
		
		Fabric.Response resp1 = devopsClient.invoke("jim", "rbccustomer","register", lisArgs1);

		if (resp1 == null)
		{
			RBCUtils.exception("regist RBCAdminUser to rbccustomer error");
		}
	
	}

}
