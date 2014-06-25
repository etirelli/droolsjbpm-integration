package org.kie.server.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kie.server.api.KieContainerInfo;
import org.kie.server.impl.KieContainerInfoImpl;


public class KieContainerInfoAdapter extends XmlAdapter<KieContainerInfoImpl, KieContainerInfo>{

    @Override
    public KieContainerInfo unmarshal(KieContainerInfoImpl v) throws Exception {
        return v;
    }

    @Override
    public KieContainerInfoImpl marshal(KieContainerInfo v) throws Exception {
        if( v instanceof KieContainerInfoImpl ) {
            return (KieContainerInfoImpl) v;
        }
        return new KieContainerInfoImpl(v.getContainerId(), v.getStatus());
    }

}
