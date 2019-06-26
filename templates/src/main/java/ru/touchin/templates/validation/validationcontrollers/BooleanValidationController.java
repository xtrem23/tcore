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

import io.reactivex.Observable;
import ru.touchin.templates.validation.ValidationState;
import ru.touchin.templates.validation.validators.SameTypeValidator;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * {@link ValidationController} for {@link Boolean} models. Eg if you have some flag that should be bounded to checkbox.
 */
public class BooleanValidationController extends ValidationController<Boolean, Boolean, SameTypeValidator<Boolean>> {

    public BooleanValidationController(@NonNull final SameTypeValidator<Boolean> validator) {
        super(validator);
    }

    /**
     * This method validates bounded view.
     *
     * @param activatedObservable emits true when we need to show error on empty fields. Eg when user clicks on Done button but he missed some
     *                            necessary fields to fill.
     * @return observable without any concrete type. Simply subscribe to this method to make it works.
     */
    @NonNull
    public Observable<?> validation(@NonNull final Observable<Boolean> activatedObservable) {
        return Observable.combineLatest(activatedObservable, getValidator().getWrapperModel().observe(),
                (activated, flag) -> {
                    final boolean selected = flag.get() == null ? false : flag.get();
                    if (activated && !selected) {
                        return ValidationState.ERROR_NO_DESCRIPTION;
                    } else if (!activated && !selected) {
                        return ValidationState.INITIAL;
                    }
                    return ValidationState.VALID;
                })
                .doOnNext(validationState -> getValidator().getValidationState().set(validationState));
    }

}