package io.github.algomaster99.maven_module_graph;

public record Pair<F,S>(F first, S second) {
	public static <F, S> Pair<F, S> of(F first, S second) {
		return new Pair<>(first, second);
	}
}
