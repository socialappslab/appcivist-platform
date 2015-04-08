package engine;

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
			Map<String, String> paramValues) {
		
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
			
			ServiceParameter param = new ServiceParameter();
			param.setServiceOperation(operation);
			param.setServiceParameter(pDef);

			String values = paramValues.get(pDef.getName()); // e.g., discussion

			if (pDef.getType().equals("BODY_PARAM")) {
				Map<String, String> bodyMap = new HashMap<String, String>(); // parameters needed in the body
				Map<String, String> bodyParamValuesMap = new HashMap<String, String>(); // values for the parameters needed in the body
				List<ServiceParameterDataModel> dm = pDef.getDataModel(); // data model of the body 

				// TODO: take into account the requirements for each parameter
				
				// When the parameter is a body param, the value related to its key contains
				// the keys for its internal values separated by commas
				// TODO: improve this by not using String inthe values but instead using another map
				String[] valuesKeys = values.split(",");
				for (String key : valuesKeys) {
					bodyParamValuesMap.put(key, paramValues.get(key));
				}

				for (ServiceParameterDataModel part : dm) {
					System.out
					.println("COMPOSER > #3.1.1"
							+ " > Parameter is a body parameter. Reading its model and building it. "
							+ " Setting body param value "
							+ part.getDataKey()
							+ " ("
							+ paramNum
							+ "/"
							+ paramCount
							+ "):"
							+ Json.toJson(pDef));
					String key = part.getDataKey();
					String value = bodyParamValuesMap.get(key);

					if (value != null && !value.equals("")) {
						bodyMap.put(key, value);
					} else {
						bodyMap.put(key, part.getDefaultValue());
					}
				}

				if (pDef.getDataType().equals("JSON")) {
					param.setValue(Json.toJson(bodyMap).toString());
				} else {
					param.setValue(bodyMap.toString());
				}
			} else { 
				// TODO: support for other data types, not only everything as string
				param.setValue(paramValues.get(pDef.getName()));
			}
			
			System.out
					.println("COMPOSER > #3.1 > Value for parameter "
							+ pDef.getName()
							+ " = "
							+ Json.toJson(param));
			
			operation.addParameter(param);
		}

		return operation;
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
