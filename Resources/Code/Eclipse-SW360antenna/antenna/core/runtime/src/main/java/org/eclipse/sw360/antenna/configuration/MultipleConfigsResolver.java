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

package org.eclipse.sw360.antenna.configuration;

import org.eclipse.sw360.antenna.api.configuration.ToolConfiguration;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.model.Configuration;
import org.eclipse.sw360.antenna.model.SW360ProjectCoordinates;
import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.ArtifactSelector;
import org.eclipse.sw360.antenna.model.reporting.MessageType;
import org.eclipse.sw360.antenna.model.reporting.Report;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.report.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Merges a list of configurations if possible. If a conflict occurs it is
 * written to the temporary reporter. With the checkReport() method it can be
 * checked if this reporter contains messages. If yes an Exception is thrown and
 * the conflicts must be solved.
 */
public class MultipleConfigsResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleConfigsResolver.class);

    private ConfigurationReader configurationReader;
    private Path antennaTargetDirectory;
    private Reporter tempReporter;

    /**
     *
     * Merges a list of configurations if possible. If a conflict occurs it is
     * written to the temporary reporter. With the checkReport() method it can
     * be checked if this reporter contains messages. If yes an Exception is
     * thrown and the conflicts must be solved.
     *
     * @param toolConfiguration
     *              General tool configuration
     *
     * @return The resolved configurations
     *
     * @throws ConfigurationException Configuration Problem
     */
    public Configuration resolveConfigs(ToolConfiguration toolConfiguration) {
        return resolveConfigs(toolConfiguration, true);
    }

    protected Configuration resolveConfigs(ToolConfiguration toolConfiguration, boolean checkReport) {
        this.configurationReader = new ConfigurationReader(toolConfiguration.getEncoding());
        this.antennaTargetDirectory = toolConfiguration.getAntennaTargetDirectory();
        this.tempReporter = new Reporter(antennaTargetDirectory, toolConfiguration.getEncoding());

        List<File> configFiles = toolConfiguration.getConfigFiles();
        List<URI> configFileUris = toolConfiguration.getConfigFileUris();

        List<Configuration> configurations = new ArrayList<>();
        if (configFiles != null && !configFiles.isEmpty()) {
            configurations.addAll(resolveConfigs(configFiles));
        }

        if (configFileUris != null && !configFileUris.isEmpty()) {
            configurations.addAll(resolveUris(configFileUris));
        }

        Configuration resolvedConfigs = mergeConfigurations(configurations);
        LOGGER.debug("List of configurations merged.");

        if(checkReport) {
            checkReport();
        }

        return resolvedConfigs;
    }

    private ArrayList<Configuration> resolveConfigs(List<File> configs) {
        LOGGER.debug("Resolve list of configurations.");
        ArrayList<Configuration> configurations = new ArrayList<>();
        for (File config : configs) {
            Configuration configuration = this.configurationReader.readConfigFromFile(config, this.antennaTargetDirectory);
            configurations.add(configuration);
        }
        return configurations;
    }

    /**
     * Check processing reports for conflict messages.
     */
    private void checkReport() {
        Report processingReport = tempReporter.getProcessingReport();
        if (processingReport.getMessageList().size() > 0) {
            tempReporter.writeReport(System.out);
            tempReporter.writeReportToReportPath();

            String msg = "There are conflicting configurations. Please have a look at the processing Report and resolve them.";
            LOGGER.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    private List<Configuration> resolveUris(List<URI> uris) {
        LOGGER.debug("Resolve list of configuration file uris.");
        List<Configuration> configurations = new ArrayList<>();
        for (URI uri : uris) {
            Configuration configuration = this.configurationReader.readConfigFromUri(uri, this.antennaTargetDirectory);
            configurations.add(configuration);

        }
        return configurations;
    }

    private Configuration mergeConfigurations(List<Configuration> configurations) {
        if (configurations.size() == 0) {
            return new Configuration(null);
        }

        Configuration mergedConfig = new Configuration();
        mergeIgnoreForSourceResolving(configurations, mergedConfig);
        mergeOverride(configurations, mergedConfig);
        mergeValidForMissingSources(configurations, mergedConfig);
        mergeValidForIncompleteSources(configurations, mergedConfig);
        mergeRemoveArtifact(configurations, mergedConfig);
        mergeAddArtifact(configurations, mergedConfig);
        mergeFinalLicenses(configurations, mergedConfig);
        mergeFailOnIncompleteSources(configurations, mergedConfig);
        mergeFailOnMissingSources(configurations, mergedConfig);
        mergeSecurityIssues(configurations, mergedConfig);
        mergeSecurityIssueSuppresses(configurations, mergedConfig);
        mergeWrappedConfiguredSW360Project(configurations, mergedConfig);
        return mergedConfig;
    }

    private void mergeWrappedConfiguredSW360Project(List<Configuration> configurations, Configuration mergedConfig) {
        if (configurations.size() > 0) {
            SW360ProjectCoordinates project = configurations.get(0).getConfiguredSW360Project();
            configurations.stream()
                    .skip(1)
                    .map(Configuration::getConfiguredSW360Project)
                    .filter(p -> !project.equals(p) && (p.getName() != null || p.getVersion() != null))
                    .findAny()
                    .ifPresent(c -> tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                            "Conflicting configurations in the \"configuredSW360Project\" section."));
            mergedConfig.setConfiguredSW360Project(project);
        }
    }

    private void mergeSecurityIssues(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setSecurityIssues(configurations.stream()
                .map(Configuration::getSecurityIssues)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private void mergeSecurityIssueSuppresses(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setSuppressedSecurityIssues(configurations.stream()
                .map(Configuration::getSuppressedSecurityIssues)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Configuration.suppressedSecurityIssuesConflictResolver)));
    }

    private void mergeAddArtifact(List<Configuration> configurations, Configuration mergedConfig) {
        List<Artifact> mergeAddArtifact = new ArrayList<>();
        for (Configuration configuration : configurations) {
            List<Artifact> addArtifact = configuration.getAddArtifact();
            for (Artifact artifact : addArtifact) {
                if (!mergeAddArtifact.contains(artifact)) {
                    mergeAddArtifact.add(artifact);
                }
            }

        }
        mergedConfig.setAddArtifact(mergeAddArtifact);
    }

    private void mergeFinalLicenses(List<Configuration> configurations, Configuration mergedConfig) {
        Map<ArtifactSelector, LicenseInformation> mergedFinalLicenses = new HashMap<>();
        for (Configuration configuration : configurations) {
            Map<ArtifactSelector, LicenseInformation> setFinalLicense = configuration.getFinalLicenses();
            for (Map.Entry<ArtifactSelector, LicenseInformation> selectorEntry : setFinalLicense.entrySet()) {
                if (!mergedFinalLicenses.containsKey(selectorEntry.getKey())) {
                    mergedFinalLicenses.put(selectorEntry.getKey(), selectorEntry.getValue());
                } else {
                    LicenseInformation merged = mergedFinalLicenses.get(selectorEntry.getKey());
                    LicenseInformation actual = setFinalLicense.get(selectorEntry.getKey());
                    if (!merged.equals(actual)) {
                        tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                                "Conflicting configurations in the \"set final license\" section, the declared licenses are not the same. " +
                                        "(artifact selector was=[" + selectorEntry.getKey().toString() + "])");
                    }
                }
            }
        }
        mergedConfig.setFinalLicenses(mergedFinalLicenses);
    }

    private void mergeFailOnIncompleteSources(List<Configuration> configurations, Configuration mergedConfig) {
        boolean equal = configurations.get(0).isFailOnIncompleteSources();
        for (Configuration configuration : configurations) {

            if (!(equal == configuration.isFailOnIncompleteSources())) {
                tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                        "Conflicting configurations for the attribute \"failOnIncompleteSources\".");
            }
        }
        mergedConfig.setFailOnIncompleteSources(equal);
    }

    private void mergeFailOnMissingSources(List<Configuration> configurations, Configuration mergedConfig) {
        boolean equal = configurations.get(0).isFailOnMissingSources();
        for (Configuration configuration : configurations) {
            if (!(equal == configuration.isFailOnMissingSources())) {
                tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                        "Conflicting configurations for the attribute \"failOnMissingSources\".");
                return;
            }
        }
        mergedConfig.setFailOnMissingSources(equal);
    }

    private void mergeRemoveArtifact(List<Configuration> configurations, Configuration mergedConfig) {
        List<ArtifactSelector> mergedRemove = new ArrayList<>();
        for (Configuration configuration : configurations) {
            List<ArtifactSelector> removeArtifact = configuration.getRemoveArtifact();
            mergedRemove.addAll(removeArtifact);
        }
        mergedConfig.setRemoveArtifact(mergedRemove);
    }

    private void mergeValidForIncompleteSources(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setValidForIncompleteSources(configurations.stream()
                .map(Configuration::getValidForIncompleteSources)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void mergeValidForMissingSources(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setValidForMissingSources(configurations.stream()
                .map(Configuration::getValidForMissingSources)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void mergeOverride(List<Configuration> configurations, Configuration mergedConfig) {
        Map<ArtifactSelector, Artifact> mergedOverride = new HashMap<>();
        for (Configuration configuration : configurations) {
            Map<ArtifactSelector, Artifact> override = configuration.getOverride();
            for (Map.Entry<ArtifactSelector, Artifact> selectorEntry : override.entrySet()) {
                Artifact compare = selectorEntry.getValue();
                if (mergedOverride.containsKey(selectorEntry.getKey())) {
                    Artifact generatedArtifact = mergedOverride.get(selectorEntry.getKey());
                    if (!generatedArtifact.equals(compare)) {
                        tempReporter.add(MessageType.CONFLICTING_CONFIGURATIONS,
                                "Conflicting configurations in the override section at artifact: "
                                        + "the override values are not equal. (artifact selector was=[" + selectorEntry.getKey().toString() + "])");
                        return;
                    }
                } else {
                    mergedOverride.put(selectorEntry.getKey(), compare);
                }
            }
        }
        mergedConfig.setOverride(mergedOverride);
    }

    private void mergeIgnoreForSourceResolving(List<Configuration> configurations, Configuration mergedConfig) {
        mergedConfig.setIgnoreForSourceResolving(configurations.stream()
                .map(Configuration::getIgnoreForSourceResolving)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList()));
    }

    public Reporter getReporter() {
        return this.tempReporter;
    }
}
