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

package ru.touchin.templates;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Emitter;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.BehaviorSubject;
import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.utils.Logic;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ServiceBinder;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 10/01/17.
 * Base class of service to extends for Touch Instinct related projects.
 * It contains {@link Logic} and could bind to it through {@link #untilDestroy(Observable)} methods.
 *
 * @param <TLogic> Type of application's {@link Logic}.
 */
public abstract class TouchinService<TLogic extends Logic> extends Service {

    //it is needed to hold strong reference to logic
    private TLogic reference;
    @NonNull
    private final Handler postHandler = new Handler();
    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create();

    /**
     * It should return specific class where all logic will be.
     *
     * @return Returns class of specific {@link Logic}.
     */
    @NonNull
    protected abstract Class<TLogic> getLogicClass();

    @Override
    public void onCreate() {
        super.onCreate();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        postHandler.post(() -> isCreatedSubject.onNext(true));
    }

    /**
     * Returns (and creates if needed) application's logic.
     *
     * @return Object which represents application's logic.
     */
    @NonNull
    protected TLogic getLogic() {
        synchronized (ViewControllerActivity.class) {
            if (reference == null) {
                reference = Logic.getInstance(this, getLogicClass());
            }
        }
        return reference;
    }

    @SuppressWarnings("CPD-START")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable {@link Observable} to subscribe until onDestroy;
     * @param <T>        Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable) {
        return untilDestroy(observable, Functions.emptyConsumer(), getActionThrowableForAssertion(Lc.getCodePoint(this, 1)), Functions.EMPTY_ACTION);
    }

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable and calls onNextAction on every emitted item.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   {@link Observable} to subscribe until onDestroy;
     * @param onNextAction Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param <T>          Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction) {
        return untilDestroy(observable, onNextAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)), Functions.EMPTY_ACTION);
    }

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable and calls onNextAction and onErrorAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    {@link Observable} to subscribe until onDestroy;
     * @param onNextAction  Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param onErrorAction Action which will raise on every {@link Emitter#onError(Throwable)} throwable;
     * @param <T>           Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return untilDestroy(observable, onNextAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable        {@link Observable} to subscribe until onDestroy;
     * @param onNextAction      Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param onErrorAction     Action which will raise on every {@link Emitter#onError(Throwable)} throwable;
     * @param onCompletedAction Action which will raise at {@link Emitter#onComplete()} on completion of observable;
     * @param <T>               Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction,
                                       @NonNull final Action onCompletedAction) {
        return until(observable, isCreatedSubject.map(created -> !created), onNextAction, onErrorAction, onCompletedAction);
    }

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to single.
     * Don't forget to process errors if single can emit them.
     *
     * @param single {@link Single} to subscribe until onDestroy;
     * @param <T>    Type of emitted by single items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Single<T> single) {
        return untilDestroy(single, Functions.emptyConsumer(), getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to single and calls onSuccessAction on emitted item.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on {@link SingleEmitter#onSuccess(Object)} item;
     * @param <T>             Type of emitted by single items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        return untilDestroy(single, onSuccessAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to single and calls onSuccessAction and onErrorAction on single events.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on {@link SingleEmitter#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link SingleEmitter#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by single items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Single<T> single,
                                       @NonNull final Consumer<T> onSuccessAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return until(single.toObservable(), isCreatedSubject.map(created -> !created), onSuccessAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to completable.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable {@link Completable} to subscribe until onDestroy;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    public Disposable untilDestroy(@NonNull final Completable completable) {
        return untilDestroy(completable, Functions.EMPTY_ACTION, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to completable and calls onCompletedAction on complete.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every {@link CompletableEmitter#onComplete()} item;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    public Disposable untilDestroy(@NonNull final Completable completable, @NonNull final Action onCompletedAction) {
        return untilDestroy(completable, onCompletedAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to completable and calls onCompletedAction and onErrorAction on completable events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Single} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on {@link CompletableEmitter#onComplete()} item;
     * @param onErrorAction     Action which will raise on every {@link CompletableEmitter#onError(Throwable)} throwable;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    public Disposable untilDestroy(@NonNull final Completable completable,
                                   @NonNull final Action onCompletedAction,
                                   @NonNull final Consumer<Throwable> onErrorAction) {
        return until(completable.toObservable(), isCreatedSubject.map(created -> !created),
                Functions.emptyConsumer(), onErrorAction, onCompletedAction);
    }

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to maybe.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe {@link Maybe} to subscribe until onDestroy;
     * @param <T>   Type of emitted by maybe items;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe) {
        return untilDestroy(maybe, Functions.emptyConsumer(), getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to maybe and calls onSuccessAction on emitted item.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe           {@link Maybe} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on {@link MaybeEmitter#onSuccess(Object)} item;
     * @param <T>             Type of emitted by maybe items;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe, @NonNull final Consumer<T> onSuccessAction) {
        return untilDestroy(maybe, onSuccessAction, getActionThrowableForAssertion(Lc.getCodePoint(this, 1)));
    }

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to maybe and calls onSuccessAction and onErrorAction on maybe events.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe           {@link Maybe} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on {@link MaybeEmitter#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link MaybeEmitter#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by maybe items;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe,
                                       @NonNull final Consumer<T> onSuccessAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return until(maybe.toObservable(), isCreatedSubject.map(created -> !created), onSuccessAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    @NonNull
    private <T> Disposable until(@NonNull final Observable<T> observable,
                                 @NonNull final Observable<Boolean> conditionSubject,
                                 @NonNull final Consumer<T> onNextAction,
                                 @NonNull final Consumer<Throwable> onErrorAction,
                                 @NonNull final Action onCompletedAction) {
        final Observable<T> actualObservable;
        if (onNextAction == Functions.emptyConsumer() && onErrorAction == (Consumer) Functions.emptyConsumer()
                && onCompletedAction == Functions.EMPTY_ACTION) {
            actualObservable = observable;
        } else {
            actualObservable = observable.observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(onCompletedAction)
                    .doOnNext(onNextAction)
                    .doOnError(onErrorAction);
        }

        return isCreatedSubject.firstOrError()
                .flatMapObservable(created -> created ? actualObservable : Observable.empty())
                .takeUntil(conditionSubject.filter(condition -> condition))
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RuntimeException) {
                        Lc.assertion(throwable);
                    }
                    return Observable.empty();
                })
                .subscribe();
    }

    @SuppressWarnings("CPD-END")
    //CPD: it is same as in other implementation based on BaseLifecycleBindable
    @NonNull
    @Override
    public IBinder onBind(@NonNull final Intent intent) {
        return new ServiceBinder<>(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
    }

    @Override
    public void onDestroy() {
        UiUtils.UI_LIFECYCLE_LC_GROUP.i(Lc.getCodePoint(this));
        postHandler.removeCallbacksAndMessages(null);
        isCreatedSubject.onNext(false);
        super.onDestroy();
    }

    @NonNull
    private Consumer<Throwable> getActionThrowableForAssertion(@NonNull final String codePoint) {
        return throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on untilDestroy at " + codePoint, throwable));
    }

}

