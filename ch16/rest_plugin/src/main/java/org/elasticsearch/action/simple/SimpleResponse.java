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

import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A response for simple action.
 */
public class SimpleResponse extends BroadcastResponse {

    private Set<String> simple;

    SimpleResponse() {
    }

    public SimpleResponse(StreamInput in) throws IOException {
        super(in);
        int n = in.readInt();
        simple = new HashSet<String>();
        for (int i = 0; i < n; i++) {
            simple.add(in.readString());
        }
    }

    SimpleResponse(int totalShards, int successfulShards, int failedShards,
                   List<DefaultShardOperationFailedException> shardFailures, Set<String> simple) {
        super(totalShards, successfulShards, failedShards, shardFailures);
        this.simple = simple;
    }

    public Set<String> getSimple() {
        return simple;
    }


    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeInt(simple.size());
        for (String t : simple) {
            out.writeString(t);
        }
    }
}