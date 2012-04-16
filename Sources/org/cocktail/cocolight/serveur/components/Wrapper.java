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

import java.util.GregorianCalendar;

import org.cocktail.cocolight.serveur.Application;
import org.cocktail.cocolight.serveur.CocolightHelpers;
import org.cocktail.cocolight.serveur.VersionMe;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxValue;
import er.ajax.AjaxUtils;

public class Wrapper extends MyWOComponent {
    private String onloadJS;
	private String erreurScript;
	private String titre;

	public Wrapper(WOContext context) {
        super(context);
    }

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {

		super.appendToResponse(response, context);

		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

//		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlAjaxWebExt.framework", "css/jquery/jquery-ui/jquery-ui.css");
//		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlAjaxWebExt.framework", "css/jquery/jquery.pnotify.default.css");
//		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlAjaxWebExt.framework", "scripts/jquery/jquery-1.4.2.js");
//		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlAjaxWebExt.framework", "scripts/jquery/jquery-ui-1.8.4.min.js");
//		AjaxUtils.addScriptCodeInHead(response, context, "jQuery.noConflict();");
//		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlAjaxWebExt.framework", "scripts/jquery/jquery.pnotify.js");

		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlThemes.framework", "scripts/window.js");
		AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/strings.js");
		AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/formatteurs.js");
		AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/cocolight.js");

		CocolightHelpers.insertStylesSheet(context, response);
		
		addScriptResource(response, "jscript/wz_tooltip.js", null, "FwkCktlWebApp.framework", 
		                            RESOURCE_TYPE_JSCRIPT, RESOURCE_DEST_END_BODY, RESOURCE_FROM_WEB_SERVER_RESOURCES);
		

	}
	
	public Erreur pageErreur() {
		Erreur nextPage = (Erreur)pageWithName(Erreur.class.getName());
		return nextPage;
	}
//	public ApresEnregistrement pageApresEnregistrer() {
//		ApresEnregistrement nextPage = (ApresEnregistrement)pageWithName(ApresEnregistrement.class.getName());
//		return nextPage;
//	}



	/**
	 * @return the onloadJS
	 */
	public String onloadJS() {
//		if (((Session)session()).isApresEnregistrer()) {
//			onloadJS = "openWinPageApresEnregistrer();";
//		}
		return onloadJS;
	}

	/**
	 * @param onloadJS the onloadJS to set
	 */
	public void setOnloadJS(String onloadJS) {
		this.onloadJS = onloadJS;
	}

	/**
	 * @return the erreurScript
	 */
	public String erreurScript() {
		String messageErreur = session().messageErreur();
		if (!StringCtrl.isEmpty(messageErreur) && !messageErreur.startsWith("[COCOLIGHT]:Exception")) {
			if (messageErreur.indexOf("ORA-") > -1) {
				messageErreur = messageErreur.substring(messageErreur.indexOf("ORA-")+10);
			}
			erreurScript = "alert("+AjaxValue.javaScriptEscaped(messageErreur)+");";
			session().setMessageErreur(null);
		} else {
			erreurScript = null;
		}
		 
		return erreurScript;
	}

	/**
	 * @param erreurScript the erreurScript to set
	 */
	public void setErreurScript(String erreurScript) {
		this.erreurScript = erreurScript;
	}

	/**
	 * @return the titre
	 */
	public String titre() {
		if (titre == null) {
			titre = "Cocolight : Portail des actes de partenariat dans le SI cocktail";
		}
		return titre;
	}

	/**
	 * @param titre the titre to set
	 */
	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String copyright() {
		return "(c) "+DateCtrl.nowDay().get(GregorianCalendar.YEAR)+" Cocktail";
	}

	public String version() {
		return VersionMe.htmlAppliVersion();
	}

	public WOActionResults quitter() {
		session().defaultEditingContext().revert();
    	return session().onQuitter();
	}

	public WOActionResults accueil() {
		session().reset();
		return pageWithName(Accueil.class.getName());
	}

	public String serverId() {
		return Application.serverBDId();
	}

}
