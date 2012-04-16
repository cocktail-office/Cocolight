package org.cocktail.cocolight.serveur.components.commons;

import org.cocktail.cocolight.serveur.components.MyWOComponent;
import org.cocktail.cocolight.serveur.components.commons.CktlAjaxRepartMultiSelect.NomenclatureHelper;
import org.cocktail.cocowork.server.metier.convention.SbDepense;
import org.cocktail.fwkcktljefyadmin.common.metier.EOLolfNomenclatureDepense;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class CktlAjaxRepartLolfDepense extends MyWOComponent {
    
    private static final long serialVersionUID = -4692755168064005529L;
    public static final String BINDING_DEPENSE = "depense";
    private NomenclatureHelper<EOLolfNomenclatureDepense> lolfHelper;

    public CktlAjaxRepartLolfDepense(WOContext context) {
        super(context);
        lolfHelper = new LolfNomenclatureDepenseHelper();
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public SbDepense depense() {
        return (SbDepense)valueForBinding(BINDING_DEPENSE);
    }
    
    public NomenclatureHelper<EOLolfNomenclatureDepense> getLolfHelper() {
        return lolfHelper;
    }
    
    public class LolfNomenclatureDepenseHelper implements NomenclatureHelper<EOLolfNomenclatureDepense> {

        private NSArray<EOLolfNomenclatureDepense> lolfs;
        private EOLolfNomenclatureDepense currentLolf;
        
        public String nomenclatureLabelKey() {
            return "lolfLibelleLong";
        }

        public NSArray<EOLolfNomenclatureDepense> nomenclatures() {
            if (lolfs == null) {
                int exo = depense().tranche().exercice().exeExercice().intValue();
                lolfs = EOLolfNomenclatureDepense.fetchAllNomenclaturesLevel2or3(depense().editingContext(), exo);
            }
            return lolfs;
        }

        public EOLolfNomenclatureDepense getCurrentNomenclature() {
            return currentLolf;
        }

        public void setCurrentNomenclature(EOLolfNomenclatureDepense currentNomenclature) {
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