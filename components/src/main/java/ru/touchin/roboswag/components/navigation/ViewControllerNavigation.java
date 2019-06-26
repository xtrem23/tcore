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

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import io.reactivex.functions.Function;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.navigation.fragments.SimpleViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.StatelessTargetedViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.StatelessViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.TargetedViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * Navigation based on {@link ViewController}s which are creating by {@link Fragment}s.
 * So basically it is just {@link FragmentNavigation} where most of fragments should be inherited from {@link ViewControllerFragment}.
 *
 * @param <TActivity> Type of activity where {@link ViewController}s should be showed.
 */
public class ViewControllerNavigation<TActivity extends ViewControllerActivity<?>> extends FragmentNavigation {

    public ViewControllerNavigation(@NonNull final Context context,
                                    @NonNull final FragmentManager fragmentManager,
                                    @IdRes final int containerViewId) {
        super(context, fragmentManager, containerViewId);
    }

    /**
     * Pushes {@link ViewControllerFragment} on top of stack.
     *
     * @param fragmentClass Class of {@link ViewControllerFragment} to instantiate;
     * @param state         Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param <TState>      Type of state of fragment.
     */
    public <TState extends AbstractState> void push(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                    @NonNull final TState state) {
        addToStack(fragmentClass, null, true, ViewControllerFragment.createState(state), null, null);
    }

