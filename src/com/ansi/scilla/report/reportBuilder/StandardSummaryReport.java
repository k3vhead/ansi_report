package com.ansi.scilla.report.reportBuilder;

import java.util.List;

import com.ansi.scilla.report.reportBuilder.common.ReportOrientation;


public abstract class StandardSummaryReport extends AbstractReport {

	private static final long serialVersionUID = 1L;

	protected StandardReport divisionSummary;
	protected StandardReport regionSummary;
	protected StandardReport companySummary;
	
	public StandardSummaryReport(StandardReport divisionSummary, StandardReport regionSummary,
			StandardReport companySummary) {
		super();
		setReportOrientation(ReportOrientation.PORTRAIT);
		this.divisionSummary = divisionSummary;
		this.regionSummary = regionSummary;
		this.companySummary = companySummary;
	}

	
	@Override
	public Integer getReportWidth() {
		Integer divisionWidth = divisionSummary == null ? 0 : divisionSummary.getReportWidth();
		Integer regionWidth = regionSummary == null ? 0 : regionSummary.getReportWidth();
		Integer companyWidth = companySummary == null ? 0 : companySummary.getReportWidth();
		
		return Math.max(divisionWidth, regionWidth) + companyWidth;
	}


	public StandardReport getDivisionSummary() {
		return divisionSummary;
	}

	public StandardReport getRegionSummary() {
		return regionSummary;
	}

	public StandardReport getCompanySummary() {
		return companySummary;
	}

	public boolean hasDivisionSummary() {
		return this.divisionSummary != null;
	}
	public boolean hasRegionSummary() {
		return this.regionSummary != null;
	}
	public boolean hasCompanySummary() {
		return this.companySummary != null;
	}
	
	
	
	public List<Object> getDivisionData() {
		return this.divisionSummary.getDataRows();
	}
	
	public List<Object> getRegionData() {
		return this.regionSummary.getDataRows();
	}

	public List<Object> getCompanyData() {
		return this.companySummary.getDataRows();
	}

	
	
	public String getDivisionTitle() {
		return this.divisionSummary.getTitle();
	}
	
	public String getRegionTitle() {
		return this.regionSummary.getTitle();
	}

	public String getCompanyTitle() {
		return this.companySummary.getTitle();
	}
	
	
	
	public String getDivisionSubtitle() {
		return this.divisionSummary.getSubtitle();
	}
	
	public String getRegionSubtitle() {
		return this.regionSummary.getSubtitle();
	}

	public String getCompanySubtitle() {
		return this.companySummary.getSubtitle();
	}

	
}
