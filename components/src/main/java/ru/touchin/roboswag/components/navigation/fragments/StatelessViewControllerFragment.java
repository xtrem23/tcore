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

import android.os.Bundle;
import android.support.annotation.NonNull;

import ru.touchin.roboswag.components.navigation.AbstractState;
import ru.touchin.roboswag.components.navigation.ViewController;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Gavriil Sitnikov on 12/03/2016.
 * Simple {@link ViewControllerFragment} with no state which is using by {@link ru.touchin.roboswag.components.navigation.ViewControllerNavigation}.
 *
 * @param <TActivity> Type of {@link ViewControllerActivity} where fragment could be attached to.
 */
@SuppressWarnings("PMD.UseUtilityClass")
//UseUtilityClass: PMD bug
public class StatelessViewControllerFragment<TActivity extends ViewControllerActivity<?>>
        extends SimpleViewControllerFragment<AbstractState, TActivity> {

    /**
     * Creates {@link Bundle} which will store state and {@link ViewController}'s class.
     *
     * @param viewControllerClass Class of {@link ViewController} which will be instantiated inside this fragment;
     * @return Returns {@link Bundle} with state inside.
     */
    @NonNull
    public static Bundle createState(@NonNull final Class<? extends ViewController> viewControllerClass) {
        return createState(viewControllerClass, new DefaultState());
    }

    @NonNull
    @Override
    public AbstractState getState() {
        Lc.assertion("Trying to access to state of stateless fragment of " + getViewControllerClass());
        return super.getState();
    }

    @Override
    protected boolean isStateRequired() {
        return false;
    }

}
