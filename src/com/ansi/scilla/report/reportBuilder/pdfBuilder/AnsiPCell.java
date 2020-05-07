package com.ansi.scilla.report.reportBuilder.pdfBuilder;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;


/**
 * itext PdfPCell with ansi default formatting (vertical-align:top, horizontal-align:left, no borders, no background,
 * padding top and bottom, no indent
 * 
 * This is the same as:
 * <code>
 * PdfPCell cell = new PdfPCell();
 * cell.setVerticalAlignment(Element.ALIGN_TOP);
 * cell.setHorizontalAlignment(Element.ALIGN_LEFT);
 * cell.setBorder(Rectangle.NO_BORDER);
 * cell.setIndent(0F);
 * cell.setPaddingTop(4F);
 * cell.setPaddingBottom(4F);
 * </code>
 * 
 * @author dclewis
 *
 */
public class AnsiPCell extends PdfPCell {

	public AnsiPCell() {
		super();
		setDefaults();
	}

	public AnsiPCell(Image image, boolean fit) {
		super(image, fit);
		setDefaults();
	}

	public AnsiPCell(Image image) {
		super(image);
		setDefaults();
	}

	public AnsiPCell(PdfPCell cell) {
		super(cell);
		setDefaults();
	}

	public AnsiPCell(PdfPTable table, PdfPCell style) {
		super(table, style);
		setDefaults();
	}

	public AnsiPCell(PdfPTable table) {
		super(table);
		setDefaults();
	}

	public AnsiPCell(Phrase phrase) {
		super(phrase);
		setDefaults();
	}

	public AnsiPCell(Chunk chunk) {
		this(new Phrase(chunk));
		setDefaults();
	}
	
	private void setDefaults() {
		setVerticalAlignment(Element.ALIGN_TOP);
		setHorizontalAlignment(Element.ALIGN_LEFT);
        setBorder(Rectangle.NO_BORDER);
//		setBorderColor(BaseColor.BLACK);
//		setBorderWidth(1F);
		setIndent(0F);
		setPaddingTop(4F);
		setPaddingBottom(4F);
//		setBackgroundColor(BaseColor.BLACK);
		
	}

	
}
