package com.ansi.scilla.report.test.itext5;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.Connection;

import com.ansi.scilla.common.utils.AppUtils;
import com.ansi.scilla.report.cashReceiptsRegister.CashReceiptsRegisterCompanySummary;
import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFBuilder;

public class TestCRRSummary {

	
	public void go() throws Exception {
		Connection conn = null;
		String fileName =  "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/crrCompanySummary.pdf";
		try {
			conn = AppUtils.getDevConn();
			CashReceiptsRegisterCompanySummary report = CashReceiptsRegisterCompanySummary.buildReport(conn);
			ByteArrayOutputStream baos = PDFBuilder.build(report);
			baos.writeTo(new FileOutputStream(fileName));
		} finally {
			AppUtils.closeQuiet(conn);
		}
	}
	
	
	public static void main(String[] args) {
		try {
			new TestCRRSummary().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
