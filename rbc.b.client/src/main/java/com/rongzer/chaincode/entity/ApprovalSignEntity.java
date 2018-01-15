package com.rongzer.chaincode.entity;

import net.sf.json.JSONObject;

/**
 * 审批实体对象
 * @author Administrator
 *
 */
public class ApprovalSignEntity implements BaseEntity{
/*
	CustomerNo   string `json:"customerNo"`   //会员No
	TxId         string `json:"txId"`         //交易ID
	Status       string `json:"status"`       //是否同意(1:同意；2:不同意)
	Desc         String `json:"desc"`         //备注	
	ApprovalTime string `json:"approvalTime"` //签名时间
	*/
	private String txId = "";//交易ID 
	private String customerNo = "";//会员No
	private String status = "";////是否同意(1:同意；2:不同意)
	private String desc = ""; //备注
	private String approvalTime = "";//签名时间

	public ApprovalSignEntity()
	{
		
	}
	
	public ApprovalSignEntity(JSONObject jData) {
		fromJSON(jData);
	}
	
	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getApprovalTime() {
		return approvalTime;
	}

	public void setApprovalTime(String approvalTime) {
		this.approvalTime = approvalTime;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jData = new JSONObject();

		jData.put("customerNo", customerNo);
		jData.put("txId", txId);
		jData.put("txTime", txTime);
		jData.put("approvalTime", approvalTime);
		jData.put("status", status);
		jData.put("desc", desc);

		return jData;
	}
	
	@Override
	public void fromJSON(JSONObject jObject) {
		if (jObject == null)
		{
			return;
		}
		customerNo = (String)jObject.get("customerNo");
		txId = (String)jObject.get("txId");
		txTime = (String)jObject.get("txTime");
		approvalTime = (String)jObject.get("approvalTime");
		status = (String)jObject.get("status");
		desc = (String)jObject.get("desc");

	}
	
	/**
	 * 新增字段
	 */
//	private String txId;
	
	private String txTime;
	
	private String idKey;


//	public String getTxId() {
//		return txId;
//	}
//
//	public void setTxId(String txId) {
//		this.txId = txId;
//	}

	public String getTxTime() {
		return txTime;
	}

	public void setTxTime(String txTime) {
		this.txTime = txTime;
	}

	public String getIdKey() {
		return idKey;
	}

	public void setIdKey(String idKey) {
		this.idKey = idKey;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	
	
}
