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

package ru.touchin.templates.validation.validators;

import android.support.annotation.NonNull;

import java.io.Serializable;

import io.reactivex.Observable;
import io.reactivex.Single;
import ru.touchin.roboswag.core.observables.Changeable;
import ru.touchin.roboswag.core.observables.NonNullChangeable;
import ru.touchin.roboswag.core.utils.pairs.HalfNullablePair;
import ru.touchin.templates.validation.ValidationState;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * This class holds information about current state of the object - {@link #getWrapperModel()}(that should be connected to some view),
 * current error state {@link #getValidationState()}. Also you need to provide a {@link ValidationState} or class that extends it
 * as an empty state. Eg, if you need to show some text in your view to show user that this view shouldn't be empty - pass needed state
 * to the {@link #getValidationStateWhenEmpty()}
 * {@link TWrapperModel} is type of class that should be connected to its bounded view. {@link TModel} is type of class
 * that represent object that we need at the end. Eg, if we want to enter some digits to {@link android.widget.EditText}
 * and get {@link java.util.Date} as a result - {@link CharSequence} or {@link String} should be the {@link TWrapperModel}
 * and {@link java.util.Date} would be the {@link TModel} type.
 */
public abstract class Validator<TWrapperModel extends Serializable, TModel extends Serializable>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final NonNullChangeable<ValidationState> validationState = new NonNullChangeable<>(ValidationState.INITIAL);
    @NonNull
    private final Changeable<TWrapperModel> wrapperModel = new Changeable<>(null);
    @NonNull
    private final NonNullChangeable<ValidationState> validationStateWhenEmpty = new NonNullChangeable<>(ValidationState.ERROR_NO_DESCRIPTION);

    /**
     * This method converts {@link TWrapperModel} into a {@link TModel}.
     *
     * @param wrapperModel - not null value that should be converted into a {@link TModel} object.
     * @return converted wrapperModel into a {@link TModel}.
     * @throws Throwable for the cases when converting cannot be processed.
     */
    @NonNull
    protected abstract TModel convertWrapperModelToModel(@NonNull final TWrapperModel wrapperModel) throws Throwable;

    /**
     * Call this method to get {@link Changeable} with {@link TWrapperModel} inside it that should be connected to its bounded view.
     *
     * @return {@link Changeable} with {@link TWrapperModel}.
     */
    @NonNull
    public Changeable<TWrapperModel> getWrapperModel() {
        return wrapperModel;
    }

    /**
     * Returns current {@link ValidationState} or its successor. Needed to connect with bounded view and react to this state changes.
     *
     * @return current validation state.
     */
    @NonNull
    public NonNullChangeable<ValidationState> getValidationState() {
        return validationState;
    }

    /**
     * This method needed to get {@link ValidationState} that needed to be shown when bounded view is empty and you need to show to user reminder,
     * that he or she needs to fill this view.
     *
     * @return {@link ValidationState} that should be shown for an empty field.
     */
    @NonNull
    public NonNullChangeable<ValidationState> getValidationStateWhenEmpty() {
        return validationStateWhenEmpty;
    }

    /**
     * Validates {@link TWrapperModel} and returns {@link Single} with {@link HalfNullablePair} of final state and resulting model.
     *
     * @param wrapperModel - not null value that should be validated.
     * @return pair with final {@link ValidationState} that is always not null and a model that we get after converting the {@link TWrapperModel}.
     * Model can be null if validation fails.
     */
    @NonNull
    public abstract Observable<HalfNullablePair<ValidationState, TModel>> fullValidateAndGetModel(@NonNull final TWrapperModel wrapperModel);

}