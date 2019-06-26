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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import ru.touchin.roboswag.components.navigation.activities.BaseActivity;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.log.LcGroup;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * General utilities related to UI (Inflation, Views, Metrics, Activities etc.).
 */
public final class UiUtils {

    /**
     * Logging group to log UI metrics (like inflation or layout time etc.).
     */
    public static final LcGroup UI_METRICS_LC_GROUP = new LcGroup("UI_METRICS");
    /**
     * Logging group to log UI lifecycle (onCreate, onStart, onResume etc.).
     */
    public static final LcGroup UI_LIFECYCLE_LC_GROUP = new LcGroup("UI_LIFECYCLE");
    /**
     * Delay to let user view ripple effect before screen changed.
     */
    public static final long RIPPLE_EFFECT_DELAY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 150 : 0;

    private static final Handler RIPPLE_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * Method to inflate view with right layout parameters based on container and add inflated view as a child to it.
     *
     * @param layoutId Id of layout resource;
     * @param parent   Container to rightly resolve layout parameters of view in XML;
     * @return Inflated view.
     */
    @NonNull
    public static View inflateAndAdd(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, true);
        return parent.getChildAt(parent.getChildCount() - 1);
    }

    /**
     * Method to inflate view with right layout parameters based on container but not adding inflated view as a child to it.
     *
     * @param layoutId Id of layout resource;
     * @param parent   Container to rightly resolve layout parameters of view in XML;
     * @return Inflated view.
     */
    @NonNull
    public static View inflate(@LayoutRes final int layoutId, @NonNull final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    /**
     * Sets click listener to view. On click it will call something after delay.
     *
     * @param targetView      View to set click listener to;
     * @param onClickListener Click listener;
     * @param delay           Delay after which click listener will be called.
     */
    public static void setOnRippleClickListener(@NonNull final View targetView, @Nullable final Action onClickListener, final long delay) {
        setOnRippleClickListener(targetView, onClickListener != null ? view -> onClickListener.run() : null, delay);
    }

    /**
     * Sets click listener to view. On click it will call something with {@link #RIPPLE_EFFECT_DELAY}.
     *
     * @param targetView      View to set click listener to;
     * @param onClickListener Click listener.
     */
    public static void setOnRippleClickListener(@NonNull final View targetView, @Nullable final Action onClickListener) {
        setOnRippleClickListener(targetView, onClickListener != null ? view -> onClickListener.run() : null, RIPPLE_EFFECT_DELAY);
    }

    /**
     * Sets click listener to view. On click it will call something after delay.
     *
     * @param targetView      View to set click listener to;
     * @param onClickListener Click listener;
     * @param delay           Delay after which click listener will be called.
     */
    public static void setOnRippleClickListener(@NonNull final View targetView, @Nullable final Consumer<View> onClickListener, final long delay) {
        if (onClickListener == null) {
            targetView.setOnClickListener(null);
            return;
        }

        final Runnable runnable = () -> {
            if (targetView.getWindowVisibility() != View.VISIBLE
                    || !targetView.hasWindowFocus()
                    || (targetView.getContext() instanceof BaseActivity && !((BaseActivity) targetView.getContext()).isActuallyResumed())) {
                return;
            }
            try {
                onClickListener.accept(targetView);
            } catch (final Exception exception) {
                Lc.assertion(exception);
            }
        };

        targetView.setOnClickListener(v -> {
            RIPPLE_HANDLER.removeCallbacksAndMessages(null);
            RIPPLE_HANDLER.postDelayed(runnable, delay);
        });
    }

    private UiUtils() {
    }

    /**
     * Utilities methods related to metrics.
     */
    public static class OfMetrics {

        private static final int MAX_METRICS_TRIES_COUNT = 5;

        /**
         * Returns right metrics with non-zero height/width.
         * It is common bug when metrics are calling at {@link Application#onCreate()} method and it returns metrics with zero height/width.
         *
         * @param context {@link Context} of metrics;
         * @return {@link DisplayMetrics}.
         */
        @SuppressWarnings("BusyWait")
        @NonNull
        public static DisplayMetrics getDisplayMetrics(@NonNull final Context context) {
            DisplayMetrics result = context.getResources().getDisplayMetrics();
            // it is needed to avoid bug with invalid metrics when user restore application from other application
            int metricsTryNumber = 0;
            while (metricsTryNumber < MAX_METRICS_TRIES_COUNT && (result.heightPixels <= 0 || result.widthPixels <= 0)) {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException ignored) {
                    return result;
                }
                result = context.getResources().getDisplayMetrics();
                metricsTryNumber++;
            }
            return result;
        }

        /**
         * Simply converts DP to pixels.
         *
         * @param context  {@link Context} of metrics;
         * @param sizeInDp Size in DP;
         * @return Size in pixels.
         */
        public static float dpToPixels(@NonNull final Context context, final float sizeInDp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getDisplayMetrics(context));
        }

        public static int pixelsToDp(@NonNull final Context context, final int pixels) {
            return (int) (pixels * getDisplayMetrics(context).density + 0.5f);
        }

        private OfMetrics() {
        }

    }

    /**
     * Utilities methods related to activities and it'sintents.
     */
    public static class OfActivities {

        /**
         * Returns action bar (on top like toolbar or appbar) common height (56dp).
         *
         * @param activity Activity of action bar;
         * @return Height of action bar.
         */
        public static int getActionBarHeight(@NonNull final Activity activity) {
            return (int) OfMetrics.dpToPixels(activity, 56);
        }

        /**
         * Returns status bar (on top where system info is) common height.
         *
         * @param activity Activity of status bar;
         * @return Height of status bar.
         */
        public static int getStatusBarHeight(@NonNull final Activity activity) {
            final int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            return resourceId > 0 ? activity.getResources().getDimensionPixelSize(resourceId) : 0;
        }

        /**
         * Returns navigation bar (on bottom where system buttons are) common height.
         * Be aware that some devices have no software keyboard (check it by {@link #hasSoftKeys(Activity)}) but this method will return you
         * size like they are showed.
         *
         * @param activity Activity of navigation bar;
         * @return Height of navigation bar.
         */
        public static int getNavigationBarHeight(@NonNull final Activity activity) {
            if (hasSoftKeys(activity)) {
                final int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                return resourceId > 0 ? activity.getResources().getDimensionPixelSize(resourceId) : 0;
            }
            return 0;
        }

        /**
         * Returns if device has software keyboard at navigation bar or not.
         *
         * @param activity Activity of navigation bar;
         * @return True if software keyboard is showing at navigation bar.
         */
        //http://stackoverflow.com/questions/14853039/how-to-tell-whether-an-android-device-has-hard-keys/14871974#14871974
        public static boolean hasSoftKeys(@NonNull final Activity activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                final Display display = activity.getWindowManager().getDefaultDisplay();

                final DisplayMetrics realDisplayMetrics = new DisplayMetrics();
                display.getRealMetrics(realDisplayMetrics);

                final DisplayMetrics displayMetrics = new DisplayMetrics();
                display.getMetrics(displayMetrics);

                return (realDisplayMetrics.widthPixels - displayMetrics.widthPixels) > 0
                        || (realDisplayMetrics.heightPixels - displayMetrics.heightPixels) > 0;
            }

            final boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
            final boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !hasMenuKey && !hasBackKey;
        }

        /**
         * Returns if {@link Intent} could be handled by system.
         *
         * @param context {@link Context} of application;
         * @param intent  {@link Intent} to be handled;
         * @return True if there are handlers for {@link Intent} (e.g. browser could handle URI intent).
         */
        public static boolean isIntentAbleToHandle(@NonNull final Context context, @NonNull final Intent intent) {
            return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();
        }

        private OfActivities() {
        }

    }

    /**
     * Utilities methods related to views.
     */
    public static class OfViews {

        private static final int GENERATED_ID_THRESHOLD = 0x00FFFFFF;
        private static final AtomicInteger NEXT_GENERATED_ID = new AtomicInteger(1);

        /**
         * Generates unique ID for view. See android {@link View#generateViewId()}.
         *
         * @return Unique ID.
         */
        @IdRes
        public static int generateViewId() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return View.generateViewId();
            }
            int result = 0;
            boolean isGenerated = false;
            while (!isGenerated) {
                result = NEXT_GENERATED_ID.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > GENERATED_ID_THRESHOLD) {
                    newValue = 1; // Roll over to 1, not 0.
                }
                if (NEXT_GENERATED_ID.compareAndSet(result, newValue)) {
                    isGenerated = true;
                }
            }
            return result;
        }

        /**
         * Returns string representation of {@link View}'s ID.
         *
         * @param view {@link View} to get ID from;
         * @return Readable ID.
         */
        @NonNull
        public static String getViewIdString(@NonNull final View view) {
            try {
                return view.getResources().getResourceName(view.getId());
            } catch (final Resources.NotFoundException exception) {
                return String.valueOf(view.getId());
            }
        }

        private OfViews() {
        }

    }

}
