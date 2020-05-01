package com.ansi.scilla.report.reportBuilder.htmlBuilder;

import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;

public class HTMLSummaryBuilder extends AbstractHTMLBuilder {

	private static final long serialVersionUID = 1L;

	

	private HTMLSummaryBuilder(StandardSummaryReport report) {
		super(report);
	}
	
	private String buildReport() throws Exception {
		StandardSummaryReport report = (StandardSummaryReport)this.report;
		String companyReport = report.hasCompanySummary()   ? HTMLBuilder.build(report.getCompanySummary())  : ""; 
		String regionReport = report.hasRegionSummary()     ? HTMLBuilder.build(report.getRegionSummary())   : "";
		String divisionReport = report.hasDivisionSummary() ? HTMLBuilder.build(report.getDivisionSummary()) : "";
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table class=\"" + HTMLReportFormatter.CSS_TABLE + ' ' + HTMLReportFormatter.CSS_HEADER_TABLE + "\">");
		buffer.append(makeHeader());
		buffer.append("\n</table>\n");
		buffer.append("<div style=\"clear:both;\"> <!-- make a place for the reports -->\n");

		if ( report.hasCompanySummary() || report.hasRegionSummary() ) {
			// the following is the left column
			buffer.append("<div style=\"float:left;\"> <!-- make a place for company and/or region reports -->\n");
			if ( report.hasCompanySummary() ) {
				buffer.append("<div style=\"clear:both;\"> <!-- company Summary -->\n");
				buffer.append(companyReport);
				buffer.append("\n");
				buffer.append("</div> <!-- end of company -->\n");
			}
			if ( report.hasRegionSummary() ) {
				buffer.append("<div style=\"clear:both; margin-top:20px;\"> <!-- Region Summary -->\n");
				buffer.append(regionReport);
				buffer.append("\n");
				buffer.append("</div>   <!-- end of region -->\n");
			}
			buffer.append("</div> <!-- end of left column -->\n");
			
			if ( report.hasDivisionSummary() ) {
				// the following is the right column
				buffer.append("<div style=\"float:right;\"> <!-- division Summary -->\n");
				buffer.append(divisionReport);
				buffer.append("\n");
				buffer.append("</div> <!-- end of division -->\n");
			}
		} else {
			// no company or region summary, so we put the division summary here
			if ( report.hasDivisionSummary() ) {
				buffer.append("<div style=\"clear:both;\"> <!-- Division Summary -->\n");
				buffer.append(divisionReport);
				buffer.append("\n");
				buffer.append("</div>   <!-- end of Division -->\n");
			} else {
				// this shouldn't happen. But "shouldn't" is frequently the kiss of death
				buffer.append("<div style=\"clear:both;\"> <!-- Division Summary -->\n");
				buffer.append("No summaries defined\n");
				buffer.append("</div>   <!-- end of Division -->\n");
			}
		}
		
		buffer.append("</div> <!-- end of reports -->");
		return buffer.toString();
	}

	

	

	
	public static String build(StandardSummaryReport report) throws Exception {	
		HTMLSummaryBuilder builder = new HTMLSummaryBuilder(report);
		return builder.buildReport();
	}
}
