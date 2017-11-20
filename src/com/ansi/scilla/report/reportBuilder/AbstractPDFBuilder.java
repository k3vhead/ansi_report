package com.ansi.scilla.report.reportBuilder;


/**
 * THis is a placeholder object to maintain the same class hierarchy as HTML Report Builders
 * and XLS Report Builders. When the PDF Reports are built, common methods (eg page headers)
 * will be moved to this class.
 * 
 * @author dclewis
 *
 */
public abstract class AbstractPDFBuilder extends ReportBuilder {

	private static final long serialVersionUID = 1L;

	public AbstractPDFBuilder(StandardReport report) {
		super(report);
	}

	public AbstractPDFBuilder(StandardSummaryReport report) {
		super(report);
	}

}
