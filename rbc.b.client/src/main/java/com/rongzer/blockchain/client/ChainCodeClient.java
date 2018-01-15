package com.rongzer.blockchain.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.protos.peer.Query.ChaincodeInfo;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionEvent;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.rongzer.blockchain.orguser.RBCUser;
import com.rongzer.chaincode.entity.ApprovalEntity;
import com.rongzer.utils.RBCUtils;
import com.rongzer.utils.StringUtil;

@Service
public class ChainCodeClient {
    private static final Log logger = LogFactory.getLog(ChainCodeClient.class);
    
    /**
     * 发布ChainCode
     * @param channel
     * @param rbcUser
     * @param langType
     * @param chainCodeName
     * @param chainCodeVersion
     * @param desc
     * @param chainCodeBytes
     * @return
     */
    public static String deployChainCode(Channel channel,RBCUser rbcUser,Type langType,String chainCodeName,String chainCodeVersion,String desc,byte[] chainCodeBytes)
    {
    	return deployChainCode(channel,rbcUser,langType,chainCodeName,chainCodeVersion,desc,chainCodeBytes,null);
	}
    
    /**
     * 发布ChainCode
     * @param channel
     * @param rbcUser
     * @param langType
     * @param chainCodeName
     * @param chainCodeVersion
     * @param desc
     * @param chainCodeBytes
     * @param policy
     * @return
     */
    public static String deployChainCode(Channel channel,RBCUser rbcUser,Type langType,String chainCodeName,String chainCodeVersion,String desc,byte[] chainCodeBytes,String policy)
    {
		final String[] txReturn = new String[2];

    	try
    	{   
    		
    		channel.setTransactionWaitTime(10*60*1000);
    		channel.setDeployWaitTime(10*60*1000);

            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed = new LinkedList<>();
            
            InstallProposalRequest installProposalRequest = channel.getClient().newInstallProposalRequest();

            ChaincodeID chainCodeID = ChaincodeID.newBuilder().setName(chainCodeName)
                    .setVersion(chainCodeVersion)
                    .setPath(chainCodeName).build();
            
            installProposalRequest.setChaincodeID(chainCodeID);
            if (langType.equals(Type.JAVA))
            {
            	installProposalRequest.setChaincodePath(null);
            }
            installProposalRequest.setChaincodeLanguage(langType);
            ////For GO language and serving just a single user, chaincodeSource is mostly likely the users GOPATH
            if (chainCodeBytes!= null && chainCodeBytes.length>0)
            {
                installProposalRequest.setChaincodeInputStream(new ByteArrayInputStream(chainCodeBytes));            
            }else
            {
    			Resource resource = new ClassPathResource("/chaincode");	
                installProposalRequest.setChaincodeSourceLocation(resource.getFile());
            }
            installProposalRequest.setChaincodeVersion(chainCodeVersion);
            installProposalRequest.setChaincodeSpecDesc(desc);

            // only a client from the same org as the peer can issue an install request
            int numInstallProposal = 0;
            //   for (SampleOrg org : testSampleOrgs) {
            //chain.getClient().setUserContext(RBCUtils.getPeerAminUser());
            Collection<Peer> peersFromOrg = channel.getPeers();
            numInstallProposal = numInstallProposal + peersFromOrg.size();
            successful.clear();
            failed.clear();
            Collection<ProposalResponse> responses = channel.getClient().sendInstallProposal(installProposalRequest, peersFromOrg);

            for (ProposalResponse response : responses) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                	logger.info(String.format("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
                    successful.add(response);
                } else {
                    failed.add(response);
                    return response.getMessage();
                }
            }
            //   }
            logger.info(String.format("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size()));

            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                logger.error(String.format("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage()));
            }
            //Note install chain code does not require transaction no need to send to Orderers
            
		   Peer peer0 = null;
		   for (Peer peer : channel.getPeers()) { 
			   peer0 = peer;
			   break;
            }
		   
			List<ChaincodeInfo> lisExistChainCodeInfo = channel.queryInstantiatedChaincodes(peer0);
			 
			boolean hasInstitute = false;
            for (ChaincodeInfo chaincodeInfo : lisExistChainCodeInfo) {
               
                if (chaincodeInfo.getName().equals(chainCodeName))
                {
                	hasInstitute = true;
                	break;
                }
            }
           
            String approvalType = "deployChainCode";
            if (hasInstitute)
            {
            	approvalType = "upgradeChainCode";
            }
            
        	List<String> approvalArgs = new ArrayList<String>();
        	approvalArgs.add(RBCUtils.getChannelName());
        	approvalArgs.add(chainCodeName);
        	approvalArgs.add(chainCodeVersion);
        	if (policy != null)
        	{
                approvalArgs.add(policy);

        	}else
        	{
        		ChaincodeEndorsementPolicy endorsementPolicy = new ChaincodeEndorsementPolicy();
        		Resource resourcePolicy = new ClassPathResource("/other/chaincodeendorsementpolicy.yaml");			

        		endorsementPolicy.fromYamlFile(resourcePolicy.getFile());
        		approvalArgs.add(new String(endorsementPolicy.getChaincodeEndorsementPolicyAsBytes(),"UTF-8"));
        	}
    
            ApprovalEntity approvalEntity = new ApprovalEntity();
            approvalEntity.setApprovalId(StringUtil.getUuid32());
            approvalEntity.setApprovalName(approvalType +"["+chainCodeName+":"+chainCodeVersion+"]");
            approvalEntity.setApprovalType(approvalType);//发布版本
            approvalEntity.setVetoType("1");//一票否决
            approvalEntity.setChaincode("lscc");
            approvalEntity.setFunc("updateToVersion");
            approvalEntity.setDesc(desc);
            
            approvalEntity.setArgs(approvalArgs);
            
            List<String> lsccArgs = new ArrayList<String>();
            lsccArgs.add("newApproval");
            lsccArgs.add(approvalEntity.toJSON().toString());


            CompletableFuture<TransactionEvent> future = invoke(channel,rbcUser,"rbcapproval",lsccArgs);
            if (future != null) {
            	future.get(60*10, TimeUnit.SECONDS);
            }
            
			if (future != null){
				Function<TransactionEvent,String> fn = new Function<TransactionEvent,String>(){
					
					@Override
					public String apply(TransactionEvent t) {

						txReturn[0] = t.getTransactionID();
						txReturn[1] = t.getErrorMsg();
				          logger.info(String.format("deploy chaincode txid:%s,msg:%s",txReturn[0],txReturn[1]));

						return null;
					}};
				future.thenApply(fn).get(600, TimeUnit.SECONDS);
			}
          
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	if (StringUtil.isNotEmpty(txReturn[1])){
    		return txReturn[1];
    	}
    	
    	return "OK";
    }

    /**
     * 执行智能合约
     * @param channel
     * @param rbcUser
     * @param chainCodeName
     * @param lisArgs
     * @return
     * @throws Exception
   
    public static CompletableFuture<TransactionEvent> invoke(Channel channel,RBCUser rbcUser,String chainCodeName,List<String> lisArgs) throws Exception
    {
		Map<String,String> logTime= (Map<String,String>)MDC.get("TIME_LOG");
		long bTime = new Date().getTime();	
		
        final String chainName = channel.getName();
        logger.info(String.format("Running Chain %s", chainName));

        final ChaincodeID chaincodeID;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        chaincodeID = ChaincodeID.newBuilder().setName(chainCodeName)
                .setPath(chainCodeName).build();
        

        successful.clear();
        failed.clear();
        channel.getClient().setUserContext(rbcUser); // select the user for all subsequent requests

        ///////////////
        /// Send transaction proposal to all peers
        TransactionProposalRequest transactionProposalRequest = channel.getClient().newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setArgs(new ArrayList<String>(lisArgs));
        logger.info("sending transactionProposal to all peers with arguments:");
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));  /// This should be returned see chaincode.
        transactionProposalRequest.setTransientMap(tm2);
        if (false)
        {     
    	 	Thread.sleep(50);

    		long eTime = new Date().getTime();
    		if (logTime != null) logTime.put("ChainCodeClientA", ""+(eTime-bTime));

        	return null;
        }
        
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
            
    			logger.info(String.format("Successful transaction proposal response Txid: %s from peer msg : %s", response.getTransactionID(), response.getProposalResponse().getResponse().getMessage()));
                successful.add(response);
            } else {
                failed.add(response);
            }
        }
        
        // Check that all the proposals are consistent with each other. We should have only one set
        // where all the proposals above are consistent.
        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
        if (proposalConsistencySets.size() != 1) {
            RDPUtil.exception(String.format("Expected only one set of consistent proposal responses but got %d", proposalConsistencySets.size()));
        }
        
        logger.info(String.format("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",transactionPropResp.size(), successful.size(), failed.size()));
        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
            logger.error("Not enough endorsers for invoke(move b,a,1):" + failed.size() + " endorser error: " +
                    firstTransactionProposalResponse.getMessage() +
                    ". Was verified: " + firstTransactionProposalResponse.isVerified());
            //发出异常终止，返回异常消息
        	RBCUtils.exception(firstTransactionProposalResponse.getMessage());
        }
        
        logger.info("Successfully received transaction proposal responses");
        
        CompletableFuture<TransactionEvent> future = channel.sendTransaction(successful);     
        
		long eTime = new Date().getTime();
		if (logTime != null) logTime.put("ChainCodeClientA", ""+(eTime-bTime));
        ////////////////////////////
        // Send Transaction Transaction to orderer
        return future;
    }
    
      */

    /**
     * 过桥Peer执行智能合约
     * @param channel
     * @param rbcUser
     * @param chainCodeName
     * @param lisArgs
     * @return
     * @throws Exception
     */
    public static CompletableFuture<TransactionEvent> invoke(Channel channel,RBCUser rbcUser,String chainCodeName,List<String> lisArgs) throws Exception
    {		
        final String chainName = channel.getName();
        logger.info(String.format("Running Chain %s", chainName));

        final ChaincodeID chaincodeID;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        chaincodeID = ChaincodeID.newBuilder().setName(chainCodeName)
                .setPath(chainCodeName).build();
        

        successful.clear();
        failed.clear();
        channel.getClient().setUserContext(rbcUser); // select the user for all subsequent requests

        ///////////////
        /// Send transaction proposal to all peers
        TransactionProposalRequest transactionProposalRequest = channel.getClient().newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setArgs(new ArrayList<String>(lisArgs));
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));  /// This should be returned see chaincode.
        transactionProposalRequest.setTransientMap(tm2);

        CompletableFuture<TransactionEvent> future = channel.sendExecTransaction(transactionProposalRequest);

        ////////////////////////////
        // Send Transaction Transaction to orderer
        return future;
    }
    
    
    /**
     * 查询智能合约
     * @param channel
     * @param rbcUser
     * @param chainCodeName
     * @param lisArgs
     * @return
     * @throws Exception
     */
    public static ByteString query(Channel channel,RBCUser rbcUser,String chainCodeName,List<String> lisArgs) throws Exception
    {
    	ByteString bReturn = null;

    	channel.setTransactionWaitTime(60);

        final ChaincodeID chaincodeID;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        chaincodeID = ChaincodeID.newBuilder().setName(chainCodeName)
                .setPath(chainCodeName).build();
        

        successful.clear();
        failed.clear();
        channel.getClient().setUserContext(rbcUser); // select the user for all subsequent requests

        ////////////////////////////
        // Send Query Proposal to all peers
        //
        logger.info(String.format("Now query chain code %s ",chainCodeName));
        QueryByChaincodeRequest queryByChaincodeRequest = channel.getClient().newQueryProposalRequest();
        queryByChaincodeRequest.setArgs(new ArrayList<String>(lisArgs));
        queryByChaincodeRequest.setFcn("invoke");
        queryByChaincodeRequest.setChaincodeID(chaincodeID);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);
        
        Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
        for (ProposalResponse proposalResponse : queryProposals) {
            if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
            	logger.error("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() +
                        ". Messages: " + proposalResponse.getMessage()
                        + ". Was verified : " + proposalResponse.isVerified());
            	
                //发出异常终止，返回异常消息
            	RBCUtils.exception(proposalResponse.getMessage());
            } else {
            	bReturn = proposalResponse.getProposalResponse().getResponse().getPayload();
            }
        }
        
        logger.info(String.format("Successfully query chain code %s ",chainCodeName));

        return bReturn;

    }
    
    
}
