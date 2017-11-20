package com.ansi.scilla.report.test.xlsdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSMaster {

	public static void main(String[] args) {
		try {
			new XLSMaster().go();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void go() throws IOException {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet1 = wb.createSheet("sheetdemo1");
		XSSFSheet sheet2 = wb.createSheet("sheetdemo2");
		
		XLSPopulator xp = new XLSPopulator();
		xp.populateSheet(sheet1);
		xp.populateSheet(sheet2);
		
		File file = new File("/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/xlsdemo.xlsx");
		FileOutputStream outputStream = new FileOutputStream(file);
		wb.write(outputStream);
		
	}
}
