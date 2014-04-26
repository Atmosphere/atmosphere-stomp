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


package org.atmosphere.stomp.test;

/**
 * <p>
 * Basic frames.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class StompFrames {

    /**
     * Send.
     */
    public static final String SEND_FRAME = "SEND\n" +
            "destination:/queue/a\n" +
            "content-type:text/plain\n" +
            "\n" +
            "hello queue a\n" +
            "^@";

    /**
     * Subscribe.
     */
    public static final String SUBSCRIBE_FRAME = "SUBSCRIBE\n" +
            "id:0\n" +
            "destination:/queue/foo\n" +
            "ack:client\n" +
            "\n" +
            "^@";

    /**
     * Unsubscribe.
     */
    public static final String UNSUBSCRIBE_FRAME = "UNSUBSCRIBE\n" +
            "id:0\n" +
            "\n" +
            "^@";

    /**
     * ACK.
     */
    public static final String ACK_FRAME = "ACK\n" +
            "id:12345\n" +
            "transaction:tx1\n" +
            "\n" +
            "^@";

    /**
     * Begin.
     */
    public static final String BEGIN_FRAME = "BEGIN\n" +
            "transaction:tx1\n" +
            "\n" +
            "^@";

    /**
     * Commit.
     */
    public static final String COMMIT_FRAME = "COMMIT\n" +
            "transaction:tx1\n" +
            "\n" +
            "^@";

    /**
     * Abort.
     */
    public static final String ABORT_FRAME = "ABORT\n" +
            "transaction:tx1\n" +
            "\n" +
            "^@";

    /**
     * Disconnect.
     */
    public static final String DISCONNECT_FRAME = "DISCONNECT\n" +
            "receipt:77\n" +
            "\n" +
            "^@";
}
