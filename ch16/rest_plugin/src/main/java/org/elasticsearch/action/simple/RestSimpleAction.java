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

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.RestBuilderListener;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;


public class RestSimpleAction extends BaseRestHandler {
    public RestSimpleAction(Settings settings, RestController controller) {
    }

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(
                new Route(POST, "/_simple"),
                new Route(POST, "/{index}/_simple"),
                new Route(POST, "/_simple/{field}"),
                new Route(GET, "/_simple"),
                new Route(GET, "/{index}/_simple"),
                new Route(GET, "/_simple/{field}")));
    }

    @Override
    public String getName() {
        return "simple_rest";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        final SimpleRequest simpleRequest = new SimpleRequest(Strings.splitStringByCommaToArray(request.param("index")));
        simpleRequest.setField(request.param("field"));
        return channel -> client.execute(SimpleAction.INSTANCE, simpleRequest, new RestBuilderListener<SimpleResponse>(channel) {
            @Override
            public RestResponse buildResponse(SimpleResponse simpleResponse, XContentBuilder builder) {
                try {
                    builder.startObject();
                    builder.field("ok", true);
                    builder.array("terms", simpleResponse.getSimple().toArray());
                    builder.endObject();

                } catch (Exception e) {
                    onFailure(e);
                }
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}
