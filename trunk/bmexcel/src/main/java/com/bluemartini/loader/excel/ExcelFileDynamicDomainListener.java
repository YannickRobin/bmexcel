package com.bluemartini.loader.excel;
import com.bluemartini.dna.BMContext;
import com.bluemartini.dna.BMException;
import com.bluemartini.dna.DNAList;
import com.bluemartini.dna.DNAListArray;
import com.bluemartini.dna.DNAListIterator;
import com.bluemartini.dna.DNAStringArray;
import com.bluemartini.dna.DomainElement;
import com.bluemartini.dna.DynamicDomainEvent;
import com.bluemartini.dna.DynamicDomainListener;
import com.bluemartini.html.HTMLContext;

public class ExcelFileDynamicDomainListener implements DynamicDomainListener {

	private static DNAListArray codeDecodes;


	/**
     * Called when one of the dynamic domains for the triListe is
     * requested.  This method sets a DNAListArray of code/decode pairs in the
     * DynamicDomainEvent object.
     *
     * NOTE: This method is apt to be called often, so care should be taken to
     * not construct the list every time.
     *
     * This method is part of the DynamicDomainListener interface.
     *
     * @see DynamicDomainEvent
     * @see DynamicDomainListener
     */
    public void dynamicDomainRequested(DynamicDomainEvent event) {
        DomainElement domainElem = event.getDomainElement();
        domainElem.clear();

        if (codeDecodes != null && BMContext.getAppConfig().getInteger("configCheckSeconds", -1) == -1)
        {
        	event.setDomainValues(codeDecodes);
        	return;
        }
        
        codeDecodes = new DNAListArray();
        
        // Builds encode and decode lists.
        DNAList dnaConfig = null;
        try {
			dnaConfig = ExcelImportConfig.getConfig();
		} catch (BMException e) {
			e.printStackTrace();
		}
		
		DNAList dnaList = dnaConfig.getList("file_types");
		DNAListIterator iter = dnaList.iterator();
        
		while(iter.hasNext())
		{
			String name = iter.nextName();
			DNAList fileType = dnaList.getList(name);
			String description = fileType.getString("description");
			domainElem.addCodeDecode(codeDecodes, name , description);
		}

        //set the DynamicDomainEvent content.
        event.setDomainValues(codeDecodes);
    }

    /**
     * This method returns the dynamic domains that this listener will respond
     * to.
     *
     * This method is part of the DynamicDomainListener interface.
     *
     * @see DynamicDomainListener
     */
    public DNAStringArray supportedDynamicDomains() {
    	DNAStringArray supportedDomains = new DNAStringArray();
    	supportedDomains.addElement("fileType");
        return supportedDomains;
    }
    
    /**
     * Register a dynamic domain list .
     *
     * @param displayProperty  name of the property to be displayed
     */
    public static void setupList() {
    	ExcelFileDynamicDomainListener listener = new ExcelFileDynamicDomainListener();
        HTMLContext.getContext().addDynamicDomainListener(listener);        
    }   
    
}
