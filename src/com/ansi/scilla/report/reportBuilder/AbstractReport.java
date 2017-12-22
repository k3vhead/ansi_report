package com.ansi.scilla.report.reportBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.ansi.scilla.common.AnsiTime;

public abstract class AbstractReport extends AnsiReport {

	private static final long serialVersionUID = 1L;

	protected final String bannerText = "American National Skyline, Inc.";
	
	private String banner;
	private String title;
	private String subtitle;	
	private List<ReportHeaderCol> headerRight;
	private List<ReportHeaderCol> headerLeft;
	private List<ReportHeaderCol> footerRight;
	private List<ReportHeaderCol> footerLeft;
	
	protected String headerNotes;
	protected ReportPageLayout reportPageLayout;
	protected Calendar runDate;
	protected ReportOrientation reportOrientation = ReportOrientation.LANDSCAPE;
	
	public AbstractReport() {
		super();
		this.runDate = Calendar.getInstance(new AnsiTime());
		this.banner = bannerText;
	}
	
	public String getBanner() {
		return banner;
	}
	public void setBanner(String banner) {
		this.banner = banner;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public List<ReportHeaderCol> getHeaderRight() {
		return headerRight;
	}
	public void setHeaderRight(List<ReportHeaderCol> headerRight) {
		this.headerRight = headerRight;
	}
	public List<ReportHeaderCol> getHeaderLeft() {
		return headerLeft;
	}
	public void setHeaderLeft(List<ReportHeaderCol> headerLeft) {
		this.headerLeft = headerLeft;
	}
	public List<ReportHeaderCol> getFooterRight() {
		return footerRight;
	}
	public void setFooterRight(List<ReportHeaderCol> footerRight) {
		this.footerRight = footerRight;
	}
	public List<ReportHeaderCol> getFooterLeft() {
		return footerLeft;
	}
	public void setFooterLeft(List<ReportHeaderCol> footerLeft) {
		this.footerLeft = footerLeft;
	}

	public String getHeaderNotes() {
		return headerNotes;
	}
	public void setHeaderNotes(String headerNotes) {
		this.headerNotes = headerNotes;
	}
	public ReportPageLayout getReportPageLayout() {
		return reportPageLayout;
	}
	public void setReportPageLayout(ReportPageLayout reportPageLayout) {
		this.reportPageLayout = reportPageLayout;
	}
	public Calendar getRunDate() {
		return runDate;
	}
	public void setRunDate(Calendar runDate) {
		this.runDate = runDate;
	}

	
	public ReportOrientation getReportOrientation() {
		return reportOrientation;
	}

	public void setReportOrientation(ReportOrientation reportOrientation) {
		this.reportOrientation = reportOrientation;
	}

	
	/**
	 * In many (most?) cases, there will only be a single column of header data to be
	 * displayed to the side of the banner.
	 * @param headerLeft
	 */
	public void makeHeaderLeft(List<ReportHeaderRow> headerLeft) {
		List<ReportHeaderCol> headerLeftCols = new ArrayList<ReportHeaderCol>();
		ReportHeaderCol col = new ReportHeaderCol(headerLeft, 0);
		headerLeftCols.add(col);
		setHeaderLeft(headerLeftCols);
	}
	/**
	 * In many (most?) cases, there will only be a single column of header data to be
	 * displayed to the side of the banner.
	 * @param headerLeft
	 */
	public void makeHeaderRight(List<ReportHeaderRow> headerRight) {
		List<ReportHeaderCol> headerRightCols = new ArrayList<ReportHeaderCol>();
		ReportHeaderCol col = new ReportHeaderCol(headerRight, 0);
		headerRightCols.add(col);
		setHeaderRight(headerRightCols);
	}

	

}
