package io.github.algomaster99.maven_module_graph.visitor;

import io.github.algomaster99.maven_module_graph.MavenModule;

public class PlainTextVisitor implements MavenModuleVisitor {
	private final StringBuilder sb = new StringBuilder();
	private final String indentString;

	public PlainTextVisitor(int indent) {
		this.indentString = " ".repeat(indent);
	}

	@Override
	public void visit(MavenModule module, int level) {
		sb.append(indentString.repeat(level));
		sb.append(String.format("%s:%s:%s", module.getGroupId(), module.getArtifactId(), module.getVersion()));
		sb.append("\n");
	}

	public String getResult() {
		return sb.toString();
	}
}
