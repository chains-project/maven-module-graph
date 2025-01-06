package io.github.algomaster99.maven_module_graph.visitor;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.algomaster99.maven_module_graph.MavenModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonVisitor implements MavenModuleVisitor {
	private final ObjectMapper mapper = new ObjectMapper();
	private final ObjectNode rootNode;
	private final int indent;

	public JsonVisitor(int indent) {
		this.rootNode = mapper.createObjectNode();
		this.indent = indent;
	}

	@Override
	public void visit(MavenModule module, int level) {
		ObjectNode currentNode = convertToJson(module);
		rootNode.set(module.getArtifactId(), currentNode);
	}

	private ObjectNode convertToJson(MavenModule module) {
		ObjectNode node = mapper.createObjectNode();
		node.put("groupId", module.getGroupId());
		node.put("artifactId", module.getArtifactId());
		node.put("version", module.getVersion());
		if (module.getSubmodules() != null && !module.getSubmodules().isEmpty()) {
			node.set("submodules", mapper.createArrayNode());
			for (MavenModule submodule : module.getSubmodules()) {
				((com.fasterxml.jackson.databind.node.ArrayNode) node.get("submodules"))
						.add(convertToJson(submodule));
			}
		}
		return node;
	}

	public void writeToFile(Path jsonPath) throws IOException {
		if (indent > 0) {
			DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
			DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
			printer.indentObjectsWith(indenter);
			printer.indentArraysWith(indenter);
			Files.writeString(jsonPath, mapper.writer(printer).writeValueAsString(rootNode));
		} else {
			Files.writeString(jsonPath, mapper.writeValueAsString(rootNode));
		}
	}
}
