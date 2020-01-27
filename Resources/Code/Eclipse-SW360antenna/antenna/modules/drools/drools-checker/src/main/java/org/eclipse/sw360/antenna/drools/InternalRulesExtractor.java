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

package org.eclipse.sw360.antenna.drools;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.sw360.antenna.util.ZipExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class InternalRulesExtractor {
    private InternalRulesExtractor() {
        // Utility class
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public static InternalRulesPackage extractRules(Path jarPath, Path extractionLocation) throws IOException {
        extractionLocation.toFile().mkdirs();
        File unzippedFile = extractionLocation.toFile();
        ZipExtractor.extractAll(jarPath.toFile(), unzippedFile);
        return new InternalRulesPackage(unzippedFile);
    }
}
