# Get All Unaccepted Events

## Overview

Retrieves all calendar events where the authenticated user has pending invitations that require a response (status: "needsAction").

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  Yes (Read-only operation is safe to retry)

## Input Parameters

This request requires no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | List<Entity(Event Details)> | List of events with pending invitations |

## Validation Rules

No input validation required as this request takes no parameters.

## Error Handling

### System Errors (SYSTEM_ERROR)
**Cause**: Google Calendar API failures or network issues
**Error Message**: "Unaccepted events retrieval failed: Unable to fetch events with pending invitations. Required: Valid calendar access and network connectivity. Resolution: Verify calendar permissions, check authentication status, and ensure Google Calendar API connectivity."
**Resolution**: Retry request after delay or re-authenticate if needed

### Authorization Errors
**Cause**: Insufficient permissions or expired authentication
**Resolution**: Re-authenticate or verify calendar permissions

## Usage Examples

### Example 1: Check Pending Invitations

**Input**: No parameters required

**Output**:
```json
[
  {
    "eventId": "abc123def456",
    "title": "Project Review Meeting",
    "location": "Conference Room A",
    "dateFrom": 1654437600000,
    "dateTo": 1654441200000,
    "details": "Quarterly project review",
    "contacts": ["pm@company.com", "team@company.com"]
  },
  {
    "eventId": "def456ghi789",
    "title": "Client Presentation",
    "location": "https://meet.google.com/xyz",
    "dateFrom": 1654524000000,
    "dateTo": 1654527600000,
    "details": "Q2 results presentation",
    "contacts": ["client@external.com", "sales@company.com"]
  }
]
```

**Result**: Retrieved 2 events requiring response from the authenticated user

## Business Rules

1. **Self-Response Only**: Only returns events where the authenticated user needs to respond
2. **Status Filtering**: Filters events where user's response status is "needsAction"
3. **All Calendars**: Searches across all accessible calendars
4. **No Time Filtering**: Returns unaccepted events regardless of date (past or future)
5. **Attendee Validation**: User must be an attendee on the event

## Technical Implementation

### Helper Class
- **Class**: MeetingAreaImpl
- **Method**: getAllUnacceptedEvents()
- **Service**: Google Calendar API events.list()

### Implementation Details
1. Retrieve all events using events.list()
2. Filter events where user is attendee with "needsAction" status
3. Convert filtered events to EventDetails entities
4. Return list of unaccepted events

## Related Catalog Requests

- [Accept Invitation](/pages/AcceptInvitation.md) - Accept pending invitations
- [Decline Invitation](/pages/DeclineInvitation.md) - Decline pending invitations
- [Tentative Invitation](/pages/TentativeInvitation.md) - Respond tentatively

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
