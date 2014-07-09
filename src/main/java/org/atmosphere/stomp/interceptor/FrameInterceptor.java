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


package org.atmosphere.stomp.interceptor;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.BroadcastFilterLifecycle;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.stomp.StompBroadcastFilter;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.Subscriptions;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.ParseException;
import org.atmosphere.stomp.protocol.StompFormat;
import org.atmosphere.stomp.protocol.StompFormatImpl;
import org.atmosphere.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * This interceptor reads the frames and parse it thanks to the {@link org.atmosphere.stomp.protocol.StompFormat}. When
 * the message is parsed, the interceptor delegates an appropriate treatment to a {@link StompInterceptor}.
 * </p>
 *
 * <p>
 * This interceptor should be executed before the {@link org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor}
 * to suspends the connection. Then it could add any {@link AtmosphereResource} to a {@link org.atmosphere.cpr.Broadcaster} if necessary.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class FrameInterceptor extends AtmosphereInterceptorAdapter implements StompInterceptor {

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
     * <p>
     * Inner class that wraps the {@link AtmosphereResource} during inspection to write frame and check the nature of
     * the operations.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.3
     * @version 1.0
     *
     */
    public class StompAtmosphereResource {

        /**
         * The wrapped resource.
         */
        private final AtmosphereResource resource;

        /**
         * The action that triggers the inspection.
         */
        private final org.atmosphere.stomp.protocol.Action action;

        /**
         * If an error frame has been written.
         */
        private boolean hasError;

        /**
         * <p>
         * Builds a new action.
         * </p>
         *
         * @param r the resource
         * @param a the action
         */
        public StompAtmosphereResource(final AtmosphereResource r, final org.atmosphere.stomp.protocol.Action a) {
            resource = r;
            action = a;
        }

        /**
         * <p>
         * Write a frame with its headers.
         * </p>
         *
         * @param a the action
         * @param headers the headers
         */
        public void write(org.atmosphere.stomp.protocol.Action a, final Map<String, String> headers) {
            write(a, headers, null);
        }

        /**
         * <p>
         * Write a frame with its headers and a content.
         * </p>
         *
         * @param a the action
         * @param headers the headers
         * @param message the message
         */
        public void write(org.atmosphere.stomp.protocol.Action a, final Map<String, String> headers, final String message) {
            resource.write(stompFormat.format(new Frame(a, headers, message)));

            if (!hasError) {
                hasError = org.atmosphere.stomp.protocol.Action.ERROR.equals(a);
            }
        }

        /**
         * <p>
         * Gets the wrapped resource.
         * </p>
         *
         * @return the resource
         */
        public AtmosphereResource getResource() {
            return resource;
        }
    }

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
     * The {@link AtmosphereResourceSessionFactory}.
     */
    private AtmosphereResourceSessionFactory arsf;

    /**
     * The interceptors used to dispatch the frame.
     */
    private Map<org.atmosphere.stomp.protocol.Action, StompInterceptor> interceptors;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final AtmosphereConfig config) {
        framework = config.framework();
        arsf = AtmosphereResourceSessionFactory.getDefault();
        setStompFormat(PropertyClass.STOMP_FORMAT_CLASS.retrieve(StompFormat.class, config));

        try {
            // TODO: user must map AtmosphereServlet to /stomp in web.xml, can we offer a chance to set a custom location ?
            framework.addAtmosphereHandler("/stomp", framework.newClassInstance(AtmosphereHandler.class, AbstractReflectorAtmosphereHandler.Default.class));

            interceptors = new ConcurrentHashMap<org.atmosphere.stomp.protocol.Action, StompInterceptor>();
            configureInterceptor(config, ConnectInterceptor.class, org.atmosphere.stomp.protocol.Action.CONNECT, org.atmosphere.stomp.protocol.Action.STOMP, org.atmosphere.stomp.protocol.Action.NULL);
            configureInterceptor(config, SubscribeInterceptor.class, org.atmosphere.stomp.protocol.Action.SUBSCRIBE);
            configureInterceptor(config, UnsubscribeInterceptor.class, org.atmosphere.stomp.protocol.Action.UNSUBSCRIBE);
            configureInterceptor(config, SendInterceptor.class, org.atmosphere.stomp.protocol.Action.SEND);
            configureInterceptor(config, DisconnectInterceptor.class, org.atmosphere.stomp.protocol.Action.DISCONNECT);

            final BroadcastFilterLifecycle filter = framework.newClassInstance(BroadcastFilterLifecycle.class, StompBroadcastFilter.class);
            framework.broadcasterFilters(filter);
            filter.init(config);
        } catch (InstantiationException e) {
            logger.error("", e);
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.atmosphere.cpr.Action inspect(final AtmosphereResource r) {
        String body = null;

        try {
            body = IOUtils.readEntirelyAsString(r).toString();

            // Let the global handler suspend the connection if no action is submitted
            if (body.length() == 0) {
                return Action.CONTINUE;
            } else if (Arrays.equals(body.getBytes(), ConnectInterceptor.STOMP_HEARTBEAT_DATA)) {
                // Particular case: the heartbeat is handled by the ConnectInterceptor
                final org.atmosphere.stomp.protocol.Action a = org.atmosphere.stomp.protocol.Action.NULL;
                return inspect(framework, new Frame(a, new HashMap<String, String>()), new StompAtmosphereResource(r, a));
            } else {
                final Frame frame = stompFormat.parse(body.substring(0, body.length() - 1));
                final StompAtmosphereResource sar = new StompAtmosphereResource(r, frame.getAction());
                return inspect(framework, frame, sar);
            }
        } catch (final IOException ioe) {
            logger.error("STOMP interceptor fails", ioe);
        } catch (final ParseException pe) {
            logger.error("Invalid STOMP string: {} ", body, pe);
        }

        return Action.CANCELLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postInspect(final AtmosphereResource atmosphereResource) {
        // The client can reconnects while he has already subscribed different destinations
        // We need to add the new request to the associated broadcasters
        if (atmosphereResource.isSuspended()) {
            final Subscriptions s = Subscriptions.getFromSession(arsf.getSession(atmosphereResource));
            final Set<String> destinations = s.getAllDestinations();

            for (final String d : destinations) {
                framework.getAtmosphereConfig().getBroadcasterFactory().lookup(d).addAtmosphereResource(atmosphereResource);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final AtmosphereFramework framework, final Frame frame, final StompAtmosphereResource r)
            throws IOException {
        final StompInterceptor interceptor = interceptors.get(frame.getAction());

        if (interceptor == null) {
            logger.warn("{} is not supported", frame.getAction().toString(), new UnsupportedOperationException());
            return Action.CANCELLED;
        }

        return interceptor.inspect(framework, frame, r);
    }

    /**
     * <p>
     * Sets the {@link StompFormat} that wire frames.
     * </p>
     *
     * @param stompFormat the new formatter
     */
    public void setStompFormat(final StompFormat stompFormat) {
        this.stompFormat = stompFormat;
    }

    /**
     * <p>
     * Adds the appropriate interceptor for each action.
     * </p>
     *
     * @param config the configuration
     * @param clazz the interceptor
     * @param action the actions
     * @throws InstantiationException if interceptor class can't be instantiated
     * @throws IllegalAccessException if interceptor class can't be instantiated
     */
    private void configureInterceptor(final AtmosphereConfig config,
                                      final Class<? extends StompInterceptor> clazz,
                                      final org.atmosphere.stomp.protocol.Action ... action)
            throws InstantiationException, IllegalAccessException {
        final StompInterceptor interceptor = framework.newClassInstance(StompInterceptor.class, clazz);
        interceptor.configure(config);

        for (org.atmosphere.stomp.protocol.Action a : action) {
            interceptors.put(a, interceptor);
        }
    }
}
