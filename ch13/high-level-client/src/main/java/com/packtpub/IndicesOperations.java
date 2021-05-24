package com.packtpub;

import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import java.io.IOException;

public class IndicesOperations {
    private final RestHighLevelClient client;

    public IndicesOperations(RestHighLevelClient client) {
        this.client = client;
    }

    public boolean checkIndexExists(String name) throws IOException {
        return client.indices().exists(new GetIndexRequest(name), RequestOptions.DEFAULT);
    }

    public void createIndex(String name) throws IOException {
        client.indices().create(new CreateIndexRequest(name), RequestOptions.DEFAULT);
    }

    public void deleteIndex(String name) throws IOException {
        client.indices().delete(new DeleteIndexRequest(name), RequestOptions.DEFAULT);
    }

    public void closeIndex(String name) throws IOException {
        client.indices().close(new CloseIndexRequest(name), RequestOptions.DEFAULT);
    }

    public void openIndex(String name) throws IOException {
        client.indices().open(new OpenIndexRequest(name), RequestOptions.DEFAULT);
    }

    public void putMapping(String index, String source) throws IOException {
        client.indices().putMapping(new PutMappingRequest(index).source(source, XContentType.JSON), RequestOptions.DEFAULT);
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        RestHighLevelClientHelper nativeClient = new RestHighLevelClientHelper();
        RestHighLevelClient client = nativeClient.getClient();
        IndicesOperations io = new IndicesOperations(client);
        String myIndex = "test";
        if (io.checkIndexExists(myIndex))
            io.deleteIndex(myIndex);
        io.createIndex(myIndex);
        Thread.sleep(1000);
        io.closeIndex(myIndex);
        io.openIndex(myIndex);
        io.deleteIndex(myIndex);

        //we need to close the client to free resources
        nativeClient.close();

    }
}
