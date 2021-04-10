package com.packtpub;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.Script;

import java.io.IOException;

public class DocumentOperations {

    public static void main(String[] args) {
        String index = "mytest";
        RestHighLevelClient client = RestHighLevelClientHelper.createHighLevelClient();
        IndicesOperations io = new IndicesOperations(client);
        try {
            if (io.checkIndexExists(index))
                io.deleteIndex(index);

            try {
                client.indices()
                        .create(new CreateIndexRequest(index).mapping(XContentFactory.jsonBuilder().startObject()
                                .startObject("properties").startObject("text").field("type", "text")
                                .field("store", "true").endObject().endObject().endObject()), RequestOptions.DEFAULT);
            } catch (IOException e) {
                System.out.println("Unable to create mapping");
            }

            IndexResponse ir = client.index(new IndexRequest(index).id("2").source("text", "unicorn"),
                    RequestOptions.DEFAULT);
            System.out.println("Version: " + ir.getVersion());

            GetResponse gr = client.get(new GetRequest(index, "2"), RequestOptions.DEFAULT);
            System.out.println("Version: " + gr.getVersion());

            UpdateResponse ur = client.update(
                    new UpdateRequest(index, "2").script(new Script("ctx._source.text = 'v2'")),
                    RequestOptions.DEFAULT);
            System.out.println("Version: " + ur.getVersion());

            DeleteResponse dr = client.delete(new DeleteRequest(index, "2"), RequestOptions.DEFAULT);
            io.deleteIndex(index);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            // we need to close the client to free resources
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
