package com.ansi.scilla.report.reportBuilder.xlsBuilder;

/**
 * Default width of report columns<br />
 * Value is 1/256 of a character (per https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/Sheet.html)<br />
 * Naming convention is &lt;DB table&gt;_&lt;field&gt;
 * 
 * @author dclewis
 *
 */
public enum ColumnWidth {
	DATETIME(3750),
	DATE(2750),
	HEADER_COL1(2000),
	HEADER_COL_RIGHT(2000),
	HEADER_ANSI(8600),
	HDR_RIGHT_NON_DISPATCHED(3500),
	
	ADDRESS_NAME(11000),
	ADDRESS_ADDRESS1(11000),
	ADDRESS_CITY(3500),
	ADDRESS_STATE(1500),
	
	CONTACT_NAME(11000),
	
	
	JOB_JOB_NBR(1400),
	JOB_JOB_FREQUENCY(1400),
	JOB_JOB_STATUS(1600), 
	JOB_PPC(2500),
	
	;
	
	private final Integer width;
	
	private ColumnWidth(Integer width) {
		this.width = width;
	}
	
	public Integer width() {
		return this.width;
	}
}
