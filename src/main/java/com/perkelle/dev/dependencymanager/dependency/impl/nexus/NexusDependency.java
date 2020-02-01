package com.perkelle.dev.dependencymanager.dependency.impl.nexus;

import com.perkelle.dev.dependencymanager.dependency.Dependency;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class NexusDependency extends Dependency {

    private final String rootUrl;
    private final String group, artifact, version;

    public NexusDependency(Plugin owner, String rootUrl, String group, String artifact, String version) {
        super(owner);

        if(rootUrl.endsWith("/")) {
            this.rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        } else {
            this.rootUrl = rootUrl;
        }

        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    protected URL buildUrl() throws IOException, ParserConfigurationException, SAXException {
        String groupSlashed = String.join("/", group.split("\\."));

        // Retrieve metadata
        URL metadataUrl = new URL(String.format("%s/%s/%s/%s/maven-metadata.xml", rootUrl, groupSlashed, artifact, version));
        HttpURLConnection conn = (HttpURLConnection) metadataUrl.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36 OPR/66.0.3515.44");

        int responseCode = conn.getResponseCode();
        if(responseCode == 403 || responseCode == 404) { // Metadata doesn't exist, pull jar straight away
            conn.disconnect();
            String jarName = String.format("%s-%s.jar", artifact, version);
            return new URL(String.format("%s/%s/%s/%s/%s", rootUrl, groupSlashed, artifact, version, jarName));
        }
        else if(responseCode == 200) {
            String metadata;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                metadata = reader.lines().collect(Collectors.joining("\n"));
            }
            conn.disconnect();

            String buildName = getBuildName(metadata);
            if(buildName == null) {
                throw new IOException("Build not found");
            }

            String jarName = String.format("%s-%s.jar", artifact, buildName);
            return new URL(String.format("%s/%s/%s/%s/%s", rootUrl, groupSlashed, artifact, version, jarName));
        }
        else {
            conn.disconnect();
            throw new IOException(String.format("Error retrieving %s:%s:%s - Error code %d", group, artifact, version, responseCode));
        }
    }

    protected String getBuildName(String metadata) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(metadata.getBytes()));

        // Parse metadata
        Element versioning = (Element) doc.getElementsByTagName("versioning").item(0);
        Element snapshotVersionsElement = (Element) versioning.getElementsByTagName("snapshotVersions").item(0);
        NodeList snapshotVersions = snapshotVersionsElement.getElementsByTagName("snapshotVersion");

        String buildName = null;
        for(int i = 0; i < snapshotVersions.getLength(); i++) {
            Element snapshotVersion = (Element) snapshotVersions.item(i);

            // Verify that the snapshot version is a jar
            String extension = snapshotVersion.getElementsByTagName("extension").item(0).getTextContent();
            if(!extension.equals("jar")) {
                continue;
            }

            buildName = snapshotVersion.getElementsByTagName("value").item(0).getTextContent();
        }

        return buildName;
    }

    @Override
    protected String getLocalName() {
        return String.format("%s_%s_%s.jar", group, artifact, version);
    }
}
