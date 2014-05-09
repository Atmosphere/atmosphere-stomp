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
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.Header;
import org.atmosphere.stomp.protocol.StompFormat;

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
     * Heartbeat desired by client.
     */
    private ThreadLocal<Integer> desiredHeartbeat = new ThreadLocal<Integer>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final AtmosphereConfig config) {
        super.configure(config);
        paddingText(new byte[] { 0x0A, });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int extractHeartbeatInterval(final AtmosphereResource resource) {
        // Extract the desired heartbeat interval
        // Won't be applied if lower than config value
        int interval = desiredHeartbeat.get();

        if (interval != 0) {
            interval = Math.max((int) TimeUnit.SECONDS.convert(interval, TimeUnit.MILLISECONDS), heartbeatFrequencyInSeconds());
        } else {
            interval = 0;
        }

        return interval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final StompFormat stompFormat, final AtmosphereFramework framework, final Frame frame, final AtmosphereResource r) {
        try {
            final Integer[] intervals = parseHeartBeat(frame.getHeaders().get(Header.HEART_BEAT));
            desiredHeartbeat.set(intervals[1]);
            return inspect(r);
        } finally {
            desiredHeartbeat.remove();
        }
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
