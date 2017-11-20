package com.ansi.scilla.report.reportBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;


public class NumberFormatter extends ReportFormatter {
	
	private static final long serialVersionUID = 1L;
	
	private String formatString;
	private DecimalFormat formatter;
	
	public NumberFormatter(String formatString, TextAlignment textAlignment) {
		super();
		this.formatString = formatString;
		this.formatter = new DecimalFormat(this.formatString);
		this.textAlignment = textAlignment;
	}
	
	public String getFormatString() {
		return formatString;
	}

	public String format(Integer number) {
		return formatter.format(number);
	}
	public String format(int number) {
		return formatter.format(number);
	}
	public String format(Double number) {
		return formatter.format(number);
	}
	public String format(Float number) {
		return formatter.format(number);
	}
	public String format(double number) {
		return formatter.format(number);
	}
	public String format(float number) {
		return formatter.format(number);
	}
	public String format(BigDecimal number) {
		return formatter.format(number.doubleValue());
	}



}
