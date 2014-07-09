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


package org.atmosphere.stomp.interceptor;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.stomp.handler.HandlerHelper;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.Subscriptions;
import org.atmosphere.stomp.protocol.Frame;

import java.io.IOException;


/**
 * <p>
 * Invokes the method annotated with {@link org.atmosphere.stomp.annotation.StompService} inside any
 * {@link org.atmosphere.config.service.ManagedService} that defines a destination that matched the destination
 * String inside the specified {@link String Strings}.
 * </p>
 *
 * <p>
 * The result of the method invocation is dispatched by the {@link org.atmosphere.cpr.Broadcaster} identified with
 * the path specified in the given {@link String Strings}. If no annotated method matches the requested send,
 * then the body is dispatched. Original body or result are sent in a {@link org.atmosphere.stomp.protocol.Action#MESSAGE}
 * frame. Finally, if the method invocation throws an exception, then an {@link org.atmosphere.stomp.protocol.Action#ERROR}
 * is sent only to the specified {@link AtmosphereResource} that sent the frame.
 * </p>
 *
 * <p>
 * Note that {@link org.atmosphere.cpr.Broadcaster} must not do anything if a transaction has been started by the
 * {@link AtmosphereResource} and if this transaction is referenced in frame's Strings.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.2
 */
public class SendInterceptor extends AtmosphereInterceptorAdapter implements StompInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final AtmosphereFramework framework, final Frame frame, final FrameInterceptor.StompAtmosphereResource r)
            throws IOException {
        final AtmosphereResource resource = r.getResource();
        final Action retval = inspect(resource);

        HandlerHelper.INSTANCE.callHandler(resource, frame.getHeaders(), framework, false, new HandlerHelper.Procedure() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void apply(final Subscriptions subscriptions, final String destination, final AtmosphereFramework.AtmosphereHandlerWrapper handler) throws IOException {
                final String body = frame.getBody();

                // TODO: atmosphereResource.getRequest() may throw an IllegalStateException
                resource.getRequest().setAttribute(FrameInterceptor.STOMP_MESSAGE_BODY,
                        body != null && body.endsWith("\n") ? body.substring(0, body.length() - 1) : body);
                handler.atmosphereHandler.onRequest(resource);
            }
        });

        return retval;
    }
}
