package io.github.algomaster99.maven_module_graph;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class MavenModuleTest {
	@Test
	void legend_engine(@TempDir Path temp) throws XmlPullParserException, IOException {
		// arrange
		MavenModule module = MavenModule.createMavenModuleGraph(Path.of("src/test/resources/legend-engine/legend-engine"), null);
		Path expectedPlainText = Path.of("src/test/resources/legend-engine/output.txt");
		Path actualPlainText = temp.resolve("legend-engine.txt");

		Path expectedJson = Path.of("src/test/resources/legend-engine/output.json");
		Path actualJson = temp.resolve("legend-engine.json");

		// act
		Utility.printToFile(module, actualPlainText, 2);
		Utility.printToJson(module, actualJson, 0);

		// assert
		assertThat(Files.readString(expectedPlainText), equalTo(Files.readString(actualPlainText)));
		assertThat(Files.readString(expectedJson), equalTo(Files.readString(actualJson)));


	}
}
