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
import android.support.annotation.Nullable;

import ru.touchin.roboswag.components.navigation.AbstractState;
import ru.touchin.roboswag.components.navigation.ViewController;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * Simple {@link ViewControllerFragment} which is using by {@link ru.touchin.roboswag.components.navigation.ViewControllerNavigation}.
 *
 * @param <TState>    Type of object which is representing it's fragment state;
 * @param <TActivity> Type of {@link ViewControllerActivity} where fragment could be attached to.
 */
public class SimpleViewControllerFragment<TState extends AbstractState, TActivity extends ViewControllerActivity<?>>
        extends ViewControllerFragment<TState, TActivity> {

    private static final String VIEW_CONTROLLER_CLASS_EXTRA = "VIEW_CONTROLLER_CLASS_EXTRA";

    /**
     * Creates {@link Bundle} which will store state and {@link ViewController}'s class.
     *
     * @param viewControllerClass Class of {@link ViewController} which will be instantiated inside this fragment;
     * @param state               State to use into {@link ViewController};
     * @return Returns {@link Bundle} with state inside.
     */
    @NonNull
    public static Bundle createState(@NonNull final Class<? extends ViewController> viewControllerClass,
                                     @NonNull final AbstractState state) {
        final Bundle result = createState(state);
        result.putSerializable(VIEW_CONTROLLER_CLASS_EXTRA, viewControllerClass);
        return result;
    }

    private Class<? extends ViewController<TActivity,
            ? extends ViewControllerFragment<TState, TActivity>>> viewControllerClass;

    @NonNull
    @Override
    public Class<? extends ViewController<TActivity,
            ? extends ViewControllerFragment<TState, TActivity>>> getViewControllerClass() {
        return viewControllerClass;
    }

    @Override
    protected boolean isStateRequired() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewControllerClass = (Class<? extends ViewController<TActivity,
                ? extends ViewControllerFragment<TState, TActivity>>>) getArguments().getSerializable(VIEW_CONTROLLER_CLASS_EXTRA);
    }

    protected static class DefaultState extends AbstractState {
        // just default implementation
    }

}
