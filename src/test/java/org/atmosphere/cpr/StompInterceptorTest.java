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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.*;
import java.util.Enumeration;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * <p>
 * Test suite for STOMP protocol support.
 * </p>
 */
public class StompInterceptorTest {

    // TODO: refactor this class, BroadcasterTest class and InterceptorTest class to promote code reuse
    private AtmosphereFramework framework;
    private AtmosphereConfig config;
    private AsynchronousProcessor processor;
    private Broadcaster broadcaster;
    private final AtmosphereHandler handler = mock(AtmosphereHandler.class);

    @BeforeMethod
    public void create() throws Throwable {

        // From InterceptorTest
        framework = new AtmosphereFramework();
        framework.setAsyncSupport(mock(AsyncSupport.class));
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
            public String getInitParameter(String name) {
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
            public Action service(AtmosphereRequest req, AtmosphereResponse res) throws IOException, ServletException {
                return action(req, res);
            }
        };

        // From BroadcasterTest
        final DefaultBroadcasterFactory factory = new DefaultBroadcasterFactory(DefaultBroadcaster.class, "NEVER", config);
        config.framework().setBroadcasterFactory(factory);
        broadcaster = factory.get(DefaultBroadcaster.class, "test");

        // Configure interceptor
        framework.addAtmosphereHandler("/*", handler);
        framework.interceptor(new StompInterceptor());
    }

    /**
     * <p>
     * Tests STOMP service with {@link org.atmosphere.stomp.test.StompBusinessService#sayHello(org.atmosphere.cpr.Broadcaster, org.atmosphere.cpr.AtmosphereResource)}
     * signature.
     * </p>
     */
    @Test(enabled = false)
    public void stompServiceWithBroadcasterParamTest() throws Exception {

        // Add an AtmosphereResource that receives a BroadcastMessage
        final StringBuilder broadcast = new StringBuilder();
        final AtmosphereResource ar = mock(AtmosphereResource.class);
        broadcaster.addAtmosphereResource(ar);
        when(ar.write(anyString())).thenAnswer(new Answer<Object>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return broadcast.append(invocationOnMock.getArguments()[0].toString());
            }
        });

        // Mock intercepted request/resource
        final AtmosphereRequest req = mock(AtmosphereRequest.class);
        final AtmosphereResponse res = mock(AtmosphereResponse.class);
        final StringWriter writer = new StringWriter();
        final BufferedReader reader = new BufferedReader(new StringReader("SEND\ndestination:Hello World!"));

        when(res.getWriter()).thenReturn(new PrintWriter(writer));
        when(req.getReader()).thenReturn(reader);

        // Run interceptor
        processor.service(req, res);

        // Expect that broadcaster's resource and requester receives message from STOMP service
        assertEquals(writer.toString(), "null says Hello World!");
        assertEquals(broadcast.toString(), "null says Hello World!");
    }

    /**
     * <p>
     * Tests STOMP service with {@link org.atmosphere.stomp.test.StompBusinessService#sayHello2(org.atmosphere.cpr.AtmosphereResource)}
     * signature.
     * </p>
     */
    @Test
    public void stompServiceWithoutBroadcasterParamTest() {

    }

    /**
     * <p>
     * Tests STOMP service with {@link org.atmosphere.stomp.test.StompBusinessService#sayHello3(org.atmosphere.stomp.test.StompBusinessService.BusinessDto)}
     * signature.
     * </p>
     */
    @Test
    public void stompServiceWithDtoParamTest() {

    }

}
