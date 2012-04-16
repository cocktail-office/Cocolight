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

import org.cocktail.cocolight.serveur.Application;
import org.cocktail.cocolight.serveur.Session;
import org.cocktail.cocolight.serveur.components.Accueil;
import org.cocktail.cocolight.serveur.components.Convention;
import org.cocktail.fwkcktlaccordsguiajax.components.controlers.CtrlModule;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSForwardException;

public class ModuleAssistant extends org.cocktail.fwkcktlaccordsguiajax.components.assistants.modules.ModuleAssistant {

	public static final String BDG_utilisateurPersId = "utilisateurPersId";

	private Integer utilisateurPersId;
	
	public ModuleAssistant(WOContext context) {
		super(context);
	}

	public ModuleAssistant(WOContext context, CtrlModule controler) {
		super(context,controler);
	}

    public Session session() {
        return (Session)super.session();
    }

    public Application application() {
        return (Application)super.application();
    }
    
	/**
	 * @return the utilisateurPersId
	 */
	public Integer utilisateurPersId() {
		return utilisateurPersId;
	}

	/**
	 * @param utilisateurPersId the utilisateurPersId to set
	 */
	public void setUtilisateurPersId(Integer utilisateurPersId) {
		this.utilisateurPersId = utilisateurPersId;
	}
	
    public WOActionResults enregistrer() {
        try {
            contrat().avantModification(applicationUser().getUtilisateur().localInstanceIn(editingContext()));
            editingContext().saveChanges();
            session().addSimpleInfoMessage("Cocolight", "Convention " + contrat().numeroContrat() + " modifiée avec succès");
        } catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        } catch (Throwable e) {
            context().response().setStatus(500);
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        return null;
    }
    
    public WOActionResults accueil() {
        return (Accueil)pageWithName(Accueil.class.getName());
    }
    
    public WOActionResults consulter() {
        Convention nextPage = (Convention)pageWithName(Convention.class.getName());
        nextPage.setConvention(contrat());
        session().setPageConvention(nextPage);
        return nextPage;
    }
	
}
