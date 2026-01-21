# Get Next Upcoming Meetings

## Overview

Retrieves a specified number of upcoming calendar events, ordered by start time, to help users view their immediate schedule.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  Yes (Read-only operation is safe to retry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Number of meeting | Number | Yes | Maximum number of upcoming events to retrieve | 5 |

### Parameter Details

**Number of meeting**:
- Must be a positive integer
- Minimum value: 1
- Maximum recommended: 100 (Google Calendar API limit)
- Determines how many upcoming events to return
- Events are ordered by start time (earliest first)

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | List<Entity(Event Details)> | List of upcoming events ordered by start time |

### Event Details Entity Structure

Each event in the list contains:
- **Event Id**: Unique event identifier
- **Title**: Event title/summary
- **Location**: Event location
- **Date From**: Event start date and time (Unix timestamp)
- **Date To**: Event end date and time (Unix timestamp)
- **Details**: Event description
- **Contacts**: List of attendee email addresses

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Number of meetings is required | "Number of meetings cannot be empty" | Provide a valid positive number |
| Number must be positive | "Number of meetings must be greater than 0" | Use a positive integer value |
| Number within limits | "Number exceeds maximum limit" | Use a value within API limits (≤100) |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: Invalid or missing number parameter
**Common Scenarios**:
- Null or empty number value
- Negative or zero values
- Non-numeric input
- Values exceeding API limits

**Resolution**: Provide a valid positive integer within acceptable range

### Logic Errors (LOGIC_ERROR)
**Cause**: Calendar access or permission issues
**Common Scenarios**:
- No calendar access permissions
- Calendar not found or inaccessible
- User account issues

**Resolution**: Verify calendar access permissions and user authentication

### System Errors (SYSTEM_ERROR)
**Cause**: Google Calendar API failures or network issues
**Common Scenarios**:
- API rate limiting
- Network connectivity problems
- Google service outages
- Authentication token expiration

**Error Message**: "Upcoming meetings retrieval failed: Unable to fetch future calendar events. Required: Valid calendar access and network connectivity. Resolution: Verify calendar permissions, check authentication status, and ensure Google Calendar API connectivity."

**Resolution**: Retry request after delay or re-authenticate if needed

### Authorization Errors
**Cause**: Insufficient permissions or expired authentication
**Common Scenarios**:
- OAuth token expired or invalid
- Calendar read permissions revoked
- User account suspended

**Resolution**: Re-authenticate or verify calendar permissions

## Usage Examples

### Example 1: Get Next 3 Meetings

**Input**:
```
Number of meeting: 3
```

**Output**:
```json
[
  {
    "eventId": "abc123def456",
    "title": "Daily Standup",
    "location": "Conference Room A",
    "dateFrom": 1654437600000,
    "dateTo": 1654439400000,
    "details": "Daily team sync",
    "contacts": ["team@company.com"]
  },
  {
    "eventId": "def456ghi789",
    "title": "Client Review",
    "location": "https://meet.google.com/xyz",
    "dateFrom": 1654441200000,
    "dateTo": 1654444800000,
    "details": "Weekly client check-in",
    "contacts": ["client@external.com", "pm@company.com"]
  },
  {
    "eventId": "ghi789jkl012",
    "title": "Project Planning",
    "location": "Conference Room B",
    "dateFrom": 1654448400000,
    "dateTo": 1654452000000,
    "details": "Sprint planning session",
    "contacts": ["dev-team@company.com"]
  }
]
```

**Result**: Retrieved 3 upcoming meetings ordered by start time

### Example 2: Get Next 10 Meetings for Weekly Planning

**Input**:
```
Number of meeting: 10
```

**Output**: List of 10 upcoming events with complete details

**Result**: Comprehensive view of upcoming week's schedule for planning purposes

### Example 3: No Upcoming Meetings

**Input**:
```
Number of meeting: 5
```

**Output**:
```json
[]
```

**Result**: Empty list returned when no upcoming meetings are scheduled

## Business Rules

1. **Time Filtering**: Only returns events starting after the current time
2. **Chronological Order**: Events are sorted by start time (earliest first)
3. **Single Events Only**: Returns individual event instances (recurring events expanded)
4. **Calendar Scope**: Searches the authenticated user's primary calendar
5. **Visibility Rules**: Only returns events visible to the authenticated user
6. **Status Independent**: Returns events regardless of acceptance status
7. **All-Day Events**: Includes all-day events in the results

## Limitations

1. **Primary Calendar Only**: Searches only the user's primary calendar
2. **No Date Range**: Cannot specify custom date ranges (always from now forward)
3. **No Filtering**: Cannot filter by event type, attendees, or other criteria
4. **API Rate Limits**: Subject to Google Calendar API quotas
5. **No Recurring Event Details**: Recurring events are expanded but don't show recurrence rules
6. **Limited Sorting**: Only sorted by start time (no other sorting options)

## Best Practices

### 1. **Reasonable Limits**
Request only the number of events you actually need to avoid unnecessary API usage

### 2. **Caching Strategy**
Consider caching results for frequently accessed upcoming events

### 3. **Error Handling**
Implement proper error handling for empty results and API failures

### 4. **User Experience**
Display results in a user-friendly format with clear time information

### 5. **Refresh Strategy**
Refresh upcoming events periodically to keep information current

### 6. **Time Zone Awareness**
Handle time zone display appropriately for user's location

## Common Use Cases

### 1. **Dashboard Display**
```
Scenario: Show upcoming meetings on user dashboard
Action: Get next 5 meetings for quick overview
Result: User sees immediate schedule at a glance
```

### 2. **Meeting Preparation**
```
Scenario: Prepare for upcoming meetings by reviewing details
Action: Get next 3 meetings to review agenda and attendees
Result: User can prepare effectively for immediate meetings
```

### 3. **Schedule Planning**
```
Scenario: Plan work schedule around upcoming meetings
Action: Get next 10 meetings to understand availability
Result: User can plan tasks around meeting commitments
```

### 4. **Mobile Quick View**
```
Scenario: Quick check of upcoming schedule on mobile device
Action: Get next 2-3 meetings for immediate awareness
Result: User stays informed of immediate commitments
```

## Related Catalog Requests

- [Get Event](GetEvent.md) - Get detailed information for specific events
- [Get All Unaccepted Events](GetAllUnacceptedEvents.md) - Find pending invitations
- [Schedule Meeting](ScheduleMeeting.md) - Create new meetings that will appear in upcoming list
- [Accept Invitation](AcceptInvitation.md) - Respond to upcoming meeting invitations

## Technical Implementation

### Helper Class
- **Class**: MeetingAreaImpl
- **Package**: app.kristasoft.extensions.essentials.collaboration.google_calendar.impl
- **Method**: getNextUpcomingMeetings(Double numberOfMeeting)
- **Service**: Google Calendar API events.list()

### Implementation Details
1. **Current Time**: Get current timestamp using System.currentTimeMillis()
2. **API Query**: Use events.list() with timeMin set to current time
3. **Limit Results**: Set maxResults to the requested number
4. **Sort Order**: Set orderBy to "startTime" for chronological order
5. **Single Events**: Set singleEvents to true to expand recurring events
6. **Process Results**: Convert Event objects to EventDetails entities

### API Integration
- **API Endpoint**: Google Calendar API v3 events.list
- **Authentication**: OAuth 2.0 with calendar scope
- **Parameters**:
  - `calendarId`: "primary"
  - `timeMin`: Current DateTime
  - `maxResults`: Requested number
  - `orderBy`: "startTime"
  - `singleEvents`: true

### Query Parameters
```java
Events events = service.events()
    .list(PRIMARY)
    .setMaxResults(numberOfMeeting.intValue())
    .setTimeMin(now)
    .setOrderBy("startTime")
    .setSingleEvents(true)
    .execute();
```

## Troubleshooting

### No Results Returned
**Cause**: No upcoming meetings scheduled
**Solution**: Normal behavior - empty list is valid response

### Permission Denied
**Cause**: Insufficient calendar read permissions
**Solution**: Verify OAuth token and calendar access rights

### Authentication Errors
**Cause**: Expired or invalid OAuth token
**Solution**: Re-authenticate and refresh access token

### API Rate Limiting
**Cause**: Too many requests in short time period
**Solution**: Implement exponential backoff and respect rate limits

### Incorrect Time Zone
**Cause**: Time zone handling issues in display
**Solution**: Ensure proper time zone conversion for user display

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Get Event](/pages/GetEvent.md) - Get detailed information for specific events
- [Get All Unaccepted Events](/pages/GetAllUnacceptedEvents.md) - Find pending invitations
