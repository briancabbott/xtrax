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
package org.eclipse.sw360.antenna.policy.workflow.processors;

import org.eclipse.sw360.antenna.model.xml.generated.License;
import org.eclipse.sw360.antenna.policy.engine.model.LicenseData;

import java.util.Optional;

class AntennaLicenseData implements LicenseData {
    private License license;

    AntennaLicenseData(License license) {
        this.license = license;
    }

    @Override
    public String getLicenseId() {
        return license.getName();
    }

    @Override
    public Optional<String> getLicenseName() {
        return Optional.ofNullable(license.getLongName());
    }

    @Override
    public Optional<String> getLicenseText() {
        return Optional.ofNullable(license.getText());
    }
}
