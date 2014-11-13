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

import org.atmosphere.cpr.AtmosphereResourceSessionFactory;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcastFilterLifecycle;
import org.atmosphere.cpr.PerRequestBroadcastFilter;
import org.atmosphere.cpr.AtmosphereConfig;

import org.atmosphere.stomp.interceptor.FrameInterceptor;
import org.atmosphere.stomp.protocol.Action;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.Header;
import org.atmosphere.stomp.protocol.StompFormat;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * <p>
 * This filter transforms the broadcasted {@code String} considered as STOMP body to a {@link Action#MESSAGE message}.
 * A specific frame will be created and broadcasted for each subscription for the mapping (the broadcaster ID).
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class StompBroadcastFilter implements PerRequestBroadcastFilter, BroadcastFilterLifecycle {

    /**
     * The formatter for frame generation.
     */
    private StompFormat stompFormat;


    /**
     * {@inheritDoc}
     */
    @Override
    public BroadcastAction filter(final String broadcasterId,
                                  final AtmosphereResource atmosphereResource,
                                  final Object originalMessage,
                                  final Object message) {
        // Get the subscriptions
        final Subscriptions subscriptions = Subscriptions.getFromSession(atmosphereResource.getAtmosphereConfig().sessionFactory().getSession(atmosphereResource));
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put(Header.DESTINATION, broadcasterId);

        final List<String> subscriptionsIds = subscriptions.getSubscriptionsForDestination(broadcasterId);
        final StringBuilder sb = new StringBuilder();

        // Generate a frame for each subscription
        for (final String id : subscriptionsIds) {
            headers.put(Header.MESSAGE_ID, String.valueOf(UUID.randomUUID()));
            headers.put(Header.SUBSCRIPTION, id);
            final Frame frame = new Frame(Action.MESSAGE, headers, String.valueOf(message));
            sb.append(stompFormat.format(frame)).append("\n");
        }

        // If the resource is added to the broadcaster that triggered the call to the filter, then at least one subscription must exists
        if (sb.length() == 0) {
            throw new IllegalStateException();
        } else {
            return new BroadcastAction(sb.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BroadcastAction filter(final String broadcasterId, final Object originalMessage, final Object message) {
        return new BroadcastAction(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final AtmosphereConfig config) {
        stompFormat = FrameInterceptor.PropertyClass.STOMP_FORMAT_CLASS.retrieve(StompFormat.class, config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // Let's gc do its job...
        this.stompFormat = null;
    }
}
