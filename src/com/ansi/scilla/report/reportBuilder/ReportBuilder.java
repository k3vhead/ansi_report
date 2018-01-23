package com.ansi.scilla.report.reportBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ansi.scilla.common.ApplicationObject;

public abstract class ReportBuilder extends ApplicationObject {

	private static final long serialVersionUID = 1L;
	/**
	 * We'll use these to create a summary line at the end of the report. The key 
	 * to each map is the fieldName. Non-null values will be added to the value in the 
	 * key/value pairs. Averages will be calculated from the sum and the count. "Count Distinct"
	 * will be the number of entries in the List&lt;String&gt;
	 */
	protected HashMap<String, Double> reportSum = new HashMap<String, Double>();
	protected HashMap<String, Integer> reportCount = new HashMap<String, Integer>();
	protected HashMap<String, List<Object>> reportCountDistinct = new HashMap<String, List<Object>>();

	/**
	 * We'll use these to create subtotal lines. As each line is added, the values will be checked
	 * against "previousValue". If the value has changed and there's a subtotal trigger, a subtotal
	 * line will be added and the subtotal counters will be cleared;
	 */	
	protected HashMap<String, Object> previousValues = new HashMap<String, Object>();
	protected HashMap<String, Double> subtotalSum = new HashMap<String, Double>();
	protected HashMap<String, Integer> subtotalCount = new HashMap<String, Integer>();
	protected HashMap<String, List<Object>> subtotalCountDistinct = new HashMap<String, List<Object>>();
	
	protected AbstractReport report;
	protected Logger logger;



	public ReportBuilder(StandardReport report) {
		this.report = report;
		this.logger = LogManager.getLogger(this.getClass());
		if ( report.getHeaderLeft() == null ) {
			// this is just so we do't need to null check later
			report.setHeaderLeft(new ArrayList<ReportHeaderCol>());
		}
		if ( report.getHeaderRight() == null ) {
			// this is just so we do't need to null check later
			report.setHeaderRight(new ArrayList<ReportHeaderCol>());
		}		 
		initializeSummaries(report.getHeaderRow());
	}
	
	public ReportBuilder(StandardSummaryReport report) {
		this.report = report;
		if ( report.getHeaderLeft() == null ) {
			// this is just so we do't need to null check later
			report.setHeaderLeft(new ArrayList<ReportHeaderCol>());
		}
		if ( report.getHeaderRight() == null ) {
			// this is just so we do't need to null check later
			report.setHeaderRight(new ArrayList<ReportHeaderCol>());
		}		 
	}

	public AbstractReport getReport() {
		return report;
	}


	/**
	 * Figure out how many rows of titles and/or header data we have. There is always at least 
	 * a single row for the banner. Count is incremented for title and subtitle, and for each row
	 * of header data (right/left columns) beyond the banner/title/subtitle.
	 * @deprecated Use ReportUtils.makeHeaderRowCount()
	 * @return Number of rows in the header
	 */
	protected int makeHeaderRowCount() {
		int headerRowCount = 1;  // we've always got a banner
		if ( ! StringUtils.isBlank(report.getTitle())) {
			headerRowCount++;
		}
		if ( ! StringUtils.isBlank(report.getSubtitle())) {
			headerRowCount++;
		}
		for ( ReportHeaderCol col : report.getHeaderLeft()) {
			if ( col.getRowList().size() > headerRowCount ) {
				headerRowCount = col.getRowList().size(); 
			}
		}
		for ( ReportHeaderCol col : report.getHeaderRight()) {
			if ( col.getRowList().size() > headerRowCount ) {
				headerRowCount = col.getRowList().size(); 
			}
		}
		return headerRowCount;
	}

	/**
	 * Format arbitrary value according to the standard report formats
	 * @param dataFormat Format for the data value
	 * @param value The data to be formatted
	 * @return formatted data
	 * @throws NoSuchMethodException A mismatch in the name of the formatter 
	 * @throws SecurityException Java reflection error -- this shouldn't happen
	 * @throws IllegalAccessException Java reflection error -- this shouldn't happen
	 * @throws IllegalArgumentException A mismatch in the type of data and the type that the formatter is expecting
	 * @throws InvocationTargetException Java reflection error -- this shouldn't happen
	 */
	protected String formatValue(DataFormats dataFormat, Object value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ReportFormatter formatter = dataFormat.formatter();
		Method method = formatter.getClass().getMethod("format", new Class[] {value.getClass()});
		return (String)method.invoke(formatter, new Object[] {value});
	}


