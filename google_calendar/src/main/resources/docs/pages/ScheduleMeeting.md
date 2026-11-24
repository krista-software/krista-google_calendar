# Schedule Meeting

## Overview

Creates a new calendar event with specified details including title, location, date/time, description, and attendees.

## Request Details

- **Area**: Meetings
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (Event creation is not idempotent - retry could create duplicate events)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Title | Text | Yes | Meeting title/subject | "Company Update Meeting" |
| Location | Text | No | Meeting location or venue | "Conference Room A" |
| Date From | Date | Yes | Meeting start date and time | 1654437600000 (5 Jun 2022 17:00) |
| Date to | Date | Yes | Meeting end date and time | 1654441200000 (5 Jun 2022 18:00) |
| Details | RichText | Yes | Meeting description or agenda | "Quarterly company updates and announcements" |
| Attendee Email | List<Email> | Yes | List of attendee email addresses | ["john@company.com", "jane@company.com"] |

### Parameter Details

**Title**: 
- Maximum length: 1024 characters
- Used as the event summary in Google Calendar
- Displayed in calendar views and notifications

**Location**:
- Optional field for meeting venue
- Can include physical addresses, room names, or virtual meeting links
- Displayed in calendar event details

**Date From/Date To**:
- Unix timestamp in milliseconds
- Must be valid future dates
- Date From must be before Date To
- Timezone handled by Google Calendar based on user settings

**Details**:
- Rich text format supported
- Can include HTML formatting
- Used as event description in Google Calendar
- Visible to all attendees

**Attendee Email**:
- Must be valid email addresses
- Attendees will receive calendar invitations
- Can include both internal and external email addresses

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Event Details | Entity(Event Details) | Complete event information including generated Event ID |

### Event Details Entity Structure

- **Event Id**: Unique identifier for the created event
- **Title**: Meeting title
- **Location**: Meeting location
- **Date From**: Start date/time
- **Date To**: End date/time
- **Details**: Meeting description
- **Contacts**: List of attendee email addresses

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Title is required | "Title cannot be empty" | Provide a valid meeting title |
| Date From is required | "Start date is required" | Provide valid start date/time |
| Date To is required | "End date is required" | Provide valid end date/time |
| Date From must be before Date To | "Start date must be before end date" | Ensure proper date sequence |
| At least one attendee required | "At least one attendee email is required" | Add valid attendee email addresses |
| Invalid email format | "Invalid email address format" | Provide valid email addresses |
| Date cannot be in the past | "Cannot schedule meetings in the past" | Use future dates only |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: Invalid or missing required parameters
**Common Scenarios**:
- Empty title or required fields
- Invalid date formats or past dates
- Malformed email addresses
- End date before start date

**Resolution**: Validate all input parameters before submission

### Logic Errors (LOGIC_ERROR)
**Cause**: Business logic validation failures
**Common Scenarios**:
- Conflicting meeting times
- Invalid attendee permissions
- Calendar access restrictions

**Resolution**: Check calendar permissions and attendee availability

### System Errors (SYSTEM_ERROR)
**Cause**: Google Calendar API failures or network issues
**Common Scenarios**:
- API rate limiting
- Network connectivity issues
- Google service outages
- Authentication token expiration

**Resolution**: Retry after delay or re-authenticate if needed

### Authorization Errors
**Cause**: Insufficient permissions or expired authentication
**Error Messages**:
- "Meeting scheduling failed: Unable to create new calendar event. Required: Valid title, date range, and calendar write permissions. Resolution: Verify all required fields are provided, date range is valid, and check Google Calendar API connectivity."
- "Calendar access denied"

**Resolution**: Re-authenticate or check calendar permissions

## Usage Examples

### Example 1: Basic Team Meeting

**Input**:
```
Title: "Weekly Team Standup"
Location: "Conference Room B"
Date From: 1654437600000 (Mon Jun 06 2022 09:00:00)
Date To: 1654439400000 (Mon Jun 06 2022 09:30:00)
Details: "Weekly team sync to discuss progress and blockers"
Attendee Email: ["team@company.com", "manager@company.com"]
```

**Output**:
```json
{
  "eventId": "abc123def456",
  "title": "Weekly Team Standup",
  "location": "Conference Room B",
  "dateFrom": 1654437600000,
  "dateTo": 1654439400000,
  "details": "Weekly team sync to discuss progress and blockers",
  "contacts": ["team@company.com", "manager@company.com"]
}
```

### Example 2: Client Presentation

**Input**:
```
Title: "Q2 Results Presentation"
Location: "https://meet.google.com/abc-defg-hij"
Date From: 1654524000000 (Tue Jun 07 2022 10:00:00)
Date To: 1654527600000 (Tue Jun 07 2022 11:00:00)
Details: "Quarterly results presentation for key stakeholders"
Attendee Email: ["client@external.com", "sales@company.com", "ceo@company.com"]
```

