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

package ru.touchin.templates.socket;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.templates.ApiModel;

/**
 * Created by Gavriil Sitnikov on 29/02/16.
 * Base class to realize socket.io logic into your project.
 */
public abstract class SocketConnection {

    @NonNull
    private final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    @NonNull
    private final Map<SocketEvent, Observable> messagesObservableCache = new HashMap<>();
    @NonNull
    private final Observable<Pair<Socket, State>> socketObservable = createSocketObservable();
    private final boolean autoConnectOnAnySubscription;

    public SocketConnection(final boolean autoConnectOnAnySubscription) {
        this.autoConnectOnAnySubscription = autoConnectOnAnySubscription;
    }

    @NonNull
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Returns {@link Observable} that creates socket connection and connects/disconnects by subscription state.
     *
     * @return Socket {@link Observable}.
     */
    @NonNull
    protected Observable<Socket> getSocket() {
        return socketObservable
                .map(pair -> pair.first)
                .distinctUntilChanged();
    }

    /**
     * Creates socket.
     *
     * @return New socket.
     * @throws Exception Exception throwing during socket creation.
     */
    @NonNull
    protected abstract Socket createSocket() throws Exception;

    @NonNull
    private Observable<Pair<Socket, State>> createSocketObservable() {
        return Observable
                .fromCallable(this::createSocket)
                .switchMap(socket -> Observable
                        .<Pair<Socket, State>>create(emitter -> {
                            socket.on(Socket.EVENT_CONNECT, args -> emitter.onNext(new Pair<>(socket, State.CONNECTED)));
                            socket.on(Socket.EVENT_CONNECTING, args -> emitter.onNext(new Pair<>(socket, State.CONNECTING)));
                            socket.on(Socket.EVENT_CONNECT_ERROR, args -> emitter.onNext(new Pair<>(socket, State.CONNECTION_ERROR)));
                            socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> emitter.onNext(new Pair<>(socket, State.CONNECTION_ERROR)));
                            socket.on(Socket.EVENT_DISCONNECT, args -> emitter.onNext(new Pair<>(socket, State.DISCONNECTED)));
                            socket.on(Socket.EVENT_RECONNECT_ATTEMPT, args -> emitter.onNext(new Pair<>(socket, State.CONNECTING)));
                            socket.on(Socket.EVENT_RECONNECTING, args -> emitter.onNext(new Pair<>(socket, State.CONNECTING)));
                            socket.on(Socket.EVENT_RECONNECT, args -> emitter.onNext(new Pair<>(socket, State.CONNECTED)));
                            socket.on(Socket.EVENT_RECONNECT_ERROR, args -> emitter.onNext(new Pair<>(socket, State.CONNECTION_ERROR)));
                            socket.on(Socket.EVENT_RECONNECT_FAILED, args -> emitter.onNext(new Pair<>(socket, State.CONNECTION_ERROR)));
                            emitter.onNext(new Pair<>(socket, State.DISCONNECTED));
                        })
                        .distinctUntilChanged()
                        .doOnSubscribe(disposable -> {
                            if (autoConnectOnAnySubscription) {
                                socket.connect();
                            }
                        })
                        .doOnDispose(() -> {
                            if (autoConnectOnAnySubscription) {
                                socket.disconnect();
                            }
                        }))
                .subscribeOn(scheduler)
                .replay(1)
                .refCount();
    }

    /**
     * Returns {@link Observable} to observe socket state.
     *
     * @return {@link Observable} to observe socket state.
     */
    @NonNull
    public Observable<State> observeSocketState() {
        return socketObservable.map(pair -> pair.second);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    //unchecked: it's OK as we are caching raw observables
    protected <T> Observable<T> observeEvent(@NonNull final SocketEvent<T> socketEvent) {
        return Observable.switchOnNext(Observable
                .fromCallable(() -> {
                    Observable<T> result = (Observable<T>) messagesObservableCache.get(socketEvent);
                    if (result == null) {
                        result = getSocket()
                                .switchMap(socket -> Observable
                                        .<T>create(emitter -> socket.on(socketEvent.getName(), new SocketListener<>(socketEvent, emitter::onNext)))
                                        .unsubscribeOn(scheduler)
                                        .doOnDispose(() -> {
                                            socket.off(socketEvent.getName());
                                            messagesObservableCache.remove(socketEvent);
                                        }))
                                .publish()
                                .refCount();
                        messagesObservableCache.put(socketEvent, result);
                    }
                    return result;
                })
                .subscribeOn(scheduler));
    }

    /**
     * State of socket connection.
     */
    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECTION_ERROR
    }

    /**
     * Interface to listen socket messages.
     *
     * @param <TMessage> Type of socket message.
     */
    public static class SocketListener<TMessage> implements Emitter.Listener {

        @NonNull
        private final SocketEvent<TMessage> socketEvent;
        @NonNull
        private final Consumer<TMessage> onMessageAction;

        public SocketListener(@NonNull final SocketEvent<TMessage> socketEvent, @NonNull final Consumer<TMessage> onMessageAction) {
            this.socketEvent = socketEvent;
            this.onMessageAction = onMessageAction;
        }

        @Override
        public void call(@Nullable final Object... args) {
            if (args == null || args[0] == null) {
                return;
            }
            try {
                final byte[] response = args[0] instanceof byte[] ? (byte[]) args[0] : args[0].toString().getBytes();
                final TMessage message = socketEvent.parse(response);
                if (socketEvent.getEventDataHandler() != null) {
                    socketEvent.getEventDataHandler().handleMessage(message);
                }
                onMessageAction.accept(message);
            } catch (final RuntimeException throwable) {
                Lc.assertion(throwable);
            } catch (final JsonProcessingException exception) {
                Lc.assertion(exception);
            } catch (final ApiModel.ValidationException exception) {
                Lc.assertion(exception);
            } catch (final Exception exception) {
                Lc.e(exception, "Socket processing error");
            }
        }

    }

}
