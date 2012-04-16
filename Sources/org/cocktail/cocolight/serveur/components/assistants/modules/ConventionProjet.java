package org.cocktail.cocolight.serveur.components.assistants.modules;

import org.cocktail.cocolight.serveur.components.controlers.ConventionProjetCtrl;

import com.webobjects.appserver.WOContext;

public class ConventionProjet extends ModuleAssistant {

	public ConventionProjetCtrl ctrl = null;

	public ConventionProjet(WOContext context) {
        super(context);
    }

	public ConventionProjetCtrl ctrl() {
		if (ctrl == null) {
	        ctrl = new ConventionProjetCtrl(this);
		}
		return ctrl;
    }
	public void setCtrl(ConventionProjetCtrl ctrl) {
		this.ctrl = ctrl;
	}

}