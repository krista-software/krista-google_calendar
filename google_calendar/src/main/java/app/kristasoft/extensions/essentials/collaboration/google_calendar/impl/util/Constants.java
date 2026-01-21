/*
 * Google Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String HASH = "#";
    public static final String WS_CONTACT = "wsContact";
    // Account and User ID Error Messages
    public static final String FAILED_TO_GET_ACCOUNT = "Account identification failed: No valid user email found in configuration. Required: Valid email address in extension setup. Resolution: Configure a valid email address in the extension settings and ensure proper authentication.";

    // Authentication Error Messages
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION = "Authentication token expired: Current session is no longer valid. Required: Fresh authentication credentials. Resolution: Complete the OAuth re-authentication flow to obtain new access tokens.";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION = "Authentication required: No valid authentication credentials found. Required: OAuth 2.0 authorization. Resolution: Complete the OAuth authentication flow by clicking 'Validate Attributes' and granting calendar permissions.";
    public static final String REFRESH_TOKEN_EXPIRED_PLEASE_REAUTHORIZE_YOURSELF = "Refresh token expired: Authentication credentials are no longer valid. Required: Fresh OAuth authorization. Resolution: Re-authenticate by clicking 'Validate Attributes' and completing the Google OAuth flow.";
    public static final String AUTHORIZATION_PROMPT = "Authorization required: Extension needs Google Calendar permissions. Required: Valid OAuth credentials. Resolution: Click 'Validate Attributes', complete Google OAuth flow, then save configuration changes.";

    // Client and API Error Messages
    public static final String FAILED_TO_GET_GRAPH_CLIENT = "Google Calendar client initialization failed: Unable to create API client. Required: Valid OAuth credentials and network connectivity. Resolution: Verify authentication status and check network connection.";
    public static final String FAILED_TO_GET_AUTHORIZE_RESPONSE = "OAuth authorization failed: Unable to generate authorization URL. Required: Valid Client ID and Client Secret. Resolution: Verify Google Cloud Console credentials and ensure OAuth client is properly configured.";
    public static final String FAILED_TO_AUTHENTICATE_USER = "User authentication failed: Unable to process authentication callback. Required: Valid OAuth callback with authorization code. Resolution: Retry authentication flow and ensure redirect URI is correctly configured.";

    // System Constants
    public static final String TEXT = "Text";
    public static final String USER_ID = "userId";
    public static final String GOOGLE_CALENDAR_CALLBACK = "/googlecalendar/callback";
    public static final String ACCESS_TYPE_OFFLINE_APPROVAL_PROMPT_FORCE = "&prompt=consent&access_type=offline";
    public static final String AUTH_CONTEXT_ID = "authContextId";
    public static final String GOOGLE_CALENDAR = "Google Calendar";
    public static final String PRIMARY = "primary";

    private Constants() {
    }

}
