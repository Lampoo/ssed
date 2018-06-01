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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class EventCallback {
    Object mSubscriber;
    Method mMethod;
    boolean mValid;

    public EventCallback(Object subscriber, Method method) {
        mSubscriber = subscriber;
        mMethod = method;
        mMethod.setAccessible(true);
        mValid = true;
    }

    public void invalidate() {
        mValid = false;
    }

    public void invoke(Object event) {
        if (!mValid) {
            return;
        }
        try {
            mMethod.invoke(mSubscriber, event);
        } catch (InvocationTargetException |
                 IllegalAccessException e) {
        }
    }
}
