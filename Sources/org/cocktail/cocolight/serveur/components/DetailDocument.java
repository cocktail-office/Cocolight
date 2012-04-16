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

import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.AvenantDocument;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryAvenant;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

public class DetailDocument extends MyWOComponent {

	private Contrat convention;
	private Avenant avenant;
	private AvenantDocument avenantDocument;

	
	public DetailDocument(WOContext context) {
        super(context);
    }

	/**
	 * @return the convention
	 */
	public Contrat convention() {
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
		return avenant;
	}

	/**
	 * @param avenant the avenant to set
	 */
	public void setAvenant(Avenant avenant) {
		this.avenant = avenant;
	}

	/**
	 * @return the avenantDocument
	 */
	public AvenantDocument avenantDocument() {
		return avenantDocument;
	}

	/**
	 * @param avenantDocument the avenantDocument to set
	 */
	public void setAvenantDocument(AvenantDocument avenantDocument) {
		this.avenantDocument = avenantDocument;
	}

	public WOComponent submit() {		
		return null;
	}


	public String disabledTFScript() {
		String disabledTFScript = "";
		
		if (convention() == null || !convention().isModifiablePar(session().applicationUser().getUtilisateur())) {
			//disabledTFScript = "$('FormGeneralites').disable();";
			disabledTFScript = "document.forms[0].disable();";
		}
		
		return disabledTFScript;
	}
	
	public String titreVisualiser() {
		String titre = null;
		if (avenantDocument() != null) {
			titre = "Visualiser "+avenantDocument().document().objet();
		}
		return titre;
	}
	public String titreSupprimer() {
		String titre = null;
		if (avenantDocument() != null) {
			titre = "Supprimer "+avenantDocument().document().objet();
		}
		return titre;
	}
	public String href() {
		return null;
	}

	public boolean isSupprimerDocumentDisabled() {
		return !isSupprimerDocumentEnabled();
	}

	public boolean isSupprimerDocumentEnabled() {
		return convention().isModifiablePar(session().applicationUser().getUtilisateur()) || session().applicationUser().hasDroitSuppressionContratsEtAvenants();
	}

	public WOActionResults supprimerUnDocument() throws Exception {
		EOEditingContext ec = convention().editingContext();
		FactoryAvenant fc = new FactoryAvenant(ec, application().isModeDebug());
		try {
			fc.supprimerDocument(ec, avenantDocument(), session().applicationUser(), Integer.valueOf(application.config().intForKey("ROOT_GED_GROUPE_PARTENAIRE")));
			ec.saveChanges();
		} catch (Exception e) {
			e.printStackTrace();
			context().response().setStatus(500);
			session().setMessageErreur(e.getMessage());
		}
		return null;
	}

}
