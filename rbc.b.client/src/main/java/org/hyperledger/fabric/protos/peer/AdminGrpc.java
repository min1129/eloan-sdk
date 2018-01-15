package org.hyperledger.fabric.protos.peer;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * Interface exported by the server.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.3.0)",
    comments = "Source: peer/admin.proto")
public final class AdminGrpc {

  private AdminGrpc() {}

  public static final String SERVICE_NAME = "protos.Admin";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> METHOD_GET_STATUS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "protos.Admin", "GetStatus"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> METHOD_START_SERVER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "protos.Admin", "StartServer"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest,
      org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> METHOD_GET_MODULE_LOG_LEVEL =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "protos.Admin", "GetModuleLogLevel"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest,
      org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> METHOD_SET_MODULE_LOG_LEVEL =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "protos.Admin", "SetModuleLogLevel"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.google.protobuf.Empty> METHOD_REVERT_LOG_LEVELS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "protos.Admin", "RevertLogLevels"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.google.protobuf.Empty.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AdminStub newStub(io.grpc.Channel channel) {
    return new AdminStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AdminBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AdminBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static AdminFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AdminFutureStub(channel);
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static abstract class AdminImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Return the serve status.
     * </pre>
     */
    public void getStatus(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_STATUS, responseObserver);
    }

    /**
     */
    public void startServer(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_START_SERVER, responseObserver);
    }

    /**
     */
    public void getModuleLogLevel(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_MODULE_LOG_LEVEL, responseObserver);
    }

    /**
     */
    public void setModuleLogLevel(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SET_MODULE_LOG_LEVEL, responseObserver);
    }

    /**
     */
    public void revertLogLevels(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REVERT_LOG_LEVELS, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_GET_STATUS,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus>(
                  this, METHODID_GET_STATUS)))
          .addMethod(
            METHOD_START_SERVER,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus>(
                  this, METHODID_START_SERVER)))
          .addMethod(
            METHOD_GET_MODULE_LOG_LEVEL,
            asyncUnaryCall(
              new MethodHandlers<
                org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest,
                org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse>(
                  this, METHODID_GET_MODULE_LOG_LEVEL)))
          .addMethod(
            METHOD_SET_MODULE_LOG_LEVEL,
            asyncUnaryCall(
              new MethodHandlers<
                org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest,
                org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse>(
                  this, METHODID_SET_MODULE_LOG_LEVEL)))
          .addMethod(
            METHOD_REVERT_LOG_LEVELS,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                com.google.protobuf.Empty>(
                  this, METHODID_REVERT_LOG_LEVELS)))
          .build();
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class AdminStub extends io.grpc.stub.AbstractStub<AdminStub> {
    private AdminStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdminStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdminStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdminStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return the serve status.
     * </pre>
     */
    public void getStatus(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_STATUS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startServer(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START_SERVER, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getModuleLogLevel(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_MODULE_LOG_LEVEL, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setModuleLogLevel(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request,
        io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_MODULE_LOG_LEVEL, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void revertLogLevels(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REVERT_LOG_LEVELS, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class AdminBlockingStub extends io.grpc.stub.AbstractStub<AdminBlockingStub> {
    private AdminBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdminBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdminBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdminBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return the serve status.
     * </pre>
     */
    public org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus getStatus(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_STATUS, getCallOptions(), request);
    }

    /**
     */
    public org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus startServer(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_START_SERVER, getCallOptions(), request);
    }

    /**
     */
    public org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse getModuleLogLevel(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_MODULE_LOG_LEVEL, getCallOptions(), request);
    }

    /**
     */
    public org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse setModuleLogLevel(org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_MODULE_LOG_LEVEL, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty revertLogLevels(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REVERT_LOG_LEVELS, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class AdminFutureStub extends io.grpc.stub.AbstractStub<AdminFutureStub> {
    private AdminFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdminFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdminFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdminFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return the serve status.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> getStatus(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_STATUS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus> startServer(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START_SERVER, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> getModuleLogLevel(
        org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_MODULE_LOG_LEVEL, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse> setModuleLogLevel(
        org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_MODULE_LOG_LEVEL, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> revertLogLevels(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REVERT_LOG_LEVELS, getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_STATUS = 0;
  private static final int METHODID_START_SERVER = 1;
  private static final int METHODID_GET_MODULE_LOG_LEVEL = 2;
  private static final int METHODID_SET_MODULE_LOG_LEVEL = 3;
  private static final int METHODID_REVERT_LOG_LEVELS = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AdminImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(AdminImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_STATUS:
          serviceImpl.getStatus((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus>) responseObserver);
          break;
        case METHODID_START_SERVER:
          serviceImpl.startServer((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.ServerStatus>) responseObserver);
          break;
        case METHODID_GET_MODULE_LOG_LEVEL:
          serviceImpl.getModuleLogLevel((org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest) request,
              (io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse>) responseObserver);
          break;
        case METHODID_SET_MODULE_LOG_LEVEL:
          serviceImpl.setModuleLogLevel((org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelRequest) request,
              (io.grpc.stub.StreamObserver<org.hyperledger.fabric.protos.peer.AdminPackage.LogLevelResponse>) responseObserver);
          break;
        case METHODID_REVERT_LOG_LEVELS:
          serviceImpl.revertLogLevels((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class AdminDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.hyperledger.fabric.protos.peer.AdminPackage.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (AdminGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AdminDescriptorSupplier())
              .addMethod(METHOD_GET_STATUS)
              .addMethod(METHOD_START_SERVER)
              .addMethod(METHOD_GET_MODULE_LOG_LEVEL)
              .addMethod(METHOD_SET_MODULE_LOG_LEVEL)
              .addMethod(METHOD_REVERT_LOG_LEVELS)
              .build();
        }
      }
    }
    return result;
  }
}
