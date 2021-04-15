package com.ansi.scilla.report.reportBuilder.reportType;

import java.util.Calendar;

import com.ansi.scilla.common.ApplicationObject;
import com.ansi.scilla.common.db.Division;
import com.ansi.scilla.report.reportBuilder.reportBy.ReportBy;

/**
 * Absolute parent of all report types. (Mostly here to make casting/reflection process easier)
 * 
 * @author dclewis
 *
 */
public abstract class AnsiReport extends ApplicationObject implements ReportBy {

	private static final long serialVersionUID = 1L;

	public abstract String makeFileName(Calendar runDate, Division division, Calendar startDate, Calendar endDate);
}
