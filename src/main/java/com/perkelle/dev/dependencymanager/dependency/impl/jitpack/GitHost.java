package com.perkelle.dev.dependencymanager.dependency.impl.jitpack;

public enum GitHost {
    GITHUB("com.github"),
    BITBUCKET("com.bitbucket"),
    GITLAB("com.gitlab"),
    GITEE("com.gitee")
    ;

    public final String jitPackPrefix;

    GitHost(String jitPackPrefix) {
        this.jitPackPrefix = jitPackPrefix;
    }
}
