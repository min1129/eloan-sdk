package com.rongzer.blockchain.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.protobuf.ByteString;
import com.rongzer.blockchain.client.ChainCodeClient;
import com.rongzer.blockchain.client.ChannelClient;
import com.rongzer.blockchain.client.RBCControllerClient;
import com.rongzer.blockchain.orguser.RBCPeerOrg;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.chaincode.entity.MessageEntity;
import com.rongzer.chaincode.entity.PageList;
import com.rongzer.rdp.common.util.JSONUtil;
import com.rongzer.rdp.common.util.StringUtil;
import com.rongzer.utils.RBCUtils;


/**
 * 区块链基础http接口
 * @author yxh
 *
 */
@Controller
@RequestMapping(value="rbc")
public class RBCBlockChainController{
	
	Logger logger = Logger.getLogger(RBCBlockChainController.class);
		
	/**
	 * 执行区块链查询
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "query", method = RequestMethod.POST)
	public @ResponseBody String query(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String chaincodeName = request.getParameter("chaincodeName");
		String funcName = request.getParameter("funcName");
		if (StringUtil.isEmpty(chaincodeName) || StringUtil.isEmpty(funcName)){
			return "query params is error";
		}
		String params = request.getParameter("params");
		List<String> lisArgs = StringUtil.split(params);
		
		RBCControllerClient rbcc = new RBCControllerClient();
		ByteString bReturn = rbcc.query(securityCert, chaincodeName, funcName, lisArgs);
		if (bReturn == null){
			return null;
		}
		return bReturn.toStringUtf8();
	}
	
	/**
	 * 执行区块链提交
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "invoke", method = RequestMethod.POST)
	public @ResponseBody String invoke(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String chaincodeName = request.getParameter("chaincodeName");
		String funcName = request.getParameter("funcName");
		if (StringUtil.isEmpty(chaincodeName) || StringUtil.isEmpty(funcName)){
			return "invoke params is error";
		}
		String params = request.getParameter("params");
		List<String> lisArgs = StringUtil.split(params);
		
		RBCControllerClient rbcc = new RBCControllerClient();
		try{
			String strReturn = rbcc.invokeSync(securityCert, chaincodeName, funcName, lisArgs);
			logger.debug(String.format("execute %:%s params %s return %s",chaincodeName,funcName,params,strReturn));

		}catch(Exception e){
			return "0";
		}

		return "1";
	}
	

	/**
	 * 部署智能合约
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "deploy", method = RequestMethod.POST)
	public @ResponseBody String deploy(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String chaincodeName = request.getParameter("chaincodeName");
		String chaincodeVersion = request.getParameter("chaincodeVersion");
		String chaincodeDesc = request.getParameter("chaincodeDesc");
		String chaincodeSrc = request.getParameter("chaincodeSrc");
		if (StringUtil.isEmpty(chaincodeName) || StringUtil.isEmpty(chaincodeVersion)){
			return "deploy params is error";
		}
		
		if (chaincodeDesc == null){
			chaincodeDesc ="";
		}
		byte[] bSrc = null;
		try{
			bSrc = Base64.decode(chaincodeSrc);
		}catch(Exception e){
			logger.error(e);
			return e.getMessage();
		}
		
		if (bSrc == null || bSrc.length<100){
			return "请上传智能合约tar.gz文件";
		}
		
		RBCControllerClient rbcc = new RBCControllerClient();

		JSONObject jGram = rbcc.getCryptogram(securityCert, chaincodeName, "__CHAINCODE", "", securityCert.getCustomerNo(), "");

		if (jGram != null && jGram.get(chaincodeName)!= null){
			byte[] bCryptogram = Base64.decode(jGram.getString(chaincodeName));
			bSrc = RBCUtils.enc(bCryptogram, bSrc);
		}
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		//初始化chain
		Channel rbcChannel = ChannelClient.getChannel(peerOrg,securityCert.getRbcUser());
		String deployResult = ChainCodeClient.deployChainCode(rbcChannel, securityCert.getRbcUser(),
				Type.JAVA, chaincodeName, chaincodeVersion, chaincodeDesc, bSrc);
	
		if (StringUtil.isNotEmpty(deployResult))
		{
			if (!deployResult.startsWith("OK")){
				return deployResult;
			}
			return "1";
		}

		return "0";
	}
	
	/**
	 * 查询一条消息
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "getMessage", method = RequestMethod.POST)
	public @ResponseBody String getMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String msgId = request.getParameter("msgId");
		if (StringUtil.isEmpty(msgId)){
			return null;
		}
		RBCControllerClient rbcc = new RBCControllerClient();
		MessageEntity messageEntity = rbcc.getMessage(securityCert, msgId);
		if (messageEntity == null){
			return null;
		}
		return messageEntity.toJSON().toString();
	}
	
	/**
	 * 查询消息列表
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "getMessageList", method = RequestMethod.POST)
	public @ResponseBody String getMessageList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String param = request.getParameter("param");
		JSONObject jParams = JSONUtil.getJSONObjectFromStr(param);
		if (jParams == null){
			jParams = new JSONObject();
		}
		int cpno = StringUtil.toInt(jParams.get("cpno"), 0);
		RBCControllerClient rbcc = new RBCControllerClient();
		PageList<MessageEntity> lisReturn = rbcc.getMessageList(securityCert, jParams,cpno);
		if (lisReturn == null){
			return null;
		}
		return lisReturn.toJSON().toString();
	}
	
	/**
	 * 设置一条消息为己读
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "readMessage", method = RequestMethod.POST)
	public @ResponseBody String readMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String msgId = request.getParameter("msgId");
		if (StringUtil.isEmpty(msgId)){
			return null;
		}
		RBCControllerClient rbcc = new RBCControllerClient();
		try{
			rbcc.readMessage(securityCert, msgId);
		}catch(Exception e){
			logger.error(e);
			return "0";
		}
		
		return "1";
	}
	
	/**
	 * 设置一条消息为己读
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "handleMessage", method = RequestMethod.POST)
	public @ResponseBody String handleMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//取默认证书
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
		
		if (securityCert == null){
			return "cant't find blockchain cert file";
		}
		
		String msgId = request.getParameter("msgId");
		if (StringUtil.isEmpty(msgId)){
			return null;
		}
		RBCControllerClient rbcc = new RBCControllerClient();
		try{
			rbcc.handleMessage(securityCert, msgId);
		}catch(Exception e){
			logger.error(e);
			return "0";
		}
		
		return "1";
	}
}
