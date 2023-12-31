package com.ansi.scilla.report.test;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestTextWrap {

	
	public void go() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();
		XSSFRow row = sheet.createRow(0);
		row.setHeight((short)300);
		
		XSSFCell cell1 = row.createCell(0);
		sheet.setColumnWidth(0, 6000);
		CellStyle style1 = workbook.createCellStyle();
		style1.setWrapText(false);
		cell1.setCellValue("four score and seven years ago our fathers");
		cell1.setCellStyle(style1);
		System.out.println(row.getHeight());
		
		XSSFCell cell2 = row.createCell(1);
		sheet.setColumnWidth(1, 6000);
		CellStyle style2 = workbook.createCellStyle();
		style2.setWrapText(true);		
		cell2.setCellValue("when in the course of human events it becomes necessary");
		cell2.setCellStyle(style2);
		System.out.println(row.getHeight());
		
		XSSFCell cell3 = row.createCell(2);
		sheet.setColumnWidth(2, 6000);
		cell3.setCellValue("when in the course of human events it becomes necessary");
		cell3.setCellStyle(style2);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 2));
		System.out.println(row.getHeight());
		
		workbook.write(new FileOutputStream("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/report_pdf/testTextWrap.xlsx"));
	}
	
	public static void main(String[] args) {
		try {
			new TestTextWrap().go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
