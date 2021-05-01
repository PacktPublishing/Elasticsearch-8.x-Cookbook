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
package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class CustomEnglishAnalyzerProvider extends AbstractIndexAnalyzerProvider<EnglishAnalyzer> {
    public static String NAME = "custom_english";

    private final EnglishAnalyzer analyzer;

    public CustomEnglishAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings,
                                         boolean ignoreCase) {
        super(indexSettings, name, settings);

        analyzer = new EnglishAnalyzer(
                Analysis.parseStopWords(env, settings, EnglishAnalyzer.getDefaultStopSet(), ignoreCase),
                Analysis.parseStemExclusion(settings, CharArraySet.EMPTY_SET));
    }

    public static CustomEnglishAnalyzerProvider getCustomEnglishAnalyzerProvider(IndexSettings indexSettings,
                                                                                 Environment env, String name,
                                                                                 Settings settings) {
        return new CustomEnglishAnalyzerProvider(indexSettings, env, name, settings, true);
    }

    @Override
    public EnglishAnalyzer get() {
        return this.analyzer;
    }
}