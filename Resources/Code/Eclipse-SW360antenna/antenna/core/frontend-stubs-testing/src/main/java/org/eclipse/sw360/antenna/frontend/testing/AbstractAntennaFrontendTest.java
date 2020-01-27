/*
 * Copyright (c) Bosch Software Innovations GmbH 2017-2018.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.frontend.testing;

import org.apache.commons.lang.StringUtils;
import org.eclipse.sw360.antenna.api.IAttachable;
import org.eclipse.sw360.antenna.api.configuration.AntennaContext;
import org.eclipse.sw360.antenna.frontend.AntennaFrontend;
import org.eclipse.sw360.antenna.frontend.testing.testProjects.*;
import org.eclipse.sw360.antenna.model.util.WorkflowComparator;
import org.eclipse.sw360.antenna.model.xml.generated.StepConfiguration;
import org.eclipse.sw360.antenna.model.xml.generated.WorkflowStep;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sw360.antenna.testing.util.AntennaTestingUtils.checkInternetConnectionAndAssume;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("PMD.UnusedFormalParameter")
@RunWith(Parameterized.class)
public abstract class AbstractAntennaFrontendTest {

    private final Supplier<AbstractTestProjectWithExpectations> testDataSupplier;
    protected AbstractTestProjectWithExpectations testData;
    protected AntennaFrontend antennaFrontend;
    protected AntennaContext antennaContext;

    protected boolean isRunExecutionTest() {
        return runExecutionTest;
    }

    protected void setRunExecutionTest(boolean runExecutionTest) {
        this.runExecutionTest = runExecutionTest;
    }

    private boolean runExecutionTest;

    @Parameterized.Parameters(name = "{index}: Test data = {1}")
    public static Collection<Object[]> data() throws IOException {
        // The current working directory is expected to be "<antenna root directory>/assembly/cli".
        String relativePathToP2Product = "../../modules/p2/p2-product/repository_manager";
        File absolutePathToP2Product = new File(relativePathToP2Product).getCanonicalFile();

        if (!absolutePathToP2Product.isDirectory()) {
            throw new IllegalStateException("AbstractAntennaFrontendTest cannot find the '" + absolutePathToP2Product +
                    "' directory. Please ensure to run tests from the Antenna root directory.");
        }

        if (new File(absolutePathToP2Product, "target/products").isDirectory()) {
            return Arrays.asList(new Object[][]{
                    {(Supplier<AbstractTestProjectWithExpectations>) MinimalTestProject::new, "minimal configuration"},
                    {(Supplier<AbstractTestProjectWithExpectations>) BasicTestProject::new, "basic configuration"},
                    {(Supplier<AbstractTestProjectWithExpectations>) ExampleTestProject::new, "example configuration"},
                    {(Supplier<AbstractTestProjectWithExpectations>) MavenTestProject::new, "maven configuration"},
                    {(Supplier<AbstractTestProjectWithExpectations>) P2TestProject::new, "p2 configuration"},
            });
        }

        return Arrays.asList(new Object[][]{
                {(Supplier<AbstractTestProjectWithExpectations>) MinimalTestProject::new, "minimal configuration"},
                {(Supplier<AbstractTestProjectWithExpectations>) BasicTestProject::new, "basic configuration"},
                {(Supplier<AbstractTestProjectWithExpectations>) ExampleTestProject::new, "example configuration"},
                {(Supplier<AbstractTestProjectWithExpectations>) MavenTestProject::new, "maven configuration"},
        });
    }

    public AbstractAntennaFrontendTest(Supplier<AbstractTestProjectWithExpectations> testDataSupplier, String name) {
        this.testDataSupplier = testDataSupplier;
    }

    @Before
    public void initializeTestProject() {
        this.testData = testDataSupplier.get();
    }

    @After
    public void cleanup() throws IOException {
        testData.cleanUpTemporaryProjectFolder();
    }

    @Test
    public void antennaFrontendReadsProject() {
        assertThat(testData.getExpectedProjectArtifactId()).isEqualTo(antennaContext.getProject().getProjectId());
        assertThat(testData.getExpectedProjectVersion()).isEqualTo(antennaContext.getProject().getVersion());
    }

    @Test
    public void checkConfiguredCoordinates() {
        assertThat(testData.getExpectedToolConfigurationProductName()).isEqualTo(antennaContext.getToolConfiguration().getProductName());
        assertThat(testData.getExpectedToolConfigurationProductFullName()).isEqualTo(antennaContext.getToolConfiguration().getProductFullName());
        assertThat(testData.getExpectedToolConfigurationProductVersion()).isEqualTo(antennaContext.getToolConfiguration().getVersion());
    }

    @Test
    public void checkFilesToAttach() {
        List<String> expectedAttachments = testData.getExpectedFilesToAttach();
        assertThat(expectedAttachments).hasSize(antennaContext.getToolConfiguration().getFilesToAttach().size());
        expectedAttachments.forEach(k -> assertThat(antennaContext.getToolConfiguration().getFilesToAttach().stream()
                .anyMatch(f -> f.equals(k))).isTrue());
        antennaContext.getToolConfiguration().getFilesToAttach()
                .forEach(a -> assertThat(StringUtils.countMatches(a, ",")).isEqualTo(2));
    }

    @Test
    public void checkConfigFiles() {
        List<String> expectedConfigFilesEndings = testData.getExpectedToolConfigurationConfigFilesEndings();
        List<File> actualConfigFiles = antennaContext.getToolConfiguration().getConfigFiles();
        assertThat(expectedConfigFilesEndings).hasSize(actualConfigFiles.size());
        assertThat(actualConfigFiles.stream()
                .map(File::toString)
                .allMatch(s -> expectedConfigFilesEndings.stream()
                        .anyMatch(s::endsWith))).isTrue();
    }

    @Test
    public void checkParsedConfigFiles() {
        List<String> expectedConfigFiles = testData.getExpectedToolConfigurationConfigFiles();
        assertThat(expectedConfigFiles).hasSize(antennaContext.getToolConfiguration().getConfigFiles().size());
        expectedConfigFiles.stream()
                .map(f -> new File(f).getName())
                .forEach(basename -> assertThat(antennaContext.getToolConfiguration().getConfigFiles().stream()
                        .anyMatch(file -> file.getName().equals(basename))).isTrue());
    }

    private void assertThatStepsAreEqualUpToOrder(List<WorkflowStep> l1, List<WorkflowStep> l2) {
        try {
            assertTrue(WorkflowComparator.areEqual(l1, l2));
        } catch (AssertionError e) {
            final Function<List<WorkflowStep>, String> lToStringFunction = l -> l.stream()
                    .map(s -> s.getName() + "(" + s.getClassHint() + ") " + stepConfigToString(s.getConfiguration()))
                    .map(s -> "[" + s + "]")
                    .collect(Collectors.joining(",\n\t\t"));
            String l1String = lToStringFunction.apply(l1);
            String l2String = lToStringFunction.apply(l2);
            String msg = "Step Configurations are not equal:\n" +
                    "\texpected contains: " + l1String +
                    "\n\tactual contains: " + l2String;
            throw new AssertionError(msg, e);
        }
    }

    private String stepConfigToString(StepConfiguration stepConfiguration) {
        return stepConfiguration == null
                ? ""
                : stepConfiguration
                .getAsMap()
                .entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    @Test
    public void checkParsedWorkflowForAnalyzers() {
        List<WorkflowStep> expectedAnalyzers = testData.getExpectedToolConfigurationAnalyzers();
        if (antennaContext.getToolConfiguration().getWorkflow().getAnalyzers() == null) {
            assertThat(expectedAnalyzers).hasSize(0);
            return;
        }
        List<WorkflowStep> actualAnalyzers = antennaContext.getToolConfiguration().getWorkflow().getAnalyzers().getStep();
        assertThatStepsAreEqualUpToOrder(expectedAnalyzers, actualAnalyzers);
    }

    @Test
    public void checkParsedWorkflowForProcessors() {
        List<WorkflowStep> expectedProcessors = testData.getExpectedToolConfigurationProcessors();
        if (antennaContext.getToolConfiguration().getWorkflow().getProcessors() == null) {
            assertThat(expectedProcessors).hasSize(0);
            return;
        }
        List<WorkflowStep> actualProcessors = antennaContext.getToolConfiguration().getWorkflow().getProcessors().getStep();
        assertThatStepsAreEqualUpToOrder(expectedProcessors, actualProcessors);
    }

    @Test
    public void checkParsedWorkflowForGenerators() {
        List<WorkflowStep> expectedGenerators = testData.getExpectedToolConfigurationGenerators();
        if (antennaContext.getToolConfiguration().getWorkflow().getGenerators() == null) {
            assertThat(expectedGenerators).hasSize(0);
            return;
        }
        List<WorkflowStep> actualGenerators = antennaContext.getToolConfiguration().getWorkflow().getGenerators().getStep();
        assertThatStepsAreEqualUpToOrder(expectedGenerators, actualGenerators);
    }

    @Test
    public void checkParsedWorkflowForOutputHandlers() {
        List<WorkflowStep> expectedOutputHandlers = testData.getExpectedToolConfigurationOutputHandlers();
        if (antennaContext.getToolConfiguration().getWorkflow().getOutputHandlers() == null) {
            assertThat(expectedOutputHandlers).hasSize(0);
            return;
        }
        List<WorkflowStep> actualOutputHandlers = antennaContext.getToolConfiguration().getWorkflow().getOutputHandlers().getStep();
        assertThatStepsAreEqualUpToOrder(expectedOutputHandlers, actualOutputHandlers);
    }

    @Test
    public void checkProxyPort() {
        assertThat(testData.getExpectedProxyPort()).isEqualTo(antennaContext.getToolConfiguration().getProxyPort());
    }

    @Test
    public void checkProxyHost() {
        assertThat(testData.getExpectedProxyHost()).isEqualTo(antennaContext.getToolConfiguration().getProxyHost());
    }

    @Test
    public void checkBooleans() {
        assertThat(testData.getExpectedToolConfigurationMavenInstalled()).isEqualTo(antennaContext.getToolConfiguration().isMavenInstalled());
        assertThat(testData.getExpectedToolConfigurationAttachAll()).isEqualTo(antennaContext.getToolConfiguration().isAttachAll());
        assertThat(testData.getExpectedToolConfigurationSkip()).isEqualTo(antennaContext.getToolConfiguration().isSkipAntennaExecution());
    }

    protected void protoypeExecutionTest(RunnableWithExceptions executor, Function<AntennaFrontend, Map<String, IAttachable>> buildArtifactsGetter)
            throws Exception {
        assumeTrue("The test data " + testData.getClass().getSimpleName() + " is not executable", testData instanceof ExecutableTestProject);
        checkInternetConnectionAndAssume(Assume::assumeTrue);

        executor.run();

        Path pathToTarget = testData.projectRoot.resolve("target");
        assertThat(pathToTarget.toFile().exists()).isTrue();

        Map<String, IAttachable> buildArtifacts = buildArtifactsGetter.apply(antennaFrontend);

        ((ExecutableTestProject) testData).assertExecutionResult(pathToTarget, buildArtifacts, antennaContext);
    }

    @FunctionalInterface
    public interface RunnableWithExceptions {
        void run() throws Exception;
    }
}
