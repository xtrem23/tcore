/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.roboswag.components.utils;

import android.support.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Emitter;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by Gavriil Sitnikov on 15/04/16.
 * Interface that should be implemented by lifecycle-based elements ({@link android.app.Activity}, {@link android.support.v4.app.Fragment} etc.)
 * to not manually manage subscriptions.
 * Use {@link #untilStop(Observable)} method to subscribe to observable where you want and unsubscribe onStop.
 * Use {@link #untilDestroy(Observable)} method to subscribe to observable where you want and unsubscribe onDestroy.
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface LifecycleBindable {

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable {@link Observable} to subscribe until onStop;
     * @param <T>        Type of emitted by observable items;
     * @return {@link Disposable} which will unsubscribes from observable onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Observable<T> observable);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction on every emitted item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   {@link Observable} to subscribe until onStop;
     * @param onNextAction Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param <T>          Type of emitted by observable items;
     * @return {@link Disposable} which will unsubscribes from observable onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Observable<T> observable, @NonNull Consumer<T> onNextAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction and onErrorAction on observable events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    {@link Observable} to subscribe until onStop;
     * @param onNextAction  Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param onErrorAction Action which will raise on every {@link Emitter#onError(Throwable)} throwable;
     * @param <T>           Type of emitted by observable items;
     * @return {@link Disposable} which will unsubscribes from observable onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Observable<T> observable, @NonNull Consumer<T> onNextAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onStop.
     * It is automatically subscribing to the observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable        {@link Observable} to subscribe until onStop;
     * @param onNextAction      Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param onErrorAction     Action which will raise on every {@link Emitter#onError(Throwable)} throwable;
     * @param onCompletedAction Action which will raise at {@link Emitter#onComplete()} on completion of observable;
     * @param <T>               Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Observable<T> observable,
                             @NonNull Consumer<T> onNextAction, @NonNull Consumer<Throwable> onErrorAction, @NonNull Action onCompletedAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single {@link Single} to subscribe until onStop;
     * @param <T>    Type of emitted by single item;
     * @return {@link Disposable} which will unsubscribes from single onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Single<T> single);

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single and calls onSuccessAction on the emitted item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onStop;
     * @param onSuccessAction Action which will raise on every {@link SingleEmitter#onSuccess(Object)} item;
     * @param <T>             Type of emitted by single item;
     * @return {@link Disposable} which will unsubscribes from single onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Single<T> single, @NonNull Consumer<T> onSuccessAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onStop.
     * It is automatically subscribing to the single and calls onSuccessAction and onErrorAction on single events.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onStop;
     * @param onSuccessAction Action which will raise on every {@link SingleEmitter#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link SingleEmitter#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Single<T> single, @NonNull Consumer<T> onSuccessAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable {@link Completable} to subscribe until onStop;
     * @return {@link Disposable} which will unsubscribes from completable onStop.
     */
    @NonNull
    Disposable untilStop(@NonNull Completable completable);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable and calls onCompletedAction on completable item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onStop;
     * @param onCompletedAction Action which will raise at {@link CompletableEmitter#onComplete()} on completion of observable;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onStop.
     */
    @NonNull
    Disposable untilStop(@NonNull Completable completable, @NonNull Action onCompletedAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onStop.
     * It is automatically subscribing to the completable and calls onCompletedAction and onErrorAction on completable item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onStop;
     * @param onCompletedAction Action which will raise at {@link CompletableEmitter#onComplete()} on completion of observable;
     * @param onErrorAction     Action which will raise on every {@link CompletableEmitter#onError(Throwable)} throwable;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onStop.
     */
    @NonNull
    Disposable untilStop(@NonNull Completable completable, @NonNull Action onCompletedAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onStop.
     * It is automatically subscribing to the maybe.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe {@link Maybe} to subscribe until onStop;
     * @return {@link Disposable} which will unsubscribes from completable onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Maybe<T> maybe);

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onStop.
     * It is automatically subscribing to the maybe and calls onCompletedAction on maybe item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe           {@link Maybe} to subscribe until onStop;
     * @param onSuccessAction Action which will raise at {@link MaybeEmitter#onSuccess(Object)} ()} on completion of observable;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Maybe<T> maybe, @NonNull Consumer<T> onSuccessAction);

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onStop.
     * It is automatically subscribing to the maybe and calls onCompletedAction and onErrorAction on maybe item.
     * Usually it is using to stop requests/execution while element is off or to not do illegal actions after onStop like fragment's stack changing.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe           {@link Maybe} to subscribe until onStop;
     * @param onSuccessAction Action which will raise at {@link MaybeEmitter#onSuccess(Object)} ()} on completion of observable;
     * @param onErrorAction   Action which will raise on every {@link MaybeEmitter#onError(Throwable)} throwable;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onStop.
     */
    @NonNull
    <T> Disposable untilStop(@NonNull Maybe<T> maybe, @NonNull Consumer<T> onSuccessAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable {@link Observable} to subscribe until onDestroy;
     * @param <T>        Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Observable<T> observable);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction on every emitted item.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable   {@link Observable} to subscribe until onDestroy;
     * @param onNextAction Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param <T>          Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Observable<T> observable, @NonNull Consumer<T> onNextAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction and onErrorAction on observable events.
     * Don't forget to process errors if observable can emit them.
     *
     * @param observable    {@link Observable} to subscribe until onDestroy;
     * @param onNextAction  Action which will raise on every {@link Emitter#onNext(Object)} item;
     * @param onErrorAction Action which will raise on every {@link Emitter#onError(Throwable)} throwable;
     * @param <T>           Type of emitted by observable items;
     * @return {@link Disposable} which is wrapping source observable to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Observable<T> observable, @NonNull Consumer<T> onNextAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that observable won't be subscribed after onDestroy.
     * It is automatically subscribing to the observable and calls onNextAction, onErrorAction and onCompletedAction on observable events.
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
    <T> Disposable untilDestroy(@NonNull Observable<T> observable,
                                @NonNull Consumer<T> onNextAction, @NonNull Consumer<Throwable> onErrorAction, @NonNull Action onCompletedAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single.
     * Don't forget to process errors if single can emit them.
     *
     * @param single {@link Single} to subscribe until onDestroy;
     * @param <T>    Type of emitted by single items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Single<T> single);

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single and calls onSuccessAction on every emitted item.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every {@link SingleEmitter#onSuccess(Object)} item;
     * @param <T>             Type of emitted by single items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Single<T> single, @NonNull Consumer<T> onSuccessAction);

    /**
     * Method should be used to guarantee that single won't be subscribed after onDestroy.
     * It is automatically subscribing to the single and calls onSuccessAction and onErrorAction on single events.
     * Don't forget to process errors if single can emit them.
     *
     * @param single          {@link Single} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every {@link SingleEmitter#onSuccess(Object)} item;
     * @param onErrorAction   Action which will raise on every {@link SingleEmitter#onError(Throwable)} throwable;
     * @param <T>             Type of emitted by single items;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Single<T> single, @NonNull Consumer<T> onSuccessAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable {@link Completable} to subscribe until onDestroy;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    Disposable untilDestroy(@NonNull Completable completable);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable and calls onCompletedAction on completable item.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every {@link CompletableEmitter#onComplete()} item;
     * @return {@link Disposable} which is wrapping source single to unsubscribe from it onDestroy.
     */
    @NonNull
    Disposable untilDestroy(@NonNull Completable completable, @NonNull Action onCompletedAction);

    /**
     * Method should be used to guarantee that completable won't be subscribed after onDestroy.
     * It is automatically subscribing to the completable and calls onCompletedAction and onErrorAction on completable events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param completable       {@link Completable} to subscribe until onDestroy;
     * @param onCompletedAction Action which will raise on every {@link CompletableEmitter#onComplete()} item;
     * @param onErrorAction     Action which will raise on every {@link CompletableEmitter#onError(Throwable)} throwable;
     * @return {@link Disposable} which is wrapping source completable to unsubscribe from it onDestroy.
     */
    @NonNull
    Disposable untilDestroy(@NonNull Completable completable, @NonNull Action onCompletedAction, @NonNull Consumer<Throwable> onErrorAction);

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to the maybe.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe {@link Maybe} to subscribe until onDestroy;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Maybe<T> maybe);

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to the maybe and calls onCompletedAction on maybe item.
     * Don't forget to process errors if maybe can emit them.
     *
     * @param maybe           {@link Maybe} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every {@link MaybeEmitter#onSuccess(Object)} ()} item;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Maybe<T> maybe, @NonNull Consumer<T> onSuccessAction);

    /**
     * Method should be used to guarantee that maybe won't be subscribed after onDestroy.
     * It is automatically subscribing to the maybe and calls onSuccessAction and onErrorAction on maybe events.
     * Don't forget to process errors if completable can emit them.
     *
     * @param maybe           {@link Maybe} to subscribe until onDestroy;
     * @param onSuccessAction Action which will raise on every {@link MaybeEmitter#onSuccess(Object)} ()} item;
     * @param onErrorAction   Action which will raise on every {@link MaybeEmitter#onError(Throwable)} throwable;
     * @return {@link Disposable} which is wrapping source maybe to unsubscribe from it onDestroy.
     */
    @NonNull
    <T> Disposable untilDestroy(@NonNull Maybe<T> maybe, @NonNull Consumer<T> onSuccessAction, @NonNull Consumer<Throwable> onErrorAction);

}