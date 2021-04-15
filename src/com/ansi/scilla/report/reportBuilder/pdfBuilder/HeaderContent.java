package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.ansi.scilla.common.ApplicationObject;

public class HeaderContent extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	public float maxLabelSize;
	public float maxDataSize;
	public HeaderDisplay[][] headerDisplayList;
	
	public HeaderContent() {
		super();
	}

	public HeaderContent(float maxLabelSize, float maxDataSize, HeaderDisplay[][] headerDisplayList) {
		this();
		this.maxLabelSize = maxLabelSize;
		this.maxDataSize = maxDataSize;
		this.headerDisplayList = headerDisplayList;
	}
	
	
}
