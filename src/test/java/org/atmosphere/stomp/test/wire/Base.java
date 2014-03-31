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


package org.atmosphere.stomp.test.wire;

import org.apache.activemq.apollo.stomp.BufferContent;
import org.apache.activemq.apollo.stomp.StompContent;
import org.apache.activemq.apollo.stomp.StompFrame;
import org.atmosphere.stomp.test.StompFrames;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.testng.annotations.BeforeClass;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for wire libraries test.
 */
public class Base {

    /**
     * Frames to parse for client.
     */
    protected List<String> clientFrames;

    /**
     * Frames to format for server.
     */
    protected List<StompFrame> serverFrames;

    /**
     * Initializes frames.
     */
    @BeforeClass
    public void initFrames() {
        clientFrames = new ArrayList<String>();

        for (int i = 0; i < 1000; i++) {
            clientFrames.add(StompFrames.SEND_FRAME);
        }

        for (int i = 0; i < 100; i++) {
            clientFrames.add(StompFrames.SUBSCRIBE_FRAME);
        }

        for (int i = 0; i < 10; i++) {
            clientFrames.add(StompFrames.UNSUBSCRIBE_FRAME);
        }

        for (int i = 0; i < 100; i++) {
            clientFrames.add(StompFrames.DISCONNECT_FRAME);
        }

        for (int i = 0; i < 1000; i++) {
            clientFrames.add(StompFrames.ACK_FRAME);
        }

        for (int i = 0; i < 100; i++) {
            clientFrames.add(StompFrames.BEGIN_FRAME);
        }

        for (int i = 0; i < 80; i++) {
            clientFrames.add(StompFrames.COMMIT_FRAME);
        }

        for (int i = 0; i < 20; i++) {
            clientFrames.add(StompFrames.ABORT_FRAME);
        }

        Collections.shuffle(clientFrames);

        serverFrames = new ArrayList<StompFrame>();

        for (int i = 0; i < 1000; i++) {
            Tuple2<AsciiBuffer, AsciiBuffer> subscription = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("subscription".getBytes()), new AsciiBuffer("0".getBytes()));
            Tuple2<AsciiBuffer, AsciiBuffer> id = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("message-id".getBytes()), new AsciiBuffer("007".getBytes()));
            Tuple2<AsciiBuffer, AsciiBuffer> destination = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("destination".getBytes()), new AsciiBuffer("/queue/a".getBytes()));
            Tuple2<AsciiBuffer, AsciiBuffer> contentType = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("content-type".getBytes()), new AsciiBuffer("text/plain".getBytes()));
            final StompContent content = new BufferContent(new AsciiBuffer("hello queue a".getBytes(), 0, "hello queue a".length()));
            final StompFrame sf = new StompFrame(new AsciiBuffer("MESSAGE".getBytes()),
                    JavaConversions.asScalaBuffer(Arrays.asList(subscription, id)).toList(), content, false, JavaConversions.asScalaBuffer(Arrays.asList(destination, contentType)).toList());
            serverFrames.add(sf);
        }

        for (int i = 0; i < 1000; i++) {
            Tuple2<AsciiBuffer, AsciiBuffer> id = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("receipt-id".getBytes()), new AsciiBuffer("message-123456".getBytes()));
            final StompContent content = new BufferContent(new AsciiBuffer("".getBytes(), 0, 0));
            final StompFrame sf = new StompFrame(new AsciiBuffer("RECEIPT".getBytes()),
                    JavaConversions.asScalaBuffer(Arrays.asList(id)).toList(), content, false, JavaConversions.asScalaBuffer(new ArrayList<Tuple2<AsciiBuffer, AsciiBuffer>>()).toList());
            serverFrames.add(sf);
        }

        for (int i = 0; i < 50; i++) {
            final String body = "The message:\n" +
                    "-----\n" +
                    "MESSAGE\n" +
                    "destined:/queue/a\n" +
                    "receipt:message-12345\n" +
                    "\n" +
                    "Hello queue a!\n" +
                    "-----\n" +
                    "Did not contain a destination header, which is REQUIRED\n" +
                    "for message propagation.";
            Tuple2<AsciiBuffer, AsciiBuffer> message = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("message".getBytes()), new AsciiBuffer("malformed frame received".getBytes()));
            Tuple2<AsciiBuffer, AsciiBuffer> id = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("receipt-id".getBytes()), new AsciiBuffer("message-123456".getBytes()));
            Tuple2<AsciiBuffer, AsciiBuffer> contentLength = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("content-length".getBytes()), new AsciiBuffer(String.valueOf(body.length()).getBytes()));
            Tuple2<AsciiBuffer, AsciiBuffer> contentType = new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer("content-type".getBytes()), new AsciiBuffer("text/plain".getBytes()));
            final StompContent content = new BufferContent(new AsciiBuffer(body.getBytes(), 0, body.length()));
            final StompFrame sf = new StompFrame(new AsciiBuffer("ERROR".getBytes()),
                    JavaConversions.asScalaBuffer(Arrays.asList(message, id)).toList(), content, false, JavaConversions.asScalaBuffer(Arrays.asList(contentLength, contentType)).toList());
            serverFrames.add(sf);
        }

        Collections.shuffle(serverFrames);
    }
}
