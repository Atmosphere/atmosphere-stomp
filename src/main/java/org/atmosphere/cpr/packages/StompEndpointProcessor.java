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


package org.atmosphere.cpr.packages;

import org.atmosphere.annotation.Processor;
import org.atmosphere.config.AtmosphereAnnotation;
import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.stomp.annotation.StompEndpoint;
import org.atmosphere.stomp.annotation.StompService;
import org.atmosphere.stomp.handler.StompSendActionAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This processor handles classes annotated with {@link org.atmosphere.stomp.annotation.StompEndpoint}. Any annotated class should provides several
 * methods annotated with {@link org.atmosphere.stomp.annotation.StompService}. Each annotated method should point to a different
 * {@link org.atmosphere.stomp.annotation.StompService#destination()}.
 * </p>
 *
 * <p>
 * When a method is discovered, an {@link org.atmosphere.stomp.handler.StompSendActionAtmosphereHandler} is associated to it. Moreover, this processor
 * creates a mapping for a {@link org.atmosphere.cpr.Broadcaster} which corresponds to the value returned by
 * {@link org.atmosphere.stomp.annotation.StompService#destination()}.
 * </p>
 *
 * <p>
 * By adding those new {@link org.atmosphere.cpr.AtmosphereHandler handlers} to the {@link AtmosphereFramework},
 * the {@link org.atmosphere.stomp.interceptor.FrameInterceptor} will be able to find the appropriate method to invoke when
 * reading the {@link org.atmosphere.stomp.protocol.Header#DESTINATION destination} in frames.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
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

        Method onHeartbeatMethod = null;
        final List<Method> list = Arrays.asList(annotatedClass.getDeclaredMethods());

        // Look for heartbeat listener first
        // Only one method can be notified per class
        for (final Iterator<Method> it = list.iterator(); it.hasNext() && onHeartbeatMethod == null;) {
            onHeartbeatMethod = detectHeartbeat(it.next());
        }

        // Look for service
        for (final Method m : annotatedClass.getDeclaredMethods()) {
            // If an handler is created, then the onHeartbeatMethod will take null so only one call will be performed
            onHeartbeatMethod = detectStompService(framework, m, instance, onHeartbeatMethod);
        }
    }

    /**
     * <p>
     * Detects if the given method is annotated with {@link Heartbeat}.
     * </p>
     *
     * @param method the method to inspect
     * @return the given method if it can receive heartbeat event, {@code null} otherwise
     */
    private Method detectHeartbeat(final Method method) {
        logger.debug("Detecting heartbeat annotation on method {}", method.getName());

        // Heartbeat listener detected
        return (method.isAnnotationPresent(Heartbeat.class)) ? method : null;
    }

    /**
     * <p>
     * Detects if the given method is annotated with {@link StompService} and creates the appropriate handler.
     * </p>
     *
     * @param framework the framework instance
     * @param method the method to inspect
     * @param instance the annotated class instance
     * @param onHeartbeatMethod the heartbeat method
     */
    private Method detectStompService(final AtmosphereFramework framework,
                                      final Method method,
                                      final Object instance,
                                      final Method onHeartbeatMethod) {
        logger.debug("Detecting annotation on method {}", method.getName());

        // Stomp service detected
        if (method.isAnnotationPresent(StompService.class)) {

            // The destination will be the broadcaster mapping
            final String destination = method.getAnnotation(StompService.class).destination();

            if (destination == null || destination.isEmpty()) {
                logger.warn("The destination in {} must not be empty", StompService.class.getName(), new IllegalStateException());
            } else {
                // Optional message annotation with encoders and decoders
                final Decoder<String, Object> decoder;
                final Encoder<Object, String> encoder;

                if (method.isAnnotationPresent(Message.class)) {
                    try {
                        // TODO: support many encoders/decoders ?
                        final Message message = method.getAnnotation(Message.class);
                        decoder = framework.newClassInstance(Decoder.class, message.decoders()[0]);
                        encoder = framework.newClassInstance(Encoder.class, message.encoders()[0]);
                    } catch (Exception iae) {
                        logger.warn("Failed to process annotation {}", Message.class.getName(), iae);
                        return onHeartbeatMethod;
                    }
                } else {
                    decoder = null;
                    encoder = null;
                }

                // Now add to the framework the handler for the declared destination
                try {
                    final Broadcaster b = framework.getBroadcasterFactory().get(destination);
                    final AtmosphereHandler ah = new StompSendActionAtmosphereHandler(instance, method, encoder, decoder, b, onHeartbeatMethod);
                    framework.addAtmosphereHandler(destination, ah);

                    // we return null so only one handler will receive heartbeat
                    return null;
                } catch (IllegalArgumentException iae) {
                    logger.warn("Method {} has not the required signature to be a {}", method.getName(), iae);
                }
            }
        }

        return onHeartbeatMethod;
    }
}
