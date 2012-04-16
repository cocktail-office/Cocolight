package org.cocktail.cocolight.serveur.components.commons;

import org.cocktail.cocolight.serveur.components.MyWOComponent;
import org.cocktail.cocolight.serveur.components.commons.CktlAjaxRepartMultiSelect.NomenclatureHelper;
import org.cocktail.cocowork.server.metier.convention.SbRecette;
import org.cocktail.fwkcktljefyadmin.common.metier.EOLolfNomenclatureRecette;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class CktlAjaxRepartLolfRecette extends MyWOComponent {
    
    public static final String BINDING_RECETTE = "recette";
    private NomenclatureHelper<EOLolfNomenclatureRecette> lolfHelper;
    
    public CktlAjaxRepartLolfRecette(WOContext context) {
        super(context);
        lolfHelper = new LolfNomenclatureRecetteHelper();
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public SbRecette recette() {
        return (SbRecette)valueForBinding(BINDING_RECETTE);
    }
    
    public NomenclatureHelper<EOLolfNomenclatureRecette> getLolfHelper() {
        return lolfHelper;
    }
    
    public class LolfNomenclatureRecetteHelper implements NomenclatureHelper<EOLolfNomenclatureRecette> {

        private NSArray<EOLolfNomenclatureRecette> lolfs;
        private EOLolfNomenclatureRecette currentLolf;
        
        public String nomenclatureLabelKey() {
            return "lolfLibelleLong";
        }

        public NSArray<EOLolfNomenclatureRecette> nomenclatures() {
            if (lolfs == null) {
                int exo = recette().tranche().exercice().exeExercice().intValue();
                lolfs = EOLolfNomenclatureRecette.fetchAllNomenclaturesLevel2or3(recette().editingContext(), exo);
            }
            return lolfs;
        }

        public EOLolfNomenclatureRecette getCurrentNomenclature() {
            return currentLolf;
        }

        public void setCurrentNomenclature(EOLolfNomenclatureRecette currentNomenclature) {
            this.currentLolf = currentNomenclature;
        }
        
        public String currentNomenclatureCss() {
            return null;
        }

        public String currentNomenclatureLabel() {
            return getCurrentNomenclature().lolfLibelleLong();
        }

        public String currentNomenclatureGroup() {
            return null;
        }

        public String currentNomenclatureGroupLabel() {
            return null;
        }

        public String noSelectionString() {
            return "Choisir un code lolf";
        }

        public boolean mustGroup() {
            return false;
        }
        
    }
    
}