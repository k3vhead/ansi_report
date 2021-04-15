package com.ansi.scilla.report.reportBuilder.reportInfo;

public enum ReportInfo {
	ACCOUNTS_RECEIVABLE("",false,false),
	AR_TOTALS("",false,false),
	AR_TOTALS_OVER_60_DETAIL("",false,false),
	AR_TOTALS_OVER_60_DETAIL_BY_DIVISION("",false,false),
	AR_TOTALS_SUMMARY("",false,false),
	AR_TOTALS_SUMMARY_BY_DIVISION("",false,false),
	ADDRESS_USAGE("",false,false),
	AGING_AR_TOTALS("",false,false),
	AGING_CASH_TOTALS("",false,false),
	AGING_INVOICE_TOTALS("",false,false),
	AGING_PAYMENT_TOTALS("",false,false),
	AGING_SERVICE_TAX_TOTALS("",false,false),
	CASH_RECEIPTS_REGISTER("",false,false),
	CLIENT_CONTACT("",false,false),
	CREDIT_CARD_FEE_DISTRIBUTION("",false,false),
	DISPATCHED_OUTSTANDING_TICKETS("",false,false),
	DRV30("",false,false),
	EXPIRING_DOCUMENT("",false,false),
	INVOICE_REGISTER("",false,false),
	INVOICE_REGISTER_SUMMARY("",false,false),
	JOB_SCHEDULE("",false,false),
	LIFT_GENIE("",false,false),
	MONTHLY_SERVICE_TAX_BY_DAY("",false,false),
	MONTHLY_SERVICE_TAX("",false,false),
	PAC("",false,false),
	PAC_MONTHLY("",false,false),
	PAC_QUARTERLY("",false,false),
	PAC_WEEKLY("",false,false),
	PAC_YTD("",false,false),
	PAST_DUE_45("",false,false),
	REPORT_SUBSCRIPTION_CHANGE("",false,false),
	QSS("",false,false),
	SERVICE_TAX("",false,false),
	SIX_MONTH_ROLLING_VOLUME("",false,false),
	SKIPPED_AND_DISPATCHED_COUNTS("",false,false),
	TICKET_STATUS_REPORT("",false,false),
	VOLUME_FORECAST("",false,false),
	VPP("",false,false),
	WO_AND_FEES("",false,false),
	;
	
	private String maker;
	private Boolean online;
	private Boolean canSubscribe;
	private ReportInfo(String maker, Boolean online, Boolean canSubscribe) {
		this.maker = maker;
		this.online = online;
		this.canSubscribe = canSubscribe;
	}
	public String maker() { return maker; }
	public Boolean online() { return online; }
	public Boolean canSubscribe() { return canSubscribe; }
	
	
	
}
