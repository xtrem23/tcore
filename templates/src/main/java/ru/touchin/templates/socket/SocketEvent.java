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

import java.io.IOException;

/**
 * Created by Gavriil Sitnikov on 29/02/16.
 * Object that represents event on socket connection by name (e.g. messages/get).
 *
 * @param <TMessage> Type of message coming from socket by event.
 */
public abstract class SocketEvent<TMessage> {

    @NonNull
    private final String name;
    @NonNull
    private final Class<TMessage> messageClass;
    @Nullable
    private final SocketMessageHandler<TMessage> eventDataHandler;

    public SocketEvent(@NonNull final String name, @NonNull final Class<TMessage> messageClass,
                       @Nullable final SocketMessageHandler<TMessage> eventDataHandler) {
        this.name = name;
        this.messageClass = messageClass;
        this.eventDataHandler = eventDataHandler;
    }

    /**
     * Returns name of event.
     *
     * @return Name of event.
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns message class;
     *
     * @return message class.
     */
    @NonNull
    public Class<TMessage> getMessageClass() {
        return messageClass;
    }

    /**
     * Returns handler to handle message after response and parsing.
     *
     * @return Message handler.
     */
    @Nullable
    public SocketMessageHandler<TMessage> getEventDataHandler() {
        return eventDataHandler;
    }

    /**
     * Parses input string to message.
     *
     * @param data Input bytes;
     * @return Message object;
     * @throws IOException Exception during parsing.
     */
    @NonNull
    public abstract TMessage parse(@NonNull final byte[] data) throws IOException;

    @Override
    public boolean equals(@Nullable final Object object) {
        return object instanceof SocketEvent
                && ((SocketEvent) object).name.equals(name)
                && ((SocketEvent) object).messageClass.equals(messageClass);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + messageClass.hashCode();
    }

}
