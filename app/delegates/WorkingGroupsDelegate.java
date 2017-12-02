package delegates;

import java.util.List;

import enums.AssemblyStatus;
import enums.WorkingGroupStatus;
import models.Assembly;
import models.User;
import models.WorkingGroup;
import models.transfer.AssemblyTransfer;
import models.transfer.WorkingGroupSummaryTransfer;

import models.transfer.WorkingGroupTransfer;
import org.dozer.DozerBeanMapper;

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

}
