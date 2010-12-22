package com.bluemartini.loader.excel.record;

import com.bluemartini.dna.BMException;
import com.bluemartini.dna.DNAList;

/**
 * Interface for excel record processor 
 * 
 * @author Yannick Robin
 *
 */
public interface ExcelRecordProcessor {
	
	public void process() throws BMException;
	public void setRecord(DNAList dnaRecord) throws BMException;
}
