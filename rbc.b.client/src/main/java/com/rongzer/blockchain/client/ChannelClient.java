package com.rongzer.blockchain.client;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.hyperledger.fabric.protos.common.Common.ChannelHeader;
import org.hyperledger.fabric.protos.common.Common.Envelope;
import org.hyperledger.fabric.protos.common.Common.Payload;
import org.hyperledger.fabric.protos.common.Common.SignatureHeader;
import org.hyperledger.fabric.protos.common.Rbc.BlockStatistics;
import org.hyperledger.fabric.protos.common.Rbc.PeerList;
import org.hyperledger.fabric.protos.common.Rbc.RBCHttpRequest;
import org.hyperledger.fabric.protos.common.Rbc.RBCMessage;
import org.hyperledger.fabric.protos.common.Rbc.transactionStatistics;
import org.hyperledger.fabric.protos.ledger.rwset.Rwset.NsReadWriteSet;
import org.hyperledger.fabric.protos.ledger.rwset.Rwset.TxReadWriteSet;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset.KVRWSet;
import org.hyperledger.fabric.protos.msp.Identities.SerializedIdentity;
import org.hyperledger.fabric.protos.orderer.Ab;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeInvocationSpec;
import org.hyperledger.fabric.protos.peer.Chaincode.ChaincodeSpec;
import org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeAction;
import org.hyperledger.fabric.protos.peer.FabricProposal.ChaincodeProposalPayload;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse.ProposalResponsePayload;
import org.hyperledger.fabric.protos.peer.FabricTransaction.ChaincodeActionPayload;
import org.hyperledger.fabric.protos.peer.FabricTransaction.Transaction;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.TransactionInfo;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.protobuf.ByteString;
import com.rongzer.blockchain.orguser.RBCPeerOrg;
import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.blockchain.security.SecurityCert;
import com.rongzer.chaincode.entity.TransactionEntity;
import com.rongzer.rdp.common.service.RDPUtil;
import com.rongzer.rdp.common.util.StringUtil;
import com.rongzer.utils.RBCUtils;

public class ChannelClient {
    private static final Log logger = LogFactory.getLog(ChannelClient.class);
    
    //通道连接的缓存
    private static Map<String,Channel> MAP_CHANNEL = new HashMap<String,Channel>();

    /**
     * 清空通道连接缓存
     */
	public static void clear()
	{
		Iterator keys = MAP_CHANNEL.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Channel channel = MAP_CHANNEL.get(key);
			try
			{
				channel.shutdown(true);
			}catch(Exception e)
			{
				
			}
		}
		
