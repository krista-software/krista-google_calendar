# Google Calendar Extension - Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture Layers](#architecture-layers)
3. [Component Interaction](#component-interaction)
4. [Data Flow](#data-flow)
5. [Authentication & Security](#authentication--security)
6. [Error Handling](#error-handling)
7. [Performance & Limitations](#performance--limitations)
8. [Deployment Architecture](#deployment-architecture)

---

## Overview

### Extension Metadata
- **Name**: Google Calendar Extension
- **Version**: 2.0.5
- **Domain**: Collaboration
- **Ecosystem**: Essentials
- **Java Version**: Java 21
- **API**: Google Calendar API v3
- **Package**: `app.kristasoft.extensions.essentials.collaboration.google_calendar`

### Purpose
The Google Calendar Extension provides seamless integration between the Krista platform and Google Calendar, enabling automated calendar management, meeting scheduling, and event coordination through a comprehensive set of catalog requests.

### Key Capabilities
- **Meeting Management**: Schedule, cancel, and modify calendar events
- **Invitation Handling**: Accept, decline, or tentatively respond to invitations
- **Attendee Management**: Add or remove attendees from events
- **Event Retrieval**: Query events by ID, date range, or user
- **OAuth 2.0 Authentication**: Secure user and service account authentication
- **Dual Authentication Modes**: Support for both personal and service accounts

---

## Architecture Layers

The extension follows a **5-layer architecture** pattern that promotes separation of concerns, testability, and maintainability.

```
┌─────────────────────────────────────────────────────────────┐
│                    LAYER 1: CATALOG                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           MeetingsArea (Catalog Requests)            │   │
│  │  - 12 @CatalogRequest annotated methods             │   │
│  │  - Input validation & type conversion                │   │
│  │  - Request routing to implementation layer           │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                 LAYER 2: IMPLEMENTATION                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         MeetingAreaImpl (Business Logic)             │   │
│  │  - Calendar event operations                         │   │
│  │  - Attendee management logic                         │   │
│  │  - Event transformation & mapping                    │   │
│  │  - Error handling & logging                          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  LAYER 3: CONNECTOR                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  GoogleProvider + GoogleProviderFactory              │   │
│  │  - Google Calendar client management                 │   │
│  │  - OAuth credential handling                         │   │
│  │  - Token refresh & validation                        │   │
│  │  - Connection pooling                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    LAYER 4: STORE                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  RefreshTokenStore + GoogleAttributeStore            │   │
│  │  - OAuth token persistence                           │   │
│  │  - Attribute configuration storage                   │   │
│  │  - Key-value store abstraction                       │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   LAYER 5: UTILITY                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Constants + EventDetails + GoogleAttributes         │   │
│  │  - Error message constants                           │   │
│  │  - Entity definitions & transformations              │   │
│  │  - Configuration management                          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Layer 1: Catalog Layer

**Purpose**: Expose catalog requests to the Krista platform

**Key Components**:
- `MeetingsArea.java` - Catalog request definitions

**Responsibilities**:
- Define 12 catalog requests with @CatalogRequest annotations
- Specify input/output parameters with type annotations
- Route requests to implementation layer
- Provide metadata for Krista platform integration

**Catalog Requests**:
1. Schedule Meeting (CHANGE_SYSTEM)
2. Cancel Event (CHANGE_SYSTEM, Retry: Yes)
3. Accept Invitation (CHANGE_SYSTEM, Retry: Yes)
4. Decline Invitation (CHANGE_SYSTEM, Retry: Yes)
5. Tentative Invitation (CHANGE_SYSTEM, Retry: Yes)
6. Add Attendees (CHANGE_SYSTEM)
7. Remove Attendee (CHANGE_SYSTEM)
8. Get Event (QUERY_SYSTEM, Retry: Yes)
9. Get Next Upcoming Meetings (CHANGE_SYSTEM, Retry: Yes)
10. Get All Unaccepted Events (CHANGE_SYSTEM, Retry: Yes)
11. Get Events for a Given User (QUERY_SYSTEM, Retry: Yes)

### Layer 2: Implementation Layer

**Purpose**: Implement business logic for calendar operations

**Key Components**:
- `MeetingAreaImpl.java` - Business logic implementation

**Responsibilities**:
- Execute Google Calendar API calls
- Transform between Google Event and EventDetails entities
- Handle attendee list manipulation
- Implement retry logic for transient failures
- Comprehensive error handling with descriptive messages
- Logging for debugging and monitoring

**Key Methods**:
- `scheduleMeeting()` - Create new calendar events
- `cancelEvent()` - Cancel existing events
- `acceptInvitation()` / `declineInvitation()` / `tentativeInvitation()` - Manage invitation responses
- `addAttendees()` / `removeAttendee()` - Modify attendee lists
- `getEvent()` - Retrieve single event by ID
- `getNextUpcomingMeetings()` - Query upcoming events
- `getAllUnacceptedEvents()` - Find pending invitations
- `getEventsForUser()` - Query events for specific user (service account mode)

### Layer 3: Connector Layer

**Purpose**: Manage Google Calendar API client connections and authentication

**Key Components**:
- `GoogleProvider.java` - Calendar client provider
- `GoogleProviderFactory.java` - Factory for creating providers
- `GoogleAttributes.java` - Configuration attributes
- `GoogleCalendarAuthenticator.java` - OAuth authenticator

**Responsibilities**:
- Create and manage Google Calendar client instances
- Handle OAuth 2.0 authentication flow
- Refresh expired access tokens automatically
- Support dual authentication modes (user vs service account)
- Manage HTTP transport and JSON factory
- Trigger re-authentication when needed

**Authentication Modes**:
1. **User Account Mode**: Individual user OAuth authentication
2. **Service Account Mode**: Domain-wide delegation for admin operations

### Layer 4: Store Layer

**Purpose**: Persist OAuth tokens and configuration attributes

**Key Components**:
- `RefreshTokenStore.java` - OAuth refresh token storage
- `GoogleAttributeStore.java` - Temporary attribute storage

**Responsibilities**:
- Store and retrieve OAuth refresh tokens
- Manage temporary attribute storage during validation
- Provide key-value abstraction over platform storage
- Handle token lifecycle (put, get, remove)

**Storage Keys**:
- Format: `{email}#{clientId}{clientSecret}`
- Ensures unique storage per configuration

### Layer 5: Utility Layer

**Purpose**: Provide shared utilities, constants, and entity definitions

**Key Components**:
- `Constants.java` - Error messages and constants
- `EventDetails.java` - Event entity definition
- `AuthenticationResource.java` - OAuth callback handler

**Responsibilities**:
- Define error message templates
- Provide entity transformation logic
- Handle OAuth callback processing
- Manage static configuration values

---

## Component Interaction

### Request Flow Diagram

```
┌──────────┐
│  Krista  │
│ Platform │
└────┬─────┘
     │ 1. Catalog Request
     ↓
┌─────────────────┐
│  MeetingsArea   │ ← Catalog Layer
│ (Catalog Entry) │
└────┬────────────┘
     │ 2. Delegate to Implementation
     ↓
┌──────────────────┐
│ MeetingAreaImpl  │ ← Implementation Layer
│ (Business Logic) │
└────┬─────────────┘
     │ 3. Get Calendar Client
     ↓
┌────────────────────┐
│ GoogleProvider     │ ← Connector Layer
│ Factory.create()   │
└────┬───────────────┘
     │ 4. Load Tokens
     ↓
┌──────────────────────┐
│ RefreshTokenStore    │ ← Store Layer
│ .get(userId)         │
└────┬─────────────────┘
     │ 5. Return Token
     ↓
┌──────────────────────┐
│ GoogleProvider       │
│ .getGoogleClient()   │
└────┬─────────────────┘
     │ 6. Build Client with Credentials
     ↓
┌──────────────────────┐
│ Google Calendar API  │ ← External Service
│ (HTTP/REST)          │
└────┬─────────────────┘
     │ 7. API Response
     ↓
┌──────────────────────┐
│ MeetingAreaImpl      │
│ Transform to Entity  │
└────┬─────────────────┘
     │ 8. Return EventDetails
     ↓
┌──────────────────────┐
│ MeetingsArea         │
│ Return to Platform   │
└────┬─────────────────┘
     │ 9. Catalog Response
     ↓
┌──────────┐
│  Krista  │
│ Platform │
└──────────┘
```

### OAuth Authentication Flow

```
┌──────────┐                                    ┌─────────────────┐
│  User    │                                    │ Google OAuth    │
└────┬─────┘                                    └────┬────────────┘
     │ 1. Initiate Request                           │
     ↓                                                │
┌─────────────────┐                                  │
│ MeetingAreaImpl │                                  │
└────┬────────────┘                                  │
     │ 2. Get Calendar Client                        │
     ↓                                                │
┌──────────────────┐                                 │
│ GoogleProvider   │                                 │
└────┬─────────────┘                                 │
     │ 3. Check Token                                │
     ↓                                                │
┌──────────────────┐                                 │
│RefreshTokenStore │                                 │
└────┬─────────────┘                                 │
     │ 4. Token Not Found                            │
     ↓                                                │
┌──────────────────────┐                             │
│ MustAuthorizeException│                            │
└────┬──────────────────┘                            │
     │ 5. Generate Auth URL                          │
     ↓                                                │
┌──────────────────────┐                             │
│GoogleCalendar        │                             │
│Authenticator         │                             │
└────┬─────────────────┘                             │
     │ 6. Redirect to Google ─────────────────────→  │
     │                                                │
     │ 7. User Authorizes  ←─────────────────────────┤
     │                                                │
     │ 8. Callback with Code ←───────────────────────┤
     ↓                                                │
┌──────────────────────┐                             │
│AuthenticationResource│                             │
└────┬─────────────────┘                             │
     │ 9. Exchange Code for Tokens ──────────────→   │
     │                                                │
     │ 10. Access + Refresh Tokens ←─────────────────┤
     ↓                                                │
┌──────────────────┐                                 │
│RefreshTokenStore │                                 │
│ .put(token)      │                                 │
└────┬─────────────┘                                 │
     │ 11. Token Stored                              │
     ↓                                                │
┌──────────────────┐                                 │
│ User Authorized  │                                 │
└──────────────────┘                                 │
```

---

## Data Flow

### Event Creation Flow

```
User Input (Krista Platform)
    ↓
┌─────────────────────────────────────────┐
│ scheduleMeeting(title, location,        │
│   dateFrom, dateTo, details, attendees) │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ prepareEvent()                          │
│ - Create Event object                   │
│ - Set EventDateTime (start/end)         │
│ - Add EventAttendee list                │
│ - Set description, location, summary    │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ getCalendar()                           │
│ - Get GoogleProvider                    │
│ - Retrieve Calendar client              │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ calendar.events().insert(PRIMARY, event)│
│ .execute()                              │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ Google Calendar API                     │
│ POST /calendars/primary/events          │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ Event Response (Google Event object)    │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ EventDetails.getEventDetailsByEvent()   │
│ - Transform Google Event to Entity      │
│ - Extract: id, title, start, end, etc.  │
└─────────────────┬───────────────────────┘
                  ↓
EventDetails Entity (Returned to Platform)
```

### Token Refresh Flow

```
API Request
    ↓
┌─────────────────────────────────────────┐
│ GoogleProvider.getGoogleClient()        │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ RefreshTokenStore.get(userId)           │
│ Returns: Refresh Token                  │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ getGoogleCredential(refreshToken)       │
│ - Build GoogleCredential                │
│ - Set client ID/secret                  │
│ - Set refresh token                     │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ credential.refreshToken()               │
│ - Call Google OAuth token endpoint      │
│ - Exchange refresh token for access     │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ New Access Token (valid 1 hour)         │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ Calendar.Builder()                      │
│ - Create Calendar client with credential│
└─────────────────┬───────────────────────┘
                  ↓
Calendar Client (Ready for API calls)
```

---

## Authentication & Security

### OAuth 2.0 Implementation

**OAuth Library**: ScribeJava 8.3.3

**OAuth Scope**: `https://www.googleapis.com/auth/calendar` (Full calendar access)

**OAuth Flow**: Authorization Code Grant with Offline Access

**Key Security Features**:
1. **Encrypted Client Secret**: Stored encrypted in Krista platform
2. **Refresh Token Persistence**: Securely stored in KeyValueStore
3. **Access Token Auto-Refresh**: Transparent token renewal
4. **State Parameter Validation**: CSRF protection in OAuth flow
5. **HTTPS Only**: All API communication over TLS
6. **Token Revocation**: Support for revoking access

### Authentication Modes

#### 1. User Account Mode (Personal Calendar)

**Use Case**: Individual user accessing their own calendar

**Configuration**:
- Client ID: OAuth 2.0 client ID from Google Cloud Console
- Client Secret: OAuth 2.0 client secret
- Email: User's Google account email

**Flow**:
1. User initiates request
2. Extension checks for refresh token
3. If not found, triggers OAuth authorization
4. User authorizes in browser
5. Extension receives authorization code
6. Exchanges code for access + refresh tokens
7. Stores refresh token for future use

**Token Storage Key**: `{userEmail}#{clientId}{clientSecret}`

#### 2. Service Account Mode (Domain-Wide Delegation)

**Use Case**: Admin accessing multiple users' calendars

**Configuration**:
- Client ID: Service account client ID
- Client Secret: Service account private key
- Email: Service account email

**Requirements**:
- Google Workspace domain
- Service account with domain-wide delegation enabled
- Calendar API scope authorized in Admin Console

**Flow**:
1. Extension uses service account credentials
2. Impersonates target user via domain-wide delegation
3. Accesses user's calendar without individual authorization

**Advantages**:
- No per-user authorization needed
- Centralized credential management
- Suitable for automated workflows

### Security Best Practices

1. **Credential Protection**:
   - Never log client secrets or tokens
   - Use encrypted storage for sensitive data
   - Rotate credentials periodically

2. **Token Management**:
   - Access tokens expire after 1 hour
   - Refresh tokens valid until revoked
   - Automatic token refresh on expiration

3. **Error Handling**:
   - Distinguish authentication vs authorization errors
   - Provide clear re-authentication guidance
   - Log security events for monitoring

4. **Network Security**:
   - All API calls over HTTPS
   - Validate SSL certificates
   - Use secure HTTP transport

---

## Error Handling

### Error Categories

#### 1. INPUT_ERROR (Client Errors)

**Cause**: Invalid input parameters or missing required fields

**HTTP Status**: 400 Bad Request

**Examples**:
- Missing Event ID
- Invalid email format
- Invalid date range (end before start)
- Empty required fields

**Handling**:
- Validate inputs before API calls
- Return descriptive error messages
- No retry (client must fix input)

**Example Error Message**:
```
"Meeting scheduling failed: Unable to create new calendar event.
Required: Valid title, date range, and calendar write permissions.
Resolution: Verify all required fields are provided, date range is valid,
and check Google Calendar API connectivity."
```

#### 2. LOGIC_ERROR (Business Logic Errors)

**Cause**: Operation violates business rules or state constraints

**HTTP Status**: 409 Conflict or 403 Forbidden

**Examples**:
- User not organizer (cannot cancel event)
- Attendee not found in event
- Event already cancelled
- Insufficient permissions

**Handling**:
- Check preconditions before operations
- Provide actionable resolution steps
- No retry (state issue must be resolved)

**Example Error Message**:
```
"Event cancellation failed: Unable to cancel calendar event.
Required: Valid Event ID and organizer permissions.
Resolution: Verify event exists, user has organizer permissions,
and check Google Calendar API connectivity."
```

#### 3. SYSTEM_ERROR (Infrastructure Errors)

**Cause**: External system failures, network issues, or API errors

**HTTP Status**: 500 Internal Server Error, 503 Service Unavailable

**Examples**:
- Network timeout
- Google API rate limiting (429)
- Google service outage
- Authentication token expired (401)

**Handling**:
- Implement retry logic with exponential backoff
- Log errors for monitoring
- Trigger re-authentication for 401 errors

**Example Error Message**:
```
"Upcoming meetings retrieval failed: Unable to fetch future calendar events.
Required: Valid calendar access and network connectivity.
Resolution: Verify calendar permissions, check authentication status,
and ensure Google Calendar API connectivity."
```

### Error Handling Pattern

```java
try {
    // API operation
    Calendar calendar = getCalendar();
    Event event = calendar.events().get(PRIMARY, eventId).execute();
    return EventDetails.getEventDetailsByEvent(event);

} catch (IOException cause) {
    LOGGER.error(cause.getMessage(), cause);
    throw new IllegalStateException(
        "Event retrieval failed: Unable to fetch calendar event details. " +
        "Required: Valid Event ID and calendar read permissions. " +
        "Resolution: Verify event ID exists, check calendar access permissions, " +
        "and ensure Google Calendar API connectivity.",
        cause
    );
}
```

### Retry Strategy

**Retry-Enabled Requests** (allowRetry = true):
- Cancel Event
- Accept/Decline/Tentative Invitation
- Get Event
- Get Next Upcoming Meetings
- Get All Unaccepted Events
- Get Events for User

**Retry Logic**:
1. **Exponential Backoff**: 1s, 2s, 4s, 8s, 16s
2. **Max Retries**: 5 attempts
3. **Retryable Errors**:
   - Network timeouts
   - 429 Rate Limit Exceeded
   - 500 Internal Server Error
   - 503 Service Unavailable

**Non-Retryable Errors**:
- 400 Bad Request (invalid input)
- 401 Unauthorized (trigger re-auth)
- 403 Forbidden (permission denied)
- 404 Not Found (resource doesn't exist)

---

## Performance & Limitations

### Google Calendar API Quotas

**Default Quotas** (per project per day):
- **Queries per day**: 1,000,000 requests
- **Queries per 100 seconds per user**: 1,000 requests
- **Queries per 100 seconds**: 10,000 requests

**Rate Limiting**:
- **HTTP 429**: Too Many Requests
- **Retry-After Header**: Indicates wait time
- **Exponential Backoff**: Required for retry logic

**Quota Management**:
1. Monitor quota usage in Google Cloud Console
2. Request quota increase if needed
3. Implement caching for frequently accessed data
4. Batch operations where possible

### Performance Characteristics

#### Latency Metrics

| Operation | Typical Latency | Notes |
|-----------|----------------|-------|
| Schedule Meeting | 200-500ms | Includes API call + token refresh |
| Get Event | 100-300ms | Single event retrieval |
| Get Upcoming Meetings | 150-400ms | Depends on result count |
| Accept/Decline Invitation | 200-500ms | Requires event patch |
| Add Attendees | 200-500ms | Event modification |
| Cancel Event | 200-500ms | Event deletion |
| Get Events for User | 300-800ms | Service account delegation overhead |

**Factors Affecting Latency**:
- Network latency to Google APIs
- Token refresh overhead (if expired)
- Number of attendees in event
- Result set size for queries
- Geographic location of API servers

#### Throughput Considerations

**Single User**:
- Max: ~10 requests/second (quota limit)
- Recommended: 2-5 requests/second (sustainable)

**Multiple Users**:
- Max: ~100 requests/second (project quota)
- Recommended: 20-50 requests/second (sustainable)

**Optimization Strategies**:
1. **Connection Reuse**: GoogleProvider maintains client instances
2. **Lazy Initialization**: OAuth service created on-demand
3. **Token Caching**: Refresh tokens stored, access tokens auto-refreshed
4. **Efficient Queries**: Use filters and pagination where available

### Scalability Limitations

#### 1. Token Storage

**Current Implementation**: KeyValueStore (in-memory or database)

**Limitations**:
- Storage capacity depends on platform configuration
- No automatic token cleanup
- Token key format: `{email}#{clientId}{clientSecret}`

**Recommendations**:
- Implement token expiration cleanup
- Monitor storage usage
- Consider external token store for high-volume deployments

#### 2. Concurrent Requests

**Thread Safety**:
- GoogleProvider: Thread-safe (synchronized OAuth service creation)
- RefreshTokenStore: Thread-safe (backed by platform KeyValueStore)
- MeetingAreaImpl: Stateless (safe for concurrent use)

**Concurrency Limits**:
- Limited by Google API quotas
- No artificial concurrency limits in extension
- Platform may impose request queue limits

#### 3. Event Size Limits

**Google Calendar Limits**:
- **Event Description**: 8,192 characters
- **Event Title**: 1,024 characters
- **Attendees per Event**: 200 attendees
- **Events per Calendar**: No hard limit (performance degrades >10,000)

**Extension Handling**:
- No validation of size limits (relies on API errors)
- Large attendee lists may impact performance
- Consider pagination for large result sets

### Performance Best Practices

#### 1. Minimize API Calls

**Bad Practice**:
```java
// Multiple API calls in loop
for (String eventId : eventIds) {
    EventDetails event = getEvent(eventId);
    processEvent(event);
}
```

**Good Practice**:
```java
// Single API call with batch or list
List<EventDetails> events = getNextUpcomingMeetings(eventIds.size());
events.forEach(this::processEvent);
```

#### 2. Cache Frequently Accessed Data

**Cacheable Data**:
- Event details (with TTL)
- User calendar settings
- Attendee lists

**Non-Cacheable Data**:
- Real-time availability
- Pending invitations
- Recent event changes

#### 3. Use Appropriate Query Filters

**Efficient Query**:
```java
// Filter on server side
Events events = service.events()
    .list(PRIMARY)
    .setTimeMin(startDate)
    .setTimeMax(endDate)
    .setMaxResults(10)
    .setSingleEvents(true)
    .execute();
```

**Inefficient Query**:
```java
// Fetch all, filter client side
Events events = service.events().list(PRIMARY).execute();
List<Event> filtered = events.getItems().stream()
    .filter(e -> e.getStart().after(startDate))
    .limit(10)
    .collect(Collectors.toList());
```

#### 4. Implement Circuit Breaker Pattern

**Purpose**: Prevent cascading failures during API outages

**Implementation**:
```java
// Pseudo-code
if (consecutiveFailures > threshold) {
    throw new ServiceUnavailableException("Circuit breaker open");
}

try {
    result = callGoogleAPI();
    consecutiveFailures = 0;
} catch (IOException e) {
    consecutiveFailures++;
    throw e;
}
```

### Known Limitations

#### 1. Single Refresh Token per User

**Issue**: Google provides only one refresh token per OAuth client

**Impact**:
- Re-authorization revokes previous refresh token
- Multiple devices/sessions share same token
- Token revocation affects all sessions

**Workaround**:
- Use service account for automated workflows
- Educate users about re-authorization impact

#### 2. No Real-Time Event Notifications

**Issue**: Extension uses polling, not push notifications

**Impact**:
- Cannot react to external calendar changes immediately
- Must periodically query for updates
- Increased API quota usage

**Workaround**:
- Implement Google Calendar Push Notifications (future enhancement)
- Use reasonable polling intervals (5-15 minutes)

#### 3. Service Account Delegation Overhead

**Issue**: Service account mode requires domain-wide delegation setup

**Impact**:
- Complex initial configuration
- Requires Google Workspace admin privileges
- Not available for personal Google accounts

**Workaround**:
- Use user account mode for personal calendars
- Provide clear setup documentation for admins

#### 4. Time Zone Handling

**Issue**: Google Calendar API uses RFC3339 timestamps

**Impact**:
- Time zone conversion required for display
- Potential for time zone mismatch errors
- Daylight saving time complications

**Workaround**:
- Always use UTC for API calls
- Convert to user's time zone for display
- Test across time zone boundaries

---

## Deployment Architecture

### Component Deployment

```
┌─────────────────────────────────────────────────────────┐
│                   Krista Platform                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │         Extension Runtime Environment             │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │   Google Calendar Extension (JAR)           │  │  │
│  │  │   - MeetingsArea (Catalog)                  │  │  │
│  │  │   - MeetingAreaImpl (Implementation)        │  │  │
│  │  │   - GoogleProvider (Connector)              │  │  │
│  │  │   - Stores (RefreshToken, Attributes)       │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  │                                                     │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │   Platform Services                         │  │  │
│  │  │   - KeyValueStore                           │  │  │
│  │  │   - RequestContext                          │  │  │
│  │  │   - AuthorizationContext                    │  │  │
│  │  │   - Invoker                                 │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                        ↕ HTTPS
┌─────────────────────────────────────────────────────────┐
│              Google Cloud Platform                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Google Calendar API v3                         │  │
│  │   - Events Endpoint                              │  │
│  │   - Calendars Endpoint                           │  │
│  │   - ACL Endpoint                                 │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Google OAuth 2.0                               │  │
│  │   - Authorization Endpoint                       │  │
│  │   - Token Endpoint                               │  │
│  │   - Revocation Endpoint                          │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Dependency Management

**Build Tool**: Gradle 8.5

**Key Dependencies**:
```gradle
// Krista Platform APIs
compileOnly 'app.krista:krista-apis:1.0.115'
annotationProcessor 'app.krista:extension-impl-anno-processors:1.0.115'

// Google Calendar API
implementation 'com.google.apis:google-api-services-calendar:v3-rev305-1.23.0'

// OAuth 2.0
implementation 'com.github.scribejava:scribejava-apis:8.3.3'

// Dependency Injection
compileOnly 'org.glassfish.hk2:hk2-api:2.6.1'

// JAX-RS (REST API)
implementation 'org.glassfish.jersey.media:jersey-media-multipart:2.35'
```

### Configuration Management

**Extension Attributes** (configured in Krista platform):
1. **clientId** (Text): OAuth 2.0 Client ID from Google Cloud Console
2. **clientSecret** (Text, Secured): OAuth 2.0 Client Secret
3. **email** (Text): Google account email for service account mode

**Runtime Configuration**:
- Routing URL: Dynamically retrieved from Invoker
- Callback URL: `{routingUrl}/rest/googlecalendar/callback`
- OAuth Scope: `https://www.googleapis.com/auth/calendar`

### Monitoring & Observability

**Logging**:
- **Framework**: SLF4J
- **Log Levels**: ERROR, INFO
- **Key Log Points**:
  - API call initiation
  - Authentication events
  - Error conditions
  - Token refresh operations

**Metrics to Monitor**:
1. **Request Latency**: Time per catalog request
2. **Error Rate**: Failed requests / total requests
3. **Token Refresh Rate**: Frequency of token refreshes
4. **API Quota Usage**: Requests per day/hour
5. **Authentication Failures**: Failed OAuth attempts

**Health Checks**:
- `TEST_CONNECTION` invoker request
- Validates OAuth credentials
- Tests API connectivity
- Returns success/failure status

### Disaster Recovery

**Token Loss Scenarios**:
1. **KeyValueStore Failure**: Users must re-authorize
2. **Token Corruption**: Automatic re-authentication triggered
3. **Google Token Revocation**: User notified to re-authorize

**Recovery Procedures**:
1. Clear corrupted tokens from store
2. Trigger re-authentication flow
3. Validate new tokens with test connection
4. Resume normal operations

**Backup Considerations**:
- No backup needed for access tokens (short-lived)
- Refresh tokens should be backed up with KeyValueStore
- Configuration attributes backed up with platform data

---

## Appendix

### Class Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    GoogleCalendarExtension                   │
│  @Extension(version="2.0.5", name="Google Calendar")        │
├──────────────────────────────────────────────────────────────┤
│  - invoker: Invoker                                          │
│  - gmailAttributes: GoogleAttributes                         │
│  - clientProviderFactory: GoogleProviderFactory              │
│  - requestAuthenticator: GoogleCalendarAuthenticator         │
├──────────────────────────────────────────────────────────────┤
│  + customTabs(): Map<String, String>                         │
│  + validateConnection(Map): void                             │
│  + testConnection(): void                                    │
│  + attributesUpdated(Map, Map): void                         │
│  + getRequestAuthenticator(): RequestAuthenticator           │
└──────────────────────────────────────────────────────────────┘
                            │
                            │ uses
                            ↓
┌──────────────────────────────────────────────────────────────┐
│                       MeetingsArea                           │
│  @Domain(name="Collaboration", ecosystem="Essentials")       │
├──────────────────────────────────────────────────────────────┤
│  - meetingArea: MeetingAreaImpl                              │
├──────────────────────────────────────────────────────────────┤
│  + scheduleMeeting(...): EventDetails                        │
│  + cancelEvent(...): EventDetails                            │
│  + acceptInvitation(...): EventDetails                       │
│  + declineInvitation(...): EventDetails                      │
│  + tentativeInvitation(...): EventDetails                    │
│  + addAttendees(...): EventDetails                           │
│  + removeAttendee(...): EventDetails                         │
│  + getEvent(...): EventDetails                               │
│  + getNextUpcomingMeetings(...): List<EventDetails>          │
│  + getAllUnacceptedEvents(): List<EventDetails>              │
│  + getEventsForAGivenUser(...): List<EventDetails>           │
└──────────────────────────────────────────────────────────────┘
                            │
                            │ delegates to
                            ↓
┌──────────────────────────────────────────────────────────────┐
│                     MeetingAreaImpl                          │
├──────────────────────────────────────────────────────────────┤
│  - clientProviderFactory: GoogleProviderFactory              │
│  - requestContext: RequestContext                            │
├──────────────────────────────────────────────────────────────┤
│  + scheduleMeeting(...): EventDetails                        │
│  + cancelEvent(...): EventDetails                            │
│  + acceptInvitation(...): EventDetails                       │
│  + declineInvitation(...): EventDetails                      │
│  + tentativeInvitation(...): EventDetails                    │
│  + addAttendees(...): EventDetails                           │
│  + removeAttendee(...): EventDetails                         │
│  + getEvent(...): EventDetails                               │
│  + getNextUpcomingMeetings(...): List<EventDetails>          │
│  + getAllUnacceptedEvents(): List<EventDetails>              │
│  + getEventsForUser(...): List<EventDetails>                 │
│  - getCalendar(): Calendar                                   │
│  - getEventAttendees(List): List<EventAttendee>              │
│  - prepareEvent(...): Event                                  │
└──────────────────────────────────────────────────────────────┘
                            │
                            │ uses
                            ↓
┌──────────────────────────────────────────────────────────────┐
│                   GoogleProviderFactory                      │
├──────────────────────────────────────────────────────────────┤
│  - refreshTokenStore: RefreshTokenStore                      │
│  - googleAttributeStore: GoogleAttributeStore                │
│  - requestContext: RequestContext                            │
│  - authorizationContext: AuthorizationContext                │
│  - defaultClientProvider: GoogleProvider                     │
├──────────────────────────────────────────────────────────────┤
│  + create(GoogleAttributes): GoogleProvider                  │
│  + create(): GoogleProvider                                  │
└──────────────────────────────────────────────────────────────┘
                            │
                            │ creates
                            ↓
┌──────────────────────────────────────────────────────────────┐
│                      GoogleProvider                          │
├──────────────────────────────────────────────────────────────┤
│  - refreshTokenStore: RefreshTokenStore                      │
│  - gmailAttributes: GoogleAttributes                         │
│  - requestContext: RequestContext                            │
│  - authorizationContext: AuthorizationContext                │
│  - authContextId: String                                     │
├──────────────────────────────────────────────────────────────┤
│  + getGoogleClient(): Calendar                               │
│  + getGoogleClientForAdmin(): Calendar                       │
│  + testConnection(): void                                    │
│  - getGoogleClient(boolean): Calendar                        │
│  - getGoogle(String, String, Boolean): Calendar              │
│  - getGoogleCredential(...): GoogleCredential                │
│  - createMustAuthorizationException(...): Exception          │
│  - getUserId(boolean): String                                │
└──────────────────────────────────────────────────────────────┘
```

### Sequence Diagram: Schedule Meeting

```
User → MeetingsArea: scheduleMeeting(title, location, dates, attendees)
MeetingsArea → MeetingAreaImpl: scheduleMeeting(...)
MeetingAreaImpl → MeetingAreaImpl: prepareEvent(...)
MeetingAreaImpl → GoogleProviderFactory: create()
GoogleProviderFactory → GoogleProvider: new GoogleProvider(...)
GoogleProvider → RefreshTokenStore: get(userId)
RefreshTokenStore → GoogleProvider: refreshToken
GoogleProvider → GoogleProvider: getGoogleCredential(refreshToken)
GoogleProvider → Google OAuth: refreshToken()
Google OAuth → GoogleProvider: accessToken
GoogleProvider → GoogleProvider: new Calendar.Builder(...)
GoogleProvider → MeetingAreaImpl: Calendar client
MeetingAreaImpl → Google Calendar API: events().insert(event).execute()
Google Calendar API → MeetingAreaImpl: Event response
MeetingAreaImpl → EventDetails: getEventDetailsByEvent(event)
EventDetails → MeetingAreaImpl: EventDetails entity
MeetingAreaImpl → MeetingsArea: EventDetails
MeetingsArea → User: EventDetails
```

### Error Scenario Diagram

```
User → MeetingAreaImpl: getEvent(invalidEventId)
MeetingAreaImpl → GoogleProvider: getGoogleClient()
GoogleProvider → RefreshTokenStore: get(userId)
RefreshTokenStore → GoogleProvider: null (no token)
GoogleProvider → GoogleProvider: createMustAuthorizationException()
GoogleProvider → MeetingAreaImpl: throw MustAuthorizeException
MeetingAreaImpl → GoogleCalendarAuthenticator: getMustAuthorizeResponse()
GoogleCalendarAuthenticator → GoogleAttributes: getOAuth20Service()
GoogleAttributes → GoogleCalendarAuthenticator: OAuth20Service
GoogleCalendarAuthenticator → OAuth20Service: getAuthorizationUrl(state)
OAuth20Service → GoogleCalendarAuthenticator: authUrl
GoogleCalendarAuthenticator → User: AuthorizationResponse(authUrl)
User → Browser: Redirect to Google OAuth
Browser → Google OAuth: User authorizes
Google OAuth → AuthenticationResource: callback(code, state)
AuthenticationResource → OAuth20Service: getAccessToken(code)
OAuth20Service → AuthenticationResource: OAuth2AccessToken
AuthenticationResource → RefreshTokenStore: put(userId, refreshToken)
RefreshTokenStore → AuthenticationResource: success
AuthenticationResource → User: "User authenticated successfully"
```

---

## Conclusion

The Google Calendar Extension provides a robust, scalable, and secure integration between the Krista platform and Google Calendar. Its 5-layer architecture ensures separation of concerns, maintainability, and testability. The extension handles OAuth 2.0 authentication transparently, supports both user and service account modes, and implements comprehensive error handling with retry logic.

**Key Strengths**:
- ✅ Clean layered architecture
- ✅ Comprehensive error handling
- ✅ Automatic token refresh
- ✅ Dual authentication modes
- ✅ Retry logic for transient failures
- ✅ Detailed logging and monitoring
- ✅ Well-documented APIs

**Areas for Future Enhancement**:
- 🔄 Implement Google Calendar Push Notifications
- 🔄 Add caching layer for frequently accessed events
- 🔄 Implement circuit breaker pattern
- 🔄 Add metrics collection and dashboards
- 🔄 Support batch operations for bulk updates
- 🔄 Add token cleanup/expiration management

For questions or support, refer to the extension documentation or contact the development team.


