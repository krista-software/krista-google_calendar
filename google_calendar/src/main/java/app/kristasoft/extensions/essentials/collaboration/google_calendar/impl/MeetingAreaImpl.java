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

package app.kristasoft.extensions.essentials.collaboration.google_calendar.impl;

import app.krista.ksdk.context.RequestContext;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.catalog.entity.EventDetails;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.connectors.GoogleProvider;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.connectors.GoogleProviderFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util.Constants.PRIMARY;

@Service
public class MeetingAreaImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeetingAreaImpl.class);
    private final GoogleProvider provider;
    private final RequestContext requestContext;


    @Inject
    public MeetingAreaImpl(GoogleProviderFactory factory, RequestContext requestContext) {
        this.provider = factory.create();
        this.requestContext = requestContext;
    }

    /**
     * Accepts a calendar event invitation by updating the attendee's response status to "accepted".
     * Retrieves the event, finds the specified attendee, updates their response status, and patches the event.
     *
     * @param eventId the unique identifier of the calendar event
     * @param emailId the email address of the attendee accepting the invitation
     * @return EventDetails entity containing the updated event information
     * @throws IllegalStateException if the event cannot be retrieved or updated due to invalid Event ID,
     *                               attendee not found, or Google Calendar API connectivity issues
     */
    public EventDetails acceptInvitation(String eventId, String emailId) {
        try {
            Calendar service = getCalendar();
            Event fetchedEvent = service.events().get(PRIMARY, eventId).execute();
            for (EventAttendee ea : fetchedEvent.getAttendees()) {
                if (ea.getEmail().equals(emailId)) {
                    ea.setResponseStatus("accepted");
                }
            }
            Event event = service.events().patch(PRIMARY, eventId, fetchedEvent).execute();
            return EventDetails.getEventDetailsByEvent(event);

        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Invitation acceptance failed: Unable to update event response status. Required: Valid Event ID and attendee email with pending invitation. Resolution: Verify event exists, attendee has pending invitation, and check Google Calendar API connectivity.", cause);
        }
    }

    /**
     * Adds new attendees to an existing calendar event.
     * Retrieves the event, appends new attendees to the existing attendee list, and updates the event.
     *
     * @param eventId the unique identifier of the calendar event
     * @param attendees list of email addresses to add as attendees
     * @return EventDetails entity containing the updated event with new attendees
     * @throws IllegalStateException if the event cannot be retrieved or updated due to invalid Event ID,
     *                               invalid email addresses, or Google Calendar API connectivity issues
     */
    public EventDetails addAttendees(String eventId, List<String> attendees) {
        try {
            Calendar service = getCalendar();
            Event getEvent = service.events().get(PRIMARY, eventId).execute();
            List<EventAttendee> eventAttendees = getEventAttendees(attendees);
            getEvent.getAttendees().addAll(eventAttendees);
            Event updatedEvent = service.events().patch(PRIMARY, eventId, getEvent).execute();
            return EventDetails.getEventDetailsByEvent(updatedEvent);
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Attendee addition failed: Unable to add new attendees to calendar event. Required: Valid Event ID and attendee email addresses. Resolution: Verify event exists, attendee emails are valid, and check Google Calendar API connectivity.", cause);
        }
    }

    /**
     * Converts a list of email addresses to EventAttendee objects for Google Calendar API.
     *
     * @param eventDetailsAttendee list of attendee email addresses
     * @return list of EventAttendee objects, or empty list if input is null or empty
     */
    private List<EventAttendee> getEventAttendees(List<String> eventDetailsAttendee) {
        List<EventAttendee> attendees = null;
        if (eventDetailsAttendee != null && !eventDetailsAttendee.isEmpty()) {
            attendees = eventDetailsAttendee.stream().map(s -> new EventAttendee().setEmail(s)).collect(Collectors.toList());
        }
        return attendees == null ? List.of() : attendees;
    }

    /**
     * Retrieves an authenticated Google Calendar service instance.
     *
     * @return Calendar service instance with valid authentication credentials
     * @throws IOException if unable to create Calendar client due to authentication or network issues
     */
    private Calendar getCalendar() throws IOException {
        return provider.getGoogleClient();
    }

    /**
     * Cancels a calendar event by deleting it from the calendar.
     * Retrieves the event details before deletion and notifies all attendees.
     *
     * @param eventId the unique identifier of the calendar event to cancel
     * @return EventDetails entity containing the cancelled event information
     * @throws IllegalStateException if the event cannot be retrieved or deleted due to invalid Event ID,
     *                               insufficient organizer permissions, or Google Calendar API connectivity issues
     */
    public EventDetails cancelEvent(String eventId) {
        try {
            Calendar service = getCalendar();
            Event cancelEvent = service.events().get(PRIMARY, eventId).execute();
            service.events().delete(PRIMARY, eventId).execute();
            return EventDetails.getEventDetailsByEvent(cancelEvent);
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Event cancellation failed: Unable to cancel calendar event. Required: Valid Event ID and organizer permissions. Resolution: Verify event exists, user has organizer permissions, and check Google Calendar API connectivity.", cause);
        }
    }

    /**
     * Declines a calendar event invitation by updating the attendee's response status to "declined".
     * Retrieves the event, finds the specified attendee, updates their response status, and patches the event.
     *
     * @param eventId the unique identifier of the calendar event
     * @param email the email address of the attendee declining the invitation
     * @return EventDetails entity containing the updated event information
     * @throws IllegalStateException if the event cannot be retrieved or updated due to invalid Event ID,
     *                               attendee not found, or Google Calendar API connectivity issues
     */
    public EventDetails declineInvitation(String eventId, String email) {
        try {
            Calendar service = getCalendar();
            Event fetchedEvent = service.events().get(PRIMARY, eventId).execute();
            for (EventAttendee ea : fetchedEvent.getAttendees()) {
                if (ea.getEmail().equals(email)) {
                    ea.setResponseStatus("declined");
                }
            }
            Event e = service.events().patch(PRIMARY, eventId, fetchedEvent).execute();
            return EventDetails.getEventDetailsByEvent(e);
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Invitation decline failed: Unable to update event response status to declined. Required: Valid Event ID and attendee email with pending invitation. Resolution: Verify event exists, attendee has pending invitation, and check Google Calendar API connectivity.", cause);
        }
    }

    /**
     * Retrieves the next upcoming calendar events starting from the current time.
     * Events are ordered by start time and limited to the specified count.
     *
     * @param numberOfMeeting the maximum number of upcoming events to retrieve
     * @return list of EventDetails entities representing upcoming events, ordered by start time
     * @throws IllegalStateException if events cannot be retrieved due to invalid calendar access,
     *                               authentication issues, or Google Calendar API connectivity problems
     */
    public List<EventDetails> getNextUpcomingMeetings(Double numberOfMeeting) {
        try {
            Calendar service = getCalendar();
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = service.events()
                    .list(PRIMARY)
                    .setMaxResults(numberOfMeeting.intValue())
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems().stream().map(EventDetails::getEventDetailsByEvent).collect(
                    Collectors.toList());
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Upcoming meetings retrieval failed: Unable to fetch future calendar events. Required: Valid calendar access and network connectivity. Resolution: Verify calendar permissions, check authentication status, and ensure Google Calendar API connectivity.", cause);
        }
    }

    /**
     * Removes a specific attendee from a calendar event.
     * Retrieves the event, filters out the specified attendee, and updates the event.
     *
     * @param eventId the unique identifier of the calendar event
     * @param attendeeEmail the email address of the attendee to remove
     * @return EventDetails entity containing the updated event without the removed attendee
     * @throws IllegalStateException if the event cannot be retrieved or updated due to invalid Event ID,
     *                               attendee not found, insufficient organizer permissions, or Google Calendar API connectivity issues
     */
    public EventDetails removeAttendee(String eventId, String attendeeEmail) {
        try {
            Calendar service = getCalendar();
            Event event = service.events().get(PRIMARY, eventId).execute();
            List<EventAttendee> attendeeList = event.getAttendees();
            List<EventAttendee> updateAttendeeList = attendeeList.stream().filter(attendee -> !attendee.getEmail().equals(attendeeEmail)).collect(Collectors.toList());
            event.setAttendees(updateAttendeeList);
            service.events().patch(PRIMARY, eventId, event).execute();
            return EventDetails.getEventDetailsByEvent(event);
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Attendee removal failed: Unable to remove attendee from calendar event. Required: Valid Event ID, attendee email, and organizer permissions. Resolution: Verify event exists, attendee is on event, user has organizer permissions, and check Google Calendar API connectivity.", cause);
        }
    }

    /**
     * Schedules a new calendar meeting with specified details and attendees.
     * Creates a new event in the primary calendar with title, location, date range, description, and attendees.
     *
     * @param title the meeting title/subject
     * @param location the meeting location or venue (can be null)
     * @param dateFrom the meeting start date/time in Unix timestamp milliseconds
     * @param dateTo the meeting end date/time in Unix timestamp milliseconds
     * @param details the meeting description or agenda
     * @param attendeeEmail list of attendee email addresses to invite
     * @return EventDetails entity containing the created event with generated Event ID
     * @throws IllegalStateException if the event cannot be created due to invalid parameters,
     *                               insufficient calendar write permissions, or Google Calendar API connectivity issues
     */
    public EventDetails scheduleMeeting(String title, String location, Long dateFrom, Long dateTo, String details, List<String> attendeeEmail) {
        Event event = prepareEvent(title, location, dateFrom, dateTo, details, attendeeEmail);
        try {
            Calendar calendar = getCalendar();
            event = calendar.events().insert(PRIMARY, event).execute();

        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Meeting scheduling failed: Unable to create new calendar event. Required: Valid title, date range, and calendar write permissions. Resolution: Verify all required fields are provided, date range is valid, and check Google Calendar API connectivity.", cause);
        }
        return EventDetails.getEventDetailsByEvent(event);
    }

    /**
     * Prepares a Google Calendar Event object from meeting parameters.
     * Constructs an Event with summary, location, description, start/end times, and attendees.
     *
     * @param title the meeting title/subject
     * @param location the meeting location or venue
     * @param dateFrom the meeting start date/time in Unix timestamp milliseconds
     * @param dateTo the meeting end date/time in Unix timestamp milliseconds
     * @param details the meeting description or agenda
     * @param attendeesEmail list of attendee email addresses
     * @return Event object ready for insertion into Google Calendar
     */
    public static Event prepareEvent(String title, String location, long dateFrom, long dateTo, String details, List<String> attendeesEmail) {
        Event event = new Event();
        event.setDescription(details);
        event.setSummary(title);
        event.setLocation(location);

        DateTime startDateTime = new DateTime(dateFrom);
        EventDateTime start = new EventDateTime().setDateTime(startDateTime);
        event.setStart(start);

        DateTime endDateTime = new DateTime(dateTo);
        EventDateTime endDate = new EventDateTime().setDateTime(endDateTime);
        event.setEnd(endDate);

        List<EventAttendee> attendees = attendeesEmail.stream().map(s -> new EventAttendee().setEmail(s)).collect(Collectors.toList());
        event.setAttendees(attendees);
        return event;
    }

    /**
     * Responds tentatively to a calendar event invitation by updating the attendee's response status to "tentative".
     * Finds the current user in the attendee list and sets their response status if they are not the organizer.
     *
     * @param eventId the unique identifier of the calendar event
     * @param attendeeEmail the email address of the attendee responding tentatively
     * @return EventDetails entity containing the updated event information
     * @throws IllegalStateException if the event cannot be retrieved or updated due to invalid Event ID,
     *                               attendee not found, or Google Calendar API connectivity issues
     */
    public EventDetails tentativeInvitation(String eventId, String attendeeEmail) {
        EventDetails eventDetails;
        try {
            Calendar calendar = getCalendar();
            Event event = calendar.events().get(PRIMARY, eventId).execute();
            List<EventAttendee> attendeeList = event.getAttendees();
            for (EventAttendee attendee : attendeeList) {
                if (attendee.isSelf() && (attendee.getOrganizer() == null || !attendee.getOrganizer())) {
                    attendee.setResponseStatus("tentative");
                    break;
                }
            }
            event.setAttendees(attendeeList);
            LOGGER.info("tentative Meeting acceptation having meeting ID :" + eventId + "request for ");
            calendar.events().patch("primary", eventId, event).execute();
            eventDetails = EventDetails.getEventDetailsByEvent(event);
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Tentative response failed: Unable to update event response status to tentative. Required: Valid Event ID and attendee email with pending invitation. Resolution: Verify event exists, attendee has pending invitation, and check Google Calendar API connectivity.", cause);
        }
        return eventDetails;
    }

    /**
     * Retrieves all calendar events with unaccepted invitations (response status "needsAction").
     * Filters events where the current user is an attendee with a pending invitation response.
     *
     * @return list of EventDetails entities representing events with pending invitations
     * @throws IllegalStateException if events cannot be retrieved due to invalid calendar access,
     *                               authentication issues, or Google Calendar API connectivity problems
     */
    public List<EventDetails> getAllUnacceptedEvents() {
        List<EventDetails> eventsDetail = new ArrayList<>();
        try {
            Events events = getCalendar().events().list(PRIMARY).execute();
            List<Event> eventList = events.getItems();
            for (Event event : eventList) {
                List<EventAttendee> attendeeList = event.getAttendees();
                for (EventAttendee attendee : attendeeList) {
                    if (attendee.isSelf() && "needsAction".equals(attendee.getResponseStatus())) {
                        eventsDetail.add(EventDetails.getEventDetailsByEvent(event));
                        break;
                    }
                }
            }
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Unaccepted events retrieval failed: Unable to fetch events with pending invitations. Required: Valid calendar access and network connectivity. Resolution: Verify calendar permissions, check authentication status, and ensure Google Calendar API connectivity.", cause);
        }
        return eventsDetail;
    }

    /**
     * Retrieves a specific calendar event by its unique identifier.
     *
     * @param eventId the unique identifier of the calendar event to retrieve
     * @return EventDetails entity containing the event information
     * @throws IllegalStateException if the event cannot be retrieved due to invalid Event ID,
     *                               insufficient calendar read permissions, or Google Calendar API connectivity issues
     */
    public EventDetails getEvent(String eventId) {
        EventDetails eventDetails;
        try {
            Calendar calendar = getCalendar();
            Event event = calendar.events().get(PRIMARY, eventId).execute();
            eventDetails = EventDetails.getEventDetailsByEvent(event);
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new IllegalStateException("Event retrieval failed: Unable to fetch calendar event details. Required: Valid Event ID and calendar read permissions. Resolution: Verify event ID exists, check calendar access permissions, and ensure Google Calendar API connectivity.", cause);
        }
        return eventDetails;
    }

    /**
     * Retrieves all calendar events for a specific user within a specified date range.
     * Requires service account with domain-wide delegation for accessing other users' calendars.
     *
     * @param userEmail the email address of the user whose events to retrieve, or "primary" for the authenticated user
     * @param fromDate the start date/time in Unix timestamp milliseconds for the event search range
     * @param toDate the end date/time in Unix timestamp milliseconds for the event search range
     * @return list of EventDetails entities representing events within the specified date range, ordered by start time
     * @throws IllegalStateException if events cannot be retrieved due to invalid user email, invalid date range,
     *                               insufficient service account permissions, or Google Calendar API connectivity issues
     */
    public List<EventDetails> getEventsForUser(String userEmail, Long fromDate, Long toDate) {
        LOGGER.info("→ getEventsForUser: Retrieving events for user {} from {} to {}", userEmail, fromDate, toDate);

        try {
            Calendar service = getCalendar();

            // Convert timestamps to DateTime objects
            DateTime timeMin = new DateTime(fromDate);
            DateTime timeMax = new DateTime(toDate);

            LOGGER.info("→ Created time range query: {} to {}", timeMin, timeMax);

            // Get events from the specified user's calendar
            Events events = service.events()
                    .list(userEmail.equals("primary") ? PRIMARY : userEmail)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            LOGGER.info("→ Retrieved event collection from Google Calendar API for user");

            List<EventDetails> eventDetailsList = new ArrayList<>();
            if (events.getItems() != null) {
                for (Event event : events.getItems()) {
                    eventDetailsList.add(EventDetails.getEventDetailsByEvent(event));
                    LOGGER.info("→ Added event: {}", event.getSummary());
                }
            }

            LOGGER.info("→ getEventsForUser: Successfully retrieved {} events for user {}", eventDetailsList.size(), userEmail);
            return eventDetailsList;

        } catch (IOException cause) {
            String errorMessage = cause.getMessage();
            LOGGER.error("→ getEventsForUser: Failed to retrieve events for user {} - {}", userEmail, errorMessage, cause);
            throw new IllegalStateException("User events retrieval failed: Unable to fetch calendar events for specified user. Required: Valid user email, date range, and service account with domain-wide delegation. Resolution: Verify user email exists in domain, date range is valid, and service account has proper permissions.", cause);
        }
    }
}
