package com.rongzer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCEECPrivateKey;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.rongzer.blockchain.common.RBCRuntimeException;
import com.rongzer.blockchain.orguser.RBCPeerOrg;
import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.blockchain.security.ISecurityCertReadWrite;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.blockchain.security.SecurityCertReadWrite;
import com.rongzer.rdp.common.service.RDPUtil;
import com.security.cipher.sm.SM2;

/**
 * RBC工具类
 * @author Administrator
 *
 */
public class RBCUtils {
	private static Logger log4j = Logger.getLogger(RBCUtils.class.getName());
	
    private static String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }
    }
    
	/**
	 * GRPC校验与TLS自动处理
	 * @param location
	 * @return
	 */
    private static String grpcTLSify(String location) {
        location = location.trim();
        boolean runTLS = tlsEnabled();
        Exception e = Utils.checkGrpcUrl(location);
        if (e != null) {
            throw new RuntimeException(String.format("Bad TEST parameters for grpc url %s", location), e);
        }
        return runTLS ?location.replaceFirst("^grpc://", "grpcs://") : location;

    }
    
    /**
     * HTTP的TLS自动处理
     * @param location
     * @return
     */
    private static String httpTLSify(String location) {
        location = location.trim();
        boolean runTLS = tlsEnabled();

        return runTLS ?location.replaceFirst("^http://", "https://") : location;
    }
	/**
	 * 构建新的交易ID
	 * @return
	 */
	public static String getTXID()
	{
		return java.util.UUID.randomUUID().toString();
	}
	
	/**
	 * 构建新的UUID
	 * @return
	 */
	public static String getUUID()
	{
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	private static Properties rpcProps = null;
	
	/**
	 * 获取application.properties中配置的参数
	 * @param key
	 * @return
	 */
	public static String getRBCProperties(String key)
	{
		String strReturn = "";
		try
		{
			//开发时设置
			rpcProps = null;
			if (rpcProps == null)
			{
				rpcProps = new Properties();
				try
				{
					Resource resource = new ClassPathResource("/rbc.properties");
					rpcProps = PropertiesLoaderUtils.loadProperties(resource);
				}catch(Exception e)
				{
			        InputStream in = Object.class.getResourceAsStream("/rbc.properties");  
			        rpcProps.load(in);  
		            in.close();  
				}				

			}

			if (rpcProps != null)
			{
				strReturn = rpcProps.getProperty(key);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return strReturn;
	}
	
	/**
	 * 打开TLS安全通道
	 * @return
	 */
	public static boolean tlsEnabled()
	{
		boolean bReturn = true;
    	if (!"true".equals(RBCUtils.getRBCProperties("tls.enabled")))
    	{
    		bReturn = false;
    	} 
    	return bReturn;
	}
	
	/**
	 * 从cert/peeradmin下获取管理用户
	 * @return RBCUser
	 */
	public static RBCUser getPeerAminUser()
	{
		RBCUser returnUser = null;
		try
		{
			RBCPeerOrg rbcPeerOrg = getRBCPeerOrg();
	
			String userName = rbcPeerOrg.getName()+"Admin";
			RBCUser rbcUser = new RBCUser(userName,rbcPeerOrg.getName());
			rbcUser.setAccount(userName);
			rbcUser.setMspId(rbcPeerOrg.getMSPID());
			rbcUser.setSecurityType("ecdh");
			rbcUser.setEnrollmentSecret(userName);
			
	        Resource certResource = new ClassPathResource("/cert/peeradmin/cert.pem");
			File certFile = certResource.getFile();
			InputStream inCertFile = new FileInputStream(certFile);
	        String certificate = new String(IOUtils.toByteArray(inCertFile), "UTF-8");
	        inCertFile.close();
	        Resource privateResource = new ClassPathResource("/cert/peeradmin/private");
			File privateFile = privateResource.getFile();
	        //PrivateKey privateKey = getPrivateKeyFromFile(privateKeyFile);
			InputStream inPrivateFile = new FileInputStream(privateFile);

	        PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(inPrivateFile));
	        inPrivateFile.close();
	        rbcUser.setEnrollment(privateKey, certificate);
	        returnUser = rbcUser;
		}catch(Exception e)
		{
			//e.printStackTrace();
		}
        
		return returnUser;
	}
	
	/**
	 * 从私钥文件解释PrivateKey对象
	 * @return RBCUser
	 */
    public static PrivateKey getPrivateKeyFromBytes(byte[] data){
    	PrivateKey privateKey = null;
    	try
    	{
	        final PEMParser pemParser = new PEMParser(new StringReader(new String(data)));
	        PrivateKeyInfo pemPair = (PrivateKeyInfo) pemParser.readObject();
	        
	        //国密算法处理
	        if (pemPair.getPrivateKeyAlgorithm() != null &&pemPair.getPrivateKeyAlgorithm().getParameters() != null 
	        		&& pemPair.getPrivateKeyAlgorithm().getParameters().toString().equals("1.2.156.10197.1.301"))
	        {
	        	ASN1Sequence seq = ASN1Sequence.getInstance(pemPair.parsePrivateKey());
	            ASN1OctetString octs = (ASN1OctetString)seq.getObjectAt(1);
	            BigInteger d = new BigInteger(1,octs.getOctets());
	            
	            ECPrivateKeyParameters priParams = new ECPrivateKeyParameters(d,SM2.Instance().ecc_bc_spec);
	            privateKey = new JCEECPrivateKey("SM2WithSM3",priParams);
	            
	            return privateKey;
	        }
	        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	        Object aa = pemPair.parsePrivateKey();
	        privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        return privateKey;
    }
	
	/**
	 * 从rbc.properties中取组网参数配置构建RBCPeerOrg
	 * @return
	 */
	public static RBCPeerOrg getRBCPeerOrg()
	{

		String peerOrg = getRBCProperties("peerOrg");
		if (StringUtil.isEmpty(peerOrg))
		{
			peerOrg = "RBCOrg";
		}
		
		String mspId =getRBCProperties("mspId");
		if (StringUtil.isEmpty(mspId))
		{
			mspId = "RBCOrgMSP";
		}
		
		RBCPeerOrg rbcPeerOrg = new RBCPeerOrg(peerOrg,mspId);

		String peerLocations = getRBCProperties("peer.locations");
		if (StringUtil.isEmpty(peerLocations))
		{
			peerLocations = "peer1.peer.com@grpc://192.168.1.234:7051";
		}
		
		String eventhubLocations = getRBCProperties("eventhub.locations");
		if (StringUtil.isEmpty(eventhubLocations))
		{
			eventhubLocations = "peer1.peer.com@grpc://192.168.1.234:7053";
		}
		
		String firstPeerName = "";
		
        String[] ps = peerLocations.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
            String[] nl = peer.split("[ \t]*@[ \t]*");
            String location = grpcTLSify(nl[1]);
            if (StringUtil.isEmpty(firstPeerName))
            {
            	firstPeerName = nl[0];
            }
            rbcPeerOrg.addPeerLocation(nl[0],location);
        }

        ps = eventhubLocations.split("[ \t]*,[ \t]*");
        for (String eventhub : ps) {
            String[] nl = eventhub.split("[ \t]*@[ \t]*");
            rbcPeerOrg.setPeerEvent(nl[0], grpcTLSify(nl[1]));
        }
        		
        if (tlsEnabled()) {
        	String domainName = getDomainName(firstPeerName);            
            String filePath = "/";
            try{
                Resource resourcePath = new ClassPathResource("/");

            	filePath =  resourcePath.getFile().getPath();
            	
            }catch(Exception e)
            {
            	RBCUtils.exception("get resource root path error");
            }
            String cert = filePath + "/rbccrypto-config/peerOrganizations/"+domainName+"/ca/ca."+domainName+"-cert.pem";
            File cf = new File(cert);
            if (!cf.exists() || !cf.isFile()) {
            	RBCUtils.exception("TEST is missing cert file " + cf.getAbsolutePath());
            }
            Properties properties = new Properties();
            properties.setProperty("pemFile", cf.getAbsolutePath());

            properties.setProperty("allowAllHostNames", "true"); //testing environment only NOT FOR PRODUCTION!

            rbcPeerOrg.setCAProperties(properties);
        }
        
		return rbcPeerOrg;
	}

	
	/**
	 * 获取rbc.properties的channel.name通道名称配置，默认是rbcchannel
	 * @return
	 */
	public static String getChannelName()
	{
		String channelName = getRBCProperties("channel.name");
		if (StringUtil.isEmpty(channelName))
		{
			channelName = "rbcchannel";
		}
		
		return channelName;
		
	}
	
	/**
	 * 字符串转整型
	 * @param str
	 * @return
	 */
	public static int str2Int(String str)
	{
		int nReturn = -1;
		
		try
		{
			nReturn = Integer.parseInt(str);
		}catch(Exception e)
		{
			
		}
		return nReturn;
	}
	
	
	/**
	 * RBC通用的异常处理
	 * @param msg
	 */
	public static void exception(String msg)
	{
		throw new RBCRuntimeException(msg);
	}
	
	private static String INVOKE_EVENT = null;
	
	/**
	 *  获取rbc.properties的事件监听配置，默认存在事件监听，压力测试时请设置成false
	 * @return
	 */
	public static boolean invokeEvent()
	{
		boolean invokeEvent = true;
		if (INVOKE_EVENT == null)
		{
			INVOKE_EVENT = getRBCProperties("invoke.event");
			if (INVOKE_EVENT == null)
			{
				INVOKE_EVENT = "true";
			}
		}
		if ("false".equals(INVOKE_EVENT))
		{
			invokeEvent = false;
		}
		return invokeEvent;
	}
	
	/**
	 * 获取证书读写类对象，获建新的证书读写类请实现ISecurityCertReadWrite
	 * 默认的证书读写类是com.rongzer.blockchain.security.SecurityCertReadWrite,实现了文件的读写
	 * @return
	 */
	public static ISecurityCertReadWrite getSecurityCertReadWrite()
	{
		ISecurityCertReadWrite iscr = null;
		
		try
		{
			String loadClass = getRBCProperties("security.loadclass");
			if (StringUtil.isEmpty(loadClass) || loadClass.trim().equals("com.rongzer.blockchain.security.SecurityCertReadWrite"))
			{
				log4j.info("loadClass :" + loadClass + "success .");
				iscr = new SecurityCertReadWrite(); 
			}else
			{
				log4j.info("loadClass :" + loadClass + " , will get newInstance .");
		        Class iscrClass = Class.forName(loadClass.trim());
		        iscr = (ISecurityCertReadWrite)iscrClass.newInstance();
			}

		}catch(Exception e)
		{
			log4j.error(e.getMessage()+":"+e.getCause());
		}
		return iscr;
	}
	
	/**
	 * 获取选择证书信息，从对象转证书
	 * @return
	 */
	public static SecurityCert getSecurityCert(Object objCert)
	{
		if (objCert != null && objCert instanceof SecurityCert)
		{
			return (SecurityCert)objCert;
		}
		
		return null;
	}
	
	/**
	 * 按用户获取证书信息如file/admin
	 * @param saveType
	 * @param customerNo
	 * @return
	 */
	public static SecurityCert getSecurityCert(String saveType,String customerNo)
	{
		ISecurityCertReadWrite iscr = getSecurityCertReadWrite();
		SecurityCert securityCert = null;
		
		if (iscr == null)
		{
			return null;
		}
		
		securityCert = iscr.readCert(saveType, customerNo);

		return securityCert;
	}
	
	/**
	 * 列出所有证书
	 * @return
	 */
	public static List<String> lisCert()
	{
		List<String> lisCert = new ArrayList<String>();
		ISecurityCertReadWrite iscr = getSecurityCertReadWrite();		
		
		if (iscr == null)
		{
			return lisCert;
		}
		
		return iscr.lisCert();
	}
	
	private static SecurityCert defaultSecurityCert = null;
	
	/**
	 * 获取默认RBC用户
	 * @return
	 */
	public static SecurityCert getDefaultSecurityCert(){
		if (defaultSecurityCert != null) {
			return defaultSecurityCert;
		}
		
		List<String> lisCerts = lisCert();
		if (lisCerts.size()>0){
			String defaultKey = lisCerts.get(0);
			String[] defaultKeys = defaultKey.split(":");
			if (defaultKeys.length == 2){
				defaultSecurityCert = getSecurityCert(defaultKeys[0],defaultKeys[1]);
				return defaultSecurityCert;
			}
		}
		return null;
	}
	
	public static String publicKeyToByte(ECPublicKey ecPublicKey){
		String publicX = Hex.toHexString(ecPublicKey.getW().getAffineX().toByteArray());
		String publicY = Hex.toHexString(ecPublicKey.getW().getAffineY().toByteArray());
		if (publicX.length()>64){
			publicX = publicX.substring(publicX.length()-64);
		}
		if (publicY.length()>64){
			publicY = publicY.substring(publicY.length()-64);
		}
		return "04"+publicX+publicY;
	}	
	
	/**
	 * hash混合
	 * @param in1
	 * @param in2
	 * @return
	 */
	public static byte[] hash(byte[] in1,byte[] in2 ){
		byte[] bReturn = null;

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(in1, 0, in1.length);			
			messageDigest.update(in2, 0, in2.length);
			bReturn = messageDigest.digest();
		} catch (Exception  e) {
			e.printStackTrace();
		}
		
		if (bReturn == null || bReturn.length<32) {
			RDPUtil.exception("get p2 hash exception");
		}
		return bReturn;
	}
	
	/**
	 * 判断Base64字符串是否加密
	 * @param msg
	 * @return
	 */
	public static boolean isCrypto(String msg)
	{
		boolean bReturn = false;
		if (msg != null  && msg.startsWith("/ty6A"))
		{
			bReturn = true;
		}
		return bReturn;
	}
	
	/**
	 * 判断是否是加密串
	 * @param msg
	 * @return
	 */
	public static boolean isCrypto(byte[] msg)
	{
		boolean bReturn = false;
		if (msg != null && msg.length >3 
				&& msg[0] == (byte)0xFE&& msg[1] == (byte)0xDC&& msg[2] == (byte)0xBA&& msg[3] == (byte)0x00)
		{
			bReturn = true;
		}
		return bReturn;
	}
	
	
	/**
	 * 加密
	 * @param msg
	 * @return
	 */
	public static byte[] enc(byte[] cryptogram,byte[] msg){
		if (msg == null || msg.length<1 || isCrypto(msg)){
			return msg;
		}
		byte[] bReturn = new byte[msg.length+4];
		bReturn[0] = (byte)0xFE;
		bReturn[1] = (byte)0xDC;
		bReturn[2] = (byte)0xBA;
		bReturn[3] = (byte)0x00;

		int c = 0;
		byte[] p2 =hash(cryptogram,(""+c).getBytes());

		int pos = 0;
		for (int i=0;i<msg.length;i++){
			bReturn[i+4] = msg[i];			
			bReturn[i+4] ^= p2[pos];
			pos ++;
			if (pos >= 32 ){
				c ++;
				p2 = hash(cryptogram,(""+c).getBytes());
				pos = 0;
			}
		}
		
		return bReturn;
	}
	
	/**
	 * 解密
	 * @param msg
	 * @return
	 */
	public static byte[] dec(byte[] cryptogram,byte[] msg){
		if (msg == null || msg.length<5 || !isCrypto(msg)){
			return msg;
		}
		
		int c = 0;

		byte[] p2 =hash(cryptogram,(""+c).getBytes());
		byte[] bReturn = new byte[msg.length-4];

		int pos = 0;
		for (int i=4;i<msg.length;i++){
			bReturn[i-4] = msg[i];			
			bReturn[i-4] ^= p2[pos];	
			pos ++;
			if (pos >= 32 ){
				c ++;
				p2 = hash(cryptogram,(""+c).getBytes());
				pos = 0;
			}
		}
		
		return bReturn;
	}
	
	
	private static KeyPair gKeyPair = null;
	
	/**
	 * 进程级，密码公钥
	 * @return
	 */
	public static String getPubG(){
		String pubG = "";
		try{
			if (gKeyPair == null){
				CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
				gKeyPair = cryptoSuite.keyGen();
	
			}
			pubG =  publicKeyToByte((ECPublicKey)gKeyPair.getPublic());
		}catch(Exception e){
			
		}
		return pubG;
	}
	/**
	 * 进程级，对密码进行解密
	 * @param bGram
	 * @param mainPub
	 * @return
	 */
	public static byte[] decCryptogram(byte[] bGram,ECPublicKey mainPub){
		try{
			if (gKeyPair == null){
				CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
				gKeyPair = cryptoSuite.keyGen();
			}
			
			RBCEncrypto rbcEncrypto= new RBCEncrypto(mainPub,(ECPrivateKey)gKeyPair.getPrivate());
			byte[] theGram = rbcEncrypto.getCryptogram("", "", "", "");
			byte[] decGram = dec(theGram,bGram);
			return decGram;
		}catch(Exception e){
			
		}

		return bGram;
	}
	
	/**
	 * 进程级，对密码进行加密
	 * @param bGram
	 * @param mainPub
	 * @return
	 */
	public static byte[] encCryptogram(byte[] bGram,ECPrivateKey mainPri,ECPublicKey gPub){
		try{
			RBCEncrypto rbcEncrypto= new RBCEncrypto(gPub,mainPri);
			byte[] theGram = rbcEncrypto.getCryptogram("", "", "", "");
			byte[] encGram = enc(theGram,bGram);

			return encGram;
		}catch(Exception e){
			
		}

		return bGram;
	}
}
