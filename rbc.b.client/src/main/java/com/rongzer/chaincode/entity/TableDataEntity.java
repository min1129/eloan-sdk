package com.rongzer.chaincode.entity;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.xerces.impl.dv.util.Base64;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.rongzer.blockchain.client.RBCControllerClient;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.rdp.common.util.StringUtil;
import com.rongzer.utils.RBCEncrypto;
import com.rongzer.utils.RBCUtils;

public class TableDataEntity implements BaseEntity{
	
	private JSONObject jObject = new JSONObject();
	
	private TableEntity tableEntity = null;
	public TableDataEntity(){
	}
	
	public TableDataEntity(String modelName,String tableName,String pubR){
		jObject.put("__PUB_R", pubR);
		jObject.put("__MODEL_NAME", modelName);
		jObject.put("__TABLE_NAME", tableName);
	}
	
	public TableDataEntity(JSONObject jData){
		fromJSON(jData);
	}
	
	@Override
	public void fromJSON(JSONObject jData) {
		//this.jObject = jObject;
		jObject.putAll(jData);
	}
	
	@Override
	public JSONObject toJSON() {
		return jObject;
	}
	
	public void setMainID(String mainId){
		jObject.put("MAIN_ID", mainId);
	}
	
	public String getMainID(){
		return getString("MAIN_ID");
	}

	public void setModelName(String modelName){
		jObject.put("__MODEL_NAME", modelName);
	}
	
	public String getModelName(){
		return getString("__MODEL_NAME");
	}

	public void setTableName(String tableName){
		jObject.put("__TABLE_NAME", tableName);
	}
	
	public String getTableName(){
		return getString("__TABLE_NAME");
	}
	
	public String getPubR(){
		return getString("__PUB_R");
	}



	/**
	 * 取整型数
	 * @param colName
	 * @return
	 */
	public int getInt(String colName)
	{
		int nReturn = -1;
		try{
			
			if (jObject.get(colName) instanceof Integer){
				nReturn = jObject.getInt(colName);
			}else{
				nReturn = Integer.parseInt((String)jObject.get(colName),-1);
			}
			
		}catch(Exception e)
		{
			
		}
		return nReturn;
	}
	
	/**
	 * 取值
	 * @param colName
	 * @return
	 */
	public String getString(String colName)
	{
		String strReturn = "";
		try{
			if (jObject.get(colName) == null){
				return strReturn;
			}
			strReturn = ""+jObject.get(colName);			
		}catch(Exception e)
		{
			
		}
		return strReturn;
	}
	
	/**
	 * 取Hash值
	 * @param colName
	 * @return
	 */
	public String getHashString(String colName)
	{
		if (jObject.get("__HS_"+colName) != null){
			return jObject.getString("__HS_"+colName);
		}
		return  getString(colName);
	}
	
	/**
	 * 设值
	 * @param colName
	 * @return
	 */
	public void setValue(String colName,String value)
	{
		jObject.put(colName, value);
	}	
	
