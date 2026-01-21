# Get Events for a Given User

## Overview

Retrieves calendar events for a specific user within a date range, requiring service account authentication with domain-wide delegation.

## Request Details

- **Area**: Meetings
- **Type**: QUERY_SYSTEM
- **Retry Support**:  Yes (Read-only operation is safe to retry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| User Email | Email | Yes | Email address of the user whose events to retrieve | "employee@company.com" |
| From Date | Date | Yes | Start date for event search (Unix timestamp) | 1654473600000 |
| To Date | Date | Yes | End date for event search (Unix timestamp) | 1654560000000 |

### Parameter Details

**User Email**:
- Must be a valid email address within the organization
- Requires service account setup with domain-wide delegation
- User must be in the same Google Workspace domain

**From Date / To Date**:
- Unix timestamps in milliseconds
- Define the date range for event retrieval
- From Date must be before To Date

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | List<Entity(Event Details)> | List of events for the specified user and date range |

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| User Email is required | "User email cannot be empty" | Provide a valid email address |
| From Date is required | "From date is required" | Provide valid start date |
| To Date is required | "To date is required" | Provide valid end date |
| Valid email format | "Invalid email address format" | Use proper email format |
| Date range validation | "From date must be before to date" | Ensure proper date sequence |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: Invalid or missing required parameters
**Resolution**: Validate all input parameters before submission

### Logic Errors (LOGIC_ERROR)
**Cause**: User access or permission issues
**Resolution**: Verify service account has domain-wide delegation and user exists

### System Errors (SYSTEM_ERROR)
**Cause**: Google Calendar API failures or network issues
**Error Message**: "User events retrieval failed: Unable to fetch calendar events for specified user. Required: Valid user email, date range, and service account with domain-wide delegation. Resolution: Verify user email exists in domain, date range is valid, and service account has proper permissions."
**Resolution**: Retry request after delay or re-authenticate if needed

## Usage Examples

### Example 1: Get Employee's Weekly Events

**Input**:
```
User Email: "employee@company.com"
From Date: 1654473600000 (Mon Jun 06 2022 00:00:00)
To Date: 1654560000000 (Mon Jun 13 2022 00:00:00)
```

**Output**:
```json
[
  {
    "eventId": "abc123def456",
    "title": "Team Meeting",
    "location": "Conference Room A",
    "dateFrom": 1654437600000,
    "dateTo": 1654441200000,
    "details": "Weekly team sync",
    "contacts": ["team@company.com"]
  }
]
```

**Result**: Retrieved employee's events for the specified week

## Business Rules

1. **Service Account Required**: Must use service account authentication with domain-wide delegation
2. **Domain Restriction**: Can only access users within the same Google Workspace domain
3. **Date Range Filtering**: Only returns events within the specified date range
4. **Visibility Rules**: Returns events visible to the service account
5. **Permission Dependent**: Requires appropriate calendar access permissions

## Technical Implementation

### Helper Class
- **Class**: MeetingAreaImpl
- **Method**: getEventsForUser(String userEmail, Long fromDate, Long toDate)
- **Service**: Google Calendar API events.list() with user impersonation

### Implementation Details
1. Impersonate the specified user using service account
2. Query events within the date range
3. Convert events to EventDetails entities
4. Return filtered event list

## Related Catalog Requests

- [Get Event](/pages/GetEvent.md) - Get specific event details
- [Get Next Upcoming Meetings](/pages/GetNextUpcomingMeetings.md) - Get current user's upcoming events

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup service account authentication
- [Authentication](/pages/Authentication.md) - Service account vs user authentication
