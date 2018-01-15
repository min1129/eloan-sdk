package org.hyperledger.fabric.sdk;

public class RejectionTransactionEvent implements TransactionEvent{
	
	private final EventHub eventHub;
  
    String transactionID = "";
    String errMsg = "";
    
    public RejectionTransactionEvent(EventHub eventHub){
    	this.eventHub = eventHub;
    }
    
    public void setTransactionID(String transactionID){
    	this.transactionID= transactionID;
    }
    
    public String getTransactionID(){
    	return transactionID;
    }
    
    public void setErrorMsg(String errMsg){
    	this.errMsg= errMsg;
    }
    
    public String getErrorMsg(){
    	return this.errMsg;
    }

	@Override
	public EventHub getEventHub() {
		return eventHub;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public byte getValidationCode() {
		return -1;
	}
	

}
