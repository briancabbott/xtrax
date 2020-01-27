/*
 * Copyright (c) Bosch Software Innovations GmbH 2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.workflow.generators;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.sw360.antenna.model.xml.generated.License;

public class HTMLReportUtils {
    private HTMLReportUtils() {
        // Utility class
    }

    public static String getLicenseAsHtmlListItem(License license) {
        return String.format("<li><a href=\"#%s\">%s</a></li>",
                StringEscapeUtils.escapeHtml4(license.getName()),
                license.getLongName() != null ?
                        StringEscapeUtils.escapeHtml4(license.getLongName()) : StringEscapeUtils.escapeHtml4(license.getName()));
    }
}
