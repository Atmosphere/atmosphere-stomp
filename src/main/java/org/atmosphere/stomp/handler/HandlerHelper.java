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


package org.atmosphere.stomp.handler;

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.stomp.Subscriptions;
import org.atmosphere.stomp.protocol.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * This enum defines a singleton that helps to extract an {@link org.atmosphere.cpr.AtmosphereHandler} according to the
 * state of an {@link AtmosphereResource}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.2
 */
public enum HandlerHelper {

    /**
     * Singleton.
     */
    INSTANCE;

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * <p>
     * This interface defined a method with a signature like a procedure to process an handler.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.1
     * @version 1.1
     */
    public static interface Procedure {

        /**
         * <p>
         * Processes an handler.
         * </p>
         *
         * @param subscriptions the subscriptions associated to the atmosphere resource
         * @param destination the destination associated to the handler
         * @param handler the handler
         * @throws java.io.IOException if processing fails
         */
        void apply(Subscriptions subscriptions, String destination, AtmosphereFramework.AtmosphereHandlerWrapper handler)
                throws IOException;
    }

    /**
     * <p>
     * Gets the handler associated to the mapping specified in the given {@link org.atmosphere.stomp.protocol.Header#DESTINATION header}
     * and applies a procedure on it.
     * </p>
     *
     * @param resource the resource
     * @param headers the headers with mapping
     * @param framework the framework providing the handler
     * @param call the procedure to call
     * @param byId consider the {@link org.atmosphere.stomp.protocol.Header#ID} to find the handler or directly use the {@link org.atmosphere.stomp.protocol.Header#DESTINATION}
     * @throws IOException of procedure fails
     */
    public void callHandler(final AtmosphereResource resource,
                             final Map<String, String> headers,
                             final AtmosphereFramework framework,
                             final boolean byId,
                             final Procedure call)
            throws IOException {
        final String mapping;
        final Subscriptions retval = Subscriptions.getFromSession(framework.sessionFactory().getSession(resource));

        // We assume that only the ID header exists, so we need to check the mapping that associates the ID to the destination
        if (byId) {
            mapping = retval.getDestinationForId(headers.get(Header.ID));
        } else {
            mapping = headers.get(Header.DESTINATION);
        }

        // The EndpointMapper is a little bit slower than retrieving the AtmosphereHandler directly from the Map, but
        // EndpointMapper support URI mapping, which is always stronger than direct mapping.
        final AtmosphereFramework.AtmosphereHandlerWrapper handler =
                framework.endPointMapper().map(mapping, framework.getAtmosphereHandlers());

        if (handler != null) {
            call.apply(retval, mapping, handler);
        } else {
            logger.warn("No handler found for destination {}", mapping, new IllegalArgumentException());
        }
    }
}
