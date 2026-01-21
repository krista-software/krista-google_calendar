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

/**
 * Edge cases and error scenarios test class for MeetingAreaImpl
 */
@ExtendWith(MockitoExtension.class)
class MeetingAreaImplEdgeCasesTest {

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
    private Calendar.Events.List eventsList;

    @Mock
    private Calendar.Events.Insert eventsInsert;

    private MeetingAreaImpl meetingArea;

    @BeforeEach
    void setUp() throws IOException {
        when(factory.create()).thenReturn(provider);
        when(provider.getGoogleClient()).thenReturn(calendar);
        when(calendar.events()).thenReturn(events);
        
        meetingArea = new MeetingAreaImpl(factory, requestContext);
    }

    @Test
    void acceptInvitation_AttendeeNotFound_NoChangeInResponseStatus() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String emailId = "notfound@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        EventAttendee attendee = new EventAttendee()
            .setEmail("different@example.com")
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
        assertEquals("needsAction", attendee.getResponseStatus()); // Should remain unchanged
    }

    @Test
    void acceptInvitation_EventWithNoAttendees_HandlesGracefully() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String emailId = "test@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        mockEvent.setAttendees(Collections.emptyList());
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.acceptInvitation(eventId, emailId);

        // Assert
        assertNotNull(result);
        assertEquals(0, mockEvent.getAttendees().size());
    }

    @Test
    void addAttendees_NullAttendeesList_HandlesGracefully() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        List<String> nullAttendees = null;
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        mockEvent.setAttendees(Arrays.asList(new EventAttendee().setEmail("existing@example.com")));
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.addAttendees(eventId, nullAttendees);

        // Assert
        assertNotNull(result);
        assertEquals(1, mockEvent.getAttendees().size()); // Only existing attendee
    }

    @Test
    void removeAttendee_AttendeeNotFound_NoChange() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String attendeeEmail = "notfound@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        List<EventAttendee> originalAttendees = Arrays.asList(
            new EventAttendee().setEmail("keep1@example.com"),
            new EventAttendee().setEmail("keep2@example.com")
        );
        mockEvent.setAttendees(originalAttendees);
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.removeAttendee(eventId, attendeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals(2, mockEvent.getAttendees().size()); // No change
        assertEquals("keep1@example.com", mockEvent.getAttendees().get(0).getEmail());
        assertEquals("keep2@example.com", mockEvent.getAttendees().get(1).getEmail());
    }

    @Test
    void removeAttendee_EmptyAttendeesList_HandlesGracefully() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String attendeeEmail = "remove@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        mockEvent.setAttendees(Collections.emptyList());
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.removeAttendee(eventId, attendeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals(0, mockEvent.getAttendees().size());
    }

    @Test
    void tentativeInvitation_AttendeeIsOrganizer_NoChange() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String attendeeEmail = "organizer@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        EventAttendee organizerAttendee = new EventAttendee()
            .setEmail(attendeeEmail)
            .setResponseStatus("accepted")
            .setSelf(true)
            .setOrganizer(true); // This attendee is the organizer
        mockEvent.setAttendees(Arrays.asList(organizerAttendee));
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.tentativeInvitation(eventId, attendeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals("accepted", organizerAttendee.getResponseStatus()); // Should remain unchanged
    }

    @Test
    void tentativeInvitation_AttendeeNotSelf_NoChange() throws IOException {
        // Arrange
        String eventId = "test-event-id";
        String attendeeEmail = "other@example.com";
        
        Event mockEvent = createMockEvent(eventId, "Test Meeting");
        EventAttendee otherAttendee = new EventAttendee()
            .setEmail(attendeeEmail)
            .setResponseStatus("needsAction")
            .setSelf(false); // This attendee is not self
        mockEvent.setAttendees(Arrays.asList(otherAttendee));
        
        when(events.get(eq("primary"), eq(eventId))).thenReturn(eventsGet);
        when(eventsGet.execute()).thenReturn(mockEvent);
        when(events.patch(eq("primary"), eq(eventId), any(Event.class))).thenReturn(eventsPatch);
        when(eventsPatch.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.tentativeInvitation(eventId, attendeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals("needsAction", otherAttendee.getResponseStatus()); // Should remain unchanged
    }

    @Test
    void getAllUnacceptedEvents_NoEvents_ReturnsEmptyList() throws IOException {
        // Arrange
        Events mockEvents = new Events();
        mockEvents.setItems(Collections.emptyList());
        
        when(events.list(eq("primary"))).thenReturn(eventsList);
        when(eventsList.execute()).thenReturn(mockEvents);

        // Act
        List<EventDetails> result = meetingArea.getAllUnacceptedEvents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsForUser_EmptyEventsList_ReturnsEmptyList() throws IOException {
        // Arrange
        String userEmail = "user@example.com";
        Long fromDate = System.currentTimeMillis();
        Long toDate = fromDate + 86400000;
        
        Events mockEvents = new Events();
        mockEvents.setItems(null); // Null items list
        
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
        assertTrue(result.isEmpty());
    }

    @Test
    void scheduleMeeting_EmptyAttendeesList_CreatesEventWithNoAttendees() throws IOException {
        // Arrange
        String title = "Test Meeting";
        String location = "Conference Room";
        Long dateFrom = System.currentTimeMillis();
        Long dateTo = dateFrom + 3600000;
        String details = "Meeting details";
        List<String> emptyAttendees = Collections.emptyList();

        Event mockEvent = createMockEvent("new-event-id", title);

        when(events.insert(eq("primary"), any(Event.class))).thenReturn(eventsInsert);
        when(eventsInsert.execute()).thenReturn(mockEvent);

        // Act
        EventDetails result = meetingArea.scheduleMeeting(title, location, dateFrom, dateTo, details, emptyAttendees);

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
