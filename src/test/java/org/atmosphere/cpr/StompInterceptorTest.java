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

import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.cpr.packages.StompEndpointProcessor;
import org.atmosphere.stomp.protocol.Action;
import org.atmosphere.stomp.test.StompBusinessService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

/**
 * <p>
 * Test suite for STOMP protocol support.
 * </p>
 */
public class StompInterceptorTest {

    private AtmosphereFramework framework;
    private AtmosphereConfig config;
    private AsynchronousProcessor processor;

    /**
     * Action injected by formatter. Could be change on the fly inside tests.
     */
    private static Action action = Action.SEND;

    /**
     * <p>
     * Initializes framework.
     * </p>
     *
     * @throws Throwable if test fails
     */
    @BeforeMethod
    public void create() throws Throwable {
        framework = new AtmosphereFramework();

        // Detect processor
        framework.addCustomAnnotationPackage(StompEndpointProcessor.class);

        // Detect service
        framework.addAnnotationPackage(StompBusinessService.class);

        // Global handler: mandatory
        framework.init(new ServletConfig() {
            @Override
            public String getServletName() {
                return "void";
            }

            @Override
            public ServletContext getServletContext() {
                return mock(ServletContext.class);
            }

            @Override
            public String getInitParameter(final String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        });

        config = framework.getAtmosphereConfig();

        processor = new AsynchronousProcessor(config) {
            @Override
            public org.atmosphere.cpr.Action service(AtmosphereRequest req, AtmosphereResponse res) throws IOException, ServletException {
                return action(req, res);
            }
        };

        framework.setAsyncSupport(processor);

        // Configure interceptor
        framework.interceptor(new StompInterceptor());
    }

    /**
     * <p>
     * Returns the frame to read.
     * </p>
     *
     * @param destination the destination's header value
     * @return the string representation
     */
    private String toRead(final String destination) {
        return action.toString()
                + "\n"
                + "destination:"
                + destination
                + "\n"
                + "content-type:text/plain\n"
                + "\n"
                + String.format("{\"timestamp\":%d, \"message\":\"%s\"}", System.currentTimeMillis(), "hello");
    }

    /**
     * <p>
     * Builds a new request
     * </p>
     *
     * @param destination the destination's header value in the request frame
     * @return the request
     */
    private AtmosphereRequest newRequest(final String destination) {
        final AtmosphereRequest req = new AtmosphereRequest.Builder()
                .pathInfo(destination)
                .method("GET")
                .body(toRead(destination))
                .build();

        req.setAttribute(ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID, "4000");

        return req;
    }

    /**
     * <p>
     * Builds a new response.
     * </p>
     *
     * @return the response
     */
    private AtmosphereResponse newResponse() {
        return AtmosphereResponse.newInstance();
    }

    /**
     * <p>
     * Builds a new atmosphere resource.
     * </p>
     *
     * @param destination the destination in the request
     * @param req the request
     * @param res the response
     * @param bindToRequest {@code true} if the created resource should be added to request attributes
     * @return the resource
     * @throws Exception if creation fails
     */
    private AtmosphereResource newAtmosphereResource(final String destination,
                                                     final AtmosphereRequest req,
                                                     final AtmosphereResponse res,
                                                     final boolean bindToRequest)
            throws Exception {

        // Add an AtmosphereResource that receives a BroadcastMessage
        final AtmosphereHandler ah = mock(AtmosphereHandler.class);
        final AtmosphereResource ar = new AtmosphereResourceImpl();
        final Broadcaster b = framework.getBroadcasterFactory().lookup(destination);
        ar.initialize(config, b, req, res, framework.asyncSupport, ah);

        if (bindToRequest) {
            req.setAttribute(FrameworkConfig.INJECTED_ATMOSPHERE_RESOURCE, ar);
        }

        return ar;
    }

    /**
     * <p>
     * Adds the given resource to the broadcaster mapped to the given destination.
     * </p>
     *
     * @param destination the destination
     * @param ar the atmosphere resource
     */
    private void addToBroadcaster(final String destination, final AtmosphereResource ar) {
        final Broadcaster b = framework.getBroadcasterFactory().lookup(destination);
        b.addAtmosphereResource(ar);
    }

    /**
     * <p>
     * Sends a message at the given destination and checks that the given regex matches the message broadcasted to a
     * resource registered to the destination.
     * </p>
     *
     * @param regex the expected regex
     * @throws Exception if test fails
     */
    private void runMessage(final String regex,
                            final String destination,
                            final AtmosphereRequest req,
                            final AtmosphereResponse res,
                            final boolean addToBroadcaster)
            throws Exception {
        final AtmosphereResource ar = newAtmosphereResource(destination, req, res, false);

        // Wait until message has been broadcasted
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicReference<String> broadcast = new AtomicReference<String>();

        // Release lock when message is broadcasted
        ar.getResponse().asyncIOWriter(new AsyncIOWriterAdapter() {
            @Override
            public AsyncIOWriter write(final AtmosphereResponse r, final byte[] data) throws IOException {
                broadcast.set(new String(data));
                countDownLatch.countDown();
                return this;
            }
        });

        // we also need to intercept AtmosphereHandler call
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                broadcast.set(String.valueOf(((AtmosphereResourceEvent) invocationOnMock.getArguments()[0]).getMessage()));
                countDownLatch.countDown();
                return null;
            }
        }).when(ar.getAtmosphereHandler()).onStateChange(any(AtmosphereResourceEvent.class));

        if (addToBroadcaster) {
            addToBroadcaster(destination, ar);
        }

        // Run interceptor
        processor.service(ar.getRequest(), ar.getResponse());

        countDownLatch.await(3, TimeUnit.SECONDS);

        // Expect that broadcaster's resource receives message from STOMP service
        assertTrue(Pattern.matches(regex, broadcast.toString()));
    }

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
        runMessage("(.*)? from " + destination, destination, newRequest(destination), newResponse(), true);
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
        runMessage("(.*)? from " + destination, destination, newRequest(destination), newResponse(), true);
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
        runMessage("\\{\"timestamp\":(\\d)*,\\s\"message\":\"hello\"\\}", destination, newRequest(destination), newResponse(), true);
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
        runMessage("(.*)? from " + destination, destination, newRequest(destination), response, false);
    }

    /**
     * <p>
     * Tests when message are not received according to unsubscription operations.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void unsubscriptionTest() throws Exception {
        final AtmosphereResponse response = newResponse();
        final String destination = StompBusinessService.DESTINATION_HELLO_WORLD2;

        // Subscribe...
        action = Action.SUBSCRIBE;
        AtmosphereResource ar = newAtmosphereResource(destination, newRequest(destination), response, true);
        processor.service(ar.getRequest(), response);

        // ... then unsubscribe...
        action = Action.UNSUBSCRIBE;
        ar = newAtmosphereResource(destination, newRequest(destination), response, true);
        processor.service(ar.getRequest(), response);

        // ... finally we should not receive message
        action = Action.SEND;
        ar = newAtmosphereResource(destination, newRequest(destination), response, true);
        runMessage("null", destination, ar.getRequest(), response, false);
    }
}
