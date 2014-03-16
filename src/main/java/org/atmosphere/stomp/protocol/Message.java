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

import java.util.Map;

public class Message {

    private Frame frame;

    private String body;

    private Map<Header, String> headers;

    public Message(Frame frame, Map<Header, String> headers, String body) {
        this.frame = frame;
        this.headers = headers;
        this.body = body;
    }

    public Frame getFrame() {
        return frame;
    }

    public String getBody() {
        return body;
    }

    public Map<Header, String> getHeaders() {
        return headers;
    }

}
