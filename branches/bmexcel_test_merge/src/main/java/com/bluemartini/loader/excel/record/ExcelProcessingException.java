package com.bluemartini.loader.excel.record;

import com.bluemartini.dna.BMException;

/**
 * Use this class to throw an exception related to the Excel upload.
 * The message will be displayed to the customer.
 * 
 * @author Yannick Robin
 *
 */
public class ExcelProcessingException extends BMException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExcelProcessingException(String message) {
		super(message);
	}

}
