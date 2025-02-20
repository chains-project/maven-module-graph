package io.github.algomaster99.maven_module_graph;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
	private Utility() {
	}

	public static MavenModule createMavenModuleGraph(Path projectRoot, MavenModule parent, Map<Object, Object> properties, boolean excludeProfiles) throws IOException, XmlPullParserException {
		Path rootPom = projectRoot.resolve("pom.xml");
		Model rootModel = readPomModel(rootPom);

		properties.putAll(rootModel.getProperties());

		String rootGroupId = parseProperty(getGroupId(rootModel), properties);
		String rootArtifactId = rootModel.getArtifactId();
		String rootVersion = parseProperty(getVersion(rootModel), properties);

		MavenModule root = new MavenModule(rootModel, rootGroupId, rootArtifactId, rootVersion, properties, projectRoot.toAbsolutePath(), parent);
		traverseUpAndUpdateProperties(root);

		List<String> submodules = getAllModules(rootModel, excludeProfiles);

		for (String module : submodules) {
			Path modulePath = projectRoot.resolve(module);
			MavenModule mavenModule = createMavenModuleGraph(modulePath, root, properties, excludeProfiles);
			root.addSubmodule(mavenModule);
		}

		return root;
	}

	private static void traverseUpAndUpdateProperties(MavenModule root) {
		if (root.updateParentProperties(root.getProperties())) {
			// only these two properties can be inherited and hence need to be updated
			MavenModule parent = root.getParent();
			parent.groupId = parseProperty(parent.groupId, root.getProperties());
			parent.version = parseProperty(parent.version, root.getProperties());
			traverseUpAndUpdateProperties(root.getParent());
		}
	}

	private static Model readPomModel(Path pomPath) throws IOException, XmlPullParserException {
		MavenXpp3Reader reader = new MavenXpp3Reader();
		return reader.read(ReaderFactory.newXmlReader(pomPath.toFile()));
	}

	private static List<String> getAllModules(Model model, boolean exludeProfiles) {
		List<String> modules = new ArrayList<>(model.getModules());
		if (exludeProfiles) {
			return modules;
		}
		model.getProfiles().forEach(profile -> modules.addAll(profile.getModules()));
		return modules;
	}


	public static void printToFile(MavenModule root, Path plainText, int indent) {
		StringBuilder sb = new StringBuilder();
		Stack<Pair<Integer, MavenModule>> levelToModule = new Stack<>();
		levelToModule.add(Pair.of(0, root));

		String indentString = " ".repeat(indent);

		while (!levelToModule.isEmpty()) {
			Pair<Integer, MavenModule> current = levelToModule.pop();
			int level = current.first();
			sb.append(indentString.repeat(level));

			MavenModule currentModule = current.second();
			sb.append(String.format("%s:%s:%s", currentModule.getGroupId(), currentModule.getArtifactId(), currentModule.getVersion()));
			List<MavenModule> children = currentModule.getSubmodules();
			for (MavenModule child : children) {
				levelToModule.add(Pair.of(level + 1, child));
			}
			sb.append("\n");
		}

		// Write to file
		try {
			Files.writeString(plainText, sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printToJson(MavenModule root, Path jsonPath, int indent) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = convertToJson(mapper, root, 0);

		try {
			if (indent > 0) {
				DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
				DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
				printer.indentObjectsWith(indenter);
				printer.indentArraysWith(indenter);
				Files.writeString(jsonPath, mapper.writer(printer).writeValueAsString(rootNode));
			} else {
				Files.writeString(jsonPath, mapper.writeValueAsString(rootNode));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ObjectNode convertToJson(ObjectMapper mapper, MavenModule module, int depth) {
		ObjectNode jsonNode = mapper.createObjectNode();
		jsonNode.put("depth", depth);
		jsonNode.put("groupId", module.getGroupId());
		jsonNode.put("artifactId", module.getArtifactId());
		jsonNode.put("version", module.getVersion());

		// Add children with incremented depth
		List<MavenModule> submodules = module.getSubmodules();
		if (!submodules.isEmpty()) {
			ArrayNode childrenArray = jsonNode.putArray("submodules");
			for (MavenModule child : submodules) {
				childrenArray.add(convertToJson(mapper, child, depth + 1));
			}
		}

		return jsonNode;
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
			return properties.get(matcher.group(0)) != null ? (String) properties.get(matcher.group(0)) : value;
		}
		return value;
	}


}
