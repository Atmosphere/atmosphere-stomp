/*
 * Copyright 2014 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package org.atmosphere.cpr;

import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.stomp.annotation.StompEndpoint;
import org.atmosphere.stomp.annotation.StompService;
import org.atmosphere.stomp.interceptor.ConnectInterceptor;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

/**
 * <p>
 * Heartbeat test class.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.2
 * @version 1.0
 */
public class HeartbeatTest extends StompTest {

    /**
     * Heartbeat counter.
     */
    private static AtomicInteger heartbeatCount = new AtomicInteger(0);

    /**
     * <p>
     * Tests when heartbeat event is triggered.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void heartbeatTest() throws Exception {
        final String d = HeartbeatStompEndpoint.DESTINATION;

        final AtmosphereResponse response = newResponse();
        final AtmosphereRequest request = newRequest(d, new String(ConnectInterceptor.STOMP_HEARTBEAT_DATA), new HashMap<String, String>());
        AtmosphereResource ar = newAtmosphereResource(d, request, response, true);
        processor.service(ar.getRequest(), response);

        assertEquals(heartbeatCount.get(), 1);
    }

    /**
     * <p>
     * Endpoint that defines a heartbeat listener.
     * </p>
     */
    @StompEndpoint
    public final static class HeartbeatStompEndpoint {

        /**
         * The heartbeat destination.
         */
        public static final String DESTINATION = "/heartbeat";

        /**
         * <p>
         * Heartbeat listener.
         * </p>
         *
         * @param resource the resource
         */
        @Heartbeat
        public void heartbeat(AtmosphereResourceEvent resource) {
            heartbeatCount.incrementAndGet();
        }

        /**
         * Service required for heartbeat.
         */
        @StompService(destination = DESTINATION)
        public void service() {

        }
    }
}
