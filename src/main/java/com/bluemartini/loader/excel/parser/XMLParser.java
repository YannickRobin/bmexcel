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
import com.bluemartini.dna.XDNATransformer;

/**
 * Parse the XML file and return a DNAListArray
 * @author Yannick Robin
 *
 * Below is an example:
 *
<PRE>
<?xml version="1.0" encoding="ISO-8859-1"?>
<dna xmlns="http://bluemartini.com/eng/arch/dna/type/">
    <dnaarray name="excel_loader">
		<dna>
			<string name="Product Name">FD100</string>
			<string name="Description">Perfect lightweight boots for getting out of the city.</string>
			<string name="Description short">Day Hiker Boots</string>
			<string name="Description long">Perfect lightweight boots for getting out of the city.</string>
			<boolean name="Water proof">true</boolean>
		</dna>
		<dna>
			<string name="Product Name">FD200</string>
			<string name="Description">A sturdy, yet lightweight, hiking boot built to last well into the current millenium.</string>
			<string name="Description short">Y2K Day Hiking Boots</string>
			<string name="Description long">This affordable, lightweight dayhiker is sturdy enough for extended trips where the going doesn't get too rough. Nubuck leather uppers are far more waterproof than mesh, and can be treated with a commercial seam-sealer treatment.</string>
			<boolean name="Water proof">false</boolean>
		</dna>
		<dna>
			<string name="Product Name">FD300</string>
			<string name="Description">A value-priced light hiker that's comfortable enough to wear right out of the box.</string>
			<string name="Description short">Temagami 2 Day Hiking Boots</string>
			<string name="Description long">The comfort of your hiking boots can literally make or break your trip into the backcountry. The Temagami 2 is suitable for day hiking with a light pack, and is even durable enough to withstand weekend trips on flat, well-travelled terrain.</string>
			<boolean name="Water proof">true</boolean>
		</dna>
	</dnaarray>
</dna>
</PRE>
 *
 *
*/

public class XMLParser implements Parser {
	
	private DNAList dnaXDNA;
	
	public XMLParser(File file) throws BMException
	{
		XDNATransformer xdnaTransformer = new XDNATransformer();
		dnaXDNA = xdnaTransformer.loadXDNAFile(file.getPath());
	}

	public DNAListArray getLines(int beginLine, int endLine) throws BMException
	{
		DNAListArray dnaLines = dnaXDNA.getListArray("excel_loader");
		return dnaLines;
	}

	public int getRowsSize() throws BMException {
		return 1;
	}
	
	public void close() throws BMException {
	}
}
