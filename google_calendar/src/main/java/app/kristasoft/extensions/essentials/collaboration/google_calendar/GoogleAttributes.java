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


import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.util.InvokerAttributes;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.api.services.calendar.CalendarScopes;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

@Service
public class GoogleAttributes {
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String EMAIL = "email";

    private final String routingUrl;
    private String clientId;
    private String clientSecret;
    private String mailId;

    private OAuth20Service oAuth20Service;

    @Inject
    public GoogleAttributes(Invoker invoker) {
        this(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                invoker.getAttributes());
    }

    public GoogleAttributes(String routingUrl, Map<String, Object> attributes) {
        this(routingUrl, InvokerAttributes.getStringOrNull(attributes, CLIENT_ID),
                InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET),
                InvokerAttributes.getStringOrNull(attributes, EMAIL));
    }

    public GoogleAttributes(String routingUrl, String clientId, String clientSecret, String mailId) {
        this.routingUrl = routingUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.mailId = mailId;
    }

    /**
     * Creates a GoogleAttributes instance from invoker and attribute map.
     * Factory method for constructing GoogleAttributes with routing URL and configuration attributes.
     *
     * @param invoker the extension invoker providing routing information
     * @param attributes map containing clientId, clientSecret, and email configuration values
     * @return GoogleAttributes instance initialized with provided configuration
     */
    public static GoogleAttributes create(Invoker invoker, Map<String, Object> attributes) {
        return new GoogleAttributes(
                invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                attributes);
    }

    /**
     * Updates the extension attributes with new configuration values.
     * Used when extension configuration is modified via INVOKER_UPDATED request.
     *
     * @param newAttributes map containing updated clientId, clientSecret, and email values
     */
    public void update(Map<String, Object> newAttributes) {
        clientId = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_ID);
        clientSecret = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_SECRET);
        mailId = InvokerAttributes.getStringOrNull(newAttributes, EMAIL);
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getMailId() {
        return mailId;
    }


    /**
     * Constructs the OAuth callback URL for Google Calendar authentication.
     * Combines routing URL with the Google Calendar callback path.
     *
     * @return complete OAuth callback URL in format: {routingUrl}/rest/googlecalendar/callback
     */
    public String getCallbackUrl() {
        return routingUrl + "/rest/googlecalendar/callback";
    }

    /**
     * Retrieves or creates the OAuth 2.0 service for Google Calendar authentication.
     * Lazily initializes the OAuth service with client credentials, calendar scope, and callback URL.
     * Thread-safe singleton pattern ensures only one service instance is created.
     *
     * @return OAuth20Service configured for Google Calendar API with full calendar scope
     */
    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            oAuth20Service = new ServiceBuilder(clientId)
                    .apiSecret(clientSecret)
                    .defaultScope(CalendarScopes.CALENDAR)
                    .callback(getCallbackUrl())
                    .build(GoogleApi20.instance());
        }
        return oAuth20Service;
    }
}
