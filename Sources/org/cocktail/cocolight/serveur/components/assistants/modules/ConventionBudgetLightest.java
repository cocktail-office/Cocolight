package org.cocktail.cocolight.serveur.components.assistants.modules;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.cocktail.cocowork.server.metier.convention.TrancheBudget;
import org.cocktail.cocowork.server.metier.convention.TrancheBudgetRec;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetMasqueCredit;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeCredit;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

public class ConventionBudgetLightest extends ConventionBudgetLight {
    
    private NSDictionary dicoTranchesAnnuellesLight;
    
    public ConventionBudgetLightest(WOContext context) {
        super(context);
    }
    
    public NSDictionary dicoTranchesAnnuellesLight() {
        if (dicoTranchesAnnuellesLight == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AvenantTranchesAnnuellesLight.plist", null, NSArray.EmptyArray));
            dicoTranchesAnnuellesLight = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoTranchesAnnuellesLight;
    }

    @Override
    public WOActionResults supprimerCreditDepense() {
        try {
            TrancheBudget.supprimerTrancheBudget(ecForBudget(), selectedTrancheBudget(), null);
            saveChanges("Suppression de ligne de crédit enregistrée avec succès");
            resetCachesAllDepenses();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
 
    @Override
    public WOActionResults ajouterCreditDepense() {
        try {
            setNewTrancheBudget(TrancheBudget.creerTrancheBudget(selectedTranche(), 
                                                                application().getDernierExerciceOuvertOuEnPreparation(), 
                                                                applicationUser().getUtilisateur().utlOrdre()));
            // On positionne le reste à positionner sur le montant par défaut
            getNewTrancheBudget().setTbMontant(resteAPositionner());
            resetNomenclatures();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    @Override
    public WOActionResults supprimerCreditRecette() {
        try {
            TrancheBudgetRec.supprimerTrancheBudgetRec(ecForBudget(), selectedTrancheBudgetRec(), null);
            saveChanges("Suppression de ligne de crédit enregistrée avec succès");
            resetCachesAllRecettes();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    @Override
    public WOActionResults ajouterCreditRecette() {
        try {
            setNewTrancheBudgetRec(TrancheBudgetRec.creerTrancheBudget(selectedTranche(), 
                            application().getDernierExerciceOuvertOuEnPreparation(), 
                            applicationUser().getUtilisateur().utlOrdre()));
            // On positionne le reste à positionner sur le montant par défaut
            getNewTrancheBudgetRec().setTbrMontant(resteAPositionner());
            resetNomenclatures();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    @Override
    public BigDecimal resteAPositionner() {
        return selectedTranche().traMontant().subtract(selectedTranche().totalPositionne());
    }
    
    @Override
    public BigDecimal newTrancheBudgetPct() {
        BigDecimal result = BigDecimal.ZERO;
        if (getNewTrancheBudget() != null) {
            BigDecimal montantTotal = selectedTranche().traMontant();
            if (montantTotal.compareTo(BigDecimal.ZERO) != 0) {
                result = getNewTrancheBudget().tbMontant().divide(montantTotal, 5, RoundingMode.HALF_UP);
                result = result.multiply(BigDecimal.valueOf(100));
            }
        }
        return result;
    }
    
    @Override
    public void setNewTrancheBudgetPct(BigDecimal pct) {
        BigDecimal montantAPositionne = selectedTranche().traMontant().multiply(pct).divide(BigDecimal.valueOf(100));
        getNewTrancheBudget().setTbMontant(montantAPositionne);
    }
    
    @Override
    public BigDecimal newTrancheBudgetRecPct() {
        BigDecimal result = BigDecimal.ZERO;
        if (getNewTrancheBudgetRec() != null) {
            BigDecimal montantTotal = selectedTranche().traMontant();
            if (montantTotal.compareTo(BigDecimal.ZERO) != 0) {
                result = getNewTrancheBudgetRec().tbrMontant().divide(montantTotal, 5, RoundingMode.HALF_UP);
                result = result.multiply(BigDecimal.valueOf(100));
            }
        }
        return result;
    }
    
    @Override
    public BigDecimal resteAPositionnerRec() {
        return selectedTranche().traMontant().subtract(selectedTranche().totalPositionneRec());
    }
    
    @Override
    public void setNewTrancheBudgetRecPct(BigDecimal pct) {
        BigDecimal montantAPositionne = selectedTranche().traMontant().multiply(pct).divide(BigDecimal.valueOf(100));
        getNewTrancheBudgetRec().setTbrMontant(montantAPositionne);
    }
    
    public NSArray<EOTypeCredit> getAllTypesCreditDepense() {
        if (typesCreditDepense == null) {
            typesCreditDepense = FinderBudgetMasqueCredit.findTypesCreditExecFromNature(ecForBudget(), getExercice(), null, EOTypeCredit.TCD_TYPE_DEPENSE);
        }
        return typesCreditDepense;
    }
    
    public NSArray<EOTypeCredit> getAllTypesCreditRecette() {
        if (typesCreditRecette == null) {
            typesCreditRecette = FinderBudgetMasqueCredit.findTypesCreditExecFromNature(ecForBudget(), getExercice(), null, EOTypeCredit.TCD_TYPE_RECETTE);
        }
        return typesCreditRecette;
    }
    
}