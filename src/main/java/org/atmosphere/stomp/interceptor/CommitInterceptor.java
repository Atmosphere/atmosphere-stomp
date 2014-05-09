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
import org.atmosphere.stomp.StompInterceptor;
import org.atmosphere.stomp.protocol.Frame;
import org.atmosphere.stomp.protocol.StompFormat;

/**
 * <p>
 * Commits a transaction started by the given {@link org.atmosphere.cpr.AtmosphereResource}.
 * </p>
 *
 * <p>
 * All {@link SendInterceptor} operation results associated to the transaction ID specified in Strings are dispatched by
 * the {@link org.atmosphere.cpr.Broadcaster}.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.2
 */
public class CommitInterceptor extends AtmosphereInterceptorAdapter implements StompInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public Action inspect(final StompFormat stompFormat, final AtmosphereFramework framework, final Frame frame, final AtmosphereResource r)  {
        final Action retval = inspect(r);

        return retval;
    }
}
