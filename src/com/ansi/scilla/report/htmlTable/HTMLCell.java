package com.ansi.scilla.report.htmlTable;

import java.lang.reflect.Method;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.DataFormats;

public class HTMLCell extends ApplicationObject implements Comparable<HTMLCell> {

	private static final long serialVersionUID = 1L;
	private Integer columnIndex;
	private Object cellValue;
	private String cellStyle ="";
	private String cellContentStyle = "";
	private DataFormats dataFormats;
	private Integer colspan=1;
	private Integer rowspan=1;
	
	public HTMLCell(Integer columnIndex) {
		super();
		this.columnIndex = columnIndex;
	}

	public String makeHTML() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<td class=\""+ this.cellStyle +"\" colspan=\"" + colspan + "\" rowspan=\"" + rowspan + "\">");
		buffer.append("<span class=\""+ this.cellContentStyle +"\">");
		
		// formatting data must be done this way, because we don't know what formatter we have
		// and we don't know what type of data we have.
		if ( this.cellValue == null ) {
			buffer.append("&nbsp;");
		} else {
			String data = String.valueOf(this.cellValue);
			if ( this.dataFormats != null ) {
				Method formatter = this.dataFormats.formatter().getClass().getMethod("format", new Class[] {this.cellValue.getClass()});
				data = (String)formatter.invoke(this.dataFormats.formatter(), new Object[] {this.cellValue});
			}
			buffer.append(data);
		}
		
		buffer.append("</span>");
		buffer.append("</td>\n");
		return buffer.toString();
	}

	public Integer getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(Integer columnIndex) {
		this.columnIndex = columnIndex;
	}

	public Object getCellValue() {
		return cellValue;
	}

	public void setCellValue(Object cellValue) {
		this.cellValue = cellValue;
	}

	public String getCellStyle() {
		return cellStyle;
	}

	public void setCellStyle(String cellStyle) {
		this.cellStyle = cellStyle;
	}

	public String getCellContentStyle() {
		return cellContentStyle;
	}

	public void setCellContentStyle(String cellContentStyle) {
		this.cellContentStyle = cellContentStyle;
	}

	public DataFormats getDataFormats() {
		return dataFormats;
	}

	public void setDataFormats(DataFormats dataFormats) {
		this.dataFormats = dataFormats;
	}

	public Integer getColspan() {
		return colspan;
	}

	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}

	public Integer getRowspan() {
		return rowspan;
	}

	public void setRowspan(Integer rowspan) {
		this.rowspan = rowspan;
	}

	@Override
	public int compareTo(HTMLCell o) {
		return this.getColumnIndex().compareTo(o.columnIndex);
	}
	
	
}
