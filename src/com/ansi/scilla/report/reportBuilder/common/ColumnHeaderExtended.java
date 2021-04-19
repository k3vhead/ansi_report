package com.ansi.scilla.report.reportBuilder.common;

import java.lang.reflect.Method;

import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;

public class ColumnHeaderExtended extends ColumnHeader {

	private static final long serialVersionUID = 1L;
	private Method getterMethod;
	
	public ColumnHeaderExtended(Method getterMethod, String valueKey, String label, Integer colspan, DataFormats formatter,
			SummaryType summaryType, String subTotalTrigger, Integer maxCharacters) {
		super(valueKey, label, colspan, formatter, summaryType, subTotalTrigger, maxCharacters);
		this.getterMethod = getterMethod;
	}

	public ColumnHeaderExtended(Method getterMethod, String valueKey, String label, Integer colspan, DataFormats formatter, SummaryType summaryType, String subTotalTrigger) {
		super(valueKey, label, colspan, formatter, summaryType, subTotalTrigger);
		this.getterMethod = getterMethod;
	}

	public ColumnHeaderExtended(Method getterMethod, String valueKey, String label, Integer colspan, DataFormats formatter, SummaryType summaryType) {
		super(valueKey, label, colspan, formatter, summaryType);
		this.getterMethod = getterMethod;
	}

	public Method getGetterMethod() {
		return getterMethod;
	}

	public void setGetterMethod(Method getterMethod) {
		this.getterMethod = getterMethod;
	}


	
}
