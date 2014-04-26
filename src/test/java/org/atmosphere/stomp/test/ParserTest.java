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

import org.atmosphere.stomp.protocol.Action;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.Header;
import org.atmosphere.stomp.protocol.ParseException;
import org.atmosphere.stomp.protocol.Parser;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Test {@link Parser} class.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class ParserTest {

    /**
     * {@link org.atmosphere.stomp.protocol.Parser#parse()} then {@link org.atmosphere.stomp.protocol.Parser#toFrame()}.
     *
     * @throws ParseException if test fails
     */
    @Test
    public void nominalTest() throws ParseException {
        final Parser parser = new Parser(StompFrames.SEND_FRAME);
        parser.parse();
        final Frame frame = parser.toFrame();
        Assert.assertEquals(frame.getAction(), Action.SEND);

        Assert.assertNotNull(frame.getHeaders().get(Header.DESTINATION));
        Assert.assertNotNull(frame.getHeaders().get(Header.CONTENT_TYPE));

        Assert.assertEquals(frame.getHeaders().get(Header.DESTINATION), "/queue/a");
        Assert.assertEquals(frame.getHeaders().get(Header.CONTENT_TYPE), "text/plain");

        Assert.assertTrue(frame.getBody().startsWith("hello queue a"));
    }
}
