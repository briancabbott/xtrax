/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 * Copyright (c) Verifa Oy 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Attributes;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;

public class SW360AuthenticationClient extends SW360Client {
    private static final String AUTHORIZATION_BASIC_VALUE = "Basic ";
    private static final String AUTHORIZATION_BEARER_VALUE = "Bearer ";

    private static final String GRANT_TYPE_VALUE = "password";
    private static final String GET_ACCESS_TOKEN_ENDPOINT = "/token";
    private static final String TOKEN_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String JSON_TOKEN_KEY = "access_token";

    private final String authServerUrl;

    public SW360AuthenticationClient(String authServerUrl, ProxySettings proxySettings) {
        super(proxySettings);
        this.authServerUrl = authServerUrl;
    }

    @Override
    public String getEndpoint() {
        return authServerUrl + GET_ACCESS_TOKEN_ENDPOINT;
    }

    public String getOAuth2AccessToken(String username, String password, String clientId, String clientPassword) {
        String body = String.format("%s=%s&%s=%s&%s=%s", SW360Attributes.AUTHENTICATOR_GRANT_TYPE, GRANT_TYPE_VALUE,
                SW360Attributes.AUTHENTICATOR_USERNAME, username, SW360Attributes.AUTHENTICATOR_PASSWORD, password);

        HttpHeaders headers = new HttpHeaders();
        addBasicAuthentication(headers, clientId, clientPassword);
        headers.add(HttpHeaders.CONTENT_TYPE, TOKEN_CONTENT_TYPE);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = doRestCall(getEndpoint(), HttpMethod.POST, httpEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                return (String) new ObjectMapper().readValue(response.getBody(), HashMap.class).get(JSON_TOKEN_KEY);
            } catch (IOException e) {
                throw new ExecutionException("Error when attempting to deserialize the response body.", e);
            }
        } else {
            throw new ExecutionException("Could not request OAuth2 access token for [" + username + "].");
        }
    }

    private void addBasicAuthentication(HttpHeaders headers, String liferayClientId, String liferayClientPassword) {
        String liferayClient = liferayClientId + ":" + liferayClientPassword;
        String base64ClientCredentials = Base64.getEncoder().encodeToString(liferayClient.getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BASIC_VALUE + base64ClientCredentials);
    }

    public HttpHeaders getHeadersWithBearerToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BEARER_VALUE + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }
}
