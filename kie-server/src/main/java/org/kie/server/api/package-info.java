@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value=org.kie.server.impl.adapters.ReleaseIdAdapter.class,type=org.kie.api.builder.ReleaseId.class),
    @XmlJavaTypeAdapter(value=org.kie.server.impl.adapters.KieContainerInfoAdapter.class,type=org.kie.server.api.KieContainerInfo.class),
    @XmlJavaTypeAdapter(value=org.kie.server.impl.adapters.BatchExecutionCommandAdapter.class,type=org.kie.server.api.command.BatchExecutionCommand.class),
    @XmlJavaTypeAdapter(value=org.kie.server.impl.adapters.ExecutionResultsAdapter.class,type=org.kie.api.runtime.ExecutionResults.class)
})
package org.kie.server.api;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

