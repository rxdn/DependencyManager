package com.perkelle.dev.dependencymanager.dependency;

import com.perkelle.dev.dependencymanager.DependencyManagerPlugin;
import com.perkelle.dev.dependencymanager.util.ContextUtils;
import com.perkelle.dev.dependencymanager.util.InjectorUtils;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class Dependency {

    private Plugin owner;
    private Relocation[] rules = new Relocation[]{};

    public Dependency(Plugin owner) {
        this.owner = owner;
    }

    public Dependency setRelocation(Relocation... rules) {
        this.rules = rules;
        return this;
    }

    protected abstract URL buildUrl() throws IOException, ParserConfigurationException, SAXException;

    public void load(Runnable onComplete, Consumer<Exception> onError) {
        try {
            File cacheFolder = new File(owner.getDataFolder(), "cache");
            if (!cacheFolder.exists())
                cacheFolder.mkdir();

            Consumer<File> inject = (f) -> ContextUtils.runAsync(owner, () -> {
                try {
                    InjectorUtils.INSTANCE.loadJar(owner, f);
                    ContextUtils.runSync(owner, onComplete);
                } catch(Exception ex) {
                    ContextUtils.runSync(owner, () -> onError.accept(ex));
                }
            });

            File cached = new File(cacheFolder, getLocalName());
            if(cached.length() == 0) {
                cached.delete();
            }

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

    private void download(Plugin owner, File dest, Consumer<File> onComplete, Consumer<Exception> onError) {
        ContextUtils.runAsync(owner, () -> {
            try {
                // For relocations, we must download the jar, relocate deps and then move it to the dest
                File downloadDest;
                if(rules.length > 0) {
                    File temp = new File(new File(owner.getDataFolder(), "cache"), "temp");
                    if(!temp.exists()) {
                        temp.mkdir();
                    }
                    downloadDest = new File(temp, dest.getName());
                } else {
                    downloadDest = dest;
                }

                URL url = buildUrl();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36 OPR/66.0.3515.44");

                InputStream stream = conn.getInputStream();
                ReadableByteChannel byteChan = Channels.newChannel(stream);

                FileOutputStream fos = new FileOutputStream(downloadDest);
                FileChannel fileChan = fos.getChannel();

                fileChan.transferFrom(byteChan, 0, Long.MAX_VALUE);

                fos.close();
                stream.close();

                // Relocate
                if(rules.length > 0) {
                    JarRelocator relocator = new JarRelocator(downloadDest, dest, Arrays.asList(rules));
                    relocator.run();
                    downloadDest.delete();
                }

                ContextUtils.runSync(owner, () -> onComplete.accept(dest));
            } catch(IOException | ParserConfigurationException | SAXException ex) {
                onError.accept(ex);
            }
        });
    }
}
