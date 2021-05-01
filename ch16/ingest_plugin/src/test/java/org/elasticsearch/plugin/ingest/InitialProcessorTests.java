/*
 * Copyright [2019] [Alberto Paro]
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

package org.elasticsearch.plugin.ingest;


import com.packtpub.ingest.InitialProcessor;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class InitialProcessorTests extends ESTestCase {

    public void testRequireSettings() throws Exception {
        InitialProcessor.Factory factory = new InitialProcessor.Factory();
        String processorTag = randomAlphaOfLength(10);
        Map<String, Object> config = new HashMap<>();

        // field parameter
        ElasticsearchParseException e = LuceneTestCase.expectThrows(ElasticsearchParseException.class,
                () -> factory.create(null, processorTag, "description", config));
        Assert.assertThat(e.getMessage(), Matchers.equalTo("[field] required property is missing"));

        // target_field parameter
        config.put("field", "source_field");
        e = LuceneTestCase.expectThrows(ElasticsearchParseException.class,
                () -> factory.create(null, processorTag, "description", config));
        Assert.assertThat(e.getMessage(), Matchers.equalTo("[target_field] required property is missing"));

    }

    public void testMinimalSuccess() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "10");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(LuceneTestCase.random(), document);

        InitialProcessor processor = new InitialProcessor(
                randomAlphaOfLength(10), "description","source_field", "target_field",
                false, null);

        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        Assert.assertThat(data, Matchers.hasKey("target_field"));
        Assert.assertThat(data.get("target_field"), Matchers.is("1"));
    }

    public void testIgnoreMissingFalse() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("field", "10");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(LuceneTestCase.random(), document);

        InitialProcessor processor = new InitialProcessor(
                randomAlphaOfLength(10), "description","source_field", "target_field",
                false, null);

        IllegalArgumentException iae = LuceneTestCase.expectThrows(IllegalArgumentException.class,
                () -> processor.execute(ingestDocument).getSourceAndMetadata());
        Assert.assertThat(iae.getMessage(), Matchers.equalTo("field [source_field] not present as part of path [source_field]"));
    }

    public void testIgnoreMissingTrue() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("field", "10");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(LuceneTestCase.random(), document);

        InitialProcessor processor = new InitialProcessor(
                randomAlphaOfLength(10), "description","source_field", "target_field",
                true, null);

        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        Assert.assertThat(data.get("target_field"), Matchers.nullValue());
    }


    public void testNoDefault() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "30");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(LuceneTestCase.random(), document);
        InitialProcessor processor = new InitialProcessor(
                randomAlphaOfLength(10), "description","source_field", "target_field",
                false, null);

        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        Assert.assertThat(data, Matchers.hasKey("target_field"));
        Assert.assertThat(data.get("target_field"), Matchers.is("3"));
    }

    public void testWithDefault() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", "30");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(LuceneTestCase.random(), document);

        InitialProcessor processor = new InitialProcessor(
                randomAlphaOfLength(10), "description","source_field", "target_field",
                false, "#");

        Map<String, Object> data = processor.execute(ingestDocument).getSourceAndMetadata();

        Assert.assertThat(data, Matchers.hasKey("target_field"));
        Assert.assertThat(data.get("target_field"), Matchers.is("3"));
    }

}

