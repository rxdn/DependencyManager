package com.perkelle.dev.dependencymanager.dependency.impl.bintray;

import org.bukkit.plugin.Plugin;

public class JCenterDependency extends BintrayDependency {

    public JCenterDependency(Plugin owner, String group, String artifact, String version) {
        super(owner, "bintray", "jcenter", group, artifact, version);
    }
}
