/*
 * Copyright (C) 2006 The Android Open Source Project
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
package me.lampoo.ssed.activities;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import me.lampoo.ssed.R;
import me.lampoo.ssed.libs.android.EventBus;
import me.lampoo.ssed.libs.android.ThreadEnforcer;

public class MainActivity extends AppCompatActivity {

    EventBus bus = new EventBus();

    TextView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (TextView)findViewById(R.id.text);

        bus.register(this);

        bus.post(ThreadEnforcer.MAIN, "onCreate");
        bus.post(ThreadEnforcer.WORKER, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.post(ThreadEnforcer.MAIN, "onPause");
        bus.post(ThreadEnforcer.WORKER, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.post(ThreadEnforcer.MAIN, "onStop");
        bus.post(ThreadEnforcer.WORKER, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.post(ThreadEnforcer.MAIN, "onResume");
        bus.post(ThreadEnforcer.WORKER, "onResume");
    }

    public final void onEvent(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            view.setText(message);
        } else {
            Log.d("onEvent", message);
        }
    }
}
