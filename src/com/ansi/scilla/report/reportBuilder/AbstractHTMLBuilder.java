package com.ansi.scilla.report.reportBuilder;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class AbstractHTMLBuilder extends ReportBuilder {

	private static final long serialVersionUID = 1L;

	protected static final HashMap<String, String> cssHeaderLeft;
	protected static final HashMap<String, String> cssHeaderRight;
	
	static {
		cssHeaderLeft = new HashMap<String, String>();
		cssHeaderLeft.put("tdLabel", HTMLReportFormatter.CSS_DATA_HEADER_LEFT);
		cssHeaderLeft.put("tdData", HTMLReportFormatter.CSS_DATA_LEFT);
		cssHeaderLeft.put("spanLabel", HTMLReportFormatter.CSS_DATA_HEADER_LEFT_TEXT);
		cssHeaderLeft.put("spanData", HTMLReportFormatter.CSS_DATA_LEFT_TEXT);
		
		cssHeaderRight = new HashMap<String, String>();
		cssHeaderRight.put("tdLabel", HTMLReportFormatter.CSS_DATA_HEADER_RIGHT);
		cssHeaderRight.put("tdData", HTMLReportFormatter.CSS_DATA_RIGHT);
		cssHeaderRight.put("spanLabel", HTMLReportFormatter.CSS_DATA_HEADER_RIGHT_TEXT);
		cssHeaderRight.put("spanData", HTMLReportFormatter.CSS_DATA_RIGHT_TEXT);
	}
	
	public AbstractHTMLBuilder(StandardReport report) {
		super(report);
	}

	public AbstractHTMLBuilder(StandardSummaryReport report) {
		super(report);
	}

	
	protected String makeHeader() throws Exception {
		Logger logger = LogManager.getLogger(this.getClass());
		StringBuffer buffer = new StringBuffer();
//		int headerRowCount = makeHeaderRowCount();
		int headerRowCount = ReportBuilderUtils.makeHeaderRowCount(this.getReport());
		logger.log(Level.DEBUG, "headerRowCount: " + headerRowCount);
		
		buffer.append(makeHeaderRow(0, report.getHeaderLeft(), report.getBanner(), report.getHeaderRight(), HTMLReportFormatter.CSS_BANNER, HTMLReportFormatter.CSS_BANNER_TEXT));
		if ( headerRowCount > 1 ) {
			logger.log(Level.DEBUG, "row 1");
			buffer.append(makeHeaderRow(1, report.getHeaderLeft(), report.getTitle(), report.getHeaderRight(), HTMLReportFormatter.CSS_TITLE, HTMLReportFormatter.CSS_TITLE_TEXT));	
		}
		if ( headerRowCount > 2 ) {
			logger.log(Level.DEBUG, "row 2");
			buffer.append(makeHeaderRow(2, report.getHeaderLeft(), report.getSubtitle(), report.getHeaderRight(), HTMLReportFormatter.CSS_SUBTITLE, HTMLReportFormatter.CSS_SUBTITLE_TEXT));
		}
		if ( headerRowCount > 3 ) {
			logger.log(Level.DEBUG, "row 3");
			for ( int i=3;i<headerRowCount;i++) {
				buffer.append(makeHeaderRow(i, report.getHeaderLeft(), "", report.getHeaderRight(), HTMLReportFormatter.CSS_EMPTY_HEADER, HTMLReportFormatter.CSS_EMPTY_HEADER_TEXT));
			}
		}
		
		if ( ! StringUtils.isBlank(report.getHeaderNotes())) {			
			Integer colspan = (2 * report.getHeaderLeft().size()) + (2 * report.getHeaderRight().size()) + 1;
			buffer.append("<tr>\n");
			buffer.append("<td class=\"" + HTMLReportFormatter.CSS_NOTES + "\" colspan=\""+ colspan + "\">");
			buffer.append("<span class=\"" + HTMLReportFormatter.CSS_NOTES_TEXT + "\">");
			buffer.append(report.getHeaderNotes());
			buffer.append("</span>");
			buffer.append("</td>");
		}
		
		return buffer.toString();
	}


	protected String makeHeaderRow(Integer index, List<ReportHeaderCol> headerLeft, String text, List<ReportHeaderCol> headerRight,String css, String cssText) throws Exception {
		StringBuffer buffer = new StringBuffer();
		String dataLeft = makeHeaderData(headerLeft, index, cssHeaderLeft);
		String dataRight = makeHeaderData(headerRight, index, cssHeaderRight);
		
		buffer.append("\n<tr class=\"" + HTMLReportFormatter.CSS_HEADER_ROW + "\">");
		buffer.append(dataLeft);
		buffer.append("\n\t<td class=\""+ css + "\">");
		buffer.append("<span class=\""+ cssText + "\">");
		buffer.append(text);
		buffer.append("</span>");
		buffer.append("</td>");
		buffer.append(dataRight);
		buffer.append("\n</tr>");
		return buffer.toString();
	}

	protected String makeHeaderData(List<ReportHeaderCol> headerData, Integer index, HashMap<String,String> cssheader) throws Exception {
		StringBuffer buffer = new StringBuffer();
		if ( ! headerData.isEmpty() ) {
			for ( ReportHeaderCol col : headerData ) {
				if ( index < col.getRowList().size() ) {
					ReportHeaderRow row = col.getRowList().get(index);
					
					Object value = row.getValue().invoke(this.report, (Object[])null); 
					String display = formatValue(row.getFormatter(), value);
					
					buffer.append("\n\t<td class=\"" +  cssheader.get("tdLabel") + "\">");
					buffer.append("<span class=\"" +  cssheader.get("spanLabel") + "\">");
					buffer.append(row.getLabel());
					buffer.append("</span>");
					buffer.append("</td>");
					buffer.append("\n\t<td class=\"" +  cssheader.get("tdData") + "\">");
					buffer.append("<span class=\"" +  cssheader.get("spanData") + "\">");
					buffer.append(display);
					buffer.append("</span>");
					buffer.append("</td>");
				} else {
					buffer.append("\n\t<td class=\"" +  cssheader.get("tdLabel") + "\">");
					buffer.append("<span class=\"" +  cssheader.get("spanLabel") + "\">");
					buffer.append("&nbsp;");
					buffer.append("</span>");
					buffer.append("</td>");
					buffer.append("\n\t<td class=\"" +  cssheader.get("tdData") + "\">");
					buffer.append("<span class=\"" +  cssheader.get("spanData") + "\">");
					buffer.append("&nbsp");
					buffer.append("</span>");
					buffer.append("</td>");
				}
			}
			
		}
		return buffer.toString();
	}

}