	/**
	 * The summary data needs to be initialized to zeros before starting. Else we've got to null check
	 * on every value, and I don't want to do that. This method is called by the constructor
	 * @param headerRow The list of columns in the report.
	 */
	protected void initializeSummaries(ColumnHeader[] headerRow) {
		if ( headerRow != null ) {
			for ( ColumnHeader header : headerRow ) {
				if ( header.getSummaryType() != SummaryType.NONE ) {
					if ( ! StringUtils.isBlank(header.getSubTotalTrigger()) ) {
						previousValues.put(header.getSubTotalTrigger(), new NoPreviousValue());
						subtotalSum.put(header.getFieldName(), 0.0D);
						subtotalCount.put(header.getFieldName(), 0);
						subtotalCountDistinct.put(header.getFieldName(), new ArrayList<Object>());
					}
					reportSum.put(header.getFieldName(), 0.0D);
					reportCount.put(header.getFieldName(), 0);
					reportCountDistinct.put(header.getFieldName(), new ArrayList<Object>());
				}
			}
		}
	}


	/**
	 * Every time a row is added to the report, add the non-null values to the summary trackers. We
	 * only do this for fields that will be in the summary row.
	 * @param columnHeader Build summary values
	 * @param value The value to be added to the summary
	 */
	protected void doSummaries(ColumnHeader columnHeader, Object value) {		
		if ( value != null && columnHeader.getSummaryType() != SummaryType.NONE) {
			// Count up the grand totals
			String fieldName = columnHeader.getFieldName();
			reportSum.put(fieldName, addNumbers(reportSum.get(fieldName), value));
			reportCount.put(fieldName, reportCount.get(fieldName)+1);
			List<Object> objectList = reportCountDistinct.get(fieldName);
			if ( ! objectList.contains(value)) {
				objectList.add(value);
			}
			reportCountDistinct.put(fieldName, objectList);
			
			// Count up the subtotals
			if ( ! StringUtils.isBlank(columnHeader.getSubTotalTrigger()) ) {
				subtotalSum.put(fieldName, addNumbers(subtotalSum.get(fieldName), value));
				subtotalCount.put(fieldName, subtotalCount.get(fieldName)+1);
				List<Object> subtotalList = subtotalCountDistinct.get(fieldName);
				if ( ! subtotalList.contains(value)) {
					subtotalList.add(value);
				}
				subtotalCountDistinct.put(fieldName, objectList);
			}
		}
	}
	
