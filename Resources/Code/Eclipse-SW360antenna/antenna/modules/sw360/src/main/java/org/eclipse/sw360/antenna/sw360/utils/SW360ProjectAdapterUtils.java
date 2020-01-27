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
package org.eclipse.sw360.antenna.sw360.utils;

import org.eclipse.sw360.antenna.sw360.rest.resource.SW360Visibility;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360Project;
import org.eclipse.sw360.antenna.sw360.rest.resource.projects.SW360ProjectType;

public class SW360ProjectAdapterUtils {
    public static void setDescription(SW360Project project, String mvnDescription) {
        if (mvnDescription != null &&
                !mvnDescription.isEmpty()) {
            project.setDescription(mvnDescription);
        }
    }

    public static void setName(SW360Project project, String applicationId) {
        if (applicationId != null &&
                !applicationId.isEmpty()) {
            project.setName(applicationId);
        }
    }

    public static void setVersion(SW360Project project, String version) {
        if (version != null &&
                !version.isEmpty()) {
            project.setVersion(version);
        }
    }

    public static void setClearingTeam(SW360Project project, String clearingTeam) {
        if (clearingTeam != null &&
                !clearingTeam.isEmpty()) {
            project.setClearingTeam(clearingTeam);
        }
    }

    public static void prepareProject(SW360Project sw360Project, String projectName, String projectVersion) {
        SW360ProjectAdapterUtils.setName(sw360Project, projectName);
        SW360ProjectAdapterUtils.setVersion(sw360Project, projectVersion);
        SW360ProjectAdapterUtils.setDescription(sw360Project, projectName + " " + projectVersion);
        sw360Project.setProjectType(SW360ProjectType.PRODUCT);
        sw360Project.setVisibility(SW360Visibility.BUISNESSUNIT_AND_MODERATORS);
    }

    public static boolean isValidProject(SW360Project project) {
        if(project.getName() == null || project.getName().isEmpty()) {
            return false;
        }
        if(project.getVersion() == null || project.getVersion().isEmpty()) {
            return false;
        }
        return true;
    }
}
