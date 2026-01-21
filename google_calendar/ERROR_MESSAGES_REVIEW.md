# Google Calendar Extension - Error Messages Review & Update

## Overview

This document summarizes the comprehensive review and rewrite of all error messages in the Google Calendar extension to follow the standardized format:
1. **Clearly identify the problem**
2. **Explain what is required**
3. **Provide actionable resolution steps**

## Updated Files

### 1. Constants.java
**Location**: `src/main/java/.../impl/util/Constants.java`

#### Before vs After Examples:

**Account Errors**:
- **Before**: `"Failed to get account."`
- **After**: `"Account identification failed: No valid user email found in configuration. Required: Valid email address in extension setup. Resolution: Configure a valid email address in the extension settings and ensure proper authentication."`

**Authentication Errors**:
- **Before**: `"Refresh Token Expired. Please reauthorize yourself"`
- **After**: `"Refresh token expired: Authentication credentials are no longer valid. Required: Fresh OAuth authorization. Resolution: Re-authenticate by clicking 'Validate Attributes' and completing the Google OAuth flow."`

**Authorization Errors**:
- **Before**: `"Authorize yourself and click 'Validate Attributes' before saving changes to proceed further."`
- **After**: `"Authorization required: Extension needs Google Calendar permissions. Required: Valid OAuth credentials. Resolution: Click 'Validate Attributes', complete Google OAuth flow, then save configuration changes."`

### 2. MeetingAreaImpl.java
**Location**: `src/main/java/.../impl/MeetingAreaImpl.java`

#### Updated Error Messages:

**Accept Invitation**:
- **Before**: `"Failed to accept invitation - " + cause.getMessage()`
- **After**: `"Invitation acceptance failed: Unable to update event response status. Required: Valid Event ID and attendee email with pending invitation. Resolution: Verify event exists, attendee has pending invitation, and check Google Calendar API connectivity."`

**Add Attendees**:
- **Before**: `"Failed to add attendees - " + cause.getMessage()`
- **After**: `"Attendee addition failed: Unable to add new attendees to calendar event. Required: Valid Event ID and attendee email addresses. Resolution: Verify event exists, attendee emails are valid, and check Google Calendar API connectivity."`

**Cancel Event**:
- **Before**: `"Failed to cancel event - " + cause.getMessage()`
- **After**: `"Event cancellation failed: Unable to cancel calendar event. Required: Valid Event ID and organizer permissions. Resolution: Verify event exists, user has organizer permissions, and check Google Calendar API connectivity."`

**Schedule Meeting**:
- **Before**: `"Failed to schedule meeting - " + cause.getMessage()`
- **After**: `"Meeting scheduling failed: Unable to create new calendar event. Required: Valid title, date range, and calendar write permissions. Resolution: Verify all required fields are provided, date range is valid, and check Google Calendar API connectivity."`

**Get Events**:
- **Before**: `"Failed to get user events: " + errorMessage`
- **After**: `"User events retrieval failed: Unable to fetch calendar events for specified user. Required: Valid user email, date range, and service account with domain-wide delegation. Resolution: Verify user email exists in domain, date range is valid, and service account has proper permissions."`

### 3. GoogleProvider.java
**Location**: `src/main/java/.../impl/connectors/GoogleProvider.java`

**Service Account Authorization**:
- **Before**: `"You are not authorized. Please ask admin to validate the attributes."`
- **After**: `"Service account authorization failed: Extension lacks proper authentication credentials. Required: Valid service account configuration with domain-wide delegation. Resolution: Contact administrator to validate extension attributes and configure service account permissions."`

### 4. AuthenticationResource.java
**Location**: `src/main/java/.../api/AuthenticationResource.java`

**OAuth Callback Validation**:
- **Before**: `"Invalid state parameters!"`
- **After**: `"OAuth callback validation failed: Invalid state parameter format. Required: Valid OAuth state parameter. Resolution: Restart authentication flow and ensure proper OAuth configuration."`

**User Authorization Mismatch**:
- **Before**: `"Unauthorised user. User email '" + email + "' configured in the setup does not match with authenticated user."`
- **After**: `"User authorization mismatch: Authenticated user email does not match configured email. Required: Authentication with configured email address '" + email + "'. Resolution: Authenticate with the correct Google account or update extension configuration with proper email."`

## Updated Documentation Files

### Catalog Request Pages
All 12 catalog request documentation pages updated with new error messages:

1. **AcceptInvitation.md** - Updated system error messages
2. **AddAttendees.md** - Updated attendee addition error messages
3. **CancelEvent.md** - Updated event cancellation error messages
4. **DeclineInvitation.md** - Updated invitation decline error messages
5. **GetEvent.md** - Updated event retrieval error messages
6. **GetNextUpcomingMeetings.md** - Updated upcoming meetings error messages
7. **GetAllUnacceptedEvents.md** - Updated unaccepted events error messages
8. **GetEventsForAGivenUser.md** - Updated user events error messages
9. **RemoveAttendee.md** - Updated attendee removal error messages
10. **ScheduleMeeting.md** - Updated meeting scheduling error messages
11. **TentativeInvitation.md** - Updated tentative response error messages

### Configuration & Authentication Pages

**authentication.md**:
- Updated token expiration error messages
- Updated authorization requirement messages

**ExtensionConfiguration.md**:
- Updated troubleshooting section with new error message format
- Updated common configuration issue descriptions

## Error Message Format Standards

All error messages now follow this consistent structure:

```
"[Operation] failed: [Specific problem description]. Required: [What is needed]. Resolution: [Actionable steps to resolve]."
```

### Examples:

**Good Format**:
```
"Event retrieval failed: Unable to fetch calendar event details. Required: Valid Event ID and calendar read permissions. Resolution: Verify event ID exists, check calendar access permissions, and ensure Google Calendar API connectivity."
```

**Previous Format**:
```
"Failed to get event for given id - Event not found"
```

## Benefits of Updated Error Messages

1. **Clear Problem Identification**: Users immediately understand what went wrong
2. **Requirements Specification**: Users know exactly what is needed for success
3. **Actionable Resolution**: Users get specific steps to resolve the issue
4. **Consistent Format**: All error messages follow the same structure
5. **Better Debugging**: Developers can quickly identify root causes
6. **Improved User Experience**: Users can self-resolve many issues

## Testing Recommendations

1. **Unit Tests**: Update unit tests to expect new error message formats
2. **Integration Tests**: Verify error messages appear correctly in API responses
3. **Documentation Tests**: Ensure all documentation examples match actual error messages
4. **User Acceptance Testing**: Validate that users can understand and act on new error messages

## Maintenance Guidelines

When adding new error messages:
1. Follow the established format: "[Operation] failed: [Problem]. Required: [Needs]. Resolution: [Steps]."
2. Be specific about the problem and requirements
3. Provide actionable resolution steps
4. Update corresponding documentation
5. Include error message in unit tests
