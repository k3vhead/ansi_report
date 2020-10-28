package com.ansi.scilla.report.reportBuilder;


public enum DataFormats {
	DATE_FORMAT(new DateFormatter("MM/dd/yyyy")),
	DATE_TIME_FORMAT(new DateFormatter("MM/dd/yyyy HH:mm:ss")),
	DETAIL_TIME_FORMAT(new DateFormatter("MM/dd/yyyy HH:mm:ss.S")),
	
	INTEGER_FORMAT(new NumberFormatter("#,##0", TextAlignment.RIGHT)),
	NUMBER_FORMAT(new NumberFormatter("0", TextAlignment.RIGHT)),
	NUMBER_CENTERED(new NumberFormatter("0", TextAlignment.CENTER)),
	NUMBER_LEFT(new NumberFormatter("0", TextAlignment.LEFT)),
	DECIMAL_FORMAT(new NumberFormatter("#,##0.00", TextAlignment.RIGHT)),
	
	CURRENCY_FORMAT(new NumberFormatter("$#,##0.00", TextAlignment.RIGHT)),
	
	STRING_FORMAT(new StringFormatter(TextAlignment.LEFT)),
	STRING_CENTERED(new StringFormatter(TextAlignment.CENTER)),
	STRING_TRUNCATE(new StringTruncateFormatter(100)),
	STRING_ABBREVIATE(new StringAbbreviateFormatter(100)),
	;
	
	private ReportFormatter reportFormatter;
	
	DataFormats(ReportFormatter reportFormatter) {
		this.reportFormatter = reportFormatter;
	}
	
	public ReportFormatter formatter() {
		return reportFormatter;
	}
}
