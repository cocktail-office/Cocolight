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

import org.cocktail.cocolight.serveur.Session;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxUtils;
import er.extensions.eof.ERXEC;

public class GestionAvenant extends MyWOComponent {

	private EOEditingContext editingContext = null;
	private NSArray modules;
	private NSArray<String> etapes;
	private Avenant avenant;
	private String erreurScript;

	public GestionAvenant(WOContext context) {
        super(context);
    }

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);

		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/default.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/alert.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/lighting.css");

		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlThemes.framework", "scripts/window.js");
		AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/cocolight.js");

		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/default.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/alert.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/lighting.css");

		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "css/CktlCommon.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "css/CktlCommonBleu.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "app", "styles/cocolight.css");
	}

	public WOActionResults annuler() {
		if (avenant != null) {
			try {
				avenant.contrat().removeFromAvenantsRelationship(avenant);
				edc().deleteObject(avenant);
				CktlAjaxWindow.close(context(), "AddAvenantModalBox");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

	public WOActionResults terminer() {
		if (avenant != null) {
			try {
				edc().saveChanges();
				Convention conventionPage = session().pageConvention();
				conventionPage.setAvenant(avenant);
//				conventionPage.setRefreshContratTreeView(true);
				CktlAjaxWindow.close(context(), "AddAvenantModalBox");
			} catch (Exception e) {
				e.printStackTrace();
				context().response().setStatus(500);
				session().setMessageErreur(e.getMessage());
			}

		}
		return null;
	}
	
	public String moduleName() {
		String moduleName = null;
		if (modules() != null && modules().count() > 0) {
			moduleName = (String) modules().objectAtIndex(
			session().indexModuleActifCreationAvenant().intValue());
		}
		return moduleName;
	}

	/**
	 * @return the modules
	 */
	public NSArray modules() {
		if (avenant() != null) {
			// TODO Integrer l'ajout des documents dans l'assistant de creation d'avenant --> Pb de multi fenetres modales
		    NSMutableArray<String> modulesTmp = new NSMutableArray<String>("AvenantGeneralites");
		    if (session().applicationUser().hasDroitConsultationEvenements())
                modulesTmp.addObject("AvenantDatesEtEvenements");
            if (session().applicationUser().hasDroitConsultationDocuments())
                modulesTmp.addObject("AvenantDocuments");
            modules = modulesTmp.immutableClone();
		}
		session().setModulesCreationAvenant(modules);
		return modules;
	}

	/**
	 * @param modules
	 *            the modules to set
	 */
	public void setModules(NSArray modules) {
		this.modules = modules;
		((Session) session()).setModulesGestionContact(modules);
	}

	public NSArray<String> etapes() {
		if (avenant() != null) {
		    NSMutableArray<String> etapesTmp = new NSMutableArray<String>("G&eacute;n&eacute;ralit&eacute;s");
            if (session().applicationUser().hasDroitConsultationEvenements())
                etapesTmp.addObject("Ev&egrave;nements");
            if (session().applicationUser().hasDroitConsultationDocuments())
                etapesTmp.addObject("Documents");
			etapes = etapesTmp.immutableClone();
		}
		return etapes;
	}

	/**
	 * @return the editingContext
	 */
	public EOEditingContext editingContext() {
		if (editingContext == null) {
			if (avenant() != null) {
				editingContext = avenant().editingContext();
			} else {
				editingContext = ERXEC.newEditingContext();
			}
		}
		return editingContext;
	}

	/**
	 * @param editingContext
	 *            the editingContext to set
	 */
	public void setEditingContext(EOEditingContext editingContext) {
		this.editingContext = editingContext;
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
	 * @return the erreurScript
	 */
	public String erreurScript() {
		String messageErreur = session().messageErreur();
		if (!StringCtrl.isEmpty(messageErreur) && !messageErreur.startsWith("[COCOLIGHT]:Exception")) {
			if (messageErreur.indexOf("ORA-") > -1) {
				messageErreur = messageErreur.substring(messageErreur.indexOf("ORA-")+10);
				messageErreur = messageErreur.replaceAll("'", "\\\\'");
				messageErreur = messageErreur.replaceAll("\n", " ");
				messageErreur = messageErreur.replaceAll("\"", "");
			}
			erreurScript = "alert('"+messageErreur+"');";
			session().setMessageErreur(null);
		} else {
			erreurScript = null;
		}
		 
		return erreurScript;
	}


}
