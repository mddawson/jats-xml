package uk.ac.jisc.jats;

import java.time.LocalDate;

public class SummaryInfo
{
	public String title;
	public LocalDate pub_earliest;
	public LocalDate pub_latest;
	/**
	 * Getters and setters
	 */
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public LocalDate getPub_earliest() {
		return pub_earliest;
	}
	public void setPub_earliest(LocalDate pub_earliest) {
		this.pub_earliest = pub_earliest;
	}
	public LocalDate getPub_latest() {
		return pub_latest;
	}
	public void setPub_latest(LocalDate pub_latest) {
		this.pub_latest = pub_latest;
	}
}
