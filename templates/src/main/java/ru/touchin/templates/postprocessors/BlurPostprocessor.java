/**
 * Copyright (C) 2015 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.touchin.templates.postprocessors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.support.annotation.NonNull;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.request.BasePostprocessor;

import ru.touchin.roboswag.components.utils.images.BlurUtils;

public class BlurPostprocessor extends BasePostprocessor {

    private static final int MAX_RADIUS = 25;
    private static final int DEFAULT_DOWN_SAMPLING = 1;

    @NonNull
    private final Context context;
    private final int radius;
    private final int sampling;

    public BlurPostprocessor(@NonNull final Context context) {
        this(context, MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }

    public BlurPostprocessor(@NonNull final Context context, final int radius) {
        this(context, radius, DEFAULT_DOWN_SAMPLING);
    }

    public BlurPostprocessor(@NonNull final Context context, final int radius, final int sampling) {
        super();
        this.context = context.getApplicationContext();
        this.radius = radius;
        this.sampling = sampling;
    }

    @Override
    public void process(@NonNull final Bitmap dest, @NonNull final Bitmap source) {

        final int width = source.getWidth();
        final int height = source.getHeight();
        final int scaledWidth = width / sampling;
        final int scaledHeight = height / sampling;

        Bitmap blurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(blurredBitmap);
        canvas.scale(1 / (float) sampling, 1 / (float) sampling);
        final Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, 0, 0, paint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                blurredBitmap = BlurUtils.blurRenderscript(context, blurredBitmap, radius);
            } catch (final RSRuntimeException exception) {
                blurredBitmap = BlurUtils.blurFast(blurredBitmap, radius, true);
            }
        } else {
            blurredBitmap = BlurUtils.blurFast(blurredBitmap, radius, true);
        }

        final Bitmap scaledBitmap =
                Bitmap.createScaledBitmap(blurredBitmap, dest.getWidth(), dest.getHeight(), true);
        if (blurredBitmap != null) {
            blurredBitmap.recycle();

        }
        super.process(dest, scaledBitmap);
    }

    @Override
    @NonNull
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    @NonNull
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey("radius=" + radius + ",sampling=" + sampling);
    }

}