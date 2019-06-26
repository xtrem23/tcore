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

import ru.touchin.roboswag.components.navigation.activities.BaseActivity;

/**
 * Created by Ilia Kurtov on 04.08.2015.
 * Class that helps to operate with deep links.
 *
 * @param <TActivity> Type of Activity to process deep links.
 */
public interface DeepLink<TActivity extends BaseActivity> {

    /**
     * Called by deep link to provide unique name.
     */
    @NonNull
    String getDeepLinkName();

    /**
     * Called by deep link to decide - whenever deep link should process uri or if we are already on that screen that deep link links to.
     */
    boolean isOnSuchScreen(@NonNull TActivity activity, @NonNull Uri deepLinkUri);

    /**
     * Called by deep link to navigate to the specific screen.
     */
    void navigateTo(@NonNull TActivity activity, @NonNull Uri deepLinkUri);

}