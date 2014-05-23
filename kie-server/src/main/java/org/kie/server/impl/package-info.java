@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value=org.kie.server.impl.adapters.ReleaseIdAdapter.class,type=org.kie.api.builder.ReleaseId.class),
    @XmlJavaTypeAdapter(value=org.kie.server.impl.adapters.KieContainerInfoAdapter.class,type=org.kie.server.api.KieContainerInfo.class),
})
package org.kie.server.impl;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

