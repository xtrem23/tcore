/*
 *  Copyright (c) 2016 Touch Instinct
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

package ru.touchin.templates.validation.validationcontrollers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;

import io.reactivex.Observable;
import ru.touchin.roboswag.core.utils.Optional;
import ru.touchin.roboswag.core.utils.pairs.NonNullPair;
import ru.touchin.templates.validation.ValidationState;
import ru.touchin.templates.validation.validators.EditTextValidator;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * ValidationController for {@link android.widget.EditText} views. It has method {@link #validation} that response
 * for validating view. To use this class properly, you need to subscribe to the {@link #modelAndViewUpdating}
 * and to the {@link #validation} methods.
 */
public class EditTextValidationController<TModel extends Serializable>
        extends ValidationController<String, TModel, EditTextValidator<TModel>> {

    private boolean showErrorOnFocusOut = true;

    public EditTextValidationController(@NonNull final EditTextValidator<TModel> validationWrapper) {
        super(validationWrapper);
    }

    /**
     * This method validates bounded view.
     *
     * @param focusOutObservable  that emits items when bounded view get focus in or out.
     * @param activatedObservable emits true when we need to show error on empty fields. Eg when user clicks on Done button but he missed some
     *                            necessary fields to fill.
     * @return observable without any concrete type. Simply subscribe to this method to make it works.
     */
    @NonNull
    public Observable<?> validation(@NonNull final Observable<Boolean> focusOutObservable, @NonNull final Observable<Boolean> activatedObservable) {
        return Observable.combineLatest(activatedObservable,
                getValidator().getWrapperModel().observe(),
                focusOutObservable,
                showErrorOnFocusOut ? getValidator().getShowFullCheck().observe() : Observable.just(false),
                this::getValidationPair)
                .switchMap(validationPair -> {
                    if (validationPair == null) {
                        return Observable.empty();
                    }
                    return validationPair.getSecond()
                            .doOnNext(validationState -> {
                                if (!validationPair.getFirst()) {
                                    getValidator().getShowFullCheck().set(showError(validationState));
                                }
                                getValidator().getValidationState().set(validationState);
                            });
                });
    }

    @Nullable
    private NonNullPair<Boolean, Observable<ValidationState>> getValidationPair(final boolean activated,
                                                                                @NonNull final Optional<String> optionalText,
                                                                                @Nullable final Boolean focusIn,
                                                                                final boolean showError) {
        final String text = optionalText.get();
        if (focusIn == null && TextUtils.isEmpty(text) && !activated && !showError) {
            return null;
        }
        final boolean focus = focusIn != null && focusIn;
        if (TextUtils.isEmpty(text)) {
            return new NonNullPair<>(focus, activated || showError
                    ? getValidator().getValidationStateWhenEmpty().observe()
                    : Observable.just(ValidationState.INITIAL));
        }
        if (!showError && focus) {
            return new NonNullPair<>(true, getValidator().primaryValidate(text));
        }
        return new NonNullPair<>(focus, getValidator().fullValidate(text));
    }

    /**
     * If we don't want to show error when focus is lost.
     *
     * @param showErrorOnFocusOut show an error or don't show an error.
     */
    public void setShowErrorOnFocusOut(final boolean showErrorOnFocusOut) {
        this.showErrorOnFocusOut = showErrorOnFocusOut;
    }

}