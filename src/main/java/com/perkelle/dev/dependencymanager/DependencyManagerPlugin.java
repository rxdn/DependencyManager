package com.perkelle.dev.dependencymanager;

import org.bukkit.plugin.java.JavaPlugin;

public class DependencyManagerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if(!getDataFolder().exists())
            getDataFolder().mkdir();
    }
}
