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
				2, 2
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
				2, 0
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
				0, 2
		);
	}

	private void runTest(
			String modulePath,
			String expectedTextPath,
			String expectedJsonPath,
			Path actualTextPath,
			Path actualJsonPath,
			int textIndent,
			int jsonIndent
	) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of(modulePath), null, new HashMap<>());
		Path expectedPlainText = Path.of(expectedTextPath);
		Path expectedJson = Path.of(expectedJsonPath);

		// act
		Utility.printToFile(module, actualTextPath, textIndent);
		Utility.printToJson(module, actualJsonPath, jsonIndent);

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
		MavenModule module = Utility.createMavenModuleGraph(Path.of(modulePath), null, new HashMap<>());
		Path expectedPlainText = Path.of(expectedTextPath);
		Path expectedJson = Path.of(expectedJsonPath);

		// act
		Utility.printToFile(module, actualTextPath, textIndent);
		Utility.printToJson(module, actualJsonPath, jsonIndent);

		// assert
		assertThat(Files.readAllLines(expectedPlainText).size(), equalTo(expectedLineCount));
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualTextPath)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJsonPath)));
	}
}
