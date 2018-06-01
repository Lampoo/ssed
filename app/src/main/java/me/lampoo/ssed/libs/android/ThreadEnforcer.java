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

import android.os.Handler;
import android.os.Looper;

public interface ThreadEnforcer {
    void post(Runnable runnable);

    // MAIN THREAD CONTEXT
    ThreadEnforcer MAIN = new ThreadEnforcer() {
        Handler mHandler = new Handler(Looper.getMainLooper());

        @Override public void post(Runnable runnable) {
            mHandler.post(runnable);
        }
    };

    // WORKER THREAD CONTEXT
    ThreadEnforcer WORKER = new ThreadEnforcer() {
        Handler mHandler = new Handler(new ThreadHandler().getLooper());

        @Override public void post(Runnable runnable) {
            mHandler.post(runnable);
        }
    };
}
