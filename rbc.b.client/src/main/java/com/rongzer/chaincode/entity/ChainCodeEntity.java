package com.rongzer.chaincode.entity;

import java.util.HashMap;
import java.util.Map;

public class ChainCodeEntity {
/**
Id		string `json:"Id"`		chaincodeID(主键)
Type	string `json:"Type"`	chaincodeType
Alias	string `json:"Alias"`	chaincode别名
Version	string `json:"Version"`	chaincode版本
Creater	string `json:"Creater"`	创建人
Code	string `json:"Code"`	chaincode代码内容
Text	string `json:"Text"`	chaincode文本描述
Name	string `json:"Name"`	chaincode名
Time	string `json:"Time"`	设置时间
Func	string `json:"Func"`	初始化方法
Args	string `json:"Args"`	初始化参数
Extend	string `json:"Extend"`	扩展属性
 */
	/*
enum Type {
        UNDEFINED = 0;
        GOLANG = 1;
        NODE = 2;
        CAR = 3;
        JAVA = 4;
    	}
	 */
	private String id = "";
	
	private int type = 1;

	private String alias = "";
	
	private String version = "";
	
	private String creater = "";
	
	private String code = "";
	
	private String text = "";

	private String name = "";

	private String time = "";
	
	private String func = "init";
	
	private String args = "";
	
	private String extend = "";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreater() {
		return creater;
	}

	public void setCreater(String creater) {
		this.creater = creater;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getFunc() {
		return func;
	}

	public void setFunc(String func) {
		this.func = func;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}
	
	public Map<String,String> toMap()
	{

		Map<String,String> mapData = new HashMap<String,String>();
		mapData.put("id", id);
		String strType = "UNDEFINED";
		if (type == 1)
		{
			strType = "GOLANG";
		}else if (type == 2)
		{
			strType = "NODE";
		}else if (type == 3)
		{
			strType = "CAR";
		}else if (type == 4)
		{
			strType = "JAVA";
		}
		mapData.put("type", strType);
		mapData.put("alias", alias);
		mapData.put("version", version);
		mapData.put("creater", creater);
		mapData.put("text", text);
		mapData.put("code", code);
		mapData.put("name", name);
		mapData.put("time", time);
		mapData.put("func", func);
		mapData.put("args", args);
		mapData.put("extend", extend);

		return mapData;
	}

}
