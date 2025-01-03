package io.github.algomaster99.maven_module_graph;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

public class Utility {
	private Utility() {
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
			sb.append(String.format("%s:%s:%s", getGroupId(currentModule), currentModule.getSelf().getArtifactId(), getVersion(currentModule)));
			List<MavenModule> children = currentModule.getSubmodules();
			for (MavenModule child : children) {
				levelToModule.add(Pair.of(level + 1, child));
			}
			sb.append("\n");
		}

		// Write to file
		try {
			Files.writeString(plainText, sb.toString().trim());
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
		jsonNode.put("groupId", getGroupId(module));
		jsonNode.put("artifactId", module.getSelf().getArtifactId());
		jsonNode.put("version", getVersion(module));

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

	private static String getGroupId(MavenModule module) {
		String groupId = module.getSelf().getGroupId();
		if (groupId == null) {
			if (module.getSelf().getParent() != null) {
				return module.getSelf().getParent().getGroupId();
			}
			if (module.getSelf().getParent() != null) {
				return getGroupId(module.getParent());
			}
		}
		return groupId;
	}

	private static String getVersion(MavenModule module) {
		String version = module.getSelf().getVersion();
		if (version == null) {
			if (module.getSelf().getParent() != null) {
				return module.getSelf().getParent().getVersion();
			}
			if (module.getSelf().getParent() != null) {
				return getVersion(module.getParent());
			}
		}
		return version;
	}


}
