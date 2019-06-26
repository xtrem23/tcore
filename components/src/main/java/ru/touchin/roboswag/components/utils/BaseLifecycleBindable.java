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
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.BehaviorSubject;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 18/04/16.
 * Simple implementation of {@link LifecycleBindable}. Could be used to not implement interface but use such object inside.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class BaseLifecycleBindable implements LifecycleBindable {

    private static final String UNTIL_DESTROY_METHOD = "untilDestroy";
    private static final String UNTIL_STOP_METHOD = "untilStop";

    @NonNull
    private final BehaviorSubject<Boolean> isCreatedSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isStartedSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> isInAfterSaving = BehaviorSubject.createDefault(false);

    /**
     * Call it on parent's onCreate method.
     */
    public void onCreate() {
        isCreatedSubject.onNext(true);
    }

    /**
     * Call it on parent's onStart method.
     */
    public void onStart() {
        isStartedSubject.onNext(true);
    }

    /**
     * Call it on parent's onResume method.
     * It is needed as sometimes onSaveInstanceState() calling after onPause() with no onStop call. So lifecycle object going in stopped state.
     * In that case onResume will be called after onSaveInstanceState so lifecycle object is becoming started.
     */
    public void onResume() {
        isInAfterSaving.onNext(false);
    }

    /**
     * Call it on parent's onSaveInstanceState method.
     */
    public void onSaveInstanceState() {
        isInAfterSaving.onNext(true);
    }

    /**
     * Call it on parent's onStop method.
     */
    public void onStop() {
        isStartedSubject.onNext(false);
    }

    /**
     * Call it on parent's onDestroy method.
     */
    public void onDestroy() {
        isCreatedSubject.onNext(false);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(observable, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD), Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable, @NonNull final Consumer<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(observable, onNextAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD), Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable,
                                    @NonNull final Consumer<T> onNextAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return untilStop(observable, onNextAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Observable<T> observable,
                                    @NonNull final Consumer<T> onNextAction,
                                    @NonNull final Consumer<Throwable> onErrorAction,
                                    @NonNull final Action onCompletedAction) {
        return until(observable, isStartedSubject.map(started -> !started)
                        .delay(item -> isInAfterSaving.filter(inAfterSaving -> !inAfterSaving)),
                onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(single, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(single, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Single<T> single,
                                    @NonNull final Consumer<T> onSuccessAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return until(single.toObservable(), isStartedSubject.map(started -> !started)
                        .delay(item -> isInAfterSaving.filter(inAfterSaving -> !inAfterSaving)),
                onSuccessAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(completable, Functions.EMPTY_ACTION, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable,
                                @NonNull final Action onCompletedAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(completable, onCompletedAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public Disposable untilStop(@NonNull final Completable completable,
                                @NonNull final Action onCompletedAction,
                                @NonNull final Consumer<Throwable> onErrorAction) {
        return until(completable.toObservable(), isStartedSubject.map(started -> !started)
                        .delay(item -> isInAfterSaving.filter(inAfterSaving -> !inAfterSaving)),
                Functions.emptyConsumer(), onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Maybe<T> maybe) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(maybe, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Maybe<T> maybe, @NonNull final Consumer<T> onSuccessAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilStop(maybe, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_STOP_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilStop(@NonNull final Maybe<T> maybe,
                                    @NonNull final Consumer<T> onSuccessAction,
                                    @NonNull final Consumer<Throwable> onErrorAction) {
        return until(maybe.toObservable(), isStartedSubject.map(started -> !started), onSuccessAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(observable, Functions.emptyConsumer(),
                getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD), Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(observable, onNextAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD), Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return untilDestroy(observable, onNextAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Observable<T> observable,
                                       @NonNull final Consumer<T> onNextAction,
                                       @NonNull final Consumer<Throwable> onErrorAction,
                                       @NonNull final Action onCompletedAction) {
        return until(observable, isCreatedSubject.map(created -> !created), onNextAction, onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(single, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single, @NonNull final Consumer<T> onSuccessAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(single, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Single<T> single,
                                       @NonNull final Consumer<T> onSuccessAction,
                                       @NonNull final Consumer<Throwable> onErrorAction) {
        return until(single.toObservable(), isCreatedSubject.map(created -> !created), onSuccessAction, onErrorAction, Functions.EMPTY_ACTION);
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(completable, Functions.EMPTY_ACTION, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable, @NonNull final Action onCompletedAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(completable, onCompletedAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public Disposable untilDestroy(@NonNull final Completable completable,
                                   @NonNull final Action onCompletedAction,
                                   @NonNull final Consumer<Throwable> onErrorAction) {
        return until(completable.toObservable(), isCreatedSubject.map(created -> !created),
                Functions.emptyConsumer(), onErrorAction, onCompletedAction);
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(maybe, Functions.emptyConsumer(), getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
    public <T> Disposable untilDestroy(@NonNull final Maybe<T> maybe, @NonNull final Consumer<T> onSuccessAction) {
        final String codePoint = Lc.getCodePoint(this, 2);
        return untilDestroy(maybe, onSuccessAction, getActionThrowableForAssertion(codePoint, UNTIL_DESTROY_METHOD));
    }

    @NonNull
    @Override
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

    @NonNull
    private Consumer<Throwable> getActionThrowableForAssertion(@NonNull final String codePoint, @NonNull final String method) {
        return throwable -> Lc.assertion(new ShouldNotHappenException("Unexpected error on " + method + " at " + codePoint, throwable));
    }

}