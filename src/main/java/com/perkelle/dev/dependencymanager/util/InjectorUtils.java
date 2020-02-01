package com.perkelle.dev.dependencymanager.util;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public enum InjectorUtils {

    INSTANCE;

    private URLClassLoader classLoader;
    private Method addUrlMethod;

    private void ensureInitiated(Plugin pl) throws NoSuchMethodException {
        classLoader = (URLClassLoader) pl.getClass().getClassLoader();

        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);

        this.addUrlMethod = method;
    }

    public void loadJar(Plugin pl, File src) throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ensureInitiated(pl);
        addUrlMethod.invoke(classLoader, src.toURI().toURL());
    }
}
