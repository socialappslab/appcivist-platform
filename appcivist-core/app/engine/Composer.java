package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Assembly;
import models.services.Service;
import models.services.ServiceOperation;
import models.services.ServiceOperationDefinition;
import models.services.ServiceParameter;
import models.services.ServiceParameterDataModel;
import models.services.ServiceParameterDefinition;
import play.libs.Json;

public class Composer {

	/**
	 * 1. Within the composition (i.e., the assembly), looks for the specific "Service" to which 
	 *    the operation key is mapped
	 * 2. Within the Service, looks for the specific "Operation Definition" to which the operation 
	 *    key is mapped
	 * 3. Using the Operation Definition (i.e., type, method, required parameters, etc.) creates 
	 *    an "executable" instance of that operation that can be later executed
	 *    
	 * @param operationKey
	 * @param composition
	 * @param paramValues
	 * @return
	 */
	public static ServiceOperation createOperationInstance(
			String operationKey,
			Assembly composition, 
			Map<String, Object> paramValues, 
			String expectedResult) {
		
		// STEP 0 => find the operation definition for the requested operation 
		Service service = composition.getServiceForOperation(operationKey);
		String operationDefinitionKey = service.getDefinitionKeyForOperation(operationKey);

		// STEP 1 => read the operation definition
		ServiceOperationDefinition operationDefinition = ServiceOperationDefinition.read(operationDefinitionKey);
		System.out
				.println("COMPOSER > #1 > Definition of ["
								+ service.getName() 
								+ "]." 
								+ operationKey 
								+ ": "
								+ Json.toJson(operationDefinition));

		// STEP 2 => create an operation instance for that definition
		System.out
				.println("COMPOSER > #2 > Preparing instance of ["
								+ service.getName() 
								+ "]." 
								+ operationKey);
		ServiceOperation operation = new ServiceOperation();
		operation.setDefinition(operationDefinition);
		operation.setService(service);
		operation.setAppCivistOperation(operationKey);
		operation.setExpectedResource(expectedResult);
		// STEP 3 => setup parameters for the operation according to definition
		List<ServiceParameterDefinition> paramDefinitions = operationDefinition.getParameters();
		System.out
				.println("COMPOSER > #3 > Preparing parameters for instance of ["
								+ service.getName()
								+ "]."
								+ operationKey
								+ " (according to its definition):"
								+ Json.toJson(paramDefinitions));
		
		int paramNum = 0;
		int paramCount = paramDefinitions.size();
		for (ServiceParameterDefinition pDef : paramDefinitions) {
			paramNum++;
			System.out
					.println("COMPOSER > #3.1"
							+ " > Setting value for Parameter "
							+ pDef.getName()
							+ " ("
							+ paramNum
							+ "/"
							+ paramCount
							+ "):"
							+ Json.toJson(pDef));
			
			ServiceParameter param = prepareOperationParameter(operation, pDef, paramValues);
					
			System.out
					.println("COMPOSER > #3.1 > Value for parameter "
							+ pDef.getName()
							+ " = "
							+ Json.toJson(param));
			
			operation.addParameter(param);
		}

		return operation;
	}

	private static ServiceParameter prepareOperationParameter(
			ServiceOperation operation, ServiceParameterDefinition pDef, Map<String, Object> paramValues) {
		
		ServiceParameter param = new ServiceParameter();
		param.setServiceOperation(operation);
		param.setServiceParameter(pDef);
		
		String paramType = pDef.getType();
		List<ServiceParameterDataModel> dataModel = pDef.getDataModel();
		String paramName = pDef.getName();
		
		if (paramType.equals("BODY_PARAM") || dataModel.size()>0 ) {
			Map<String, Object> bodyMap = new HashMap<String, Object>(); // parameters needed in the body
			List<ServiceParameterDataModel> dmList = pDef.getDataModel(); // data model of the body 

			
			//Map<String, Object> values = (Map<String, Object>) paramValues.get(paramName); // e.g., discussion
			bodyMap = processDataModel(paramName, dmList, paramValues);
			
			if (pDef.getDataType().equals("JSON")) {
				param.setValue(Json.toJson(bodyMap).toString());
			} else {
				param.setValue(bodyMap.toString()); // TODO: add XML, YAML and other formats for the body
			}
		} else { 
			Object value = paramValues.get(paramName);
			if (value==null) {
				param.setValue(pDef.getDefaultValue());
			} else {
				param.setValue((String) value); // TODO: support for other data types, not only everything as string
			}
		}

		return param;
	}

