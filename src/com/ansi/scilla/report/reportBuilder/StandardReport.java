package com.ansi.scilla.report.reportBuilder;

import java.util.ArrayList;
import java.util.List;


public abstract class StandardReport extends AbstractReport {

	private static final long serialVersionUID = 1L;

	private ColumnHeader[] headerRow;
	private String[] pageBreakFieldList;	
	private List<Object> dataRows;
	
	public StandardReport() {
		super();
		this.dataRows = new ArrayList<Object>();
	}

	public ColumnHeader[] getHeaderRow() {
		return headerRow;
	}
	public void setHeaderRow(ColumnHeader[] headerRow) {
		this.headerRow = headerRow;
	}
	public String[] getPageBreakFieldList() {
		return pageBreakFieldList;
	}
	public void setPageBreakFieldList(String[] pageBreakFieldList) {
		this.pageBreakFieldList = pageBreakFieldList;
	}
	public List<Object> getDataRows() {
		return dataRows;
	}
	public void setDataRows(List<Object> dataRows) {
		this.dataRows = dataRows;
	}
	public void addDataRow(Object dataRow) {
		this.dataRows.add(dataRow);
	}
	
}
