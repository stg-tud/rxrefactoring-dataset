/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.elasticsearch.test.rest;

import com.google.common.collect.Maps;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.rest.client.RestClient;
import org.elasticsearch.test.rest.client.RestException;
import org.elasticsearch.test.rest.client.RestResponse;
import org.elasticsearch.test.rest.spec.RestSpec;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Execution context passed across the REST tests.
 * Holds the REST client used to communicate with elasticsearch.
 * Caches the last obtained test response and allows to stash part of it within variables
 * that can be used as input values in following requests.
 */
public class RestTestExecutionContext implements Closeable {

    private static final ESLogger logger = Loggers.getLogger(RestTestExecutionContext.class);

    private final RestClient restClient;

    private final String esVersion;

    private final Stash stash = new Stash();

    private RestResponse response;

    public RestTestExecutionContext(InetSocketAddress[] addresses, RestSpec restSpec) throws RestException, IOException {
        this.restClient = new RestClient(addresses, restSpec);
        this.esVersion = restClient.getEsVersion();
    }

    /**
     * Calls an elasticsearch api with the parameters and request body provided as arguments.
     * Saves the obtained response in the execution context.
     * @throws RestException if the returned status code is non ok
     */
    public RestResponse callApi(String apiName, Map<String, String> params, List<Map<String, Object>> bodies) throws IOException, RestException  {
        //makes a copy of the parameters before modifying them for this specific request
        HashMap<String, String> requestParams = Maps.newHashMap(params);
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (stash.isStashedValue(entry.getValue())) {
                entry.setValue(stash.unstashValue(entry.getValue()).toString());
            }
        }

        String body = actualBody(bodies);

        try {
            response = callApiInternal(apiName, requestParams, body);
            //we always stash the last response body
            stash.stashValue("body", response.getBody());
            return response;
        } catch(RestException e) {
            response = e.restResponse();
            throw e;
        }
    }

    private String actualBody(List<Map<String, Object>> bodies) throws IOException {
        if (bodies.isEmpty()) {
            return "";
        }

        if (bodies.size() == 1) {
            return bodyAsString(stash.unstashMap(bodies.get(0)));
        }

        StringBuilder bodyBuilder = new StringBuilder();
        for (Map<String, Object> body : bodies) {
            bodyBuilder.append(bodyAsString(stash.unstashMap(body))).append("\n");
        }
        return bodyBuilder.toString();
    }

    private String bodyAsString(Map<String, Object> body) throws IOException {
        return XContentFactory.jsonBuilder().map(body).string();
    }

    /**
     * Calls an elasticsearch api internally without saving the obtained response in the context.
     * Useful for internal calls (e.g. delete index during teardown)
     * @throws RestException if the returned status code is non ok
     */
    public RestResponse callApiInternal(String apiName, String... params) throws IOException, RestException {
        return restClient.callApi(apiName, params);
    }

    private RestResponse callApiInternal(String apiName, Map<String, String> params, String body) throws IOException, RestException  {
        return restClient.callApi(apiName, params, body);
    }

    /**
     * Extracts a specific value from the last saved response
     */
    public Object response(String path) throws IOException {
        return response.evaluate(path);
    }

    /**
     * Clears the last obtained response and the stashed fields
     */
    public void clear() {
        logger.debug("resetting response and stash");
        response = null;
        stash.clear();
    }

    public Stash stash() {
        return stash;
    }

    /**
     * Returns the current es version as a string
     */
    public String esVersion() {
        return esVersion;
    }

    /**
     * Closes the execution context and releases the underlying resources
     */
    public void close() {
        this.restClient.close();
    }
}
