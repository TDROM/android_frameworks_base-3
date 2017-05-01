/**
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.server.radio;

import android.annotation.NonNull;
import android.hardware.radio.ITuner;
import android.hardware.radio.ITunerCallback;
import android.hardware.radio.RadioManager;
import android.util.Slog;

class Tuner extends ITuner.Stub {
    // TODO(b/36863239): rename to RadioService.Tuner when native service goes away
    private static final String TAG = "RadioServiceJava.Tuner";

    /**
     * This field is used by native code, do not access or modify.
     */
    private final long mNativeContext;

    private final Object mLock = new Object();
    private int mRegion;  // TODO(b/36863239): find better solution to manage regions

    Tuner(@NonNull ITunerCallback clientCallback, int region) {
        mRegion = region;
        mNativeContext = nativeInit(clientCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        nativeFinalize(mNativeContext);
        super.finalize();
    }

    private native long nativeInit(@NonNull ITunerCallback clientCallback);
    private native void nativeFinalize(long nativeContext);
    private native void nativeClose(long nativeContext);

    private native void nativeSetConfiguration(long nativeContext,
            @NonNull RadioManager.BandConfig config);
    private native RadioManager.BandConfig nativeGetConfiguration(long nativeContext, int region);

    @Override
    public void close() {
        synchronized (mLock) {
            nativeClose(mNativeContext);
        }
    }

    @Override
    public void setConfiguration(RadioManager.BandConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("The argument must not be a null pointer");
        }
        synchronized (mLock) {
            nativeSetConfiguration(mNativeContext, config);
            mRegion = config.getRegion();
        }
    }

    @Override
    public RadioManager.BandConfig getConfiguration() {
        synchronized (mLock) {
            return nativeGetConfiguration(mNativeContext, mRegion);
        }
    }

    @Override
    public int getProgramInformation(RadioManager.ProgramInfo[] infoOut) {
        if (infoOut == null || infoOut.length != 1) {
            throw new IllegalArgumentException("The argument must be an array of length 1");
        }
        Slog.d(TAG, "getProgramInformation()");
        return RadioManager.STATUS_INVALID_OPERATION;
    }
}
