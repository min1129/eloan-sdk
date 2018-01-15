package org.hyperledger.fabric.sdk;

import java.util.Iterator;

import org.hyperledger.fabric.protos.peer.PeerEvents.Event;

public class RejectionEvent implements BaseEvent{
	private final EventHub eventHub;
    private String channelId = "";
    private String transactionId= "";
    private String errMsg= "";
    
    
	public RejectionEvent(EventHub eventHub, Event event)
	{
		this.eventHub = eventHub;
		this.channelId = event.getRejection().getChainId();
		this.transactionId = event.getRejection().getTxId();
		this.errMsg = event.getRejection().getErrorMsg();
		
	}
	
	@Override
	public int getEventCase() {
		return 4;
	}
	
	@Override
	public String getChannelId() {
		return channelId;
	}

	@Override
	public Iterable<TransactionEvent> getTransactionEvents() {

		return new TransactionEventIterable();
	}
	
    class TransactionEventIterable implements Iterable<TransactionEvent> {

        @Override
        public Iterator<TransactionEvent> iterator() {
            return new TransactionEventIterator();
        }
    }

   
    class TransactionEventIterator implements Iterator<TransactionEvent> {
        int ci = 0;
        final int max;

        TransactionEventIterator() {
            max = 1;

        }

        @Override
        public boolean hasNext() {
            return ci < max;

        }

        @Override
        public TransactionEvent next() {
        	TransactionEvent te = new RejectionTransactionEvent(eventHub);
        	te.setTransactionID(transactionId);
        	te.setErrorMsg(errMsg);
            return te;            
        }

    }

}
