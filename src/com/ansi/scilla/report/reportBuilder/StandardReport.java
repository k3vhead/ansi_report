package com.ansi.scilla.report.reportBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.functors.UniquePredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thewebthing.commons.lang.StringUtils;


public abstract class StandardReport extends AbstractReport {

	private static final long serialVersionUID = 1L;

	private ColumnHeader[] headerRow;
	private String[] pageBreakFieldList;	
	private List<Object> dataRows;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public StandardReport() {
		super();
		this.dataRows = new ArrayList<Object>();
	}

	public ColumnHeader[] getHeaderRow() {
		return headerRow;
	}
	public void setHeaderRow(ColumnHeader[] headerRow) {
		this.headerRow = headerRow;		
	}
	public String[] getPageBreakFieldList() {
		return pageBreakFieldList;
	}
	public void setPageBreakFieldList(String[] pageBreakFieldList) {
		this.pageBreakFieldList = pageBreakFieldList;
	}
	public List<Object> getDataRows() {
		return dataRows;
	}

	public void setDataRows(List<Object> dataRows) {
		this.dataRows = dataRows;
	}
	public void addDataRow(Object dataRow) {
		this.dataRows.add(dataRow);		
	}

	@Override
	public Integer getReportWidth() {
//		return this.getHeaderRow().length + 2;  // we add 2 because first and last columns are double wide
		Integer reportWidth = 0;
		for ( ColumnHeader columnHeader : this.getHeaderRow() ) {
			reportWidth = reportWidth + columnHeader.getColspan();
		}
		return reportWidth;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer getReportHeight() {

		boolean hasSummary = false;
		List<String> subTotalFieldList = new ArrayList<String>();
		Integer subtotalRows = 0;
		for ( ColumnHeader header : getHeaderRow() ) {
			if ( header.getSummaryType() != SummaryType.NONE) {
				hasSummary = true;
			}
			if ( ! StringUtils.isBlank(header.getSubTotalTrigger()) ) {
				subTotalFieldList.add(header.getSubTotalTrigger());
			}
		}
		Integer summarySize = hasSummary ? 1 : 0;
		
		if ( subTotalFieldList.size() > 0 ) {
			List<Integer> sizeList = new ArrayList<Integer>();
			// figure out how many rows added for subtotals:		
			CollectionUtils.filter(subTotalFieldList, new UniquePredicate());  // first -- make sure we have a unique set of fields
			for ( String subTotalField : subTotalFieldList ) {
				String getterName = "get" + StringUtils.capitalize(subTotalField);
				// get list of all values in that field
				List<Object> tlist = (List<Object>)CollectionUtils.collect(this.getDataRows(), new InvokerTransformer(getterName, new Class<?>[]{}, new Object[]{}));
				// get list of unique values from that list
				CollectionUtils.filter(tlist, new UniquePredicate());
				sizeList.add(tlist.size());
			}
			Collections.sort(sizeList);
			subtotalRows = sizeList.get(sizeList.size()-1);  //maximum value
		}
		
		Integer rowHeight = super.getReportHeight() + this.dataRows.size() + subtotalRows + summarySize + 2;   // add 2 for spacer row + column headers
		return rowHeight;
	}
	
	
	
}
