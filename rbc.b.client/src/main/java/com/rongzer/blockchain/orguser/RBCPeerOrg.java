package com.rongzer.blockchain.orguser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.rongzer.rdp.common.util.StringUtil;

/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * Sample Organization Representation
 *
 * Keeps track which resources are defined for the Organization it represents.
 *
 */
public class RBCPeerOrg {
	//组织名称
    final String name;
    //用户中心ID member service provider id
    final String mspid;
    
    //Peer节点列表
    List<Map<String,String>> peerConfigList = new ArrayList<Map<String,String>>(); 
 
    //ca访问的参数
    private Properties caProperties= null;
    

    public RBCPeerOrg(String name, String mspid) {
        this.name = name;
        this.mspid = mspid;
    }

    public String getMSPID() {
        return mspid;
    }
    
    public List<Map<String,String>> getPeerConfigList() {
    	return peerConfigList;
    }
    
    public void addPeerLocation(String peerName,String peerLocation) {
    	if (StringUtil.isEmpty(peerName) || StringUtil.isEmpty(peerLocation)){
    		return;
    	}
    	Map<String,String> peerConfig = null;
    	for (Map<String,String> thePeerConfig : peerConfigList){
    		if (peerName.equals(thePeerConfig.get("PEER_NAME"))){//不可重复定义
    			return;
    		}    		
    	}
    	peerConfig = new HashMap<String,String>();
    	peerConfig.put("PEER_NAME", peerName);
    	peerConfig.put("PEER_LOCATION", peerLocation);
    	peerConfigList.add(peerConfig);
    }
    
    public void setPeerEvent(String peerName,String eventLocation) {
    	if (StringUtil.isEmpty(peerName) || StringUtil.isEmpty(eventLocation)){
    		return;
    	}
    	Map<String,String> peerConfig = null;
    	for (Map<String,String> thePeerConfig : peerConfigList){
    		if (peerName.equals(thePeerConfig.get("PEER_NAME"))){//不可重复定义
    			peerConfig = thePeerConfig;
    			break;
    		}    		
    	}
    	
    	if (peerConfig == null){
    		return;
    	}
    	peerConfig.put("EVENT_LOCATION", eventLocation);
    }

    public String getName() {
        return name;
    }

    public void setCAProperties(Properties CAProperties) {
        this.caProperties = CAProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }
}
