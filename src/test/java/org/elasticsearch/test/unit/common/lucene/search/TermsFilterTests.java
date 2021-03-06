/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.test.unit.common.lucene.search;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PublicTermsFilter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.FixedBitSet;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.lucene.search.TermFilter;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 */
@Test
public class TermsFilterTests {

    @Test
    public void testTermFilter() throws Exception {
        String fieldName = "field1";
        Directory rd = new RAMDirectory();
        IndexWriter w = new IndexWriter(rd, new IndexWriterConfig(Lucene.VERSION, new KeywordAnalyzer()));
        for (int i = 0; i < 100; i++) {
            Document doc = new Document();
            int term = i * 10; //terms are units of 10;
            doc.add(new Field(fieldName, "" + term, Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("all", "xxx", Field.Store.NO, Field.Index.NOT_ANALYZED));
            w.addDocument(doc);
            if ((i % 40) == 0) {
                w.commit();
            }
        }
        IndexReader reader = w.getReader();
        w.close();

        TermFilter tf = new TermFilter(new Term(fieldName, "19"));
        FixedBitSet bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits, nullValue());

        tf = new TermFilter(new Term(fieldName, "20"));
        bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits.cardinality(), equalTo(1));

        tf = new TermFilter(new Term("all", "xxx"));
        bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits.cardinality(), equalTo(100));

        reader.close();
        rd.close();
    }

    @Test
    public void testTermsFilter() throws Exception {
        String fieldName = "field1";
        Directory rd = new RAMDirectory();
        IndexWriter w = new IndexWriter(rd, new IndexWriterConfig(Lucene.VERSION, new KeywordAnalyzer()));
        for (int i = 0; i < 100; i++) {
            Document doc = new Document();
            int term = i * 10; //terms are units of 10;
            doc.add(new Field(fieldName, "" + term, Field.Store.NO, Field.Index.NOT_ANALYZED));
            doc.add(new Field("all", "xxx", Field.Store.NO, Field.Index.NOT_ANALYZED));
            w.addDocument(doc);
            if ((i % 40) == 0) {
                w.commit();
            }
        }
        IndexReader reader = w.getReader();
        w.close();

        PublicTermsFilter tf = new PublicTermsFilter();
        tf.addTerm(new Term(fieldName, "19"));
        FixedBitSet bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits, nullValue());

        tf.addTerm(new Term(fieldName, "20"));
        bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits.cardinality(), equalTo(1));

        tf.addTerm(new Term(fieldName, "10"));
        bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits.cardinality(), equalTo(2));

        tf.addTerm(new Term(fieldName, "00"));
        bits = (FixedBitSet) tf.getDocIdSet(reader);
        assertThat(bits.cardinality(), equalTo(2));

        reader.close();
        rd.close();
    }
}
