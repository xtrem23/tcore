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

package ru.touchin.roboswag.components.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 24/03/16.
 * Base class representing application's logic.
 * In specific application it should be child of it (usually one) which contains all methods/objects related to logic.
 * It should contains interface to work with API/preferences/database/file system/system parameters etc.
 * Be sure that all objects/instances/services created to represents logic are not getting a lot of time to be instantiated, if they take a lot time
 * for instantiation then it is wrong logic and it should be moved into asynchronous operations via {@link Observable} or so.
 * Also it shouldn't create massive data objects and a lot of objects instantly. Basically it should just create bunch of interfaces inside
 * which will allows to access to some logic methods.
 * In fact it is similar to dependency injection pattern but with full control of instantiation and only one single instance of {@link Logic} per app.
 * If you want to use it then just create getter in {@link android.app.Service}/{@link android.app.Activity}/{@link android.content.BroadcastReceiver}
 * or any else context-based elements and do not forget to store reference to {@link Logic} into field because else it will be consumed by GC.
 * Sample of {@link Logic} using is in {@link ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity}.
 * NOTE: Ideally creation of logic should be asynchronous and stored in specific {@link android.app.Service} so it should be accessed
 * asynchronously via {@link Observable} or so. But in fact it requires {@link android.app.Service} plus more complex methods to access to logic.
 * So currently it is more simple to access via simple bridge based on singletons stored into {@link WeakReference} because anyway instantiation of
 * logic have to be as fast as it can. If it's not then it is just a bug and problem of optimization.
 */
public class Logic {

    private static final Map<Class<? extends Logic>, WeakReference<Logic>> LOGIC_INSTANCES = new HashMap<>();

    /**
     * Returns instance of {@link Logic} depends on class. There should be no more than one instance per class.
     *
     * @param context    Context of application where this {@link Logic} related to;
     * @param logicClass Class of {@link Logic};
     * @param <T>        Type of class of {@link Logic};
     * @return Instance of {@link Logic}.
     */
    @SuppressWarnings({"unchecked", "PMD.SingletonClassReturningNewInstance"})
    //SingletonClassReturningNewInstance: it is OK to create instance every time if WeakReference have died
    @NonNull
    public static <T extends Logic> T getInstance(@NonNull final Context context, @NonNull final Class<T> logicClass) {
        T result;
        synchronized (LOGIC_INSTANCES) {
            final WeakReference<Logic> reference = LOGIC_INSTANCES.get(logicClass);
            result = reference != null ? (T) reference.get() : null;
            if (result == null) {
                result = constructLogic(context.getApplicationContext(), logicClass);
                LOGIC_INSTANCES.put(logicClass, new WeakReference<>(result));
            }
        }
        return result;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private static <T extends Logic> T constructLogic(@NonNull final Context context, @NonNull final Class<T> logicClass) {
        if (logicClass.getConstructors().length != 1 || logicClass.getConstructors()[0].getParameterTypes().length != 1) {
            throw new ShouldNotHappenException("There should be only one public constructor(Context) for class " + logicClass);
        }
        final Constructor<?> constructor = logicClass.getConstructors()[0];
        try {
            return (T) constructor.newInstance(context);
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

    @NonNull
    private final Context context;

    public Logic(@NonNull final Context context) {
        this.context = context;
    }

    /**
     * Returns {@link android.app.Application}'s context.
     *
     * @return Context (possibly application).
     */
    @NonNull
    public Context getContext() {
        return context;
    }

}
