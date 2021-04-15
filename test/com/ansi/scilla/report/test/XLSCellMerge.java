package com.ansi.scilla.report.test;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.io.FileOutputStream;
import java.text.AttributedString;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ansi.scilla.report.reportBuilder.xlsBuilder.XLSReportFormatter;

public class XLSCellMerge {

	private final String REPORT_NOTE = "* = Tickets with statuses 'Non Dispatched' or 'Locked Freq' " +
			"require updating before they can run. Please contact the administrative team and let them " +
			"know what you would like to do with these tickets. \n** = A status of 'Finished' means that the " +
			"ticket has been processed as being completed but has not yet been assigned to an invoice for billing";
//	private final String testFile = "/Users/jwlew/Documents/mergetest.xlsx";
	private final String testFile = "/home/dclewis/Documents/webthing_v2/projects/ANSI/testresults/mergetest.xlsx";
	
	public static void main(String[] args) {
		System.out.println("start");
		try {
//			new XLSCellMerge().go();
			new XLSCellMerge().testRow();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end");
	}

	private void testRow() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();	
		XSSFSheet sheet = workbook.createSheet();

		for ( int j = 0; j < 5; j++ ) {
			System.out.println("Trying row " + j);
			XSSFRow row = sheet.getRow(j);
			if ( row == null ) {
				System.out.println("\tcreating " + j);
				row = sheet.createRow(j);
			}
			for ( int i = 0 ; i < 5; i++ ) {
				XSSFCell cell = row.createCell(i);
				cell.setCellValue("Col " + j + "-" + i);
			}
		}
		
		for ( int j = 0; j < 10; j++ ) {
			System.out.println("Trying row " + j);

			XSSFRow row = sheet.getRow(j);
			if ( row == null ) {
				System.out.println("\tcreating " + j);
				row = sheet.createRow(j);
			}
			for ( int i = 3 ; i < 12; i++ ) {
				XSSFCell cell = row.createCell(i);
				cell.setCellValue("v2 " + j + "-" + i);
			}
		}
		workbook.write(new FileOutputStream(testFile));
	}

	public void go() throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XLSReportFormatter reportFormatter = new XLSReportFormatter(workbook);
		CellStyle cellStyle = reportFormatter.cellStyleReportNote;
//		cellStyle.setWrapText(true);
		XSSFFont myFont = reportFormatter.fontReportNote;
//		XSSFFont fontStandardWhiteBold = workbook.createFont();
//	    fontStandardWhiteBold.setColor(HSSFColor.BLACK.index);
//	    fontStandardWhiteBold.setBold(true);
//	    fontStandardWhiteBold.setFontHeight(12);
//	    
//		CellStyle cellStyleColHdrLeft = workbook.createCellStyle();
//		cellStyleColHdrLeft.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleColHdrLeft.setFillPattern(CellStyle.ALIGN_FILL);
//	    cellStyleColHdrLeft.setAlignment(CellStyle.ALIGN_LEFT);		
//	    cellStyleColHdrLeft.setWrapText(true);
//	    cellStyleColHdrLeft.setFont(fontStandardWhiteBold);
	    
	    
		XSSFSheet sheet = workbook.createSheet();
		XSSFRow row = sheet.createRow(0);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue("this is col 0 1");
		cell = row.createCell(1);
		cell.setCellValue("this is col 0 2");
		
		row = sheet.createRow(1);
		XSSFCell bigcell = row.createCell(0);
		bigcell.setCellValue(REPORT_NOTE);
		bigcell.setCellStyle(cellStyle);
		int endCell = 13;
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, endCell));
		
		row = sheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue("this is col 2 1");
		cell = row.createCell(1);
		cell.setCellValue("this is col 2 2");

		
		
		row = sheet.getRow(1);
		System.out.println("Old height: " + row.getHeight());
		
		
