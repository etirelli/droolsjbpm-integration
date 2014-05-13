package org.kie.server.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.builder.ReleaseId;


public class ReleaseIdAdapter extends XmlAdapter<ReleaseIdImpl, ReleaseId>{

    @Override
    public ReleaseId unmarshal(ReleaseIdImpl v) throws Exception {
        return v;
    }

    @Override
    public ReleaseIdImpl marshal(ReleaseId v) throws Exception {
        if( v instanceof ReleaseIdImpl ) {
            return (ReleaseIdImpl) v;
        }
        return new ReleaseIdImpl(v.getGroupId(), v.getArtifactId(), v.getVersion());
    }

}
