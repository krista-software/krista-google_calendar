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

import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.GoogleAttributes;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.store.GoogleAttributeStore;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.store.RefreshTokenStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;

@Service
public class GoogleProviderFactory {
    private final RefreshTokenStore refreshTokenStore;
    private final GoogleAttributeStore googleAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final GoogleProvider defaultClientProvider;

    @Inject
    public GoogleProviderFactory(RefreshTokenStore refreshTokenStore, GoogleAttributeStore googleAttributeStore, GoogleAttributes gmailAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, googleAttributeStore, requestContext, authorizationContext, new GoogleProvider(refreshTokenStore, gmailAttributes, requestContext, authorizationContext, null));
    }

    public GoogleProviderFactory(RefreshTokenStore refreshTokenStore, GoogleAttributeStore googleAttributeStore, RequestContext requestContext, AuthorizationContext authorizationContext, GoogleProvider defaultClientProvider) {
        this.refreshTokenStore = refreshTokenStore;
        this.googleAttributeStore = googleAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.defaultClientProvider = defaultClientProvider;
    }

    /**
     * Creates a GoogleProvider instance with custom GoogleAttributes.
     * Saves attributes to store and creates provider with authentication context ID.
     * Used during attribute validation flow with unsaved configuration.
     *
     * @param gmailAttributes the custom GoogleAttributes containing OAuth credentials
     * @return GoogleProvider instance configured with provided attributes and generated auth context ID
     */
    public GoogleProvider create(GoogleAttributes gmailAttributes) {
        String authContextId = googleAttributeStore.save(gmailAttributes);
        return new GoogleProvider(refreshTokenStore, gmailAttributes, requestContext, authorizationContext, authContextId);
    }

    /**
     * Creates a GoogleProvider instance with default configuration.
     * Returns the default provider initialized with saved extension attributes.
     * Used for normal catalog request execution with saved configuration.
     *
     * @return default GoogleProvider instance with saved extension configuration
     */
    public GoogleProvider create() {
        return defaultClientProvider;
    }
}
