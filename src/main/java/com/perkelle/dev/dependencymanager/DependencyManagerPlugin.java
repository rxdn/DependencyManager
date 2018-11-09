package com.perkelle.dev.dependencymanager;

import com.perkelle.dev.dependencymanager.dependency.impl.MavenCentralDependency;
import org.bukkit.plugin.java.JavaPlugin;

public class DependencyManagerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();

        System.out.println("loading toml4j");
        new MavenCentralDependency(this, "com.moandjiezana.toml", "toml4j", "0.7.2")
                .load(() -> {
                    System.out.println("\n\nn\n\n\n\\n\n\n\n\n\nLoaded toml4j!");
                    System.out.println(new com.moandjiezana.toml.Toml().read("\"a\"=5").getLong("a").toString());
                }, Throwable::printStackTrace);
    }
}