	public String toString(){
		//处理权限
		return jObject.toString();

	}
	public void enc(SecurityCert securityCert){
		
		String mainId = this.getMainID();
		if (StringUtil.isEmpty(mainId)){
			return;
		}
		try
		{
			String modelName = getModelName();
			String tableName = getTableName();
			if (StringUtil.isEmpty(modelName) || StringUtil.isEmpty(tableName)){
				RBCUtils.exception(String.format("model %s table %s is not exist",modelName,tableName));
				return;
			}
			RBCControllerClient rbcc = new RBCControllerClient();

			if (tableEntity ==null)
			{
				tableEntity = rbcc.getTable(securityCert, modelName, tableName);
			}
			
			if (tableEntity ==null)
			{
				RBCUtils.exception(String.format("model %s table %s is not exist",modelName,tableName));
				return;
			}
			
			String customerNoMD5 = getString("CUSTOMER_NO");

			if (StringUtil.isNotEmpty(customerNoMD5))
			{
				customerNoMD5 = StringUtil.MD5("HASH:"+customerNoMD5);
			}
			
			List<String> indexCols = new ArrayList<String>();
			//索引字段
			for(IndexEntity indexEntity:tableEntity.getIndexList())
			{
				indexCols.addAll(StringUtil.split(indexEntity.getIndexCols()));
			}

			//处理数据的hash值
			//遍历数据进行加密处理
			for (ColumnEntity columnEntity : tableEntity.getColList()){
				String colName = columnEntity.getColumnName();
			
				//保留字、唯一键、检索键、索引字段保留md5的hash值
				if ("MAIN_ID".equals(colName)||"CUSTOMER_NO".equals(colName)||columnEntity.getColumnUnique() == 1 || columnEntity.getColumnIndex() == 1 || indexCols.indexOf(colName)>=0){
					
					String value = getString(colName);

					if (StringUtil.isEmpty(value)){
						continue;
					}
					
					//唯一键、索引键、字段名保留MD5的hash值
					setValue("__HS_"+colName, StringUtil.MD5("HASH:"+value));
				}				
			}
			
			
			String pubR = this.getPubR();
			CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			ECPublicKey ecPublicKey = (ECPublicKey)cryptoSuite.bytesToPublicKey(securityCert.getRbcUser().getEnrollment().getCert().getBytes());

			String curPub =  RBCUtils.publicKeyToByte(ecPublicKey);
			JSONObject jCryptogram = new JSONObject();
			String mainIdMd5 = StringUtil.MD5("HASH:"+mainId);
			if (StringUtil.isEmpty(pubR) || pubR.equals(curPub)){
				pubR =curPub;
				jObject.put("__PUB_R", pubR);
				//对请求数据进行加密
				ECPublicKey mainPub = rbcc.getMainPublicKey(securityCert, modelName, mainIdMd5);
				if (mainPub == null){
					return;
				}
				RBCEncrypto rbcEncrypto= new RBCEncrypto(mainPub,(ECPrivateKey)securityCert.getRbcUser().getEnrollment().getKey());
				String cryptoCustomerNo = customerNoMD5;
				//不需要部门间控制时,不加customer的盐
				if (StringUtil.isEmpty(tableEntity.getControlRole())){
					cryptoCustomerNo = "";
				}
				//获取加密密码
				for (ColumnEntity columnEntity : tableEntity.getColList()){
					String colName = columnEntity.getColumnName();
					
					jCryptogram.put(colName,Base64.encode(rbcEncrypto.getCryptogram(modelName,tableName,colName,cryptoCustomerNo)));
				}

			}else {
				jCryptogram = rbcc.getCryptogram(securityCert,modelName,tableName,mainId,customerNoMD5,pubR);
				if (jCryptogram == null){
					return;
				}
			}



			JSONObject jData = new JSONObject();
			jData.putAll(jObject);

			//遍历数据进行加密处理
			for (ColumnEntity columnEntity : tableEntity.getColList()){
				String colName = columnEntity.getColumnName();

				//处理权限,不能写的数据不能提交,但是唯一键是一定要提交的
				if (!columnEntity.canWrite()&& 1 != columnEntity.getColumnUnique()){
					jData.remove(colName);
					jData.remove("__HS_"+colName);

				}
				
				String value = getString(colName);

				if (StringUtil.isEmpty(value)){
					continue;
				}
				
				if (StringUtil.isEmpty(jCryptogram.get(colName))){
					continue;
				}
				
				//数据己加密
				if (StringUtil.isEmpty(value) || RBCUtils.isCrypto(value)){
					continue;
				}
				
				try{
					byte[] cryptogram = Base64.decode(jCryptogram.getString(colName));
					
					byte[] bReturn = RBCUtils.enc(cryptogram, value.getBytes("UTF-8"));
					jData.put(colName, Base64.encode(bReturn));
				}catch(Exception e1)
				{
					
				}
			}
			
			jObject = jData;
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	public String getTxId() {
		return (String)jObject.get("txId");
	}

	public void setTxId(String txId) {
		jObject.put("txId", txId);
	}

	public String getTxTime() {
		return (String)jObject.get("txTime");
	}

	public void setTxTime(String txTime) {
		jObject.put("txTime", txTime);
	}

	public String getIdKey() {
		return (String)jObject.get("idKey");
	}

	public void setIdKey(String idKey) {
		jObject.put("idKey", idKey);
	}
}
