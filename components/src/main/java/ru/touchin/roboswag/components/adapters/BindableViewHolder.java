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

package ru.touchin.roboswag.components.adapters;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 12/8/2016.
 * ViewHolder that implements {@link LifecycleBindable} and uses parent bindable object as bridge (Activity, ViewController etc.).
 */
@SuppressWarnings("PMD.TooManyMethods")
public class BindableViewHolder extends RecyclerView.ViewHolder implements LifecycleBindable {

    @NonNull
    private final LifecycleBindable baseLifecycleBindable;

    public BindableViewHolder(@NonNull final LifecycleBindable baseLifecycleBindable, @NonNull final View itemView) {
        super(itemView);
        this.baseLifecycleBindable = baseLifecycleBindable;
    }

    /**
     * Look for a child view with the given id.  If this view has the given id, return this view.
     *
     * @param id The id to search for;
     * @return The view that has the given id in the hierarchy.
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(@IdRes final int id) {
        final T viewById = (T) itemView.findViewById(id);
        if (viewById == null) {
            throw new ShouldNotHappenException("No view for id=" + itemView.getResources().getResourceName(id));
        }
        return viewById;
    }

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param resId The resource id to search for data;
     * @return String The string data associated with the resource.
     */
    @NonNull
    public String getString(@StringRes final int resId) {
        return itemView.getResources().getString(resId);
    }

    /**
     * Return the string value associated with a particular resource ID.  It
     * will be stripped of any styled text information.
     *
     * @param resId      The resource id to search for data;
     * @param formatArgs The format arguments that will be used for substitution.
     * @return String The string data associated with the resource.
     */
    @NonNull
    public String getString(@StringRes final int resId, @Nullable final Object... formatArgs) {
        return itemView.getResources().getString(resId, formatArgs);
    }

    /**
     * Return the color value associated with a particular resource ID.
     * Starting in {@link android.os.Build.VERSION_CODES#M}, the returned
     * color will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return int A single color value in the form 0xAARRGGBB.
     */
    @ColorInt
    public int getColor(@ColorRes final int resId) {
        return ContextCompat.getColor(itemView.getContext(), resId);
    }

    /**
     * Returns a drawable object associated with a particular resource ID.
     * Starting in {@link android.os.Build.VERSION_CODES#LOLLIPOP}, the
     * returned drawable will be styled for the specified Context's theme.
     *
     * @param resId The resource id to search for data;
     * @return Drawable An object that can be used to draw this resource.
     */
    @NonNull
    public Drawable getDrawable(@DrawableRes final int resId) {
        return ContextCompat.getDrawable(itemView.getContext(), resId);
    }

    @SuppressWarnings("CPD-START")
    //CPD: it's ok as it's LifecycleBindable
    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilStop(observable);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable, @NonNull final Consumer<T> onNextAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable,
                                    @NonNull final Consumer<T> onNextAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable,
                                    @NonNull final Consumer<T> onNextAction,
                                    @NonNull final Consumer<Throwable> onErrorAction,
                                    @NonNull final Action onCompletedAction) {
        return baseLifecycleBindable.untilStop(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single) {
        return baseLifecycleBindable.untilStop(single);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        return baseLifecycleBindable.untilStop(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single,
                                    @NonNull final Consumer<T> onSuccessAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable) {
        return baseLifecycleBindable.untilStop(completable);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable, @NonNull final Action onCompletedAction) {
        return baseLifecycleBindable.untilStop(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable,
                                @NonNull final Action onCompletedAction,
                                @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(completable, onCompletedAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Maybe<T> maybe) {
        return baseLifecycleBindable.untilStop(maybe);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Maybe<T> maybe, @NonNull final Consumer<T> onSuccessAction) {
        return baseLifecycleBindable.untilStop(maybe, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Maybe<T> maybe,
                                    @NonNull final Consumer<T> onSuccessAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilStop(maybe, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable) {
        return baseLifecycleBindable.untilDestroy(observable);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable, @NonNull final Consumer<T> onNextAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction,
                                       @NonNull final Action onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(observable, onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single) {
        return baseLifecycleBindable.untilDestroy(single);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        return baseLifecycleBindable.untilDestroy(single, onSuccessAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single,
                                       @NonNull final Consumer<T> onSuccessAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(single, onSuccessAction, onErrorAction);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable) {
        return baseLifecycleBindable.untilDestroy(completable);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable, @NonNull final Action onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(completable, onCompletedAction);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable,
                                   @NonNull final Action onCompletedAction,
                                   @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(completable, onCompletedAction, onErrorAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe) {
        return baseLifecycleBindable.untilDestroy(maybe);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe, @NonNull final Consumer<T> onSuccessAction) {
        return baseLifecycleBindable.untilDestroy(maybe, onSuccessAction);
    }

    @SuppressWarnings("CPD-END")
    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe,
                                       @NonNull final Consumer<T> onSuccessAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(maybe, onSuccessAction, onErrorAction);
    }

}
