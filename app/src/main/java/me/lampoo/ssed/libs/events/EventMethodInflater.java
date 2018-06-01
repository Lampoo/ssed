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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// A valid event callback function must follow below declaration in a class:
//   public final void onEventNNNN(EventClass event)
class EventMethodInflater {
    private static final String METHOD_PREFIX = "onEvent";

    private static final ConcurrentMap<Class<?>, Map<Class<?>, Set<Method>>> METHOD_INFLATERS =
            new ConcurrentHashMap<Class<?>, Map<Class<?>, Set<Method>>>();

    public static Map<Class<?>, Set<Method>> loadMethods(Object object) {
        return loadMethods(object.getClass());
    }

    public static Map<Class<?>, Set<Method>> loadMethods(Class<?> clazz) {
        Map<Class<?>, Set<Method>> eventHandler = METHOD_INFLATERS.get(clazz);

        if (eventHandler == null) {
            eventHandler = new HashMap<Class<?>, Set<Method>>();
        }

        if (eventHandler.isEmpty()) {
            Method clazzMethods[] = clazz.getDeclaredMethods();
            for (Method method : clazzMethods) {
                // The compiler sometimes creates synthetic bridge methods as part of the
                // type erasure process. As of JDK8 these methods now include the same
                // annotations as the original declarations. They should be ignored for
                // subscribe/produce.
                if (method.isBridge()) {
                    continue;
                }

                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers) ||
                    !Modifier.isFinal(modifiers)) {
                    continue;
                }

                if (!method.getReturnType().equals(Void.TYPE) ||
                    !method.getName().startsWith(METHOD_PREFIX)) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes == null ||
                    parameterTypes.length != 1 ||
                    parameterTypes[0].isInterface()) {
                    continue;
                }

                Set<Method> eventMethods = eventHandler.get(parameterTypes[0]);
                if (eventMethods == null) {
                    eventMethods = new HashSet<Method>();
                    eventHandler.put(parameterTypes[0], eventMethods);
                }

                eventMethods.add(method);
            } // for(Method method : clazzMethods)

            METHOD_INFLATERS.putIfAbsent(clazz, eventHandler);
        }

        return eventHandler;
    }
}
