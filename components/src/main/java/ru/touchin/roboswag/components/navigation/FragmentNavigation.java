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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import io.reactivex.functions.Function;
import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * Navigation which is controlling fragments on activity using {@link android.support.v4.app.FragmentManager}.
 * Basically there are 4 main actions to add fragments to activity.
 * 1) {@link #setInitial} means to set fragment on top and remove all previously added fragments from stack;
 * 2) {@link #push} means to simply add fragment on top of the stack;
 * 3) {@link #setAsTop} means to push fragment on top of the stack with specific {@link #TOP_FRAGMENT_TAG_MARK} tag.
 * It is useful to realize up/back navigation: if {@link #up()} method will be called then stack will go to nearest fragment with TOP tag.
 * If {@link #back()} method will be called then stack will go to previous fragment.
 * Usually such logic using to set as top fragments from sidebar and show hamburger when some of them appeared;
 * 4) {@link #pushForResult} means to push fragment with target fragment. It is also adding {@link #WITH_TARGET_FRAGMENT_TAG_MARK} tag.
 * Also if such up/back navigation logic is not OK then {@link #backTo(Function)} method could be used with any condition to back to.
 * In that case in any stack-change method it is allowed to setup fragment transactions.
 */
public class FragmentNavigation {

    protected static final String TOP_FRAGMENT_TAG_MARK = "TOP_FRAGMENT";
    protected static final String WITH_TARGET_FRAGMENT_TAG_MARK = "FRAGMENT_WITH_TARGET";

    @NonNull
    private final Context context;
    @NonNull
    private final FragmentManager fragmentManager;
    @IdRes
    private final int containerViewId;

    public FragmentNavigation(@NonNull final Context context, @NonNull final FragmentManager fragmentManager, @IdRes final int containerViewId) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.containerViewId = containerViewId;
    }

    /**
     * Returns {@link Context} that is using to instantiate fragments.
     *
     * @return {@link Context}.
     */
    @NonNull
    public Context getContext() {
        return context;
    }

    /**
     * Returns {@link FragmentManager} using for navigation.
     *
     * @return {@link FragmentManager}.
     */
    @NonNull
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    /**
     * Returns if last fragment in stack is top (added by {@link #setAsTop} or {@link #setInitial}) like fragment from sidebar menu.
     *
     * @return True if last fragment on stack has TOP_FRAGMENT_TAG_MARK.
     */
    public boolean isCurrentFragmentTop() {
        if (fragmentManager.getBackStackEntryCount() == 0) {
            return true;
        }

        final String topFragmentTag = fragmentManager
                .getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1)
                .getName();
        return topFragmentTag != null && topFragmentTag.contains(TOP_FRAGMENT_TAG_MARK);
    }

    /**
     * Allowed to react on {@link android.app.Activity}'s menu item selection.
     *
     * @param item Selected menu item;
     * @return True if reaction fired.
     */
    @SuppressLint("InlinedApi")
    //InlinedApi: it is ok as android.R.id.home contains in latest SDK
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        return item.getItemId() == android.R.id.home && back();
    }

    /**
     * Base method which is adding fragment to stack.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param targetFragment   Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment};
     * @param addToStack       Flag to add this transaction to the back stack;
     * @param args             Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment};
     * @param backStackTag     Tag of {@link Fragment} in back stack;
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    @SuppressLint("CommitTransaction")
    //CommitTransaction: it is ok as we could setup transaction before commit
    protected void addToStack(@NonNull final Class<? extends Fragment> fragmentClass,
                              @Nullable final Fragment targetFragment,
                              final boolean addToStack,
                              @Nullable final Bundle args,
                              @Nullable final String backStackTag,
                              @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        if (fragmentManager.isDestroyed()) {
            Lc.assertion("FragmentManager is destroyed");
            return;
        }

        final Fragment fragment = Fragment.instantiate(context, fragmentClass.getName(), args);
        if (targetFragment != null) {
            if (fragmentManager != targetFragment.getFragmentManager()) {
                Lc.assertion("FragmentManager of target is differ then of creating fragment. Target will be lost after restoring activity. "
                        + targetFragment.getFragmentManager() + " != " + fragmentManager);
            }
            fragment.setTargetFragment(targetFragment, 0);
        }

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(containerViewId, fragment, null);
        if (addToStack) {
            fragmentTransaction.addToBackStack(backStackTag);
        }
        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentTransaction.setTransition(getDefaultTransition());
        }
        if (transactionSetup != null) {
            try {
                transactionSetup.apply(fragmentTransaction).commit();
            } catch (final Exception exception) {
                Lc.assertion(exception);
                fragmentTransaction.commit();
            }
        } else {
            fragmentTransaction.commit();
        }
    }

    /**
     * Returns default transition animation.
     *
     * @return {@link FragmentTransaction#TRANSIT_FRAGMENT_OPEN}.
     */
    protected int getDefaultTransition() {
        return FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
    }

    /**
     * Simply calls {@link FragmentManager#popBackStack()}.
     *
     * @return True if it have back to some entry in stack.
     */
    public boolean back() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    /**
     * Backs to fragment which back stack's entry satisfy to specific condition.
     *
     * @param condition Condition of back stack entry to be satisfied;
     * @return True if it have back to some entry in stack.
     */
    public boolean backTo(@NonNull final Function<FragmentManager.BackStackEntry, Boolean> condition) {
        final int stackSize = fragmentManager.getBackStackEntryCount();
        Integer id = null;
        try {
            for (int i = stackSize - 2; i >= 0; i--) {
                final FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(i);
                id = backStackEntry.getId();
                if (condition.apply(backStackEntry)) {
                    break;
                }
            }
        } catch (final Exception exception) {
            Lc.assertion(exception);
            return false;
        }
        if (id != null) {
            fragmentManager.popBackStack(id, 0);
            return true;
        }
        return false;
    }

    /**
     * Backs to fragment with specific {@link #TOP_FRAGMENT_TAG_MARK} tag.
     * This tag is adding if fragment added to stack via {@link #setInitial} or {@link #setAsTop(Class)} methods.
     * It can be used to create simple up/back navigation.
     *
     * @return True if it have back to some entry in stack.
     */
    @SuppressWarnings("PMD.ShortMethodName")
    //ShortMethodName: it is ok because method name is good!
    public boolean up() {
        return backTo(backStackEntry ->
                backStackEntry.getName() != null && backStackEntry.getName().endsWith(TOP_FRAGMENT_TAG_MARK));
    }

    /**
     * Pushes {@link Fragment} on top of stack.
     *
     * @param fragmentClass Class of {@link Fragment} to instantiate.
     */
    public void push(@NonNull final Class<? extends Fragment> fragmentClass) {
        addToStack(fragmentClass, null, true, null, null, null);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific arguments.
     *
     * @param fragmentClass Class of {@link Fragment} to instantiate;
     * @param args          Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment}.
     */
    public void push(@NonNull final Class<? extends Fragment> fragmentClass,
                     @NonNull final Bundle args) {
        addToStack(fragmentClass, null, true, args, null, null);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific transaction setup.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void push(@NonNull final Class<? extends Fragment> fragmentClass,
                     @NonNull final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, true, null, null, transactionSetup);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific arguments and transaction setup.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param args             Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void push(@NonNull final Class<? extends Fragment> fragmentClass,
                     @Nullable final Bundle args,
                     @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, true, args, null, transactionSetup);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific target fragment.
     *
     * @param fragmentClass  Class of {@link Fragment} to instantiate;
     * @param targetFragment Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment}.
     */
    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment) {
        addToStack(fragmentClass, targetFragment, true, null, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific target fragment and arguments.
     *
     * @param fragmentClass  Class of {@link Fragment} to instantiate;
     * @param targetFragment Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment};
     * @param args           Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment}.
     */
    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment,
                              @NonNull final Bundle args) {
        addToStack(fragmentClass, targetFragment, true, args, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific target fragment and transaction setup.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param targetFragment   Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment,
                              @NonNull final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, true, null, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific target fragment, arguments and transaction setup.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param targetFragment   Target fragment to be set as {@link Fragment#getTargetFragment()} of instantiated {@link Fragment};
     * @param args             Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void pushForResult(@NonNull final Class<? extends Fragment> fragmentClass,
                              @NonNull final Fragment targetFragment,
                              @Nullable final Bundle args,
                              @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, true, args, fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link Fragment} on top of stack with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param fragmentClass Class of {@link Fragment} to instantiate.
     */
    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass) {
        addToStack(fragmentClass, null, true, null, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific arguments and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param fragmentClass Class of {@link Fragment} to instantiate;
     * @param args          Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment}.
     */
    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass,
                         @NonNull final Bundle args) {
        addToStack(fragmentClass, null, true, args, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, null);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific transaction setup
     * and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass,
                         @NonNull final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, true, null, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pushes {@link Fragment} on top of stack with specific transaction setup, arguments
     * and with {@link #TOP_FRAGMENT_TAG_MARK} tag used for simple up/back navigation.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param args             Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void setAsTop(@NonNull final Class<? extends Fragment> fragmentClass,
                         @Nullable final Bundle args,
                         @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, true, args, fragmentClass.getName() + ';' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link Fragment} on top of stack.
     *
     * @param fragmentClass Class of {@link Fragment} to instantiate.
     */
    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass) {
        setInitial(fragmentClass, null, null);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link Fragment} on top of stack with specific arguments.
     *
     * @param fragmentClass Class of {@link Fragment} to instantiate;
     * @param args          Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment}.
     */
    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass,
                           @NonNull final Bundle args) {
        setInitial(fragmentClass, args, null);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link Fragment} on top of stack with specific transaction setup.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass,
                           @NonNull final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setInitial(fragmentClass, null, transactionSetup);
    }

    /**
     * Pops all {@link Fragment}s and places new initial {@link Fragment} on top of stack with specific transaction setup and arguments.
     *
     * @param fragmentClass    Class of {@link Fragment} to instantiate;
     * @param args             Bundle to be set as {@link Fragment#getArguments()} of instantiated {@link Fragment};
     * @param transactionSetup Function to setup transaction before commit. It is useful to specify transition animations or additional info.
     */
    public void setInitial(@NonNull final Class<? extends Fragment> fragmentClass,
                           @Nullable final Bundle args,
                           @Nullable final Function<FragmentTransaction, FragmentTransaction> transactionSetup) {
        beforeSetInitialActions();
        setAsTop(fragmentClass, args, transactionSetup);
    }

    /**
     * Method calls every time before initial {@link Fragment} will be placed.
     */
    protected void beforeSetInitialActions() {
        if (fragmentManager.isDestroyed()) {
            Lc.assertion("FragmentManager is destroyed");
            return;
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

}
