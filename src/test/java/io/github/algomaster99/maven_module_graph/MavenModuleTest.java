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
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of("src/test/resources/legend-engine/legend-engine"), null, new HashMap<>());
		Path expectedPlainText = Path.of("src/test/resources/legend-engine/output.txt");
		Path actualPlainText = temp.resolve("legend-engine.txt");

		Path expectedJson = Path.of("src/test/resources/legend-engine/output.json");
		Path actualJson = temp.resolve("legend-engine.json");

		// act
		Utility.printToFile(module, actualPlainText, 2);
		Utility.printToJson(module, actualJson, 2);

		// assert
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualPlainText)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJson)));
	}

	@Test
	void neo4j_profileModules(@TempDir Path temp) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of("src/test/resources/neo4j/neo4j"), null, new HashMap<>());
		Path expectedPlainText = Path.of("src/test/resources/neo4j/output.txt");
		Path actualPlainText = temp.resolve("neo4j.txt");

		Path expectedJson = Path.of("src/test/resources/neo4j/output.json");
		Path actualJson = temp.resolve("neo4j.json");

		// act
		Utility.printToFile(module, actualPlainText, 2);
		Utility.printToJson(module, actualJson, 0);

		// assert
		assertThat(Files.readAllLines(expectedPlainText).size(), equalTo(125));
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualPlainText)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJson)));
	}

	@Test
	void arthas_mavenProperties(@TempDir Path temp) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of("src/test/resources/arthas/arthas"), null, new HashMap<>());
		Path expectedPlainText = Path.of("src/test/resources/arthas/output.txt");
		Path actualPlainText = temp.resolve("arthas.txt");

		Path expectedJson = Path.of("src/test/resources/arthas/output.json");
		Path actualJson = temp.resolve("arthas.json");

		// act
		Utility.printToFile(module, actualPlainText, 2);
		Utility.printToJson(module, actualJson, 0);

		// assert
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualPlainText)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJson)));
	}

	@Test
	void persistence_differentProjectRoot(@TempDir Path temp) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = Utility.createMavenModuleGraph(Path.of("src/test/resources/persistence/persistence/api"), null, new HashMap<>());
		Path expectedPlainText = Path.of("src/test/resources/arthas/output.txt");
		Path actualPlainText = temp.resolve("arthas.txt");

		Path expectedJson = Path.of("src/test/resources/arthas/output.json");
		Path actualJson = temp.resolve("arthas.json");

		// act
		Utility.printToFile(module, actualPlainText, 0);
		Utility.printToJson(module, actualJson, 2);

		// assert
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualPlainText)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJson)));
	}

}
