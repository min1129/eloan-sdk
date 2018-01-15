package org.hyperledger.fabric.sdk;

public interface TransactionEvent{
	
    public void setTransactionID(String transactionID);
    
    public String getTransactionID();
    
    public void setErrorMsg(String errMsg);
    
    public String getErrorMsg();
    
    public EventHub getEventHub();
    
    public boolean isValid();
    
    public byte getValidationCode();


    
}