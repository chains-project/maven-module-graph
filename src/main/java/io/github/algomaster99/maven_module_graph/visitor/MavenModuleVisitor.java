package io.github.algomaster99.maven_module_graph.visitor;

import io.github.algomaster99.maven_module_graph.MavenModule;

public interface MavenModuleVisitor {
	void visit(MavenModule module, int level);
}
