package com.ansi.scilla.report.reportBuilder.htmlBuilder;

import java.util.ArrayList;
import java.util.List;

import com.ansi.scilla.report.reportBuilder.StandardReport;
import com.ansi.scilla.report.reportBuilder.common.ColumnHeader;
import com.ansi.scilla.report.reportBuilder.common.NoPreviousValue;
import com.ansi.scilla.report.reportBuilder.common.SummaryType;
import com.thewebthing.commons.lang.StringUtils;


public class HTMLBuilder extends AbstractHTMLBuilder {

	private static final long serialVersionUID = 1L;

	
	private HTMLBuilder(StandardReport report) {
		super(report);
	}
	
	private String buildReport() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table class=\"" + HTMLReportFormatter.CSS_TABLE + ' ' + HTMLReportFormatter.CSS_HEADER_TABLE + "\">");
		buffer.append(makeHeader());
		buffer.append("\n</table>");
		buffer.append("<table class=\"" + HTMLReportFormatter.CSS_TABLE + ' ' + HTMLReportFormatter.CSS_DATA_TABLE + "\">");
		buffer.append(makeColumnHeader((StandardReport)this.report));		
		buffer.append(makeDetails((StandardReport)this.report));
		buffer.append(makeFinalSubtotal((StandardReport)this.report));
		buffer.append(makeSummary((StandardReport)this.report));
		buffer.append("\n</table>");
		return buffer.toString();
	}




	private String makeColumnHeader(StandardReport report) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\n<tr class=\"" + HTMLReportFormatter.CSS_HEADER_ROW + "\">");
		for ( ColumnHeader columnHeader : report.getHeaderRow() ) {
			buffer.append("\n\t<th class=\"" + HTMLReportFormatter.CSS_COLHDR + "\">");
			buffer.append("<span class=\"" + HTMLReportFormatter.CSS_COLHDR_TEXT + "\">");
			buffer.append(columnHeader.getLabel());
			buffer.append("</span>");
			buffer.append("</th>");
		}
		buffer.append("\n</tr>");
		return buffer.toString();
	}

	private String makeDetails(StandardReport report) throws Exception {
		StringBuffer buffer = new StringBuffer();
		for ( Object row : report.getDataRows() ) {
			makeSubtotal(report, row, buffer);
			
			buffer.append("\n<tr class=\"" + HTMLReportFormatter.CSS_DATA_ROW + "\">");
			
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				ColumnHeader columnHeader = report.getHeaderRow()[i];				
				Object value = makeDisplayData(columnHeader,row);
				String display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
				super.doSummaries(columnHeader, value);
				buffer.append("\n\t<td class=\"ansi-stdrpt-column-" + i + "\">");
				buffer.append("<span class=\"ansi-stdrpt-column-text-" + i + "\">");
				buffer.append(display);
				buffer.append("</span>");
				buffer.append("</td>");
			}
			buffer.append("\n</tr>\n");
		}
		
		
		return buffer.toString();
	}


	
	
	private void makeSubtotal(StandardReport report, Object row, StringBuffer buffer) throws Exception {
		List<String> fieldsToDisplay = new ArrayList<String>();
		List<String> fieldsThatChanged = new ArrayList<String>();
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			String fieldName = columnHeader.getFieldName();
			if (this.previousValues.containsKey(columnHeader.getFieldName())) {
				// we need to check for changed values because this field is a trigger for a subtotal
				Object previousValue = this.previousValues.get(fieldName);
				Object newValue = makeDisplayData(columnHeader,row);
				if ( ! previousValue.equals(new NoPreviousValue()) && ! previousValue.equals(newValue)) {
					// we have a value change, so add a subtotal row
					fieldsThatChanged.add(fieldName);
				}
				this.previousValues.put(fieldName,  newValue);
			}			
		}
		
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			String fieldName = columnHeader.getFieldName();
			if ( fieldsThatChanged.contains(columnHeader.getSubTotalTrigger())) {
				fieldsToDisplay.add(fieldName);
			}
		}
		
		if ( ! fieldsToDisplay.isEmpty() ) {
			buffer.append("<tr class=\""+HTMLReportFormatter.CSS_SUBTOTAL_ROW+"\">\n");
			for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
				buffer.append("<td class=\""+HTMLReportFormatter.CSS_SUBTOTAL+"\">");
				ColumnHeader columnHeader = report.getHeaderRow()[i];
				String fieldName = columnHeader.getFieldName();
				if ( fieldsToDisplay.contains(fieldName)) {
					String subtotal = super.makeSubtotalData(columnHeader);
					buffer.append("<span class=\""+HTMLReportFormatter.CSS_SUBTOTAL_TEXT+"\">");
					buffer.append(subtotal);
					buffer.append("</span>");
				} else {
					buffer.append("&nbsp;");
				}
				buffer.append("</td>\n");
			}
			buffer.append("</tr>\n");
		}
	}

	private String makeFinalSubtotal(StandardReport report) throws Exception {
		StringBuffer buffer = new StringBuffer();
		boolean addASub = false;
		buffer.append("<tr class=\""+HTMLReportFormatter.CSS_SUBTOTAL_ROW+"\">\n");
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];	
			buffer.append("<td class=\""+HTMLReportFormatter.CSS_SUBTOTAL+"\">");
			if ( StringUtils.isBlank(columnHeader.getSubTotalTrigger())) {
				buffer.append("&nbsp;");
			} else {
				// we're doing a subtotal for this field				
				addASub = true;
				String subtotal = super.makeSubtotalData(columnHeader);
				buffer.append("<span class=\""+HTMLReportFormatter.CSS_SUBTOTAL_TEXT+"\">");
				buffer.append(subtotal);
				buffer.append("</span>");
			}
			buffer.append("</td>\n");
					
		}
		buffer.append("</tr>\n");
		return addASub ? buffer.toString() : "";
	}

	private String makeSummary(StandardReport report) throws Exception {
		boolean addASummary = false;		
		StringBuffer buffer = new StringBuffer();
		buffer.append("\n<tr class=\""+HTMLReportFormatter.CSS_SUMMARY_ROW+"\">");
		for ( int i = 0; i < report.getHeaderRow().length; i++ ) {
			ColumnHeader columnHeader = report.getHeaderRow()[i];
			buffer.append("\n\t<td class=\"" + HTMLReportFormatter.CSS_SUMMARY + " ansi-stdrpt-column-" + i + "\">");
			buffer.append("<span class=\"" + HTMLReportFormatter.CSS_SUMMARY_TEXT + " ansi-stdrpt-column-text-" + i + "\">");
			if ( columnHeader.getSummaryType().equals(SummaryType.NONE)) {
				buffer.append("&nbsp;");
			} else {
				addASummary = true;
				buffer.append(makeSummaryData(columnHeader));
			}
			buffer.append("</span>");
			buffer.append("</td>");
		}
		buffer.append("\n</tr>");
		return addASummary ? buffer.toString() : "";  // only return HTML if we have a summary row
	}

	

	
	public static String build(StandardReport report) throws Exception {	
		HTMLBuilder builder = new HTMLBuilder(report);
		return builder.buildReport();
	}
}
