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

package ru.touchin.roboswag.components.navigation.fragments;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.components.navigation.AbstractState;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 11/04/2016.
 * Simple {@link ViewControllerFragment} with attached {@link #getTargetFragment()}
 * which is using by {@link ru.touchin.roboswag.components.navigation.ViewControllerNavigation}.
 *
 * @param <TState>    Type of object which is representing it's fragment state;
 * @param <TActivity> Type of {@link ViewControllerActivity} where fragment could be attached to.
 */
public class TargetedViewControllerFragment<TState extends AbstractState,
        TTargetState extends AbstractState,
        TActivity extends ViewControllerActivity<?>>
        extends SimpleViewControllerFragment<TState, TActivity> {

    /**
     * Returns specific {@link ViewControllerFragment} which is attached to this fragment as {@link #getTargetFragment()}.
     *
     * @return Target fragment.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public ViewControllerFragment<TTargetState, TActivity> getTarget() {
        if (!(getTargetFragment() instanceof ViewControllerFragment)) {
            throw new ShouldNotHappenException();
        }
        return (ViewControllerFragment<TTargetState, TActivity>) getTargetFragment();
    }

}
