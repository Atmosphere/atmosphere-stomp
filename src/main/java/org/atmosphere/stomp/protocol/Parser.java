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

import org.apache.activemq.apollo.broker.store.MessageRecord;
import org.apache.activemq.apollo.stomp.StompCodec;
import org.apache.activemq.apollo.stomp.StompFrameMessage;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.apache.activemq.apollo.stomp.StompFrame;
import scala.Tuple2;
import scala.collection.Iterator;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class parses text stream that respects STOMP protocol to extract a structured {@link Frame} that provides a
 * set of information. A {@link Frame} contains:
 * <ul>
 *     <li>The {@link Action}</li>
 *     <li>The {@link Header headers} and their values</li>
 *     <li>The body in {@code String} value</li>
 * </ul>
 * </p>
 *
 * <p>
 * If the text stream violates the STOMP protocol, then several {@link ParseException} could be thrown.
 * The {@link ParseException} class provides different subclasses that detail the cause.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
public class Parser {

    /**
     * The stream parsed by this parser.
     */
    private String stream;

    /**
     * Resulting frame.
     */
    private StompFrame sm;

    /**
     * The exception that occurred of frame can't be decoded.
     */
    private Exception error;

    /**
     * <p>
     * Builds a new {@link Parser} for the given text stream.
     * </p>
     *
     * @param textStream the {@code String} to parse
     */
    public Parser(final String textStream) {
        stream = textStream;
    }

    /**
     * <p>
     * Parses the {@link #stream} to extract data.
     * </p>
     */
    public void parse() {
        try {
            final MessageRecord mr = new MessageRecord();
            mr.buffer_$eq(new Buffer(stream.getBytes()));
            final StompFrameMessage sfm = StompCodec.decode(mr);
            sm = sfm.frame();
        } catch (Exception spe) {
            error = spe;
        }
    }

    /**
     * <p>
     * Builds a {@link Frame} that provides information extracted from {@link #stream}. If {@link #parse()} has not
     * been already called, then this method invokes it.
     * </p>
     *
     * <p>
     * If the result of the {@link #parse()} operation has detected any protocol violation, then the appropriate
     * {@link ParseException} is thrown by this method.
     * </p>
     *
     * @return the message
     * @throws ParseException if {@link #stream} violates STOMP protocol
     */
    public Frame toFrame() throws ParseException {
        // parse() not already called
        if (sm == null && error == null) {
            parse();
            return toFrame();
        // parse() failed
        } else if (error != null) {
            throw new ParseException(error);
        }

        // Read action
        final Action action = Action.parse(sm.action().toString());

        // Read headers
        final Map<String, String> headers = new HashMap<String, String>();
        final Iterator<Tuple2<AsciiBuffer, AsciiBuffer>> it = sm.headers().iterator();

        // TODO: check mandatory headers
        while (it.hasNext()) {
            final Tuple2<AsciiBuffer, AsciiBuffer> tuple = it.next();
            headers.put(tuple._1().toString(), tuple._2().toString());
        }

        // Read body
        final ByteArrayOutputStream content = new ByteArrayOutputStream();
        sm.content().writeTo(content);

        return new Frame(action, headers, new String(content.toByteArray()));
    }
}
