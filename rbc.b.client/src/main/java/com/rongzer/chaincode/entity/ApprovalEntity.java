package com.rongzer.chaincode.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rongzer.utils.JSONUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 审批实体对象
 * @author Administrator
 *
 */
public class ApprovalEntity implements BaseEntity{
/*
	ApprovalId   string  `json:"approvalId"`   //Id(主键)
	ApprovalName string  `json:"approvalName"` //审批事项
	ApprovalType string  `json:"approvalType"` //审批事项类型，判断执行哪个方法(1:版本升级)
	VetoType     string  `json:"vetoType"`     //事项投票类型，判断是否执行方法(1:一票否决型)
	Chaincode    string  `json:"chaincode"`    //执行的智能合约
	Func      	 string  `json:"func"`      //执行合约的方法
	Args         []string  `json:"args"`         //执行方法的参数
	Creater      string  `json:"creater"`      //事项发起人CustomerNo
	CreateTime   string  `json:"createTime"`   //发起时间
	Status       string  `json:"status"`       //事项状态
	Signs        []*Sign `json:"signs"`        //会员签名
	Desc         string   `json:"desc"`         //备注
	Dict         string  `json:"dict"`         //扩展属性
	*/
	private String approvalId = "";//Id(主键)
	private String approvalName = "";//审批事项
	private String approvalType = "";//审批事项类型，判断执行哪个方法(1:版本升级)
	private String vetoType = "";//事项投票类型，判断是否执行方法(1:一票否决型)
	private String chaincode = "";//执行的智能合约
	private String func = ""; //执行合约的方法
	private List<String> args = new ArrayList<String>();//执行方法的参数
	private String creater = "";//事项发起人CustomerNo
	private String createTime = "";//发起时间
	private String status = "";//事项状态
	private List<ApprovalSignEntity> signs = new ArrayList<ApprovalSignEntity>();//会员签名
	private String desc = "";//备注
	private Map<String,String> mapDict = new HashMap<String,String>(); //扩展属性

	public ApprovalEntity()
	{
		
	}
	
	public ApprovalEntity(JSONObject jData) {
		fromJSON(jData);
	}
	
	public String getApprovalId() {
		return approvalId;
	}

	public void setApprovalId(String approvalId) {
		this.approvalId = approvalId;
	}

	public String getApprovalName() {
		return approvalName;
	}

	public void setApprovalName(String approvalName) {
		this.approvalName = approvalName;
	}

	public String getApprovalType() {
		return approvalType;
	}

	public void setApprovalType(String approvalType) {
		this.approvalType = approvalType;
	}

	public String getVetoType() {
		return vetoType;
	}

	public void setVetoType(String vetoType) {
		this.vetoType = vetoType;
	}

	public String getChaincode() {
		return chaincode;
	}

	public void setChaincode(String chaincode) {
		this.chaincode = chaincode;
	}

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public String getCreater() {
		return creater;
	}

	public void setCreater(String creater) {
		this.creater = creater;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ApprovalSignEntity> getSigns() {
		return signs;
	}

	public void setSigns(List<ApprovalSignEntity> signs) {
		this.signs = signs;
	}

	public Map<String, String> getMapDict() {
		return mapDict;
	}

	public void setMapDict(Map<String, String> mapDict) {
		this.mapDict = mapDict;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jData = new JSONObject();
		jData.put("txId", txId);
		jData.put("txTime", txTime);
		jData.put("idKey", idKey);

		jData.put("approvalId", approvalId);
		jData.put("approvalName", approvalName);
		jData.put("approvalType", approvalType);
		jData.put("vetoType", vetoType);
		jData.put("chaincode", chaincode);
		jData.put("func", func);
		jData.put("creater", creater);
		jData.put("createTime", createTime);
		jData.put("status", status);
		jData.put("desc", desc);
		
		JSONArray jArray = new JSONArray();
		for (String arg:args)
		{
			jArray.add(" "+arg);
		}
		jData.put("args", jArray);
 
		if (mapDict != null && !mapDict.isEmpty())
		{
			jData.put("dict",JSONUtil.map2json(mapDict));
		}

		return jData;
	}
	
	@Override
	public void fromJSON(JSONObject jObject) {
		if (jObject == null)
		{
			return;
		}
		txId = (String)jObject.get("txId");
		txTime = (String)jObject.get("txTime");
		idKey = (String)jObject.get("idKey");

		approvalId = (String)jObject.get("approvalId");
		approvalName = (String)jObject.get("approvalName");
		approvalType = (String)jObject.get("approvalType");
		vetoType = (String)jObject.get("vetoType");
		chaincode = (String)jObject.get("chaincode");
		func = (String)jObject.get("func");
		//args = (String)jObject.get("args");
		if (jObject.get("args") != null && jObject.get("args") instanceof JSONArray)
		{
			JSONArray jArray = (JSONArray)jObject.get("args");
			for (int i=0;i<jArray.size();i++)
			{
				args.add(jArray.getString(i));
			}
		}
		
		creater = (String)jObject.get("creater");
		createTime = (String)jObject.get("createTime");
		status = (String)jObject.get("status");
		desc = (String)jObject.get("desc");

		if(jObject.get("dict") != null){
			mapDict = JSONUtil.json2Map(jObject.get("dict").toString());
		}
		
		if (jObject.get("signs") != null && jObject.get("signs") instanceof JSONArray) {
			JSONArray jArray = (JSONArray)jObject.get("signs");
			for (int i=0;i<jArray.size();i++)
			{
				ApprovalSignEntity approvalSignEntity = new ApprovalSignEntity();
				approvalSignEntity.fromJSON(jArray.getJSONObject(i));
				signs.add(approvalSignEntity);
			}
		}
	}
	
	
	/**
	 * 新增字段
	 */
	private String txId;
	
	private String txTime;
	
	private String idKey;


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
