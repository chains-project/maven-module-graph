package io.github.algomaster99.maven_module_graph.visitor;

import io.github.algomaster99.maven_module_graph.MavenModule;
import io.github.algomaster99.maven_module_graph.Pair;

import java.util.List;
import java.util.Stack;

public class MavenModuleProcessor {
	public static void process(MavenModule root, MavenModuleVisitor visitor) {
		Stack<Pair<Integer, MavenModule>> stack = new Stack<>();
		stack.push(new Pair<>(0, root));

		while (!stack.isEmpty()) {
			Pair<Integer, MavenModule> current = stack.pop();
			int level = current.first();
			MavenModule module = current.second();

			visitor.visit(module, level);

			List<MavenModule> submodules = module.getSubmodules();
			for (int i = submodules.size() - 1; i >= 0; i--) {
				stack.push(new Pair<>(level + 1, submodules.get(i)));
			}
		}
	}
}
