package io.github.algomaster99.maven_module_graph;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import static io.github.algomaster99.maven_module_graph.Utility.printToFile;
import static io.github.algomaster99.maven_module_graph.Utility.printToJson;

@CommandLine.Command(
		name = "module-graph",
		mixinStandardHelpOptions = true
)
public class ModuleGraph implements Callable<Integer> {

	@CommandLine.Spec private CommandLine.Model.CommandSpec sp;

	@CommandLine.Option(
			names = {"-p", "--project-root"},
			required = true,
			description = "Path to maven project root."
	)
	private Path project;

	@CommandLine.ArgGroup(exclusive = false)
	private PlainText plainText;

	private static class PlainText {
		@CommandLine.Option(
				names = "--plain-text",
				description = "Get maven module graph in a text file.",
				required = true
		)
		private Path plainText;

		@CommandLine.Option(
				names = "--plain-text-indent",
				description = "Indentation for the text file."
		)
		private int indent = 2;
	}

	@CommandLine.ArgGroup(exclusive = false)
	private Json json;

	private static class Json {
		@CommandLine.Option(
				names = "--json",
				description = "Get maven module graph in a json file.",
				required = true
		)
		private Path json;

		@CommandLine.Option(
				names = "--json-indent",
				description = "Indentation for the json file."
		)
		private int indent = 2;
	}

	@Override
	public Integer call() throws XmlPullParserException, IOException {
		validate();
		MavenModule moduleGraphRoot = MavenModule.createMavenModuleGraph(project, null);
		if (plainText != null) {
			printToFile(moduleGraphRoot, plainText.plainText, plainText.indent);
		}
		if (json != null) {
			printToJson(moduleGraphRoot, json.json, json.indent);
		}
		return 0;
	}

	private void validate() {
		Path pomFile = project.resolve("pom.xml");
		if (!pomFile.toFile().exists()) {
			throw new CommandLine.ParameterException(sp.commandLine(), "Invalid project root. pom.xml not found.");
		}
		if (plainText == null && json == null) {
			throw new CommandLine.ParameterException(sp.commandLine(), "Please specify either --plain-text or --json.");
		}
	}


	public static void main(String[] args) {
		new CommandLine(new ModuleGraph()).execute(args);
	}

}
