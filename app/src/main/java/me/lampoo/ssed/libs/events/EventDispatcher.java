/*
 * Copyright (C) 2018 Lampoo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.lampoo.ssed.libs.events;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventDispatcher {
    // List of registered subscribers
    private final ConcurrentMap<Object /* subscriber */, Map<Class<?> /* event */, Set<EventCallback> /* callback */>>
        mSubscribers = new ConcurrentHashMap<Object, Map<Class<?>, Set<EventCallback>>>();

    // List of registered callbacks
    private final ConcurrentMap<Class<?> /* event */, Set<EventCallback> /* callback */>
        mCallbacks = new ConcurrentHashMap<Class<?>, Set<EventCallback>>();

    // Invoke callbacks for event dispatching
    public void dispatch(Object event) {
        // Retrieve the registered callbacks for this event type
        Set<EventCallback> callbacks = mCallbacks.get(event.getClass());
        // Return if none
        if (callbacks == null) { return; }
        // Now invoke the callback one by one
        for (EventCallback cb : callbacks) { cb.invoke(event); }
    }

    // Register a subscriber and callbacks for interested events
    public void register(Object subscriber) {
        // Check if this subscriber is already registered or not
        Map<Class<?> /* event */, Set<EventCallback>> callbacks = mSubscribers.get(subscriber);
        // Return if yes
        if (callbacks != null) { return; }

        // Build a map of callbacks in this subscriber
        callbacks = new HashMap<Class<?> /* event */, Set<EventCallback>>();
        // Load methods
        Map<Class<?> /* event */, Set<Method>> methods = EventMethodInflater.loadMethods(subscriber);
        // Iterate the methods and fills the map
        for (Map.Entry<Class<?> /* event */, Set<Method>> e : methods.entrySet()) {
            Class<?> type = e.getKey(); /* event type */
            Set<EventCallback> cb = callbacks.get(type);
            if (cb == null) {
                cb = new HashSet<EventCallback>();
                callbacks.put(type, cb);
            }
            for (Method method : e.getValue()) {
                cb.add(new EventCallback(subscriber, method));
            }
        }

        // STEP 1: register subscriber using concurrent putIfAbsent, might
        //         fail if someone else did it already
        if (mSubscribers.putIfAbsent(subscriber, callbacks) != null) {
            return; // Already registered, return
        }

        // STEP 2: register callbacks
        for (Map.Entry<Class<?> /* event */, Set<EventCallback>> e : callbacks.entrySet()) {
            Class<?> type = e.getKey(); /* event */
            Set<EventCallback> cb = mCallbacks.get(type);
            if (cb == null) {
                // Concurrent CopyOnWriteArraySet and putIfAbsent
                Set<EventCallback> handlerCreation = new CopyOnWriteArraySet<EventCallback>();
                cb = mCallbacks.putIfAbsent(type, handlerCreation);
                if (cb == null) {
                    cb = handlerCreation;
                }
            }
            cb.addAll(e.getValue());
        }
    }

    // Unregister a subscriber and remove its registered callbacks
    public void unregister(Object subscriber) {
        // Remove subscriber
        Map<Class<?> /* event */, Set<EventCallback>> callbacks = mSubscribers.remove(subscriber);
        if (callbacks == null) {
            return; // Return if no registration of this subscriber, or removed already
        }

        // Remove callbacks
        for (Map.Entry<Class<?> /* event */, Set<EventCallback>> e : callbacks.entrySet()) {
            Class<?> type = e.getKey(); /* event */
            Set<EventCallback> cb = mCallbacks.get(type);
            if (cb == null) {
                continue;
            }
            for (EventCallback h : e.getValue()) {
                h.invalidate(); // invalidate the callback
            }
            cb.removeAll(e.getValue());
        }
    }
}
