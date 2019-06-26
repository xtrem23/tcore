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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.reactivex.functions.BiConsumer;
import ru.touchin.roboswag.components.navigation.OnFragmentStartedListener;
import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * Non-background fragment that have specific activity as a parent.
 *
 * @param <TActivity> Type of activity which to such fragment could be attached.
 */
public abstract class ViewFragment<TActivity extends AppCompatActivity> extends Fragment
        implements OnFragmentStartedListener {

    private boolean appeared;
    private boolean started;

    /**
     * Returns if fragment have parent fragment.
     *
     * @return Returns true if fragment is in some fragment's children stack.
     */
    public boolean isChildFragment() {
        return getParentFragment() != null;
    }

    /**
     * Returns specific activity which to this fragment could be attached.
     *
     * @return Returns parent activity.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected final TActivity getBaseActivity() {
        if (getActivity() == null) {
            return null;
        }

        try {
            return (TActivity) getActivity();
        } catch (final ClassCastException exception) {
            Lc.assertion(exception);
            return null;
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        throw new IllegalStateException("Method onCreateView() should be overridden");
    }

    @Override
    @CallSuper
    public void onFragmentStarted(@NonNull final Fragment fragment) {
        //do nothing
    }

    @Deprecated
    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() == null || getBaseActivity() == null) {
            Lc.assertion("View and activity shouldn't be null");
            return;
        }
        onActivityCreated(getView(), getBaseActivity(), savedInstanceState);
    }

    /**
     * Replacement of {@link #onActivityCreated} with non null activity as first parameter.
     *
     * @param view               Instantiated view.
     * @param activity           Activity which fragment attached to.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @CallSuper
    public void onActivityCreated(@NonNull final View view, @NonNull final TActivity activity, @Nullable final Bundle savedInstanceState) {
        //do nothing
    }

    private void callMethodAfterInstantiation(@NonNull final BiConsumer<View, TActivity> action) {
        if (getView() == null || getBaseActivity() == null) {
            Lc.assertion("View and activity shouldn't be null");
            return;
        }
        try {
            action.accept(getView(), getBaseActivity());
        } catch (final Exception exception) {
            Lc.assertion(exception);
        }
    }

    @Deprecated
    @Override
    public void onStart() {
        super.onStart();
        started = true;
        callMethodAfterInstantiation(this::onStart);
    }

    /**
     * Replacement of {@link #onStart} with non null activity as first parameter.
     *
     * @param view     Instantiated view.
     * @param activity Activity which fragment attached to.
     */
    @CallSuper
    @SuppressWarnings("RestrictedApi")
    //RestrictedApi: we need isMenuVisible() to check analytics rightly!
    protected void onStart(@NonNull final View view, @NonNull final TActivity activity) {
        if (getParentFragment() instanceof OnFragmentStartedListener) {
            ((OnFragmentStartedListener) getParentFragment()).onFragmentStarted(this);
        } else if (activity instanceof OnFragmentStartedListener) {
            ((OnFragmentStartedListener) activity).onFragmentStarted(this);
        }
        if (!appeared && isMenuVisible()) {
            onAppear(view, activity);
        }
    }

    /**
     * Called when fragment is moved in started state and it's {@link #isMenuVisible()} sets to true.
     * Usually it is indicating that user can't see fragment on screen and useful to track analytics events.
     *
     * @param view     Instantiated view.
     * @param activity Activity which fragment attached to.
     */
    protected void onAppear(@NonNull final View view, @NonNull final TActivity activity) {
        appeared = true;
    }

    @Deprecated
    @Override
    public void onResume() {
        super.onResume();
        callMethodAfterInstantiation(this::onResume);
    }

    /**
     * Replacement of {@link #onResume} with non null activity as first parameter.
     *
     * @param view     Instantiated view.
     * @param activity Activity which fragment attached to.
     */
    @CallSuper
    protected void onResume(@NonNull final View view, @NonNull final TActivity activity) {
        //do nothing
    }

    @Override
    public void setMenuVisibility(final boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (getBaseActivity() != null && getView() != null) {
            if (!appeared && menuVisible && started) {
                onAppear(getView(), getBaseActivity());
            }
            if (appeared && (!menuVisible || !started)) {
                onDisappear(getView(), getBaseActivity());
            }
        }
    }

    @Deprecated
    @Override
    public void onPause() {
        callMethodAfterInstantiation(this::onPause);
        super.onPause();
    }

    /**
     * Replacement of {@link #onPause} with non null activity as first parameter.
     *
     * @param view     Instantiated view.
     * @param activity Activity which fragment attached to.
     */
    @CallSuper
    protected void onPause(@NonNull final View view, @NonNull final TActivity activity) {
        // do nothing
    }

    /**
     * Called when fragment is moved in stopped state or it's {@link #isMenuVisible()} sets to false.
     * Usually it is indicating that user can't see fragment on screen and useful to track analytics events.
     *
     * @param view     Instantiated view.
     * @param activity Activity which fragment attached to.
     */
    protected void onDisappear(@NonNull final View view, @NonNull final TActivity activity) {
        appeared = false;
    }

    @Deprecated
    @Override
    public void onStop() {
        started = false;
        callMethodAfterInstantiation(this::onStop);
        super.onStop();
    }

    /**
     * Replacement of {@link #onStop} with non null activity as first parameter.
     *
     * @param view     Instantiated view.
     * @param activity Activity which fragment attached to.
     */
    @CallSuper
    protected void onStop(@NonNull final View view, @NonNull final TActivity activity) {
        if (appeared) {
            onDisappear(view, activity);
        }
    }

    @Deprecated
    @Override
    public void onDestroyView() {
        if (getView() == null) {
            Lc.assertion("View shouldn't be null");
            return;
        }
        onDestroyView(getView());
        super.onDestroyView();
    }

    /**
     * Replacement of {@link #onDestroyView} with non null activity as first parameter.
     *
     * @param view Instantiated view.
     */
    @CallSuper
    protected void onDestroyView(@NonNull final View view) {
        //do nothing
    }

}
