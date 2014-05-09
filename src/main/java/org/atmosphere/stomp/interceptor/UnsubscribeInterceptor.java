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
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.stomp.handler.HandlerHelper;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.Subscriptions;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.Header;
import org.atmosphere.stomp.protocol.StompFormat;

import java.io.IOException;

/**
 * <p>
 * Removes the given {@link AtmosphereResource} from the {@link org.atmosphere.cpr.Broadcaster} identified with the
 * path specified in the given {@link String Strings}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.2
 */
public class UnsubscribeInterceptor extends AtmosphereInterceptorAdapter implements StompInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final StompFormat stompFormat, final AtmosphereFramework framework, final Frame frame, final AtmosphereResource resource)
            throws IOException {
        final Action retval = inspect(resource);
        HandlerHelper.INSTANCE.callHandler(resource, frame.getHeaders(), framework, true, new HandlerHelper.Procedure() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply(final Subscriptions subscriptions, final String destination, final AtmosphereFramework.AtmosphereHandlerWrapper handler) {
                handler.broadcaster.removeAtmosphereResource(framework.getAtmosphereConfig().resourcesFactory().find(resource.uuid()));
                subscriptions.removeSubscription(frame.getHeaders().get(Header.ID));
            }
        });

        return retval;
    }
}
