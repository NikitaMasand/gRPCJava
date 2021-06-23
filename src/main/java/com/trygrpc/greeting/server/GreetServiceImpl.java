package com.trygrpc.greeting.server;

import generatedclass.proto.greet.*;
import grpc.generatedclass.proto.greet.GreetServiceGrpc.GreetServiceImplBase;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceImplBase {
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        //the method returns void
        //basically we have to do something to the request
        //and return it to the responseObserver
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        String result = "Hello "+ firstName;
        GreetResponse response = GreetResponse.newBuilder()
                                                .setResult(result)
                                                .build();
        //because the server is asynchronous, we can't directly return response, we
        //have something called stream observer

        //send the response
        responseObserver.onNext(response);

        //complete the RPC call
        responseObserver.onCompleted();
//        super.greet(request, responseObserver);
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {

        String firstName = request.getGreeting().getFirstName();
        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + firstName + " response num: " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();

                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            responseObserver.onCompleted();
        }
//        super.greetManyTimes(request, responseObserver);
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StreamObserver<LongGreetRequest> requestOberver = new StreamObserver<LongGreetRequest>() {
            String result = "";
            @Override
            public void onNext(LongGreetRequest value) {
                //client sends a message
                result += "hello "+ value.getGreeting().getFirstName() + " ! ";
            }

            @Override
            public void onError(Throwable t) {
                //client sends an error
            }

            @Override
            public void onCompleted() {
                //client is done
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(result).build());
                responseObserver.onCompleted();
            }
        };
        return requestOberver;
//        return super.longGreet(responseObserver);
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        StreamObserver<GreetEveryoneRequest> requestObserver = new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                //client sends the request
                String result = "Hello " + value.getGreeting().getFirstName();
                GreetEveryoneResponse response = GreetEveryoneResponse.newBuilder()
                                                    .setResult(result)
                                                    .build();

                //the server should also send the response
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
//        return super.greetEveryone(responseObserver);
    }
}
