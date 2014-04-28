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

import org.atmosphere.cpr.AtmosphereResourceSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * An {@link org.atmosphere.cpr.AtmosphereResource} can subscribes to many destination. The STOMP protocol allow to
 * subscribe many times to the same destination. This class can provide subscriptions to a destination or the destination
 * for a subscription ID.
 * </p>
 *
 * @author Guillaume DROUET
 * @since 0.1
 * @version 1.0
 */
public class Subscriptions {

    /**
     * The key used to store subscriptions in {@link org.atmosphere.cpr.AtmosphereResourceSession}.
     */
    private static final String ATTRIBUTE_KEY = Subscriptions.class.getName() + ".key";

    /**
     * <p>
     * Gets a {@link Subscriptions} object from the given session. If no subscription is bound to the session, a new one
     * is created and then returned.
     * </p>
     *
     * @param session the {@link AtmosphereResourceSession session}
     * @return the subscriptions
     */
    public static Subscriptions getFromSession(final AtmosphereResourceSession session) {
        Object retval = session.getAttribute(ATTRIBUTE_KEY);

        if (retval == null) {
            retval = new Subscriptions();
            session.setAttribute(ATTRIBUTE_KEY, retval);
        }

        return Subscriptions.class.cast(retval);
    }

    /**
     * A subscription associate an unique ID for the {@link org.atmosphere.cpr.AtmosphereResource} with a destination.
     *
     * @author Guillaume DROUET
     * @since 0.1

     * @version 1.0
     */
    class Subscription {

        /**
         * The ID.
         */
        private String id;

        /**
         * The destination.
         */
        private String destination;

        /**
         * <p>
         * Builds a new instance.
         * </p>
         *
         * @param id the id
         * @param destination the destination
         */
        Subscription(final String id, final String destination) {
            this.destination = destination;
            this.id = id;
        }

        /**
         * <p>
         * Gets the subscription ID.
         * </p>
         *
         * @return the ID
         */
        String getId() {
            return id;
        }

        /**
         * <p>
         * Gets the destination.
         * </p>
         *
         * @return the destination
         */
        String getDestination() {
            return destination;
        }
    }

    /**
     * All the subscriptions.
     */
    private List<Subscription> subscriptionList;

    /**
     * <p>
     * Builds a new instance.
     * </p>
     */
    public Subscriptions() {
        subscriptionList = new ArrayList<Subscription>();
    }

    /**
     * <p>
     * Gets all the destinations this client has subscribed to.
     * </p>
     *
     * @return the destinations set
     */
    public Set<String> getAllDestinations() {
        final Set<String> destinations = new HashSet<String>();

        for (final Subscription s : subscriptionList) {
            destinations.add(s.getDestination());
        }

        return destinations;
    }

    /**
     * <p>
     * Adds a subscription to the set of subscriptions.
     * </p>
     *
     * @param id the subscription ID
     * @param destination the subscribed destination
     */
    public void addSubscription(final String id, final String destination) {
        subscriptionList.add(new Subscription(id, destination));
    }

    /**
     * <p>
     * Gets the subscriptions for the given destination.
     * </p>
     *
     * @param destination the destination
     * @return all the IDs mapped to the destination
     */
    public List<String> getSubscriptionsForDestination(final String destination) {
        final List<String> retval = new ArrayList<String>();

        for (final Subscription s : subscriptionList) {
            if (s.getDestination().equals(destination)) {
                retval.add(s.getId());
            }
        }

        return retval;
    }

    /**
     * <p>
     * Gets the destination for the given subscription ID.
     * </p>
     *
     * @param id the ID
     * @return the destination the ID subscribes to
     */
    public String getDestinationForId(final String id) {
        for (final Subscription s : subscriptionList) {
            if (s.getId().equals(id)) {
                return s.getDestination();
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * <p>
     * Removes the subscription identified by the given ID.
     * </p>
     *
     * @param id the Id
     */
    public void removeSubscription(final String id) {
        for (final Iterator<Subscription> it = subscriptionList.iterator(); it.hasNext();) {
            final Subscription s = it.next();

            if (s.getId().equals(id)) {
                subscriptionList.remove(s);
                return;
            }
        }
    }
}
