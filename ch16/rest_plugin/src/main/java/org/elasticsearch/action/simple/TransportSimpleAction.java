/*
 * Copyright 2021 Alberto Paro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.elasticsearch.action.simple;

import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.node.TransportBroadcastByNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple index/indices action.
 */
public class TransportSimpleAction
        extends TransportBroadcastByNodeAction<SimpleRequest, SimpleResponse, ShardSimpleResponse> {

    private final IndicesService indicesService;

    @Inject
    public TransportSimpleAction(ClusterService clusterService,
                                 TransportService transportService, IndicesService indicesService,
                                 ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver) {
        super(SimpleAction.NAME, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, SimpleRequest::new, ThreadPool.Names.SEARCH);
        this.indicesService = indicesService;
    }

    @Override
    protected SimpleResponse newResponse(SimpleRequest request, int totalShards, int successfulShards, int failedShards,
                                         List<ShardSimpleResponse> shardSimpleResponses,
                                         List<DefaultShardOperationFailedException> shardFailures,
                                         ClusterState clusterState) {
        Set<String> simple = new HashSet<String>();
        for (ShardSimpleResponse shardSimpleResponse : shardSimpleResponses) {
            simple.addAll(shardSimpleResponse.getTermList());
        }

        return new SimpleResponse(totalShards, successfulShards, failedShards, shardFailures, simple);
    }

    @Override
    protected ShardSimpleResponse shardOperation(SimpleRequest request, ShardRouting shardRouting, Task task) throws IOException {
        IndexService indexService = indicesService.indexServiceSafe(shardRouting.shardId().getIndex());
        IndexShard indexShard = indexService.getShard(shardRouting.shardId().id());
        indexShard.store().directory();
        Set<String> set = new HashSet<String>();
        set.add(request.getField() + "_" + shardRouting.shardId());
        return new ShardSimpleResponse(shardRouting, set);
    }

    @Override
    protected ShardSimpleResponse readShardResult(StreamInput in) throws IOException {
        return ShardSimpleResponse.readShardResult(in);
    }

    @Override
    protected SimpleRequest readRequestFrom(StreamInput in) throws IOException {
        return new SimpleRequest(in);
    }

    @Override
    protected ShardsIterator shards(ClusterState clusterState, SimpleRequest request, String[] concreteIndices) {
        return clusterState.routingTable().allShards(concreteIndices);
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, SimpleRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, SimpleRequest request, String[] concreteIndices) {
        return state.blocks().indicesBlockedException(ClusterBlockLevel.METADATA_READ, concreteIndices);
    }
}