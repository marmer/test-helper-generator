package org.java.test.helper.generator.maven.plugin;

import org.apache.commons.io.FileUtils;

import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import org.codehaus.plexus.util.cli.CommandLineException;

import org.hamcrest.io.FileMatchers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.hamcrest.Matchers.is;

import static org.hamcrest.io.FileMatchers.anExistingFile;

import static org.junit.Assert.assertThat;


public class MatchersMojoJDK6AndOldHamcrestSystemTest {
	@Rule
	public TestResources testResources = new TestResources();

	private File testProject;

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteQuietly(testProject);
		testProject = testResources.getBasedir("testprojectJava6AndOldestPossibleHamcrestVersion");
	}

	@Test
	public void testTestprojectShouldHavePom() throws Exception {
		assertThat(pomFile(), FileMatchers.aFileNamed(equalTo("pom.xml")));
	}

	@Test
	public void testTestprojectShouldBeBuildableWithoutMojoExecution() throws Exception {
		// Preparation

		// Execution
		final int exitStatus = executeGoals("clean", "compile");

		// Expectation
		assertThat("Execution exit status", exitStatus, is(0));
	}

	@Test
	public void testPluginExecutionShouldWorkWithoutAnyErrors() throws Exception {
		// Preparation

		// Execution
		final int exitStatus = executeGoals("testHelperGenerator:matchers");

		// Expectation
		assertThat("Execution exit status", exitStatus, is(0));
	}

	@Test
	public void testPhaseTestShouldStillWorkAfterPluginExecutionWithoutAnyErrors() throws Exception {
		// Preparation

		// Execution
		final int exitStatus = executeGoals("testHelperGenerator:matchers", "test");

		// Expectation
		assertThat("Execution exit status", exitStatus, is(0));
	}

	@Test
	public void testPluginRunHasCreatedMatcherSourceOnCallingPluginGoalDirectly() throws Exception {

		// Preparation
		assertThat("For this test required file is missing",
			inSrcMainJava("some/pck/model/SimpleModel.java"),
			is(anExistingFile()));

		// Execution
		executeGoals("testHelperGenerator:matchers");

		// Expectation
		assertThat(
			"Should have been generated: " +
			inGeneratedTestSourcesDir("some/pck/model/SimpleModelMatcher.java"),
			inGeneratedTestSourcesDir("some/pck/model/SimpleModelMatcher.java"),
			is(anExistingFile()));
	}

	@Test
	public void testPluginRunHasCreatedMatcherSourceOnTestGoal() throws Exception {

		// Preparation
		assertThat("For this test required file is missing",
			inSrcMainJava("some/pck/model/SimpleModel.java"),
			is(anExistingFile()));

		// Execution
		executeGoals("test");

		// Expectation
		assertThat(
			"Should have been generated: " +
			inGeneratedTestSourcesDir("some/pck/model/SimpleModelMatcher.java"),
			inGeneratedTestSourcesDir("some/pck/model/SimpleModelMatcher.java"),
			is(anExistingFile()));
	}

	private File inGeneratedTestSourcesDir(final String path) {
		return new File(generatedTestSourcesDir(), path);
	}

	private File inSrcMainJava(final String path) {
		return new File(getSrcMainJavaDir(), path);
	}

	private File getSrcMainJavaDir() {
		return new File(testProject, "src/main/java");
	}

	private File generatedTestSourcesDir() {
		return new File(targetDir(), "generated-test-sources");
	}

	private File targetDir() {
		return new File(testProject, "target");
	}

	private File pomFile() {
		return new File(testProject, "pom.xml");
	}

	private int executeGoals(final String... goals) throws MavenInvocationException, CommandLineException {
		final InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(pomFile());
		request.setGoals(Arrays.asList(goals));

		final Invoker invoker = new DefaultInvoker();
		prepareExecutableFor(invoker);

		final InvocationResult result = invoker.execute(request);

		if (result.getExecutionException() != null) {
			throw result.getExecutionException();
		}

		return result.getExitCode();
	}

	private void prepareExecutableFor(final Invoker invoker) {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			final String getenv = System.getenv("M2_HOME");
			File mavenExecutable = new File(getenv, "bin/mvn.cmd");

			if (!mavenExecutable.exists()) {
				mavenExecutable = new File(getenv, "bin/mvn.bat");
			}

			invoker.setMavenExecutable(mavenExecutable);
		}
	}
}
