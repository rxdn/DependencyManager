package com.perkelle.dev.dependencymanager.dependency.impl.nexus.sonatype;

import com.perkelle.dev.dependencymanager.dependency.impl.nexus.NexusDependency;
import org.bukkit.plugin.Plugin;

public class SonatypeOSSSnapshotDependency extends NexusDependency {

    public SonatypeOSSSnapshotDependency(Plugin owner, String group, String artifact, String version) {
        super(owner, "https://oss.sonatype.org/content/repositories/snapshots/", group, artifact, version);
    }
}
