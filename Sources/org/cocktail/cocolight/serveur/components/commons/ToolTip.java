package org.cocktail.cocolight.serveur.components.commons;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

public class ToolTip extends ERXStatelessComponent {
    
    public static final String BINDING_HTMLTEXT = "htmlText";
    
    public ToolTip(WOContext context) {
        super(context);
    }
    
    public String htmlText() {
        String htmlText = stringValueForBinding(BINDING_HTMLTEXT);
        return "<div class='tip'>" + htmlText + "</div>";
    }
    
}