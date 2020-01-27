/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
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

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360LicenseList;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360SparseLicense;
import org.eclipse.sw360.antenna.sw360.utils.RestUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SW360LicenseClient extends SW360Client {
    private static final String LICENSES_ENDPOINT = "/licenses";
    private final String restUrl;

    public SW360LicenseClient(String restUrl, ProxySettings proxySettings) {
        super(proxySettings);
        this.restUrl = restUrl;
    }

    @Override
    public String getEndpoint() {
        return restUrl + LICENSES_ENDPOINT;
    }

    public List<SW360SparseLicense> getLicenses(HttpHeaders header) {
        ResponseEntity<Resource<SW360LicenseList>> response = doRestGET(getEndpoint(), header,
                new ParameterizedTypeReference<Resource<SW360LicenseList>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            SW360LicenseList resource = Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
            if (resource.get_Embedded() != null &&
                    resource.get_Embedded().getLicenses() != null) {
                return new ArrayList<>(resource.get_Embedded().getLicenses());
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new ExecutionException("Request to get all licenses failed with " + response.getStatusCode());
        }
    }

    public SW360License getLicenseByName(String name, HttpHeaders header) {
        ResponseEntity<Resource<SW360License>> response = doRestGET(getEndpoint() + "/" + name, header,
                new ParameterizedTypeReference<Resource<SW360License>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
        } else {
            throw new ExecutionException("Request to get license " + name + " failed with "
                    + response.getStatusCode());
        }
    }

    public SW360License createLicense(SW360License sw360License, HttpHeaders header) {
        HttpEntity<String> httpEntity = RestUtils.convertSW360ResourceToHttpEntity(sw360License, header);
        ResponseEntity<Resource<SW360License>> response;
        try {
            response = doRestPOST(getEndpoint(), httpEntity,
                new ParameterizedTypeReference<Resource<SW360License>>() {});
        } catch (HttpClientErrorException e) {
            throw new ExecutionException("Request to create license " + sw360License.getFullName() + " failed with "
                    + e.getStatusCode());
        }

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return Optional.ofNullable(response.getBody())
                    .orElseThrow(() -> new ExecutionException("Body was null"))
                    .getContent();
        } else {
            throw new ExecutionException("Request to create license " + sw360License.getFullName() + " failed with "
                    + response.getStatusCode());
        }
    }
}
