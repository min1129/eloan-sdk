package com.rongzer.blockchain.security;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.rongzer.blockchain.orguser.RBCPeerOrg;
import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.utils.ByteUtils;
import com.rongzer.utils.FileUtil;
import com.rongzer.utils.RBCUtils;
import com.rongzer.utils.StringUtil;
import com.softkey.jsyunew4;

public class SecurityCertReadWrite implements ISecurityCertReadWrite{

	Logger log = Logger.getLogger(SecurityCertReadWrite.class);
	/**
	 * 读取证书信息
	 */
	@Override
	public SecurityCert readCert(String securityType,String customerNo) {
		
		if (customerNo == null)
		{
			return null;
		}
		RBCPeerOrg rbcPeerOrg = RBCUtils.getRBCPeerOrg();
		SecurityCert securityCert = new SecurityCert();
		securityCert.setSecurityType(securityType);

		//从propertity文件取证书
		if ("usbkey".equals(securityType))
		{
			String devicePath = jsyunew4.FindPort(0);
			if (devicePath != null && !devicePath.startsWith("not"))
			{
				securityCert.setDevicePath(devicePath);
				//尝试读取证书
				byte[] publicCert = jsyunew4.ReadSM2Cert(devicePath);
				
			}
			
		}else
		{
			String securityFile =customerNo;
			if (StringUtil.isNotEmpty(customerNo) && !customerNo.startsWith("user."))
			{
				securityFile = "user."+rbcPeerOrg.getName()+"."+customerNo+".cert";
			}

			
			if (!securityFile.endsWith(".cert"))
			{
				securityFile +=".cert";
			}

			
			Properties certProp = readCert(securityFile);
			if (certProp != null)
			{
				securityCert.setSecurityType(certProp.getProperty("type"));
				securityCert.setCustomerId(certProp.getProperty("customerId"));
				securityCert.setCustomerNo(certProp.getProperty("customerNo"));

	            try {
					byte[] bUser = ByteUtils.fromHex(certProp.getProperty("rbcUser"));
					//序列化RBCUser
					ByteArrayInputStream bis = new ByteArrayInputStream(bUser);
					ObjectInputStream ois = new ObjectInputStream(bis);
	                RBCUser rbcUser = (RBCUser) ois.readObject();
	                ois.close();
	                bis.close();
	                securityCert.setRbcUser(rbcUser);
	            }catch(Exception e1)
	            {
	            	e1.printStackTrace();
	            }
			}
		}
		
		return securityCert;
	}

	@Override
	public boolean writeCert(SecurityCert securityCert) {
		
		if (securityCert == null)
		{
			log.error("securityCert is null");
			return false;
		}
		RBCPeerOrg rbcPeerOrg = RBCUtils.getRBCPeerOrg();

		if ("usbkey".equals(securityCert.getSecurityType()))
		{
			
		}else //保存至文件
		{
			String securityFile = "user."+rbcPeerOrg.getName()+"."+securityCert.getCustomerNo()+".cert";;
			if (StringUtil.isEmpty(securityFile))
			{
				securityFile = "default.cert";
			}
			
			if (securityFile.indexOf(".")<0)
			{
				securityFile +=".cert";
			}
			log.info("cert file name is : " + securityFile);
			Resource resource = new ClassPathResource("/cert");
			
			try {
				String filePath =resource.getFile().getAbsolutePath() +"/"+securityFile;
				log.info("write cert path is : " + filePath);
				FileUtil.writeTextFile(filePath, securityCert.toString());
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return false;
			}		
		}
		
		return true;
	}
	
	/**
	 * 获取application.properties中配置的参数
	 * @param key
	 * @return
	 */
	protected static Properties readCert(String fileName)
	{
		Properties prop = null;
		//兼容老的证书类型
		try
		{
			try
			{
				Resource resource = new ClassPathResource("/cert/"+fileName);
				prop = PropertiesLoaderUtils.loadProperties(resource);
			}catch(Exception e)
			{
				prop = new Properties();
		        InputStream in = Object.class.getResourceAsStream("/cert/"+fileName);  
		        prop.load(in);  
	            in.close();  
			}
		}catch(Exception e)
		{
			prop = null;
		}
		
		if (prop != null ){
			return prop;
		}
		
		
		return prop;
	}

	@Override
	public List<String> lisCert() {
		List<String> lisCert = new ArrayList<String>();
		/*
		//查找usbkey
		int k=0;
		try
		{
			while(k>=0)
			{
				String devicePath = jsyunew4.FindPort(k);
				if (StringUtil.isNotEmpty(devicePath) && !devicePath.startsWith("not"))
				{
					k++;
					byte[] bPubkey = jsyunew4.GetPubKey(devicePath);
					if (bPubkey.length>=64)
					{
	
						byte[] bUserName = ByteUtils.subBuf(bPubkey,64,bPubkey.length-64);
						String sm2UserName = new String(bUserName);
						if (StringUtil.isNotEmpty(sm2UserName))
						{
							lisCert.add("usbkey:"+sm2UserName);
						}
					}else
					{
						k=-1;
						break;
					}
				}
			}
		}catch(Exception e)
		{
			
		}
		*/
		//查找文件
		try
		{
			Resource resource = new ClassPathResource("/cert");
			File theFile = resource.getFile();
			File[] arrFile = theFile.listFiles();
			
			for (int i=0;i<arrFile.length;i++)
			{
				if (arrFile[i].getName().endsWith(".cert"))
				{
					String fileName = arrFile[i].getName();
					fileName = fileName.substring(0,fileName.length()-5);
					lisCert.add("file:"+fileName);
				}
			}
		}catch(Exception e)
		{
			
		}
		// TODO Auto-generated method stub
		return lisCert;
	}
	
	
}
