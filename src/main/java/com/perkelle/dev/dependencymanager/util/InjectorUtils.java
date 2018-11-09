package com.perkelle.dev.dependencymanager.util;

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

    private void ensureInitiated() throws NoSuchMethodException {
        classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);

        this.addUrlMethod = method;
    }

    public void loadJar(File src) throws MalformedURLException, IllegalAccessException, InvocationTargetException {
        addUrlMethod.invoke(classLoader, src.toURI().toURL());
    }
}
