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

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.Serializable;

/**
 * Created by Ilia Kurtov on 13/04/2016.
 * Basic state of {@link ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment}.
 * This object is saving as serializable in {@link android.os.Bundle} at {@link Fragment#onSaveInstanceState(Bundle)} point.
 * Also this object is passing into {@link Fragment#getArguments()} on fragment instantiation.
 * Do NOT store such object in fields outside of it's {@link ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment}:
 * 1) it should be used as state of fragment but not state of other fragments or parts of logic;
 * 2) if you want to modify such object then you should pass it's fragment as {@link Fragment#getTargetFragment()};
 * 3) if you are using {@link ViewControllerNavigation} then just use ***ForResult methods to pass target;
 * 4) as it is serializable object then all initialization logic (like binding) should NOT be in constructor. Use {@link #onCreate()} method.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
//AbstractClassWithoutAbstractMethod: objects of this class actually shouldn't exist
public abstract class AbstractState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Calls right after construction. All inner object's instantiation logic should be in this method.
     * Do NOT do some instantiation logic in constructor except fields setup.
     */
    public void onCreate() {
        // do nothing
    }

}
