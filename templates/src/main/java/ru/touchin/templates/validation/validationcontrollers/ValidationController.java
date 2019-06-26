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

import java.io.Serializable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import ru.touchin.roboswag.core.utils.Optional;
import ru.touchin.templates.validation.ValidationState;
import ru.touchin.templates.validation.ViewWithError;
import ru.touchin.templates.validation.validators.Validator;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * This class holds information about related {@link Validator} class and how to connect model with view.
 */
public class ValidationController
        <TWrapperModel extends Serializable, TModel extends Serializable, TValidator extends Validator<TWrapperModel, TModel>> {

    @NonNull
    private final TValidator validator;

    public ValidationController(@NonNull final TValidator validator) {
        this.validator = validator;
    }

    @NonNull
    public TValidator getValidator() {
        return validator;
    }

    /**
     * Bind to this observable to connect view and model. If you provide first argument (viewStateObservable) - the connection would be two-way.
     * If not - one-way. This method changes updates view with current {@link ValidationState}.
     *
     * @param viewStateObservable input view state {@link Observable}.
     *                            Eg it can be observable with input text from the {@link android.widget.EditText}
     * @param updateViewAction    action that updates current state of the bounded view.
     * @param viewWithError       view that implements {@link ViewWithError} interface and could reacts to the validation errors.
     * @return observable without any concrete type. Simply subscribe to this method to make it works.
     */
    @NonNull
    public Observable<?> modelAndViewUpdating(@Nullable final Observable<TWrapperModel> viewStateObservable,
                                              @NonNull final Consumer<Optional<TWrapperModel>> updateViewAction,
                                              @NonNull final ViewWithError viewWithError) {

        final Observable<?> stateObservable = viewStateObservable != null
                ? viewStateObservable.doOnNext(flag -> getValidator().getWrapperModel().set(flag))
                : Observable.empty();
        return Observable
                .merge(getValidator().getWrapperModel().observe()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(updateViewAction),
                        getValidator().getValidationState().observe()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(validationState -> {
                                    if (!showError(validationState)) {
                                        viewWithError.hideError();
                                    } else {
                                        viewWithError.showError(validationState);
                                    }
                                }),
                        stateObservable);
    }

    /**
     * Helper function to check if validation state in error state ot not
     *
     * @param validationState the state you want to check for the errors.
     * @return true if validation state is in error and false otherwise.
     */
    protected boolean showError(@NonNull final ValidationState validationState) {
        return !validationState.equals(ValidationState.VALID) && !validationState.equals(ValidationState.INITIAL);
    }

}