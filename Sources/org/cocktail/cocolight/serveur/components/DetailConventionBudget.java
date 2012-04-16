package org.cocktail.cocolight.serveur.components;

import java.math.BigDecimal;

import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetParametre;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPrevisionBudget;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPrevisionBudgetNatureLolf;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPropositionBudget;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOTypeAction;
import org.cocktail.fwkcktlcompta.common.metier.EOPlanComptableExer;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeCredit;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.ajax.AjaxUpdateContainer;
import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXArrayUtilities;

public class DetailConventionBudget extends MyWOComponent {
    
    private static final long serialVersionUID = -8660612314353830294L;
    public static final String BINDING_TRANCHE = "tranche";
    public static final String BINDING_PREVISION = "prevision";
    public static final String BINDING_MODE_DEPENSE = "modeDepense";
    
//    private Contrat contrat;
    private NSArray<EOTypeCredit> typesCredit;
    private EOTypeCredit currentTypeCredit;
    private EOPrevisionBudget currentPrevision;
    private EOPropositionBudget currentProposition;
    private EOPlanComptableExer currentNature;
    private NSArray<EOTypeAction> lolfs;
    private EOTypeAction currentLolf;
    private String formattedZero;
    private Integer niveauBibasse;
    private EOPrevisionBudget selectedPrevision;
    private boolean modeDepense;
    private String containerId;
    
    public DetailConventionBudget(WOContext context) {
        super(context);
        formattedZero = application().app2DecimalesFormatter().format(BigDecimal.ZERO);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        // Si selectedPrevision a change ...
        setSelectedPrevision();
        setModeDepense();
        super.appendToResponse(response, context);
    }
    
    public String containerId() {
        if (containerId == null)
            containerId = "BudgetDep_" + ERXWOContext.safeIdentifierName(context(), true);
        return containerId;
    }
    
    public EOPrevisionBudget selectedPrevision() {
        return selectedPrevision;
    }
    
    public boolean modeDepenseHasDepensesLeft() {
        return modeDepense() && !tranche().depensesSansPrevision().isEmpty();
    }
    
    public boolean modeRecetteHasRecettesLeft() {
        return !modeDepense() && !tranche().recettesSansPrevision().isEmpty();
    }
    
    @SuppressWarnings("unchecked")
    public WOActionResults recalculerDimensions() {
        // recuperations des types de credit correspondant 
        typesCredit = ERXArrayUtilities.arrayWithoutDuplicates(
                (NSArray<EOTypeCredit>) selectedPrevision().previsionBudgetNatureLolfs(modeDepense()).valueForKey(
                        EOPrevisionBudgetNatureLolf.TYPE_CREDIT_KEY));
        typesCredit = ERXS.sorted(typesCredit, ERXS.asc(EOTypeCredit.TCD_CODE_KEY));
        // recuperation des lolfs
        lolfs = ERXArrayUtilities.arrayWithoutDuplicates(
                (NSArray<EOTypeAction>) selectedPrevision().previsionBudgetNatureLolfs(modeDepense()).valueForKey(
                        EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY)); 
        lolfs = ERXS.sorted(lolfs, ERXS.asc(EOTypeAction.TYAC_CODE_KEY));
        // On ferme les fentres ouvertes
        return null;
    }
    
    public WOActionResults closeAndRefresh() {
        CktlAjaxWindow.close(context());
        AjaxUpdateContainer.updateContainerWithID(containerId(), context());
        recalculerDimensions();
        return null;
    }
    
    public String formattedZero() {
        return formattedZero;
    }
    
    public BigDecimal prevGestionForCurrentLolfAndTypeCredit() {
        return selectedPrevision().sumPrevsFieldWith2Dimensions(
                EOPrevisionBudgetNatureLolf.PVBNL_MONTANT_KEY,
                EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf, 
                EOPrevisionBudgetNatureLolf.TYPE_CREDIT_KEY, currentTypeCredit, modeDepense());
    }
    
