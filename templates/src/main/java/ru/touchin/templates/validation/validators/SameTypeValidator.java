package ru.touchin.templates.validation.validators;


import android.support.annotation.NonNull;

import java.io.Serializable;

import io.reactivex.Observable;
import ru.touchin.roboswag.core.utils.pairs.HalfNullablePair;
import ru.touchin.templates.validation.ValidationState;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * Class that simplifies work with {@link Validator}'s that have the same wrapper model and model type.
 *
 * @param <TModel> model that should be bounded with a view.
 */
public class SameTypeValidator<TModel extends Serializable> extends Validator<TModel, TModel> {

    /**
     * Simply returns the same model without any converting.
     *
     * @param wrapperModel input model.
     * @return the same model as input parameter.
     * @throws Throwable - in this case no throwable would be thrown.
     */
    @NonNull
    @Override
    protected TModel convertWrapperModelToModel(@NonNull final TModel wrapperModel)
            throws Throwable {
        return wrapperModel;
    }

    /**
     * Validates {@link TModel} and returns {@link Observable} with {@link HalfNullablePair} of final state and resulting model.
     *
     * @param wrapperModel - not null value that should be validated.
     * @return pair with final {@link ValidationState} that is always not null and a model that we get after converting the {@link TModel}.
     * Model can be null if validation fails.
     */
    @NonNull
    @Override
    public Observable<HalfNullablePair<ValidationState, TModel>> fullValidateAndGetModel(@NonNull final TModel wrapperModel) {
        return Observable.just(new HalfNullablePair<>(ValidationState.VALID, wrapperModel));
    }

}
