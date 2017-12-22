package com.ansi.scilla.report.reportBuilder;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSReportFormatter {
	
	public Integer fontHeight = 9;
	public Integer reportBannerHeight = 14;
	public Integer reportTitleHeight = 12;
	public Integer reportSubTitleHeight = 10;
	public Integer reportNoteHeight = 8;

	public short standardHeaderHeight = (short)400;    //this may be a good idea at some point, but we're not doing it now
	public short standardDetailHeight = (short)400;    //this may be a good idea at some point, but we're not doing it now
	
	public XSSFFont fontStandardBlack;
	public XSSFFont fontStandardBlackBold;
	public XSSFFont fontStandardWhite;
	public XSSFFont fontStandardWhiteBold;
	public XSSFFont fontReportBanner;
	public XSSFFont fontReportTitle;
	public XSSFFont fontReportSubTitle;
	public XSSFFont fontReportNote;
	
	public short dataFormatDate;
	public short dataFormatDateTime;
	public short dataFormatDecimal;
	public short dataFormatInteger;
	public short dataFormatNumber;
	public short dataFormatCurrency;
	
	public CellStyle cellStyleColHdrLeft;
	public CellStyle cellStyleColHdrCenter;
	public CellStyle cellStyleStandardLeft;
	public CellStyle cellStyleStandardCenter;
	public CellStyle cellStyleStandardRight;
	public CellStyle cellStyleStandardDecimal;
	public CellStyle cellStyleStandardNumber;
	public CellStyle cellStyleStandardCurrency;
	public CellStyle cellStyleStandardInteger;
	public CellStyle cellStyleNumberCenter;
	public CellStyle cellStyleDateCenter;
	public CellStyle cellStyleDateLeft;
	public CellStyle cellStyleDateTimeLeft;
	public CellStyle cellStyleReportBanner;
	public CellStyle cellStyleReportTitle;
	public CellStyle cellStyleReportSubTitle;
	public CellStyle cellStyleReportHeaderLabelCenter;
	public CellStyle cellStyleReportHeaderLabelLeft;
	public CellStyle cellStyleReportHeaderLabelRight;
	public CellStyle cellStyleReportNote;
	
	
	public CellStyle cellStyleSubtotalDecimal;
	
	//public Calendar runDate;
	
	public XLSReportFormatter(XSSFWorkbook workbook) {
		super();
		CreationHelper createHelper = workbook.getCreationHelper();		
		
		fontStandardBlack = workbook.createFont();
		fontStandardBlack.setColor(HSSFColor.BLACK.index);		
		fontStandardBlack.setFontHeight(fontHeight);
		
		fontStandardWhite = workbook.createFont();
		fontStandardWhite.setColor(HSSFColor.WHITE.index);
		fontStandardWhite.setFontHeight(fontHeight);
		
	    fontStandardBlackBold = workbook.createFont();
	    fontStandardBlackBold.setColor(HSSFColor.BLACK.index);
	    fontStandardBlackBold.setBold(true);
	    fontStandardBlackBold.setFontHeight(fontHeight);
	    
	    fontStandardWhiteBold = workbook.createFont();
	    fontStandardWhiteBold.setColor(HSSFColor.WHITE.index);
	    fontStandardWhiteBold.setBold(true);
	    fontStandardWhiteBold.setFontHeight(fontHeight);
	    
	    fontReportBanner = workbook.createFont();
	    fontReportBanner.setColor(HSSFColor.BLACK.index);
	    fontReportBanner.setBold(true);
	    fontReportBanner.setFontHeight(reportBannerHeight);
	    
	    fontReportTitle = workbook.createFont();
	    fontReportTitle.setColor(HSSFColor.BLACK.index);
	    fontReportTitle.setBold(true);
	    fontReportTitle.setFontHeight(reportTitleHeight);
	    
	    fontReportSubTitle = workbook.createFont();
	    fontReportSubTitle.setColor(HSSFColor.BLACK.index);
	    fontReportSubTitle.setBold(true);
	    fontReportSubTitle.setFontHeight(reportSubTitleHeight);
	    
	    fontReportNote = workbook.createFont();
	    fontReportNote.setColor(HSSFColor.BLACK.index);
	    fontReportNote.setBold(false);
	    fontReportNote.setFontHeight(reportNoteHeight);
	    
	    
	    dataFormatDate = createHelper.createDataFormat().getFormat("mm/dd/yyyy");
	    dataFormatDateTime = createHelper.createDataFormat().getFormat("mm/dd/yyyy hh:mm:ss");
	    dataFormatDecimal = createHelper.createDataFormat().getFormat("#,##0.00");
	    dataFormatInteger = createHelper.createDataFormat().getFormat("#,##0");
	    dataFormatNumber = createHelper.createDataFormat().getFormat("#0");
	    dataFormatCurrency = createHelper.createDataFormat().getFormat("$#,##0.00");
	    
	    
		cellStyleColHdrLeft = workbook.createCellStyle();
		cellStyleColHdrLeft.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
	    cellStyleColHdrLeft.setFillPattern(CellStyle.ALIGN_FILL);
	    cellStyleColHdrLeft.setAlignment(CellStyle.ALIGN_LEFT);		
	    cellStyleColHdrLeft.setWrapText(true);
	    cellStyleColHdrLeft.setFont(fontStandardWhiteBold);

		cellStyleColHdrCenter = workbook.createCellStyle();
		cellStyleColHdrCenter.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
		cellStyleColHdrCenter.setFillPattern(CellStyle.ALIGN_FILL);
		cellStyleColHdrCenter.setWrapText(true);
		cellStyleColHdrCenter.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyleColHdrCenter.setFont(fontStandardWhiteBold);

	    cellStyleStandardLeft = workbook.createCellStyle();
//	    cellStyleStandardLeft.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleStandardLeft.setFillPattern(CellStyle.ALIGN_FILL);
	    cellStyleStandardLeft.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleStandardLeft.setFont(fontStandardBlack);

	    cellStyleStandardRight = workbook.createCellStyle();
	    //cellStyleStandardRight.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    //cellStyleStandardRight.setFillPattern(CellStyle.ALIGN_FILL);
	    cellStyleStandardRight.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleStandardRight.setFont(fontStandardBlack);

	    cellStyleStandardCenter = workbook.createCellStyle();
	    cellStyleStandardCenter.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleStandardCenter.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleStandardCenter.setFont(fontStandardBlack);
	    
	    cellStyleDateCenter = workbook.createCellStyle();
	    cellStyleDateCenter.setDataFormat(dataFormatDate);
	    cellStyleDateCenter.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleDateCenter.setFont(fontStandardBlack);
	    
	    cellStyleDateLeft = workbook.createCellStyle();
	    cellStyleDateLeft.setDataFormat(dataFormatDate);
	    cellStyleDateLeft.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleDateLeft.setFont(fontStandardBlack);

	    cellStyleDateTimeLeft = workbook.createCellStyle();
	    cellStyleDateTimeLeft.setDataFormat(dataFormatDateTime);
	    cellStyleDateTimeLeft.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleDateTimeLeft.setFont(fontStandardBlack);
	    
	    cellStyleStandardDecimal = workbook.createCellStyle();
	    cellStyleStandardDecimal.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleStandardDecimal.setDataFormat(dataFormatDecimal);
	    cellStyleStandardDecimal.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleStandardDecimal.setFont(fontStandardBlack);
	    
	    cellStyleSubtotalDecimal = workbook.createCellStyle();
	    cellStyleSubtotalDecimal.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleSubtotalDecimal.setDataFormat(dataFormatDecimal);
	    cellStyleSubtotalDecimal.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleSubtotalDecimal.setFont(fontStandardBlackBold);
	    
	    cellStyleReportBanner = workbook.createCellStyle();
	    cellStyleReportBanner.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportBanner.setDataFormat(dataFormat);
	    cellStyleReportBanner.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleReportBanner.setFont(fontReportBanner);
	    
	    cellStyleReportTitle = workbook.createCellStyle();
	    cellStyleReportTitle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportTitle.setDataFormat(dataFormat);
	    cellStyleReportTitle.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleReportTitle.setFont(fontReportTitle);
	    
	    cellStyleReportSubTitle = workbook.createCellStyle();
	    cellStyleReportSubTitle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportTitle.setDataFormat(dataFormat);
	    cellStyleReportSubTitle.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleReportSubTitle.setFont(fontReportSubTitle);
	    
	    cellStyleReportHeaderLabelCenter = workbook.createCellStyle();
	    cellStyleReportHeaderLabelCenter.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportHeaderLabel.setDataFormat(dataFormat);
	    cellStyleReportHeaderLabelCenter.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleReportHeaderLabelCenter.setFont(fontStandardBlackBold);

	    cellStyleReportHeaderLabelLeft = workbook.createCellStyle();
	    cellStyleReportHeaderLabelLeft.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportHeaderLabel.setDataFormat(dataFormat);
	    cellStyleReportHeaderLabelLeft.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleReportHeaderLabelLeft.setFont(fontStandardBlackBold);
	    
	    cellStyleReportHeaderLabelRight = workbook.createCellStyle();
	    cellStyleReportHeaderLabelRight.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportHeaderLabel.setDataFormat(dataFormat);
	    cellStyleReportHeaderLabelRight.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleReportHeaderLabelRight.setFont(fontStandardBlackBold);
	    
	    cellStyleStandardInteger = workbook.createCellStyle();
	    cellStyleStandardInteger.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleStandardInteger.setDataFormat(dataFormatInteger);
	    cellStyleStandardInteger.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleStandardInteger.setFont(fontStandardBlack);
	    
	    cellStyleNumberCenter = workbook.createCellStyle();
	    cellStyleNumberCenter.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleNumberCenter.setDataFormat(dataFormatNumber);
	    cellStyleNumberCenter.setAlignment(CellStyle.ALIGN_CENTER);
	    cellStyleNumberCenter.setFont(fontStandardBlack);
	    
	    cellStyleStandardNumber = workbook.createCellStyle();
	    cellStyleStandardNumber.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleStandardNumber.setDataFormat(dataFormatNumber);
	    cellStyleStandardNumber.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleStandardNumber.setFont(fontStandardBlack);
	    
	    cellStyleStandardCurrency = workbook.createCellStyle();
	    cellStyleStandardCurrency.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
	    cellStyleStandardCurrency.setDataFormat(dataFormatCurrency);
	    cellStyleStandardCurrency.setAlignment(CellStyle.ALIGN_RIGHT);
	    cellStyleStandardCurrency.setFont(fontStandardBlack);
	    
	    cellStyleReportNote = workbook.createCellStyle();
	    cellStyleReportNote.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
//	    cellStyleReportNote.setDataFormat(xxx);
	    cellStyleReportNote.setWrapText(true);
	    cellStyleReportNote.setAlignment(CellStyle.ALIGN_LEFT);
	    cellStyleReportNote.setFont(fontReportNote);
	}
}
