package com.perkelle.dev.dependencymanager.dependency.impl.nexus;

import org.bukkit.plugin.Plugin;

public class MavenCentralDependency extends NexusDependency {

    public MavenCentralDependency(Plugin owner, String group, String artifact, String version) {
        super(owner, "https://repo1.maven.org/maven2", group, artifact, version);
    }
}
