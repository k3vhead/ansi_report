package com.ansi.scilla.report.reportBuilder.xlsBuilder;

import com.ansi.scilla.common.ApplicationObject;

/**
 * Defines offset for beginning of report on a page
 * 
 * @author dclewis
 *
 */
public class ReportStartLoc extends ApplicationObject {
	
	private static final long serialVersionUID = 1L;

	public Integer columnIndex;
	public Integer rowIndex;
	public ReportStartLoc(Integer columnIndex, Integer rowIndex) {
		super();
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}
	

}