    public BigDecimal prevGestionAOuvrirForCurrentLolfAndTypeCredit() {
        return selectedPrevision().sumPrevsFieldWith2Dimensions(
                EOPrevisionBudgetNatureLolf.PVBNL_A_OUVRIR_KEY,
                EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf, 
                EOPrevisionBudgetNatureLolf.TYPE_CREDIT_KEY, currentTypeCredit, modeDepense());
    }
    
    public EOPrevisionBudgetNatureLolf prevBudgetNatureLolfForCurrentLolfAndNatureAndTypeCredit() {
        return selectedPrevision().getOrCreatePrevisionBudgetNatureLolf(currentNature, currentLolf, currentTypeCredit);
    }
    
    public BigDecimal totalForCurrentTypeCredit() {
        return selectedPrevision().sumPrevsFieldWithDimension(
                EOPrevisionBudgetNatureLolf.PVBNL_MONTANT_KEY, 
                EOPrevisionBudgetNatureLolf.TYPE_CREDIT_KEY, currentTypeCredit, modeDepense());
    }
    
    public BigDecimal totalAOuvrirForCurrentTypeCredit() {
        return selectedPrevision().sumPrevsFieldWithDimension(
                EOPrevisionBudgetNatureLolf.PVBNL_A_OUVRIR_KEY, 
                EOPrevisionBudgetNatureLolf.TYPE_CREDIT_KEY, currentTypeCredit, modeDepense());
    }
    
    public BigDecimal totalForCurrentLolf() {
        return selectedPrevision().sumPrevsFieldWithDimension(
                EOPrevisionBudgetNatureLolf.PVBNL_MONTANT_KEY, 
                EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf, modeDepense());
    }
    
    public BigDecimal totalAOuvrirForCurrentLolf() {
        return selectedPrevision().sumPrevsFieldWithDimension(
                EOPrevisionBudgetNatureLolf.PVBNL_A_OUVRIR_KEY, 
                EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf, modeDepense());
    }
    
    public BigDecimal totalForCurrentLolfAndNature() {
        return selectedPrevision().sumPrevsFieldWith2Dimensions(
                EOPrevisionBudgetNatureLolf.PVBNL_MONTANT_KEY,
                EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf, 
                EOPrevisionBudgetNatureLolf.PLAN_COMPTABLE_EXER_KEY, currentNature, modeDepense());
    }
    
    public BigDecimal totalAOuvrirForCurrentLolfAndNature() {
        return selectedPrevision().sumPrevsFieldWith2Dimensions(
                EOPrevisionBudgetNatureLolf.PVBNL_A_OUVRIR_KEY,
                EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf, 
                EOPrevisionBudgetNatureLolf.PLAN_COMPTABLE_EXER_KEY, currentNature, modeDepense());
    }
    
    public BigDecimal allTotal() {
        return selectedPrevision().sumAllPrevsField(EOPrevisionBudgetNatureLolf.PVBNL_MONTANT_KEY, modeDepense());
    }
    
    public BigDecimal allTotalAOuvrir() {
        return selectedPrevision().sumAllPrevsField(EOPrevisionBudgetNatureLolf.PVBNL_A_OUVRIR_KEY, modeDepense());
    }
    
    public String cssTotalAOuvrirForCurrentTypeCredit() {
        int comp = totalAOuvrirForCurrentTypeCredit().compareTo(totalForCurrentTypeCredit());
        if (comp == 1)
            return "total_dep";
        else if (comp == 0)
            return "total_eq";
        else
            return "";
    }
    
    public String cssAllTotalAOuvrir() {
        int comp = allTotalAOuvrir().compareTo(allTotal());
        if (comp == 1)
            return "total_dep";
        else if  (comp == 0)
            return "total_eq";
        else
            return "";
    }
    
    public boolean propositionAValiderOuHasNoDroitPropositionBudgetaire() {
        return selectedPrevision().lastPropositionBudgetAValider() != null || !applicationUser().hasDroitPropositionBudget();
    }
    
