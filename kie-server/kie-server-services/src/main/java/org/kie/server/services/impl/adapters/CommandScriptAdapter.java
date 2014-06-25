package org.kie.server.services.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kie.server.services.api.command.CommandScript;
import org.kie.server.services.api.command.impl.CommandScriptImpl;


public class CommandScriptAdapter extends XmlAdapter<CommandScriptImpl, CommandScript>{

    @Override
    public CommandScript unmarshal(CommandScriptImpl v) throws Exception {
        return v;
    }

    @Override
    public CommandScriptImpl marshal(CommandScript v) throws Exception {
        return (CommandScriptImpl)v;
    }

}
