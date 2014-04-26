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

import java.util.Collections;
import java.util.Map;

/**
 * <p>
 * In STOMP protocol, the frame basically defines an {@link Action}, some {@link Header headers} and eventually a body.
 * </p>
 *
 * <p>
 * A frame is immutable.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public final class Frame {

    /**
     * The action.
     */
    private final Action action;

    /**
     * The body.
     */
    private final String body;

    /**
     * The headers.
     */
    private final Map<String, String> headers;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     *
     * @param action the action
     * @param headers the headers
     * @param body the body
     */
    public Frame(final Action action, final Map<String, String> headers, final String body) {
        this.action = action;
        this.body = body;

        headers.put(Header.CONTENT_LENGTH, String.valueOf(body == null ? 0 : body.getBytes().length));
        this.headers = Collections.unmodifiableMap(headers);

    }

    /**
     * <p>
     * Gets the action.
     * </p>
     *
     * @return
     */
    public Action getAction() {
        return action;
    }

    /**
     * <p>
     * Gets the body.
     * </p>
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * <p>
     * Gets the headers.
     * </p>
     *
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
}
