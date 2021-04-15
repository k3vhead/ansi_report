package com.ansi.scilla.report.reportBuilder.formatter;

import org.apache.commons.lang3.StringUtils;


/**
 * Returns "abc def ..." for strings longer than maxLength - 3
 * Else return unaltered string
 * 
 * @author dclewis
 *
 */
public class StringTruncateFormatter extends ReportFormatter {
	private static final long serialVersionUID = 1L;

	private Integer maxLength;
	
	public StringTruncateFormatter(Integer maxLength) {
		this.maxLength = maxLength;
		this.textAlignment = TextAlignment.LEFT;
	}

	public String format(String string) {
		String formattedString = string.length() > (maxLength-3) ? StringUtils.abbreviate(string, maxLength) : string;		
		return formattedString;
	}
	
}
