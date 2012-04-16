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
package org.cocktail.cocolight.serveur.components.controlers;

import org.cocktail.cocolight.serveur.Application;
import org.cocktail.cocolight.serveur.components.assistants.modules.ConventionIndicateurs;
import org.cocktail.cocowork.server.metier.convention.IndicateursContrat;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryContrat;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.modules.IModuleAssistant;
import org.cocktail.fwkcktlaccordsguiajax.components.controlers.CtrlModule;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.foundation.NSArray;

public class ConventionIndicateursCtrl extends CtrlModule {

    private ConventionIndicateurs wocomponent;

	private IndicateursContrat unIndicateur;
	private NSArray lesIndicateurs;
	
    public ConventionIndicateursCtrl(IModuleAssistant component) {
        super(component);
    }
	
	public NSArray lesIndicateurs() {
		if (lesIndicateurs == null) {
			lesIndicateurs = EOUtilities.objectsForEntityNamed(editingContext(), IndicateursContrat.ENTITY_NAME);
		}
		return lesIndicateurs;
	}
	/**
	 * @return the unIndicateur
	 */
	public IndicateursContrat unIndicateur() {
		return unIndicateur;
	}

	/**
	 * @param unIndicateur the unIndicateur to set
	 */
	public void setUnIndicateur(IndicateursContrat unIndicateur) {
		this.unIndicateur = unIndicateur;
	}


	/**
	 * @return the isIndicateurChecked
	 */
	public boolean isIndicateurChecked() {
		NSArray indicateursContrat = (NSArray)wocomponent.contrat().repartIndicateursContrat().valueForKey("indicateursContrat");
		boolean isIndicateurChecked = indicateursContrat.contains(unIndicateur());
		return isIndicateurChecked;
	}

	/**
	 * @param isIndicateurChecked the isIndicateurChecked to set
	 */
	public void setIsIndicateurChecked(boolean isIndicateurChecked) {
		FactoryContrat fc = new FactoryContrat(editingContext(), ((Application)wocomponent.application()).isModeDebug());
		if (isIndicateurChecked) {
			fc.activerIndicateur(wocomponent.contrat(), unIndicateur());
		} else {
			fc.desactiverIndicateur(wocomponent.contrat(), unIndicateur());
		}
	}

	
}
