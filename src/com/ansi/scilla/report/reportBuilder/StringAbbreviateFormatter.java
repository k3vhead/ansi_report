package com.ansi.scilla.report.reportBuilder;

import org.apache.commons.lang3.StringUtils;


/**
 * Returns "abc def ... xyz" for strings longer than maxLength - 3
 * Else return unaltered string
 * 
 * @author dclewis
 *
 */
public class StringAbbreviateFormatter extends ReportFormatter {
	private static final long serialVersionUID = 1L;

	private Integer maxLength;
	
	public StringAbbreviateFormatter(Integer maxLength) {
		this.maxLength = maxLength;
		this.textAlignment = TextAlignment.LEFT;
	}

	public String format(String string) {
		String formattedString = string.length() > (maxLength-3) ? StringUtils.abbreviateMiddle(string, "...", maxLength) : string;		
		return formattedString;
	}

}
