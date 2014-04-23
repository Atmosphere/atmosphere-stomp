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

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.stomp.protocol.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Default {@link AtmosphereStompAdapter} used by Atmosphere framework.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
public class AtmosphereStompAdapterImpl implements AtmosphereStompAdapter {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * We track, for each UUID (key) an association between the subscription ID and the destination.
     */
    private final Map<String, Map<String, String>> subscriptions = new HashMap<String, Map<String, String>>();

    /**
     * The lock for subscription map.
     */
    private final Lock subscriptionLock = new ReentrantLock();

    /**
     * <p>
     * This interface defined a method with a signature like a procedure to process an handler.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 2.2
     * @version 1.0
     */
    private static interface Procedure {

        /**
         * <p>
         * Processes an handler.
         * </p>
         *
         * @param destination the destination associated to the handler
         * @param handler the handler
         * @throws IOException if processing fails
         */
        void apply(String destination, AtmosphereFramework.AtmosphereHandlerWrapper handler) throws IOException;
    }

    /**
     * <p>
     * Gets the handler associated to the mapping specified in the given {@link Header#DESTINATION header} and applies
     * a procedure on it.
     * </p>
     *
     * @param uuid the resource's UUID
     * @param headers the headers with mapping
     * @param framework the framework providing the handler
     * @param call the procedure to call
     * @param byId consider the {@link Header#ID} to find the handler or direclty use the {@link Header#DESTINATION}
     * @throws IOException of procedure fails
     */
    private void callHandler(final String uuid,
                             final Map<Header, String> headers,
                             final AtmosphereFramework framework,
                             final boolean byId,
                             final Procedure call)
            throws IOException {
        final String mapping;

        // We assume that only the ID header exists, so we need to check the mapping that associates the ID to the destination
        if (byId) {
            try {
                subscriptionLock.lock();
                final Map<String, String> asso = subscriptions.get(uuid);

                // Second try with destination header
                if (asso == null) {
                    mapping = headers.get(Header.DESTINATION);
                } else {
                    mapping = asso.get(headers.get(Header.ID));
                }
            } finally {
                subscriptionLock.unlock();
            }
        } else {
            mapping = headers.get(Header.DESTINATION);
        }

        final AtmosphereFramework.AtmosphereHandlerWrapper handler = framework.getAtmosphereHandlers().get(mapping);

        if (handler != null) {
            call.apply(mapping, handler);
        } else {
            logger.warn("No handler found for destination {}", mapping, new IllegalArgumentException());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(final AtmosphereResource resource, final Map<Header, String> headers, final AtmosphereFramework framework)
            throws IOException {
        callHandler(resource.uuid(), headers, framework, false, new Procedure() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply(final String destination, final AtmosphereFramework.AtmosphereHandlerWrapper handler)
                    throws IOException {

                // TODO: go async ?
                try {
                    subscriptionLock.lock();

                    // TODO: how to purge old UUID ?
                    Map<String, String> subscriptionMap = subscriptions.get(resource.uuid());

                    if (subscriptionMap == null) {
                        subscriptionMap = new HashMap<String, String>();
                        subscriptions.put(resource.uuid(), subscriptionMap);
                    }

                    // When we subscribe, we have and ID and a destination
                    // Atmosphere maps to the destination, but STOMP protocol uses subscription ID to unsubscribe
                    // Consequently, we have to track assocation between the ID and the destination related to its subscription
                    subscriptionMap.put(headers.get(Header.ID), destination);
                } finally {
                    subscriptionLock.unlock();
                }

                handler.broadcaster.addAtmosphereResource(resource);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(final AtmosphereResource resource, final Map<Header, String> headers, final AtmosphereFramework framework)
            throws IOException {
        callHandler(resource.uuid(), headers, framework, true, new Procedure() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply(final String destination, final AtmosphereFramework.AtmosphereHandlerWrapper handler) {
                handler.broadcaster.removeAtmosphereResource(framework.getAtmosphereConfig().resourcesFactory().find(resource.uuid()));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final AtmosphereResource atmosphereResource, final Map<Header, String> headers, final String body, final AtmosphereFramework framework)
            throws IOException {
        callHandler(atmosphereResource.uuid(), headers, framework, false, new Procedure() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply(final String destination, final AtmosphereFramework.AtmosphereHandlerWrapper handler) throws IOException {
                atmosphereResource.getRequest().setAttribute(StompInterceptor.STOMP_MESSAGE_BODY,
                        body != null && body.endsWith("\n") ? body.substring(0, body.length() - 1) : body);
                handler.atmosphereHandler.onRequest(atmosphereResource);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin(final AtmosphereResource atmosphereResource) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(final AtmosphereResource atmosphereResource, final Map<Header, String> headers) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort(final AtmosphereResource atmosphereResource, final Map<Header, String> headers) {
        // TODO
    }
}
