package org.jboss.tusk.hadoop;

public class TuskHBaseConfiguration {

	private String mpTable = "messages";
	private String mpDataCF = "data";
	private String mpMetadataCF = "metadata";
	private String mpValueQuantifier = "value";

	private String mpIndexTable = "message-index";
	private String mpFieldsCF = "fields";
	public String getMpTable() {
		return mpTable;
	}
	public void setMpTable(String mpTable) {
		this.mpTable = mpTable;
	}
	public String getMpDataCF() {
		return mpDataCF;
	}
	public void setMpDataCF(String mpDataCF) {
		this.mpDataCF = mpDataCF;
	}
	public String getMpMetadataCF() {
		return mpMetadataCF;
	}
	public void setMpMetadataCF(String mpMetadataCF) {
		this.mpMetadataCF = mpMetadataCF;
	}
	public String getMpValueQuantifier() {
		return mpValueQuantifier;
	}
	public void setMpValueQuantifier(String mpValueQuantifier) {
		this.mpValueQuantifier = mpValueQuantifier;
	}
	public String getMpIndexTable() {
		return mpIndexTable;
	}
	public void setMpIndexTable(String mpIndexTable) {
		this.mpIndexTable = mpIndexTable;
	}
	public String getMpFieldsCF() {
		return mpFieldsCF;
	}
	public void setMpFieldsCF(String mpFieldsCF) {
		this.mpFieldsCF = mpFieldsCF;
	}
	
	
}
