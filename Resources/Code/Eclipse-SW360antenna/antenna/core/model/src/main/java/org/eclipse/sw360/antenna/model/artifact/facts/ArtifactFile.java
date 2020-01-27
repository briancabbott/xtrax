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

package org.eclipse.sw360.antenna.model.artifact.facts;

import org.eclipse.sw360.antenna.model.artifact.ArtifactFact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactFactWithPayload;

import java.nio.file.Path;

public class ArtifactFile extends ArtifactFactWithPayload<Path> implements ArtifactIdentifier {
    public ArtifactFile(Path path) {
        super(path.normalize());
    }

    @Override
    public String getFactContentName() {
        return "Artifact File";
    }

    @Override
    public Class<? extends ArtifactFact> getKey() {
        return ArtifactFile.class;
    }

    @Override
    public boolean matches(ArtifactIdentifier artifactIdentifier) {
        if (artifactIdentifier instanceof ArtifactFile) {
            ArtifactFile artifactFile = (ArtifactFile) artifactIdentifier;
            return get().equals(artifactFile.get());
        }
        return false;
    }
}
