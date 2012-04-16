package org.cocktail.cocolight.serveur.components.commons;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;

public class TrancheValideColonne extends WOComponent {
    public TrancheValideColonne(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public String frameworkNameLocal() {
        return frameworkName();
    }
    
}