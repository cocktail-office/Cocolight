package org.cocktail.cocolight.serveur;

import org.cocktail.cocowork.server.CocoworkAutorisationCache;
import org.cocktail.fwkcktlpersonne.common.metier.droits.ApplicationUser;

public class CocolightApplicationUser extends ApplicationUser {

    public CocolightApplicationUser(Integer persId) {
        super(new CocoworkAutorisationCache(persId));
    }
    
    public CocoworkAutorisationCache getCocoworkAutorisationCache() {
        return (CocoworkAutorisationCache)getAutorisationsCacheForAppId("COCONUT");
    }
    
}
