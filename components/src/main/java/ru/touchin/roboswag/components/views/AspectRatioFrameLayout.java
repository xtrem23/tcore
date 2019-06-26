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

package ru.touchin.roboswag.components.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ru.touchin.roboswag.components.R;


/**
 * Created by Gavriil Sitnikov on 01/07/14.
 * FrameLayout that holds specific aspect ratio sizes.
 * For example if aspect ratio equals 1.0 then this view will layout as square.
 */
public class AspectRatioFrameLayout extends FrameLayout {

    private static final float DEFAULT_ASPECT_RATIO = 1.0f;
    private static final float EPSILON = 0.0000001f;

    private float aspectRatio;
    private boolean wrapToContent;

    public AspectRatioFrameLayout(@NonNull final Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioFrameLayout(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        if (attrs == null) {
            wrapToContent = false;
            aspectRatio = DEFAULT_ASPECT_RATIO;
        } else {
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout);
            wrapToContent = typedArray.getBoolean(R.styleable.AspectRatioFrameLayout_wrapToContent, false);
            aspectRatio = typedArray.getFloat(R.styleable.AspectRatioFrameLayout_aspectRatio, DEFAULT_ASPECT_RATIO);
            typedArray.recycle();
        }
    }

    /* Returns aspect ratio of layout */
    public float getAspectRatio() {
        return aspectRatio;
    }

    /* Sets aspect ratio of layout */
    public void setAspectRatio(final float aspectRatio) {
        if (Math.abs(aspectRatio - this.aspectRatio) < EPSILON) {
            return;
        }

        this.aspectRatio = aspectRatio;
        requestLayout();
    }

    /* Returns if layout is wrapping to content but holds aspect ratio */

    /**
     * Returns if layout is wrapping to content but holds aspect ratio.
     * If it is true it means that minimum size of view will equals to maximum size of it's child (biggest width or height) depends on aspect ratio.
     * Else maximum size of view will equals to minimum available size which parent could give to this view depends on aspect ratio.
     *
     * @return True if wrapping to content.
     */
    public boolean isWrapToContent() {
        return wrapToContent;
    }

    /**
     * Sets if layout is wrapping to content but holds aspect ratio.
     *
     * @param wrapToContent True if wrapping to content.
     */
    public void setWrapToContent(final boolean wrapToContent) {
        if (wrapToContent == this.wrapToContent) {
            return;
        }

        this.wrapToContent = wrapToContent;
        requestLayout();
    }

    private void setMeasuredDimensionWithAspectOfLesser(final int measuredWidth, final int measuredHeight) {
        final float heightBasedOnMw = measuredWidth / aspectRatio;
        if (heightBasedOnMw > measuredHeight) {
            setMeasuredDimension((int) (measuredHeight * aspectRatio), measuredHeight);
        } else {
            setMeasuredDimension(measuredWidth, (int) heightBasedOnMw);
        }
    }

    private void setMeasuredDimensionWithAspectOfHigher(final int measuredWidth, final int measuredHeight) {
        final float heightBasedOnMw = measuredWidth / aspectRatio;
        if (heightBasedOnMw < measuredHeight) {
            setMeasuredDimension((int) (measuredHeight * aspectRatio), measuredHeight);
        } else {
            setMeasuredDimension(measuredWidth, (int) heightBasedOnMw);
        }
    }

    @NonNull
    private Point measureWrapChildren(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Point result = new Point();
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            child.measure(widthMeasureSpec, heightMeasureSpec);
            if (result.x < child.getMeasuredWidth()) {
                result.x = child.getMeasuredWidth();
            }
            if (result.y < child.getMeasuredHeight()) {
                result.y = child.getMeasuredHeight();
            }
        }
        return result;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (wrapToContent) {
            final Point bounds = measureWrapChildren(widthMeasureSpec, heightMeasureSpec);
            width = widthMode == MeasureSpec.UNSPECIFIED ? bounds.x : Math.min(bounds.x, width);
            height = heightMode == MeasureSpec.UNSPECIFIED ? bounds.y : Math.min(bounds.y, height);
        }

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                measureBothUnspecified(width, height);
            } else {
                measureOnlyUnspecifiedWidth(width, height);
            }
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            measureOnlyUnspecifiedHeight(width, height);
        } else {
            measureBothSpecified(width, height);
        }
    }

    private void measureBothSpecified(final int width, final int height) {
        if (wrapToContent) {
            setMeasuredDimensionWithAspectOfHigher(width, height);
        } else {
            setMeasuredDimensionWithAspectOfLesser(width, height);
        }
    }

    private void measureOnlyUnspecifiedHeight(final int width, final int height) {
        if (wrapToContent) {
            measureWrapToContent(width, height);
        } else {
            setMeasuredDimension(width, (int) (width / aspectRatio));
        }
    }

    private void measureWrapToContent(final int width, final int height) {
        if (width < (int) (height * aspectRatio)) {
            setMeasuredDimension((int) (height * aspectRatio), height);
        } else {
            setMeasuredDimension(width, (int) (width / aspectRatio));
        }
    }

    private void measureOnlyUnspecifiedWidth(final int width, final int height) {
        if (wrapToContent) {
            measureWrapToContent(width, height);
        } else {
            setMeasuredDimension((int) (height * aspectRatio), height);
        }
    }

    private void measureBothUnspecified(final int width, final int height) {
        if (wrapToContent) {
            setMeasuredDimensionWithAspectOfHigher(width, height);
        } else {
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            setMeasuredDimensionWithAspectOfLesser(metrics.widthPixels, metrics.heightPixels);
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            final int widthMeasureSpec;
            final int heightMeasureSpec;
            final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            final int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            switch (lp.width) {
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                    break;
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
                    break;
                default:
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                    break;
            }

            switch (lp.height) {
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                    break;
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
                    break;
                default:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                    break;
            }

            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

}