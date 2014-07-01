@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value=org.kie.server.services.impl.adapters.ReleaseIdAdapter.class,type=org.kie.api.builder.ReleaseId.class),
    @XmlJavaTypeAdapter(value=org.kie.server.services.impl.adapters.KieContainerInfoAdapter.class,type=org.kie.server.api.entity.KieContainerInfo.class),
    @XmlJavaTypeAdapter(value=org.kie.server.services.impl.adapters.ExecutionResultsAdapter.class,type=org.kie.api.runtime.ExecutionResults.class)
})
package org.kie.server.services.api;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

