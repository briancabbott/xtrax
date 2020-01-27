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

package org.eclipse.sw360.antenna.p2resolver;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These tests also document how the process will be called:
 * The process called is the antenna-p2-repository-manager, which is an eclipse product and must be called via the command line.
 * To ensure that calls succeed, when changing any of the test also carefully look at the ProjectArgumentExtractor in antenna-p2.
 * It's behaviour is documented in the file ProjectArgumentExtractorTest.java
 */
public class EclipseProcessBuilderTest {

    @Test
    public void testEclipseProcessBuilderCommandWorksCorrectlyWithHttpRepositoryAndOneArtifact() {
        Artifact artifact = new Artifact();
        artifact.addCoordinate(new Coordinate(Coordinate.Types.P2, "TestBundle", "1.0.0"));
        File installArea = new File("test_file1");
        File downloadArea = new File("test_file2");

        ProcessBuilder processBuilder = EclipseProcessBuilder.setupEclipseProcess(
                installArea, downloadArea, Collections.singletonList(artifact), Collections.singletonList("http://www.example.org"));

        assertThat(processBuilder.command()).contains(OperatingSystemSpecifics.getEclipseExecutable(installArea).toString());
        assertThat(processBuilder.command()).contains("-download-area test_file2");
        assertThat(processBuilder.command()).contains("-repositories http://www.example.org");
        assertThat(processBuilder.command()).contains("-coordinates TestBundle,1.0.0");
    }

    @Test
    public void testEclipseProcessBuilderCommandPrependsFileToUriIfItDoesNotHaveAScheme() {
        Artifact artifact = new Artifact();
        artifact.addCoordinate(new Coordinate(Coordinate.Types.P2, "TestBundle", "1.0.0"));
        File installArea = new File("test_file1");
        File downloadArea = new File("test_file2");

        ProcessBuilder processBuilder = EclipseProcessBuilder.setupEclipseProcess(
                installArea, downloadArea, Collections.singletonList(artifact), Collections.singletonList("/home/somebody/repository"));

        String repositoryString = Paths.get("/home/somebody/repository").normalize().toUri().toString();
        assertThat(repositoryString).startsWith("file:/");
        assertThat(processBuilder.command()).contains("-repositories " + repositoryString);
    }

    @Test
    public void testEclipseProcessBuilderCommandCorrectlyChainsArtifacts() {
        Artifact artifact1 = new Artifact();
        artifact1.addCoordinate(new Coordinate(Coordinate.Types.P2, "TestBundle1", "1.0.0"));
        Artifact artifact2 = new Artifact();
        artifact2.addCoordinate(new Coordinate(Coordinate.Types.P2, "TestBundle2", "1.0.0.v201"));
        File installArea = new File("test_file1");
        File downloadArea = new File("test_file2");

        ProcessBuilder processBuilder = EclipseProcessBuilder.setupEclipseProcess(
                installArea, downloadArea, Arrays.asList(artifact1, artifact2), Collections.singletonList("http://www.example.org"));

        assertThat(processBuilder.command()).contains("-coordinates TestBundle1,1.0.0;TestBundle2,1.0.0.v201");
    }
}
