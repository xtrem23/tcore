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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.components.navigation.activities.BaseActivity;

/**
 * Created by Ilia Kurtov on 04.08.2015.
 * Simple DeepLinkController that process deep links as it is. When deep links received it would have been processing and navigating id should.
 */
public abstract class SimpleActivityDeepLinkController<TActivity extends BaseActivity, TDeepLink extends DeepLink<TActivity>>
        extends ActivityDeepLinkController<TActivity> {

    @Override
    protected void processDeepLink(@NonNull final TActivity activity, @NonNull final Uri deepLinkUri) {
        deleteDeepLink(activity);
        final TDeepLink deepLink = getDeepLinkByUri(deepLinkUri);
        if (deepLink != null && !deepLink.isOnSuchScreen(activity, deepLinkUri)) {
            deleteDeepLink(activity);
            deepLink.navigateTo(activity, deepLinkUri);
        }
    }

    /**
     * Returns deep link that extending {@link DeepLink}.
     */
    @Nullable
    protected abstract TDeepLink getDeepLinkByUri(@NonNull final Uri deepLinkUri);

}