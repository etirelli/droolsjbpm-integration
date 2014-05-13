package org.kie.server.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.server.impl.KSReleaseIdImpl;


public class ReleaseIdAdapter extends XmlAdapter<KSReleaseIdImpl, ReleaseId>{

    @Override
    public ReleaseId unmarshal(KSReleaseIdImpl v) throws Exception {
        return new ReleaseIdImpl(v.getGroupId(), v.getArtifactId(), v.getVersion());
    }

    @Override
    public KSReleaseIdImpl marshal(ReleaseId v) throws Exception {
        return new KSReleaseIdImpl(v.getGroupId(), v.getArtifactId(), v.getVersion());
    }

}
