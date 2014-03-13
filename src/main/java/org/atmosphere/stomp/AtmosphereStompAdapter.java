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
 * This interface adapts all the operations sent to atmosphere using the STOMP protocol. The purpose is to map existing
 * Atmosphere features to the actions that could be performed with several STOMP frames.
 * </p>
 *
 * <p>
 * Any method of this interface should be invoked when a frame has been validated. Consequently, implementation could
 * assert that all mandatory {@link Header headers} will be specified in parameter when declared in method signature.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
public interface AtmosphereStompAdapter {

    /**
     * <p>
     * Adds the given {@link AtmosphereResource} to the {@link org.atmosphere.cpr.Broadcaster} identified with the path
     * specified in the given {@link Header headers}. The {@link AtmosphereResource} lifecycle is then delegated
     * to the {@link org.atmosphere.cpr.Broadcaster} used by the Atmosphere framework.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that sent the subscription frame
     * @param headers the headers extracted from the message that contain the destination path
     * @see org.atmosphere.cpr.Broadcaster#addAtmosphereResource(org.atmosphere.cpr.AtmosphereResource)
     */
    void subscribe(AtmosphereResource atmosphereResource, Map<Header, String> headers);

    /**
     * <p>
     * Removes the given {@link AtmosphereResource} from the {@link org.atmosphere.cpr.Broadcaster} identified with the
     * path specified in the given {@link Header headers}.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that sent the unsubscription frame
     * @param headers the headers extracted from the message that contain the destination path
     * @see org.atmosphere.cpr.Broadcaster#removeAtmosphereResource(org.atmosphere.cpr.AtmosphereResource)
     */
    void unsubscribe(AtmosphereResource atmosphereResource, Map<Header, String> headers);

    /**
     * <p>
     * Invokes the method annotated with {@link org.atmosphere.stomp.annotation.StompService} inside any
     * {@link org.atmosphere.config.service.ManagedService} that defines a destination that matched the destination
     * header inside the specified {@link Header headers}.
     * </p>
     *
     * <p>
     * The result of the method invocation is dispatched by the {@link org.atmosphere.cpr.Broadcaster} identified with
     * the path specified in the given {@link Header headers}. If no annotated method matches the requested send,
     * then the body is dispatched. Original body or result are sent in a {@link org.atmosphere.stomp.protocol.Frame#MESSAGE}
     * frame. Finally, if the method invocation throws an exception, then an {@link org.atmosphere.stomp.protocol.Frame#ERROR}
     * is sent only to the specified {@link AtmosphereResource} that sent the frame.
     * </p>
     *
     * <p>
     * Note that {@link org.atmosphere.cpr.Broadcaster} must not do anything if a transaction has been started by the
     * {@link AtmosphereResource} and if this transaction is referenced in frame's headers.
     * </p>
     *
     * @param atmosphereResource the atmosphere resource that sent the send
     * @param headers the headers extracted from the message that contain the destination path and optionally a transaction ID
     * @param body the body extracted from the frame
     * @see #begin(org.atmosphere.cpr.AtmosphereResource)
     */
    void send(AtmosphereResource atmosphereResource, Map<Header, String> headers, String body);

    /**
     * <p>
     * Begins a transaction for the given {@link AtmosphereResource}.
     * </p>
     *
     * <p>
     * A transaction ID is generated that could be specified in future {@link org.atmosphere.stomp.protocol.Frame#SEND}
     * to include their result in the transaction.
     * </p>
     *
     * <p>
     * In that case, all {@link #send(org.atmosphere.cpr.AtmosphereResource, java.util.Map, String)} operation results
     * won't be dispatched by the {@link org.atmosphere.cpr.Broadcaster} until the {@link AtmosphereResource} sends a
     * commit frame for the associated transaction ID.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that begins the transaction
     */
    void begin(AtmosphereResource atmosphereResource);

    /**
     * <p>
     * Commits a transaction started by the given {@link AtmosphereResource}.
     * </p>
     *
     * <p>
     * All {@link #send(org.atmosphere.cpr.AtmosphereResource, java.util.Map, String)} operation results
     * associated to the transaction ID specified in headers are dispatched by the {@link org.atmosphere.cpr.Broadcaster}.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that begins the transaction
     * @param headers the headers that contain the transaction ID
     */
    void commit(AtmosphereResource atmosphereResource, Map<Header, String> headers);

    /**
     * <p>
     * Aborts a transaction began by the given {@link AtmosphereResource}.
     * </p>
     *
     * <p>
     * All {@link #send(org.atmosphere.cpr.AtmosphereResource, java.util.Map, String)} operations results associated to
     * the transaction ID specified inside the given headers are erased.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that aborts the transaction
     * @param headers the headers that contain the transaction ID
     */
    void abort(AtmosphereResource atmosphereResource, Map<Header, String> headers);

}
