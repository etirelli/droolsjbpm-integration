package org.kie.server.services.api.command;

import javax.xml.bind.annotation.XmlRootElement;

import org.kie.api.command.Command;

@XmlRootElement
public interface KieServerCommand extends Command<ServiceResponse> {

    ServiceResponse execute(KieServerCommandContext context);

}
