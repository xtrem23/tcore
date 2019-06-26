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

package ru.touchin.roboswag.components.navigation;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Created by Gavriil Sitnikov on 08/10/2014.
 * Base interface to listen child fragments start.
 * Usually it helps to determine that fragment have showed on screen and we can change {@link android.app.Activity}'s navigation state for example.
 */
public interface OnFragmentStartedListener {

    /**
     * Calls by child fragment (added via {@link android.support.v4.app.FragmentManager}) on it'sstart.
     *
     * @param fragment Child fragment which called this method.
     */
    void onFragmentStarted(@NonNull Fragment fragment);

}