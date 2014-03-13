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


package org.atmosphere.stomp.test;

import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.stomp.annotation.StompService;

import java.util.Date;

/**
 * <p>
 * An basic annotated service class that use stomp support.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
@ManagedService
public class StompBusinessService {

    /**
     * <p>
     * A basic DTO for test purpose using the {@link Message} annotation.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 2.2
     * @version 1.0
     */
    public static final class BusinessDto {

        /**
         * The date when message was written.
         */
        private Long timestamp;

        /**
         * The message sent.
         */
        private String message;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param timestamp the date when message was written
         * @param message the message
         */
        public BusinessDto(final Long timestamp, final String message) {
            this.timestamp = timestamp;
            this.message = message;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return new Date(timestamp).toString() + ": " + message;
        }
    }

    /**
     * <p>
     * Encoder that converts a {@link BusinessDto} into its {@code String} representation.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 2.2
     * @version 1.0
     */
    public static final class BusinessDtoEncoder implements Encoder<BusinessDto, String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(final BusinessDto s) {
            return s.toString();
        }
    }


    /**
     * <p>
     * Decoders that build a {@link BusinessDto} from its {@code String} representation.
     * </p>
     *
     * @author Guillaume DROUET
     * @since 2.2
     * @version 1.0
     */
    public static final class BusinessDtoDecoder implements Decoder<String, BusinessDto> {

        /**
         * {@inheritDoc}
         */
        @Override
        public BusinessDto decode(final String s) {
            final int limit = s.indexOf('|');
            return new BusinessDto(Long.parseLong(s.substring(0, limit)), s.substring(limit));
        }
    }

    /**
     * <p>
     * Invoked when a {@link org.atmosphere.stomp.protocol.Frame#SEND} is sent to atmosphere.
     * Just uses the given {@link Broadcaster} to send a message containing the {@link AtmosphereResource#uuid()}
     * of the specified {@link AtmosphereResource}.
     * </p>
     *
     * @param b the broadcaster associated to the path specified in frame headers
     * @param r the atmosphere resource associated to the client connection which sent the message
     */
    @StompService(destination = "Hello World!")
    public void sayHello(final Broadcaster b, final AtmosphereResource r) {
        b.broadcast(r.uuid() + " says Hello World!");
    }

    /**
     * <p>
     * Invoked when a {@link org.atmosphere.stomp.protocol.Frame#SEND} is sent to atmosphere.
     * Just returns a message containing the {@link AtmosphereResource#uuid()} of the specified {@link AtmosphereResource}.
     * The value will be dispatched by the {@link Broadcaster} associated to the path specified in frame headers
     * transparently.
     * </p>
     *
     * @param r the atmosphere resource associated to the client connection which sent the message
     * @return the message to send by the broadcaster associated to the path specified in frame headers
     */
    @StompService(destination = "Hello World2!")
    public String sayHello2(final AtmosphereResource r) {
        return r.uuid() + " says Hello World!";
    }

    /**
     * <p>
     * Invoked when a {@link org.atmosphere.stomp.protocol.Frame#SEND} is sent to atmosphere.
     * Just returns the specified parameter. The value will be dispatched by the {@link Broadcaster}
     * associated to the path specified in frame headers transparently.
     * </p>
     *
     * <p>
     * Since the parameter is not a {@code String}, the method provides the {@link Encoder} and {@link Decoder} to solve
     * conversion issue.
     * </p>
     *
     * @param dto the complex object
     * @return the dto to send by the broadcaster associated to the path specified in frame headers
     */
    @StompService(destination = "Hello World3!")
    @Message(encoders = { BusinessDtoEncoder.class }, decoders = {BusinessDtoDecoder.class })
    public BusinessDto sayHello3(final BusinessDto dto) {
        return dto;
    }
}
