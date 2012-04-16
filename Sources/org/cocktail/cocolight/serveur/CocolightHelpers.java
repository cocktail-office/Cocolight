package org.cocktail.cocolight.serveur;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;

public class CocolightHelpers {
    /**
     * Rajoute les styles css aux réponses.
     * @param context le contexte
     * @param response la réponse
     */
    public static void insertStylesSheet(WOContext context, WOResponse response) {
        AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/default.css");
        AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/alert.css");
        AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/lighting.css");

        AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "css/CktlCommon.css");
        AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "css/CktlCommonBleu.css");
        AjaxUtils.addStylesheetResourceInHead(context, response, "app", "styles/cocolight.css");
    }
}
