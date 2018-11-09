package com.perkelle.dev.dependencymanager.dependency.impl;

import com.perkelle.dev.dependencymanager.DependencyManagerPlugin;
import com.perkelle.dev.dependencymanager.dependency.Dependency;
import com.perkelle.dev.dependencymanager.util.ContextUtils;
import com.perkelle.dev.dependencymanager.util.InjectorUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

public class MavenCentralDependency extends Dependency {

    private final Plugin owner;
    private final String group;
    private final String artifact;
    private final String version;

    public MavenCentralDependency(Plugin owner, String group, String artifact, String version) {
        this.owner = owner;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    public void load(Runnable onComplete, Consumer<Exception> onError) {
        try {
            Plugin core = JavaPlugin.getPlugin(DependencyManagerPlugin.class);

            File cacheFolder = new File(core.getDataFolder(), "cache");
            if (!cacheFolder.exists())
                cacheFolder.mkdir();

            String localName = String.format("%s:%s:%s.jar", group, artifact, version);

            Consumer<File> inject = (f) -> ContextUtils.runAsync(owner, () -> {
                try {
                    InjectorUtils.INSTANCE.loadJar(f);
                    ContextUtils.runSync(owner, onComplete);
                } catch(Exception ex) {
                    ContextUtils.runSync(owner, () -> onError.accept(ex));
                }
            });


            File cached = new File(cacheFolder, localName);
            if(!cached.exists()) {
                cached.createNewFile();
                download(owner, cached, inject, onError);
            }
        } catch(Exception ex) {
            onError.accept(ex);
        }
    }

    protected URL buildUrl() throws MalformedURLException {
        String groupSlashed = String.join("/", group.split("\\."));
        String jarName = String.format("%s-%s.jar", artifact, version);
        return new URL(String.format("http://central.maven.org/maven2/%s/%s/%s/%s", groupSlashed, artifact, version, jarName));
    }
}