	/**
	 * Reads the DataModel of a parameter and setups the correct parameter values based on the provided
	 * paramValues table
	 * 
	 * @param paramName Name of the parameter used to identify its value in the the paramValues table
	 * 					Nested datamodles are identified by concatenating their names: 
	 * 					- discussion.title => title field in the DataModel of the discussion parameter
	 * 					- questions.0.question => question field in the DataModel of the questions parameter, 
	 * 						with questions being a list 
	 * @param dmList
	 * @param paramValues
	 * @return
	 */
	private static Map<String, Object> processDataModel(
											String parentKey,
											List<ServiceParameterDataModel> dmList, 
											Map<String, Object> paramValues) {
		Map<String, Object> bodyMap = new HashMap<String, Object>(); 
		
		
		for (ServiceParameterDataModel dm : dmList) {
			String dmKey = dm.getDataKey();
			String dmDefault = dm.getDefaultValue();
			Boolean dmRequired = dm.getRequired();
			Boolean dmIsList = dm.getList();
			ServiceParameterDefinition dmDef = dm.getDefinition();
			ServiceParameterDataModel parent = dm.getParentDataModel();
			List<ServiceParameterDataModel> childDataModels = dm.getChildDataModel();
			
			if(childDataModels.size()>0) {
				// avoid circular connections
				if (parent!=null && !dm.equals(parent.getParentDataModel())) {
					if (dmIsList) {			
						Map<String,Object> dmValuesMap = (Map<String,Object>) paramValues.get(parentKey);			
						List<Map<String,Object>> valueMapList = (List<Map<String, Object>>) dmValuesMap.get(dmKey);
						List<Map<String,Object>> childParamValues = new ArrayList<Map<String,Object>>();
						for (Map<String, Object> valueMap : valueMapList) {
							Map<String,Object> childParamValueMap = processDataModel(dmKey, childDataModels,valueMap);
							childParamValues.add(childParamValueMap);
						}
						bodyMap.put(dmKey, childParamValues);
					} else {
						Map<String,Object> valueMap = (Map<String, Object>) paramValues.get(parentKey);
						Map<String,Object> childParamValues = processDataModel(dmKey, childDataModels,valueMap);
						bodyMap.put(dmKey, childParamValues);
					}
				} else if (parent==null) {
					if (dmIsList) {						
						Map<String,Object> dmValuesMap = (Map<String,Object>) paramValues.get(parentKey);			
						List<Map<String,Object>> valueMapList = (List<Map<String, Object>>) dmValuesMap.get(dmKey);
						List<Map<String,Object>> childParamValues = new ArrayList<Map<String,Object>>();
						for (Map<String, Object> valueMap : valueMapList) {
							Map<String,Object> childParamValueMap = processDataModel(dmKey, childDataModels,valueMap);
							childParamValues.add(childParamValueMap);
						}
						bodyMap.put(dmKey, childParamValues);
					} else {
						Map<String,Object> valueMap = (Map<String, Object>) paramValues.get(parentKey);
						Map<String,Object> childParamValues = processDataModel(dmKey, childDataModels,valueMap);
						bodyMap.put(dmKey, childParamValues);
					}
				} else { // TODO: what todo in circular connections
				}
			} else {
				String key = dmKey;
				Map<String,Object> dmValuesMap = (Map<String,Object>) paramValues.get(parentKey);
				String value = (String) dmValuesMap.get(key);
				if (value != null && !value.equals("")) {
					bodyMap.put(key, value);
				} else {
					bodyMap.put(key, dmDefault);
				}
			}

			String logMessage = "COMPOSER > #3.1.1"
					+ " > Reading DataModel of "+parentKey+" and associating with its value. "
					+ " Setting" + parentKey 
					+ "."
					+ dmKey
					+ ":";
			
			if (dmDef!=null) {
				logMessage += Json.toJson(dmDef);
			} else {
				logMessage+= Json.toJson(parent);
			}
			System.out.println(logMessage);	
		}
		
		return bodyMap;
	}
	
	/**
	 * Saves an instance of an operation in the database, so that can be executed without having 
	 * to be rebuild the next time
	 * @param op
	 * @return
	 */
	public static ServiceOperation saveOperationInstance(ServiceOperation op) {
		ServiceOperation.create(op);
		return op;
	}

}
