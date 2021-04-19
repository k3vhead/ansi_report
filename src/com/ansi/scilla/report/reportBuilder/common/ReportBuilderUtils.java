package com.ansi.scilla.report.reportBuilder.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.report.reportBuilder.formatter.DataFormats;
import com.ansi.scilla.report.reportBuilder.formatter.ReportFormatter;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;
import com.ansi.scilla.report.reportBuilder.reportType.StandardSummaryReport;

public class ReportBuilderUtils extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Figure out how many rows of titles and/or header data we have. There is always at least 
	 * a single row for the banner. Count is incremented for title and subtitle, and for each row
	 * of header data (right/left columns) beyond the banner/title/subtitle.
	 * @param report Any ansi report
	 * @return number of rows in the report page header
	 */
	public static int makeHeaderRowCount(AbstractReport report) {
		int headerRowCount = 1;  // we've always got a banner
		if ( ! StringUtils.isBlank(report.getTitle())) {
			headerRowCount++;
		}
		if ( ! StringUtils.isBlank(report.getSubtitle())) {
			headerRowCount++;
		}
		if ( report.getHeaderLeft() != null ) {
			for ( ReportHeaderCol col : report.getHeaderLeft()) {
				if ( col.getRowList().size() > headerRowCount ) {
					headerRowCount = col.getRowList().size(); 
				}
			}
		}
		if ( report.getHeaderRight() != null ) {
			for ( ReportHeaderCol col : report.getHeaderRight()) {
				if ( col.getRowList().size() > headerRowCount ) {
					headerRowCount = col.getRowList().size(); 
				}
			}
		}
		return headerRowCount;
	}
	
	
	/**
	 * Figure out how wide a summary report by looking at the included reports.
	 * @param report Any standard summary report
	 * @return the number of columns in the report
	 */
	public static Integer makeColumnCount(StandardSummaryReport report) {
		StandardSummaryReport myReport = (StandardSummaryReport)report;
		Integer companyColumnCount = myReport.hasCompanySummary() ? myReport.getCompanySummary().getHeaderRow().length : 0;
		Integer regionColumnCount = myReport.hasRegionSummary() ? myReport.getRegionSummary().getHeaderRow().length : 0;
		Integer divisionColumnCount = myReport.hasDivisionSummary() ? myReport.getDivisionSummary().getHeaderRow().length : 0;
		Integer colCount = Math.max(companyColumnCount, regionColumnCount) + divisionColumnCount;
		return colCount;
	}
	
	/**
	 * Format arbitrary value according to the standard report formats
	 * @param dataFormat	The function for formatting the data
	 * @param value	The data to be formatted
	 * @return	Formatted data
	 * @throws NoSuchMethodException The formatter doesn't have a correct formatting method
	 * @throws SecurityException Java reflection error - this shouldn't happen
	 * @throws IllegalAccessException Java reflection error - this shouldn't happen
	 * @throws IllegalArgumentException The formatting method doesn't accept java.lang.Object as a parm
	 * @throws InvocationTargetException Java reflection error - this shouldn't happen
	 */
	public static String formatValue(DataFormats dataFormat, Object value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ReportFormatter formatter = dataFormat.formatter();
		Method method = formatter.getClass().getMethod("format", new Class[] {value.getClass()});
		return (String)method.invoke(formatter, new Object[] {value});
	}
}
