/**
 * 
 */
package com.bluemartini.loader.excel.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;

/**
 * Parse the CSV file and return a DNAListArray that contains the CSV lines
 * @author Yannick Robin
 *
 */
public class CSVParser implements Parser {

	private BufferedReader reader = null;
	private String[] header = null;
	private boolean bReaderOpen = false;
	private int size = 0;
	private static final char separator_ = ',';
	private static final char beginChain_ = '"';	
	private static final char endChain_ = '"';
	
	public CSVParser(File file) throws BMException
	{
		try {	
			LineNumberReader lnReader = new LineNumberReader(new FileReader(file));
		    while ((lnReader.readLine()) != null) {
	            size=size+1;
	        }
	        lnReader.close();

	        reader = new BufferedReader(new FileReader(file));
			bReaderOpen = true;
			header = getHeader();
		} catch (IOException e) {
			throw new BMException("Failed to open the CSV file. It could be due to an upload issue. Try again.", e, null);
		}	
	}
	
	private String[] getHeader() throws BMException
	{		
		String sLine = null;
		try {
			sLine = reader.readLine();
		} catch (IOException e) {
			throw new BMException("Failed to read line.", e, null);
		}	
		String[] sCells =  sLine.split(String.valueOf(separator_));
		header = new String [sCells.length];
		
		for (int iCol=0; iCol<sCells.length; iCol++)
		{
			header[iCol] = sCells[iCol];
		}
		
		return header;
	}

	public DNAListArray getLines(int beginLine, int endLine) throws BMException
	{
		DNAListArray dnaLines = new DNAListArray();
				
		//parse each row
		for (int iRow=beginLine; iRow<endLine; iRow++)
		{
			String sLine = null;
			try {
				sLine = reader.readLine();
			} catch (IOException e) {
				throw new BMException("Failed to read line.", e, null);
			}
			
			DNAList dnaLine = new DNAList();

			int iCol=0;
			String sCell = "";
			boolean chain = false;
			for (int c=0; c<sLine.length(); c++)
			{
				char myChar = sLine.charAt(c);
				
				//last character
				if (c==sLine.length()-1)
				{
					if (myChar != endChain_)
						sCell = sCell + myChar;
					if (sCell.equals(""))
						dnaLine.setNull(header[iCol]);
					else
						dnaLine.setString(header[iCol], sCell);
				}
				//ommit '"'
				else if (!chain && myChar == beginChain_)
				{
					chain = true;
				}
				//ommit '"'
				else if (chain && myChar == endChain_)
				{
					chain = false;
				}
				//',' character
				else if (!chain && myChar == separator_)
				{
					if (sCell.equals(""))
						dnaLine.setNull(header[iCol]);
					else
						dnaLine.setString(header[iCol], sCell);
					sCell = "";
					iCol++;					
				}
				else
					sCell = sCell + myChar;
			}
			dnaLines.addElement(dnaLine);
		}
		return dnaLines;
	}

	public int getRowsSize() throws BMException {
		return size;
	}
	
	public void close() throws BMException {	
		try {
			if (bReaderOpen)
				reader.close();
		} catch (IOException e) {
			throw new BMException("Failed to close CSV file.", e, null);
		}
	}
}
