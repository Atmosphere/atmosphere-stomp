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


package org.atmosphere.stomp.protocol;

/**
 * <p>
 * This enumeration defines all the action that could performed in STOMP protocol.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public enum Action {

    /**
     * When client connects.
     */
    CONNECT,

    /**
     * When client disconnects.
     */
    DISCONNECT,

    /**
     * Legacy connection frame.
     */
    STOMP,

    /**
     * Connection notification from server.
     */
    CONNECTED,

    /**
     * Client data push for a subscription.
     */
    SEND,

    /**
     * Begins a transaction.
     */
    BEGIN,

    /**
     * Commits a transaction.
     */
    COMMIT,

    /**
     * Aborts a transaction/
     */
    ABORT,

    /**
     * When client subscribes to a destination.
     */
    SUBSCRIBE,

    /**
     * When client stop its subscription to a destination.
     */
    UNSUBSCRIBE,

    /**
     * Acknowledgement sent to the server.
     */
    ACK,

    /**
     * Negative acknowledgement sent to the server.
     */
    NACK,

    /**
     * Message pushed by the server.
     */
    MESSAGE,

    /**
     * Receipt pushed by the server.
     */
    RECEIPT,

    /**
     * Error pushed by the server.
     */
    ERROR;

    /**
     * <p>
     * Gets an {@link Action} from its {@code String} representation.
     * </p>
     *
     * @param str the string representation
     * @return the corresponding {@link Action}
     * @throws IllegalActionException if the no action match the string representation
     */
    public static Action parse(final String str) throws IllegalActionException {
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new IllegalActionException();
        }
    }
}