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


package org.atmosphere.stomp.handler;

import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.annotation.StompEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>
 * This handler wraps the method to be invoked when the {@link org.atmosphere.stomp.protocol.Action#SEND send} action is performed
 * with a STOMP frame. The frame indicates the particular {@link org.atmosphere.stomp.protocol.Header#DESTINATION destination}
 * which is mapped to the appropriate annotated method.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class StompSendActionAtmosphereHandler extends AbstractReflectorAtmosphereHandler {

    /**
     * Method signature requirement message.
     */
    private static final String IAE_MESSAGE = String.format(
            "Method can expects as parameter '%s', '%s'. Otherwise it must provides decoders/encoders through the '%s' annotation",
            AtmosphereResource.class.getName(),
            String.class,
            Message.class.getName());

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The object which provides the method.
     */
    private final Object toProxy;

    /**
     * The method to invoke.
     */
    private final Method method;

    /**
     * Provide each parameter required to invoke the method.
     */
    private final ParamProvider[] paramProviders;

    /**
     * Optional encoder that converts {@code String} to expected parameter type.
     */
    private final Encoder<Object, String> encoder;

    /**
     * The broadcaster associated to this handler.
     */
    private final Broadcaster broadcaster;

    /**
     * <p>
     * Creates a new instance.
     * </p>
     *
     * @param toProxy the object to proxy
     * @param method the method to invoke on proxy object
     * @param encoder encodes into expected parameter type
     * @param decoder converts returned type into {@code String} wrapped in text frame
     * @param broadcaster the broadcaster associated to the destination declared in the annotated method
     */
    public StompSendActionAtmosphereHandler(final Object toProxy,
                                            final Method method,
                                            final Encoder<Object, String> encoder,
                                            final Decoder<String, ?> decoder,
                                            final Broadcaster broadcaster) {
        this.toProxy = toProxy;
        this.method = method;
        this.encoder = encoder;
        this.broadcaster = broadcaster;

        // Detect appropriate provider for each parameter type
        final Class<?>[] paramTypes = method.getParameterTypes();
        paramProviders = new ParamProvider[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            final Class<?> paramType = paramTypes[i];

            // The atmosphere resource is just the one that sent the message
            if (paramType.isAssignableFrom(AtmosphereResource.class)) {
                paramProviders[i] = new ParamProvider() {
                    @Override
                    public Object getParam(final AtmosphereResource atmosphereResource) {
                        return atmosphereResource;
                    }
                };
            } else if (paramType.isAssignableFrom(Broadcaster.class)) {
                paramProviders[i] = new ParamProvider() {
                    @Override
                    public Object getParam(final AtmosphereResource atmosphereResource) {
                        return broadcaster;
                    }
                };
            // The string will be the raw message body
            } else if (paramType.isAssignableFrom(String.class)) {
                paramProviders[i] = new ParamProvider() {
                    @Override
                    public Object getParam(final AtmosphereResource atmosphereResource) {
                        return atmosphereResource.getRequest().getAttribute(StompInterceptor.STOMP_MESSAGE_BODY);
                    }
                };
            // Otherwise we use the decoder to compute the appropriate parameter type
            } else if (decoder != null) {
                paramProviders[i] = new ParamProvider() {
                    @Override
                    public Object getParam(final AtmosphereResource atmosphereResource) {
                        return decoder.decode(atmosphereResource.getRequest().getAttribute(StompInterceptor.STOMP_MESSAGE_BODY).toString());
                    }
                };
            // No decoder provided, we don't know how to convert raw string into expected parameter type
            } else {
                throw new IllegalArgumentException(IAE_MESSAGE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequest(final AtmosphereResource atmosphereResource) throws IOException {
        try {
            // Compute parameters
            final Object[] params = new Object[paramProviders.length];

            for (int i = 0; i < params.length; i++) {
                params[i] = paramProviders[i].getParam(atmosphereResource);
            }

            // Invoke stomp service
            final Object retval = method.invoke(toProxy, params);

            if (retval != null) {
                // TODO: wrap to frame message
                broadcaster.broadcast(encoder == null ? retval : encoder.encode(retval));
            } else {
                // TODO: ack?
            }
        } catch (IllegalAccessException iae) {
            logger.warn("Failed to process class annotated {}", StompEndpoint.class.getName(), iae);
        } catch (InvocationTargetException ite) {
            // TODO send error frame
        }
    }

    /**
     * <p>
     * Provides a parameter of expected type from the given {@link AtmosphereResource}.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 0.1

     * @version 1.0
     */
    private interface ParamProvider {

        /**
         * <p>
         * Gets the parameter.
         * </p>
         *
         * @param atmosphereResource the request resource
         * @return the object of expected type
         */
        Object getParam(AtmosphereResource atmosphereResource);
    }
}
