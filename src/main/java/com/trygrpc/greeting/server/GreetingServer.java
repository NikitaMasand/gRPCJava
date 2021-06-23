package com.trygrpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("hello grpc");

        Server server = ServerBuilder.forPort(50051)
                            .addService(new GreetServiceImpl())
                            .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        //if we don't do this, our server will start and
        //program will finish
        server.awaitTermination();
    }
}
