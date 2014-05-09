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

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.StompFormat;

import java.io.IOException;

/**
 * <p>
 * A {@link StompInterceptor} is an extension of {@link AtmosphereInterceptor} to inspect an {@link AtmosphereResource}
 * that contains in its request body a {@link Frame}.
 * </p>
 *
 * <p>
 * This interface adapts all the operations sent to atmosphere using the STOMP protocol. The purpose is to map existing
 * Atmosphere features to the actions that could be performed with several STOMP frames.
 * </p>
 *
 * <p>
 * Any method of this interface should be invoked when a frame has been validated. Consequently, implementation could
 * assert that all mandatory headers will be specified in parameter when declared in method signature.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.1
 */
public interface StompInterceptor extends AtmosphereInterceptor {

    /**
     * <p>
     * Inspects the {@link AtmosphereResource} with its extracted {@link Frame}.
     * </p>
     *
     * @param stompFormat the objects that generates frames when needed
     * @param framework the framework
     * @param frame the frame
     * @param r the resource
     * @throws IOException if inspection fails
     */
    Action inspect(StompFormat stompFormat, AtmosphereFramework framework, Frame frame, AtmosphereResource r) throws IOException;
}
