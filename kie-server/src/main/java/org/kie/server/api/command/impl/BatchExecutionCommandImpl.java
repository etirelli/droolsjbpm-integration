/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.command.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.kie.server.api.command.BatchExecutionCommand;
import org.kie.server.api.command.KieServerCommand;
import org.kie.server.api.command.KieServerCommandContext;
import org.kie.server.api.command.ServiceResponse;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XmlRootElement(name = "batch-execution")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "batch-execution", propOrder = {"lookup", "commands"})
public class BatchExecutionCommandImpl implements BatchExecutionCommand {

    private static final long serialVersionUID = 510l;

    @XmlAttribute
    @XStreamAsAttribute
    private String            lookup;

    @XmlElements({
        @XmlElement(name = "deploy-module", type = DeployModuleCommand.class),
        @XmlElement(name = "list-containers", type = ListContainersCommand.class)
    })
    protected List<KieServerCommand> commands;

    public BatchExecutionCommandImpl() {
    }

    public BatchExecutionCommandImpl(List<KieServerCommand> commands) {
        this.commands = commands;
    }

    public BatchExecutionCommandImpl(List<KieServerCommand> commands, String lookup) {
        this.commands = commands;
        this.lookup = lookup;
    }

    public List<KieServerCommand> getCommands() {
        if (commands == null) {
            commands = new ArrayList<KieServerCommand>();
        }
        return this.commands;
    }

    public List<ServiceResponse> execute(KieServerCommandContext context) {
        List<ServiceResponse> response = new ArrayList<ServiceResponse>();
        for (KieServerCommand command : commands) {
            response.add(command.execute(context));
        }
        return response;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    public String getLookup() {
        return lookup;
    }

    public String toString() {
        return "BatchExecutionCommandImpl{" +
                "lookup='" + lookup + '\'' +
                ", commands=" + commands +
                '}';
    }
}
