package com.rongzer.blockchain.client;

import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.TransactionEvent;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.google.protobuf.ByteString;
import com.rongzer.blockchain.orguser.RBCPeerOrg;
import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.chaincode.entity.ApprovalEntity;
import com.rongzer.chaincode.entity.ColumnEntity;
import com.rongzer.chaincode.entity.CustomerEntity;
import com.rongzer.chaincode.entity.MessageEntity;
import com.rongzer.chaincode.entity.ModelEntity;
import com.rongzer.chaincode.entity.PageList;
import com.rongzer.chaincode.entity.TableDataEntity;
import com.rongzer.chaincode.entity.TableEntity;
import com.rongzer.rdp.common.context.RDPContext;
import com.rongzer.rdp.memcached.CacheClient;
import com.rongzer.rdp.memcached.MemcachedException;
import com.rongzer.utils.JSONUtil;
import com.rongzer.utils.RBCUtils;
import com.rongzer.utils.StringUtil;

import freemarker.log.Logger;

/**
 * 智能合约总控
 * @author Administrator
 *
 */
public class RBCControllerClient {
    private static final Log logger = LogFactory.getLog(RBCControllerClient.class);
	CacheClient cacheClient = null;
	public RBCControllerClient()
	{
		cacheClient = (CacheClient) RDPContext.getContext()
				.getBean("cacheClient");
	}

	
	////////////////////////////////////////////////////// 数据建模

	public CacheClient getCacheClient() {
		return cacheClient;
	}

	/**
	 * 通过controller查询chainCode
	 * @param signCert
	 * @param funcName
	 * @param chainCodeName
	 * @param args
	 * @return
	 */
	public ByteString queryEncJson(SecurityCert securityCert,String chainCodeName,String funcName, JSONObject funcArgs) {
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(0, funcName);
		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);
		ByteString bReturn = null;
		try {
			// 转换数据类型
			JSONUtil.translateIntFields2Str(funcArgs);
			lisArgs.add(1,funcArgs.toString());

			//加密数据  TODO...

			bReturn = ChainCodeClient.query(channel, rbcUser, chainCodeName, lisArgs);
		} catch (Exception e) {
			//RBCUtils.exception(e.getMessage());
		}

