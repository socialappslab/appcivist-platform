package delegates;

import java.util.List;

import enums.AssemblyStatus;
import enums.WorkingGroupStatus;
import models.*;
import models.transfer.AssemblyTransfer;
import models.transfer.WorkingGroupSummaryTransfer;

import models.transfer.WorkingGroupTransfer;
import org.dozer.DozerBeanMapper;

import play.Logger;
import play.Play;

public class WorkingGroupsDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = Play.application().configuration()
				.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}

	public static WorkingGroupSummaryTransfer readListedWorkingGroup(Long gid, User requestor) {
		// 1. Read the assembly and check if it is listed
		WorkingGroup g = WorkingGroup.read(gid);
		if (g!=null && g.getListed()) {
			return mapper.map(g, WorkingGroupSummaryTransfer.class);
		} else {
			return null;
		}
	}

	public static WorkingGroup publish(Long workingGroupId) {
		WorkingGroup workingGroup =  WorkingGroup.read(workingGroupId);
		workingGroup.setStatus(WorkingGroupStatus.PUBLISHED);
		workingGroup.update();
		workingGroup.refresh();
		return  workingGroup;
	}

	public static void addContributionToWorkingGroups(Contribution c, List<WorkingGroup> workingGroupAuthors, Boolean checkIfExistsFirst) {
		for (WorkingGroup wg : workingGroupAuthors) {
			ResourceSpace groupRS = wg.getResources();
			Boolean contributionAlreadyExists = false;
			if (checkIfExistsFirst) {
				Contribution cInGroup = Contribution.findByResourceSpaceId(groupRS.getResourceSpaceId());
				if (cInGroup != null) {
					contributionAlreadyExists = true;
                    Logger.debug("Contribution already exists into Working Group: "+c.getContributionId()+" - " +wg.getGroupId());
                }
			}

			if (!contributionAlreadyExists) {
				Logger.debug("Contribution added to Working Group: "+c.getContributionId()+" - " +wg.getGroupId());
				List<ResourceSpace> contributionContainers = c.getContainingSpaces();
				groupRS.addContribution(c);
				contributionContainers.add(groupRS);
				groupRS.update();
			}
		}
	}

}
