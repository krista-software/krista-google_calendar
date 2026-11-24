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

package app.kristasoft.extensions.essentials.collaboration.google_calendar;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.ksdk.context.AuthorizationContext;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.connectors.GoogleProviderFactory;
import app.kristasoft.extensions.essentials.collaboration.google_calendar.impl.store.GoogleAttributeStore;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Field.Text(value = GoogleAttributes.CLIENT_ID)
@Field.Text(value = GoogleAttributes.CLIENT_SECRET, isSecured = true)
@Field.Text(value = GoogleAttributes.EMAIL)
@Extension(version = "2.0.5",name="Google Calendar")
@StaticResource(path = "docs", file = "docs")
public class GoogleCalendarExtension {

    public static final String EITHER_BOTH_TOPIC_AND_ALERT_MUST_BE_PROVIDED_OR_BOTH_MUST_BE_MISSING = "Either both topic and alert must be provided, or both must be missing.";
    private final Invoker invoker;
    private final GoogleAttributes gmailAttributes;
    private final GoogleProviderFactory clientProviderFactory;
    private final GoogleCalendarAuthenticator requestAuthenticator;

    @Inject
    public GoogleCalendarExtension(Invoker invoker, GoogleAttributes gmailAttributes, GoogleProviderFactory clientProviderFactory, GoogleAttributeStore gmailAttributeStore, AuthorizationContext context) {
        this(invoker, gmailAttributes,
                clientProviderFactory,
                new GoogleCalendarAuthenticator(gmailAttributeStore, gmailAttributes, context));
    }

    public GoogleCalendarExtension(Invoker invoker, GoogleAttributes gmailAttributes, GoogleProviderFactory clientProviderFactory, GoogleCalendarAuthenticator requestAuthenticator) {
        this.invoker = invoker;
        this.gmailAttributes = gmailAttributes;
        this.clientProviderFactory = clientProviderFactory;
        this.requestAuthenticator = requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    /**
     * Provides custom tabs for the extension UI.
     * Adds a Documentation tab pointing to static documentation resources.
     *
     * @return map of tab names to their corresponding static resource paths
     */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    /**
     * Validates Google Calendar connection using unsaved configuration attributes.
     * Tests authentication with Client ID and Client Secret from UI layer before saving.
     * Attempts to authenticate and fetch user events to verify credentials are valid.
     *
     * @param connectionAttributes map containing unsaved clientId, clientSecret, and email values
     * @throws IOException if connection test fails due to invalid credentials, authentication errors, or API connectivity issues
     */
    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateConnection(Map<String, Object> connectionAttributes) throws IOException {
        GoogleAttributes attributes = GoogleAttributes.create(invoker, connectionAttributes);
        clientProviderFactory.create(attributes).testConnection();
    }

    /**
     * Tests Google Calendar connection using saved configuration attributes.
     * Validates authentication with Client ID and Client Secret from database layer.
     * Attempts to authenticate and fetch user events to verify saved credentials are still valid.
     *
     * @throws IOException if connection test fails due to invalid credentials, expired tokens, or API connectivity issues
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() throws IOException {
        clientProviderFactory.create().testConnection();
    }

    /**
     * Handles updates to extension configuration attributes.
     * Called when invoker parameters are modified to synchronize attribute changes.
     * Updates the GoogleAttributes instance with new clientId, clientSecret, and email values.
     *
     * @param oldAttributes map containing previous attribute values (not used)
     * @param newAttributes map containing updated clientId, clientSecret, and email values
     */
    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        gmailAttributes.update(newAttributes);
    }
}
