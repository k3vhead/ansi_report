package com.ansi.scilla.report.reportBuilder;

import com.ansi.scilla.common.ApplicationObject;

public class ColumnHeader extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	private String label;
	private DataFormats formatter;
	private SummaryType summaryType;	
	private String subTotalTrigger;
	private Integer maxCharacters;
	
	public ColumnHeader(String fieldName, String label, DataFormats formatter, SummaryType summaryType) {
		this(fieldName, label, formatter, summaryType, null);
	}
	
//	public ColumnHeader(String fieldName, String label, DataFormats formatter,
//			SummaryType summaryType, Integer maxCharacters) {
//		this(fieldName, label, formatter, summaryType, null, maxCharacters);
//	}
	
	/**
	 * Make a column header for report details
	 * @param fieldName Field name
	 * @param label Text for the column header
	 * @param formatter Method for formatting the data
	 * @param summaryType Whether/what kind of summary to display at end of column
	 * @param subTotalTrigger - name of the field that, upon value change, triggers the display of a subtotal
	 */
	public ColumnHeader(String fieldName, String label, DataFormats formatter, SummaryType summaryType, String subTotalTrigger) {
		super();
		this.fieldName = fieldName;
		this.label = label;
		this.formatter = formatter;
		this.summaryType = summaryType;
		this.subTotalTrigger = subTotalTrigger;
	}
	
	public ColumnHeader(String fieldName, String label, DataFormats formatter, 
			SummaryType summaryType, String subTotalTrigger, Integer maxCharacters) {
		super();
		this.fieldName = fieldName;
		this.label = label;
		this.formatter = formatter;
		this.summaryType = summaryType;
		this.subTotalTrigger = subTotalTrigger;
		this.maxCharacters = maxCharacters;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public DataFormats getFormatter() {
		return formatter;
	}
	public void setFormatter(DataFormats formatter) {
		this.formatter = formatter;
	}
	public SummaryType getSummaryType() {
		return summaryType;
	}
	public void setSummaryType(SummaryType summaryType) {
		this.summaryType = summaryType;
	}

	public String getSubTotalTrigger() {
		return subTotalTrigger;
	}

	public void setSubTotalTrigger(String subTotalTrigger) {
		this.subTotalTrigger = subTotalTrigger;
	}

	public Integer getMaxCharacters() {
		return maxCharacters;
	}

	public void setMaxCharacters(Integer maxCharacters) {
		this.maxCharacters = maxCharacters;
	}
	
	
}
