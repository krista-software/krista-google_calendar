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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingAreaImplTest {

    @Mock
    private GoogleProviderFactory factory;
    
    @Mock
    private RequestContext requestContext;
    
    @Mock
    private GoogleProvider provider;
    
    @Mock
    private Calendar calendar;
    
    @Mock
    private Calendar.Events events;
    
    @Mock
    private Calendar.Events.Get eventsGet;
    
    @Mock
    private Calendar.Events.Patch eventsPatch;
    
    @Mock
    private Calendar.Events.Insert eventsInsert;
    
    @Mock
    private Calendar.Events.Delete eventsDelete;
    
    @Mock
    private Calendar.Events.List eventsList;

    private MeetingAreaImpl meetingArea;

    @BeforeEach
    void setUp() throws IOException {
        when(factory.create()).thenReturn(provider);
        when(provider.getGoogleClient()).thenReturn(calendar);
        when(calendar.events()).thenReturn(events);
        
        meetingArea = new MeetingAreaImpl(factory, requestContext);
    }

    @Test
    void acceptInvitation_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String emailId = "test@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        EventAttendee attendee = new EventAttendee()
            .setEmail(emailId)
            .setResponseStatus("needsAction");
        mockEvent.setAttendees(Arrays.asList(attendee));
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.acceptInvitation(eventId, emailId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.eventId);
        assertEquals("accepted", attendee.getResponseStatus());
        verify(events).get("primary", eventId);
        verify(events).patch(eq("primary"), eq(eventId), any(Event.class));
    }

    @Test
    void acceptInvitation_IOExceptionThrown_ThrowsIllegalStateException() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String emailId = "test@example.com";
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenThrow(new IOException("API Error"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> meetingArea.acceptInvitation(eventId, emailId));
        
        assertTrue(exception.getMessage().contains("Invitation acceptance failed"));
        assertTrue(exception.getMessage().contains("Unable to update event response status"));
    }

    @Test
    void addAttendees_EmptyAttendeesList_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        List<String> emptyAttendees = Collections.emptyList();
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        mockEvent.setAttendees(Arrays.asList(new EventAttendee().setEmail("existing@example.com")));
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.addAttendees(eventId, emptyAttendees);

        // Assert
        assertNotNull(result);
        assertEquals(1, mockEvent.getAttendees().size()); // Only existing attendee
    }

    @Test
    void cancelEvent_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.delete(eq("primary"), eq(eventId))).thenReturn(eventsDelete);
        doNothing().when(eventsDelete).execute();

        // Act
        EventDetails result = meetingArea.cancelEvent(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.eventId);
        verify(events).get("primary", eventId);
        verify(events).delete("primary", eventId);
    }

    @Test
    void declineInvitation_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String emailId = "test@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        EventAttendee attendee = new EventAttendee()
            .setEmail(emailId)
            .setResponseStatus("needsAction");
        mockEvent.setAttendees(Arrays.asList(attendee));
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.declineInvitation(eventId, emailId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.eventId);
        assertEquals("declined", attendee.getResponseStatus());
        verify(events).get("primary", eventId);
        verify(events).patch(eq("primary"), eq(eventId), any(Event.class));
    }

    @Test
    void getNextUpcomingMeetings_ValidInput_ReturnsEventDetailsList() throws IOException {
        // Arrange
        Double numberOfMeetings = 5.0;
        Events mockEvents = new Events();
        List<Event> eventList = Arrays.asList(
            createMockEvent("event1", "Meeting 1"),
            createMockEvent("event2", "Meeting 2")
        );
        mockEvents.setItems(eventList);
        
        when(events.list(eq("primary"))).thenReturn(eventsList);
        when(eventsList.setMaxResults(5)).thenReturn(eventsList);
        when(eventsList.setTimeMin(any(DateTime.class))).thenReturn(eventsList);
        when(eventsList.setOrderBy("startTime")).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(mockEvents);

        // Act
        List<EventDetails> result = meetingArea.getNextUpcomingMeetings(numberOfMeetings);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("event1", result.get(0).eventId);
        assertEquals("event2", result.get(1).eventId);
    }

    @Test
    void removeAttendee_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String attendeeEmail = "remove@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        List<EventAttendee> attendees = Arrays.asList(
            new EventAttendee().setEmail("keep@example.com"),
            new EventAttendee().setEmail(attendeeEmail)
        );
        mockEvent.setAttendees(attendees);
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.removeAttendee(eventId, attendeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.eventId);
        assertEquals(1, mockEvent.getAttendees().size());
        assertEquals("keep@example.com", mockEvent.getAttendees().get(0).getEmail());
    }

    @Test
    void scheduleMeeting_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String title = "New Meeting";
        String location = "Conference Room A";
        Long dateFrom = System.currentTimeMillis();
        Long dateTo = dateFrom + 3600000; // 1 hour later
        String details = "Meeting details";
        List<String> attendeeEmails = Arrays.asList("attendee1@example.com", "attendee2@example.com");
        
        Event mockEvent = createMockEvent("new-event-id", title);
        
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(eventsInsert);
        when(eventsInsert.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.scheduleMeeting(title, location, dateFrom, dateTo, details, attendeeEmails);

        // Assert
        assertNotNull(result);
        assertEquals("new-event-id", result.eventId);
        assertEquals(title, result.title);
        verify(events).insert(eq("primary"), any(Event.class));
    }

    @Test
    void tentativeInvitation_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String attendeeEmail = "test@example.com";

        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        EventAttendee attendee = new EventAttendee()
            .setEmail(attendeeEmail)
            .setResponseStatus("needsAction")
            .setSelf(true)
            .setOrganizer(false);
        mockEvent.setAttendees(Arrays.asList(attendee));

        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.tentativeInvitation(eventId, attendeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.eventId);
        assertEquals("tentative", attendee.getResponseStatus());
        verify(events).get("primary", eventId);
        verify(events).patch("primary", eventId, mockEvent);
    }

    @Test
    void getAllUnacceptedEvents_ValidInput_ReturnsUnacceptedEvents() throws IOException {
        // Arrange
        Events mockEvents = new Events();

        // Create accepted event (should not be included)
        Event acceptedEvent = createMockEvent("accepted-event", "Accepted Meeting");
        EventAttendee acceptedAttendee = new EventAttendee()
            .setEmail("user@example.com")
            .setResponseStatus("accepted")
            .setSelf(true);
        acceptedEvent.setAttendees(Arrays.asList(acceptedAttendee));

        // Create unaccepted event (should be included)
        Event unacceptedEvent = createMockEvent("unaccepted-event", "Pending Meeting");
        EventAttendee pendingAttendee = new EventAttendee()
            .setEmail("user@example.com")
            .setResponseStatus("needsAction")
            .setSelf(true);
        unacceptedEvent.setAttendees(Arrays.asList(pendingAttendee));

        mockEvents.setItems(Arrays.asList(acceptedEvent, unacceptedEvent));

        when(events.list(eq("primary"))).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(mockEvents);

        // Act
        List<EventDetails> result = meetingArea.getAllUnacceptedEvents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("unaccepted-event", result.get(0).eventId);
    }

    @Test
    void getEvent_ValidInput_ReturnsEventDetails() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        Event mockEvent = createMockEvent(eventId, "Test Meeting");

        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.getEvent(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(eventId, result.eventId);
        assertEquals("Test Meeting", result.title);
        verify(events).get("primary", eventId);
    }

    @Test
    void getEvent_IOExceptionThrown_ThrowsIllegalStateException() throws IOException {
        // Arrange
        String eventId = "test-event-id";

        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenThrow(new IOException("Event not found"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> meetingArea.getEvent(eventId));

        assertTrue(exception.getMessage().contains("Event retrieval failed"));
        assertTrue(exception.getMessage().contains("Unable to fetch calendar event details"));
    }

    @Test
    void getEventsForUser_ValidInput_ReturnsEventsList() throws IOException {
        // Arrange
        String userEmail = "user@example.com";
        Long fromDate = System.currentTimeMillis();
        Long toDate = fromDate + 86400000; // 1 day later

        Events mockEvents = new Events();
        List<Event> eventList = Arrays.asList(
            createMockEvent("event1", "User Meeting 1"),
            createMockEvent("event2", "User Meeting 2")
        );
        mockEvents.setItems(eventList);

        when(events.list(eq(userEmail))).thenReturn(eventsList);
        when(eventsList.setTimeMin(any(DateTime.class))).thenReturn(eventsList);
        when(eventsList.setTimeMax(any(DateTime.class))).thenReturn(eventsList);
        when(eventsList.setOrderBy("startTime")).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(mockEvents);

        // Act
        List<EventDetails> result = meetingArea.getEventsForUser(userEmail, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("event1", result.get(0).eventId);
        assertEquals("event2", result.get(1).eventId);
        verify(events).list(userEmail);
    }

    @Test
    void getEventsForUser_PrimaryUser_UsesPrimaryCalendar() throws IOException {
        // Arrange
        String userEmail = "primary";
        Long fromDate = System.currentTimeMillis();
        Long toDate = fromDate + 86400000;

        Events mockEvents = new Events();
        mockEvents.setItems(Arrays.asList(createMockEvent("event1", "Primary Meeting")));

        when(events.list(eq("primary"))).thenReturn(eventsList);
        when(eventsList.setTimeMin(any(DateTime.class))).thenReturn(eventsList);
        when(eventsList.setTimeMax(any(DateTime.class))).thenReturn(eventsList);
        when(eventsList.setOrderBy("startTime")).thenReturn(eventsList);
        when(eventsList.setSingleEvents(true)).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(mockEvents);

        // Act
        List<EventDetails> result = meetingArea.getEventsForUser(userEmail, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(events).list("primary"); // Should use "primary" not the email
    }

    @Test
    void scheduleMeeting_PrepareEventLogic_CreatesEventCorrectly() throws IOException {
        // Arrange
        String title = "Test Meeting";
        String location = "Conference Room";
        Long dateFrom = System.currentTimeMillis();
        Long dateTo = dateFrom + 3600000; // 1 hour later
        String details = "Meeting details";
        List<String> attendeeEmails = Arrays.asList("attendee1@example.com", "attendee2@example.com");

        Event mockEvent = createMockEvent("new-event-id", title);

        when(events.insert(eq("primary"), any(Event.class))).thenReturn(eventsInsert);
        when(eventsInsert.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.scheduleMeeting(title, location, dateFrom, dateTo, details, attendeeEmails);

        // Assert
        assertNotNull(result);
        assertEquals("new-event-id", result.eventId);
        assertEquals(title, result.title);
        verify(events).insert(eq("primary"), any(Event.class));
    }

    // Helper method to create mock events
    private Event createMockEvent(String eventId, String title) {
        Event event = new Event();
        event.setId(eventId);
        event.setSummary(title);
        event.setDescription("Test description");
        event.setLocation("Test location");

        DateTime startTime = new DateTime(System.currentTimeMillis());
        DateTime endTime = new DateTime(System.currentTimeMillis() + 3600000);

        EventDateTime start = new EventDateTime().setDateTime(startTime);
        EventDateTime end = new EventDateTime().setDateTime(endTime);

        event.setStart(start);
        event.setEnd(end);

        return event;
    }
}
