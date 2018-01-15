package org.hyperledger.fabric.sdk;

public interface BaseEvent {

	public int getEventCase();
	
	public String getChannelId();
	 
	public Iterable<TransactionEvent> getTransactionEvents();
}
