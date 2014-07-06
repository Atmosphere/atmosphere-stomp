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
import org.atmosphere.cpr.AsynchronousProcessor;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEventImpl;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.StompFormat;

import java.io.IOException;

/**
 * <p>
 * Evaluates the {@link org.atmosphere.stomp.protocol.Action#DISCONNECT connection} frame.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.3
 */
public class DisconnectInterceptor extends AtmosphereInterceptorAdapter implements StompInterceptor {

    /**
     * Configured asynchronous processor.
     */
    private AsynchronousProcessor p;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final AtmosphereConfig config) {
        if (AsynchronousProcessor.class.isAssignableFrom(config.framework().getAsyncSupport().getClass())) {
            p = AsynchronousProcessor.class.cast(config.framework().getAsyncSupport());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final StompFormat stompFormat,
                          final AtmosphereFramework framework,
                          final Frame frame,
                          final AtmosphereResource resource)
            throws IOException {
        // Block websocket closing detection
        AtmosphereResourceEventImpl.class.cast(resource.getAtmosphereResourceEvent()).isClosedByClient(true);
        p.completeLifecycle(resource, false);
        return Action.CANCELLED;
    }
}
