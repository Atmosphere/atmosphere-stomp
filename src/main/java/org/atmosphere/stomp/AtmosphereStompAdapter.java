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

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResource;

import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * This interface adapts all the operations sent to atmosphere using the STOMP protocol. The purpose is to map existing
 * Atmosphere features to the actions that could be performed with several STOMP frames.
 * </p>
 *
 * <p>
 * Any method of this interface should be invoked when a frame has been validated. Consequently, implementation could
 * assert that all mandatory {@link String Strings} will be specified in parameter when declared in method signature.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public interface AtmosphereStompAdapter {

    /**
     * <p>
     * Adds the given {@link AtmosphereResource} to the {@link org.atmosphere.cpr.Broadcaster} identified with the path
     * specified in the given {@link String Strings}. The {@link AtmosphereResource} lifecycle is then delegated
     * to the {@link org.atmosphere.cpr.Broadcaster} used by the Atmosphere framework.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that sent the subscription frame
     * @param Strings the Strings extracted from the message that contain the destination path
     * @param framework the {@link AtmosphereFramework}
     * @throws IOException if request can't be processed
     * @see org.atmosphere.cpr.Broadcaster#addAtmosphereResource(org.atmosphere.cpr.AtmosphereResource)
     */
    void subscribe(AtmosphereResource atmosphereResource, Map<String, String> Strings, AtmosphereFramework framework) throws IOException;

    /**
     * <p>
     * Removes the given {@link AtmosphereResource} from the {@link org.atmosphere.cpr.Broadcaster} identified with the
     * path specified in the given {@link String Strings}.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that sent the unsubscription frame
     * @param Strings the Strings extracted from the message that contain the destination path
     * @param framework the {@link AtmosphereFramework}
     * @throws IOException if request can't be processed
     * @see org.atmosphere.cpr.Broadcaster#removeAtmosphereResource(org.atmosphere.cpr.AtmosphereResource)
     */
    void unsubscribe(AtmosphereResource atmosphereResource, Map<String, String> Strings, AtmosphereFramework framework) throws IOException;

    /**
     * <p>
     * Invokes the method annotated with {@link org.atmosphere.stomp.annotation.StompService} inside any
     * {@link org.atmosphere.config.service.ManagedService} that defines a destination that matched the destination
     * String inside the specified {@link String Strings}.
     * </p>
     *
     * <p>
     * The result of the method invocation is dispatched by the {@link org.atmosphere.cpr.Broadcaster} identified with
     * the path specified in the given {@link String Strings}. If no annotated method matches the requested send,
     * then the body is dispatched. Original body or result are sent in a {@link org.atmosphere.stomp.protocol.Action#MESSAGE}
     * frame. Finally, if the method invocation throws an exception, then an {@link org.atmosphere.stomp.protocol.Action#ERROR}
     * is sent only to the specified {@link AtmosphereResource} that sent the frame.
     * </p>
     *
     * <p>
     * Note that {@link org.atmosphere.cpr.Broadcaster} must not do anything if a transaction has been started by the
     * {@link AtmosphereResource} and if this transaction is referenced in frame's Strings.
     * </p>
     *
     * @param atmosphereResource the atmosphere resource that sent the send
     * @param Strings the Strings extracted from the message that contain the destination path and optionally a transaction ID
     * @param body the body extracted from the frame
     * @param framework the {@link AtmosphereFramework}
     * @throws IOException if request can't be processed
     * @see #begin(org.atmosphere.cpr.AtmosphereResource)
     */
    void send(AtmosphereResource atmosphereResource, Map<String, String> Strings, String body, AtmosphereFramework framework)
            throws IOException;

    /**
     * <p>
     * Begins a transaction for the given {@link AtmosphereResource}.
     * </p>
     *
     * <p>
     * A transaction ID is generated that could be specified in future {@link org.atmosphere.stomp.protocol.Action#SEND}
     * to include their result in the transaction.
     * </p>
     *
     * <p>
     * In that case, all {@link #send(org.atmosphere.cpr.AtmosphereResource, java.util.Map, String, AtmosphereFramework)}
     * operation results won't be dispatched by the {@link org.atmosphere.cpr.Broadcaster} until the {@link AtmosphereResource}
     * sends a commit frame for the associated transaction ID.
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
     * All {@link #send(org.atmosphere.cpr.AtmosphereResource, java.util.Map, String, AtmosphereFramework)} operation results
     * associated to the transaction ID specified in Strings are dispatched by the {@link org.atmosphere.cpr.Broadcaster}.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that begins the transaction
     * @param Strings the Strings that contain the transaction ID
     */
    void commit(AtmosphereResource atmosphereResource, Map<String, String> Strings);

    /**
     * <p>
     * Aborts a transaction began by the given {@link AtmosphereResource}.
     * </p>
     *
     * <p>
     * All {@link #send(org.atmosphere.cpr.AtmosphereResource, java.util.Map, String, AtmosphereFramework)} operations
     * results associated to the transaction ID specified inside the given Strings are erased.
     * </p>
     *
     * @param atmosphereResource the {@link AtmosphereResource} that aborts the transaction
     * @param Strings the Strings that contain the transaction ID
     */
    void abort(AtmosphereResource atmosphereResource, Map<String, String> Strings);
}
