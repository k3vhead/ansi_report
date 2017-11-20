package com.ansi.scilla.report.reportBuilder;

import com.ansi.scilla.common.ApplicationObject;

public abstract class ReportFormatter extends ApplicationObject {

	private static final long serialVersionUID = 1L;
	protected TextAlignment textAlignment = TextAlignment.LEFT;
	
	public TextAlignment getTextAlignment() {
		return textAlignment;
	}

}
