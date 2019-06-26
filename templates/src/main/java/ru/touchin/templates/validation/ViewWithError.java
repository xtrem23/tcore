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

package ru.touchin.templates.validation;

import android.support.annotation.NonNull;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * Interface for views that must be validated. Have two states - show error or hide error.
 * You can provide your own Validation State to provide, eg string resource.
 * In this case use instanceOf to define what state do you have.
 */
public interface ViewWithError {

    /**
     * Hides the error when validation passes successful.
     */
    void hideError();

    /**
     * Shows error
     * Pass here error state.
     * It is not correct to pass here {@link ValidationState#VALID} or {@link ValidationState#INITIAL}
     *
     * @param validationState error state. Can be other than {@link ValidationState} if you have successor of base {@link ValidationState}.
     */
    void showError(@NonNull final ValidationState validationState);

}
