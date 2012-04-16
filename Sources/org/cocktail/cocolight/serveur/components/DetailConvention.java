/*
 * Copyright COCKTAIL (www.cocktail.org), 1995, 2010 This software 
 * is governed by the CeCILL license under French law and abiding by the
 * rules of distribution of free software. You can use, modify and/or 
 * redistribute the software under the terms of the CeCILL license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 * As a counterpart to the access to the source code and rights to copy, modify 
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. In this 
 * respect, the user's attention is drawn to the risks associated with loading,
 * using, modifying and/or developing or reproducing the software by the user 
 * in light of its specific status of free software, that may mean that it
 * is complicated to manipulate, and that also therefore means that it is 
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the 
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally, 
 * to use and operate it in the same conditions as regards security. The
 * fact that you are presently reading this means that you have had knowledge 
 * of the CeCILL license and that you accept its terms.
 */
package org.cocktail.cocolight.serveur.components;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.components.commons.Tabs;
import org.cocktail.cocolight.serveur.components.commons.Tabs.Tab;
import org.cocktail.cocolight.serveur.components.commons.Tabs.TabsDelegate;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktlwebapp.server.CktlEOUtilities;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

public class DetailConvention extends MyWOComponent implements TabsDelegate {
		
    private static final long serialVersionUID = 5606745819525296597L;
    private static final Logger Log = Logger.getLogger(DetailConvention.class);

    private Contrat convention;

	public static final String BINDING_convention = "convention";

	private Tabs conventionTabs;
	private Tab generalitesTab;
	private Tab partenairesTab;
	private Tab budgetTab;
	private Tab evenementsTab;
	private Tab documentsTab;
	private Tab financementTab;
	private Tab depensesRecettesTab;
	
	private Tab selectedTab;
	
    public DetailConvention(WOContext context) {
        super(context);
    }

    @Override
    public void awake() {
        super.awake();
        if (conventionTabs == null) {
            if (convention().isModeBudgetAvance()) {
                conventionTabs = new Tabs(generalitesTab = new Tab("Généralités", "generalites"), 
                                partenairesTab = new Tab("Détail partenariat", "partenaires"),
                                financementTab = new Tab("Financement", "financement"),
                                depensesRecettesTab = new Tab("Depenses & recettes", "deprec"),
                                budgetTab = new Tab("Budget", "budget"),
                                evenementsTab = new Tab("Évènements", "evenement"),
                                documentsTab = new Tab("Documents", "documents"));
            } else {
                conventionTabs = new Tabs(generalitesTab = new Tab("Généralités", "generalites"), 
                                partenairesTab = new Tab("Détail partenariat", "partenaires"),
                                financementTab = new Tab("Financement", "financement"),
                                budgetTab = new Tab("Budget", "budget"),
                                evenementsTab = new Tab("Évènements", "evenement"),
                                documentsTab = new Tab("Documents", "documents"));
            }
            conventionTabs.setTabsDelegate(this);
        }
    }

	/**
	 * @return the convention
	 */
	public Contrat convention() {
		if (convention == null && hasBinding(BINDING_convention)) {
			convention = (Contrat)valueForBinding(BINDING_convention);
		}
		return convention;
	}

	/**
	 * @param convention the convention to set
	 */
	public void setConvention(Contrat convention) {
		this.convention = convention;
	}

	public Tabs conventionTabs() {
	    return conventionTabs;
	}

	public Tab getBudgetTab() {
        return budgetTab;
    }
	
	public Tab getDocumentsTab() {
        return documentsTab;
    }
	
	public Tab getEvenementsTab() {
        return evenementsTab;
    }
	
	public Tab getFinancementTab() {
        return financementTab;
    }
	
	public Tab getDepensesRecettesTab() {
	    return depensesRecettesTab;
	}
	
	public Tab getGeneralitesTab() {
        return generalitesTab;
    }
	
	public Tab getPartenairesTab() {
        return partenairesTab;
    }
	
	public Tab getSelectedTab() {
        return selectedTab;
    }
	
	public void setSelectedTab(Tab selectedTab) {
        this.selectedTab = selectedTab;
    }
	
	public boolean shouldChangeSelectedTab(Tab selected, Tab willBeSelected) {
	    EOEditingContext ec = selected.getEditingContext();
	    boolean hasChanges = ec.hasChanges();
	    if (hasChanges && Log.isDebugEnabled()) {
//	          session().addSimpleInfoMessage("Cocolight", "Des changements n'ont pas été enregistrés sur l'onglet \"" + 
//	                          selected.getLibelle() + "\", veuillez enregistrer ou annuler ces changements");
	        String message = "\n------------------------------------------------------------------------------------------" + "\n" +
	                         "Passage de l'onglet '" + selected.getLibelle() + "' à l'onglet '" + willBeSelected.getLibelle() + "' : " +
	                         "Des changements n'ont pas été enregistrés sur la convention conOrdre " + convention().conOrdre() + "\n" +
	                         "------------------------------------------------------------------------------------------" + "\n";
	        message = message + CktlEOUtilities.printChanges(ec);
	        Log.debug(message);
	    }
	    // AT : temporairement on laisse passer à l'onglet suivant tant que le mystère des changements non voulus n'est pas résolu
	    return true;
	}
    
}
