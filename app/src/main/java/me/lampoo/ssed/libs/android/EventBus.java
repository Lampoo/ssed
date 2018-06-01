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
package me.lampoo.ssed.libs.android;

import me.lampoo.ssed.libs.events.EventDispatcher;

public class EventBus {
    private class EventHandler implements Runnable {
        private Object mEvent;

        public EventHandler(Object event) {
            mEvent = event;
        }

        @Override
        public void run() {
            processEvent(mEvent);
        }
    }

    // Event dispatcher
    private EventDispatcher mDispatcher;

    public EventBus() {
        mDispatcher = new EventDispatcher();
    }

    // Execute event callbacks
    private void processEvent(Object event) {
        mDispatcher.dispatch(event);
    }

    // Register subscriber
    public void register(Object subscriber) {
        mDispatcher.register(subscriber);
    }

    // Unregister subscriber
    public void unregister(Object subscriber) {
        mDispatcher.unregister(subscriber);
    }

    // Dispatch and execute callback in calling thread
    public void dispatch(Object event) {
        processEvent(event);
    }

    // Post and execute callback in given thread
    public void post(ThreadEnforcer enforcer, Object event) {
        enforcer.post(new EventHandler(event));
    }
}