		MAP_CHANNEL.clear();
	}
	
	 /**
     * 移除Channel
     */
	public static void removeChannel(Channel channel)
	{
		Iterator keys = MAP_CHANNEL.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Channel channel1 = MAP_CHANNEL.get(key);
			try
			{
				if (channel1 .equals(channel))
				{
					MAP_CHANNEL.remove(key);
					channel.shutdown(true);
				}
			}catch(Exception e)
			{
				
			}
		}
		
	}
	
	/**
	 * 获取通道连接
	 * @param peerOrg
	 * @param rbcUser
	 * @return
	 */
	public static Channel getChannel(RBCPeerOrg peerOrg,RBCUser rbcUser)
	{

		Channel theChannel = null;
		try
		{
			if (rbcUser == null || rbcUser.getEnrollment() == null || rbcUser.getEnrollment().getCert() == null)
			{
				return null;
			}
			
			String key = peerOrg.getName() +"-"+ rbcUser.getName();
			theChannel = MAP_CHANNEL.get(key);
			if (theChannel !=null && theChannel.isInitialized() && !theChannel.isShutdown())
			{
				return theChannel;
			}
			MAP_CHANNEL.remove(key);
            //Create instance of client.
            HFClient client = HFClient.createNewInstance();

            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            
            theChannel = getExistChain(client,peerOrg,rbcUser);          
            
			if (theChannel != null &&  theChannel.getPeers() != null  && theChannel.getPeers().size() > 0)
			{
				MAP_CHANNEL.put(key, theChannel);
			}else
			{
				theChannel = null;
			}
			
			
		}catch(Exception e)
		{
			 e.printStackTrace();
			 //logger.error("get channel has exception", e);
		}

		return theChannel;
	}
	
	/**
	 * 初始化通道
	 * @param peerOrg
	 * @return
	 * @throws Exception
	 */
	public static Channel initChannel(RBCPeerOrg peerOrg) throws Exception {
		Channel theChannel = null;
		try
		{

			RBCUser peerAdminUser = RBCUtils.getPeerAminUser();
			if (peerAdminUser == null)
			{
				RBCUtils.exception("peer admin cert is null ,can not init Channel");
			}
			
            //Create instance of client.
            HFClient client = HFClient.createNewInstance();

            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            
            theChannel = constructChain(client,peerOrg,peerAdminUser);
            
            if (theChannel == null){
            	RBCUtils.exception("init channel fail");
            }
			
		}catch(Exception e)
		{
			logger.error("init channel has exception",e);
			 throw e;
		}
		
		return theChannel;
	}
	
	/**
	 * 构建新通道
	 * @param client
	 * @param peerOrg
	 * @param rbcUser
	 * @return
	 * @throws Exception
	 */
    private static Channel constructChain(HFClient client,RBCPeerOrg peerOrg, RBCUser rbcUser) throws Exception {
		String chainName = RBCUtils.getChannelName();
        client.setUserContext(rbcUser);
        
        List<Peer> lisPeer = new ArrayList<Peer>();

        for (Map<String,String> thePeerConfig : peerOrg.getPeerConfigList()){
        	String peerName = thePeerConfig.get("PEER_NAME");            	
            String peerLocation = thePeerConfig.get("PEER_LOCATION");
            Peer peer = client.newPeer(peerName, peerLocation, getPeerProperties(peerName));
            lisPeer.add(peer);
        }
        
        Resource resource = new ClassPathResource("/chain/"+RBCUtils.getChannelName()+".tx");
		File theFile = resource.getFile();
		
        ChannelConfiguration chainConfiguration = new ChannelConfiguration(theFile);
        
        Channel newChannel = client.newChannel(chainName, lisPeer.get(0), chainConfiguration, client.getChannelConfigurationSignature(lisPeer.get(0),chainConfiguration, rbcUser));
       
        logger.info(String.format("Created chain %s", chainName));

        for (Peer peer : lisPeer) {
            newChannel.joinPeer(peer);
            logger.info(String.format("Peer %s joined chain %s", peer.getName(), chainName));
        }

        for (Map<String,String> thePeerConfig : peerOrg.getPeerConfigList()){
        	String peerName = thePeerConfig.get("PEER_NAME");            	
            String eventLocation = thePeerConfig.get("EVENT_LOCATION");
            EventHub eventHub = client.newEventHub(peerName,eventLocation,getEventHubProperties(eventLocation));
            newChannel.addEventHub(eventHub);
        }            
        
        newChannel.setTransactionWaitTime(2*60*1000);
        newChannel.setDeployWaitTime(10*60*1000);

        newChannel.initialize();
        
		Thread.sleep(2000);


        logger.info(String.format("Finished initialization chain %s", chainName));
        return newChannel;
    }

    /**
     * 获取存在的通道
     * @param client
     * @param peerOrg
     * @param rbcUser
     * @return
     * @throws Exception
     */
    private static Channel getExistChain(HFClient client,RBCPeerOrg peerOrg,RBCUser rbcUser) throws Exception {
        //////////////////////////// TODo Needs to be made out of bounds and here chain just retrieved
        //Construct the chain
        //
    	Channel existChannel = null;
    	try
    	{
    		String chainName = RBCUtils.getChannelName();

            client.setUserContext(rbcUser);

            List<Peer> lisPeer = new ArrayList<Peer>();
            for (Map<String,String> thePeerConfig : peerOrg.getPeerConfigList()){
            	String peerName = thePeerConfig.get("PEER_NAME");            	
                String peerLocation = thePeerConfig.get("PEER_LOCATION");
                Peer peer = client.newPeer(peerName, peerLocation, getPeerProperties(peerName));
                lisPeer.add(peer);
            }
            Channel theChannel = null;
            Peer thePeer = null;
            for (Peer peer :lisPeer){
            	try{
            		theChannel = client.newChannel(chainName,peer);
            	}catch(Exception e)
            	{
            		logger.error(String.format("can't connect peer to %s:%s ,eer:%s",peer.getName(),peer.getUrl(),e.getMessage()));
            	}
            	if (theChannel != null && !theChannel.isShutdown()){
            		thePeer = peer;
            		break;
            	}else{
            		theChannel = null;
            	}            	                        
            }
            
            //不能连接上所有节点
            if (theChannel == null || theChannel.isShutdown()||thePeer == null){
            	throw new Exception("can't connect all peers");
            }
            
            theChannel.addPeer(thePeer);
            /*
            boolean isExist = false;
            for (Peer peer : lisPeer) {
                if (client.queryChannels(peer).contains(chainName))
                {
                	theChannel.addPeer(peer);
                }
                                
                Set<String> cs = client.queryChannels(peer);
                if (cs.contains(chainName))
                {
                	isExist = true;
                }
                logger.info("channels:"+cs.toString());
                
            }
            
            if (!isExist)
            {
            	return null;
            }*/

            for (Map<String,String> thePeerConfig : peerOrg.getPeerConfigList()){
            	String peerName = thePeerConfig.get("PEER_NAME");            	
                String eventLocation = thePeerConfig.get("EVENT_LOCATION");
                if (thePeer.getName().equals(peerName)){
                    EventHub eventHub = client.newEventHub(peerName,eventLocation,getEventHubProperties(eventLocation));
                    theChannel.addEventHub(eventHub);
                    break;
                }
            }            
            
            theChannel.setTransactionWaitTime(2*60*1000);
            theChannel.setDeployWaitTime(10*60*1000);

            theChannel.initialize();
            
            existChannel = theChannel;
    	}catch(Exception e)
    	{
    		
    	}        
        return existChannel;
    } 
    
    /**
     * 通过管理用户增加节点
     * @param peerOrg
     * @param peerName
     * @param peerLocation
     * @throws Exception
     */
    public static void joinPeer(RBCPeerOrg peerOrg,String peerName,String peerLocation)throws Exception{
    	RBCUser peerAdminUser = RBCUtils.getPeerAminUser();
		if (peerAdminUser == null)
		{
			return;
		}
		
        //Create instance of client.
        HFClient client = HFClient.createNewInstance();
        
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        
        Channel theChannel = getExistChain(client,peerOrg,peerAdminUser);
        
        if (theChannel == null){
        	return;
        }
        
        Peer peer = client.newPeer(peerName, peerLocation, getPeerProperties(peerName));
        
        theChannel.joinPeer(peer);
    }
    
    /**
     * 向Peer发送错误信息
     * @param httpRequest
     * @return
     * @throws Exception
     */
    public static byte[] httpRequest(RBCHttpRequest httpReq)throws Exception{
    	
		RBCPeerOrg peerOrg = RBCUtils.getRBCPeerOrg();
		SecurityCert securityCert = RBCUtils.getDefaultSecurityCert();
    
    	Channel theChannel = getChannel(peerOrg, securityCert.getRbcUser());

    	
    	Peer thePeer = theChannel.getPeers().iterator().next();
    	    	
    	RBCMessage rbcMessage = RBCMessage.newBuilder()
    			.setType(21)
    			.setData(httpReq.toByteString())
    			.build();
    	
    	Ab.BroadcastResponse bcRes = thePeer.sendMessage("",rbcMessage);
    	if (bcRes == null) {
    		RDPUtil.exception("send http message return null");
    	}
    	
    	return bcRes.getData().toByteArray();
    }

    private static Properties getPeerProperties(String name) {
    	Properties ret =  getEndPointProperties("peer", name);
        ret.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 1024*1024*50);
        ret.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 1024*1024*50);
        
        return ret;
    }

    private static Properties getEventHubProperties(String name) {
    	Properties ret =  getEndPointProperties("peer", name);

    	ret.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] {5L, TimeUnit.MINUTES});
    	ret.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] {8L, TimeUnit.SECONDS});
        
        return ret; //uses same as named peer
    }
    

    private static Properties getEndPointProperties(final String type, final String name) {
    	
        Properties ret = new Properties();

    	
    	if (!"true".equals(RBCUtils.getRBCProperties("tls.enabled")))
    	{
    		return ret;
    	}
        final String domainName = getDomainName(name);
        
        String filePath = "/";
        try{
            Resource resourcePath = new ClassPathResource("/");

        	filePath =  resourcePath.getFile().getPath();
        	
        }catch(Exception e)
        {
        	logger.error("get resource root path error",e);
        	RBCUtils.exception("get resource root path error");
        }
        
        File cert = Paths.get(filePath, "rbccrypto-config/ordererOrganizations".replace("orderer", type), domainName, type + "s",
                name, "tls/server.crt").toFile();
        if (!cert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
                    cert.getAbsolutePath()));
        }

        ret.setProperty("pemFile", cert.getAbsolutePath());
        //      ret.setProperty("trustServerCertificate", "true"); //testing environment only NOT FOR PRODUCTION!
        ret.setProperty("hostnameOverride", name);
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");

        return ret;
    }

    private static String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }

    }

    /**
     * 根据交易ID获取交易对象
     * @param channel
     * @param txId
     * @return
     */
    public static TransactionEntity getTransactionByID(Channel channel , String txId)
    {
    	TransactionEntity txEntity = new TransactionEntity();
    	try {
    		//BlockStatistics blockStatistics = channel.queryBlockStatistics(-1);
			TransactionInfo txInfo = channel.queryTransactionByID(txId);
			txEntity.setValidationCode(""+txInfo.getValidationCode().getNumber());
			
			Envelope envelope = txInfo.getEnvelope();
			txEntity.setTxSign(Base64.encode(envelope.getSignature().toByteArray()));
			
			Payload payload = Payload.parseFrom(envelope.getPayload());
			Transaction transaction = Transaction.parseFrom(payload.getData());
			ChannelHeader chr = ChannelHeader.parseFrom(payload.getHeader().getChannelHeader());
			ChaincodeSpec ccs = ChaincodeSpec.parseFrom(chr.getExtension()) ;
			String chainCodeId = "";
			if (ccs != null && ccs.getChaincodeId() != null)
			{
				chainCodeId = ccs.getChaincodeId().getName();
				txEntity.setChainCodeId(chainCodeId);
			}
			
			SignatureHeader sighHd = SignatureHeader.parseFrom(payload.getHeader().getSignatureHeader());
			SerializedIdentity sid = SerializedIdentity.parseFrom(sighHd.getCreator());
			
			txEntity.setTxId(chr.getTxId());
			txEntity.setTxTime(StringUtil.dateTimeToStr(new Date(chr.getTimestamp().getSeconds()*1000)));
			txEntity.setTxCert(sid.getIdBytes().toStringUtf8());

			if (transaction.getActions(0).getHeader() != null){
				SignatureHeader asighHd =  SignatureHeader.parseFrom(transaction.getActions(0).getHeader());
				if (asighHd.getCreator() != null){
					SerializedIdentity asid = SerializedIdentity.parseFrom(asighHd.getCreator());
					if (sid.getIdBytes() != null){
						txEntity.setTxCert(asid.getIdBytes().toStringUtf8());
					}
				}
			}

			
			ChaincodeActionPayload chainCodeAction = ChaincodeActionPayload.parseFrom(transaction.getActions(0).getPayload());
			ChaincodeProposalPayload chaincodeProposalPayload = ChaincodeProposalPayload.parseFrom(chainCodeAction.getChaincodeProposalPayload());
			//FabricProposal.Proposal proposal = FabricProposal.Proposal.parseFrom(chaincodeProposalPayload.getInput());
			ChaincodeInvocationSpec chaincodeInvocationSpec = ChaincodeInvocationSpec.parseFrom(chaincodeProposalPayload.getInput());
			List<ByteString> args = chaincodeInvocationSpec.getChaincodeSpec().getInput().getArgsList();
			String strArgs = "";
			for (ByteString byteArgs : args)
			{
				strArgs+=byteArgs.toStringUtf8()+",";
			}
			if (strArgs.endsWith(","))
			{
				strArgs= strArgs.substring(0, strArgs.length()-1);
			}
			
			txEntity.setArgs(strArgs);
			ProposalResponsePayload proposalResponsePayload =ProposalResponsePayload.parseFrom(chainCodeAction.getAction().getProposalResponsePayload());
			ChaincodeAction chaincodeAction = ChaincodeAction.parseFrom(proposalResponsePayload.getExtension());
			TxReadWriteSet txReadWriteSet = TxReadWriteSet.parseFrom(chaincodeAction.getResults());
			
			String results = "";
			for (NsReadWriteSet nsReadWriteSet : txReadWriteSet.getNsRwsetList())
			{
				try
				{
				KVRWSet kVRWSet = KVRWSet.parseFrom(nsReadWriteSet.getRwset());
				results += kVRWSet.getWritesList().toString();
				}catch(Exception e1)
				{
					
				}

			}
			txEntity.setRWResult(results);

			
		} catch (Exception e) {
			return null;
		}
    	
    	
    	return txEntity;
    }
    
    /**
     * 查询当前orderer所有节点列表
     * @param channel
     * @return
     */
    public static PeerList getPeerList(Channel channel)
    {
    	PeerList peerList = null;
    	try
    	{
    		peerList = channel.queryPeerList();
    	}catch(Exception e)
    	{
    		logger.error("getPeerList exception ", e);
    	}
    	return peerList;
    }
    
	/**
	 * 查询区块的统计信息
	 * 
	 * @param channel
	 * @param blockIndex
	 * @return
	 */
	public static BlockStatistics queryBlockStatistics(Channel channel, int blockIndex) {
		try {
			return channel.queryBlockStatistics(blockIndex);
		} catch (ProposalException e) {
			e.printStackTrace();
			logger.error("queryBlockStatistics exception ", e);
			return null;
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
			logger.error("queryBlockStatistics exception ", e);
			return null;
		}
	}
	
	/**
	 * 查询交易
	 * 
	 * @param channel
	 * @param txID
	 * @return
	 */
	public static List<String> queryStatisticsTransaction(Channel channel, String txId,String transStaticKey) {
		try {
			return channel.queryStatisticsTransaction(txId,transStaticKey);
		} catch (InvalidArgumentException e) {
			e.printStackTrace();
			logger.error("queryStatisticsTransaction exception ", e);
			return null;
		} catch (ProposalException e) {
			e.printStackTrace();
			logger.error("queryStatisticsTransaction exception ", e);
			return null;
		}

	}
	
	
	public static Map<String, Object> getPerformance(Channel channel){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		int blockSizeNow = 0;
		int blockSizeOver = 0;
		int blockSizePass = 0;
		
		//当前块
		BlockStatistics blockStatisticsNow = queryBlockStatistics(channel, -1);
		long blockNoNow = blockStatisticsNow.getNumber();
		List<transactionStatistics> list = blockStatisticsNow.getListList();
		for(int i=0;i<list.size();i++){
			transactionStatistics trans = list.get(i);
			String chaincodeId = trans.getChaincodeId();
			String funcName = trans.getFunc();
			String validateCode = trans.getValidationCode();
			if(StringUtil.isEmpty(chaincodeId) && StringUtil.isEmpty(funcName) && "0".equals(validateCode)){
				blockSizeNow = trans.getTxSum();
				break;
			}
		}
		
		//上一个块
		long blockNoOver = blockNoNow - 1;
		if(blockNoOver < 1){
			blockNoOver = 1;
		}
		BlockStatistics blockStatisticsOver = queryBlockStatistics(channel, (int) blockNoOver);
		List<transactionStatistics> listOver = blockStatisticsOver.getListList();
		for(int i=0;i<listOver.size();i++){
			transactionStatistics trans = listOver.get(i);
			String chaincodeId = trans.getChaincodeId();
			String funcName = trans.getFunc();
			String validateCode = trans.getValidationCode();
			if(StringUtil.isEmpty(chaincodeId) && StringUtil.isEmpty(funcName) && "0".equals(validateCode)){
				blockSizeOver = trans.getTxSum();
				break;
			}
		}
		
		//前100个块
		long blockNoPass = blockNoNow - 100;
		if(blockNoPass < 1){
			blockNoPass = 1;
		}
		BlockStatistics blockStatisticsPass = queryBlockStatistics(channel, (int) blockNoPass);
		List<transactionStatistics> listPass = blockStatisticsPass.getListList();
		for(int i=0;i<listPass.size();i++){
			transactionStatistics trans = listPass.get(i);
			String chaincodeId = trans.getChaincodeId();
			String funcName = trans.getFunc();
			String validateCode = trans.getValidationCode();
			if(StringUtil.isEmpty(chaincodeId) && StringUtil.isEmpty(funcName) && "0".equals(validateCode)){
				blockSizePass = trans.getTxSum();
				break;
			}
		}
		
		int blockRecent = blockSizeNow - blockSizeOver;//当前块的交易量
		double blockMax = (blockSizeNow - blockSizePass) * 0.2;//最大吞吐量
		double blockAverage = (blockSizeNow - blockSizePass) * 0.01;//平均吞吐量
		
		returnMap.put("blockAll", blockSizeNow);//交易总量
		returnMap.put("blockRecent", blockRecent);//当前块的交易量
		returnMap.put("blockMax", blockMax);//最大吞吐量
		returnMap.put("blockAverage", blockAverage);//平均吞吐量
		System.out.println(returnMap);
		return returnMap;
	}
	
}
