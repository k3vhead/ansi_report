package com.ansi.scilla.report.reportBuilder.reportType;


public abstract class CompoundReport extends AnsiReport {

	private static final long serialVersionUID = 1L;
	protected AbstractReport[] reports;

	
	public CompoundReport(AbstractReport[] reports) {
		this.reports = reports;
	}


	public AbstractReport[] getReports() {		
		return reports;
	}

}
