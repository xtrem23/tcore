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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment;
import ru.touchin.roboswag.components.utils.BaseLifecycleBindable;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Class to control view of specific fragment, activity and application by logic bridge.
 *
 * @param <TActivity> Type of activity where such {@link ViewController} could be;
 * @param <TFragment> Type of fragment where such {@link ViewController} could be;
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessivePublicCount"})
public class ViewController<TActivity extends ViewControllerActivity<?>,
        TFragment extends ViewControllerFragment<?, TActivity>>
        implements LifecycleBindable {

    @NonNull
    private final TActivity activity;
    @NonNull
    private final TFragment fragment;
    @NonNull
    private final ViewGroup container;
    @NonNull
    private final BaseLifecycleBindable baseLifecycleBindable = new BaseLifecycleBindable();
    private boolean destroyed;

    @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter"})
    //UnusedFormalParameter: savedInstanceState could be used by children
    public ViewController(@NonNull final CreationContext creationContext, @Nullable final Bundle savedInstanceState) {
        this.activity = (TActivity) creationContext.activity;
        this.fragment = (TFragment) creationContext.fragment;
        this.container = creationContext.container;
    }

    /**
     * Returns activity where {@link ViewController} could be.
     *
     * @return Returns activity.
     */
    @NonNull
    public final TActivity getActivity() {
        return activity;
    }

    /**
     * Returns fragment where {@link ViewController} could be.
     *
     * @return Returns fragment.
     */
    @NonNull
    public final TFragment getFragment() {
        return fragment;
    }

    /**
     * Returns view instantiated in {@link #getFragment()} fragment attached to {@link #getActivity()} activity.
     * Use it to inflate your views into at construction of this {@link ViewController}.
     *
     * @return Returns view.
     */
    @NonNull
    public final ViewGroup getContainer() {
        return container;
    }

    /**
     * Returns if {@link ViewController} destroyed or not.
     *
     * @return True if it is destroyed.
     */
    public final boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Return a localized string from the application's package's default string table.
     *
     * @param resId Resource id for the string
     */
    @NonNull
    public final String getString(@StringRes final int resId) {
        return getActivity().getString(resId);
    }

    /**
     * Return a localized formatted string from the application's package's default string table, substituting the format arguments as defined in
     * {@link java.util.Formatter} and {@link java.lang.String#format}.
     *
     * @param resId      Resource id for the format string
     * @param formatArgs The format arguments that will be used for substitution.
     */
    @NonNull
    public final String getString(@StringRes final int resId, @NonNull final Object... formatArgs) {
        return getActivity().getString(resId, formatArgs);
    }

    /**
     * Set the view controller content from a layout resource.
     * This layout is placed directly into the container's ({@link #getContainer()}) view hierarchy.
     *
     * @param layoutResId Resource ID to be inflated.
     */
    public final void setContentView(@LayoutRes final int layoutResId) {
        if (getContainer().getChildCount() > 0) {
            getContainer().removeAllViews();
        }
        UiUtils.inflateAndAdd(layoutResId, getContainer());
    }

    /**
     * Set the view controller content to an explicit view.
     * This view is placed directly into the container's ({@link #getContainer()}) view hierarchy.
     *
     * @param view The desired content to display.
     */
    public final void setContentView(@NonNull final View view) {
        setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * Set the view controller content to an explicit view with specific layout parameters.
     * This view is placed directly into the container's ({@link #getContainer()}) view hierarchy.
     *
     * @param view         The desired content to display;
     * @param layoutParams Layout parameters for the view.
     */
    public final void setContentView(@NonNull final View view, @NonNull final ViewGroup.LayoutParams layoutParams) {
        if (getContainer().getChildCount() > 0) {
            getContainer().removeAllViews();
        }
        getContainer().addView(view, layoutParams);
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
        final T viewById = (T) getContainer().findViewById(id);
        if (viewById == null) {
            throw new ShouldNotHappenException("No view for id=" + getActivity().getResources().getResourceName(id));
        }
        return viewById;
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
        return getActivity().getColorCompat(resId);
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
        return getActivity().getDrawableCompat(resId);
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar etc.
     * If it will be called or not depends on {@link Fragment#hasOptionsMenu()} and {@link Fragment#isMenuVisible()}.
     *
     * @param menu     The options menu in which you place your items;
     * @param inflater Helper to inflate menu items.
     */
    public void onConfigureNavigation(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        // do nothing
    }

    /**
     * Calls right after construction of {@link ViewController}.
     * Happens at {@link ViewControllerFragment#onActivityCreated(View, ViewControllerActivity, Bundle)}.
     */
    @CallSuper
    public void onCreate() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onCreate();
    }

    /**
     * Calls when {@link ViewController} have started.
     * Happens at {@link ViewControllerFragment#onStart(View, ViewControllerActivity)}.
     */
    @CallSuper
    public void onStart() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onStart();
    }

    /**
     * Called when fragment is moved in started state and it's {@link #getFragment().isMenuVisible()} sets to true.
     * Usually it is indicating that user can't see fragment on screen and useful to track analytics events.
     */
    public void onAppear() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    /**
     * Calls when {@link ViewController} have resumed.
     * Happens at {@link ViewControllerFragment#onResume(View, ViewControllerActivity)}.
     */
    @CallSuper
    public void onResume() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onResume();
    }

    /**
     * Calls when {@link ViewController} have goes near out of memory state.
     * Happens at {@link ViewControllerFragment#onLowMemory()}.
     */
    @CallSuper
    public void onLowMemory() {
        //do nothing
    }

    /**
     * Calls when {@link ViewController} have paused.
     * Happens at {@link ViewControllerFragment#onPause(View, ViewControllerActivity)}.
     */
    @CallSuper
    public void onPause() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    /**
     * Calls when {@link ViewController} should save it's state.
     * Happens at {@link ViewControllerFragment#onSaveInstanceState(Bundle)}.
     * Try not to use such method for saving state but use {@link ViewControllerFragment#getState()} from {@link #getFragment()}.
     */
    @CallSuper
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        baseLifecycleBindable.onSaveInstanceState();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    /**
     * Called when fragment is moved in stopped state or it's {@link #getFragment().isMenuVisible()} sets to false.
     * Usually it is indicating that user can't see fragment on screen and useful to track analytics events.
     */
    public void onDisappear() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    /**
     * Calls when {@link ViewController} have stopped.
     * Happens at {@link ViewControllerFragment#onStop(View, ViewControllerActivity)}.
     */
    @CallSuper
    public void onStop() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onStop();
    }

    /**
     * Calls when {@link ViewController} have destroyed.
     * Happens usually at {@link ViewControllerFragment#onDestroyView(View)}. In some cases at {@link ViewControllerFragment#onDestroy()}.
     */
    @CallSuper
    public void onDestroy() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        baseLifecycleBindable.onDestroy();
        destroyed = true;
    }

    /**
     * Callback from parent fragment.
     */
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        // Do nothing
    }

    /**
     * Similar to {@link ViewControllerFragment#onOptionsItemSelected(MenuItem)}.
     *
     * @param item Selected menu item;
     * @return True if selection processed.
     */
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return false;
    }

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
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe, @NonNull final Consumer<T> onCompletedAction) {
        return baseLifecycleBindable.untilDestroy(maybe, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe,
                                       @NonNull final Consumer<T> onCompletedAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return baseLifecycleBindable.untilDestroy(maybe, onCompletedAction, onErrorAction);
    }

    @SuppressWarnings("CPD-END")
    /*
     * Helper class to simplify constructor override.
     */
    public static class CreationContext {

        @NonNull
        private final ViewControllerActivity activity;
        @NonNull
        private final ViewControllerFragment fragment;
        @NonNull
        private final ViewGroup container;

        public CreationContext(@NonNull final ViewControllerActivity activity,
                               @NonNull final ViewControllerFragment fragment,
                               @NonNull final ViewGroup container) {
            this.activity = activity;
            this.fragment = fragment;
            this.container = container;
        }

    }

}