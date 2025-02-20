package io.github.algomaster99.maven_module_graph;

import org.apache.maven.model.Model;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenModule {
	private final MavenModule parent;

	private final Model actualModel;

	String groupId;

	private final String artifactId;

	String version;

	private final Path fileSystemPath;

	private final List<MavenModule> submodules = new ArrayList<>();

	private final Map<Object, Object> properties = new HashMap<>();

	 public MavenModule(Model actualModel, String groupId, String artifactId, String version, Map<Object, Object> properties, Path fileSystemPath, MavenModule parent) {
		this.actualModel = actualModel;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.properties.putAll(properties);
		this.fileSystemPath = fileSystemPath;
		this.parent = parent;
	}

	public boolean updateParentProperties(Map<Object, Object> properties) {
		if (parent != null) {
			Map<Object, Object> initialParentProperties = new HashMap<>(parent.getProperties());
			parent.properties.putAll(properties);
			return !initialParentProperties.equals(parent.getProperties());
		}
		return false;

	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public void addSubmodule(MavenModule child) {
		submodules.add(child);
	}

	public Model getActualModel() {
		return actualModel;
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
		return actualModel.equals(that.actualModel);
	}

	@Override
	public int hashCode() {
		return actualModel.hashCode();
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}
}