    public String colspan() {
        return selectedPrevision().isValide() ? "2" : "1";
    }
    
    public String ouvertOuAOuvrir() {
        if (selectedPrevision().lastPropositionBudgetValide() != null)
            return "Ouvert";
        else
            return "A Ouvrir";
    }
    
    public NSArray<EOPlanComptableExer> naturesForCurrentLolf() {
        EOQualifier qualifier = ERXQ.equals(EOPrevisionBudgetNatureLolf.TYPE_ACTION_KEY, currentLolf);
        NSArray<EOPrevisionBudgetNatureLolf> props = selectedPrevision().previsionBudgetNatureLolfs(qualifier);
        NSArray<EOPlanComptableExer> natures = 
            ERXArrayUtilities.arrayWithoutDuplicates(
                    (NSArray<EOPlanComptableExer>)props.valueForKey(EOPrevisionBudgetNatureLolf.PLAN_COMPTABLE_EXER_KEY));
        natures = ERXS.sorted(natures, ERXS.asc(EOPlanComptableExer.PCO_NUM_KEY));
        return natures;
    }
    
    public Tranche tranche() {
        return (Tranche)valueForBinding(BINDING_TRANCHE);
    }
    
    public String messageAjoutPrev() {
        String libelOrgan = (String)EOOrgan.NIV_LIB_MAP.get(niveauBibasse());
        return "Il reste des dépenses sans ligne budgétaire ou avec une ligne budgétaire de niveau " + libelOrgan; 
    }
    
    public Integer niveauBibasse() {
        if (niveauBibasse == null)
            niveauBibasse = FinderBudgetParametre.getParametreOrganNiveauSaisie(
                    tranche().editingContext(), tranche().exerciceCocktail().exercice());
        return niveauBibasse;
    }
    
    public NSArray<EOTypeCredit> getTypesCredit() {
        return typesCredit;
    }
    
    public EOPrevisionBudget getCurrentPrevision() {
        return currentPrevision;
    }
    
    public void setCurrentPrevision(EOPrevisionBudget currentPrevision) {
        this.currentPrevision = currentPrevision;
    }
    
    public EOPrevisionBudget getSelectedPrevision() {
        return selectedPrevision;
    }
    
    public void setSelectedPrevision() {
        EOPrevisionBudget prev = (EOPrevisionBudget)valueForBinding(BINDING_PREVISION);
        if (prev != null && !prev.equals(selectedPrevision)) {
            selectedPrevision = prev;
            recalculerDimensions();
        }
    }
    
    public boolean modeDepense() {
        return modeDepense;
    }
    
    public void setModeDepense() {
        Boolean modeDep = booleanValueForBinding(BINDING_MODE_DEPENSE, null);
        if (modeDep != null && !modeDep.equals(modeDepense)) {
            modeDepense = modeDep;
            recalculerDimensions();
        }
    }
    
    public EOTypeCredit getCurrentTypeCredit() {
        return currentTypeCredit;
    }
    
    public void setCurrentTypeCredit(EOTypeCredit currentTypeCredit) {
        this.currentTypeCredit = currentTypeCredit;
    }
    
    public EOPropositionBudget getCurrentProposition() {
        return currentProposition;
    }
    
    public void setCurrentProposition(EOPropositionBudget currentProposition) {
        this.currentProposition = currentProposition;
    }
    
    public NSArray<EOTypeAction> getLolfs() {
        return lolfs;
    }
    
    public EOTypeAction getCurrentLolf() {
        return currentLolf;
    }
    
    public void setCurrentLolf(EOTypeAction currentLolf) {
        this.currentLolf = currentLolf;
    }
    
    public EOPlanComptableExer getCurrentNature() {
        return currentNature;
    }
    
    public void setCurrentNature(EOPlanComptableExer currentNature) {
        this.currentNature = currentNature;
    }
}