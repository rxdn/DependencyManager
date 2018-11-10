package com.perkelle.dev.dependencymanager.dependency.impl.nexus.sonatype;

import com.perkelle.dev.dependencymanager.dependency.impl.nexus.NexusDependency;
import org.bukkit.plugin.Plugin;

public class SonatypeOSSStagingDependency extends NexusDependency {

    public SonatypeOSSStagingDependency(Plugin owner, String group, String artifact, String version) {
        super(owner, "https://oss.sonatype.org/content/repositories/staging/", group, artifact, version);
    }
}
