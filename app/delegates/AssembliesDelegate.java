package delegates;

import java.util.ArrayList;
import java.util.List;

import models.Assembly;
import models.transfer.AssemblySummaryTransfer;

import org.dozer.DozerBeanMapper;

import play.Play;

public class AssembliesDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = Play.application().configuration()
				.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}
	
	/**
	 * Return queries based on a query and a filter
	 * 
	 */
	public static List<Assembly> findAssemblies(String query, String filter, Boolean onlyListed) {
		List<Assembly> a = null;
		if (filter.equals("featured")) {
			a = Assembly.findFeaturedAssemblies(query, onlyListed);
		} else if (filter.equals("random")) { 
			a = Assembly.findRandomAssemblies(query, onlyListed);
//		TODO: } else if (filter.equals("nearby")) { return Assembly.findNearbyAssemblies();
//		TODO: } else if (filter.equals("summary")) { return Assembly.findAssembliesSummaries(query);
		} else {
			if (query!=null && !query.isEmpty()) return Assembly.findBySimilarName(query, onlyListed);
			else return Assembly.findAll(onlyListed);
		}
		
		return a;
	}
	
	
	/**
	 * Return queries based on a query and a filter
	 * 
	 */
	public static List<AssemblySummaryTransfer> findAssembliesPublic(String query, String filter) {
		List<Assembly> a = findAssemblies(query, filter, false);
		List<AssemblySummaryTransfer> result = new ArrayList<AssemblySummaryTransfer>();
		for (Assembly assembly : a) {
			AssemblySummaryTransfer destObject =  mapper.map(assembly, AssemblySummaryTransfer.class);
			result.add(destObject);
		}
		return result;
	}
}
