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

/**
 * <p>
 * This class represents a base exception for any frame parsing issue.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 2.2
 * @version 1.0
 */
public class ParseException extends Exception {

    /**
     * <p>
     * Builds a new exception with an origin.
     * </p>
     *
     * @param origin the origin
     */
    public ParseException(final Exception origin) {
        super(origin);
    }

    /**
     * <p>
     * Builds a new exception.
     * </p>
     */
    public ParseException() {
    }
}
