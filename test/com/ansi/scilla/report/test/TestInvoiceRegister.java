package com.ansi.scilla.report.test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.common.report.invoiceRegisterReport.InvoiceRegisterReport;
import com.ansi.scilla.common.utils.AppUtils;

public class TestInvoiceRegister {

	public static void main(String[] args) {
		try {
			new TestInvoiceRegister().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void go() throws Exception {
		Connection conn = null;
		Integer divisionId = 100;
		Calendar startDate = new GregorianCalendar(2017, Calendar.JUNE, 12);
		try {
			conn = AppUtils.getProdConn();						
			XSSFWorkbook workbook = InvoiceRegisterReport.makeReport(conn, divisionId, startDate);
			//workbook.write(new FileOutputStream("/home/dclewis/Documents/projects/ANSI_Scheduling/testResult/invoiceRegister.xlsx"));
			workbook.write(new FileOutputStream("InvoiceRegisterTest.xlsx"));
		} finally {
			if(conn !=null){
				conn.close();
			}
		}
	}

}
