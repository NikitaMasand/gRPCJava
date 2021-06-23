package com.trygrpc.calculator.server;

import generatedclass.proto.calculator.*;
import grpc.generatedclass.proto.calculator.CalculatorServiceGrpc.CalculatorServiceImplBase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceImplBase {
    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        Integer number = request.getNumber();
        Integer divisor = 2;
        while (number>1){
            if(number%divisor==0){
                number = number/divisor;
                responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder()
                                        .setPrimeFactor(divisor).build()
                );
            }
            else {
                divisor = divisor+1;
            }
        }

        responseObserver.onCompleted();
//        super.primeNumberDecomposition(request, responseObserver);
    }

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {

        int firstNum = request.getFirstNum();
        int secondNum = request.getSecondNum();

        int result = firstNum + secondNum;

        SumResponse response = SumResponse.newBuilder()
                                            .setResult(result)
                                            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
//        super.sum(request, responseObserver);
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        StreamObserver<ComputeAverageRequest> requestObserver = new StreamObserver<ComputeAverageRequest>() {
            double prev_avg = 0;
            double new_avg = 0;
            int count = 0;
            @Override
            public void onNext(ComputeAverageRequest value) {
                //client sends a message / number
                int x = value.getNumber();
                new_avg = (prev_avg*count + x) / (count+1);
                count++;
                prev_avg = new_avg;
            }

            @Override
            public void onError(Throwable t) {
                //client sends an error
            }

            @Override
            public void onCompleted() {
                //client is done sending numbers
                //set up response observer to send the computed average value
                responseObserver.onNext(
                ComputeAverageResponse.newBuilder()
                                        .setAverage(new_avg)
                                        .build()
                );
                responseObserver.onCompleted();

            }
        };
        return requestObserver;
//        return super.computeAverage(responseObserver);
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        StreamObserver<FindMaximumRequest> requestObserver = new StreamObserver<FindMaximumRequest>() {
            int max = 0;
            @Override
            public void onNext(FindMaximumRequest value) {
                //client sends a number
                int curr = value.getNumber();
                if(curr>max){
                    max = curr;
                    responseObserver.onNext(FindMaximumResponse.newBuilder()
                                            .setMaximum(max).build());
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                //client is done sending the numbers
                responseObserver.onNext(FindMaximumResponse.newBuilder().setMaximum(max).build());
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
//        return super.findMaximum(responseObserver);
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        Integer number = request.getNumber();
        if(number>=0) {
            double numberRoot = Math.sqrt(number);
            responseObserver.onNext(SquareRootResponse.newBuilder().setNumberRoot(numberRoot).build());
            responseObserver.onCompleted();
        }
        else {

            //we construct the exception
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Number sent is not positive")
                        .augmentDescription("Number sent: "+number)
                    .asRuntimeException()
            );
        }
//        super.squareRoot(request, responseObserver);
    }
}
