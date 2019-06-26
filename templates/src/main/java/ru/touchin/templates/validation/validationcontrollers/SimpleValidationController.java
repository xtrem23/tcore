package ru.touchin.templates.validation.validationcontrollers;

import android.support.annotation.NonNull;

import java.io.Serializable;

import io.reactivex.Observable;
import ru.touchin.templates.validation.ValidationState;
import ru.touchin.templates.validation.validators.Validator;

/**
 * Created by Ilia Kurtov on 24/01/2017.
 * {@link ValidationController} for models that have the same modal as wrapper model. You can use it when you simply need to be sure
 * that user have selected some item and it is not null.
 *
 * @param <TModel>     type of the model.
 * @param <TValidator> corresponding {@link Validator}
 */
public class SimpleValidationController<TModel extends Serializable, TValidator extends Validator<TModel, TModel>>
        extends ValidationController<TModel, TModel, TValidator> {

    public SimpleValidationController(@NonNull final TValidator validator) {
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
        return Observable.combineLatest(activatedObservable,
                getValidator().getWrapperModel().observe(), (activated, model) -> {
                    if (model == null) {
                        return activated ? ValidationState.ERROR_NO_DESCRIPTION : ValidationState.INITIAL;
                    }
                    return ValidationState.VALID;
                })
                .doOnNext(validationState -> getValidator().getValidationState().set(validationState));
    }

}