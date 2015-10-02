package delegates;

import java.util.List;

import models.Contribution;
import models.ResourceSpace;

import org.dozer.DozerBeanMapper;

import play.Play;

public class ContributionsDelegate {

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
	public static List<Contribution> findContributionsInResourceSpace(Long sid,
			String query) {
		return query != null && !query.isEmpty() ? Contribution
				.findAllByContainingSpace(sid) : Contribution
				.findAllByContainingSpaceAndQuery(sid, query);
	}

	public static List<Contribution> findContributionsInResourceSpace(
			ResourceSpace rs, String type, String query) {
		if (type != null && !type.isEmpty()) {
			return query != null && !query.isEmpty() ? Contribution
					.findAllByContainingSpaceAndType(rs, type) : Contribution
					.findAllByContainingSpaceAndTypeAndQuery(rs, type, query);
		} else {
			return query != null && !query.isEmpty() ? findContributionsInResourceSpace(
					rs.getResourceSpaceId(), query) : rs != null ? rs
					.getContributions() : null;
		}
	}
}
