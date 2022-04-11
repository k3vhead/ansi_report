package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.ansi.scilla.common.ApplicationObject;


public class TableSize extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	private Float height;
	private Float width;
	
	public TableSize() {
		super();
	}
	
	public TableSize(Float x, Float y) {
		this();
		this.height = x;
		this.width = y;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}
	
	
	
}
