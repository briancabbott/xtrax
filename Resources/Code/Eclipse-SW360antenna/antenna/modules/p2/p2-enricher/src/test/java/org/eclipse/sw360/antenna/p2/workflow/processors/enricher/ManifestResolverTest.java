/*
 * Copyright (c) Bosch Software Innovations GmbH 2013,2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.p2.workflow.processors.enricher;

import org.assertj.core.api.Assertions;
import org.eclipse.sw360.antenna.api.IProject;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFile;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.java.ArtifactPathnames;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.eclipse.sw360.antenna.testing.AntennaTestWithMockedContext;
import org.eclipse.sw360.antenna.testing.util.JarCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class ManifestResolverTest extends AntennaTestWithMockedContext {

    @Mock
    private IProject iProject = mock(IProject.class);

    private JarCreator jarCreator;
    private ManifestResolver resolver;

    @Before
    public void setUp() throws Exception {
        this.jarCreator = new JarCreator();
        resolver = new ManifestResolver();
        resolver.setAntennaContext(antennaContextMock);
        when(antennaContextMock.getProject()).thenReturn(iProject);
        when(antennaContextMock.getProject().getBasedir()).thenReturn(temporaryFolder.newFolder("project-basedir"));
    }

    @After
    public void tearDown() {
        this.jarCreator.cleanUp();
        verify(antennaContextMock, atLeast(0)).getProject();
    }

    private List<Artifact> makeArtifacts(Path path) {
        List<Artifact> artifacts = new ArrayList<>();
        Artifact artifact = new Artifact();
        artifact.addFact(new ArtifactPathnames(Collections.singletonList(path.toString())));
        artifact.addFact(new ArtifactFilename(path.getFileName().toString()));
        artifacts.add(artifact);
        return artifacts;
    }

    private void assertManifestMetadata(Artifact artifact) {
        final Optional<ArtifactCoordinates> artifactCoordinates = artifact.askFor(ArtifactCoordinates.class);
        final Optional<Coordinate> bundleCoordinates = artifactCoordinates
                .flatMap(c -> c.getCoordinateForType(Coordinate.Types.P2));
        Assertions.assertThat(bundleCoordinates.isPresent()).isTrue();
        Assertions.assertThat(bundleCoordinates.get().getName())
                .isEqualTo(JarCreator.testManifestSymbolicName);
        Assertions.assertThat(bundleCoordinates.get().getVersion())
                .isEqualTo(JarCreator.testManifestVersion);
    }

    @Test
    public void resolveArtifactsTest() throws IOException {
        Path jarWithManifest = jarCreator.createJarWithManifest(JarCreator.jarWithManifestName);
        List<Artifact> artifacts = makeArtifacts(jarWithManifest);

        resolver.process(artifacts);

        assertManifestMetadata(artifacts.get(0));
    }

    @Test
    public void testWithoutManifest() throws IOException {
        Path jarWithoutManifest = jarCreator.createJarWithoutManifest();
        List<Artifact> artifacts = makeArtifacts(jarWithoutManifest);

        resolver.process(artifacts);

        Assertions.assertThat(artifacts.get(0).getCoordinateForType(Coordinate.Types.P2).isPresent())
                .isFalse();
    }

    @Test
    public void testJarInJar() throws IOException {
        Path jarInJarInJar = jarCreator.createJarInJar()
                .resolve(JarCreator.jarWithManifestName);
        List<Artifact> artifacts = makeArtifacts(jarInJarInJar);

        resolver.process(artifacts);

        final Optional<Path> artifactFile = artifacts.get(0).askForGet(ArtifactFile.class);

        Assertions.assertThat(artifactFile.isPresent()).isTrue();
        Assertions.assertThat(artifactFile.get().toFile().getName()).isEqualTo(JarCreator.jarWithManifestName);
        assertManifestMetadata(artifacts.get(0));
    }

    @Test
    public void testJarInJarInNestedFolders() throws IOException {
        Path jarInJarInJar = jarCreator.createJarInJarInNestedFolders()
                .resolve(JarCreator.jarInFoldersName);
        List<Artifact> artifacts = makeArtifacts(jarInJarInJar);

        resolver.process(artifacts);

        final Optional<Path> artifactFile = artifacts.get(0).askForGet(ArtifactFile.class);

        Assertions.assertThat(artifactFile.isPresent()).isTrue();
        Assertions.assertThat(artifactFile.get().toFile().getName()).isEqualTo(JarCreator.jarWithManifestName);
        assertManifestMetadata(artifacts.get(0));
    }

    @Test
    public void testJarInJarInJar() throws IOException {
        Path jarInJarInJar = jarCreator.createJarInJarInJar()
                .resolve(JarCreator.jarInJarName)
                .resolve(JarCreator.jarWithManifestName);
        List<Artifact> artifacts = makeArtifacts(jarInJarInJar);

        resolver.process(artifacts);

        final Optional<Path> artifactFile = artifacts.get(0).askForGet(ArtifactFile.class);

        Assertions.assertThat(artifactFile.isPresent()).isTrue();
        Assertions.assertThat(artifactFile.get().toFile().getName()).isEqualTo(JarCreator.jarWithManifestName);
        assertManifestMetadata(artifacts.get(0));
    }
}