//		short newHeight = option1(sheet, myFont);
		short newHeight = option2(sheet, endCell);
		
		
		System.out.println("New Height: " + newHeight);
		row.setHeight(newHeight);
		workbook.write(new FileOutputStream(testFile));
		
	}

	
	private short option2(XSSFSheet sheet, int endCell) {
		XSSFCell cell = sheet.getRow(1).getCell(0);
		XSSFFont myFont = cell.getCellStyle().getFont();
		short rowHeight = sheet.getRow(1).getHeight();
		float cellWidth = sheet.getColumnWidth(0); // in units of 1/256 character width
		System.out.println("CellWidth: " + cellWidth);
		float charactersThatWillFit = cellWidth / 256 * myFont.getFontHeightInPoints() * (endCell + 1);
		System.out.println("charactersThatWillFit: " + charactersThatWillFit);
		float charactersThatWeHave = REPORT_NOTE.length();
		System.out.println("charactersThatWeHave: " + charactersThatWeHave);
		Float linesWeNeed = charactersThatWeHave / charactersThatWillFit;
		System.out.println("linesWeNeed: " + linesWeNeed);
		int lineCount = linesWeNeed.intValue() + 1;  // round up for partial lines
		System.out.println("lineCount: " + lineCount);
		short newHeight = (short)(rowHeight * lineCount * 1.8);  // include a fudge factor
		
		return newHeight;
	}

	
	
	private short option1(XSSFSheet sheet, XSSFFont fontStandardWhiteBold) {
		float bigCellWidth = sheet.getColumnWidth(0)/256F;  // getColumnWidht() returns units of 1/256 of character width
		System.out.println("Cell Width: " + bigCellWidth);
		int fontStyle = Font.PLAIN;
		if ( fontStandardWhiteBold.getBold() ) { fontStyle = Font.BOLD; }
		if ( fontStandardWhiteBold.getItalic() ) { fontStyle = Font.ITALIC; }
		
		
		java.awt.Font currFont = new java.awt.Font(
				fontStandardWhiteBold.getFontName(),
				fontStyle,
				fontStandardWhiteBold.getFontHeightInPoints()
				);
		AttributedString attrString = new AttributedString(REPORT_NOTE);
		attrString.addAttribute(TextAttribute.FONT, currFont);
		
		FontRenderContext frc = new FontRenderContext(null, true, true);
		LineBreakMeasurer measurer = new LineBreakMeasurer(attrString.getIterator(), frc);
		int nextPos = 0;
		int lineCount = 0;
		while ( measurer.getPosition() < REPORT_NOTE.length()) {
			nextPos = measurer.nextOffset(bigCellWidth);
			lineCount++;
			measurer.setPosition(nextPos);
		}
		
		XSSFRow row = sheet.getRow(1);
		short newHeight = row.getHeight();
		System.out.println("Line Count: " + lineCount);
		if ( lineCount > 1 ) {
			newHeight = (short)(row.getHeight() * lineCount * 0.7);
		}
		return newHeight;
	}
	/*

			int colIdx = rowInfo.cells.indexOf(longestCell);

            // Figure out available width in pixels, taking colspans into account
            //
            float columnWidthInPx = columnWidthsInPx[colIdx];
            int numberOfMergedColumns = longestCell.colSpan;
            (numberOfMergedColumns - 1).times {
                columnWidthInPx += columnWidthsInPx[colIdx + it];
            }

            // Setup the font we'll use for figuring out where the text will be wrapped
            //
            XSSFFont cellFont = longestCell.getCellFont();
            int fontStyle = Font.PLAIN;
            if ( cellFont.getBold() ) fontStyle = Font.BOLD; 
            if ( cellFont.getItalic() ) fontStyle = Font.ITALIC;

            java.awt.Font currFont = new java.awt.Font(
                cellFont.getFontName(), 
                fontStyle, 
                cellFont.getFontHeightInPoints());

            AttributedString attrStr = new AttributedString(cellText);
            attrStr.addAttribute(TextAttribute.FONT, currFont);

            // Use LineBreakMeasurer to count number of lines needed for the text
            //
            FontRenderContext frc = new FontRenderContext(null, true, true);
            LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), frc);
            int nextPos = 0;
            int lineCnt = 0;
            while (measurer.getPosition() < cellText.length()) {
                nextPos = measurer.nextOffset( columnWidthInPx );
                lineCnt++;
                measurer.setPosition(nextPos);
            }

            if ( lineCnt > 1 ) {
                excelRow.setHeight((short)(excelRow.getHeight() * lineCnt * 0.7));
            }


	*/
}
