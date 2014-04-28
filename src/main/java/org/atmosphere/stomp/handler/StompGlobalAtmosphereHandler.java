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

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.stomp.Subscriptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * <p>
 * This handler suspends all connections established to the server.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class StompGlobalAtmosphereHandler extends AbstractReflectorAtmosphereHandler {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AtmosphereResourceSessionFactory arsf;

    /**
     * <p>
     * Creates a new instance.
     * </p>
     */
    public StompGlobalAtmosphereHandler() {
        arsf = AtmosphereResourceSessionFactory.getDefault();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequest(final AtmosphereResource atmosphereResource) throws IOException {
        logger.info("Suspending AtmosphereResource with UUID {}", atmosphereResource.uuid());

        // If this handler is reached, we always suspend the connection
        atmosphereResource.suspend();

        // The client can reconnect while he has already subscribed different destinations
        // We need to add the new request to the associated broadcasters
        final Subscriptions s = Subscriptions.getFromSession(arsf.getSession(atmosphereResource));
        final Set<String> destinations = s.getAllDestinations();

        for (final String d : destinations) {
            BroadcasterFactory.getDefault().lookup(d).addAtmosphereResource(atmosphereResource);
        }
    }
}
