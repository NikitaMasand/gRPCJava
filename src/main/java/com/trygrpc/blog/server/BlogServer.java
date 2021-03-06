package com.trygrpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("hello from blog server");

        Server server = ServerBuilder.forPort(50051)
                                    .addService(new BlogServiceImpl())
                                    .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}
