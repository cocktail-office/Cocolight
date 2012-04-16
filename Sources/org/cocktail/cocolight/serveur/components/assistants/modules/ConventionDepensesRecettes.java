package org.cocktail.cocolight.serveur.components.assistants.modules;

import org.cocktail.cocolight.serveur.components.GestionDepenses;
import org.cocktail.cocolight.serveur.components.GestionRecettes;
import org.cocktail.cocolight.serveur.components.controlers.AvenantVoletFinancierCtrl;
import org.cocktail.cocowork.server.metier.convention.SbDepense;
import org.cocktail.cocowork.server.metier.convention.Tranche;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

public class ConventionDepensesRecettes extends ModuleAssistant {
    
    public AvenantVoletFinancierCtrl ctrl = null;
    private String selectedTab;
    private Boolean isModeNature;
    private static EOQualifier QUAL_DEPREC_NATURE = SbDepense.PLANCO.isNotNull();
    private static EOQualifier QUAL_DEPREC_DESTINATION = SbDepense.LOLF.isNotNull();
    
    public ConventionDepensesRecettes(WOContext context) {
        super(context);
        initialiserOnglets();
    }
    
    @Override
    public void awake() {
        super.awake();
        if (isModeNature == null) {
            setModeNature(true);
        }
    }
    
    public WOActionResults rafraichirDepensesEtRecettes() {
        EOQualifier qual;
        if (isModeNature()) {
            qual = QUAL_DEPREC_NATURE;
        }  else {
            qual = QUAL_DEPREC_DESTINATION;
        }
        ctrl().dgDepenses().setQualifier(qual);
        ctrl().dgDepenses().updateDisplayedObjects();
        ctrl().dgRecettes().setQualifier(qual);
        ctrl().dgRecettes().updateDisplayedObjects();
        return null;
    }
    
    public WOActionResults openAjouterUneDepense() {
        GestionDepenses nextPage = ctrl().ajouterUneDepense();
        nextPage.setModeNature(isModeNature());
        return nextPage;
    }
    
    public WOActionResults openModifierUneDepense() {
        GestionDepenses nextPage = ctrl().modifierUneDepense();
        nextPage.setModeNature(isModeNature());
        return nextPage;
    }
    
    public WOActionResults openAjouterUneRecette() {
        GestionRecettes nextPage = ctrl().ajouterUneRecette();
        nextPage.setModeNature(isModeNature());
        return nextPage;
    }
    
    public WOActionResults openModifierUneRecette() {
        GestionRecettes nextPage = ctrl().modifierUneRecette();
        nextPage.setModeNature(isModeNature());
        return nextPage;
    }
    
    public AvenantVoletFinancierCtrl ctrl() {
        if (ctrl == null) {
            ctrl = new AvenantVoletFinancierCtrl(this);
            Class[] notificationArray = new Class[] { NSNotification.class };
            NSNotificationCenter.defaultCenter().addObserver(ctrl, new NSSelector("refreshTranches",notificationArray), "refreshTranchesNotification", null);
            NSNotificationCenter.defaultCenter().addObserver(ctrl, new NSSelector("refreshDepenses",notificationArray), "refreshDepensesNotification", null);
            NSNotificationCenter.defaultCenter().addObserver(ctrl, new NSSelector("refreshRecettes",notificationArray), "refreshRecettesNotification", null);
        }
        return ctrl;
    }

    private void initialiserOnglets() {
        if (selectedTab == null) {
            selectedTab = "Dépenses";
        }
    }
    
    public String titleForNewRecette() {
        return "Ajouter une nouvelle recette sur la tranche " + ((Tranche)ctrl().dgTranchesAnnuelles().selectedObject()).exerciceCocktail().exeExercice();
    }
    
    public String titleForNewDepense() {
        return "Ajouter une nouvelle dépense sur la tranche " + ((Tranche)ctrl().dgTranchesAnnuelles().selectedObject()).exerciceCocktail().exeExercice();
    }
    
    public boolean isTrancheBudgetSelected() {
        Tranche selectedTranche = (Tranche)ctrl.dgTranchesAnnuelles().selectedObject();
        return selectedTranche != null && selectedTranche.traNatureMontant() != null;
    }
    
    public void setIsTabRecettesSelected(boolean isSelected) {
        if (isSelected) selectedTab = "Recettes";
    }
    
    public boolean isTabRecettesSelected() {
        return selectedTab == "Recettes";
    }
    
    public boolean isTabRecettesVisibled() {
        return ctrl.dgTranchesAnnuelles() != null && 
            ctrl.dgTranchesAnnuelles().selectedObject() != null &&
            ((Tranche)ctrl.dgTranchesAnnuelles().selectedObject()).traNatureMontant().indexOf("RECETTE")>-1;
    }
    
    public boolean isModifierUneRecetteDisabled() {
        return ctrl.dgRecettes().selectedObject() == null || ctrl.nePeutPasEditerDepensesOuRecettes();
    }
    
    public boolean isSupprimerUneRecetteDisabled() {
        return ctrl.dgRecettes().selectedObject() == null || ctrl.nePeutPasEditerDepensesOuRecettes();
    }

    public void setIsTabDepensesSelected(boolean isSelected) {
        if (isSelected) selectedTab = "Dépenses";
    }
    
    public boolean isTabDepensesSelected() {
        return selectedTab == "Dépenses";
    }
    
    public boolean isTabDepensesVisibled() {
        return ctrl.dgTranchesAnnuelles() != null && 
            ctrl.dgTranchesAnnuelles().selectedObject() != null &&
            ((Tranche)ctrl.dgTranchesAnnuelles().selectedObject()).traNatureMontant().indexOf("DEPENSE")>-1;
    }
    
    public boolean isModifierUneDepenseDisabled() {
        return ctrl.dgDepenses().selectedObject() == null || ctrl.nePeutPasEditerDepensesOuRecettes();
    }
    
    public boolean isSupprimerUneDepenseDisabled() {
        return ctrl.dgDepenses().selectedObject() == null || ctrl.nePeutPasEditerDepensesOuRecettes();
    }
    
    public boolean isModeNature() {
        return isModeNature;
    }
    
    public void setModeNature(boolean isModeNature) {
        this.isModeNature = isModeNature;
        rafraichirDepensesEtRecettes();
    }
    
    public boolean isModeDestination() {
        return !isModeNature();
    }
    
    public void setModeDestination(boolean isModeDestination) {
        setModeNature(!isModeDestination);
    }
    
}