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

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.stomp.protocol.Header;

import java.util.Map;

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
     * {@inheritDoc}
     */
    @Override
    public void subscribe(final AtmosphereResource atmosphereResource, final Map<Header, String> headers) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(final AtmosphereResource atmosphereResource, final Map<Header, String> headers) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final AtmosphereResource atmosphereResource, final Map<Header, String> headers, String body) {
        // TODO
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
