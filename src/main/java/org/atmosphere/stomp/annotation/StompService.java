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


package org.atmosphere.stomp.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * This annotation, when used on a method, indicates which invocation should be done for a particular
 * {@link org.atmosphere.stomp.protocol.Frame#SEND send}.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StompService {

    /**
     * <p>
     * Gets the destination that matches the destination header send in the {@link org.atmosphere.stomp.protocol.Frame}
     * to determine if the annotated method is related to it and should be invoked or not.
     * </p>
     *
     * @return the value that matches the header value
     */
    String destination();
}
