/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.model.test;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactCoordinates;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelectorAndSet;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIdentifier;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactSelectorTest {

    private final String defaultFileName = "defaultFileName";

    @Test
    public void testJustFilename() {
        // Test identical Artifact and ArtifactExampel;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "symbolicName");
        ArtifactSelector example = createArtifactSelector(defaultFileName, null, null, null, null, null, null);
        assertThat(example.matches(artifact)).isTrue();
    }

    @Test
    public void test() {
        // Test identical Artifact and ArtifactExampel;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "symbolicName");
        ArtifactSelector example = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", "bundleVersion", "symbolicName");
        assertThat(example.matches(artifact)).isTrue();
    }

    @Test
    public void testWithOutBundleCoordinates1() {
        // Test ArtifactExample without BundleCoordinates;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "symbolicName");
        ArtifactSelector example = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", null, null);
        assertThat(example.matches(artifact)).isTrue();
    }

    @Test
    public void testWithOutBundleCoordinates2() {
        // Test Artifact without BundleCoordinates;
        Artifact artifact2 = createArtifact("testHash", "testMvnID", "testGroup", "version", null,
                null);
        ArtifactSelector example2 = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", "bundleVersion", "symbolicName");
        assertThat(example2.matches(artifact2)).isFalse();
    }

    @Test
    public void testWithOutBundleCoordinates3() {
        // Test both without BundleCoordinates;
        Artifact artifact3 = createArtifact("testHash", "testMvnID", "testGroup", "version", null,
                null);
        ArtifactSelector example3 = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", null, null);
        assertThat(example3.matches(artifact3)).isTrue();
    }

    @Test
    public void testWithOutMavenCoordinates1() {
        // Test ArtifactExample without MavenCoordinates;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example = createArtifactSelector(defaultFileName, "testHash", null, null, null, "bundleVersion",
                "bundleVersion");
        assertThat(example.matches(artifact)).isTrue();
    }

    @Test
    public void testWithOutMavenCoordinates2() {
        // Test Artifact without MavenCoordinates;
        Artifact artifact2 = createArtifact("testHash", null, null, null, "bundleVersion",
                "symbolicName");
        ArtifactSelector example2 = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", "bundleVersion", "symbolicName");
        assertThat(example2.matches(artifact2)).isFalse();
    }

    @Test
    public void testWithOutMavenCoordinates3() {
        // Test both without MavenCoordinates;
        Artifact artifact3 = createArtifact("testHash", null, null, null, "symbolicName",
                "bundleVersion");
        ArtifactSelector example3 = createArtifactSelector(defaultFileName, "testHash", null, null, null, "symbolicName",
                "bundleVersion");
        assertThat(example3.matches(artifact3)).isTrue();
    }

    @Test
    public void testWithOutArtifactExampleAttributes1() {
        // Test ArtifactExample without MavenCoordinates;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example = createArtifactSelector(defaultFileName, "testHash", null, null, null, "bundleVersion",
                "bundleVersion");
        assertThat(example.matches(artifact)).isTrue();
    }

    @Test
    public void testWithOutArtifactExampleAttributes2() {
        // Test Artifact without MavenCoordinates;
        Artifact artifact2 = createArtifact("testHash", null, null, null, "bundleVersion",
                "symbolicName");
        ArtifactSelector example2 = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", "bundleVersion", "symbolicName");
        assertThat(example2.matches(artifact2)).isFalse();
    }

    @Test
    public void testWithOutArtifactExampleAttributes3() {
        // Test both without MavenCoordinates;
        Artifact artifact3 = createArtifact("testHash", null, null, null, "symbolicName",
                "bundleVersion");
        ArtifactSelector example3 = createArtifactSelector(defaultFileName, "testHash", null, null, null, "symbolicName",
                "bundleVersion");
        assertThat(example3.matches(artifact3)).isTrue();
    }

    @Test
    public void testWithWrongExampleAttributes1() {
        // Test ArtifactExample without MavenCoordinates;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example = createArtifactSelector(defaultFileName, "testHash", "bla", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        assertThat(example.matches(artifact)).isFalse();
    }

    @Test
    public void testWithWrongExampleAttributes2() {
        // Test Artifact without MavenCoordinates;
        Artifact artifact2 = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example2 = createArtifactSelector(defaultFileName, "bla", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        assertThat(example2.matches(artifact2)).isFalse();
    }

    @Test
    public void testWithWrongExampleAttributes3() {
        // Test both without MavenCoordinates;
        Artifact artifact3 = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example3 = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", "test", "test");
        assertThat(example3.matches(artifact3)).isFalse();
    }

    @Test
    public void testWithPatternExampleAttributes1() {
        // Test ArtifactExample without MavenCoordinates;
        Artifact artifact = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example = createArtifactSelector("default*Name", "testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        assertThat(example.matches(artifact)).isTrue();
    }

    @Test
    public void testWithPatternExampleAttributes2() {
        // Test Artifact without MavenCoordinates;
        Artifact artifact2 = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example2 = createArtifactSelector(defaultFileName, "testHash", "testMvnID", "testGroup",
                "version", "bundle*Version", "bundle*");
        assertThat(example2.matches(artifact2)).isTrue();
    }

    @Test
    public void testWithPatternExampleAttributes3() {
        // Test both without MavenCoordinates;
        Artifact artifact3 = createArtifact("testHash", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        ArtifactSelector example3 = createArtifactSelector(defaultFileName, "test*", "testMvnID", "testGroup", "version",
                "bundleVersion", "bundleVersion");
        assertThat(example3.matches(artifact3)).isTrue();
    }

    private Artifact createArtifact(String hash, String artifactId, String groupId, String version,
                                    String bundleVersion, String symbolicName) {
        final Artifact artifact = new Artifact();
        if (hash != null) {
            artifact.addFact(new ArtifactFilename(defaultFileName, hash));
        }
        if (artifactId != null || groupId != null || version != null) {
            artifact.addCoordinate(new Coordinate(Coordinate.Types.MAVEN, groupId, artifactId, version));
        }
        if (symbolicName != null || bundleVersion != null) {
                artifact.addCoordinate(new Coordinate(Coordinate.Types.P2, symbolicName, bundleVersion));
        }
        return artifact;
    }

    private ArtifactSelector createArtifactSelector(String filename, String hash, String artifactId, String groupId,
            String version, String bundleVersion, String symbolicName) {

        Set<ArtifactIdentifier> identifierSet = new HashSet<>();
        if (filename != null || hash != null) {
            identifierSet.add(new ArtifactFilename(filename, hash));
        }
        if (artifactId != null || groupId != null || version != null) {
            identifierSet.add(new ArtifactCoordinates(new Coordinate(Coordinate.Types.MAVEN, groupId, artifactId, version)));
        }
        if (symbolicName != null || bundleVersion != null) {
            identifierSet.add(new ArtifactCoordinates(new Coordinate(Coordinate.Types.P2, symbolicName, bundleVersion)));
        }
        return new ArtifactSelectorAndSet(identifierSet);
    }
}
