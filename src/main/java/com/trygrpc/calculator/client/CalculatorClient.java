package com.trygrpc.calculator.client;

import generatedclass.proto.calculator.*;
import grpc.generatedclass.proto.calculator.CalculatorServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) {
        System.out.println("hello from calculator client");
        CalculatorClient calculatorClient = new CalculatorClient();
        calculatorClient.run();
    }

    private void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                .usePlaintext()
                .build();

//        doUnary(channel);
//        doServerStreaming(channel);
//        doClientStreaming(channel);
//        doBiDiStreaming(channel);
        doErrorCall(channel);
        System.out.println("shutting down channel");
        channel.shutdown();
    }

    private void doBiDiStreaming(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                //server sends a response
                System.out.println("maximum value: "+value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("server is done sending the responses");
                countDownLatch.countDown();
            }
        });

        Arrays.asList(10, 30, 20, 40, 50, 10).forEach(num -> {
            System.out.println("sending num "+num);
            requestObserver.onNext(FindMaximumRequest.newBuilder().setNumber(num).build());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        requestObserver.onCompleted();
        try {
            countDownLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }

    private void doUnary(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
//        unary
        SumRequest sumRequest = SumRequest.newBuilder()
                                                    .setFirstNum(5)
                                                    .setSecondNum(10)
                                                    .build();

        SumResponse sumResponse = stub.sum(sumRequest);
        System.out.println(sumRequest.getFirstNum() + " + "+ sumRequest.getSecondNum() + " = "+ sumResponse.getResult());
    }

    private void doServerStreaming(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        //server streaming
        Integer number = 678;

        PrimeNumberDecompositionRequest request = PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(number)
                .build();

        stub.primeNumberDecomposition(request).forEachRemaining(primeNumberDecompositionResponse -> {
            System.out.println(primeNumberDecompositionResponse.getPrimeFactor());
        });
    }

    private void doClientStreaming(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestObserver =
                asyncClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                //server sends a response only once
                System.out.println("Received a response from the server");
                System.out.println(value.getAverage());
            }

            @Override
            public void onError(Throwable t) {
                //server sends an error
            }

            @Override
            public void onCompleted() {
                //server is done sending the response
                //this is called right after onNext() is called
                System.out.println("server is done sending the response");
                countDownLatch.countDown();
            }
        });

        System.out.println("client sending message #1");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                                .setNumber(10)
                                .build());
        System.out.println("client sending message #2");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(15)
                .build());

        System.out.println("client sending message #3");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(20)
                .build());

        System.out.println("client sending message #4");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(30)
                .build());

        System.out.println("client sending message #5");
        requestObserver.onNext(ComputeAverageRequest.newBuilder()
                .setNumber(35)
                .build());

        // client is done sending the messages
        requestObserver.onCompleted();
        try {
            countDownLatch.await(1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doErrorCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        int num = -1;
        try {
            stub.squareRoot(SquareRootRequest.newBuilder().setNumber(num).build());
        }
        catch (StatusRuntimeException e) {
            System.out.println("got an exception for square root");
            e.printStackTrace();
        }
    }

}
