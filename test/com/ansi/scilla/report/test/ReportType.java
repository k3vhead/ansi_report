package com.ansi.scilla.report.test;

public enum ReportType {
	/*
	 This is copied from struts-config.  They are the valid options for the destination JSP
    <forward name="reportByDivEnd" path="/reportByDivEnd.jsp" />
    <forward name="reportByDivMonthYear" path="/reportByDivMonthYear.jsp" />
    <forward name="reportByDivStartEnd" path="/reportByDivStartEnd.jsp" />
    <forward name="reportByStartEnd" path="/reportByStartEnd.jsp" />     
	*/
	
	CASH_RECEIPTS_REGISTER(
			"reportByStartEnd", 
			"com.ansi.scilla.common.report.cashReceiptsRegister.CashReceiptsRegisterReport",
			"com.ansi.scilla.web.request.report.ValidateStartEnd", 
			new String[] {"startDate", "endDate"}
		),
	INVOICE_REGISTER_REPORT(
			"reportByDivMonthYear",
			"com.ansi.scilla.common.report.invoiceRegisterReport.InvoiceRegisterReport",
			"com.ansi.scilla.web.request.report.ValidateDivMonthYear", 
			new String[] {"divisionId", "month", "year"}
		),
	PAC_REPORT(
			"reportByDivStartEnd", 
			"com.ansi.scilla.common.report.pac.PacReport",
			"com.ansi.scilla.web.request.report.ValidateDivStartEnd", 
			new String[] {"divisionId", "startDate", "endDate"}
		),
	SIX_MONTH_ROLLING_VOLUME_REPORT(
			"reportByDivMonthYear", 
			"com.ansi.scilla.common.report.sixMonthRollingVolume.SixMonthRollingVolumeReport",
			"com.ansi.scilla.web.request.report.ValidateDivMonthYear",  
			new String[] {"divisionId", "month", "year"}
		),
	TICKET_STATUS_REPORT(
			"reportByDivStartEnd", 
			"com.ansi.scilla.common.report.ticketStatus.TicketStatusReport",
			"com.ansi.scilla.web.request.report.ValidateDivStartEnd", 
			new String[] {"divisionId", "startDate", "endDate"}
		),
	TEST_MY_REPORT(
			"reportByDivStartEnd", 
			"com.ansi.scilla.common.report.testMyReport.TestMyReport",
			"com.ansi.scilla.web.request.report.ValidateDivStartEnd", 
			new String[] {"divisionId", "startDate", "endDate"}
		);
		
	private final String jsp;
	private final String reportClassName;
	private final String validatorClassName;
	private final String[] builderParms;
	
	ReportType(String jsp, String reportClassName, String validatorClassName, String[] builderParms) {
		this.jsp = jsp;
		this.reportClassName = reportClassName;
		this.validatorClassName = validatorClassName;
		this.builderParms = builderParms;
	}
	
	public String jsp() {
		return this.jsp;
	}
	
	public String reportClassName() {
		return this.reportClassName;
	}
	
	public String validatorClassName() {
		return this.validatorClassName;
	}
	
	public String[] builderParms() {
		return this.builderParms;
	}
}
