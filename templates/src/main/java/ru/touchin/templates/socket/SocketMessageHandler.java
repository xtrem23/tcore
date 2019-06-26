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

/**
 * Created by Gavriil Sitnikov on 29/02/16.
 * Interface to implement for objects which could handle message coming from socket.
 *
 * @param <TMessage> Type of message coming from socket.
 */
public interface SocketMessageHandler<TMessage> {

    /**
     * Method to handle message
     *
     * @param message Message to handle;
     * @return Result of handling message;
     * @throws Exception Throws during handling.
     */
    @NonNull
    TMessage handleMessage(@NonNull TMessage message) throws Exception;

}
