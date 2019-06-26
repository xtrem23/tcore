/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.utils.audio;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 02/11/2015.
 * Simple class to control and observe volume of specific stream type (phone call, music etc.).
 */
public final class VolumeController {

    @NonNull
    private final AudioManager audioManager;
    private final int maxVolume;
    @NonNull
    private final Observable<Integer> volumeObservable;
    @NonNull
    private final PublishSubject<Integer> selfVolumeChangedEvent = PublishSubject.create();

    public VolumeController(@NonNull final Context context) {
        this(context, AudioManager.STREAM_MUSIC);
    }

    public VolumeController(@NonNull final Context context, final int streamType) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(streamType);
        volumeObservable = Observable
                .fromCallable(VolumeObserver::new)
                .switchMap(volumeObserver -> selfVolumeChangedEvent
                        .mergeWith(volumeObserver.systemVolumeChangedEvent
                                .map(event -> getVolume())
                                .doOnSubscribe(disposable -> context.getContentResolver()
                                        .registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver))
                                .doOnDispose(() -> context.getContentResolver()
                                        .unregisterContentObserver(volumeObserver)))
                        .startWith(getVolume()))
                .distinctUntilChanged()
                .replay(1)
                .refCount();
    }

    /**
     * Max volume amount to set.
     *
     * @return max volume.
     */
    public int getMaxVolume() {
        return maxVolume;
    }

    /**
     * Sets volume.
     *
     * @param volume Volume value to set from 0 to {@link #getMaxVolume()}.
     */
    public void setVolume(final int volume) {
        if (volume < 0 || volume > maxVolume) {
            Lc.assertion(new ShouldNotHappenException("Volume: " + volume + " out of bounds [0," + maxVolume + ']'));
            return;
        }
        if (getVolume() != volume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            selfVolumeChangedEvent.onNext(volume);
        }
    }

    /**
     * Returns volume.
     *
     * @return Returns volume value from 0 to {@link #getMaxVolume()}.
     */
    public int getVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * Observes current volume.
     *
     * @return Observable which will provide current volume and then it's updates.
     */
    @NonNull
    public Observable<Integer> observeVolume() {
        return volumeObservable;
    }

    private static class VolumeObserver extends ContentObserver {

        @NonNull
        private final PublishSubject<Void> systemVolumeChangedEvent = PublishSubject.create();

        public VolumeObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        @Override
        public void onChange(final boolean selfChange) {
            super.onChange(selfChange);
            systemVolumeChangedEvent.onNext(null);
        }

    }

}
