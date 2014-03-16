/*
 * Copyright 2013 Jeanfrancois Arcand
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


package org.atmosphere.stomp.annotation;

import org.atmosphere.annotation.Processor;
import org.atmosphere.config.AtmosphereAnnotation;
import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.stomp.StompAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * <p>
 * This processor handles classes annotated with {@link StompEndpoint}. Any annotated class should provides several
 * methods annotated with {@link StompService}. Each annotated method should point to a different
 * {@link org.atmosphere.stomp.annotation.StompService#destination()}.
 * </p>
 *
 * <p>
 * When a method is discovered, an {@link StompAtmosphereHandler} is associated to it. Moreover, this processor
 * creates a mapping for a {@link org.atmosphere.cpr.Broadcaster} which corresponds to the value returned by
 * {@link org.atmosphere.stomp.annotation.StompService#destination()}.
 * </p>
 *
 * <p>
 * By adding those new {@link org.atmosphere.cpr.AtmosphereHandler handlers} to the {@link AtmosphereFramework},
 * the {@link org.atmosphere.stomp.StompInterceptor} will be able to find the appropriate method to invoke when
 * reading the {@link org.atmosphere.stomp.protocol.Header#DESTINATION destination} in frames.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
@AtmosphereAnnotation(StompEndpoint.class)
public class StompEndpointProcessor implements Processor<Object> {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final AtmosphereFramework framework, final Class<Object> annotatedClass) {
        logger.info("Handling {}", annotatedClass.getName());
        final Object instance;

        // Try to retrieve an instance for the given class
        try {
             instance = framework.newClassInstance(Object.class, annotatedClass);
        } catch (Exception iae) {
             logger.warn("Failed to process class annotated with {}", StompEndpoint.class.getName(), iae);
            return;
        }

        // Inspect method
        for (final Method m : annotatedClass.getDeclaredMethods()) {
            logger.debug("Detecting annotation on method {}", m.getName());

            // Stomp service detected
            if (m.isAnnotationPresent(StompService.class)) {

                // The destination will be the broadcaster mapping
                final String destination = m.getAnnotation(StompService.class).destination();

                if (destination == null || destination.isEmpty()) {
                    logger.warn("The destination in {} must not be empty", StompService.class.getName(), new IllegalStateException());
                    continue;
                } else {
                    // Optional message annotation with encoders and decoders
                    final Decoder<String, Object> decoder;
                    final Encoder<Object, String> encoder;

                    if (m.isAnnotationPresent(Message.class)) {
                        try {
                            // TODO: support many encoders/decoders ?
                            final Message message = m.getAnnotation(Message.class);
                            decoder = framework.newClassInstance(Decoder.class, message.decoders()[0]);
                            encoder = framework.newClassInstance(Encoder.class, message.encoders()[0]);
                        } catch (Exception iae) {
                            logger.warn("Failed to process annotation {}", Message.class.getName(), iae);
                            continue;
                        }
                    } else {
                        decoder = null;
                        encoder = null;
                    }

                    // Now add to the framework the handler for the declared destination
                    try {
                        framework.addAtmosphereHandler(destination, new StompAtmosphereHandler(instance, m, encoder, decoder,
                                framework.getBroadcasterFactory().get(destination)));
                    } catch (IllegalArgumentException iae) {
                        logger.warn("Method {} has not the required signature to be a {}", m.getName(), iae);
                    }
                }
            }
        }
    }
}
