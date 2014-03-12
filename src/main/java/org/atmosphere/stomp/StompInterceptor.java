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


package org.atmosphere.stomp;

import org.atmosphere.cpr.AtmosphereInterceptorAdapter;

/**
 * <p>
 * This interceptor reads the messages and parse it thanks to the {@link org.atmosphere.stomp.protocol.Parser}. When
 * the message is parsed, the interceptor invokes a method provided by an {@link AtmosphereInterceptorAdapter} according
 * to the {@link org.atmosphere.stomp.protocol.Frame} specified inside the message.
 * </p>
 *
 * <p>
 * This interceptor expects by default that all messages respect the STOMP protocol. It could be fault tolerant for
 * messages not respecting STOMP by setting the {@link #IGNORE_ERROR} setting in atmosphere.xml or web.xml file.
 * </p>
 *
 * <p>
 * By default the interceptor uses by default {@link AtmosphereStompAdapterImpl} to delegate the frame processing. User
 * can set its own implementation by specifying the class name in {@link #ADAPTER_CLASS} setting in atmosphere.xml or
 * web.xml file.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
public class StompInterceptor extends AtmosphereInterceptorAdapter {

    /**
     * Setting that specifies if the interceptor ignores messages that don't respect STOMP protocol.
     */
    public static final String IGNORE_ERROR = "org.atmosphere.stomp.ignoreError";

    /**
     * Setting that specifies the {@link AtmosphereStompAdapter} implementation class used by the interceptor.
     */
    public static final String ADAPTER_CLASS = "org.atmosphere.stomp.atmosphereStompAdapterClass";
}
