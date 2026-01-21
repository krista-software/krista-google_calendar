/*
 * Google Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.kristasoft.extensions.essentials.collaboration.google_calendar.catalog.entity;

import app.krista.extension.impl.anno.*;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util.Constants;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.stream.Collectors;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion ="17a093a2-3059-4a0e-b441-d2248d456fdd")
@Entity(name = "Event Details", id = "localDomainEntity_b13aad88-786e-440a-8131-84351124a9cf", primaryKey = "Event Id", supportStore = false, options = {})
public class EventDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDetails.class);

    @Field.Text(name = "Event Id")
    public String eventId;

    @Field.Text(name = "Location")
    public String location;

    @Searchable
    @Field.Text(name = "Title")
    public String title;

    @Field(name = "Contacts", type = "Checkboxes", attributes = {@Attribute(name = "options", value = "[]"), })
    public Object contacts;

    @Field(name = "Details", type = "RichText", attributes = {@Attribute(name = "visualWidth", value = "L")})
    public String details;
    @Field.Date(name = "Date From", required = true, showHowManyDaysInViewer = 60, allowPast = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Long dateFrom;

    @Field.Date(name = "Date To", required = true, showHowManyDaysInViewer = 60, allowPast = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public Long dateTo;

    public static EventDetails getEventDetailsByEvent(Event event) {
        LOGGER.info("→ getEventDetailsByEvent: Processing event  {}", Constants.GSON.toJson(event));
        EventDetails details = new EventDetails();
        details.eventId = event.getId();
        details.dateFrom = extractDateValue(event.getStart());
        details.dateTo = extractDateValue(event.getEnd());
        details.details = event.getDescription();
        details.title = event.getSummary() != null ? event.getSummary() : "";

        details.location = event.getLocation();
        if (event.getAttendees() != null) {
            details.contacts = event.getAttendees().stream()
                    .map(EventAttendee::getEmail)
                    .collect(Collectors.toList());
            LOGGER.info("→ Contacts: {} attendees found", ((java.util.List<?>) details.contacts).size());
        } else {
            details.contacts = Collections.emptyList();
        }
        return details;
    }

    private static Long extractDateValue(EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return null;
        }
        if (eventDateTime.getDateTime() != null) {
            return eventDateTime.getDateTime().getValue();
        }
        if (eventDateTime.getDate() != null) {
            return eventDateTime.getDate().getValue();
        }
        return null;
    }

}