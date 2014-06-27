package org.kie.server.services.impl;

import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlElement;

import org.kie.api.builder.ReleaseId;

/**
 * This is a JAXB friendly ReleaseId implementation
 * used for JAXB marshalling/unmarshalling only 
 */
public class KSReleaseIdImpl implements ReleaseId {

    @QueryParam("group-id")
    private String groupId;
    @QueryParam("artifact-id")
    private String artifactId;
    @QueryParam("version")
    private String version;

    public KSReleaseIdImpl() {
        super();
    }

    public KSReleaseIdImpl(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @XmlElement(required = true, name = "group-id")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @XmlElement(required = true, name = "artifact-id")
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @XmlElement(required = true, name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isSnapshot() {
        return version.endsWith("-SNAPSHOT");
    }

    @Override
    public String toExternalForm() {
        return groupId + ":" + artifactId + ":" + version;
    }

}
