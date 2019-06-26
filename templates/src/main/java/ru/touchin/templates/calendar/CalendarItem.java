/*
 *  Copyright (c) 2016 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.templates.calendar;

/**
 * Created by Ilia Kurtov on 17/03/2016.
 * Interface for items for {@link CalendarAdapter}. Instead of storing data about all calendar cells separately,
 * it sores list of this items. CalendarItem represents range with the same calendar items.
 */
public interface CalendarItem {

    /**
     * Returns number of starting cell of this range.
     *
     * @return number of starting cell of this range.
     */
    int getStartRange();

    /**
     * Returns number of ending cell of this range.
     *
     * @return number of ending cell of this range.
     */
    int getEndRange();

}