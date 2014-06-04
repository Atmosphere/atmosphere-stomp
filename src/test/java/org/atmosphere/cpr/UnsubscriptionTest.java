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


package org.atmosphere.cpr;

import org.atmosphere.stomp.test.StompBusinessService;
import org.testng.annotations.Test;

/**
 * <p>
 * Dedicated test class for unsubscription.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.2
 * @version 1.0
 */
public class UnsubscriptionTest extends StompTest {

    /**
     * <p>
     * Tests when message are not received according to unsubscription operations.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test(priority = -1)
    public void unsubscriptionTest() throws Exception {
        final AtmosphereResponse response = newResponse();
        final String destination = StompBusinessService.DESTINATION_HELLO_WORLD2;

        // Subscribe...
        action = org.atmosphere.stomp.protocol.Action.SUBSCRIBE;
        AtmosphereResource ar = newAtmosphereResource(destination, newRequest(destination), response, true);
        processor.service(ar.getRequest(), response);

        // ... then unsubscribe...
        action = org.atmosphere.stomp.protocol.Action.UNSUBSCRIBE;
        ar = newAtmosphereResource(destination, newRequest(destination), response, true);
        processor.service(ar.getRequest(), response);

        // ... finally we should not receive message
        action = org.atmosphere.stomp.protocol.Action.SEND;
        ar = newAtmosphereResource(destination, newRequest(destination), response, true);
        runMessage("null", destination, ar.getRequest(), response, false);
    }
}
