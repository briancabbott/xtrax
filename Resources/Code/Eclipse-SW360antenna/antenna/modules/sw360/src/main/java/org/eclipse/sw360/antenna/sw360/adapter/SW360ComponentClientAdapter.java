/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.adapter;

import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.sw360.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.rest.resource.components.SW360SparseComponent;
import org.eclipse.sw360.antenna.sw360.utils.SW360ComponentAdapterUtils;
import org.eclipse.sw360.antenna.util.ProxySettings;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SW360ComponentClientAdapter {
    private final SW360ComponentClient componentClient;

    public SW360ComponentClientAdapter(String restUrl, ProxySettings proxySettings) {
        this.componentClient = new SW360ComponentClient(restUrl, proxySettings);
    }

    public SW360Component getOrCreateComponent(SW360Component componentFromRelease, HttpHeaders header) {
        if(componentFromRelease.getComponentId() != null) {
            return getComponentById(componentFromRelease.getComponentId(), header);
        }
        return getComponentByName(componentFromRelease.getName(), header)
                .orElseGet(() -> createComponent(componentFromRelease, header));
    }

    public SW360Component createComponent(SW360Component component, HttpHeaders header) {
        if(! SW360ComponentAdapterUtils.isValidComponent(component)) {
            throw new ExecutionException("Can not write invalid component for " + component.getName());
        }
        return componentClient.createComponent(component, header);
    }

    public SW360Component getComponentById(String componentId, HttpHeaders header) {
        return componentClient.getComponent(componentId, header);
    }

    public Optional<SW360Component> getComponentByArtifact(Artifact artifact, HttpHeaders header) {
        String componentName = SW360ComponentAdapterUtils.createComponentName(artifact);

        return getComponentByName(componentName, header);
    }

    public Optional<SW360Component> getComponentByName(String componentName, HttpHeaders header) {
        List<SW360Component> completeComponents = new ArrayList<>();
        List<SW360SparseComponent> components = componentClient.searchByName(componentName, header);

        List<String> componentIds = components.stream()
                .filter(c -> c.getName().equals(componentName))
                .map(c -> SW360HalResourceUtility.getLastIndexOfSelfLink(c.get_Links()).orElse(""))
                .collect(Collectors.toList());

        for (String componentId : componentIds) {
            completeComponents.add(getComponentById(componentId, header));
        }

        return completeComponents.stream()
                .filter(c -> c.getName().equals(componentName))
                .findFirst();

    }
}
