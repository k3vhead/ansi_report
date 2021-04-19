package com.ansi.scilla.report.reportBuilder.reportType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.AnsiTime;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderCol;
import com.ansi.scilla.report.reportBuilder.common.ReportHeaderRow;
import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;
import com.ansi.scilla.report.reportBuilder.formatter.ReportPageLayout;
import com.thewebthing.commons.lang.StringUtils;

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
	
	private Logger logger = LogManager.getLogger(this.getClass());
	
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

	
	public abstract Integer getReportWidth();
	
	/**
	 * Figure out how many rows are in the report banner.
	 * 
	 * @return nuber of rows in the banner
	 */
	public Integer getReportHeight() {
		Integer bannerHeight = StringUtils.isBlank(banner) ? 0 : 1;
		Integer titleHeight = StringUtils.isBlank(title) ? 0 : 1;
		Integer subTitle = StringUtils.isBlank(subtitle) ? 0 : 1;
		Integer noteHeight = StringUtils.isBlank(headerNotes) ? 0 : 1;
		Integer pageBannerHeight = bannerHeight + titleHeight + subTitle + noteHeight;
		
		Integer headerRightSize = headerRight == null ? 0 : makeHeaderDataSize(headerRight);
		Integer headerLeftSize = headerLeft == null ? 0 : makeHeaderDataSize(headerLeft);;	
		
		List<Integer> testList = Arrays.asList(new Integer[] {pageBannerHeight, headerRightSize, headerLeftSize});
		Collections.sort(testList);
		Integer headerHeight = testList.get(testList.size()-1);  // get length of longest header column
		
		
		
		Integer footerRightSize = footerRight == null ? 0 : makeHeaderDataSize(footerRight);
		Integer footerLeftSize = footerLeft == null ? 0 : makeHeaderDataSize(footerLeft);
		Integer footerDataHeight = Math.max(footerRightSize, footerLeftSize);

		Integer reportHeight = headerHeight + footerDataHeight;		
		return reportHeight;
	}

	private int makeHeaderDataSize(List<ReportHeaderCol> headerColumnList) {
		Integer maxSize = 0;
		for ( ReportHeaderCol reportHeaderCol : headerColumnList ) {
			if (reportHeaderCol.size() > maxSize ) {
				maxSize = reportHeaderCol.size();
			}
		}
		return maxSize;
	}

	/**
	 * In many (most?) cases, there will only be a single column of header data to be
	 * displayed to the side of the banner.
	 * @param headerLeft Label/Data for data on the left side of the banner
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
	 * @param headerRight Label/Data for data on the right side of the banner
	 */
	public void makeHeaderRight(List<ReportHeaderRow> headerRight) {
		List<ReportHeaderCol> headerRightCols = new ArrayList<ReportHeaderCol>();
		ReportHeaderCol col = new ReportHeaderCol(headerRight, 0);
		headerRightCols.add(col);
		setHeaderRight(headerRightCols);
	}

	

}
