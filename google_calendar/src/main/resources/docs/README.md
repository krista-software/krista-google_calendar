# Google Calendar Extension

## Overview

The Google Calendar Extension enables seamless integration with Google Calendar, allowing you to manage events, invitations, and attendees directly from your Krista workflows. This extension provides comprehensive calendar management capabilities including scheduling meetings, managing invitations, and querying calendar data.

## Key Features

- **Event Management**: Schedule, cancel, and retrieve calendar events
- **Invitation Management**: Accept, decline, or tentatively respond to meeting invitations
- **Attendee Management**: Add or remove attendees from existing events
- **Query Operations**: Retrieve upcoming events, unaccepted invitations, and user-specific calendars
- **OAuth 2.0 Authentication**: Secure authentication with both service account and user account modes
- **Real-time Synchronization**: Direct integration with Google Calendar API

## Quick Start Guide

1. **Setup Google App**: Create a Google Cloud project and configure OAuth credentials
2. **Configure Extension**: Set up Client ID, Client Secret, and authentication parameters
3. **Authenticate**: Complete OAuth flow for secure access to Google Calendar
4. **Start Using**: Access 12 catalog requests for comprehensive calendar management

## Documentation Structure

### Getting Started
- [Extension Configuration](pages/ExtensionConfiguration.md) - Complete setup parameters and configuration
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication modes and token management
- [Creating Google App](pages/CreatingGoogleApp.md) - Step-by-step Google Cloud setup guide

### Event Management
- [Schedule Meeting](pages/ScheduleMeeting.md) - Create new calendar events with attendees
- [Cancel Event](pages/CancelEvent.md) - Cancel existing calendar events

### Invitation Management
- [Accept Invitation](pages/AcceptInvitation.md) - Accept meeting invitations
- [Decline Invitation](pages/DeclineInvitation.md) - Decline meeting invitations
- [Tentative Invitation](pages/TentativeInvitation.md) - Respond tentatively to invitations

### Attendee Management
- [Add Attendees](pages/AddAttendees.md) - Add attendees to existing events
- [Remove Attendee](pages/RemoveAttendee.md) - Remove attendees from events

### Query Operations
- [Get Event](pages/GetEvent.md) - Retrieve specific event details
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - Get upcoming events
- [Get All Unaccepted Events](pages/GetAllUnacceptedEvents.md) - Find pending invitations
- [Get Events for a Given User](pages/GetEventsForAGivenUser.md) - Retrieve user-specific events

## Support & Resources

- **Extension Version**: 2.0.5
- **Google Calendar API**: v3
- **Authentication**: OAuth 2.0 with PKCE
- **Required Scopes**: `https://www.googleapis.com/auth/calendar`
- **Java Version**: Java 21

## Additional Resources

- [Release Notes](pages/release-notes.md) - Version history and updates
