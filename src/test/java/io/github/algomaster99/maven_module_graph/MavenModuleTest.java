package io.github.algomaster99.maven_module_graph;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class MavenModuleTest {

	@Test
	void legend_engine(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/legend-engine/legend-engine",
				"src/test/resources/legend-engine/output.txt",
				"src/test/resources/legend-engine/output.json",
				temp.resolve("legend-engine.txt"),
				temp.resolve("legend-engine.json"),
				2, 2, false
		);
	}

	@Test
	void neo4j_profileModules(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTestWithLineCountCheck(
				"src/test/resources/neo4j/neo4j",
				"src/test/resources/neo4j/output.txt",
				"src/test/resources/neo4j/output.json",
				temp.resolve("neo4j.txt"),
				temp.resolve("neo4j.json"),
				2, 0,
				125
		);
	}

	@Test
	void arthas_mavenProperties(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/arthas/arthas",
				"src/test/resources/arthas/output.txt",
				"src/test/resources/arthas/output.json",
				temp.resolve("arthas.txt"),
				temp.resolve("arthas.json"),
				2, 0, false
		);
	}

	@Test
	void persistence_differentProjectRoot(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/persistence/persistence/api",
				"src/test/resources/persistence/output.txt",
				"src/test/resources/persistence/output.json",
				temp.resolve("persistence.txt"),
				temp.resolve("persistence.json"),
				0, 2, false
		);
	}

	@Test
	void slingfeature_maven_plugin_differentPomEncoding(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/sling-maven/sling-maven",
				"src/test/resources/sling-maven/output.txt",
				"src/test/resources/sling-maven/output.json",
				temp.resolve("slingfeature-maven-plugin.txt"),
				temp.resolve("slingfeature-maven-plugin.json"),
				2, 2, false
		);
	}

	@Test
	void karaf_oneModuleAbsent(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/karaf/karaf",
				"src/test/resources/karaf/output.txt",
				"src/test/resources/karaf/output.json",
				temp.resolve("karaf.txt"),
				temp.resolve("karaf.json"),
				0, 0, true
		);
	}

	@Test
	void bnd_rootVersionIsNull(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/bnd/bnd/maven",
				"src/test/resources/bnd/output.txt",
				"src/test/resources/bnd/output.json",
				temp.resolve("bnd.txt"),
				temp.resolve("bnd.json"),
				2, 2, true
		);
	}

	@Test
	void sampleModule_embedFsPath(@TempDir Path temp) throws XmlPullParserException, IOException {
		runTest(
				"src/test/resources/sample-module/sample-module",
				"src/test/resources/sample-module/output.txt",
				"src/test/resources/sample-module/output.json",
				temp.resolve("sample-module.txt"),
				temp.resolve("sample-module.json"),
				2, 2, false, true
		);
	}

	private void runTest(
			String modulePath,
			String expectedTextPath,
			String expectedJsonPath,
			Path actualTextPath,
			Path actualJsonPath,
			int textIndent,
			int jsonIndent,
			boolean excludeProfiles,
			boolean fsPath) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of(modulePath), null, new HashMap<>(), excludeProfiles);
		Path expectedPlainText = Path.of(expectedTextPath);
		Path expectedJson = Path.of(expectedJsonPath);

		// act
		Utility.printToFile(module, actualTextPath, textIndent);
		Utility.printToJson(module, actualJsonPath, jsonIndent, fsPath);

		// assert
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualTextPath)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJsonPath)));
	}

	private void runTest(
			String modulePath,
			String expectedTextPath,
			String expectedJsonPath,
			Path actualTextPath,
			Path actualJsonPath,
			int textIndent,
			int jsonIndent,
			boolean excludeProfiles
	) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of(modulePath), null, new HashMap<>(), excludeProfiles);
		Path expectedPlainText = Path.of(expectedTextPath);
		Path expectedJson = Path.of(expectedJsonPath);

		// act
		Utility.printToFile(module, actualTextPath, textIndent);
		Utility.printToJson(module, actualJsonPath, jsonIndent, false);

		// assert
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualTextPath)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJsonPath)));
	}

	private void runTestWithLineCountCheck(
			String modulePath,
			String expectedTextPath,
			String expectedJsonPath,
			Path actualTextPath,
			Path actualJsonPath,
			int textIndent,
			int jsonIndent,
			int expectedLineCount
	) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of(modulePath), null, new HashMap<>(), false);
		Path expectedPlainText = Path.of(expectedTextPath);
		Path expectedJson = Path.of(expectedJsonPath);

		// act
		Utility.printToFile(module, actualTextPath, textIndent);
		Utility.printToJson(module, actualJsonPath, jsonIndent, false);

		// assert
		assertThat(Files.readAllLines(expectedPlainText).size(), equalTo(expectedLineCount));
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualTextPath)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJsonPath)));
	}
}
