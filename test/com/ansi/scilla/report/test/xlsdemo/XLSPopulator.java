package com.ansi.scilla.report.test.xlsdemo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.ansi.scilla.common.AnsiTime;

public class XLSPopulator {

	public void populateSheet(XSSFSheet sheet) {
		XSSFRow row = sheet.createRow(0); 
		XSSFCell cell = row.createCell(0);
		
		Calendar now = Calendar.getInstance(new AnsiTime());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.S");
		
		cell.setCellValue(sdf.format(now.getTime()));
	}

}
