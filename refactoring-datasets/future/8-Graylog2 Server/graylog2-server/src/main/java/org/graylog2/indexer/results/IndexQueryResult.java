/**
 * Copyright 2013 Lennart Koopmann <lennart@torch.sh>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.indexer.results;

import com.beust.jcommander.internal.Lists;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class IndexQueryResult {

    private final String originalQuery;
    private final TimeValue took;
    private final BytesReference builtQuery;

    public IndexQueryResult(String originalQuery, BytesReference builtQuery, TimeValue took) {
        this.originalQuery = originalQuery;
        this.took = took;
        this.builtQuery = builtQuery;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public String getBuiltQuery() {
        try {
            return XContentHelper.convertToJson(builtQuery, false);
        } catch (IOException ignored) {
            // exception comes from InputStream, but that stream is from a byte array, so won't do IO.
        }
        return null;
    }

    public TimeValue took() {
        return took;
    }

    protected List<ResultMessage> buildResults(SearchHits hits) {
        List<ResultMessage> r = Lists.newArrayList();

        Iterator<SearchHit> i = hits.iterator();
        while(i.hasNext()) {
            r.add(ResultMessage.parseFromSource(i.next()));
        }

        return r;
    }
}
