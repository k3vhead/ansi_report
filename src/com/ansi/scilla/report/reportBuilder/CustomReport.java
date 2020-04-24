package com.ansi.scilla.report.reportBuilder;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * A Custom report cannot use the standard builders (because it's custom, not standard).
 * So each custom report must implement its own builders, either to create HTML, an 
 * XLS workbook, or to add itself to an existing workbook
 *
 * @author dclewis
 *
 */
public abstract class CustomReport extends AbstractReport {

	private static final long serialVersionUID = 1L;
	

	public abstract XSSFWorkbook makeXLS() throws Exception;
	public abstract void add2XLS(XSSFWorkbook workbook) throws Exception;
	public abstract String makeHTML() throws Exception;
	
}