	/**
	 * When we add a value to the summary, we don't know what kind of value it is, so figure it out
	 * and do the math
	 * @param summary
	 * @param value
	 * @return
	 */
	private Double addNumbers(Double summary, Object value) {
		Double sum = summary;
		if ( value instanceof Double ) {
			sum = summary + (Double)value;
		} else if ( value instanceof BigDecimal ) {
			sum = summary + ((BigDecimal)value).doubleValue();
		} else if ( value instanceof Float ) {
			sum = summary + (Float)value;
		} else if ( value instanceof Integer ) {
			sum = summary + (Integer)value;
		} else if ( value instanceof String ) {
			// skip this one
		} else {
			logger.log(Level.ERROR, "Skipping a value I don't recognize: " + value.getClass().getName());			
		}
		return sum;
	}

	
	/**
	 * We've reached the end of the report and need to add a summary row. For each column header
	 * where summary type is not "NONE", call this method to get/format the data from the 
	 * summary trackers. Averages and sums are formatted in the same way as the data. Counts 
	 * are formatted as pretty integers. 
	 * @param columnHeader Which column are we building a summary for
	 * @return The formatted summary data
	 * @throws Exception Something bad happened
	 */
	protected String makeSummaryData(ColumnHeader columnHeader) throws Exception {
		String display = "";
		if ( columnHeader.getSummaryType().equals(SummaryType.AVERAGE)) {
			Integer count = reportCount.get(columnHeader.getFieldName());
			Double sum = reportSum.get(columnHeader.getFieldName());
			Double value = sum / new Double(count);
			display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
		} else if ( columnHeader.getSummaryType().equals(SummaryType.COUNT)) {
			Integer value = reportCount.get(columnHeader.getFieldName());
			display = makeFormattedDisplayData(DataFormats.INTEGER_FORMAT, value);
		} else if ( columnHeader.getSummaryType().equals(SummaryType.COUNT_DISTINCT)) {
			Integer value = reportCountDistinct.get(columnHeader.getFieldName()).size();
			display = makeFormattedDisplayData(DataFormats.INTEGER_FORMAT, value);
		} else if ( columnHeader.getSummaryType().equals(SummaryType.SUM)) {
			Double value = reportSum.get(columnHeader.getFieldName());
			display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
		} else {
			throw new RuntimeException("Unknown summary type");
		}
		return display;
	}
	

	
	protected String makeSubtotalData(ColumnHeader columnHeader) throws Exception {
		String display = "";
		String fieldName = columnHeader.getFieldName();
		if ( columnHeader.getSummaryType().equals(SummaryType.AVERAGE)) {
			Integer count = subtotalCount.get(fieldName);
			Double sum = subtotalSum.get(fieldName);
			Double value = sum / new Double(count);
			display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
		} else if ( columnHeader.getSummaryType().equals(SummaryType.COUNT)) {
			Integer value = subtotalCount.get(fieldName);
			display = makeFormattedDisplayData(DataFormats.INTEGER_FORMAT, value);
		} else if ( columnHeader.getSummaryType().equals(SummaryType.COUNT_DISTINCT)) {
			Integer value = subtotalCountDistinct.get(fieldName).size();
			display = makeFormattedDisplayData(DataFormats.INTEGER_FORMAT, value);
		} else if ( columnHeader.getSummaryType().equals(SummaryType.SUM)) {
			Double value = subtotalSum.get(fieldName);
			display = makeFormattedDisplayData(columnHeader.getFormatter(), value);
		} else {
			throw new RuntimeException("Unknown summary type");
		}
		
		// reset the counts
		subtotalCount.put(fieldName, 0);
		subtotalSum.put(fieldName, 0D);
		reportCountDistinct.put(fieldName, new ArrayList<Object>());
		
		return display;
	}
	/**
	 * Get the data from an arbitrary row object
	 * @param columnHeader Which column of the report are we building
	 * @param row The row data object which contains the data to be displayed
	 * @return The data to be displayed
	 * @throws NoSuchMethodException The method in the formatter is wrong
	 * @throws SecurityException java reflection error - this shouldn't happen
	 * @throws IllegalAccessException java reflection error - this shouldn't happen
	 * @throws IllegalArgumentException Mismatch in the getter's parameters (there are no parms, so this shouldn't happen either)
	 * @throws InvocationTargetException java reflection error - this shouldn't happen
	 */
	protected Object makeDisplayData(ColumnHeader columnHeader, Object row) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		String methodName = "get" + StringUtils.capitalize(columnHeader.getFieldName());
		Method dataMethod = row.getClass().getMethod(methodName, (Class<?>[])null);
		Object value = dataMethod.invoke(row, (Object[])null);
		return value;
	}


	/**
	 * Format the data according to the standard method specified.
	 * @param dataFormats How to format the value
	 * @param value the data to be formatted
	 * @return Formatted data
	 * @throws NoSuchMethodException The formatter has an invalid formatting method name
	 * @throws SecurityException java reflection error - this shouldn't happen
	 * @throws IllegalAccessException java reflection error - this shouldn't happen
	 * @throws IllegalArgumentException The formatting method has a mismatch in the expected arguments
	 * @throws InvocationTargetException java reflection error - this shouldn't happen
	 */
	protected String makeFormattedDisplayData(DataFormats dataFormats, Object value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String display = "";
		if ( value != null ) {
			ReportFormatter formatter = dataFormats.formatter();
//			Method formatterMethod = formatter.getClass().getMethod("format", new Class<?>[] {dataMethod.getReturnType()});
			Method formatterMethod = formatter.getClass().getMethod("format", new Class<?>[] {value.getClass()});
			display = (String)formatterMethod.invoke(formatter, new Object[] {value} );
		}
		
		return display;
	}

	
}
