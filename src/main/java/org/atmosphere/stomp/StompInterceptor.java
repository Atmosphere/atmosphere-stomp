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


package org.atmosphere.stomp;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcastFilterLifecycle;
import org.atmosphere.stomp.handler.StompGlobalAtmosphereHandler;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.ParseException;
import org.atmosphere.stomp.protocol.StompFormat;
import org.atmosphere.stomp.protocol.StompFormatImpl;
import org.atmosphere.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>
 * This interceptor reads the messages and parse it thanks to the {@link org.atmosphere.stomp.protocol.Parser}. When
 * the message is parsed, the interceptor invokes a method provided by an {@link AtmosphereInterceptorAdapter} according
 * to the {@link org.atmosphere.stomp.protocol.Action} specified inside the message.
 * </p>
 *
 * <p>
 * This interceptor expects by default that all messages respect the STOMP protocol. It could be fault tolerant for
 * messages not respecting STOMP by setting the {@link #IGNORE_ERROR} setting in atmosphere.xml or web.xml file.
 * </p>
 *
 * <p>
 * By default the interceptor uses by default {@link AtmosphereStompAdapterImpl} to delegate the frame processing. User
 * can set its own implementation by specifying the class name in {@link PropertyClass#ADAPTER_CLASS} setting in
 * atmosphere.xml or web.xml file.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class StompInterceptor extends AtmosphereInterceptorAdapter {

    /**
     * <p>
     * This enum is dedicated to properties that represents a class to instantiate.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.1

     * @version 1.0
     */
    public enum PropertyClass {

        /**
         * Setting that specifies the {@link AtmosphereStompAdapter} implementation class used by the interceptor.
         */
        ADAPTER_CLASS("org.atmosphere.stomp.atmosphereStompAdapterClass", AtmosphereStompAdapterImpl.class.getName()),

        /**
         * Setting that specifies the {@link org.atmosphere.stomp.protocol.StompFormat} implementation class used by the interceptor.
         */
        STOMP_FORMAT_CLASS("org.atmosphere.stomp.stompFormatClass", StompFormatImpl.class.getName());

        /**
         * The logger.
         */
        private final Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * The property name.
         */
        private String propertyName;

        /**
         * The default implementation if property not set by user.
         */
        private String defaultClass;

        /**
         * <p>
         * Builds a new enumeration.
         * </p>
         *
         * @param propertyName the property name
         * @param defaultClass the default implementation class
         */
        private PropertyClass(final String propertyName, final String defaultClass) {
            this.propertyName = propertyName;
            this.defaultClass = defaultClass;
        }

        /**
         * <p>
         * Checks in the {@link AtmosphereConfig} if the {@link #propertyName} is defined as an init-param and instantiate
         * the appropriate class.
         * </p>
         *
         * <p>
         * If instantiation fails, the exception is logged and {@code null} is returned.
         * </p>
         *
         * @param desiredType the type to be returned
         * @param config the configuration that provides parameters
         * @param <T> the generic for modular call
         * @return the instance of the expected class, {@code null} if an error occurs
         */
        public <T> T retrieve(final Class<T> desiredType, final AtmosphereConfig config) {
            final String initParameter = config.getInitParameter(this.propertyName);
            final String className = (initParameter != null) ? initParameter : defaultClass;

            try {
                final AtmosphereFramework fwk = config.framework();
                final Object retval = fwk.newClassInstance(desiredType, desiredType.getClass().cast(Class.forName(className)));
                return desiredType.cast(retval);
            } catch (Exception e) {
                logger.error("Unable to initialize {}", getClass().getName(), e);
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return propertyName;
        }
    }

    /**
     * Setting that specifies if the interceptor ignores messages that don't respect STOMP protocol.
     */
    public static final String IGNORE_ERROR = "org.atmosphere.stomp.ignoreError";

    /**
     * The attribute name this interceptor uses to inject a parsed body in the request when it is extracted from the frame.
     */
    public static final String STOMP_MESSAGE_BODY = "org.atmosphere.stomp.body";

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The framework extracted from the {@link AtmosphereConfig} when {@link #configure(AtmosphereConfig)} is called.
     */
    private AtmosphereFramework framework;

    /**
     * The formatter that can encode and decode frames.
     */
    private StompFormat stompFormat;

    /**
     * The adapter that do the appropriate stuff for each detected frame.
     */
    private AtmosphereStompAdapter adapter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final AtmosphereConfig config) {
        framework = config.framework();
        stompFormat = PropertyClass.STOMP_FORMAT_CLASS.retrieve(StompFormat.class, config);
        adapter = PropertyClass.ADAPTER_CLASS.retrieve(AtmosphereStompAdapter.class, config);
        final BroadcastFilterLifecycle filter;
        try {
            filter = framework.newClassInstance(BroadcastFilterLifecycle.class, StompBroadcastFilter.class);
            framework.broadcasterFilters(filter);
            filter.init(config);
        } catch (InstantiationException e) {
            logger.error("", e);
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }

        // TODO: user must map AtmosphereServlet to /stomp in web.xml, can we offer a chance to set a custom location ?
        framework.addAtmosphereHandler("/stomp", new StompGlobalAtmosphereHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.atmosphere.cpr.Action inspect(final AtmosphereResource r) {
        StringBuilder body = null;

        try {
            body = IOUtils.readEntirelyAsString(r);

            // Let the global handler suspend the connection if no action is submitted
            if (body.length() == 0) {
                return Action.CONTINUE;
            }

            body.deleteCharAt(body.length() - 1);

            // Suspend first before attempting to add the resource to any broadcaster
            // We do the job in place of global handler so we will skip it at the end of the interceptor
            // POST method don't need to be suspended
            if ("GET".equals(r.getRequest().getMethod())) {
                r.suspend();
            }

            final Frame message = stompFormat.parse(body.toString());

            switch (message.getAction()) {
                case SEND:
                    adapter.send(r, message.getHeaders(), message.getBody(), framework);
                    break;
                case SUBSCRIBE:
                    adapter.subscribe(r, message.getHeaders(), framework);
                    break;
                case UNSUBSCRIBE:
                    adapter.unsubscribe(r, message.getHeaders(), framework);
                    break;
            }
        } catch (final IOException ioe) {
            logger.error("STOMP interceptor fails", ioe);
        } catch (final ParseException pe) {
            logger.error("Invalid STOMP string: {} ", body, pe);
        }

        return Action.SKIP_ATMOSPHEREHANDLER;
    }
}
