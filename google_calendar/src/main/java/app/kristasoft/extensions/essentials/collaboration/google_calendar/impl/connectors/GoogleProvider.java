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

package app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedValuedField;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.GoogleAttributes;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.store.RefreshTokenStore;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util.Constants;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util.Constants.GOOGLE_CALENDAR;
import static app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util.Constants.PRIMARY;

public class GoogleProvider {

    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleProvider.class);

    private final RefreshTokenStore refreshTokenStore;
    private final GoogleAttributes gmailAttributes;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final String authContextId;

    @Inject
    public GoogleProvider(RefreshTokenStore refreshTokenStore, GoogleAttributes googleAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, googleAttributes, requestContext, authorizationContext, null);
    }

    public GoogleProvider(RefreshTokenStore refreshTokenStore, GoogleAttributes googleAttributes, RequestContext requestContext, AuthorizationContext authorizationContext, String authContextId) {
        this.refreshTokenStore = refreshTokenStore;
        this.gmailAttributes = googleAttributes;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.authContextId = authContextId;
    }

    public GoogleAttributes getGoogleAttributes() {
        return gmailAttributes;
    }

    /**
     * Retrieves an authenticated Google Calendar client instance.
     * Determines whether to use service account or user account based on request context.
     *
     * @return Calendar service instance with valid authentication credentials
     * @throws IOException if unable to create Calendar client due to authentication or network issues
     */
    public Calendar getGoogleClient() throws IOException {
        return getGoogleClient(!requestContext.invokeAsUser());
    }

    /**
     * Retrieves an authenticated Google Calendar client instance for admin/service account operations.
     * Always uses the configured email address from extension attributes.
     *
     * @return Calendar service instance with service account authentication
     * @throws IOException if unable to create Calendar client due to authentication or network issues
     */
    public Calendar getGoogleClientForAdmin() throws IOException {
        return getGoogleClient(true);
    }

    /**
     * Creates an authenticated Google Calendar client with specified authentication mode.
     * Retrieves refresh token from store and builds Calendar service instance.
     *
     * @param useEmail true to use configured email (service account), false to use authenticated user
     * @return Calendar service instance with valid authentication credentials
     * @throws MustAuthorizeException if refresh token is not found and authorization is required
     * @throws RuntimeException if client creation fails due to authentication or network issues
     */
    private Calendar getGoogleClient(boolean useEmail) {
        try {
            String userId = getUserId(useEmail);
            String refreshToken = refreshTokenStore.get(userId);
            if (refreshToken == null) {
                throw createMustAuthorizationException(userId, false);
            }
            return getGoogle(userId, refreshToken, useEmail);
        } catch (RuntimeException cause) {
            LOGGER.error(Constants.FAILED_TO_GET_GRAPH_CLIENT + "{} {}", cause.getClass().getName(), cause.getMessage());
            throw cause;
        }
    }

    /**
     * Builds a Google Calendar service instance with authenticated credentials.
     * Creates HTTP transport, refreshes credentials, and constructs Calendar client.
     *
     * @param userId the user identifier for logging and error handling
     * @param refreshToken the OAuth refresh token for credential renewal
     * @param isUseEmail true if using service account email, false for user account
     * @return Calendar service instance ready for API calls
     * @throws IllegalStateException if service account authorization fails
     * @throws MustAuthorizeException if user account authorization is required
     */
    private Calendar getGoogle(String userId, String refreshToken, Boolean isUseEmail) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = getGoogleCredential(refreshToken, httpTransport, isUseEmail);
            return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(GOOGLE_CALENDAR)
                    .build();
        } catch (IOException | GeneralSecurityException cause) {
            if (!requestContext.invokeAsUser()) {
                throw new IllegalStateException("Service account authorization failed: Extension lacks proper authentication credentials. Required: Valid service account configuration with domain-wide delegation. Resolution: Contact administrator to validate extension attributes and configure service account permissions.");
            } else {
                throw createMustAuthorizationException(userId, true);
            }
        }
    }

    /**
     * Creates and refreshes Google OAuth credentials using the provided refresh token.
     * Builds GoogleCredential with client secrets, sets refresh token, and refreshes access token.
     *
     * @param refreshToken the OAuth refresh token for credential renewal
     * @param HTTP_TRANSPORT the HTTP transport for API communication
     * @param isUseEmail true if using service account email, false for user account
     * @return GoogleCredential with refreshed access token
     * @throws IOException if credential refresh fails due to invalid token or network issues
     */
    private GoogleCredential getGoogleCredential(String refreshToken, NetHttpTransport HTTP_TRANSPORT, Boolean isUseEmail) throws IOException {
        // validate if extn attributes has the required values else return err
        GoogleCredential credential =
                new GoogleCredential.Builder()
                        .setClientSecrets(gmailAttributes.getClientId(), gmailAttributes.getClientSecret())
                        .setJsonFactory(JacksonFactory.getDefaultInstance())
                        .setTransport(HTTP_TRANSPORT)
                        .build();
        credential.setRefreshToken(refreshToken);
        credential.refreshToken();
        return credential;
    }

    /**
     * Creates a MustAuthorizeException to trigger OAuth authorization flow.
     * Constructs exception with user ID and optional auth context ID for state management.
     *
     * @param userId the user identifier for authorization
     * @param reAuthentication true if this is a re-authentication (token expired), false for initial authentication
     * @return MustAuthorizeException with appropriate error message and authorization details
     */
    public MustAuthorizeException createMustAuthorizationException(String userId, boolean reAuthentication) {
        LOGGER.error(reAuthentication ? Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION : Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION);
        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField userIdField = new NamedValuedField(Constants.USER_ID, Constants.TEXT, userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField(Constants.AUTH_CONTEXT_ID, Constants.TEXT, authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
        }
        return new MustAuthorizeException(reAuthentication ? Constants.REFRESH_TOKEN_EXPIRED_PLEASE_REAUTHORIZE_YOURSELF : Constants.AUTHORIZATION_PROMPT, details);
    }

    /**
     * Retrieves the user identifier for authentication and token storage.
     * Combines user email with client credentials to create a unique storage key.
     *
     * @param calledFromValidateAttributes true if called during attribute validation, false for normal operations
     * @return composite user identifier in format: {email}#{clientId}{clientSecret}
     * @throws IllegalStateException if user email cannot be determined from configuration or authorization context
     */
    public String getUserId(boolean calledFromValidateAttributes) {
        String userId = calledFromValidateAttributes || !requestContext.invokeAsUser()
                ? gmailAttributes.getMailId()
                : authorizationContext.getAuthorizedAccount().getAccountId();
        if (userId == null) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ACCOUNT);
        }
        return userId + Constants.HASH + gmailAttributes.getClientId() + gmailAttributes.getClientSecret();
    }

    /**
     * Tests the Google Calendar connection by attempting to list events.
     * Validates authentication credentials and API connectivity.
     * If unauthorized (401), triggers re-authentication flow.
     *
     * @throws MustAuthorizeException if authentication fails with 401 status, requiring re-authorization
     */
    public void testConnection() {
        try {
            getGoogleClientForAdmin().events().list(PRIMARY).setMaxResults(Math.toIntExact(1L))
                    .setTimeMin(new DateTime(System.currentTimeMillis()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true).execute();
        } catch (IOException exception) {
            if (((GoogleJsonResponseException) exception).getStatusCode() == 401) {
                throw createMustAuthorizationException(getUserId(true), true);
            }
        }
    }
}
