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

package org.eclipse.sw360.antenna.core;

import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.api.exceptions.ConfigurationException;
import org.eclipse.sw360.antenna.api.exceptions.ExecutionException;
import org.eclipse.sw360.antenna.workflow.AntennaWorkflow;
import org.eclipse.sw360.antenna.workflow.AntennaWorkflowConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Central class for Antenna functionality. Orchestrates the entire workflow
 * including analysis, rule verification, report generation, source code
 * bundling and result writing.
 */
public class AntennaCore {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntennaCore.class);

    private final AntennaWorkflow tw;
    private final AntennaContext context;

    /**
     * Run a Antenna execution.
     *
     * @return Attachment map
     *
     * @throws ExecutionException Error in execution
     */
    public Map<String, IAttachable> compose() {
        // Execution workflow begins
        if (context.getToolConfiguration().isSkipAntennaExecution()) {
            LOGGER.info("Antenna execution is skipped.");
            return new HashMap<>();
        } else {
            return tw.execute();
        }
    }

    /**
     * Initialize components for start of the workflow.
     *
     * @param context The antenna context run
     *
     * @throws ConfigurationException Configuration problem
     */
    public AntennaCore(AntennaContext context) {
        this.context = context;
        LOGGER.info("Initializing core ...");
        AntennaWorkflowConfiguration twc = new AntennaWorkflowConfiguration(context);
        tw = new AntennaWorkflow(twc);
        LOGGER.info("Initializing core done");
    }

    public IAttachable writeAnalysisReport() {
        LOGGER.info("Create Analysis Report...");
        final IAttachable report = this.context.getProcessingReporter().writeReportToReportPath();
        LOGGER.info("Create Analysis Report...done");
        return report;
    }

    public AntennaContext getAntennaContext() {
        return context;
    }
}
