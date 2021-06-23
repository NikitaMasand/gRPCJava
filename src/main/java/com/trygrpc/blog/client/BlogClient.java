package com.trygrpc.blog.client;

import generatedclass.proto.blog.*;
import grpc.generatedclass.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public static void main(String[] args) {
        System.out.println("hello from blog client");
        BlogClient main = new BlogClient();
        main.run();
    }

    private void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                                    .usePlaintext()
                                    .build();
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        Blog blog = Blog.newBuilder()
                        .setAuthorId("n1")
                        .setTitle("grpc")
                        .setContent("hello from grpc create blog")
                        .build();

        CreateBlogResponse createResponse = blogClient.createBlog(CreateBlogRequest.newBuilder().setBlog(blog).build());

        System.out.println("Received create blog request");
        System.out.println(createResponse.toString());

        String blogId = createResponse.getBlog().getId();
        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder().setBlogId(blogId).build());
        System.out.println(readBlogResponse.toString());
//        try {
//            ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder().setBlogId("wrong-id").build());
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }


        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("n2")
                .setTitle("updated")
                .setContent("hello from grpc updated blog")
                .build();

        System.out.println("updating blog...");
        UpdateBlogResponse updateBlogResponse;
        try {
             updateBlogResponse = blogClient.updateBlog(UpdateBlogRequest.newBuilder().setBlog(newBlog).build());
            System.out.println("updated blog");
            System.out.println(updateBlogResponse.toString());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("deleting blog");
        DeleteBlogResponse response = blogClient.deleteBlog(DeleteBlogRequest.newBuilder()
                                                            .setBlogId(blogId).build());
        System.out.println("deleted blog");

//        DeleteBlogResponse responseAfterDeletion = blogClient.deleteBlog(DeleteBlogRequest.newBuilder()
//                .setBlogId(blogId).build());






    }
}
