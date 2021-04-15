package com.ansi.scilla.report.reportBuilder.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.ApplicationObject;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

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


	/**
	 * Make a string that looks like:
	 *   <code>&lt;td class="ansi-stdrpt-column-i" style="xxxx"&gt;&lt;span "ansi-stdrpt-column-text-i" style="yyyy"&gt;value&lt;/span&gt;&lt;/td&gt;</code>
	 * where xxxx and yyyy are based on the custom formatter values
	 * @param columnIndex
	 * @return
	 * @throws Exception 
	 */
	public String makeHtml(int columnIndex) throws Exception {
		String textStyle = format.makeHtmlTextStyle();
		String cellStyle = format.makeHtmlCellStyle();
		StringBuffer html = new StringBuffer();
		html.append("<td");
		html.append(" class=\"ansi-stdrpt-column-" + columnIndex + "\"");
		html.append(" style=\"" + cellStyle + "\">");
		html.append("<span");
		html.append(" class=\"ansi-stdrpt-column-text-" + columnIndex + "\"");
		html.append(" style=\"" + textStyle + "\">");
		html.append(format.formatValueAsText(this.value));
		html.append("</span>");
		html.append("</td>");
		
		return StringUtils.join(html, " ");
	}
	
	
	/**
	 * Courtesy method for cell formatter
	 * 
	 * @return
	 * @throws Exception
	 */
	public String formatValueAsText() throws Exception {
		return format.formatValueAsText(this.value);
	}

	public PdfPCell makePdfCell() throws Exception {
		String display = format.formatValueAsText(this.value);
		Chunk chunk = format.makePdfDisplay(display);
		PdfPCell pdfCell = new PdfPCell(new Phrase(chunk));
		format.formatPdfCell(pdfCell);
		return pdfCell;
	}
}
