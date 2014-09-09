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

import org.atmosphere.HeartbeatAtmosphereResourceEvent;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.packages.StompEndpointProcessor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.Header;
import org.atmosphere.util.Version;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Evaluates the {@link org.atmosphere.stomp.protocol.Action#CONNECT connection} frame.
 * </p>
 *
 * <p>
 * Key features are the heartbeat negotiation and the user authentication when associated headers are specified in the
 * frame.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.2
 */
public class ConnectInterceptor extends HeartbeatInterceptor implements StompInterceptor {

    /**
     * The padding data for STOMP heartbeat.
     */
    public static final byte[] STOMP_HEARTBEAT_DATA = new byte[] { 0x0A, };

    /**
     * The default supported version.
     */
    public static final float DEFAULT_VERSION = 1.0f;

    /**
     * The highest version we currently support.
     */
    public static final float HIGHEST_VERSION = 1.1f;

    /**
     * Supported versions.
     */
    public static final String VERSIONS = String.format("%f,%f", DEFAULT_VERSION, HIGHEST_VERSION);

    /**
     * Server name sent to client.
     */
    private static final String SERVER = "Atmosphere/" + Version.getDotedVersion();

    /**
     * Heartbeat desired by client.
     */
    private ThreadLocal<Integer> desiredHeartbeat = new ThreadLocal<Integer>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final AtmosphereConfig config) {
        super.configure(config);

        // Atmosphere.js does not like empty strings, works fine with "hb".
        paddingText(/*STOMP_HEARTBEAT_DATA*/"hb".getBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int extractHeartbeatInterval(final AtmosphereResourceImpl resource) {
        return desiredHeartbeat.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final AtmosphereFramework framework, final Frame frame, final FrameInterceptor.StompAtmosphereResource r) {
        try {
            // Hack: we suspect a heartbeat here
            if (org.atmosphere.stomp.protocol.Action.NULL.equals(frame.getAction())) {
                // Dispatch an event to notify that a heartbeat has been intercepted
                // TODO: see https://github.com/Atmosphere/atmosphere/issues/1561
                final AtmosphereResourceEvent event = new HeartbeatAtmosphereResourceEvent(AtmosphereResourceImpl.class.cast(r.getResource()));

                r.getResource().addEventListener(new AtmosphereResourceEventListenerAdapter.OnHeartbeat() {
                    @Override
                    public void onHeartbeat(AtmosphereResourceEvent event) {
                        StompEndpointProcessor.invokeOnHeartbeat(event);
                    }
                });

                // Fire event
                r.getResource().notifyListeners(event);

                desiredHeartbeat.set(0);
                return inspect(r.getResource());
            }

            // Send headers response to client
            final Map<String, String> headers = new HashMap<String, String>();

            // Protocol negotiation
            final float version = parseVersion(frame.getHeaders().get(Header.ACCEPT_VERSION));

            // No version in common between server and client
            if (version == -1) {
                headers.put(Header.VERSION, VERSIONS);
                r.write(org.atmosphere.stomp.protocol.Action.ERROR, headers, "Supported protocol versions are " + VERSIONS);

                return Action.CANCELLED;
            } else {
                // Extracts heartbeat then clock
                final Integer[] intervals = parseHeartBeat(frame.getHeaders().get(Header.HEART_BEAT));

                // Extract the desired heartbeat interval
                // Won't be applied if lower than config value
                int serverInterval = intervals[1];

                if (serverInterval != 0) {
                    serverInterval = Math.max((int) TimeUnit.SECONDS.convert(serverInterval, TimeUnit.MILLISECONDS), heartbeatFrequencyInSeconds());
                } else {
                    serverInterval = 0;
                }

                desiredHeartbeat.set(serverInterval);
                final Action retval = inspect(r.getResource());

                headers.put(Header.VERSION, String.valueOf(version));
                headers.put(Header.SESSION, r.getResource().uuid());
                headers.put(Header.SERVER, SERVER);
                headers.put(Header.HEART_BEAT, TimeUnit.MILLISECONDS.convert(serverInterval, TimeUnit.SECONDS) + "," + intervals[0]);

                r.write(org.atmosphere.stomp.protocol.Action.CONNECTED, headers);

                return retval;
            }
        } finally {
            desiredHeartbeat.remove();
        }
    }

    /**
     * <p>
     * Parse the given header value to extract the most appropriate version sent by client. If value is {@code null},
     * then {@link #DEFAULT_VERSION} is returned. Otherwise, the method looks for the highest version supported by
     * both client and server. If no version are in common, -1 is returned and an error should be sent
     * </p>
     *
     * @param acceptVersion the header value
     * @return the extracted version
     */
    private float parseVersion(final String acceptVersion) {
        float retval;

        if (acceptVersion != null) {
            retval = -1;
            final String[] versions = acceptVersion.split(",");

            for (final String version : versions) {
                final float clientVersion = Float.parseFloat(version);

                if (clientVersion == DEFAULT_VERSION || clientVersion == HIGHEST_VERSION) {
                    retval = Math.max(retval, clientVersion);
                }
            }
        } else {
            retval = DEFAULT_VERSION;
        }

        return retval;
    }

    /**
     * <p>
     * Extracts the heartbeat from the given header value. The value contains two integers. The first one is the
     * heartbeat interval in milliseconds the sender of the header can assume. The second one is what he wants to
     * receive.
     * </p>
     *
     * <p>
     * If the value is {@code null} then 0 will be returned for each direction do disable heartbeat.
     * </p>
     *
     * @param heartbeat the header value
     * @return the parsed array
     */
    private Integer[] parseHeartBeat(final String heartbeat) {
        if (heartbeat == null) {
            return new Integer[] { 0, 0, };
        } else {
            final String[] arr = heartbeat.split(",");
            return new Integer[] { Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), };
        }
    }
}
