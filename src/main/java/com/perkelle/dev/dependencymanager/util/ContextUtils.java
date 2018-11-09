package com.perkelle.dev.dependencymanager.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ContextUtils {

    public static void runSync(Plugin pl, Runnable block) {
        Bukkit.getScheduler().runTask(pl, block);
    }

    public static void runAsync(Plugin pl, Runnable block) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, block);
    }
}
