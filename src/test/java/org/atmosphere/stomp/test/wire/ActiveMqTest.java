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

import org.apache.activemq.transport.stomp.StompWireFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Tests with Active MQ.
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class ActiveMqTest extends Base {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Broken. http://activemq.2283324.n4.nabble.com/STOMP-EOFException-when-content-length-header-missing-td4679460.html
     */
    @Test(enabled = false)
    public void decodeClientFrameTest() throws IOException {
        final long start = System.currentTimeMillis();
        for (final String frame : clientFrames) {
            logger.info(frame);
            new StompWireFormat().unmarshal(new DataInputStream(new ByteArrayInputStream(frame.getBytes())));
        }

        final long ms = System.currentTimeMillis() - start;
        logger.info("ActivesMQ decodes client frames {}ms", ms);
    }
}
