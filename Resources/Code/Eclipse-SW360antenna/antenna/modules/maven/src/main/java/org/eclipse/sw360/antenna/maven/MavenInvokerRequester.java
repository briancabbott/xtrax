/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2019.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.maven;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.model.coordinates.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Requests jar files for artifacts by using Maven Invoker. Use of this class
 * requires Maven to be installed locally.
 * See http://maven.apache.org/shared/maven-invoker/usage.html
 */
public class MavenInvokerRequester extends IArtifactRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenInvokerRequester.class);
    public static final String POM_FILENAME = "pom.xml";

    private static final String MVN_ARG_GROUP_ID = "\"-DgroupId=%s\"";
    private static final String MVN_ARG_ARTIFACT_ID = "\"-DartifactId=%s\"";
    private static final String MVN_ARG_VERSION = "\"-Dversion=%s\"";
    private static final String MVN_ARG_DEST = "\"-Ddest=%s\"";
    private static final String MVN_ARG_CLASSIFIER = "\"-Dclassifier=%s\"";
    private static final String MVN_ARG_REPOS = "\"-DremoteRepositories=%s\"";
    private static final String MVN_DOWNLOAD_CMD = "dependency:get --quiet";
    private final File basedir;

    private DefaultInvoker defaultInvoker;
    private Optional<URL> sourceRepositoryUrl;

    public MavenInvokerRequester(File basedir) {
        this(basedir, new DefaultInvoker(), Optional.empty());
    }

    public MavenInvokerRequester(File basedir, URL sourceRepositoryUrl) {
        this(basedir, new DefaultInvoker(), Optional.of(sourceRepositoryUrl));
    }

    public MavenInvokerRequester(File basedir, DefaultInvoker defaultInvoker, Optional<URL> sourceRepositoryUrl) {
        super();
        this.basedir = basedir;
        this.defaultInvoker = defaultInvoker;
        if (System.getenv("M2_HOME") != null) {
            defaultInvoker.setMavenExecutable(new File(System.getenv("M2_HOME")));
        }
        this.sourceRepositoryUrl = sourceRepositoryUrl;
    }

    @Override
    public Optional<File> requestFile(Coordinate mavenCoordinate, Path targetDirectory, ClassifierInformation classifierInformation) {

        File expectedJarFile = getExpectedJarFile(mavenCoordinate, targetDirectory, classifierInformation);

        if (expectedJarFile.exists()) {
            LOGGER.info("The file " + expectedJarFile + " already exists and won't be downloaded again");
            return Optional.of(expectedJarFile);
        }

        LOGGER.debug("Requesting artifact with id " + mavenCoordinate.toString());
        boolean requestSuccessful = callMavenInvoker(mavenCoordinate, targetDirectory, classifierInformation.classifier);

        String jarType = classifierInformation.isSource ? "sources jar" : classifierInformation.classifier + " jar";
        if (!requestSuccessful) {
            LOGGER.warn("Failed to find " + jarType + ": Artifact " + mavenCoordinate.toString() + " not found in repo.");
            return Optional.empty();
        } else if (!expectedJarFile.exists()) {
            LOGGER.warn("Failed to find " + jarType + ": Maven call succeeded but Artifact was not generated in the expected place.");
            return Optional.empty();
        }

        return Optional.of(getExpectedJarFile(mavenCoordinate, targetDirectory, classifierInformation));
    }

    private boolean callMavenInvoker(Coordinate mavenCoordinate, Path targetDirectory, String classifier) {
        final List<String> mvnDownloadCmd = buildBasicMvnDownloadCmd(mavenCoordinate, targetDirectory);
        sourceRepositoryUrl
                .ifPresent(url -> mvnDownloadCmd.add(String.format(MVN_ARG_REPOS, url.toString())));
        if (!classifier.isEmpty()) {
            mvnDownloadCmd.add(String.format(MVN_ARG_CLASSIFIER, classifier));
        }
        InvocationRequest request = buildInvocationRequest(mvnDownloadCmd);
        return callMavenInvocationRequest(request);
    }

    private List<String> buildBasicMvnDownloadCmd(Coordinate mavenCoordinate, Path targetDirectory) {
        List<String> mvnDownloadCmd = new ArrayList<>();

        mvnDownloadCmd.add(String.format(MVN_ARG_GROUP_ID, mavenCoordinate.getNamespace()));
        mvnDownloadCmd.add(String.format(MVN_ARG_ARTIFACT_ID, mavenCoordinate.getName()));
        mvnDownloadCmd.add(String.format(MVN_ARG_VERSION, mavenCoordinate.getVersion()));
        mvnDownloadCmd.add(String.format(MVN_ARG_DEST, targetDirectory));
        mvnDownloadCmd.add(MVN_DOWNLOAD_CMD);

        return mvnDownloadCmd;
    }

    protected File getPomFileFromContext() {
        return new File(basedir, POM_FILENAME);
    }

    private InvocationRequest buildInvocationRequest(List<String> mvnDownloadCmd) {
        InvocationRequest request = new DefaultInvocationRequest();

        request.setPomFile(getPomFileFromContext());
        request.setGoals(mvnDownloadCmd);
        request.setOutputHandler(LOGGER::debug);

        return request;
    }

    private File getExpectedJarFile(Coordinate mavenCoordinate, Path targetDirectory, ClassifierInformation classifierInformation) {
        String jarBaseName = getExpectedJarBaseName(mavenCoordinate, classifierInformation);
        return new File(targetDirectory.toFile(), jarBaseName);
    }

    private boolean callMavenInvocationRequest(InvocationRequest request) {
        try {
            LOGGER.info("Calling Maven Invoker with command " + String.join(", ", request.getGoals()));
            return defaultInvoker.execute(request)
                    .getExitCode() == 0;
        } catch (MavenInvocationException e) {
            throw new ExecutionException("Error when getting jar: " + e);
        }

    }
}
