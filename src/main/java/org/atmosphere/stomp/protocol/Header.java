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
 * This interface defines all default headers in STOMP protocol.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public interface Header {

    /**
     * The versions of the STOMP protocol the client supports.
     */
    String ACCEPT_VERSION = "accept-version";

    /**
     * The name of a virtual host that the client wishes to connect to.
     */
    String HOST = "host";

    /**
     * The user id used to authenticate against a secured STOMP server.
     */
    String LOGIN = "login";

    /**
     * The password used to authenticate against a secured STOMP server.
     */
    String PASSCODE = "passcode";

    /**
     * Specify the test of healthiness of the underlying TCP connection
     */
    String HEART_BEAT = "heart-beat";

    /**
     * Indicates the destination to which the client wants to subscribe.
     */
    String DESTINATION = "destination";

    /**
     * Mime type which describes the format of the body.
     */
    String CONTENT_TYPE = "content-type";

    /**
     * byte count for the length of the message body.
     */
    String CONTENT_LENGTH = "content-length";

    /**
     * The subscription ID.
     */
    String ID = "id";

    /**
     * Specify message acknowledgment.
     */
    String ACK = "ack";

    /**
     * The transaction ID.
     */
    String TRANSACTION = "transaction";

    /**
     * Ask for server acknowledgment.
     */
    String RECEIPT = "receipt";

    /**
     * Server acknowledgment ID.
     */
    String RECEIPT_ID = "receipt-id";

    /**
     * Subscription ID.
     */
    String SUBSCRIPTION = "subscription";

    /**
     * Delivered message ID.
     */
    String MESSAGE_ID = "message-id";

    /**
     * Error message.
     */
    String MESSAGE = "message";
}