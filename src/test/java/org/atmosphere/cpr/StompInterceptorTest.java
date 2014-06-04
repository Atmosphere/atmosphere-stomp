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

import org.atmosphere.stomp.protocol.Action;
import org.atmosphere.stomp.test.StompBusinessService;
import org.testng.annotations.Test;

/**
 * <p>
 * Test suite for STOMP protocol support.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class StompInterceptorTest extends StompTest {

    /**
     * <p>
     * Tests STOMP service with {@link org.atmosphere.stomp.test.StompBusinessService#sayHello(org.atmosphere.cpr.AtmosphereResource, Broadcaster)}
     * signature.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void stompServiceWithBroadcasterParamTest() throws Exception {
        final String destination = StompBusinessService.DESTINATION_HELLO_WORLD;
        runMessage("(.*)? from " + destination + ".*", destination, newRequest(destination), newResponse(), true);
    }

    /**
     * <p>
     * Tests STOMP service with {@link org.atmosphere.stomp.test.StompBusinessService#sayHello2(org.atmosphere.cpr.AtmosphereResource)}
     * signature.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void stompServiceWithoutBroadcasterParamTest() throws Exception {
        final String destination = StompBusinessService.DESTINATION_HELLO_WORLD2;
        runMessage("(.*)? from " + destination + ".*", destination, newRequest(destination), newResponse(), true);
    }

    /**
     * <p>
     * Tests STOMP service with {@link org.atmosphere.stomp.test.StompBusinessService#sayHello3(org.atmosphere.stomp.test.StompBusinessService.BusinessDto)}
     * signature.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void stompServiceWithDtoParamTest() throws Exception {
        final String destination = StompBusinessService.DESTINATION_HELLO_WORLD3;
        runMessage("(.*)?\\{\"timestamp\":(\\d)*,\\s\"message\":\"hello\"\\}.*", destination, newRequest(destination), newResponse(), true);
    }

    /**
     * <p>
     * Tests when message are received according to subscription operations.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void subscriptionTest() throws Exception {
        final AtmosphereResponse response = newResponse();
        final String destination = StompBusinessService.DESTINATION_HELLO_WORLD2;
        action = Action.SUBSCRIBE;
        processor.service(newRequest(destination), response);
        action = Action.SEND;
        runMessage("(.*)? from " + destination + ".*", destination, newRequest(destination), response, false);
    }

    /**
     * <p>
     * Tests when an error occurs.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void errorTest() throws Exception {
        final AtmosphereResponse response = newResponse();
        final String destination = StompBusinessService.DESTINATION_ERROR;

        // Exception an error
        action = Action.SEND;
        final AtmosphereResource ar = newAtmosphereResource(destination, newRequest(destination), response, false);
        runMessage("(.*)?ERROR.*", destination, ar.getRequest(), response, false);
    }
}
