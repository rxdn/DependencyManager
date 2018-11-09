package com.perkelle.dev.dependencymanager.dependency;

import com.perkelle.dev.dependencymanager.util.ContextUtils;
import org.bukkit.plugin.Plugin;

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

    protected abstract URL buildUrl() throws MalformedURLException;

    public abstract void load(Runnable onComplete, Consumer<Exception> onError);

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
