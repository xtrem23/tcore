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


import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * Basic class for validation states. If you need to have more states with more data in it -
 * create class that extends this class and don't forget to redefine {@link #equals(Object)} and {@link #hashCode()} methods.
 * Don't use same {@link #code} for different states.
 */
public class ValidationState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Initial state of validation. It indicates that no validation rules applied yet.
     */
    public static final ValidationState INITIAL = new ValidationState(-1);
    /**
     * Valid state.
     */
    public static final ValidationState VALID = new ValidationState(-2);
    /**
     * Error shows when model (e.g. DateTime) is failing on conversion from raw data (e.g. from String) for validation.
     */
    public static final ValidationState ERROR_CONVERSION = new ValidationState(-3);
    /**
     * Error shows when we don't need to show any description of error (e.g. just highlight input field with red color).
     */
    public static final ValidationState ERROR_NO_DESCRIPTION = new ValidationState(-4);

    private final int code;

    public ValidationState(final int code) {
        this.code = code;
    }

    /**
     * Returns unique code of the {@link ValidationState}.
     *
     * @return code or the ValidationState.
     */
    public int getCode() {
        return code;
    }

    /**
     * Don't forget to override this method!
     *
     * @param object that you want to compare.
     * @return true if objects equals and false otherwise.
     */
    @Override
    public boolean equals(@Nullable final Object object) {
        return this == object
                || !(object == null || getClass() != object.getClass()) && code == ((ValidationState) object).code;
    }

    @Override
    public int hashCode() {
        return 31 * code;
    }

}