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

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcel;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import ru.touchin.roboswag.components.navigation.AbstractState;
import ru.touchin.roboswag.components.navigation.ViewController;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.Optional;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import ru.touchin.roboswag.core.utils.pairs.NullablePair;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Fragment instantiated in specific activity of {@link TActivity} type that is holding {@link ViewController} inside.
 *
 * @param <TState>    Type of object which is representing it's fragment state;
 * @param <TActivity> Type of {@link ViewControllerActivity} where fragment could be attached to.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class ViewControllerFragment<TState extends AbstractState, TActivity extends ViewControllerActivity<?>>
        extends ViewFragment<TActivity> {

    private static final String VIEW_CONTROLLER_STATE_EXTRA = "VIEW_CONTROLLER_STATE_EXTRA";

    private static boolean inDebugMode;
    private static long acceptableUiCalculationTime = 100;

    /**
     * Enables debugging features like serialization of {@link #getState()} every creation.
     */
    public static void setInDebugMode() {
        inDebugMode = true;
    }

    /**
     * Sets acceptable UI calculation time so there will be warnings in logs if ViewController's inflate/layout actions will take more than that time.
     * Works only if {@link #setInDebugMode()} called.
     * It's 100ms by default.
     */
    public static void setAcceptableUiCalculationTime(final long acceptableUiCalculationTime) {
        ViewControllerFragment.acceptableUiCalculationTime = acceptableUiCalculationTime;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private static <T extends Serializable> T reserialize(@NonNull final T serializable) {
        Parcel parcel = Parcel.obtain();
        parcel.writeSerializable(serializable);
        final byte[] serializableBytes = parcel.marshall();
        parcel.recycle();
        parcel = Parcel.obtain();
        parcel.unmarshall(serializableBytes, 0, serializableBytes.length);
        parcel.setDataPosition(0);
        final T result = (T) parcel.readSerializable();
        parcel.recycle();
        return result;
    }

    /**
     * Creates {@link Bundle} which will store state.
     *
     * @param state State to use into ViewController.
     * @return Returns bundle with state inside.
     */
    @NonNull
    public static Bundle createState(@Nullable final AbstractState state) {
        final Bundle result = new Bundle();
        result.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, state);
        return result;
    }

    @NonNull
    private final BehaviorSubject<Optional<TActivity>> activitySubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<NullablePair<PlaceholderView, Bundle>> viewSubject = BehaviorSubject.create();
    @Nullable
    private ViewController viewController;
    private Disposable viewControllerSubscription;
    private TState state;
    private boolean started;
    private boolean stateCreated;

    private void tryCreateState(@Nullable final Context context) {
        if (!stateCreated && state != null && context != null) {
            state.onCreate();
            stateCreated = true;
        }
    }

    /**
     * Returns specific {@link AbstractState} which contains state of fragment and it's {@link ViewController}.
     *
     * @return Object represents state.
     */
    @NonNull
    public TState getState() {
        return state;
    }

    /**
     * It should return specific {@link ViewController} class to control instantiated view by logic after activity creation.
     *
     * @return Returns class of specific {@link ViewController}.
     */
    @NonNull
    public abstract Class<? extends ViewController<TActivity,
            ? extends ViewControllerFragment<TState, TActivity>>> getViewControllerClass();

    /**
     * Returns if ViewControllerFragment requires state or not.
     *
     * @return true if state is required
     */
    protected abstract boolean isStateRequired();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(!isChildFragment());

        state = savedInstanceState != null
                ? (TState) savedInstanceState.getSerializable(VIEW_CONTROLLER_STATE_EXTRA)
                : (getArguments() != null ? (TState) getArguments().getSerializable(VIEW_CONTROLLER_STATE_EXTRA) : null);
        if (state != null) {
            if (inDebugMode) {
                state = reserialize(state);
            }
            tryCreateState(getContext());
        } else if (isStateRequired()) {
            Lc.assertion("State is required and null");
        }
        viewControllerSubscription = Observable
                .combineLatest(activitySubject.distinctUntilChanged(), viewSubject.distinctUntilChanged(),
                        (activityOptional, viewInfo) -> {
                            final TActivity activity = activityOptional.get();
                            final PlaceholderView container = viewInfo.getFirst();
                            if (activity == null || container == null) {
                                return new Optional<ViewController>(null);
                            }
                            final ViewController newViewController = createViewController(activity, container, viewInfo.getSecond());
                            newViewController.onCreate();
                            return new Optional<>(newViewController);
                        })
                .subscribe(this::onViewControllerChanged,
                        throwable -> Lc.cutAssertion(throwable, InvocationTargetException.class, InflateException.class));
    }

    @NonNull
    private ViewController createViewController(@NonNull final TActivity activity, @NonNull final PlaceholderView view,
                                                @Nullable final Bundle savedInstanceState) {

        if (getViewControllerClass().getConstructors().length != 1) {
            throw new ShouldNotHappenException("There should be single constructor for " + getViewControllerClass());
        }
        final Constructor<?> constructor = getViewControllerClass().getConstructors()[0];
        final ViewController.CreationContext creationContext = new ViewController.CreationContext(activity, this, view);
        final long creationTime = inDebugMode ? SystemClock.elapsedRealtime() : 0;
        try {
            switch (constructor.getParameterTypes().length) {
                case 2:
                    return (ViewController) constructor.newInstance(creationContext, savedInstanceState);
                case 3:
                    return (ViewController) constructor.newInstance(this, creationContext, savedInstanceState);
                default:
                    throw new ShouldNotHappenException("Wrong constructor parameters count: " + constructor.getParameterTypes().length);
            }
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        } finally {
            checkCreationTime(creationTime);
        }
    }

    private void checkCreationTime(final long creationTime) {
        if (inDebugMode) {
            final long creationPeriod = SystemClock.elapsedRealtime() - creationTime;
            if (creationPeriod > acceptableUiCalculationTime) {
                UiUtils.UI_METRICS_LC_GROUP.w("Creation of %s took too much: %dms", getViewControllerClass(), creationPeriod);
            }
        }
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        tryCreateState(context);
    }

    @Deprecated
    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return new PlaceholderView(inflater.getContext(), getViewControllerClass().getName());
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view instanceof PlaceholderView) {
            viewSubject.onNext(new NullablePair<>((PlaceholderView) view, savedInstanceState));
        } else {
            Lc.assertion("View should be instanceof PlaceholderView");
        }
    }

    @Override
    public void onActivityCreated(@NonNull final View view, @NonNull final TActivity activity, @Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(view, activity, savedInstanceState);
        activitySubject.onNext(new Optional<>(activity));
    }

    @Override
    protected void onStart(@NonNull final View view, @NonNull final TActivity activity) {
        super.onStart(view, activity);
        started = true;
        if (viewController != null) {
            viewController.onStart();
        }
    }

    @Override
    protected void onAppear(@NonNull final View view, @NonNull final TActivity activity) {
        super.onAppear(view, activity);
        if (viewController != null) {
            viewController.onAppear();
        }
    }

    @Override
    protected void onResume(@NonNull final View view, @NonNull final TActivity activity) {
        super.onResume(view, activity);
        if (viewController != null) {
            viewController.onResume();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (viewController != null) {
            viewController.onLowMemory();
        }
    }

    /**
     * Calls when activity configuring ActionBar, Toolbar, Sidebar etc.
     * If it will be called or not depends on {@link #hasOptionsMenu()} and {@link #isMenuVisible()}.
     *
     * @param menu     The options menu in which you place your items;
     * @param inflater Helper to inflate menu items.
     */
    protected void onConfigureNavigation(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        if (viewController != null) {
            viewController.onConfigureNavigation(menu, inflater);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        onConfigureNavigation(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return (viewController != null && viewController.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    private void onViewControllerChanged(@NonNull final Optional<ViewController> viewControllerOptional) {
        if (this.viewController != null) {
            this.viewController.onDestroy();
        }
        this.viewController = viewControllerOptional.get();
        if (this.viewController != null) {
            if (started) {
                this.viewController.onStart();
            }
            this.viewController.getActivity().reconfigureNavigation();
        }
    }

    @Override
    protected void onPause(@NonNull final View view, @NonNull final TActivity activity) {
        super.onPause(view, activity);
        if (viewController != null) {
            viewController.onPause();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (viewController != null) {
            viewController.onSaveInstanceState(savedInstanceState);
        }
        savedInstanceState.putSerializable(VIEW_CONTROLLER_STATE_EXTRA, state);
    }

    @Override
    protected void onDisappear(@NonNull final View view, @NonNull final TActivity activity) {
        super.onDisappear(view, activity);
        if (viewController != null) {
            viewController.onDisappear();
        }
    }

    @Override
    protected void onStop(@NonNull final View view, @NonNull final TActivity activity) {
        started = false;
        if (viewController != null) {
            viewController.onStop();
        }
        super.onStop(view, activity);
    }

    @Override
    protected void onDestroyView(@NonNull final View view) {
        viewSubject.onNext(new NullablePair<>(null, null));
        super.onDestroyView(view);
    }

    @Override
    public void onDetach() {
        activitySubject.onNext(new Optional<>(null));
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        viewControllerSubscription.dispose();
        if (viewController != null && !viewController.isDestroyed()) {
            viewController.onDestroy();
            viewController = null;
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        if (viewController != null) {
            viewController.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static class PlaceholderView extends FrameLayout {

        @NonNull
        private final String tagName;
        private long lastMeasureTime;

        public PlaceholderView(@NonNull final Context context, @NonNull final String tagName) {
            super(context);
            this.tagName = tagName;
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (inDebugMode && lastMeasureTime == 0) {
                lastMeasureTime = SystemClock.uptimeMillis();
            }
        }

        @Override
        protected void onDraw(@NonNull final Canvas canvas) {
            super.onDraw(canvas);
            if (inDebugMode && lastMeasureTime > 0) {
                final long layoutTime = SystemClock.uptimeMillis() - lastMeasureTime;
                if (layoutTime > acceptableUiCalculationTime) {
                    UiUtils.UI_METRICS_LC_GROUP.w("Measure and layout of %s took too much: %dms", tagName, layoutTime);
                }
                lastMeasureTime = 0;
            }
        }

    }

}
