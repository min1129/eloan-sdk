package com;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import protos.Fabric.Message;
import protos.PeerGrpc;
import protos.PeerGrpc.PeerStub;

import com.google.protobuf.ByteString;
import com.rongzer.blockchain.client.ChannelPool;

public class TestChat {
	public static void main(String[] args) throws Exception {
        final CountDownLatch finishLatch = new CountDownLatch(1);

		ManagedChannel channel = ChannelPool.getChannel("192.168.1.16", 30303);
		PeerStub peerStub = PeerGrpc.newStub(channel);

		Message req = Message.newBuilder().setPayload(ByteString.copyFromUtf8("aaaa")).build();	
		StreamObserver<Message> responseObserver = 
				new StreamObserver<Message>() {

					@Override
					public void onNext(Message message) {
						System.out.println("MSG:"+message);
					}

					@Override
					public void onError(Throwable e) {
						System.out.println("ERROR:"+e.toString());

					}

					@Override
					public void onCompleted() {
						System.out.println("ON COMPLETE");

					}

				};
	
		
		StreamObserver<Message> nso = peerStub.chat(responseObserver);
		
		nso.onNext(req);
		
        finishLatch.await(2, TimeUnit.MINUTES);

        System.out.println("OVER");
	}
}
