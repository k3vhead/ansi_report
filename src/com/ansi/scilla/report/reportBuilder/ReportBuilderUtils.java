package com.ansi.scilla.report.reportBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.common.ApplicationObject;

public class ReportBuilderUtils extends ApplicationObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Figure out how many rows of titles and/or header data we have. There is always at least 
	 * a single row for the banner. Count is incremented for title and subtitle, and for each row
	 * of header data (right/left columns) beyond the banner/title/subtitle.
	 * @return
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
	 * @param report
	 * @return
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
	 * @param dataFormat
	 * @param value
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static String formatValue(DataFormats dataFormat, Object value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ReportFormatter formatter = dataFormat.formatter();
		Method method = formatter.getClass().getMethod("format", new Class[] {value.getClass()});
		return (String)method.invoke(formatter, new Object[] {value});
	}
}
