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
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

    /**
     * <p>
     * Creates a new instance.
     * </p>
     */
    public StompGlobalAtmosphereHandler() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequest(final AtmosphereResource atmosphereResource) throws IOException {
        logger.info("Suspending AtmosphereResource with UUID {}", atmosphereResource.uuid());
        atmosphereResource.suspend();
    }
}
