package com.ansi.scilla.report.reportBuilder.common;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;

public class CustomCell extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private Object value;
	private CustomCellFormat format;
	
	
	public CustomCell() {
		super();
	}
	
	public CustomCell(Object value, CustomCellFormat format) {
		this();
		this.value = value;
		this.format = format;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public CustomCellFormat getFormat() {
		return format;
	}

	public void setFormat(CustomCellFormat format) {
		this.format = format;
	}

	/**
	 * Courtesy method for format object
	 * 
	 * @param workbook
	 * @return
	 */
	public CellStyle makeXlsStyle(XSSFWorkbook workbook) {
		return format.makeXlsStyle(workbook);
	}
}
