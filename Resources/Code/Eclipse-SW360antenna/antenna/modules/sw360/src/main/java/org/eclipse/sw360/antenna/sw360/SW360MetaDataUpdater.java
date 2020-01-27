/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.sw360;

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.sw360.adapter.SW360LicenseClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ProjectClientAdapter;
import org.eclipse.sw360.antenna.sw360.adapter.SW360ReleaseClientAdapter;
import org.eclipse.sw360.antenna.sw360.rest.resource.licenses.SW360License;
import org.eclipse.sw360.antenna.sw360.rest.resource.releases.SW360Release;
import org.eclipse.sw360.antenna.sw360.workflow.SW360ConnectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SW360MetaDataUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(SW360MetaDataUpdater.class);

    // rest service adapters
    private SW360ProjectClientAdapter projectClientAdapter;
    private SW360LicenseClientAdapter licenseClientAdapter;
    private SW360ReleaseClientAdapter releaseClientAdapter;
    private SW360ConnectionConfiguration sw360ConnectionConfiguration;

    private final boolean updateReleases;
    private final boolean uploadSources;

    public SW360MetaDataUpdater(SW360ConnectionConfiguration sw360ConnectionConfiguration, boolean updateReleases, boolean uploadSources) {
        projectClientAdapter = sw360ConnectionConfiguration.getSW360ProjectClientAdapter();
        licenseClientAdapter = sw360ConnectionConfiguration.getSW360LicenseClientAdapter();
        releaseClientAdapter = sw360ConnectionConfiguration.getSW360ReleaseClientAdapter();
        this.sw360ConnectionConfiguration = sw360ConnectionConfiguration;
        this.updateReleases = updateReleases;
        this.uploadSources = uploadSources;
    }

    public Set<SW360License> getLicenses(List<License> licenses) {
        HttpHeaders header = sw360ConnectionConfiguration.getHttpHeaders();

        return licenses.stream()
                .filter(license -> isLicenseInSW360(license, header))
                .map(license -> licenseClientAdapter.getSW360LicenseByAntennaLicense(license, header))
                .collect(Collectors.toSet());
    }

    private boolean isLicenseInSW360(License license, HttpHeaders header) {
        if (licenseClientAdapter.isLicenseOfArtifactAvailable(license, header)) {
            LOGGER.debug("License [" + license.getName() + "] found in SW360.");
            return true;
        }
        LOGGER.debug("License [" + license.getName() + "] unknown in SW360.");
        return false;
    }

    public SW360Release getOrCreateRelease(SW360Release sw360ReleaseFromArtifact) {
        HttpHeaders header = sw360ConnectionConfiguration.getHttpHeaders();
        return releaseClientAdapter.getOrCreateRelease(sw360ReleaseFromArtifact, header, uploadSources, updateReleases);
    }

    public void createProject(String projectName, String projectVersion, Collection<SW360Release> releases) {
        HttpHeaders header = sw360ConnectionConfiguration.getHttpHeaders();

        Optional<String> projectId = projectClientAdapter.getProjectIdByNameAndVersion(projectName, projectVersion, header);

        String id;
        if (projectId.isPresent()) {
            // TODO: Needs endpoint on sw360 to update project on sw360
            LOGGER.debug("Could not update project " + projectId.get() + ", because the endpoint is not available.");
            id = projectId.get();
        } else {
            id = projectClientAdapter.addProject(projectName, projectVersion, header);
        }
        projectClientAdapter.addSW360ReleasesToSW360Project(id, releases, header);
    }
}
