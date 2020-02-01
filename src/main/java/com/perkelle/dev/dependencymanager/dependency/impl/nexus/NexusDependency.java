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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

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
        Scanner scanner = new Scanner(metadataUrl.openStream());
        String metadata = scanner.useDelimiter("\\A").next();
        scanner.close(); // Make sure we close the stream

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(metadata);

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

        if(buildName == null) {
            throw new IOException("Build not found");
        }

        String jarName = String.format("%s-%s.jar", artifact, buildName);
        return new URL(String.format("%s/%s/%s/%s/%s", rootUrl, groupSlashed, artifact, version, jarName));
    }

    @Override
    protected String getLocalName() {
        return String.format("%s_%s_%s.jar", group, artifact, version);
    }
}
