package com.trygrpc.greeting.client;

import generatedclass.proto.greet.*;
import grpc.generatedclass.proto.dummy.DummyServiceGrpc;
import grpc.generatedclass.proto.greet.GreetServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("hello from grpc client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() {
        //this channel is our transport
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() //force ssl to not come up
                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
//        doClientStreamingCall(channel);
        doBiDiStreamingCall(channel);

        System.out.println("shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel){
        //creating synchronous client
//        DummyServiceGrpc.DummyServiceBlockingStub syncClient =
//                DummyServiceGrpc.newBlockingStub(channel);

//        DummyServiceGrpc.DummyServiceFutureStub asyncClient =
//                DummyServiceGrpc.newFutureStub(channel);

        System.out.println("creating stub");
        //created a greet service client (blocking --> synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient =
                GreetServiceGrpc.newBlockingStub(channel);

        //unary
        //created a protocol buffer greeting message
        //prepare request
        Greeting greeting = Greeting.newBuilder()
                            .setFirstName("n1")
                            .setLastName("n2")
                            .build();

        //do same for greetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder()
                                    .setGreeting(greeting)
                                    .build();

        //call the rpc and get back the greet response (protocol buffers)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        System.out.println(greetResponse.getResult());

    }

    private void doServerStreamingCall(ManagedChannel channel){
        //server streaming
        System.out.println("creating stub");
        GreetServiceGrpc.GreetServiceBlockingStub greetClient =
                GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("n1")
                .setLastName("n2")
                .build();
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        //we stream the responses (in a blocking manner)
        //as long as the server does not send onCompleted,
        //this is expecting a response.
        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());

                });
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        // create a client (stub)
        //long greet is streaming client thus it has to be async client
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                //we get a response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
                //onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                //we get an error from the server
            }

            @Override
            public void onCompleted() {
                //the server is done sending us the data
                System.out.println("server has completed sending us something");
                countDownLatch.countDown();
                //onCompleted will be called right after onNext

            }
        });

        // streaming message #1
        System.out.println("sending message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder().
                setGreeting(Greeting.newBuilder()
                        .setFirstName("n1")).
                build());

        // streaming message #2
        System.out.println("sending message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder().
                setGreeting(Greeting.newBuilder()
                        .setFirstName("n2")).
                build());

        //streaming message #3
        System.out.println("sending message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder().
                setGreeting(Greeting.newBuilder()
                        .setFirstName("n3")).
                build());

        //we tell the server that the client is done sending the data
        requestObserver.onCompleted();

        try {
            countDownLatch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asynClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver =
                asynClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
                    @Override
                    public void onNext(GreetEveryoneResponse value) {
                        //server sends the response to responseobserver
                        System.out.println("response from server: "+value.getResult());
                    }

                    @Override
                    public void onError(Throwable t) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("server is done sending the data");
                        countDownLatch.countDown();
                    }
                });

        Arrays.asList("n1", "n2", "n3", "n4").forEach(name -> {

                    System.out.println("sending.. "+name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name).build())
                            .build());

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                );

        requestObserver.onCompleted();
        try {
            countDownLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
