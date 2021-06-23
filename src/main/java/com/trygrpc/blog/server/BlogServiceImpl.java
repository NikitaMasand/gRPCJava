package com.trygrpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import generatedclass.proto.blog.*;
import grpc.generatedclass.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = mongoDatabase.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received create blog request");
        Blog blog = request.getBlog();
        Document doc = new Document("author_id", blog.getAuthorId())
                            .append("title", blog.getTitle())
                            .append("content", blog.getContent());

        //we insert (create) the document in mongodb
        System.out.println("inserting blog");
        collection.insertOne(doc);
        //we retrieve the mongodb generated id
        String id = doc.getObjectId("_id").toString();
        System.out.println("inserted blog "+id);


//        CreateBlogResponse response = CreateBlogResponse.newBuilder()
//                                        .setBlog(Blog.newBuilder()
//                                            .setAuthorId(blog.getAuthorId())
//                                            .setContent(blog.getContent())
//                                            .setTitle(blog.getTitle())
//                                            .setId(id)
//                                        )
//                                        .build();

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                                       .setBlog(blog.toBuilder().setId(id))
                                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();


//        super.createBlog(request, responseObserver);
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Receieved read blog request");
        String blog_id = request.getBlogId();
        System.out.println("searching for a blog");
        Document result = collection.find(eq("_id", new ObjectId(blog_id))).first();
        if(result==null){
            System.out.println("blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with corresponding id was not found")
                            .asRuntimeException()
            );

        }
        else {
            System.out.println("blog found");
           Blog blog = documentToBlog(result);
            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
            responseObserver.onCompleted();
        }
//        super.readBlog(request, responseObserver);
    }

    private Blog documentToBlog(Document document) {
       return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("in update blog metheod");
        Blog blog = request.getBlog();
        System.out.println("Received update blog request");
        String blogId = request.getBlog().getId();
        System.out.println("searching for a blog to update it");
        Document result = collection.find(eq("_id",new ObjectId(blogId))).first();
        if(result==null){
            System.out.println("blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with corresponding id was not found")
                            .asRuntimeException()
            );
        }
        else {

            Document replacement = new Document("author_id", blog.getAuthorId())
                                            .append("title", blog.getTitle())
                                            .append("content", blog.getContent())
                                            .append("_id", new ObjectId(blogId));

            System.out.println("replacing in the db");
            collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);
            System.out.println("replaced, sending response");
            responseObserver.onNext(UpdateBlogResponse.newBuilder()
                                    .setBlog(documentToBlog(replacement))
                                    .build());
            responseObserver.onCompleted();
        }
//        super.updateBlog(request, responseObserver);
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("received delete blog response");
        String blogId = request.getBlogId();
        DeleteResult result = null;
        try {
          result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        }
        catch (Exception e) {
            responseObserver.onError(Status.
                    NOT_FOUND
                    .withDescription("the blog with corresponding id was not found")
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException());
        }

       if(result.getDeletedCount()==0) {
           responseObserver.onError(Status.
                   NOT_FOUND
                   .withDescription("the blog with corresponding id was not found")
                   .asRuntimeException());
       }
       else {
           System.out.println("blog was deleted");
           responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());
           responseObserver.onCompleted();
       }

    }
}
