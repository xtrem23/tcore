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

package ru.touchin.roboswag.components.deeplinks;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.components.navigation.activities.BaseActivity;

/**
 * Created by Ilia Kurtov on 04.08.2015.
 * Controller for deep links. Its main goal to decide when deep link should be processed.
 * Call methods that starts with 'on' prefix from {@link TActivity} that should process deep links.
 *
 * @param <TActivity> Type of Activity to process deep links.
 * @see #onNewDeepLink(Uri)
 * @see #onActivityReadyToProcessDeepLink(BaseActivity)
 * @see #onActivityStopBeingReady()
 */

public abstract class DeepLinkController<TActivity extends BaseActivity> {

    @Nullable
    private Uri deepLinkUri;
    @Nullable
    private TActivity activity;
    private boolean allowDeepLinkToProcess = true;

    /**
     * Get current deep link.
     *
     * @return - current deep link
     */
    @Nullable
    protected Uri getDeepLinkUri() {
        return deepLinkUri;
    }

    /**
     * Call this method after receiving new deep link {@link Uri} from your activity.
     * It saves new deepLinkUri and tries to process deep link if possible.
     * In most common cases call this method in {@link Activity#onCreate(Bundle)}
     * if bundle == null or if you want to restore deep link
     * in {@link Activity#onCreate(Bundle)} or in {@link Activity#onRestoreInstanceState(Bundle)}
     * methods.
     *
     * @param deepLinkUri - received deep link.
     */
    public void onNewDeepLink(@Nullable final Uri deepLinkUri) {
        this.deepLinkUri = deepLinkUri;
        startToProcessDeepLinkIfPossible();
    }

    /**
     * Call this method when your activity should be ready to process deep link.
     * In most common cases call this method on {@link Activity#onStart()}
     *
     * @param activity - that should be able to process deep link.
     */
    public void onActivityReadyToProcessDeepLink(@NonNull final TActivity activity) {
        this.activity = activity;
        startToProcessDeepLinkIfPossible();
    }

    /**
     * Call this method when your activity stopped being ready to process deep link.
     * In most common cases call this method on {@link Activity#onStop()}
     */
    public void onActivityStopBeingReady() {
        activity = null;
    }

    /**
     * This method should be called when you need to add additional condition
     * for processing deep links. By default {@link #allowDeepLinkToProcess}
     * equals true.
     *
     * @param allowDeepLinkToProcess - pass true here if you want to allow deep
     *                               link to process, otherwise - pass false.
     */
    public void setAllowDeepLinkToProcess(final boolean allowDeepLinkToProcess) {
        this.allowDeepLinkToProcess = allowDeepLinkToProcess;
        startToProcessDeepLinkIfPossible();
    }

    private void startToProcessDeepLinkIfPossible() {
        if (activity != null && deepLinkUri != null && allowDeepLinkToProcess) {
            processDeepLink(activity, deepLinkUri);
        }
    }

    /**
     * This method would be called if there are non null {@link TActivity},
     * non null {@link #deepLinkUri} and {@link #allowDeepLinkToProcess} equals true.
     * Don't forget to call activity.getIntent().setData(null) after deep link processing
     *
     * @param activity    - that should be able to process deep link.
     * @param deepLinkUri - received deep link.
     */
    protected abstract void processDeepLink(@NonNull final TActivity activity,
                                            @NonNull final Uri deepLinkUri);

}