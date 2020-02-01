package com.perkelle.dev.dependencymanager.dependency.impl.bintray;

import com.perkelle.dev.dependencymanager.dependency.Dependency;
import org.bukkit.plugin.Plugin;

import java.net.MalformedURLException;
import java.net.URL;

public class BintrayDependency extends Dependency {

    private final String repositoryOwner, repositoryName;
    private final String group, artifact, version;

    public BintrayDependency(Plugin owner, String repositoryOwner, String repositoryName, String group, String artifact, String version) {
        super(owner);

        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;

        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    protected URL buildUrl() throws MalformedURLException {
        String groupSlashed = String.join("%2F", group.split("\\."));
        String jarName = String.format("%s-%s.jar", artifact, version);
        return new URL(String.format("https://bintray.com/%s/%s/download_file?file_path/%s%%2F%s%%2F%s%%2F%s", repositoryOwner, repositoryName, groupSlashed, artifact, version, jarName));
    }

    @Override
    protected String getLocalName() {
        return String.format("%s_%s_%s.jar", group, artifact, version);
    }
}
