# Remove Attendee

## Overview

Removes a specific attendee from an existing calendar event, updating the attendee list and sending notifications.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  Yes (Idempotent operation - removing an already removed attendee has no adverse effect)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Event Id | Text | Yes | Unique identifier of the calendar event | "abc123def456ghi789" |
| Attendee Email | Email | Yes | Email address of the attendee to remove | "former.employee@company.com" |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Updated event information with attendee removed |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Event ID is required | "Event ID cannot be empty" | Provide a valid Event ID |
| Attendee Email is required | "Attendee email cannot be empty" | Provide a valid email address |
| Valid email format | "Invalid email address format" | Use proper email format |
| Event must exist | "Event not found" | Verify Event ID exists and is accessible |
| Event must be modifiable | "Insufficient permissions to modify event" | Ensure user has edit access |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: Invalid or missing required parameters
**Resolution**: Validate all input parameters before submission

### Logic Errors (LOGIC_ERROR)
**Cause**: Business logic validation failures
**Resolution**: Verify event exists and user has permissions

### System Errors (SYSTEM_ERROR)
**Cause**: Google Calendar API failures or network issues
**Error Message**: "Attendee removal failed: Unable to remove attendee from calendar event. Required: Valid Event ID, attendee email, and organizer permissions. Resolution: Verify event exists, attendee is on event, user has organizer permissions, and check Google Calendar API connectivity."
**Resolution**: Retry request after delay or re-authenticate if needed

## Usage Examples

### Example 1: Remove Former Employee

**Input**:
```
Event Id: "abc123def456ghi789"
Attendee Email: "former.employee@company.com"
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
  "contacts": ["team@company.com", "manager@company.com"]
}
```

**Result**: Former employee removed from meeting, updated attendee list returned

## Business Rules

1. **Edit Permissions**: User must have edit access to the event
2. **Attendee Filtering**: Removes attendee from the event's attendee list
3. **Notification Handling**: Google Calendar handles cancellation notifications
4. **Idempotent Operation**: Removing non-existent attendee has no negative effect
5. **Organizer Protection**: Cannot remove the event organizer

## Technical Implementation

### Helper Class
- **Class**: MeetingAreaImpl
- **Method**: removeAttendee(String eventId, String attendeeEmail)
- **Service**: Google Calendar API events.patch()

### Implementation Details
1. Retrieve event details using events.get()
2. Filter attendee list to exclude specified email
3. Update event with modified attendee list
4. Return updated event details

## Related Catalog Requests

- [Add Attendees](/pages/AddAttendees.md) - Add attendees to events
- [Get Event](/pages/GetEvent.md) - Review current attendee list
- [Cancel Event](/pages/CancelEvent.md) - Alternative to removing all attendees

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
