package org.kie.remote.services.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessIdsCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.util.StringUtils;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentNotFoundException;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.api.RuntimeResource;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.util.FormURLGenerator;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceFormResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItemResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

/**
 * If a method in this class is annotated by a @Path annotation, 
 * then the name of the method should match the URL specified in the @Path, 
 * where "_" characters should be used for all "/" characters in the path. 
 * <p>
 * For example: 
 * <pre>
 * @Path("/begin/{varOne: [_a-zA-Z0-9-:\\.]+}/midddle/{varTwo: [a-z]+}")
 * public void begin_varOne_middle_varTwo() { 
 * </pre>
 * 
 * If the method is annotated by the @Path anno, but is the "root", then
 * give it a name that explains it's function.
 */
@Path("/runtime/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@RequestScoped
public class RuntimeResourceImpl extends ResourceBase implements RuntimeResource {

    /* REST information */
    
    @Context
    protected HttpHeaders headers;
    
    @PathParam("deploymentId")
    protected String deploymentId;
    
    /* KIE information and processing */
    
    @Inject
    private RuntimeDataService runtimeDataService;
   
    @Inject
    private DefinitionService bpmn2DataService;

    @Inject
    private FormURLGenerator formURLGenerator;


    // Rest methods --------------------------------------------------------------------------------------------------------------

    /**
     * The "/execute" method, primarily for the classes in the kie-services-client jar. 
     * </p>
     * A pain to support.. 
     * 
     * @param cmdsRequest The {@link JaxbCommandsRequest} containing the {@link Command} and other necessary info.
     * @return A {@link JaxbCommandsResponse} with the result from the {@link Command}
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return restProcessJaxbCommandsRequest(cmdsRequest);
    } 
 
    @Override
    public Response getProcessDefinitionInfo(String processId) {
        ProcessDefinition processAssetDescList = runtimeDataService.getProcessesByDeploymentIdProcessId(deploymentId, processId);
        JaxbProcessDefinition jaxbProcDef = convertProcAssetDescToJaxbProcDef(processAssetDescList);
        Map<String, String> variables = bpmn2DataService.getProcessVariables(deploymentId, processId);
        jaxbProcDef.setVariables(variables);
        return createCorrectVariant(jaxbProcDef, headers);
    }
    
    @Override
    public Response startProcessInstance(String processId) {
        Map<String, String[]> requestParams = getRequestParams();
        String oper = getRelativePath();
        Map<String, Object> params = extractMapFromParams(requestParams, oper);

        ProcessInstance result = startProcessInstance(processId, params);

        JaxbProcessInstanceResponse responseObj = new JaxbProcessInstanceResponse(result, getRequestUri());
        return createCorrectVariant(responseObj, headers);
    }

    @Override
    public Response getProcessInstanceStartForm(String processId) {
        List<String> result = (List<String>) processRequestBean.doKieSessionOperation(new GetProcessIdsCommand(), deploymentId, null);

        if (result != null && result.contains(processId)) {
            String opener = "";

            List<String> openers = headers.getRequestHeader("host");
            if (openers.size() == 1) {
                opener = openers.get(0);
            }

            String formUrl = formURLGenerator.generateFormProcessURL(getBaseUri(), processId, deploymentId, opener);
            if (!StringUtils.isEmpty(formUrl)) {
                JaxbProcessInstanceFormResponse response = new JaxbProcessInstanceFormResponse(formUrl, getRequestUri());
                return createCorrectVariant(response, headers);
            }
        }
        throw KieRemoteRestOperationException.notFound("Process " + processId + " is not available.");
    }

    @Override
    public Response getProcessInstance(Long procInstId) {
        ProcessInstance procInst = getProcessInstance(procInstId, true);
        JaxbProcessInstanceResponse response = new JaxbProcessInstanceResponse(procInst); 
        if( procInst == null ) { 
            response.setStatus(JaxbRequestStatus.NOT_FOUND);
        }
        return createCorrectVariant(response, headers);
    }

    @Override
    public Response abortProcessInstance(Long procInstId) {
        Command<?> cmd = new AbortProcessInstanceCommand();
        ((AbortProcessInstanceCommand) cmd).setProcessInstanceId(procInstId);
       
        try { 
            processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                procInstId);
        } catch( IllegalArgumentException iae ) { 
            if( iae.getMessage().startsWith("Could not find process instance") ) {
                throw KieRemoteRestOperationException.notFound("Process instance " + procInstId + " is not available.");
            }
            throw iae;
        }
                
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }

    @Override
    public Response signalProcessInstance(Long procInstId) {
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);
        Command<?> cmd = new SignalEventCommand(procInstId, eventType, event);
        
        processRequestBean.doKieSessionOperation(cmd, deploymentId, procInstId);
        
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);

    }

    @Override
    public Response getProcessInstanceVariableByProcInstIdByVarName(Long procInstId, String varName) {
        Object procVar;
        try {
            procVar =  processRequestBean.getVariableObjectInstanceFromRuntime(deploymentId, procInstId, varName);
        } catch( ProcessInstanceNotFoundException pinfe ) { 
            throw KieRemoteRestOperationException.notFound(pinfe.getMessage(), pinfe);
        } catch( DeploymentNotFoundException dnfe ) { 
            throw new org.kie.remote.services.exception.DeploymentNotFoundException(dnfe.getMessage());
        }
     
        // return
        return createCorrectVariant(procVar, headers);
    }
  
    @Override
    public Response signalProcessInstances() {
        String oper = getRelativePath();
        Map<String, String[]> requestParams = getRequestParams();
        String eventType = getStringParam("signal", true, requestParams, oper);
        Object event = getObjectParam("event", false, requestParams, oper);

        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(eventType, event),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, requestParams, oper, true));
        
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }

    @Override
    public Response getWorkItem(Long workItemId) { 
        String oper = getRelativePath();
        WorkItem workItem = (WorkItem) processRequestBean.doKieSessionOperation(
                new GetWorkItemCommand(workItemId),
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, getRequestParams(), oper, true));
               
        if( workItem == null ) { 
            throw KieRemoteRestOperationException.notFound("WorkItem " + workItemId + " does not exist.");
        }
        
        return createCorrectVariant(new JaxbWorkItemResponse(workItem), headers);
    }
    
    @Override
    public Response doWorkItemOperation(Long workItemId, String operation) {
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        Command<?> cmd = null;
        if ("complete".equalsIgnoreCase((operation.trim()))) {
            Map<String, Object> results = extractMapFromParams(params, operation);
            cmd = new CompleteWorkItemCommand(workItemId, results);
        } else if ("abort".equalsIgnoreCase(operation.toLowerCase())) {
            cmd = new AbortWorkItemCommand(workItemId);
        } else {
            throw KieRemoteRestOperationException.badRequest("Unsupported operation: " + oper);
        }
      
        // Will NOT throw an exception if the work item does not exist!!
        processRequestBean.doKieSessionOperation(
                cmd, 
                deploymentId, 
                (Long) getNumberParam(PROC_INST_ID_PARAM_NAME, false, params, oper, true));
                
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }


    /**
     * WithVars methods
     */

    @Override
    public Response withVarsStartProcessInstance(String processId) {
        Map<String, String[]> requestParams = getRequestParams();
        String oper = getRelativePath();
        Map<String, Object> params = extractMapFromParams(requestParams, oper );

        ProcessInstance procInst = startProcessInstance(processId, params);
        
        Map<String, String> vars = getVariables(procInst.getId());
        JaxbProcessInstanceWithVariablesResponse resp = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, getRequestUri());
        
        return createCorrectVariant(resp, headers);
    }

    @Override
    public Response withVarsGetProcessInstance(Long procInstId) {
        ProcessInstance procInst = getProcessInstance(procInstId, true);
        Map<String, String> vars = getVariables(procInstId);
        JaxbProcessInstanceWithVariablesResponse responseObj 
            = new JaxbProcessInstanceWithVariablesResponse(procInst, vars, getRequestUri());
        
        return createCorrectVariant(responseObj, headers);
    }

    @Override
    public Response withVarsSignalProcessInstance(Long procInstId) {
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        String eventType = getStringParam("signal", true, params, oper);
        Object event = getObjectParam("event", false, params, oper);

        processRequestBean.doKieSessionOperation(
                new SignalEventCommand(procInstId, eventType, event),
                deploymentId, 
                procInstId);
        
        ProcessInstance processInstance = getProcessInstance(procInstId, false);
        Map<String, String> vars = getVariables(procInstId);
        
        return createCorrectVariant(new JaxbProcessInstanceWithVariablesResponse(processInstance, vars), headers);
    }

    // Helper methods --------------------------------------------------------------------------------------------------------------
    
    private ProcessInstance getProcessInstance(long procInstId, boolean throwEx ) { 
        Command<?> cmd = new GetProcessInstanceCommand(procInstId);
        ((GetProcessInstanceCommand) cmd).setReadOnly(true);
        Object procInstResult = processRequestBean.doKieSessionOperation(
                cmd,
                deploymentId, 
                procInstId);
        
        if (procInstResult != null) {
            return (ProcessInstance) procInstResult;
        } else if( throwEx ) {
            throw KieRemoteRestOperationException.notFound("Unable to retrieve process instance " + procInstId
                    + " which may have been completed. Please see the history operations.");
        } else { 
            return null;
        }
    }
    
    private Map<String, String> getVariables(long processInstanceId) {
        Object result = processRequestBean.doKieSessionOperation(
                new FindVariableInstancesCommand(processInstanceId),
                deploymentId, 
                processInstanceId);
        List<VariableInstanceLog> varInstLogList = (List<VariableInstanceLog>) result;
        
        Map<String, String> vars = new HashMap<String, String>();
        if( varInstLogList.isEmpty() ) { 
            return vars;
        }
        
        Map<String, VariableInstanceLog> varLogMap = new HashMap<String, VariableInstanceLog>();
        for( VariableInstanceLog varLog: varInstLogList ) {
            String varId = varLog.getVariableId();
            VariableInstanceLog prevVarLog = varLogMap.put(varId, varLog);
            if( prevVarLog != null ) { 
                if( prevVarLog.getDate().after(varLog.getDate()) ) { 
                  varLogMap.put(varId, prevVarLog);
                } 
            }
        }
        
        for( Entry<String, VariableInstanceLog> varEntry : varLogMap.entrySet() ) { 
            vars.put(varEntry.getKey(), varEntry.getValue().getValue());
        }
            
        return vars;
    }
    
    private ProcessInstance startProcessInstance(String processId, Map<String, Object> params) { 
        Object result = null;
        try { 
            result = processRequestBean.doKieSessionOperation(
                new StartProcessCommand(processId, params),
                deploymentId, 
                null);
        } catch( IllegalArgumentException iae ) { 
            if( iae.getMessage().startsWith("Unknown process ID")) { 
                throw KieRemoteRestOperationException.notFound("Process '" + processId + "' is not known to this deployment.");
            }
            throw iae;
        }
        return (ProcessInstance) result;
    }

    protected QName getRootElementName(Object object) { 
        boolean xmlRootElemAnnoFound = false;
        Class<?> objClass = object.getClass();
        
        // This usually doesn't work in the kie-wb/bpms environment, see comment below
        XmlRootElement xmlRootElemAnno = objClass.getAnnotation(XmlRootElement.class);
        logger.debug("Getting XML root element annotation for " + object.getClass().getName());
        if( xmlRootElemAnno != null ) { 
            xmlRootElemAnnoFound = true;
            return new QName(xmlRootElemAnno.name());
        } else { 
            /**
             * There seem to be weird classpath issues going on here, probably related
             * to the fact that kjar's have their own classloader..
             * (The XmlRootElement class can't be found in the same classpath as the
             * class from the Kjar)
             */
            for( Annotation anno : objClass.getAnnotations() ) { 
                Class<?> annoClass = anno.annotationType();
                // we deliberately compare *names* and not classes because it's on a different classpath!
                if( XmlRootElement.class.getName().equals(annoClass.getName()) ) { 
                    xmlRootElemAnnoFound = true;
                    try {
                        Method nameMethod = annoClass.getMethod("name");
                        Object nameVal = nameMethod.invoke(anno);
                        if( nameVal instanceof String ) { 
                            return new QName((String) nameVal); 
                        }
                    } catch (Exception e) {
                        throw KieRemoteRestOperationException.internalServerError("Unable to retrieve XmlRootElement info via reflection", e);
                    } 
                }
            }
            if( ! xmlRootElemAnnoFound ) { 
                String errorMsg = "Unable to serialize " + object.getClass().getName() + " instance "
                        + "because it is missing a " + XmlRootElement.class.getName() + " annotation with a name value.";
                throw KieRemoteRestOperationException.internalServerError(errorMsg);
            }
            return null;
        }
    }

}
