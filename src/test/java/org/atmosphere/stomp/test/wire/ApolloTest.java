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

import org.apache.activemq.apollo.broker.store.MessageRecord;
import org.apache.activemq.apollo.stomp.StompCodec;
import org.apache.activemq.apollo.stomp.StompFrame;
import org.apache.activemq.apollo.stomp.StompFrameMessage;
import org.apache.activemq.util.DataByteArrayOutputStream;
import org.fusesource.hawtbuf.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Tests with Apollo.
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class ApolloTest extends Base {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Tests decode.
     */
    @Test
    public void decodeClientFrameTest() {
        final long start = System.currentTimeMillis();

        for (final String frame : clientFrames) {
            final MessageRecord mr = new MessageRecord();
            mr.buffer_$eq(new Buffer(frame.getBytes()));
            StompFrameMessage sfm = StompCodec.decode(mr);
            final StompFrame sm = sfm.frame();
            logger.info(sm.action().toString());
        }

        final long ms = System.currentTimeMillis() - start;
        logger.info("Apollo decodes client frames in {}ms", ms);
    }


    /**
     * Tests encode.
     */
    @Test
    public void encodeServerFrameTest() {
        final long start = System.currentTimeMillis();

        for (final StompFrame frame : serverFrames) {
            final DataByteArrayOutputStream dbaos = new DataByteArrayOutputStream(32);
            new StompCodec().encode(frame, dbaos);
            logger.info(new String(dbaos.getData()));
        }

        final long ms = System.currentTimeMillis() - start;
        logger.info("Apollo encodes server frames in {}ms", ms);
    }
}
