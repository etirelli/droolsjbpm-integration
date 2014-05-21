package org.kie.server.impl;

import org.kie.api.builder.ReleaseId;

/**
 * This is a JAXB friendly ReleaseId implementation
 * used for JAXB marshalling/unmarshalling only 
 */
public class KSReleaseIdImpl implements ReleaseId {

    private String groupId;
    private String artifactId;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

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
