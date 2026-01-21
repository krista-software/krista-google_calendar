# Decline Invitation

## Overview

Declines a meeting invitation for a specific attendee, updating their response status to "declined" in the calendar event.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  Yes (Idempotent operation - declining an already declined invitation has no adverse effect)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Event Id | Text | Yes | Unique identifier of the calendar event | "abc123def456ghi789" |
| Attendee Email | Email | Yes | Email address of the attendee declining the invitation | "john.doe@company.com" |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Updated event information with modified attendee status |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is required | "Event ID cannot be empty" | Provide a valid Event ID |
| Attendee Email is required | "Attendee email cannot be empty" | Provide a valid email address |
| Valid email format | "Invalid email address format" | Use proper email format |
| Event must exist | "Event not found" | Verify Event ID exists and is accessible |
| Attendee must be invited | "Attendee not found in event" | Ensure email is in the original attendee list |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: Invalid or missing required parameters
**Resolution**: Validate all input parameters before submission

### Logic Errors (LOGIC_ERROR)
**Cause**: Business logic validation failures
**Resolution**: Verify event exists and attendee has proper access

### System Errors (SYSTEM_ERROR)
**Cause**: Google Calendar API failures or network issues
**Error Message**: "Invitation decline failed: Unable to update event response status to declined. Required: Valid Event ID and attendee email with pending invitation. Resolution: Verify event exists, attendee has pending invitation, and check Google Calendar API connectivity."
**Resolution**: Retry request after delay or re-authenticate if needed

## Usage Examples

### Example 1: Decline Team Meeting

**Input**:
```
Event Id: "abc123def456ghi789"
Attendee Email: "john.doe@company.com"
```

**Output**:
```json
{
  "eventId": "abc123def456ghi789",
  "title": "Weekly Team Standup",
  "location": "Conference Room B",
  "dateFrom": 1654437600000,
  "dateTo": 1654439400000,
  "details": "Weekly team sync to discuss progress and blockers",
  "contacts": ["team@company.com", "manager@company.com", "john.doe@company.com"]
}
```

**Result**: John Doe's invitation status updated to "declined", organizer notified

## Business Rules

1. **Attendee Validation**: Only existing attendees can decline invitations
2. **Status Update**: Changes attendee response status to "declined"
3. **Notification Sending**: Google Calendar automatically notifies organizer
4. **Idempotent Operation**: Declining an already declined invitation has no negative effect
5. **Permission Requirements**: User must have access to the event

## Technical Implementation

### Helper Class
- **Class**: MeetingAreaImpl
- **Method**: declineInvitation(String eventId, String email)
- **Service**: Google Calendar API events.patch()

### Implementation Details
1. Retrieve event details using events.get()
2. Find attendee in event's attendee list by email
3. Set attendee's responseStatus to "declined"
4. Update event using events.patch()
5. Return updated event details

## Related Catalog Requests

- [Accept Invitation](/pages/AcceptInvitation.md) - Accept meeting invitations
- [Tentative Invitation](/pages/TentativeInvitation.md) - Respond tentatively to invitations
- [Get All Unaccepted Events](/pages/GetAllUnacceptedEvents.md) - Find pending invitations

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
