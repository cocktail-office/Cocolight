package org.cocktail.cocolight.serveur.components;

import org.cocktail.cocolight.serveur.CocolightHelpers;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;

public class WrapperModal extends MyWOComponent {

    private static final long serialVersionUID = -2077366146157269189L;

    public WrapperModal(WOContext context) {
        super(context);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
        AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlThemes.framework", "scripts/window.js");
        AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/strings.js");
        AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/formatteurs.js");
        AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/cocolight.js");
        CocolightHelpers.insertStylesSheet(context, response);
    }
    
}