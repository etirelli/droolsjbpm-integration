package org.kie.server.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kie.server.api.command.BatchExecutionCommand;
import org.kie.server.api.command.impl.BatchExecutionCommandImpl;


public class BatchExecutionCommandAdapter extends XmlAdapter<BatchExecutionCommandImpl, BatchExecutionCommand>{

    @Override
    public BatchExecutionCommand unmarshal(BatchExecutionCommandImpl v) throws Exception {
        return v;
    }

    @Override
    public BatchExecutionCommandImpl marshal(BatchExecutionCommand v) throws Exception {
        return (BatchExecutionCommandImpl)v;
    }

}
