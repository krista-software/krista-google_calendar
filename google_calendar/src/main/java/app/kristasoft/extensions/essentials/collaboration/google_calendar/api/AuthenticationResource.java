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

package app.kristasoft.extensions.essentials.collaboration.google_calendar.api;

import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.GoogleAttributes;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.connectors.GoogleProvider;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.connectors.GoogleProviderFactory;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.store.GoogleAttributeStore;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.store.RefreshTokenStore;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.util.Constants;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.api.client.util.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.github.scribejava.core.model.OAuthConstants.CODE;
import static com.github.scribejava.core.model.OAuthConstants.STATE;

@Path("/")
public final class AuthenticationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);
    private final GoogleAttributeStore googleAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GoogleProviderFactory providerFactory;
    private final AuthorizationListener authorizationListener;
    private final AuthorizationContext context;


    @Inject
    public AuthenticationResource(GoogleAttributeStore googleAttributeStore, RefreshTokenStore refreshTokenStore, GoogleProviderFactory providerFactory, AuthorizationListener authorizationListener, AuthorizationContext context) {
        this.googleAttributeStore = googleAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.authorizationListener = authorizationListener;
        this.context = context;
    }

    /**
     * Handles the OAuth 2.0 callback from Google after user authorization.
     * Exchanges authorization code for access and refresh tokens, stores tokens, and validates user access.
     * Supports both studio user (service account) and client user (individual account) authentication modes.
     *
     * @param code the authorization code from Google OAuth callback
     * @param state the state parameter containing userId and optional authContextId for OAuth flow tracking
     * @return success message if authentication completes successfully
     * @throws BadRequestException if state parameter format is invalid
     * @throws IllegalStateException if token exchange fails, user access validation fails, or authentication is interrupted
     */
    @GET
    @Path("/callback")
    public String getCallBack(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        Objects.requireNonNull(code);
        String[] parts = state.split(Constants.HASH);
        if (parts[0].isBlank() || parts.length > 3) {
            throw new BadRequestException("OAuth callback validation failed: Invalid state parameter format. Required: Valid OAuth state parameter. Resolution: Restart authentication flow and ensure proper OAuth configuration.");
        }
        String key = parts[0] + Constants.HASH + parts[1];
        String authContextId = parts.length == 3 ? parts[2] : null;

        try {
            GoogleProvider clientProvider = getGoogleClientProvider(authContextId);
            OAuth2AccessToken accessToken = clientProvider.getGoogleAttributes().getOAuth20Service().getAccessToken(code);
            String refreshTokenString = accessToken.getRefreshToken();
            String accessTokenString = accessToken.getAccessToken();
            String refreshTokenStored = refreshTokenStore.get(key);

            // Google Provides 1 refresh token at a time
            // For studio user key starts with setup emailId
            // For client user key starts with wsContact
            // if the refresh token fetched is for client then store access token
            if (key.startsWith(Constants.WS_CONTACT) && refreshTokenString == null) {
                refreshTokenStore.put(key, accessTokenString); // Storing access token for client
            }
            // if the refresh token stored for studio user is access token then revoke the token
            else if (refreshTokenStored != null && refreshTokenStored.startsWith("ya29")) {
                LOGGER.error("Refresh token access removed. Re-authorization is needed.");
                // Revoking access token to get refresh token for existing studio users
                clientProvider.getGoogleAttributes().getOAuth20Service().revokeToken(accessToken.getAccessToken());
                refreshTokenStore.remove(key);
                return "Token refresh required: Access token detected instead of refresh token. Required: Fresh OAuth authorization. Resolution: Complete authentication flow again to obtain proper refresh token.";
            }
            // if the refresh token is not null and key doesn't start with wsContact
            // store refresh token for studio user
            else {
                refreshTokenStore.put(key, refreshTokenString);
            }

            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                refreshTokenStore.remove(key);
                return "User authorization mismatch: Authenticated user email does not match configured email. Required: Authentication with configured email address '" + key.split("#")[0] + "'. Resolution: Authenticate with the correct Google account or update extension configuration with proper email.";
            }
            if (context.isAuthenticated()) {
                authorizationListener.authorized();
                return "User authenticated successfully.";
            }
            return "User authenticated successfully. Save the changes.";
        } catch (OAuthException cause) {
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());
        } catch (IOException | ExecutionException cause) {
            throw new IllegalStateException("OAuth authorization processing failed: Unable to complete authentication flow. Required: Valid authorization code and network connectivity. Resolution: Retry authentication flow and verify network connection.", cause.getCause());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OAuth authorization interrupted: Authentication process was cancelled. Required: Uninterrupted authentication flow. Resolution: Restart authentication process without interruption.", interruptedException.getCause());
        } finally {
            if (authContextId != null) {
                googleAttributeStore.remove(authContextId);
            }
        }
    }

    /**
     * Retrieves the appropriate GoogleProvider based on authentication context.
     * Uses default provider for normal flow, or creates provider with stored attributes for validation flow.
     *
     * @param authContextId optional authentication context ID for attribute validation flow
     * @return GoogleProvider instance configured with appropriate attributes
     * @throws IOException if unable to load attributes from store
     */
    private GoogleProvider getGoogleClientProvider(String authContextId) throws IOException {
        GoogleProvider clientProvider;
        if (authContextId == null) {
            clientProvider = providerFactory.create();
        } else {
            GoogleAttributes effectiveGmailAttributes = googleAttributeStore.load(authContextId);
            clientProvider = providerFactory.create(effectiveGmailAttributes);
        }
        return clientProvider;
    }

    /**
     * Extracts and formats error description from OAuthException.
     * Parses JSON error response to extract error code and description.
     *
     * @param cause the OAuthException containing error details
     * @return formatted error description with resolution guidance
     */
    private String getErrorDescription(OAuthException cause) {
        String errorDescription = "OAuth authorization failed: ";
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
            if (exceptionMap.containsKey("error")) {
                errorDescription += exceptionMap.get("error") + ". ";
            }
            if (exceptionMap.containsKey("error_description")) {
                errorDescription += exceptionMap.get("error_description") + ". ";
            }
        }
        errorDescription += "Required: Valid OAuth credentials and user consent. Resolution: Verify Client ID/Secret configuration and retry authentication flow.";
        return errorDescription;
    }

    /**
     * Validates that the authenticated user has access to Google Calendar.
     * Attempts to list events from the primary calendar to verify permissions.
     *
     * @param googleProvider the GoogleProvider with authenticated credentials
     * @return true if user has calendar access, false if access is denied or API call fails
     */
    private boolean hasUserAccess(GoogleProvider googleProvider) {
        try {
            googleProvider.getGoogleClient().events().list("primary").setMaxResults(Math.toIntExact(1L))
                    .setTimeMin(new DateTime(System.currentTimeMillis()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true).execute();

            return true;
        } catch (IOException cause) {
            LOGGER.error("-------IGNORED EXCEPTION-----{}", cause.getMessage());
            return false;
        }
    }

}