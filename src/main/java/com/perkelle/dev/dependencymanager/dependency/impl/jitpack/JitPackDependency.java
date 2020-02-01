package com.perkelle.dev.dependencymanager.dependency.impl.jitpack;

import com.perkelle.dev.dependencymanager.dependency.Dependency;
import org.bukkit.plugin.Plugin;

import java.net.MalformedURLException;
import java.net.URL;

public class JitPackDependency extends Dependency {

    private final GitHost gitHost;
    private final String user, repository, tag;

    public JitPackDependency(Plugin owner, GitHost gitHost, String user, String repository, String tag) {
        super(owner);

        this.gitHost = gitHost;

        this.user = user;
        this.repository = repository;
        this.tag = tag;
    }

    @Override
    protected URL buildUrl() throws MalformedURLException {
        String hostPrefixSlashed = String.join("/", gitHost.jitPackPrefix.split("\\."));
        return new URL(String.format("https://jitpack.io/%s/%s/%s/%s/%s-%s.jar", hostPrefixSlashed, user, repository, tag, repository, tag));
    }

    @Override
    protected String getLocalName() {
        return String.format("jitpack_%s_%s_%s.jar", user, repository, tag);
    }
}
