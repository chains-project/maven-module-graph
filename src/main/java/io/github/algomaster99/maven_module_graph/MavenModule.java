package io.github.algomaster99.maven_module_graph;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MavenModule {
	private final MavenModule parent;

	private final Model self;

	private final Path fileSystemPath;

	private final List<MavenModule> submodules = new ArrayList<>();

	private static final Map<Object, Object> properties = new HashMap<>();

	private MavenModule(Model self, Path fileSystemPath, MavenModule parent) {
		this.self = self;
		this.fileSystemPath = fileSystemPath;
		this.parent = parent;
	}

	public static Map<Object, Object> getProperties() {
		return properties;
	}

	public void addSubmodule(MavenModule child) {
		submodules.add(child);
	}

	public Model getSelf() {
		return self;
	}

	public List<MavenModule> getSubmodules() {
		return submodules;
	}

	public Path getFileSystemPath() {
		return fileSystemPath;
	}

	public MavenModule getParent() {
		return parent;
	}

	public MavenModule findSubmodule(String artifactIdOfModule) {
		Queue<MavenModule> queue = new ArrayDeque<>();
		queue.add(topLevelParent());
		while (!queue.isEmpty()) {
			MavenModule module = queue.poll();
			if (module.getSelf().getArtifactId().equals(artifactIdOfModule)) {
				return module;
			}
			queue.addAll(module.getSubmodules());
		}
		return null;
	}

	public MavenModule topLevelParent() {
		if (parent == null) {
			return this;
		}
		return parent.topLevelParent();
	}

	/**
	 * This is delegated to {@link Model#equals(Object)}.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MavenModule that = (MavenModule) obj;
		return self.equals(that.self);
	}

	@Override
	public int hashCode() {
		return self.hashCode();
	}

	public static MavenModule createMavenModuleGraph(Path projectRoot, MavenModule parent) throws IOException, XmlPullParserException {
		Path rootPom = projectRoot.resolve("pom.xml");
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model rootModel = reader.read(new FileReader(rootPom.toFile()));

		properties.putAll(rootModel.getProperties());

		MavenModule root = new MavenModule(rootModel, projectRoot.toAbsolutePath(), parent);

		List<String> submodules = rootModel.getModules();

		rootModel.getProfiles().forEach(profile -> {
			submodules.addAll(profile.getModules());
		});

		for (String module : submodules) {
			Path modulePath = projectRoot.resolve(module);
			MavenXpp3Reader moduleReader = new MavenXpp3Reader();
			Model moduleModel = moduleReader.read(
					new FileReader(modulePath.resolve("pom.xml").toFile()));
			MavenModule mavenModule = new MavenModule(moduleModel, modulePath, root);
			if (moduleModel.getModules() != null) {
				List<String> childModules = moduleModel.getModules();
				moduleModel.getProfiles().forEach(profile -> {
					childModules.addAll(profile.getModules());
				});
				List<MavenModule> children = childModules.stream()
						.map(childModule -> {
							try {
								return createMavenModuleGraph(modulePath.resolve(childModule), mavenModule);
							} catch (IOException | XmlPullParserException e) {
								throw new RuntimeException(e);
							}
						})
						.toList();
				children.forEach(mavenModule::addSubmodule);
			}
			root.addSubmodule(mavenModule);
		}
		return root;
	}
}
