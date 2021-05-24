package com.packtpub;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.Script;

import java.io.IOException;

public class BulkOperations {

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
                                .startObject("properties").startObject("position").field("type", "integer")
                                .field("store", "true").endObject().endObject().endObject()), RequestOptions.DEFAULT);
                ;
            } catch (IOException e) {
                System.out.println("Unable to create mapping");
            }
            BulkRequest bulker = new BulkRequest();
            for (int i = 1; i <= 1000; i++) {
                bulker.add(new IndexRequest(index).id(Integer.toString(i)).source("position", Integer.toString(i)));
            }

            System.out.println("Number of actions for index: " + bulker.numberOfActions());
            client.bulk(bulker, RequestOptions.DEFAULT);

            bulker = new BulkRequest();
            for (int i = 1; i <= 1000; i++) {
                bulker.add(
                        new UpdateRequest(index, Integer.toString(i)).script(new Script("ctx._source.position += 2")));
            }
            System.out.println("Number of actions for update: " + bulker.numberOfActions());
            client.bulk(bulker, RequestOptions.DEFAULT);

            bulker = new BulkRequest();
            for (int i = 1; i <= 1000; i++) {
                bulker.add(new DeleteRequest(index, Integer.toString(i)));
            }
            System.out.println("Number of actions for delete: " + bulker.numberOfActions());
            client.bulk(bulker, RequestOptions.DEFAULT);

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
