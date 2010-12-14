/**
 * 
 */
package com.bluemartini.loader.excel.parser;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import jxl.BooleanCell;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.BMMessages;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.loader.excel.record.ExcelProcessingException;

/**
 * Parse the Excel spreadsheet and return a DNAListArray that contains the excel table
 * Use JExcelAPI, see http://www.andykhan.com/jexcelapi/tutorial.html
 * @author Yannick Robin
 *
 */
public class ExcelParser implements Parser {

	private Workbook workbook = null;
	private Sheet sheet = null;
	private String[] header = null;
	private int headerLineNumber;
	private boolean bWorkbookOpen = false;
	
	public ExcelParser(File file, int headerLineNumber, int sheetNumber) throws BMException
	{
		try {
			workbook = Workbook.getWorkbook(file);
			bWorkbookOpen = true;
			sheet = workbook.getSheet(sheetNumber);
			this.headerLineNumber = headerLineNumber;
			header = getHeader();
		} catch (BiffException e) {
        	String sMessage = BMMessages.getError("EXCEL_PARSER", null);
			throw new ExcelProcessingException(sMessage); 
		} catch (IOException e) {
			throw new BMException("Fail to to open the Excel file. It could be due to an upload issue. Try again to import the Excel file.", e, null);
		}
	}
	
	private String[] getHeader()
	{		
		String[] header = new String[sheet.getColumns()];
		if (header==null)
			return null;
		
		for (int iCol=0; iCol<header.length; iCol++)
		{
			Cell cell = sheet.getCell(iCol,headerLineNumber-1);
			String value = cell.getContents();
			header[iCol] = value;
		}
		return header;
	}

	public DNAListArray getLines(int beginLine, int endLine)
	{
		DNAListArray dnaLines = new DNAListArray();
				
		//parse each row
		for (int iRow=beginLine; iRow<endLine; iRow++)
		{
			DNAList dnaLine = new DNAList();
			// parse each column of the row
			for (int iCol=0; iCol<header.length; iCol++)
			{
				Cell cell = sheet.getCell(iCol,iRow);
				dnaLine = addFormatedCell(dnaLine, cell, header[iCol]);				
			}
			dnaLines.addElement(dnaLine);
		}
		return dnaLines;
	}

	private DNAList addFormatedCell(DNAList dnaLine, Cell cell, String colName) {
		
		//Normally it should be handle by type == CellType.EMPTY except for the last in the column
		if (cell == null)
		{
			dnaLine.setNull(colName);
			return dnaLine;
		}

		CellType type = cell.getType();		
		if (type == CellType.LABEL)
		{
		   LabelCell lc = (LabelCell) cell;
		   dnaLine.setString(colName, lc.getString());
		}
		 
		if (type == CellType.STRING_FORMULA)
		{
		   LabelCell lc = (LabelCell) cell;
		   dnaLine.setString(colName, lc.getString());
		}
		
		//Note Integer and Long doesn't exist in Excel, only Double
		if (type == CellType.NUMBER || type == CellType.NUMBER_FORMULA)
		{
		   NumberCell nc = (NumberCell) cell;
		   dnaLine.setDouble(colName, nc.getValue());
		   
		   /*
		   NumberFormat format = nc.getNumberFormat();
		   java.util.Currency currency = format.getCurrency();
		   if (currency == null)
			   dnaLine.setDouble(colName, nc.getValue());
		   else
		   {
			   //@TODO  The java currency is not the same than BM currency so we should convert 
			   com.bluemartini.dna.Currency bmCurrency = new com.bluemartini.dna.Currency(nc.getValue());
			   dnaLine.setCurrency(colName, bmCurrency);
		   }
		   */
		}
		 
		if (type == CellType.BOOLEAN  || type == CellType.BOOLEAN_FORMULA)
		{
		   BooleanCell nc = (BooleanCell) cell;
		   dnaLine.setBoolean(colName, nc.getValue());
		}
		
		if (type == CellType.DATE || type == CellType.DATE_FORMULA)
		{
		   DateCell dc = (DateCell) cell;
		   Calendar cal = new GregorianCalendar();
		   cal.setTime(dc.getDate());
		   dnaLine.setDateTime(colName, cal);
		}
		 
		if (type == CellType.EMPTY || type == CellType.ERROR || type == CellType.FORMULA_ERROR)
		{
			 dnaLine.setNull(colName);
		}
				
		return dnaLine;
	}

	public int getRowsSize() {
		return sheet.getRows();
	}
	
	public void close() {
		if (bWorkbookOpen)
			workbook.close();
	}
}
