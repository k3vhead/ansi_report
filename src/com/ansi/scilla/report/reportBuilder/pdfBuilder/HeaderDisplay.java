package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.ansi.scilla.common.ApplicationObject;
import com.itextpdf.text.Chunk;

public class HeaderDisplay extends ApplicationObject {
	private static final long serialVersionUID = 1L;
	
	public Chunk label;
	public Chunk display;
	public Float labelSize;
	public Float displaySize;
	public HeaderDisplay(String label, String display) {
		super();
		this.label = new Chunk(label, PDFReportFormatter.fontStandardBlackBold);
		this.display = new Chunk(display, PDFReportFormatter.fontStandardBlack);
		this.labelSize = this.label.getWidthPoint();
		this.displaySize = this.display.getWidthPoint();
		
	}
	
	
}
