# Extension Configuration

## Overview

The Google Calendar Extension requires proper configuration of OAuth 2.0 credentials and authentication parameters to establish secure communication with Google Calendar API. This configuration enables both service account and user account authentication modes.

## Configuration Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| Client ID | Text | Yes | OAuth 2.0 client identifier from Google Cloud Console | "123456789.apps.googleusercontent.com" |
| Client Secret | Text | Yes | OAuth 2.0 client secret (secured field) | "GOCSPX-abcdefghijklmnopqrstuvwxyz" |
| Email | Text | Yes | Email address for service account authentication | "admin@company.com" |

## Step-by-Step Setup

### 1. Obtain Google OAuth Credentials

Before configuring the extension, you must create OAuth 2.0 credentials in Google Cloud Console:

1. Follow the [Creating Google App](pages/CreatingGoogleApp.md) guide to set up your Google Cloud project
2. Enable the Google Calendar API
3. Create OAuth 2.0 credentials (Client ID and Client Secret)
4. Configure authorized redirect URIs

### 2. Configure Extension Parameters

1. **Navigate to Extension Setup**: Go to your Krista platform and locate the Google Calendar Extension
2. **Enter Client ID**: Paste the Client ID from your Google Cloud Console credentials
3. **Enter Client Secret**: Paste the Client Secret (this field is secured and encrypted)
4. **Enter Email**: Provide the email address that will be used for service account authentication

### 3. Authentication Type Selection

The extension supports two authentication modes:

#### Service Account Authentication
- **When to use**: For system-level operations where the extension acts on behalf of the configured email account
- **Configuration**: Uncheck "Have people authenticate with their account" checkbox
- **Behavior**: Uses the configured email address for all calendar operations

#### User Account Authentication  
- **When to use**: When individual users need to authenticate with their own Google accounts
- **Configuration**: Check "Have people authenticate with their account" checkbox
- **Behavior**: Prompts each user to authenticate with their Google account during conversation execution

### 4. Redirect URI Configuration

The extension requires a specific redirect URI to handle OAuth callbacks:

1. **Get Routing ID**: Copy the routing ID from the extension details page
2. **Construct Redirect URI**: Append `/rest/googlecalendar/callback` to the routing ID
   - Format: `https://your-krista-instance.com/routing-id/rest/googlecalendar/callback`
3. **Add to Google Console**: Add this URI to the "Authorized redirect URIs" in your Google Cloud Console OAuth credentials

### 5. Allow Retry Flag

The extension includes built-in retry mechanisms for handling transient failures:

- **Automatic Retry**: The system automatically retries failed requests for network timeouts and temporary API errors
- **Retry Logic**: Implements exponential backoff for rate limiting scenarios
- **Error Handling**: Distinguishes between retryable and non-retryable errors

## Security Considerations

### 1. Credential Protection
- **Client Secret**: Always stored encrypted in the Krista platform
- **Access Tokens**: Temporarily cached and automatically refreshed
- **Refresh Tokens**: Securely stored and used for token renewal

### 2. Scope Limitations
- **Minimal Scopes**: Extension only requests necessary Google Calendar permissions
- **Read/Write Access**: Limited to calendar events and attendee management
- **No Sensitive Data**: Does not access other Google services or personal information

### 3. Authentication Security
- **OAuth 2.0**: Industry-standard authentication protocol
- **PKCE Support**: Proof Key for Code Exchange for enhanced security
- **Token Expiration**: Automatic handling of token lifecycle

## Troubleshooting

### Common Configuration Issues

#### Invalid Client ID or Client Secret
**Symptoms**: "OAuth authorization failed: Unable to generate authorization URL. Required: Valid Client ID and Client Secret. Resolution: Verify Google Cloud Console credentials and ensure OAuth client is properly configured."
**Resolution**:
1. Verify credentials are copied correctly from Google Cloud Console
2. Ensure no extra spaces or characters
3. Confirm the Google Cloud project has Calendar API enabled

#### Redirect URI Mismatch
**Symptoms**: "OAuth callback validation failed: Invalid state parameter format. Required: Valid OAuth state parameter. Resolution: Restart authentication flow and ensure proper OAuth configuration."
**Resolution**:
1. Verify the redirect URI in Google Cloud Console matches exactly
2. Check for trailing slashes or protocol mismatches (http vs https)
3. Ensure the routing ID is current and active

#### Email Configuration Issues
**Symptoms**: "Account identification failed: No valid user email found in configuration. Required: Valid email address in extension setup. Resolution: Configure a valid email address in the extension settings and ensure proper authentication."
**Resolution**:
1. Verify the email address is valid and accessible
2. For service accounts, ensure the email has appropriate calendar permissions
3. Check that the email domain allows external calendar access

#### API Quota Exceeded
**Symptoms**: "Google Calendar client initialization failed: Unable to create API client. Required: Valid OAuth credentials and network connectivity. Resolution: Verify authentication status and check network connection."
**Resolution**:
1. Check Google Cloud Console for API usage limits
2. Implement request throttling in high-volume scenarios
3. Consider upgrading Google Cloud quotas if needed

### Validation Steps

After configuration, validate the setup:

1. **Test Connection**: Use the "Test Connection" button in the extension setup
2. **Verify Authentication**: Complete the OAuth flow successfully
3. **Check Permissions**: Ensure calendar access is granted
4. **Test Basic Operations**: Try retrieving calendar events to confirm functionality

## Best Practices

### 1. Environment Management
- Use separate Google Cloud projects for development, staging, and production
- Maintain different OAuth credentials for each environment
- Document credential rotation procedures

### 2. Access Control
- Limit calendar access to necessary users and systems
- Regularly review and audit authentication logs
- Implement proper user permission management

### 3. Monitoring and Maintenance
- Monitor API usage and quota consumption
- Set up alerts for authentication failures
- Regularly update and rotate credentials as needed

## See Also

- [Authentication](/pages/Authentication.md) - Detailed OAuth 2.0 flow and token management
- [Creating Google App](/pages/CreatingGoogleApp.md) - Google Cloud Console setup guide
- [Troubleshooting](../README.md#support--resources) - Additional support resources
