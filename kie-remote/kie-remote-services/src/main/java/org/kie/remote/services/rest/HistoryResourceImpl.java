package org.kie.remote.services.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.remote.services.rest.api.HistoryResource;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
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
 * give it a name that explains it's funtion.
 */
@RequestScoped
@SuppressWarnings("unchecked")
public class HistoryResourceImpl extends ResourceBase implements HistoryResource {

    /* REST information */
    
    @Context
    private HttpHeaders headers;
   
    // Rest methods --------------------------------------------------------------------------------------------------------------
   
    @Override
    public Response clear() {
        getAuditLogService().clear();
        return createCorrectVariant(new JaxbGenericResponse(getRequestUri()), headers);
    }

    @Override
    public Response getProcessInstanceLogs() {
        String oper = getRelativePath();
        Map<String, String []> params = getRequestParams();
        
        List<ProcessInstanceLog> procInstLogResults = getAuditLogService().findProcessInstances();
        sortProcessInstanceLogs(procInstLogResults);
        
        List<Object> results = new ArrayList<Object>(procInstLogResults);
        JaxbHistoryLogList resultList =  paginateAndCreateResult(params, oper, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }

    @Override
    public Response getProcessInstanceLog(long procInstId) {
        ProcessInstanceLog procInstLog = getAuditLogService().findProcessInstance(procInstId);
        JaxbProcessInstanceLog jaxbProcLog = new JaxbProcessInstanceLog(procInstLog);
        
        return createCorrectVariant(jaxbProcLog, headers);
    }

    @Override
    public Response getInstanceLogsByProcInstId(Long instId, String logType) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        List<? extends Object> varInstLogList;
        if ("child".equalsIgnoreCase(logType)) {
            Object result = getAuditLogService().findSubProcessInstances(instId);
            varInstLogList = (List<Object>) result;
            sortProcessInstanceLogs((List<ProcessInstanceLog>) varInstLogList);
        } else if ("node".equalsIgnoreCase(logType)) {
            Object result = getAuditLogService().findNodeInstances(instId);
            varInstLogList = (List<Object>) result;
            sortNodeInstanceLogs((List<NodeInstanceLog>) varInstLogList);
        } else if ("variable".equalsIgnoreCase(logType)) {
            Object result = getAuditLogService().findVariableInstances(instId);
            varInstLogList = (List<Object>) result;
            sortVariableInstanceLogs((List<VariableInstanceLog>) varInstLogList);
        } else {
            throw KieRemoteRestOperationException.badRequest("Unsupported operation: " + oper );
        }

        JaxbHistoryLogList resultList =  paginateAndCreateResult(params, oper, (List<Object>) varInstLogList, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }

    @Override
    public Response getInstanceLogsByProcInstIdByLogId(Long procInstId, String operation, String logId) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        List<? extends Object> varInstLogList;
        if ("node".equalsIgnoreCase(operation)) {
            Object result = getAuditLogService().findNodeInstances(procInstId, logId);
            varInstLogList = (List<Object>) result;
            sortNodeInstanceLogs((List<NodeInstanceLog>) varInstLogList);
        } else if ("variable".equalsIgnoreCase(operation)) {
            Object result = getAuditLogService().findVariableInstances(procInstId, logId);
            varInstLogList = (List<Object>) result;
            sortVariableInstanceLogs((List<VariableInstanceLog>) varInstLogList);
        } else {
            throw KieRemoteRestOperationException.badRequest("Unsupported operation: " + oper );
        }
        
        JaxbHistoryLogList resultList = paginateAndCreateResult(params, oper, (List<Object>) varInstLogList, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }

    @Override
    public Response getProcessInstanceLogsByProcessId(String processId) {
        Map<String, String []> params = getRequestParams();
        Number statusParam = getNumberParam("status", false, params, getRelativePath(), false);
        String oper = getRelativePath();
        int[] pageInfo = getPageNumAndPageSize(params, oper);

        Object result;
        if (statusParam != null) {
            if (statusParam.intValue() == ProcessInstance.STATE_ACTIVE) {
                result = getAuditLogService().findActiveProcessInstances(processId);
            } else {
                result = getAuditLogService().findProcessInstances(processId);
            }
        } else {
            result = getAuditLogService().findProcessInstances(processId);
        }
        
        List<ProcessInstanceLog> procInstLogList = (List<ProcessInstanceLog>) result;
        
        List<ProcessInstanceLog> filteredProcLogList = procInstLogList;
        if (statusParam != null && !statusParam.equals(ProcessInstance.STATE_ACTIVE)) {
            filteredProcLogList = new ArrayList<ProcessInstanceLog>();
            for (int i = 0; 
                    i < procInstLogList.size() && filteredProcLogList.size() < getMaxNumResultsNeeded(pageInfo);
                    ++i) {
                ProcessInstanceLog procLog = procInstLogList.get(i);
                if (procLog.getStatus().equals(statusParam.intValue())) {
                    filteredProcLogList.add(procLog);
                }
            }
        }
        
        sortProcessInstanceLogs(filteredProcLogList);
        List<Object> results = new ArrayList<Object>(filteredProcLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(pageInfo, results, new JaxbHistoryLogList());
        return createCorrectVariant(resultList, headers);
    }

    @Override
    public Response getVariableInstanceLogsByVariableId(String variableId) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        
        List<VariableInstanceLog> varInstLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        sortVariableInstanceLogs(varInstLogList);
        
        List<Object> results = new ArrayList<Object>(varInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(params, oper, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }
    
    @Override
    public Response getVariableInstanceLogsByVariableIdByVariableValue(String variableId, String value) {
        Map<String, String []> params = getRequestParams();
        String oper = getRelativePath();
        List<VariableInstanceLog> varInstLogList = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        sortVariableInstanceLogs(varInstLogList);
        
        List<Object> results = new ArrayList<Object>(varInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(params, oper, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    } 
   
    @Override
    public Response getProcessInstanceLogsByVariableId(String variableId) {
        Map<String, String[]> params = getRequestParams();
        String oper = getRelativePath();

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, null, params, oper);
        
        // get process instance logs
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);
        List<ProcessInstanceLog> procInstLogList = getProcessInstanceLogsByVariable(varLogList, maxNumResults);
       
        // paginate
        List<Object> results = new ArrayList<Object>(procInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(pageInfo, results, new JaxbHistoryLogList());
        
        return createCorrectVariant(resultList, headers);
    }
    
    @Override
    public Response getProcessInstanceLogsByVariableIdByVariableValue(String variableId, String value) {
        Map<String, String[]> params = getRequestParams();
        String oper = getRelativePath();

        // get variables
        List<VariableInstanceLog> varLogList = internalGetVariableInstancesByVarAndValue(variableId, value, params, oper);
        
        // get process instance logs
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);
        List<ProcessInstanceLog> procInstLogList = getProcessInstanceLogsByVariable(varLogList, maxNumResults);
        
        List<Object> results = new ArrayList<Object>(procInstLogList);
        JaxbHistoryLogList resultList = paginateAndCreateResult(pageInfo, results, new JaxbHistoryLogList());
        return createCorrectVariant(resultList, headers);
    }
    
    // Helper methods --------------------------------------------------------------------------------------------------------------

    private List<VariableInstanceLog> internalGetVariableInstancesByVarAndValue(String varId, String value, 
            Map<String, String[]> params, String oper) { 
        // active processes parameter
        String activeProcsParam = getStringParam("activeProcesses", false, params, oper); 
        boolean onlyActiveProcesses = false;
        if( activeProcsParam != null ) { 
            onlyActiveProcesses = Boolean.parseBoolean(activeProcsParam);
        }
       
        Object result;
        if( value == null ) { 
            result = getAuditLogService().findVariableInstancesByName(varId, onlyActiveProcesses);
        } else { 
            result = getAuditLogService().findVariableInstancesByNameAndValue(varId, value, onlyActiveProcesses);
        }
        
        return (List<VariableInstanceLog>) result;
    }

    private List<ProcessInstanceLog> getProcessInstanceLogsByVariable(List<VariableInstanceLog> varLogList, int maxNumResults) {
        int numVarLogs = varLogList.size();
        int numProcInsts = 0;
       
        List<ProcessInstanceLog> resultList = new ArrayList<ProcessInstanceLog>();
        for( int i = 0; i < numVarLogs && numProcInsts < maxNumResults; ++i ) { 
            long procInstId = varLogList.get(i).getProcessInstanceId();
            ProcessInstanceLog procInstlog = getAuditLogService().findProcessInstance(procInstId);
            if( procInstlog != null ) { 
                resultList.add(procInstlog);
                ++numProcInsts;
            }
        }
        return resultList;
    }

    private void sortProcessInstanceLogs(List<ProcessInstanceLog> procInstLogList) { 
        Collections.sort(procInstLogList, new Comparator<ProcessInstanceLog>() {
    
            @Override
            public int compare( ProcessInstanceLog o1, ProcessInstanceLog o2 ) {
                if( ! o1.getExternalId().equals(o2.getExternalId()) ) { 
                   return o1.getExternalId().compareTo(o2.getExternalId());
                }
                if( ! o1.getProcessId().equals(o2.getProcessId()) ) { 
                   return o1.getProcessId().compareTo(o2.getProcessId());
                }
                return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
            }
        });
    }
    

    private void sortNodeInstanceLogs(List<NodeInstanceLog> procInstLogList) { 
        Collections.sort(procInstLogList, new Comparator<NodeInstanceLog>() {
    
            @Override
            public int compare( NodeInstanceLog o1, NodeInstanceLog o2 ) {
                if( ! o1.getExternalId().equals(o2.getExternalId()) ) { 
                   return o1.getExternalId().compareTo(o2.getExternalId());
                }
                if( ! o1.getProcessId().equals(o2.getProcessId()) ) { 
                   return o1.getProcessId().compareTo(o2.getProcessId());
                }
                if( ! o1.getProcessInstanceId().equals(o2.getProcessInstanceId()) ) { 
                   return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
                }
                if( ! o1.getNodeId().equals(o2.getNodeId()) ) { 
                   return o1.getNodeId().compareTo(o2.getNodeId());
                }
                return o1.getNodeInstanceId().compareTo(o2.getNodeInstanceId());
            }
        });
    }

    private void sortVariableInstanceLogs(List<VariableInstanceLog> varInstLogList ) { 
        Collections.sort(varInstLogList, new Comparator<VariableInstanceLog>() {

            @Override
            public int compare( VariableInstanceLog o1, VariableInstanceLog o2 ) {
                if( ! o1.getExternalId().equals(o2.getExternalId()) ) { 
                    return o1.getExternalId().compareTo(o2.getExternalId());
                }
                if( ! o1.getProcessId().equals(o2.getProcessId()) ) { 
                    return o1.getProcessId().compareTo(o2.getProcessId());
                }
                if( ! o1.getProcessInstanceId().equals(o2.getProcessInstanceId()) ) { 
                   return o1.getProcessInstanceId().compareTo(o2.getProcessInstanceId());
                }
                if( ! o1.getVariableId().equals(o2.getVariableId()) ) { 
                   return o1.getVariableId().compareTo(o2.getVariableId());
                }
                return o1.getVariableInstanceId().compareTo(o2.getVariableInstanceId());
            }
        });
    }
}
    