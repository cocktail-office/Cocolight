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
package org.cocktail.cocolight.serveur.components.assistants.modules;

import org.cocktail.cocolight.serveur.components.controlers.ConventionGeneralitesCtrl;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ContratPartContact;
import org.cocktail.cocowork.server.metier.convention.ContratPartenaire;
import org.cocktail.cocowork.server.metier.convention.Parametre;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.Assistant;
import org.cocktail.fwkcktlpersonne.common.metier.EOSecretariat;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;


public class ConventionGeneralites extends ModuleAssistant {
	
	public ConventionGeneralitesCtrl ctrl = null;

	private Boolean isAjouterCentreAuxPartenairesChecked;
	private Boolean isAjouterSecretairesAuxContactsChecked;
	private Avenant currentAvenant;
	
    public ConventionGeneralites(WOContext context) {
        super(context);
    }
    
	public ConventionGeneralitesCtrl ctrl() {
		if (ctrl == null) {
	        ctrl = new ConventionGeneralitesCtrl(this);
		}
		return ctrl;
	}

	public void setCtrl(ConventionGeneralitesCtrl ctrl) {
		this.ctrl = ctrl;
	}
	
	@Override
	public boolean valider() {
		boolean validate = false;
		Contrat contrat = contrat();
		NSArray<String> failureMessages = new NSArray<String>();
		Assistant assistant = (Assistant)parent();
		
		if (contrat != null && assistant != null) {
			if (contrat.etablissement() == null) {
				failureMessages = failureMessages.arrayByAddingObject("l'\u00E9tablissement gestionnaire");
			}
			if (contrat.centreResponsabilite() == null) {
				failureMessages = failureMessages.arrayByAddingObject("le service gestionnaire");
			}
			if (contrat.modeDeGestion() == null) {
				failureMessages = failureMessages.arrayByAddingObject("le mode de gestion");
			}
			if (contrat.typeContrat() == null) {
				failureMessages = failureMessages.arrayByAddingObject("la nature");
			}
			if (contrat.conObjet() == null || contrat.conObjet().length()==0) {
				failureMessages = failureMessages.arrayByAddingObject("l'objet");
			}
			if (contrat.dateDebut() == null) {
				failureMessages = failureMessages.arrayByAddingObject("la date de d\u00E9but");
			}
			if (failureMessages.count()==0) {
				validate = true;
				assistant.setFailureMessage(null);
			} else {
				assistant.setFailureMessage("Veuillez renseigner "+failureMessages.componentsJoinedByString(", ")+".");
			}
		} else {
			assistant.setFailureMessage("Veuillez renseigner les champs obligatoires (en rouge).");
		}
    	return validate;
    }
	public Boolean isAjouterCentreAuxPartenairesChecked() {
//		if (contrat().centreResponsabilite() == null) {
//			isAjouterCentreAuxPartenairesChecked = false;
//		}
		return isAjouterCentreAuxPartenairesChecked;
	}
	public void setIsAjouterCentreAuxPartenairesChecked(Boolean isAjouterCentreAuxPartenairesChecked) {
		this.isAjouterCentreAuxPartenairesChecked = isAjouterCentreAuxPartenairesChecked;
	}
	public Boolean isAjouterSecretairesAuxContactsChecked() {
//		isAjouterSecretairesAuxContactsChecked = contrat().partenaireCentreResponsabilite()!=null && isAjouterSecretairesAuxContactsDisabled();
//		if (contrat().centreResponsabilite() == null || contrat().partenaireCentreResponsabilite() == null) {
//			isAjouterSecretairesAuxContactsChecked = false;
//		}
		
		return isAjouterSecretairesAuxContactsChecked;
	}
	public void setIsAjouterSecretairesAuxContactsChecked(Boolean isAjouterSecretairesAuxContactsChecked) {
		this.isAjouterSecretairesAuxContactsChecked = isAjouterSecretairesAuxContactsChecked;
	}

	public boolean isAjouterCentreAuxPartenairesDisabled() {
		return (contrat().centreResponsabilite() == null || contrat().partenaireCentreResponsabilite()!=null);
	}

	public boolean isAjouterSecretairesAuxContactsDisabled() {
		boolean isAjouterSecretairesAuxContactsDisabled = true;
		if (contrat().centreResponsabilite() != null && contrat().partenaireCentreResponsabilite() != null) {
			ContratPartenaire partenaire = contrat().partenaireCentreResponsabilite();
			NSArray lesContacts = partenaire.contratPartContacts();
			NSArray lesContactsIndividus = (NSArray)lesContacts.valueForKeyPath(ContratPartContact.PERSONNE_KEY);
			NSArray lesSecretaires = contrat().centreResponsabilite().toSecretariats();
			NSArray lesSecretairesIndividus = (NSArray)lesSecretaires.valueForKeyPath(EOSecretariat.TO_INDIVIDU_KEY);
			NSSet setLesContacts = new NSSet(lesContactsIndividus);
			NSSet setLesSecretaires = new NSSet(lesSecretairesIndividus);
			isAjouterSecretairesAuxContactsDisabled = setLesSecretaires.isSubsetOfSet(setLesContacts);
		}
		return isAjouterSecretairesAuxContactsDisabled;
	}

	@Override
	public boolean disabled() {
	    if (contrat().isSigne()) {
	        return !Parametre.paramBooleanForKey(Parametre.GENERALITES_MODIFIABLES_APRES_VALIDATION);
	    } else {
	        return super.disabled();
	    }
	}
	
	public boolean isCBLucrativeDisabled() {
	    if (disabled())
	        return true;
	    else {
	        return (contrat() != null && contrat().modeDeGestion() != null && contrat().modeDeGestion().isModeRA());
	    }
	}
	
	public boolean isCBCreditsLimitatifsDisabled() {
	    if (disabled())
	        return true;
	    else {
	        return (contrat() != null && contrat().modeDeGestion() != null && contrat().modeDeGestion().isModeRA());
	    }
	}

	public boolean isTypeOptionnelDisabled() {
	    if (disabled())
	        return true;
	    else {
	        boolean isTypeOptionnelDisabled = true;
	        if (contrat() != null && ctrl() != null && ctrl().lesTypesOptionnels() != null && ctrl().lesTypesOptionnels().count() > 0) {
	            isTypeOptionnelDisabled = false;
	        }
	        return isTypeOptionnelDisabled;
	    }
	}

	public Avenant getCurrentAvenant() {
        return currentAvenant;
    }
	
	public void setCurrentAvenant(Avenant currentAvenant) {
        this.currentAvenant = currentAvenant;
    }
	
	public NSTimestamp contratDateDebut() {
        return contrat().dateDebut();
    }
    
    public void setContratDateDebut(NSTimestamp date) {
        try {
            contrat().setDateDebut(date);
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
    }
    
    public NSTimestamp contratDateFin() {
        return contrat().dateFin();
    }
    
    public void setContratDateFin(NSTimestamp date) {
        try {
            contrat().setDateFin(date);
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
    }

}
