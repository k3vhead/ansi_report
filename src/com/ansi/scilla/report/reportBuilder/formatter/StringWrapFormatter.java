package com.ansi.scilla.report.reportBuilder.formatter;

/**
 * This is so we can have a formatter for strings that matches the .format() method with
 * date formatter and decimal formatters. It's just to make coding easier in the report builder
 * @author dclewis
 *
 */
public class StringWrapFormatter extends ReportFormatter {
	private static final long serialVersionUID = 1L;

	public StringWrapFormatter(TextAlignment textAlignment) {
		super();
		this.textAlignment = textAlignment;
	}

	public String format(String string) {
		return string;
	}
}
