package com.ansi.scilla.report.test.itext5;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import com.ansi.scilla.report.reportBuilder.pdfBuilder.PDFReportFormatter;
import com.itextpdf.text.pdf.BaseFont;

public class TestTTF  {

	public static void main(String[] args) {

		try {
			String fontFile = null;
			if ( SystemUtils.IS_OS_LINUX ) {
				System.out.println("OS Linux");
				File file = new File("/usr/share/fonts/calibri.ttf");
				if ( file.exists() && file.canRead() ) {
					fontFile = "/usr/share/fonts/calibri.ttf";
				}
			} else if ( SystemUtils.IS_OS_WINDOWS ) {
				System.out.println("OS Linux");
				File file = new File("C:\\Windows\\fonts\\calibri.ttf");
				if ( file.exists() && file.canRead() ) {
					fontFile = "C:\\Windows\\fonts\\calibri.ttf";
				}
			}
			System.out.println("Fontfile: " + fontFile);
			if ( fontFile == null ) {
				File tempDir = SystemUtils.getJavaIoTmpDir();
				if ( ! tempDir.exists() ) {
					System.out.println("Making " + tempDir);
					tempDir.mkdirs();
				}
				File ttfFile = new File(tempDir.getName() + File.separator + "calibri.ttf");
				System.out.println("TTF File: " + ttfFile);
				if ( ! ttfFile.exists() ) {
					InputStream is = PDFReportFormatter.class.getClassLoader().getResourceAsStream("resources/calibri.ttf");
					byte[] data = IOUtils.toByteArray(is);
					FileUtils.writeByteArrayToFile(ttfFile, data);
				}
				fontFile = ttfFile.getAbsolutePath();
				System.out.println(ttfFile.getAbsolutePath());
			}
			File file = new File(fontFile);
			System.out.println(fontFile + "\t" + file.exists());
			BaseFont calibri = BaseFont.createFont(fontFile, BaseFont.WINANSI, true);			
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}



}
