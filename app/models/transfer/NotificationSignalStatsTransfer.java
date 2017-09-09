package models.transfer;

import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel(value="NotificationSignalStatsTransfer", description="Notification Signals Stats")
public class NotificationSignalStatsTransfer {

	private Integer read;
	private Integer unread;
	private Integer total;
	private Integer pages;

	public Integer getRead() {
		return read;
	}

	public void setRead(Integer read) {
		this.read = read;
	}

	public Integer getUnread() {
		return unread;
	}

	public void setUnread(Integer unread) {
		this.unread = unread;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getPages() {
		return pages;
	}

	public void setPages(Integer pages) {
		this.pages = pages;
	}
}
