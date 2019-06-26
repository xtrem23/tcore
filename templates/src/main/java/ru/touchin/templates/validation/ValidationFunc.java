package ru.touchin.templates.validation;

import android.support.annotation.NonNull;

import java.io.Serializable;

import io.reactivex.functions.Function;

/**
 * Created by Ilia Kurtov on 30/01/2017.
 * Simple interface that gets one parameter with {@link TInput} type as input and returns other type {@link TReturn} as a result.
 * Interface extends {@link Serializable} to survive after {@link ru.touchin.roboswag.components.navigation.AbstractState} recreation.
 * Created as a replace for {@link Function} because it needed to be {@link Serializable}
 *
 * @param <TInput>  input type.
 * @param <TReturn> return type.
 */
public interface ValidationFunc<TInput, TReturn> extends Serializable {

    /**
     * The method maps some {@link TInput} type argument into a {@link TReturn} type.
     *
     * @param input data;
     * @return mapped data into a {@link TReturn} type.
     * @throws Throwable for catching conversion errors.
     */
    @NonNull
    TReturn call(@NonNull final TInput input) throws Throwable;

}