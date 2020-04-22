package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.ansi.scilla.common.ApplicationObject;

/**
 * Defines offset for beginning of report on a page
 * 
 * @author dclewis
 *
 */
public class ReportStartLoc extends ApplicationObject {
	
	private static final long serialVersionUID = 1L;

	public Float columnIndex;
	public Float rowIndex;
	public ReportStartLoc(Float columnIndex, Float rowIndex) {
		super();
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}
	

}
