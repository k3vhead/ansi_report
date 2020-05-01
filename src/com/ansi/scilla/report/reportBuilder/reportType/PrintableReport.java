/**
 * 
 */
package com.ansi.scilla.report.reportBuilder;

import java.util.Calendar;
import java.util.Date;

import com.ansi.scilla.common.AnsiTime;

/**
 * A common place to hold print-only values and methods. (ie. for XLS and PDF builders)
 *
 */
public abstract class PrintableReport extends ReportBuilder {

	private static final long serialVersionUID = 1L;

	/**
	 * Standard Margins for paper-based reports
	 */
	public static final Double marginTopDefault = 0.25D;
	public static final Double marginBottomDefault = 0.5D; // need room for the page number in the footer
	public static final Double marginLeftDefault = 0.25D;
	public static final Double marginRightDefault = 0.25D;
	
	
	protected Double marginTop = marginTopDefault;
	protected Double marginBottom = marginBottomDefault;
	protected Double marginLeft = marginLeftDefault;
	protected Double marginRight = marginRightDefault;
	protected Date runDate;
	protected Date startDate;
	protected Date endDate;
	
	
	protected PrintableReport(StandardReport report) {
		super(report);
	}
	protected PrintableReport(StandardSummaryReport report) {
		super(report);
	}
	
	protected void makeHeaderDates() {
		Calendar calendar = Calendar.getInstance(new AnsiTime());
		runDate = calendar.getTime();
		calendar.clear();
		//calendar.set(year, month, 1);
		startDate = calendar.getTime();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getMaximum(Calendar.DAY_OF_MONTH));
		endDate = calendar.getTime();
	}
	
	
	public Double getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(Double marginTop) {
		this.marginTop = marginTop;
	}

	public Double getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(Double marginBottom) {
		this.marginBottom = marginBottom;
	}

	public Double getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(Double marginLeft) {
		this.marginLeft = marginLeft;
	}

	public Double getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(Double marginRight) {
		this.marginRight = marginRight;
	}
}
