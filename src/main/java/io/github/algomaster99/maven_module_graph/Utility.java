package io.github.algomaster99.maven_module_graph;

import io.github.algomaster99.maven_module_graph.visitor.JsonVisitor;
import io.github.algomaster99.maven_module_graph.visitor.MavenModuleProcessor;
import io.github.algomaster99.maven_module_graph.visitor.PlainTextVisitor;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
	private Utility() {
	}

	public static MavenModule createMavenModuleGraph(Path projectRoot, MavenModule parent, Map<Object, Object> properties) throws IOException, XmlPullParserException {
		Path rootPom = projectRoot.resolve("pom.xml");
		Model rootModel = readPomModel(rootPom);

		properties.putAll(rootModel.getProperties());

		String rootGroupId = parseProperty(getGroupId(rootModel), properties);
		String rootArtifactId = rootModel.getArtifactId();
		String rootVersion = parseProperty(getVersion(rootModel), properties);

		MavenModule root = new MavenModule(rootModel, rootGroupId, rootArtifactId, rootVersion, properties, projectRoot.toAbsolutePath(), parent);

		List<String> submodules = getAllModules(rootModel);

		for (String module : submodules) {
			Path modulePath = projectRoot.resolve(module);
			MavenModule mavenModule = createMavenModuleGraph(modulePath, root, properties);
			root.addSubmodule(mavenModule);
		}

		return root;
	}

	private static Model readPomModel(Path pomPath) throws IOException, XmlPullParserException {
		MavenXpp3Reader reader = new MavenXpp3Reader();
		return reader.read(new FileReader(pomPath.toFile()));
	}

	private static List<String> getAllModules(Model model) {
		List<String> modules = new ArrayList<>(model.getModules());
		model.getProfiles().forEach(profile -> modules.addAll(profile.getModules()));
		return modules;
	}

	public static void printToFile(MavenModule root, Path plainText, int indent) {
		PlainTextVisitor plainTextVisitor = new PlainTextVisitor(indent);
		MavenModuleProcessor.process(root, plainTextVisitor);

		try {
			Files.writeString(plainText, plainTextVisitor.getResult());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printToJson(MavenModule root, Path jsonPath, int indent) {
		JsonVisitor jsonVisitor = new JsonVisitor(indent);
		MavenModuleProcessor.process(root, jsonVisitor);
		try {
			jsonVisitor.writeToFile(jsonPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getGroupId(Model module) {
		String groupId = module.getGroupId();
		if (groupId == null) {
			if (module.getParent() != null) {
				return module.getParent().getGroupId();
			}
		}
		return groupId;
	}

	private static String getVersion(Model module) {
		String version = module.getVersion();
		if (version == null) {
			if (module.getParent() != null) {
				return module.getParent().getVersion();
			}
		}
		return version;
	}

	private static String parseProperty(String value, Map<Object, Object> properties) {
		final String regex = "(?<=\\$\\{)[\\w\\d\\.]+(?=\\})";

		final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(value);

		while (matcher.find()) {
			return (String) properties.get(matcher.group(0));
		}
		return value;
	}


}
