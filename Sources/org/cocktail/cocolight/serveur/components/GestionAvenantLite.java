package org.cocktail.cocolight.serveur.components;

import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;

import er.ajax.AjaxUtils;

public class GestionAvenantLite extends MyWOComponent {
    
    private Avenant avenant;
    
    public GestionAvenantLite(WOContext context) {
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
    
    public Avenant avenant() {
        return avenant;
    }
    
    public void setAvenant(Avenant avenant) {
        this.avenant = avenant;
    }
    
    public EOEditingContext editingContext() {
        return avenant().editingContext();
    }
    
    public WOActionResults fermer() {
        editingContext().revert();
        CktlAjaxWindow.close(context());
        return null;
    }
    
}