package com.bluemartini.loader.excel.record;

/**
 * Use this class to throw an exception during the import processing.
 * It has to be used in place of BMException when we don't want to stop the processing.
 * It means only for errors specific to a line or a cell. 
 * 
 * @author Yannick Robin
 *
 */
public class ExcelLineProcessingException extends ExcelProcessingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExcelLineProcessingException(String message) {
		super(message);
	}

}
