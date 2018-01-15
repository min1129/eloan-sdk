package com.rongzer.chaincode.entity;

import java.util.HashMap;
import java.util.Map;

public class TransactionEntity {
	
	//交易ID
	private String txId;	
	//交易时间
	private String txTime ;
	//交易证书
	private String txCert;
	//交易签名
	private String txSign;
	//合约ID
	private String chainCodeId;
	//执行状态，0代表成功执行
	private String validationCode;
	//执行参数
	private String args;
	//执行结果
	private String RWResult;

	
	public Map<String,String> toMap()
	{
		Map<String,String> mapTx = new HashMap<String,String>();
		
		mapTx.put("txId", txId);
		mapTx.put("txTime", txTime);
		mapTx.put("txCert", txCert);
		mapTx.put("txSign", txSign);
		
		mapTx.put("chainCodeId", chainCodeId);
		mapTx.put("validationCode", validationCode);
		mapTx.put("args", args);
		mapTx.put("RWResult", RWResult);

		return mapTx;
	}
	
	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getTxTime() {
		return txTime;
	}

	public void setTxTime(String txTime) {
		this.txTime = txTime;
	}

	public String getTxCert() {
		return txCert;
	}

	public void setTxCert(String txCert) {
		this.txCert = txCert;
	}

	public String getTxSign() {
		return txSign;
	}

	public void setTxSign(String txSign) {
		this.txSign = txSign;
	}

	public String getChainCodeId() {
		return chainCodeId;
	}

	public void setChainCodeId(String chainCodeId) {
		this.chainCodeId = chainCodeId;
	}

	public String getValidationCode() {
		return validationCode;
	}

	public void setValidationCode(String validationCode) {
		this.validationCode = validationCode;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getRWResult() {
		return RWResult;
	}

	public void setRWResult(String RWResult) {
		this.RWResult = RWResult;
	}
	
}