    /**
     * Pushes {@link ViewControllerFragment} on top of stack with specific transaction setup.
     *
     * @param fragmentClass    Class of {@link ViewControllerFragment} to instantiate;
     * @param state            Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>         Type of state of fragment.
     */
    public <TState extends AbstractState> void push(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                    @Nullable final TState state,
                                                    @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, true, ViewControllerFragment.createState(state), null, transactionSetup);
    }

    /**
     * Pushes {@link ViewControllerFragment} on top of stack with specific target fragment.
     *
     * @param fragmentClass  Class of {@link ViewControllerFragment} to instantiate;
     * @param targetFragment Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment};
     * @param state          Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param <TState>       Type of state of fragment.
     */
    public <TState extends AbstractState> void pushForResult(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                             @NonNull final Fragment targetFragment,
                                                             @NonNull final TState state) {
        addToStack(fragmentClass, targetFragment, true, ViewControllerFragment.createState(state),
                fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link ViewControllerFragment} on top of stack with specific target fragment and specific transaction setup.
     *
     * @param fragmentClass    Class of {@link ViewControllerFragment} to instantiate;
     * @param targetFragment   Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment};
     * @param state            Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>         Type of state of fragment.
     */
    public <TState extends AbstractState> void pushForResult(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                             @NonNull final Fragment targetFragment,
                                                             @Nullable final TState state,
                                                             @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, true, ViewControllerFragment.createState(state),
                fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link ViewControllerFragment} on top of stack with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param fragmentClass Class of {@link ViewControllerFragment} to instantiate.
     * @param state         Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param <TState>      Type of state of fragment.
     */
    public <TState extends AbstractState> void setAsTop(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                        @NonNull final TState state) {
        setAsTop(fragmentClass, ViewControllerFragment.createState(state), null);
    }

    /**
     * Pushes {@link ViewControllerFragment} on top of stack with specific transaction setup
     * and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param fragmentClass    Class of {@link ViewControllerFragment} to instantiate.
     * @param state            Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>         Type of state of fragment.
     */
    public <TState extends AbstractState> void setAsTop(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                        @Nullable final TState state,
                                                        @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setAsTop(fragmentClass, ViewControllerFragment.createState(state), transactionSetup);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link ViewControllerFragment} on top of stack.
     *
     * @param fragmentClass Class of {@link ViewControllerFragment} to instantiate;
     * @param state         Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param <TState>      Type of state of fragment.
     */
    public <TState extends AbstractState> void setInitial(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                          @NonNull final TState state) {
        setInitial(fragmentClass, ViewControllerFragment.createState(state), null);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link ViewControllerFragment} on top of stack with specific transaction setup.
     *
     * @param fragmentClass    Class of {@link ViewControllerFragment} to instantiate;
     * @param state            Specific {@link AbstractState} of {@link ViewControllerFragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>         Type of state of fragment.
     */
    public <TState extends AbstractState> void setInitial(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                          @Nullable final TState state,
                                                          @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setInitial(fragmentClass, ViewControllerFragment.createState(state), transactionSetup);
    }

    /**
     * Pushes {@link ViewController} on top of stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed.
     */
    public void pushViewController(@NonNull final Class<? extends ViewController<TActivity,
            StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        addStatelessViewControllerToStack(viewControllerClass, null, null, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link ViewControllerFragment#getState()}.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void pushViewController(@NonNull final Class<? extends ViewController<TActivity,
            SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
                                                                  @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, null, state, null, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific with specific {@link ViewControllerFragment#getState()}
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param <TState>            Type of state of fragment;
     * @param <TTargetState>      Type of state of target fragment. State is using to affect on that fragment;
     * @param <TTargetFragment>   Type of target fragment.
     */
    public <TState extends AbstractState, TTargetState extends AbstractState, TTargetFragment extends ViewControllerFragment<? extends TTargetState,
            TActivity>> void pushViewController(@NonNull final Class<? extends ViewController<TActivity,
            SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
                                                @NonNull final TTargetFragment targetFragment,
                                                @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, targetFragment, state,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific transaction setup.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;.
     */
    public void pushViewController(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addStatelessViewControllerToStack(viewControllerClass, null, null, transactionSetup);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link ViewControllerFragment#getState()} and with specific transaction setup.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void pushViewController(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addViewControllerToStack(viewControllerClass, null, state, null, transactionSetup);
    }

    /**
     * Pushes {@link ViewController} without adding to stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed.
     */
    public void pushSingleViewController(@NonNull final Class<? extends ViewController<TActivity,
            StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        addToStack(StatelessViewControllerFragment.class, null, false, StatelessViewControllerFragment.createState(viewControllerClass), null, null);
    }

    /**
     * Pushes {@link ViewController} without adding to stack and with specific {@link ViewControllerFragment#getState()}.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void pushSingleViewController(@NonNull final Class<? extends ViewController<TActivity,
            SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass, @NonNull final TState state) {
        addToStack(SimpleViewControllerFragment.class, null, false, SimpleViewControllerFragment.createState(viewControllerClass, state), null, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link StatelessTargetedViewControllerFragment#getTarget()}.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param <TTargetState>      Type of state of target fragment. State is using to affect on that fragment;
     * @param <TTargetFragment>   Type of target fragment.
     */
    public <TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<? extends TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    StatelessTargetedViewControllerFragment<TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment) {
        addTargetedStatelessViewControllerToStack(viewControllerClass, targetFragment,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link StatelessTargetedViewControllerFragment#getTarget()} and transaction setup.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TTargetState>      Type of state of target fragment. State is using to affect on that fragment;
     * @param <TTargetFragment>   Type of target fragment.
     */
    public <TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<? extends TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    StatelessTargetedViewControllerFragment<TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addTargetedStatelessViewControllerToStack(viewControllerClass, targetFragment,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific with specific {@link ViewControllerFragment#getState()}
     * and with specific {@link TargetedViewControllerFragment#getTarget()}.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param <TState>            Type of state of fragment;
     * @param <TTargetState>      Type of state of target fragment. State is using to affect on that fragment;
     * @param <TTargetFragment>   Type of target fragment.
     */
    @SuppressWarnings("CPD-START")
    public <TState extends AbstractState, TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<? extends TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    TargetedViewControllerFragment<TState, TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment,
            @NonNull final TState state) {
        addTargetedViewControllerToStack(viewControllerClass, targetFragment, state,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link ViewControllerFragment#getState()}
     * and with specific {@link TargetedViewControllerFragment#getTarget()} and transaction setup.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment;
     * @param <TTargetState>      Type of state of target fragment. State is using to affect on that fragment;
     * @param <TTargetFragment>   Type of target fragment.
     */
    @SuppressWarnings("CPD-END")
    public <TState extends AbstractState, TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<? extends TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    TargetedViewControllerFragment<TState, TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment,
            @NonNull final TState state,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addTargetedViewControllerToStack(viewControllerClass, targetFragment, state,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link ViewController} on top of stack with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed.
     */
    public void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        addStatelessViewControllerToStack(viewControllerClass, null, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link ViewControllerFragment#getState()}
     * and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, null, state, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific transaction setup
     * and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addStatelessViewControllerToStack(viewControllerClass, null, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link ViewController} on top of stack with specific {@link ViewControllerFragment#getState()} and with specific transaction setup
     * and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addViewControllerToStack(viewControllerClass, null, state, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link ViewController} on top of stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     */
    public void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link ViewController} on top of stack
     * with specific {@link ViewControllerFragment#getState()}.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state) {
        setInitialViewController(viewControllerClass, state, null);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link ViewController} on top of stack with specific transaction setup.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     */
    public void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass, transactionSetup);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link ViewController} on top of stack
     * with specific {@link ViewControllerFragment#getState()} and specific transaction setup.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment.
     */
    public <TState extends AbstractState> void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass, state, transactionSetup);
    }

    /**
     * Base method to push stateless {@link ViewControllerFragment} to stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param backStackTag        Tag of {@link ViewControllerFragment} in back stack;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    protected void addStatelessViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity, ? extends StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Fragment targetFragment,
            @Nullable final String backStackTag,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(StatelessViewControllerFragment.class, targetFragment, true,
                StatelessViewControllerFragment.createState(viewControllerClass), backStackTag, transactionSetup);
    }

    /**
     * Base method to push stateful {@link ViewControllerFragment} with target to stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param backStackTag        Tag of {@link ViewControllerFragment} in back stack;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment.
     * @param <TTargetState>      Type of state of target fragment. State is using to affect on that fragment;
     */
    protected <TState extends AbstractState, TTargetState extends AbstractState> void addTargetedViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity,
                    ? extends TargetedViewControllerFragment<TState, TTargetState, TActivity>>> viewControllerClass,
            @NonNull final Fragment targetFragment,
            @NonNull final TState state,
            @Nullable final String backStackTag,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(TargetedViewControllerFragment.class, targetFragment, true,
                TargetedViewControllerFragment.createState(viewControllerClass, state), backStackTag, transactionSetup);
    }

    /**
     * Base method to push stateless {@link ViewControllerFragment} with target to stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param backStackTag        Tag of {@link ViewControllerFragment} in back stack;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment.
     */
    protected <TState extends AbstractState> void addTargetedStatelessViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity,
                    ? extends StatelessTargetedViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final Fragment targetFragment,
            @Nullable final String backStackTag,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(StatelessTargetedViewControllerFragment.class, targetFragment, true,
                StatelessTargetedViewControllerFragment.createState(viewControllerClass), backStackTag, transactionSetup);
    }

    /**
     * Base method to push stateful {@link ViewControllerFragment} to stack.
     *
     * @param viewControllerClass Class of {@link ViewController} to be pushed;
     * @param targetFragment      {@link ViewControllerFragment} to be set as target;
     * @param state               {@link AbstractState} of {@link ViewController}'s fragment;
     * @param backStackTag        Tag of {@link ViewControllerFragment} in back stack;
     * @param transactionSetup    Function to setup transaction before commit. It is useful to specify transition animations or additional info;
     * @param <TState>            Type of state of fragment.
     */
    protected <TState extends AbstractState> void addViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity, ? extends SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @Nullable final Fragment targetFragment,
            @NonNull final TState state,
            @Nullable final String backStackTag,
            @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(SimpleViewControllerFragment.class, targetFragment, true,
                SimpleViewControllerFragment.createState(viewControllerClass, state), backStackTag, transactionSetup);
    }

}