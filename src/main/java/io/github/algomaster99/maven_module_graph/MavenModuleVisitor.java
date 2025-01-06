package io.github.algomaster99.maven_module_graph;

public interface MavenModuleVisitor {
	void visit(MavenModule module, int level);
}
