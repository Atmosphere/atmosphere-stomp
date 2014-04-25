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

import org.apache.activemq.apollo.stomp.BufferContent;
import org.apache.activemq.apollo.stomp.StompCodec;
import org.apache.activemq.apollo.stomp.StompContent;
import org.apache.activemq.apollo.stomp.StompFrame;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.DataByteArrayOutputStream;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This {@link StompFormat} implementation is based in apache Apollo.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
public class StompFormatImpl implements StompFormat {

    /**
     * {@inheritDoc}
     */
    @Override
    public Frame parse(final String str) throws ParseException {
        final Parser parser = new Parser(str);
        parser.parse();
        return parser.toFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(final Frame msg) {
        final DataByteArrayOutputStream dbaos = new DataByteArrayOutputStream();
        final List<Tuple2<AsciiBuffer, AsciiBuffer>> headers = new ArrayList<Tuple2<AsciiBuffer, AsciiBuffer>>();

        for (final Map.Entry<String, String> header : msg.getHeaders().entrySet()) {
            headers.add(new Tuple2<AsciiBuffer, AsciiBuffer>(new AsciiBuffer(header.getKey().toString().getBytes()), new AsciiBuffer(header.getValue().getBytes())));
        }

        final StompContent content = new BufferContent(new AsciiBuffer(msg.getBody().getBytes(), 0, msg.getBody().length()));
        final StompFrame sf = new StompFrame(new AsciiBuffer("MESSAGE".getBytes()),
                JavaConversions.asScalaBuffer(headers).toList(), content, false, JavaConversions.asScalaBuffer(new ArrayList<Tuple2<AsciiBuffer, AsciiBuffer>>()).toList());
        new StompCodec().encode(sf, dbaos);
        return new String(dbaos.getData());
    }
}
