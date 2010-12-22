/**
 * 
 */
package com.bluemartini.loader.excel.parser;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.DNAListArray;

/**
 * Excel parser interface
 * @author Yannick Robin
 *
 */
public interface Parser {

	
	public DNAListArray getLines(int beginLine, int endLine) throws BMException;

	public int getRowsSize() throws BMException;
	
	public void close() throws BMException;
}
