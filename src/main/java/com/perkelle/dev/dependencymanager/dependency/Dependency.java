package com.perkelle.dev.dependencymanager.dependency;

import com.perkelle.dev.dependencymanager.DependencyManagerPlugin;
import com.perkelle.dev.dependencymanager.util.ContextUtils;
import com.perkelle.dev.dependencymanager.util.InjectorUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;

public abstract class Dependency {

    private Plugin owner;

    public Dependency(Plugin owner) {
        this.owner = owner;
    }

    protected abstract URL buildUrl() throws MalformedURLException;

    public void load(Runnable onComplete, Consumer<Exception> onError) {
        try {
            Plugin core = JavaPlugin.getPlugin(DependencyManagerPlugin.class);

            File cacheFolder = new File(core.getDataFolder(), "cache");
            if (!cacheFolder.exists())
                cacheFolder.mkdir();

            Consumer<File> inject = (f) -> ContextUtils.runAsync(owner, () -> {
                try {
                    InjectorUtils.INSTANCE.loadJar(f);
                    ContextUtils.runSync(owner, onComplete);
                } catch(Exception ex) {
                    ContextUtils.runSync(owner, () -> onError.accept(ex));
                }
            });


            File cached = new File(cacheFolder, getLocalName());
            if(!cached.exists()) {
                cached.createNewFile();
                download(owner, cached, inject, onError);
            }

            inject.accept(cached);
        } catch(Exception ex) {
            onError.accept(ex);
        }
    }

    protected abstract String getLocalName();

    protected void download(Plugin owner, File dest, Consumer<File> onComplete, Consumer<Exception> onError) {
        ContextUtils.runAsync(owner, () -> {
            try {
                URL url = buildUrl();

                InputStream stream = url.openStream();
                ReadableByteChannel byteChan = Channels.newChannel(stream);

                FileOutputStream fos = new FileOutputStream(dest);
                FileChannel fileChan = fos.getChannel();

                fileChan.transferFrom(byteChan, 0, Long.MAX_VALUE);

                fos.close();
                stream.close();

                ContextUtils.runSync(owner, () -> onComplete.accept(dest));
            } catch(IOException ex) {
                onError.accept(ex);
            }
        });
    }
}
