package com.ansi.scilla.report.common;

import org.apache.commons.lang3.StringUtils;

import com.ansi.scilla.report.reportBuilder.common.ReportHeaderCol;
import com.ansi.scilla.report.reportBuilder.reportType.AbstractReport;

public class ReportUtils {

	public static Integer makeHeaderRowCount(AbstractReport report) {
		int headerRowCount = 1;  // we've always got a banner
		if ( ! StringUtils.isBlank(report.getTitle())) {
			headerRowCount++;
		}
		if ( ! StringUtils.isBlank(report.getSubtitle())) {
			headerRowCount++;
		}
		for ( ReportHeaderCol col : report.getHeaderLeft()) {
			if ( col.getRowList().size() > headerRowCount ) {
				headerRowCount = col.getRowList().size(); 
			}
		}
		for ( ReportHeaderCol col : report.getHeaderRight()) {
			if ( col.getRowList().size() > headerRowCount ) {
				headerRowCount = col.getRowList().size(); 
			}
		}
		return headerRowCount;
	}

	
	
}
