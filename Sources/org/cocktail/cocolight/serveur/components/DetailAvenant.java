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

import org.cocktail.cocolight.serveur.components.commons.Tabs;
import org.cocktail.cocolight.serveur.components.commons.Tabs.Tab;
import org.cocktail.cocolight.serveur.components.commons.Tabs.TabsDelegate;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXRedirect;

public class DetailAvenant extends MyWOComponent implements TabsDelegate {
	
    private static final long serialVersionUID = -2409949548829536321L;

    private Contrat convention;

	public static final String BINDING_convention = "convention";
	public static final String BINDING_avenant = "avenant";
	
	private Tabs avenantTabs;
	private Tab generalitesTab;
	private Tab evenementsTab;
	private Tab documentsTab;
	
    public DetailAvenant(WOContext context) {
        super(context);
        avenantTabs = new Tabs(generalitesTab = new Tab("Généralités", "generalites"), 
                        evenementsTab = new Tab("Évènements", "evenement"),
                        documentsTab = new Tab("Documents", "documents"));
        avenantTabs.setTabsDelegate(this);    
    }

    public WOActionResults succesEtRafraichir() {
        session().addSimpleInfoMessage("Cocolight", "Les modifications ont bien été enregistrées");
        ERXRedirect redirect = (ERXRedirect)pageWithName(ERXRedirect.class.getName());
        redirect.setComponent(context().page());
        return redirect;
    }
    
    public WOActionResults annulerEtRafraichir() {
        session().addSimpleInfoMessage("Cocolight", "Les modifications ont bien été annulées");
        ERXRedirect redirect = (ERXRedirect)pageWithName(ERXRedirect.class.getName());
        redirect.setComponent(context().page());
        return redirect;
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
	/**
	 * @return the avenant
	 */
	public Avenant avenant() {
		return (Avenant)valueForBinding(BINDING_avenant);
	}

	public Tabs getAvenantTabs() {
        return avenantTabs;
    }
	
	public Tab getDocumentsTab() {
        return documentsTab;
    }
	
	public Tab getEvenementsTab() {
        return evenementsTab;
    }
	
	public Tab getGeneralitesTab() {
        return generalitesTab;
    }
	
    public boolean shouldChangeSelectedTab(Tab selected, Tab willBeSelected) {
        boolean hasChanges = selected.getEditingContext().hasChanges();
        if (hasChanges) {
              session().addSimpleInfoMessage("Cocolight", "Des changements n'ont pas été enregistrés sur l'onglet \"" + 
                              selected.getLibelle() + "\", veuillez enregistrer ou annuler ces changements");
        }
        return !hasChanges;
    }

}
