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

import ru.touchin.roboswag.components.navigation.activities.BaseActivity;


/**
 * Controller that helps to manage deep links in activity.
 * It helps to save and restore deep link and deletes deep link info from intent.
 * As tin he base class - call methods that starts with 'on' prefix from activity.
 *
 * @see #onActivityRestoreInstanceState(Bundle)
 * @see #onActivitySavedInstanceState(Bundle)
 */
public abstract class ActivityDeepLinkController<TActivity extends BaseActivity> extends DeepLinkController<TActivity> {

    private static final String DEEP_LINK_EXTRA = "DEEP_LINK_EXTRA";

    /**
     * Call this method on restore instance state -
     * in {@link Activity#onCreate(Bundle)} or in {@link Activity#onRestoreInstanceState(Bundle)}.
     *
     * @param savedInstanceState - activity's savedInstanceState.
     */
    public void onActivityRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        final String deepLinkUrl = savedInstanceState.getString(DEEP_LINK_EXTRA, null);
        onNewDeepLink(deepLinkUrl == null ? null : Uri.parse(deepLinkUrl));
    }

    /**
     * Call this method while saving stat of activity - in {@link Activity#onSaveInstanceState(Bundle)}.
     *
     * @param stateToSave - activity's stateToSave.
     */
    public void onActivitySavedInstanceState(@NonNull final Bundle stateToSave) {
        if (getDeepLinkUri() != null) {
            stateToSave.putString(DEEP_LINK_EXTRA, getDeepLinkUri().toString());
        }
    }

    /**
     * Helps to delete info about deep link from activity's intent and from this controller.
     * Call this after successful deep link processing.
     *
     * @param activity - that should delete info about processed deep link.
     */
    protected void deleteDeepLink(@NonNull final TActivity activity) {
        onNewDeepLink(null);
        activity.getIntent().setData(null);
    }

}