		return bReturn;

	}

	/**
	 * 通过controller同步执行chainCode, 数据类型转换，并加密输入参数。若错误抛出RBC运行异常。
	 * @param securityCert
	 * @param chainCodeName
	 * @param funcName
	 * @param funcArgs
	 * @return
	 */
	public String invokeEncJson(SecurityCert securityCert,String chainCodeName, String funcName,JSONObject funcArgs)
	{
		final String[] txReturn = new String[2];
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(0, funcName);

		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);
		try
		{
			// 转换数据类型
			JSONUtil.translateIntFields2Str(funcArgs);
			lisArgs.add(1,funcArgs.toString());

			//加密数据  TODO...

			CompletableFuture<TransactionEvent> future = ChainCodeClient.invoke(channel, rbcUser, chainCodeName, lisArgs);

			if (future != null){
				Function<TransactionEvent,String> fn = new Function<TransactionEvent,String>(){

					@Override
					public String apply(TransactionEvent t) {
						txReturn[0] = t.getTransactionID();
						txReturn[1] = t.getErrorMsg();
						if (StringUtil.isNotEmpty(t.getErrorMsg())){
							RBCUtils.exception(t.getErrorMsg());
						}
						return null;
					}};
				future.thenApply(fn).get(10, TimeUnit.SECONDS);
			}


		} catch (Exception e) {
			if (!RBCUtils.invokeEvent() && e instanceof TimeoutException)
			{
				return txReturn[0];
			}else if(StringUtil.isNotEmpty(txReturn[1])){
				RBCUtils.exception(txReturn[1]);
			}else
			{
				RBCUtils.exception(e.getMessage());
			}
		}

		return txReturn[0];

	}

	////////////////////////////////////////////////////// 数据建模

	/**
	 * 通过controller查询chainCode
	 * @param securityCert
	 * @param funcName
	 * @param chainCodeName
	 * @param lisArgs
	 * @return
	 */
	public ByteString query(SecurityCert securityCert,String chainCodeName,String funcName, List<String> lisArgs) {
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		lisArgs.add(0, funcName);
		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);
		ByteString bReturn = null;
		try {
			bReturn = ChainCodeClient.query(channel, rbcUser, chainCodeName, lisArgs);
		} catch (Exception e) {
			RBCUtils.exception(e.getMessage());
		}

		return bReturn;

	}

	/**
	 * 通过controller执行chainCode
	 * @param securityCert
	 * @param funcName
	 * @param chainCodeName
	 * @param lisArgs
	 * @return
	 */
	public CompletableFuture<TransactionEvent> invoke(SecurityCert securityCert,String chainCodeName, String funcName,List<String> lisArgs)
	{
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		CompletableFuture<TransactionEvent> future = null;
		lisArgs.add(0, funcName);
		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);

		try {
			future = ChainCodeClient.invoke(channel, rbcUser, chainCodeName, lisArgs);
		} catch (Exception e) {
			RBCUtils.exception(e.getMessage());
		}
		return future;
	}

	/**
	 * 通过controller同步执行chainCode
	 * @param securityCert
	 * @param funcName
	 * @param chainCodeName
	 * @param lisArgs
	 * @return
	 */
	public String invokeSync(SecurityCert securityCert,String chainCodeName, String funcName,List<String> lisArgs)
	{	
		final String[] txReturn = new String[2];
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();
		
		lisArgs.add(0, funcName);
		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);
		try
		{
			CompletableFuture<TransactionEvent> future = ChainCodeClient.invoke(channel, rbcUser, chainCodeName, lisArgs);

			if (future != null){
				Function<TransactionEvent,String> fn = new Function<TransactionEvent,String>(){
					
					@Override
					public String apply(TransactionEvent t) {
						txReturn[0] = t.getTransactionID();
						txReturn[1] = t.getErrorMsg();
						if (StringUtil.isNotEmpty(t.getErrorMsg())){
							RBCUtils.exception(t.getErrorMsg());
						}
						return null;
					}};
				future.thenApply(fn).get(10, TimeUnit.SECONDS);
			}

			
		} catch (Exception e) {
			if (!RBCUtils.invokeEvent() && e instanceof TimeoutException)
			{
				return txReturn[0];
			}else if(StringUtil.isNotEmpty(txReturn[1])){
				RBCUtils.exception(txReturn[1]); //抛出RBC异常
			}else
			{			
				RBCUtils.exception(e.getMessage());
			}
		}
		
		return txReturn[0];
	}
	
	/**
	 * 通过controller执行进行审批发起
	 * @param securityCert
	 * @param approvalEntity
	 */
	public void newApproval(SecurityCert securityCert,ApprovalEntity approvalEntity)
	{		
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);
		try
		{

	        approvalEntity.setApprovalId(StringUtil.getUuid32());
	      
	        List<String> approvalArgs = new ArrayList<String>();
	        approvalArgs.add("newApproval");

	        approvalArgs.add(approvalEntity.toJSON().toString());

	        CompletableFuture<TransactionEvent> future = ChainCodeClient.invoke(channel,rbcUser, "rbcapproval",  approvalArgs);
	        if (future != null)
	        {
	        	future.get(30, TimeUnit.SECONDS);
	        }
		} catch (Exception e) {
			RBCUtils.exception(e.getMessage());
		}
	}
	

	/**
	 * 通过controller执行进行审批
	 * @param securityCert
	 * @param approvalId
	 * @param status
	 * @param desc
	 */
	public void approval(SecurityCert securityCert,String approvalId ,String status,String desc)
	{		
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();

		RBCUser rbcUser = securityCert.getRbcUser();
		Channel channel = ChannelClient.getChannel(peerOrg,rbcUser);
		try
		{
			if (StringUtil.isEmpty(approvalId))
			{
				RBCUtils.exception("approval is is empty");
			}

	        ApprovalEntity approvalEntity = new ApprovalEntity();
	        approvalEntity.setApprovalId(approvalId);
	        approvalEntity.setStatus(status);
	        approvalEntity.setDesc(desc);
	        List<String> approvalArgs = new ArrayList<String>();
	        approvalArgs.add(0, "approval");

	        approvalArgs.add(approvalEntity.toJSON().toString());

	        CompletableFuture<TransactionEvent> future = ChainCodeClient.invoke(channel,rbcUser, "rbcapproval",  approvalArgs);
	        if (future != null)
	        {
	        	future.get(30, TimeUnit.SECONDS);
	        }
	        			        
		} catch (Exception e) {
			RBCUtils.exception(e.getMessage());
		}
	}
	
	/**
	 * 获取主体公钥
	 * @param securityCert
	 * @param modelName
	 * @param mainId
	 * @return
	 */
	public ECPublicKey getMainPublicKey(SecurityCert securityCert,String modelName,String mainId)
	{
		if (StringUtil.isEmpty(mainId)){
			RBCUtils.exception("main id is empty");
		}
		CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();

		String key = "__MAINPUBKEY_"+modelName+"_"+mainId;
		byte[]  pubKeyBuf = null;
		try{
			pubKeyBuf = (byte[])cacheClient.get(key);
			if (pubKeyBuf != null){
				return cryptoSuite.bytesToPublicKey(pubKeyBuf);
			}
		}catch(Exception e)
		{
			
		}
		
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(mainId);
		//从区块链上获取主体公钥
		ByteString bReturn = query(securityCert, "rbcmodel", "getMainPublicKey", lisArgs);
		if (bReturn == null || bReturn.toByteArray().length<65){
			return null;
		}
		ECPublicKey ecPublicKey = null;
		try{
			String pubKeyStr = bReturn.toStringUtf8();
			pubKeyStr = pubKeyStr.replaceAll("\"", "");
			if (StringUtil.isEmpty(pubKeyStr)){
				return null;
			}
			pubKeyBuf = Hex.decode(pubKeyStr);
			ecPublicKey = cryptoSuite.bytesToPublicKey(pubKeyBuf);
			cacheClient.add(key, pubKeyBuf);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return ecPublicKey;
	}
	
	/**
	 * 获取密码
	 * @param securityCert
	 * @param modelName
	 * @param tableName
	 * @param mainId
	 * @param customerNo
	 * @param pubR
	 * @return
	 */
	public JSONObject getCryptogram(SecurityCert securityCert,String modelName,String tableName,String mainId,String customerNo,String pubR){
		JSONObject jData = new JSONObject();
		jData.put("mainId", mainId);
		jData.put("pubR", pubR);
		jData.put("customerNo", customerNo);
		if ("__CHAINCODE".equals(tableName)){
			jData.put("chaincodeName", modelName);
			jData.put("etype", "gram");
		}
		
		//从解密中心获取密码customerNo
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(tableName);
		lisArgs.add(RBCUtils.getPubG());
		lisArgs.add(jData.toString());
		
		ByteString bReturn = query(securityCert, "rbcmodel", "getMainCryptogram", lisArgs);
		if (bReturn == null || bReturn.toByteArray().length <10){
			return null;
		}
		JSONObject jCryptogram = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		try{
			if (jCryptogram != null && jCryptogram.get("__ENCGRAM")!= null){
				byte[] bCryptogram = Base64.decode(jCryptogram.getString("__ENCGRAM"));
				String thePubR = (String)jCryptogram.get("pubR");
				byte[] decCryptogram = RBCUtils.decCryptogram(bCryptogram, CryptoSuite.Factory.getCryptoSuite().bytesToPublicKey(Hex.decode(thePubR)));
				jCryptogram = JSONUtil.getJSONObjectFromStr(new String(decCryptogram));
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		
		return jCryptogram;
	}
	
	/**
	 * 根据条件查询用户
	 * @param securityCert
	 * @param customerNo
	 * @return
	 */
	public CustomerEntity getCustomerEntity(SecurityCert securityCert, String customerNo)
	{
		String key = "__RBCCUSRTOMER_"+customerNo;
		CustomerEntity customerEntity = null;
		try{
			customerEntity = (CustomerEntity)cacheClient.get(key);
			if (customerEntity != null){
				return customerEntity;
			}
		}catch(Exception e)
		{
			
		}
		
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(customerNo);
		ByteString  bReturn = query(securityCert,"rbccustomer", "queryOne", lisArgs);
		if (bReturn == null)
		{ 
			RBCUtils.exception("query user from chaincode error,"+customerNo);
		}
		
		JSONObject jReturn = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		if(StringUtil.isEmpty(jReturn) || StringUtil.isNotEmpty(jReturn.get("Error"))){
			return null;
		}
		customerEntity = new CustomerEntity();
		customerEntity.fromJSON(jReturn);
		try {
			cacheClient.add(key, customerEntity);
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return customerEntity;
	}

	/**
	 * 发送消息通知
	 * @param stub
	 * @param bizId 消息数据ID
	 * @param msgType 消息类型
	 * @param toCustomer 接收会员
	 * @param msgParams	消息参数
	 * @param msgDesc 消息描述
	 */
	public void sendMessage(SecurityCert securityCert,String bizId,String msgType,String toCustomer){
		List<String> msgParams = new ArrayList<String>();
		msgParams.add(bizId);
		sendMessage(securityCert,bizId,msgType,toCustomer,msgParams,"");
	}
	
	/**
	 * 发送消息通知
	 * @param stub
	 * @param bizId 消息数据ID
	 * @param msgType 消息类型
	 * @param toCustomer 接收会员
	 * @param msgParams	消息参数
	 * @param msgDesc 消息描述
	 */
	public void sendMessage(SecurityCert securityCert,String bizId,String msgType,String toCustomer,List<String> msgParams,String msgDesc){
		if (StringUtil.isEmpty(bizId)||StringUtil.isEmpty(msgType)||StringUtil.isEmpty(toCustomer)||msgParams ==null || msgParams.size()<1){
			logger.error("sendmessage error:params is error");
			return;
		}
		
		CustomerEntity customerEntity = getCustomerEntity(securityCert, toCustomer);
		if (customerEntity == null || StringUtil.isEmpty(customerEntity.getCustomerId())){
			logger.error("sendmessage error:params is empty");
			return;
		}
		
		MessageEntity messageEntity = new MessageEntity();
		messageEntity.setBizId(bizId);
		messageEntity.setMsgType(msgType);
		messageEntity.setToCustomer(toCustomer);
		messageEntity.setMsgParams(msgParams);
		messageEntity.setMsgDesc(msgDesc);
		
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(messageEntity.toJSON().toString());
		//异步发送消息
		invoke(securityCert,"rbcnotify", "sendMessage", lisArgs);
	}
	
	
	/**
	 * 查询一条消息
	 * @param stub
	 * @param msgId 消息Id
	 */
	public MessageEntity getMessage(SecurityCert securityCert,String msgId){
		if (StringUtil.isEmpty(msgId)){
			return null;
		}
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(msgId);
		//异步发送消息
		ByteString bReturn = query(securityCert,"rbcnotify", "getMessage", lisArgs);
		if (bReturn == null){
			return null;
		}
		JSONObject jData = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		if (jData == null){
			return null;
		}
		
		MessageEntity messageEntity = new MessageEntity(jData);
		return messageEntity;
	}
	
	/**
	 * 根据条件查询消息列表
	 * @param securityCert
	 * @param jQuery
	 * @param cpno
	 * @return
	 */
	public PageList<MessageEntity> getMessageList(SecurityCert securityCert,JSONObject jParams,int cpno){
		if (jParams == null){
			jParams = new JSONObject();
		}
		PageList<MessageEntity> lisMessageEntity = new PageList<MessageEntity>();

		jParams.put("cpno", ""+cpno);
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(jParams.toString());
		//异步发送消息
		ByteString bReturn = query(securityCert,"rbcnotify", "getMessageList", lisArgs);
		if (bReturn == null){
			return lisMessageEntity;
		}
		JSONObject jData = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		if (jData == null){
			return lisMessageEntity;
		}
		
		lisMessageEntity.fromJSON(jData, MessageEntity.class);
		return lisMessageEntity;
	}
	
	/**
	 * 设置(关闭通知)消息己读
	 * @param stub
	 * @param msgId 消息Id
	 */
	public void readMessage(SecurityCert securityCert,String msgId){
		if (StringUtil.isEmpty(msgId)){
			logger.error("msgId is empty");
			return;
		}
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(msgId);
		//异步发送消息
		invoke(securityCert,"rbcnotify", "readMessage", lisArgs);
	}
	
	
	/**
	 * 处理(关闭)消息通知
	 * @param stub
	 * @param msgId 消息Id
	 */
	public void handleMessage(SecurityCert securityCert,String msgId){
		if (StringUtil.isEmpty(msgId)){
			logger.error("msgId is empty");
			return;
		}
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(msgId);
		//异步发送消息
		invoke(securityCert,"rbcnotify", "handleMessage", lisArgs);
	}
	

	
	/**
	 * 清空设置某一主键数据的所有消息
	 * @param stub
	 * @param bizId 消息业务Id
	 */
	public void clearMessage(SecurityCert securityCert,String bizId){
		if (StringUtil.isEmpty(bizId)){
			logger.error("bizId is empty");
			return;
		}
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(bizId);
		//异步发送消息
		invoke(securityCert,"rbcnotify", "clearMessage", lisArgs);
	}
	
	/**
	 * 获取模型表
	 * @param securityCert
	 * @param modelName
	 * @return
	 */
	public ModelEntity getModel(SecurityCert securityCert, String modelName)
	{
		String key = "__RBCMODEL_"+modelName;
		ModelEntity modelEntity = null;
		try{
			modelEntity = (ModelEntity)cacheClient.get(key);
			if (modelEntity != null){
				return modelEntity;
			}
		}catch(Exception e)
		{
			
		}

		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		ByteString  bReturn = query(securityCert,"rbcmodel", "getModel", lisArgs);
		if (bReturn == null)
		{ 
			return null;
		}
		
		JSONObject jReturn = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		if(StringUtil.isEmpty(jReturn) || StringUtil.isNotEmpty(jReturn.get("Error"))){
			return null;
		}
		modelEntity = new ModelEntity();
		modelEntity.fromJSON(jReturn);
		try {
			cacheClient.add(key, modelEntity);
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		return modelEntity;
	}

	/**
	 * 获取访问者读写权限模型表
	 * @param securityCert
	 * @param modelName
	 * @param tableName
	 * @return
	 */
	public TableEntity getTable(SecurityCert securityCert, String modelName,String tableName)
	{
		return getTable(securityCert,modelName,tableName,securityCert.getCustomerNo());
	}
	
	/**
	 * 获取带有指定会员权限模型表
	 * @param securityCert
	 * @param modelName
	 * @param tableName
	 * @param customerNo 数据的访问者
	 * @return
	 */
	public TableEntity getTable(SecurityCert securityCert, String modelName,String tableName,String customerNo)
	{

		String key = "__RBCMODEL_"+modelName+"_"+tableName+"_"+customerNo;
		TableEntity tableEntity = null;
		try{
			tableEntity = (TableEntity)cacheClient.get(key);
			if (tableEntity != null){
				return tableEntity;
			}
		}catch(Exception e)
		{
			
		}
		
		boolean visitAllData = false;
		CustomerEntity customerEntity = null;
		List<String> lisRoleNos = null;
		//未指定数据的访问者，则表示所有权限
		if (StringUtil.isNotEmpty(customerNo)){
			customerEntity = getCustomerEntity(securityCert,customerNo);	
		}
		
		if (customerEntity == null || StringUtil.isEmpty(customerEntity.getCustomerId())){
			visitAllData = true;
		}else{
			lisRoleNos = StringUtil.split(StringUtil.safeTrim(customerEntity.getRoleNos()));
		}
		
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(tableName);
		ByteString  bReturn = query(securityCert,"rbcmodel", "getTable", lisArgs);
		if (bReturn == null)
		{ 
			RBCUtils.exception("query table from chaincode error");
		}
		
		JSONObject jReturn = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		if(StringUtil.isEmpty(jReturn) || StringUtil.isNotEmpty(jReturn.get("Error"))){
			return null;
		}
		tableEntity = new TableEntity();
		tableEntity.fromJSON(jReturn);
				

		//设置字段的读写权限
		for (ColumnEntity columnEntity : tableEntity.getColList()){
			if (visitAllData){
				columnEntity.setCanRead(true);
				columnEntity.setCanWrite(true);
				continue;
			}
			
			List<String> lisReadRoles =  StringUtil.split(StringUtil.safeTrim(columnEntity.getReadRoles()));
			List<String> lisWriteRoles =  StringUtil.split(StringUtil.safeTrim(columnEntity.getWriteRoles()));
			//如果未设置角色，角色则存在所有权限
			boolean bRead = false;
			boolean bWrite = false;
			if (lisRoleNos.size() <1){
				bRead = true;
				bWrite = true;
			}
			
			if (lisReadRoles.size()<1){
				bRead = true;
			}
			
			if (lisWriteRoles.size()<1){
				bRead = true;
				bWrite = true;
			}
			
			for (String roleNo : lisReadRoles){
				if (lisRoleNos.indexOf(roleNo)>=0){
					bRead = true;
					break;
				}
			}

			for (String roleNo : lisWriteRoles){
				if (lisRoleNos.indexOf(roleNo)>=0){
					bRead = true;
					bWrite = true;
					break;
				}
			}
			columnEntity.setCanRead(bRead);
			columnEntity.setCanWrite(bWrite);
			
		}
		
		try {
			cacheClient.add(key, tableEntity);
		} catch (MemcachedException e) {
			e.printStackTrace();
		}
		
		return tableEntity;
	}
	

	/**
	 * 设置模型中表的列表
	 * @param securityCert
	 * @param tableDataEntity
	 */
	public void setTableData(SecurityCert securityCert,String chainCodeName,String modelName,String tableName,TableDataEntity tableDataEntity)
	{
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(tableName);
		//加密数据
		tableDataEntity.enc(securityCert);
		lisArgs.add(tableDataEntity.toString());

		invokeSync(securityCert,chainCodeName, "__setTableData", lisArgs);
	}
	
	/**
	 * 删除模型中表的数据
	 * @param securityCert
	 * @param idKey
	 */
	public void delTableData(SecurityCert securityCert,String chainCodeName,String modelName,String tableName,String idKey)
	{
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(tableName);
		lisArgs.add(idKey);

		invokeSync(securityCert,chainCodeName, "__delTableData", lisArgs);
		
	}
	
	/**
	 * 获取模型表
	 * @param securityCert
	 * @param tableName
	 * @param idKey
	 * @return
	 */
	public TableDataEntity getTableData(SecurityCert securityCert,String chainCodeName,String modelName, String tableName,String idKey)
	{
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(tableName);
		lisArgs.add(idKey);

		ByteString  bReturn = query(securityCert,chainCodeName, "__getTableData", lisArgs);
		if (bReturn == null)
		{ 
			return null;
		}
		
		JSONObject jReturn = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
		if(StringUtil.isEmpty(jReturn) || StringUtil.isNotEmpty(jReturn.get("Error"))){
			return null;
		}
		TableDataEntity tableDataEntity = new TableDataEntity();
		tableDataEntity.fromJSON(jReturn);
		
		return tableDataEntity;
	}

	
	/**
	 * 获取模型中表的列表
	 * @param securityCert
	 * @param indexValue
	 */
	public PageList<TableDataEntity> lisTableData(SecurityCert securityCert,String chainCodeName,String modelName,String tableName,String indexValue,int cpno)
	{
		List<String> lisArgs = new ArrayList<String>();
		lisArgs.add(modelName);
		lisArgs.add(tableName);
		lisArgs.add(indexValue);
		lisArgs.add(""+cpno);

		ByteString bReturn = query(securityCert,chainCodeName, "__lisTableData", lisArgs);
		PageList<TableDataEntity> lisEntity = new PageList<TableDataEntity>();

		if (bReturn == null)
		{
			return lisEntity;
		}
		
		JSONObject jReturn = JSONUtil.getJSONObjectFromStr(bReturn.toStringUtf8());
				
		
		lisEntity.fromJSON(jReturn, TableDataEntity.class);
		return lisEntity;		
	}
}