**Output**:
```json
{
  "eventId": "xyz789uvw012",
  "title": "Q2 Results Presentation",
  "location": "https://meet.google.com/abc-defg-hij",
  "dateFrom": 1654524000000,
  "dateTo": 1654527600000,
  "details": "Quarterly results presentation for key stakeholders",
  "contacts": ["client@external.com", "sales@company.com", "ceo@company.com"]
}
```

### Example 3: All-Day Event

**Input**:
```
Title: "Company Retreat"
Location: "Mountain Resort, Colorado"
Date From: 1654473600000 (Mon Jun 06 2022 00:00:00)
Date To: 1654560000000 (Tue Jun 07 2022 00:00:00)
Details: "Annual company retreat with team building activities"
Attendee Email: ["all-staff@company.com"]
```

## Business Rules

1. **Meeting Duration**: Minimum duration is 15 minutes, maximum is 24 hours
2. **Attendee Limits**: Maximum 100 attendees per meeting (Google Calendar limit)
3. **Future Dates Only**: Cannot schedule meetings in the past
4. **Time Zone Handling**: All times are processed in the authenticated user's timezone
5. **Invitation Sending**: Calendar invitations are automatically sent to all attendees
6. **Organizer Assignment**: The authenticated user becomes the meeting organizer
7. **Default Visibility**: Meetings are created with default visibility settings
8. **Recurrence**: This request creates single events only (no recurring meetings)

## Limitations

1. **No Recurrence Support**: Cannot create recurring meetings through this request
2. **Basic Formatting**: Rich text support is limited to basic HTML formatting
3. **Attachment Restrictions**: Cannot attach files directly to calendar events
4. **Room Booking**: Does not integrate with Google Workspace room booking
5. **External Calendar Limits**: Some features may be limited for external attendees
6. **API Rate Limits**: Subject to Google Calendar API quotas and rate limiting

## Best Practices

### 1. **Descriptive Titles**
Use clear, descriptive meeting titles that indicate purpose and context

### 2. **Detailed Descriptions**
Include agenda, dial-in information, and relevant links in the details field

### 3. **Appropriate Duration**
Schedule realistic meeting durations with buffer time between meetings

### 4. **Attendee Management**
Only invite necessary participants to avoid calendar clutter

### 5. **Location Information**
Provide complete location details including room numbers or virtual meeting links

### 6. **Time Zone Awareness**
Be mindful of attendee time zones when scheduling cross-regional meetings

## Common Use Cases

### 1. **Recurring Team Meetings**
```
Scenario: Schedule weekly team standup meetings
Action: Create meeting with consistent title, location, and attendees
Result: Team members receive calendar invitation and can plan accordingly
```

### 2. **Client Presentations**
```
Scenario: Schedule important client presentation
Action: Include client emails, meeting location/link, and detailed agenda
Result: Professional calendar invitation sent to all stakeholders
```

### 3. **Company Events**
```
Scenario: Schedule all-hands company meeting
Action: Use company-wide distribution list and detailed event information
Result: Organization-wide calendar event with proper visibility
```

## Related Catalog Requests

- [Cancel Event](pages/CancelEvent.md) - Cancel scheduled meetings
- [Add Attendees](pages/AddAttendees.md) - Add attendees to existing meetings
- [Get Event](pages/GetEvent.md) - Retrieve meeting details
- [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md) - View scheduled meetings

## Technical Implementation

### Helper Class
- **Class**: MeetingAreaImpl
- **Package**: app.kristasoft.extensions.essentials.collaboration.google_calendar.impl
- **Method**: scheduleMeeting(String title, String location, Long dateFrom, Long dateTo, String details, List<String> attendeeEmail)
- **Service**: Google Calendar API events.insert()

### Event Preparation
The system uses `prepareEvent()` method to construct the Google Calendar Event object:
- Sets event summary (title)
- Configures start and end DateTime objects
- Creates EventAttendee objects for each email
- Sets event description and location

### API Integration
- **API Endpoint**: Google Calendar API v3 events.insert
- **Authentication**: OAuth 2.0 with calendar scope
- **Response**: Complete Event object with generated ID

## Troubleshooting

### Meeting Not Created
**Cause**: API authentication or permission issues
**Solution**: Verify OAuth token and calendar permissions

### Attendees Not Receiving Invitations
**Cause**: Email delivery issues or spam filtering
**Solution**: Check email addresses and spam folders

### Time Zone Confusion
**Cause**: Incorrect timestamp conversion
**Solution**: Ensure timestamps are in correct timezone format

### Duplicate Meetings
**Cause**: Multiple request submissions
**Solution**: Implement client-side duplicate prevention

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication](/pages/Authentication.md) - OAuth 2.0 authentication details
- [Creating Google App](/pages/CreatingGoogleApp.md) - Google Cloud setup guide
