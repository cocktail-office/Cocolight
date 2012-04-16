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
package org.cocktail.cocolight.serveur.components.assistants;

import org.cocktail.cocolight.serveur.components.Accueil;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.Assistant;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class AssistantConvention extends Assistant {
	
	public static final String CONTRAT_BDG = "contrat";
	public static final String AVENANT_BDG = "avenant";

	private Contrat contrat;
	private Avenant avenant;
	
	public AssistantConvention(WOContext context) {
        super(context);
    }

	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
	}


	public boolean isPrecedentEnabled() {
		return !isPrecedentDisabled();
	}
	public boolean isSuivantEnabled() {
		return !isSuivantDisabled();
	}

	public boolean isTerminerEnabled() {
		return !isTerminerDisabled();
	}
	public boolean isTerminerDisabled() {
		Contrat contrat = contrat();
		boolean isTerminerDisabled = (contrat == null || contrat.avenantZero() == null ||
				contrat.modeDeGestion() == null || contrat.typeContrat() == null ||
				StringCtrl.isEmpty(contrat.conObjet()) || contrat.dateDebut() == null ||
				contrat.etablissement() == null || contrat.centreResponsabilite() == null ||
				contrat.contratPartenaires() == null || contrat.contratPartenaires().count()<2);
		
		return isTerminerDisabled;
	}

	public WOActionResults apresTerminer() {
		WOActionResults nextPage = (WOActionResults)valueForBinding(ACTION_APRES_TERMINER_BDG);
//		ApresEnregistrement nextPage = (ApresEnregistrement)pageWithName(ApresEnregistrement.class.getName());
//		nextPage.setContrat(contrat);
		return nextPage;		
	}

	public WOActionResults apresEnregistrer() {
		ApresEnregistrement nextPage = (ApresEnregistrement)pageWithName(ApresEnregistrement.class.getName());
		nextPage.setContrat(contrat);
		return nextPage;		
	}
	public WOActionResults annuler() {
		Accueil nextPage = (Accueil)pageWithName(Accueil.class.getName());
		if (editingContext() != null) {
			editingContext().revert();
		} else if (contrat() != null && contrat().editingContext() != null) {
			contrat().editingContext().revert();
		}
		setIndexModuleActif(null);
		return nextPage;
	}

	/**
	 * @return the contrat
	 */
	public Contrat contrat() {
		if (hasBinding(CONTRAT_BDG)) {
			contrat = (Contrat)valueForBinding(CONTRAT_BDG);
		}
		return contrat;
	}
	/**
	 * @param contrat the contrat to set
	 */
	public void setContrat(Contrat contrat) {
		this.contrat = contrat;
		if (hasBinding(CONTRAT_BDG)) {
			setValueForBinding(contrat, CONTRAT_BDG);
		}
	}

	/**
	 * @return the avenant
	 */
	public Avenant avenant() {
		if (hasBinding(AVENANT_BDG)) {
			avenant = (Avenant)valueForBinding(AVENANT_BDG);
		}
		return avenant;
	}


	/**
	 * @param avenantInitial the avenant to set
	 */
	public void setAvenant(Avenant avenant) {
		this.avenant = avenant;
		if (hasBinding(AVENANT_BDG)) {
			setValueForBinding(avenant, AVENANT_BDG);
		}
	}


}
