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

package app.kristasoft.extensions.essentials.collaboration.google_calendar.catalog;

import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.*;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.catalog.entity.EventDetails;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.MeetingAreaImpl;

import javax.inject.Inject;
import java.util.List;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "83226409-571b-4a55-a580-eb4fcb804246")
public class MeetingsArea {

    @Inject
    MeetingAreaImpl meetingArea;

    @CatalogRequest(
            id = "localDomainRequest_d564897b-ad17-4289-af2c-bd79425c5341",
            name = "Accept Invitation",
            description = "This request is used to Accept invitation for the scheduled event.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails acceptInvitation(
            @Field.Text(name = "Event Id") String eventId,
            @Field(name = "Attendee Email", type = "Email", attributes = {@Attribute(name = "visualWidth", value = "S")}) String attendeeEmail) {
        return meetingArea.acceptInvitation(eventId, attendeeEmail);
    }

    @CatalogRequest(
            id = "localDomainRequest_2e73b843-ace4-4b80-bfab-4f466706e5c5",
            name = "Add Attendees",
            description = "This request is used to add one more attendee for the scheduled event.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails addAttendees(
            @Field.Text(name = "Event Id") String eventId,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> requiredAttendeeEmail) {
        return meetingArea.addAttendees(eventId, requiredAttendeeEmail);
    }

    @CatalogRequest(
            id = "localDomainRequest_44d8fe1f-3f2d-438d-bb9f-bb486b9a9e56",
            name = "Cancel Event",
            description = "This request cancels the scheduled event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails cancelEvent(@Field.Text(name = "Event Id") String eventId) {
        return meetingArea.cancelEvent(eventId);
    }

    @CatalogRequest(
            id = "localDomainRequest_9de59f5f-b77d-42e3-84da-63f587f65358",
            name = "Decline Invitation",
            description = "This request is used to Decline invitation of a scheduled event.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails declineInvitation(
            @Field.Text(name = "Event Id") String eventId,
            @Field(name = "Attendee Email", type = "Email", attributes = {@Attribute(name = "visualWidth", value = "S")}) String attendeeEmail) {
        return meetingArea.declineInvitation(eventId, attendeeEmail);
    }

    @CatalogRequest(
            id = "localDomainRequest_771024d0-02e5-4163-869e-8f8860a10b62",
            name = "Get Next Upcoming Meetings",
            description = "This request gives the Next Upcoming Events.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]",required = false)
    public List<EventDetails> getNextUpcomingMeetings(
            @Field(name = "Number of meeting", type = "Number") Double numberOfMeeting) {
        return meetingArea.getNextUpcomingMeetings(numberOfMeeting);
    }


    @CatalogRequest(
            id = "localDomainRequest_653a694e-2327-41d5-8a8c-48b30b07dc89",
            name = "Remove Attendee",
            description = "This request remove attendee from the scheduled event.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails removeAttendee(
            @Field.Text(name = "Event Id") String eventId,
            @Field(name = "Attendee Email", type = "Email", attributes = {@Attribute(name = "visualWidth", value = "S")}) String attendeeEmail) {
        return meetingArea.removeAttendee(eventId, attendeeEmail);
    }

    @CatalogRequest(
            id = "localDomainRequest_f9cf27ee-ffe7-4b65-8e0a-ce0afa25f5dc",
            name = "Schedule Meeting",
            description = "Schedule meeting with given details",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails scheduleMeeting(
            @Field.Text(name = "Title") String title,
            @Field.Text(name = "Location", required = false) String location,
            @Field.Date(name = "Date From", required = true)  Long dateFrom,
            @Field.Date(name = "Date to", required = true)  Long dateTo,
            @Field(name = "Details", type = "RichText", attributes = {@Attribute(name = "visualWidth", value = "L")}) String details,
            @Field.Desc(name = "Attendee Email", type = "[ Email ]") List<String> attendeeEmail) {
        return meetingArea.scheduleMeeting(title, location, dateFrom, dateTo, details, attendeeEmail);
    }

    @CatalogRequest(
            id = "localDomainRequest_5bf7458f-953f-40c4-b75c-17b1f76a08b1",
            name = "Tentative Invitation",
            description = "This request is used to Accept tentative invitation of a scheduled event",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)",required = false)
    public EventDetails tentativeInvitation(
            @Field.Text(name = "Event Id") String eventId,
            @Field(name = "Attendee Email", type = "Email", attributes = {@Attribute(name = "visualWidth", value = "S")}) String attendeeEmail) {
        return meetingArea.tentativeInvitation(eventId, attendeeEmail);
    }

    @CatalogRequest(
            id = "localDomainRequest_e0f748a2-17df-4e29-84e0-a1d0d06934d9",
            name = "Get All Unaccepted Events",
            description = "This request will Retrieve all unaccepted events.",
            area = "Meetings",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]",required = false)
    public List<EventDetails> getAllUnacceptedEvents() {
        return meetingArea.getAllUnacceptedEvents();
    }

    @CatalogRequest(
            id = "localDomainRequest_48062a11-b237-4307-9394-a6c3496bff14",
            name = "Get Event",
            description = "Get Event details for the given Event ID",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "Entity(Event Details)", required = false)
    public EventDetails getEvent(@Field(name = "Event ID", type = "Text") String eventID) {
        return meetingArea.getEvent(eventID);
    }
    @CatalogRequest(
            id = "localDomainRequest_74cb78ed-7c37-4234-850b-29069186c9a8",
            name = "Get Events for a given User",
            description = "This request will return the events scheduled for a given user given that the setup is done using a system account.",
            area = "Meetings",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Event Details", type = "[ Entity(Event Details) ]", required = false)
    public List<EventDetails> getEventsForAGivenUser(
            @Field(name = "User Email", type = "Email", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String userEmail,
            @Field.Date(name = "From", required = true, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = true, allowToday = true, allowFuture = true, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Long from,
            @Field.Date(name = "To", required = true, includeTimeOfDay = true, showHowManyDaysInViewer = 60, allowPast = true, allowToday = true, allowFuture = true, defaultTimeSpan = 1, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) Long to) {

        return meetingArea.getEventsForUser(userEmail, from, to);
    }
}