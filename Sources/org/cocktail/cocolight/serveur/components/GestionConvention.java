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

import java.util.Enumeration;

import org.cocktail.cocolight.serveur.Session;
import org.cocktail.cocolight.serveur.components.assistants.ApresEnregistrement;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktlpersonne.common.metier.EORepartAssociation;
import org.cocktail.fwkcktlpersonne.common.metier.EORepartStructure;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXRedirect;
import er.extensions.foundation.ERXStringUtilities;

public class GestionConvention extends MyWOComponent {

	private Contrat contrat;
	private NSArray<String> modules;
	private NSArray<String> etapes;
	
	public GestionConvention(WOContext context) {
        super(context);
    }

	public String moduleName() {
		String moduleName = null;
		if (modules() != null && modules().count()>0) {
			moduleName = (String)modules().objectAtIndex(session().indexModuleActifCreationConvention().intValue());
		}
		return moduleName;
	}

	/**
	 * @return the modules
	 */
	public NSArray<String> modules() {
	    if (contrat() != null) {
	        NSMutableArray<String> modulesTmp = new NSMutableArray<String>("ConventionGeneralites");
	        if (session().applicationUser().hasDroitConsultationPartenaires())
	            modulesTmp.addObject("ConventionPartenaires");
	        modules = modulesTmp.immutableClone();
	    }
	    session().setModulesCreationConvention(modules);
	    return modules;
	}
	
	/**
	 * @param modules the modules to set
	 */
	public void setModules(NSArray modules) {
		this.modules = modules;
		((Session)session()).setModulesGestionContact(modules);
	}

	public NSArray<String> etapes() {
		if (etapes == null) {
		    NSMutableArray<String> etapesTmp = new NSMutableArray<String>("G&eacute;n&eacute;ralit&eacute;s");
		    if (session().applicationUser().hasDroitConsultationPartenaires())
		        etapesTmp.addObject("Partenaires");
		    etapes = etapesTmp.immutableClone();
		}
		return etapes;
	}

	public WOActionResults terminer() {
		boolean isCreation = contrat().numeroContrat().endsWith("????");
		WOActionResults result = null;
		try {
			EOEditingContext edc = contrat().editingContext();
			edc.saveChanges();
			String referenceDocumentaire = contrat().conReferenceExterne();
			if (ERXStringUtilities.emptyStringForNull(referenceDocumentaire).length()==0) {
				referenceDocumentaire = contrat().numeroContrat()+"/DOC";
				contrat().setConReferenceExterne(referenceDocumentaire);
			}
			String libelleGroupe = "Partenaires de l'acte " + contrat().numeroContrat();
			EOStructure groupePartenaires = contrat().groupePartenaire();
			// Maj des dates des differents roles des differents partenaires et contacts
			NSArray<EORepartStructure> partenairesEtContacts = groupePartenaires.toRepartStructuresElts();
			Enumeration<EORepartStructure> enumPartenairesEtContacts = partenairesEtContacts.objectEnumerator();
			while (enumPartenairesEtContacts.hasMoreElements()) {
				EORepartStructure eoRepartStructure = (EORepartStructure) enumPartenairesEtContacts.nextElement();
				NSArray<EORepartAssociation> lesRoles = eoRepartStructure.toRepartAssociations(null);
				Enumeration<EORepartAssociation> enumLesRoles = lesRoles.objectEnumerator();
				while (enumLesRoles.hasMoreElements()) {
					EORepartAssociation eoRepartAssociation = (EORepartAssociation) enumLesRoles.nextElement();
					eoRepartAssociation.setRasDOuverture(contrat().dateDebut());
					eoRepartAssociation.setRasDFermeture(contrat().dateFin());
				}
			}
			if (isCreation) {
				// Renommer le groupe partenaire
				groupePartenaires.setStrAffichage(libelleGroupe);
			}
			edc.saveChanges();
			session().addSimpleInfoMessage("Cocolight", "La convention a bien été créée");
	        ERXRedirect redirect = (ERXRedirect)pageWithName(ERXRedirect.class.getName());
	        Convention nextPage = (Convention)pageWithName(Convention.class.getName());
	        nextPage.setConvention(contrat());
	        session().setPageConvention(nextPage);
	        redirect.setComponent(nextPage);
	        result = redirect;
		} catch (ValidationException e2) {
			context().response().setStatus(500);
			session().setMessageErreur(e2.getMessage());
		} catch (Throwable e1) {
			context().response().setStatus(500);
			edc().revert();
			throw NSForwardException._runtimeExceptionForThrowable(e1);
		} 
		return result;
	
	}

	public WOActionResults apresTerminer() {
		ApresEnregistrement nextPage = (ApresEnregistrement)pageWithName(ApresEnregistrement.class.getName());
		nextPage.setContrat(contrat());
		return nextPage;		
	}

	/**
	 * @return the contrat
	 */
	public Contrat contrat() {
		return contrat;
	}

	/**
	 * @param contrat the contrat to set
	 */
	public void setContrat(Contrat contrat) {
		this.contrat = contrat;
	}


}
