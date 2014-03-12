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
 * This class parses text stream that respects STOMP protocol to extract a structured {@link Message} that provides a
 * set of information. A {@link Message} contains:
 * <ul>
 *     <li>The {@link Frame}</li>
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
        // TODO
    }

    /**
     * <p>
     * Builds a {@link Message} that provides information extracted from {@link #stream}. If {@link #parse()} has not
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
    public Message toMessage() throws ParseException {
        // TODO
        return null;
    }
}
