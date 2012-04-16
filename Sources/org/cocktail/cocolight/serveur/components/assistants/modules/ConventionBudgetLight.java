package org.cocktail.cocolight.serveur.components.assistants.modules;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.components.commons.Tabs;
import org.cocktail.cocolight.serveur.components.commons.Tabs.Tab;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.HistoCreditPositionne;
import org.cocktail.cocowork.server.metier.convention.HistoCreditPositionneRec;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.cocowork.server.metier.convention.TrancheBudget;
import org.cocktail.cocowork.server.metier.convention.TrancheBudgetRec;
import org.cocktail.cocowork.server.metier.convention.VCreditsPositionnes;
import org.cocktail.cocowork.server.metier.convention.VCreditsPositionnesRec;
import org.cocktail.cocowork.server.metier.convention.VDepensesTranche;
import org.cocktail.cocowork.server.metier.convention.VRecettesTranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetMasqueCredit;
import org.cocktail.fwkcktlcompta.common.metier.EOPlanComptableExer;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderOrgan;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderUtilisateur;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExerciceCocktail;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeCredit;
import org.cocktail.fwkcktljefyadmin.common.metier.EOUtilisateur;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.ajax.AjaxUpdateContainer;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.foundation.ERXSelectorUtilities;

public class ConventionBudgetLight extends ModuleAssistant {
    
    public final static Logger LOG = Logger.getLogger(ConventionBudget.class); 
    public static final String WillOpenAjoutPrevision = "WillOpenAjoutPrevision";
    
    private NSDictionary dicoTranchesAnnuelles;
    private ERXDisplayGroup<Tranche> dgTranchesAnnuelles;
    private Tranche currentTrancheAnnuelle;
//    private String tabSelected = "Depenses";
    private NSDictionary dicoDepenses;
    private NSDictionary dicoRecettes;
    private ERXDisplayGroup<VDepensesTranche> dgDepenses;
    private ERXDisplayGroup<VRecettesTranche> dgRecettes;
    private VDepensesTranche currentDepense;
    private VRecettesTranche currentRecette;
    private NSDictionary dicoCredits;
    private NSDictionary dicoCreditsRec;
    private ERXDisplayGroup<VCreditsPositionnes> dgCreditsDepenses;
    private ERXDisplayGroup<VCreditsPositionnesRec> dgCreditsRecettes;
    private VCreditsPositionnes currentCreditPositionne;
    private VCreditsPositionnesRec currentCreditPositionneRec;
    private EOOrgan currentOrgan;
    private NSArray<EOOrgan> organs;
    private NSArray<EOOrgan> organsSupplementaires;
    protected NSArray<EOTypeCredit> typesCreditDepense;
    protected NSArray<EOTypeCredit> typesCreditRecette;
    private EOTypeCredit currentTypeCredit;
    private TrancheBudget newTrancheBudget;
    private TrancheBudgetRec newTrancheBudgetRec;
    
    private NSDictionary dicoCreditsPositionnesRecap;
    private NSDictionary dicoCreditsPositionnesRecRecap;
    private ERXDisplayGroup<TrancheBudget> dgCreditsDepensesRecap;
    private ERXDisplayGroup<TrancheBudgetRec> dgCreditsRecettesRecap;
    private TrancheBudget currentTrancheBudget;
    private TrancheBudgetRec currentTrancheBudgetRec;
    
    private EOUtilisateur userJefyAdmin;

    private Tab depensesTab;
    private Tab recettesTab;
    private Tab reportTab;
    
    public ConventionBudgetLight(WOContext context) {
        super(context);
        new Tabs(depensesTab = new Tab("Budget dépenses", "dep"),
                 recettesTab = new Tab("Budget recettes", "rec"),
                 reportTab = new Tab("Report de crédits", "rep"));
        NSNotificationCenter.defaultCenter().addObserver(
                this, 
                ERXSelectorUtilities.notificationSelector("rafraichirTranches"), 
                "refreshTranchesNotification", null);
        NSNotificationCenter.defaultCenter().addObserver(
                        this, 
                        ERXSelectorUtilities.notificationSelector("refreshDepensesEtRecettes"), 
                        "refreshDepensesNotification", null);
        NSNotificationCenter.defaultCenter().addObserver(
                        this, 
                        ERXSelectorUtilities.notificationSelector("refreshDepensesEtRecettes"), 
                        "refreshRecettesNotification", null);
    }
    
    @Override
    public void awake() {
        super.awake();
    }
    
    public WOActionResults ajouterCreditDepense() {
        try {
            // AT est-ce utile ?
//            if (selectedTranche().translateOrgansDepenses()) {
//                session().addSimpleInfoMessage("Cocolight", "Les dépenses ont été faites sur une ligne budgétaire d'un exercice antérieur." +
//                                "Après enregistrement de la modification, la depense sera faite sur la ligne budgétaire identique mais de l'exercice " + 
//                                selectedTranche().exerciceCocktail().exeExercice());
//            }
            newTrancheBudget = TrancheBudget.creerTrancheBudget(selectedTranche(), 
                                                                application().getDernierExerciceOuvertOuEnPreparation(), 
                                                                applicationUser().getUtilisateur().utlOrdre());
            // On positionne le reste à positionner sur le montant par défaut
            newTrancheBudget.setTbMontant(resteAPositionner());
            resetNomenclatures();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    public WOActionResults annulerAjoutCreditDep() {
        ecForBudget().revert();
        CktlAjaxWindow.close(context());
        return null;
    }
    
    
    public WOActionResults validerAjoutCreditDep() {
        // Bon le déroulement de cette sauvegarde est très particulier... 
        if (getNewTrancheBudget() != null) {
            // - On fait manuellement la validation :
            try {
                getNewTrancheBudget().validateForSave();
                HistoCreditPositionne.checkPreCreation(selectedDepenses(), getNewTrancheBudget().tbMontant());
            }
            catch (ValidationException e) {
                session().addSimpleErrorMessage("Cocolight", e.getMessage());
                return null;
            }
            // Merge
            getNewTrancheBudget().mergeAvecExistant();
            // Si ça passe on enregistre la journalisation et on merge 
            // On crée l'enregistrement d'historisation juste avant
            EOPlanComptableExer planco = null;
            if (selectedTranche().contrat().isModeBudgetAvance()) {
                planco = selectedDepenses() != null ? selectedDepenses().planco() : null;
            }
            HistoCreditPositionne.creerHistoCreditPositionne(
                            ecForBudget(), selectedTranche(), planco, 
                            getNewTrancheBudget().typeCredit(),
                            getNewTrancheBudget().organ(),
                            getNewTrancheBudget().tbMontant());
            // Maintenant on fait le saveChanges, mais s'il y a une erreur de validation, ou autre, c'est un bug,
            // on balance une erreur rude
            saveChangesAjoutBudget();
        }
        return null;
    }
    
    public WOActionResults validerTranche() {
        try {
            selectedTranche().valider(applicationUser().getUtilisateur());
            saveChanges("La tranche a été validée avec succès");
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    public void refreshDepensesEtRecettes(NSNotification notif) {
        refreshDepensesEtRecettes();
    }
    
    public WOActionResults refreshDepensesEtRecettes() {
        this.dgDepenses = null;
        this.dgRecettes = null;
        this.resetCachesAllDepenses();
        this.resetCachesAllRecettes();
        return null;
    }
    
    public WOActionResults refreshCreditsDepenses() {
        this.dgCreditsDepenses = null;
        return null;
    }
    
    protected void resetNomenclatures() {
        this.typesCreditDepense = null;
        this.typesCreditRecette = null;
    }
    
    protected void resetCachesAllDepenses() {
        this.dgCreditsDepenses = null;
        this.dgCreditsDepensesRecap = null;
    }

    public WOActionResults supprimerCreditDepense() {
        try {
            TrancheBudget.supprimerLigneCreditDepense(ecForBudget(), application().getDernierExerciceOuvertOuEnPreparation(), 
                                                      selectedCreditsDepensesPos());
            saveChanges("Suppression de ligne de crédit enregistrée avec succès");
            resetCachesAllDepenses();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    protected EOEditingContext ecForBudget() {
        return editingContext();
    }
    
    public EOUtilisateur userJefyAdmin() {
        if (userJefyAdmin == null)
            userJefyAdmin = FinderUtilisateur.getUtilisateur(ecForBudget(), applicationUser().getUtilisateur().utlOrdre());
        return userJefyAdmin;
    }
    
    private void saveChangesAjoutBudget() {
        try {
            ecForBudget().saveChanges();
            session().addSimpleInfoMessage("Cocolight", "Ajout d'une nouvelle ligne de crédit effectué avec succès");
            CktlAjaxWindow.close(context());
            AjaxUpdateContainer.updateContainerWithID("ContainerBudgetDetail", context());
            resetCachesAllDepenses();
            resetCachesAllRecettes();
        } catch (ValidationException e) {
        	session().addSimpleErrorMessage("Cocolight", e);
        } catch (Exception e) {
            LOG.error("Une erreur est survenue lors de l'enregistrement d'une ligne de crédit", e);
            ecForBudget().invalidateAllObjects();
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }
    
    protected boolean saveChanges(String successMessage) {
        boolean success = false;
        try {
            ecForBudget().saveChanges();
            session().addSimpleInfoMessage("Cocolight", successMessage);
            success = true;
        } catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        } catch (Exception e) {
            ecForBudget().invalidateAllObjects();
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        return success;
    }
    
    public void rafraichirTranches(NSNotification notif) {
        dgTranchesAnnuelles = null;
    }
    
    public boolean hasNoPriseEnChargeRA() {
        return selectedTranche().contrat().isModeRA() && selectedTranche().contrat().brouillardsValides().isEmpty();
    }
    
    public NSDictionary dicoTranchesAnnuelles() {
        if (dicoTranchesAnnuelles == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AvenantTranchesAnnuellesReadOnly.plist", null, NSArray.EmptyArray));
            dicoTranchesAnnuelles = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoTranchesAnnuelles;
    }
    
    public NSDictionary dicoTranchesAnnuellesLight() {
        if (dicoTranchesAnnuelles == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AvenantTranchesAnnuellesLight.plist", null, NSArray.EmptyArray));
            dicoTranchesAnnuelles = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoTranchesAnnuelles;
    }
    
    public Tranche getCurrentTrancheAnnuelle() {
        return currentTrancheAnnuelle;
    }
    
    public void setCurrentTrancheAnnuelle(Tranche currentTrancheAnnuelle) {
        this.currentTrancheAnnuelle = currentTrancheAnnuelle;
    }
    
    public String validerTrancheText() {
        return "Valider tranche " + selectedTranche().exerciceCocktail().exeExercice().toString();
    }
    
    public boolean peutValiderTranche() {
        boolean bonExo = selectedTranche().exerciceCocktail().exeExercice().intValue() <=
                            application().getDernierExerciceOuvertOuEnPreparation().exeExercice().intValue();
        return !super.disabled() && bonExo && applicationUser().hasDroitValidationTranche() && !selectedTranche().isValide();
    }
    
    public ERXDisplayGroup<Tranche> dgTranchesAnnuelles() {
        if (dgTranchesAnnuelles==null) {
                dgTranchesAnnuelles = new ERXDisplayGroup<Tranche>();
                dgTranchesAnnuelles.setSelectsFirstObjectAfterFetch(true);
            Avenant avenant = avenant();
            if (avenant != null) {
                Contrat contrat = avenant.contrat().localInstanceIn(ecForBudget());
                EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(Tranche.class),ecForBudget());
                ds.setArray(contrat.tranches());
                NSArray<EOSortOrdering> sortOrderingExercice = Tranche.EXERCICE_COCKTAIL.dot(EOExerciceCocktail.EXE_EXERCICE_KEY).ascs();
                dgTranchesAnnuelles.setSortOrderings(sortOrderingExercice);
                dgTranchesAnnuelles.setDataSource(ds);
                dgTranchesAnnuelles.fetch();
            }
        }
        return dgTranchesAnnuelles;
    }
 
    public ERXDisplayGroup<VDepensesTranche> dgDepenses() {
        if (dgDepenses==null) {
            dgDepenses = new ERXDisplayGroup<VDepensesTranche>();
            dgDepenses.setSelectsFirstObjectAfterFetch(true);
            Tranche tranche = dgTranchesAnnuelles().selectedObject();
            if (tranche != null) {
                dgDepenses.setObjectArray(tranche.sommeDepensesByNature());
            }
        }
        return dgDepenses;
    }
    
    public ERXDisplayGroup<VCreditsPositionnes> dgCreditsDepenses() {
        if (dgCreditsDepenses==null) {
            dgCreditsDepenses = new ERXDisplayGroup<VCreditsPositionnes>();
            dgCreditsDepenses.setSelectsFirstObjectAfterFetch(true);
            VDepensesTranche depenseTranche = dgDepenses().selectedObject();
            if (depenseTranche != null) {
                dgCreditsDepenses.setObjectArray(depenseTranche.creditsPositionnes());
            }
        }
        return dgCreditsDepenses;
    }
    
    public ERXDisplayGroup<TrancheBudget> dgCreditsDepensesRecap() {
        if (dgCreditsDepensesRecap==null) {
            dgCreditsDepensesRecap = new ERXDisplayGroup<TrancheBudget>();
            dgCreditsDepensesRecap.setSelectsFirstObjectAfterFetch(true);
            if (selectedTranche() != null) {
                dgCreditsDepensesRecap.setObjectArray(selectedTranche().trancheBudgetsNonSupprimes());
            }
        }
        return dgCreditsDepensesRecap;
    }
    
    /**
     * @return the dicoDepenses
     */
    public NSDictionary dicoDepenses() {
        if (dicoDepenses == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionTrancheSbDepensesLight.plist", null, NSArray.EmptyArray));
            dicoDepenses = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoDepenses;
    }
    
    /**
     * @return the dicoDepenses
     */
    public NSDictionary dicoCreditsPositionnes() {
        if (dicoCredits == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionCreditsPositionnesLight.plist", null, NSArray.EmptyArray));
            dicoCredits = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoCredits;
    }
    
    /**
     * @return the dicoDepenses
     */
    public NSDictionary dicoCreditsPositionnesRecap() {
        if (dicoCreditsPositionnesRecap == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionCreditsPositionnesRecapLight.plist", null, NSArray.EmptyArray));
            dicoCreditsPositionnesRecap = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoCreditsPositionnesRecap;
    }
    
    public Tranche selectedTranche() {
        return dgTranchesAnnuelles().selectedObject();
    }
    
    public TrancheBudget selectedTrancheBudget() {
        return dgCreditsDepensesRecap().selectedObject();
    }
    
    public VDepensesTranche selectedDepenses() {
        return dgDepenses().selectedObject();
    }
    
    public VCreditsPositionnes selectedCreditsDepensesPos() {
        return dgCreditsDepenses().selectedObject();
    }
    
    public EOExercice getExercice() {
        return selectedTranche().exerciceCocktail().exercice();
    }
    
    public boolean isExerceOuvertOuPreparation() {
        return getExercice() != null && (getExercice().estOuvert() || getExercice().estPreparation());
    }
   
    public boolean hasRecettesOuDepenses() {
        return !selectedTranche().sbRecettes().isEmpty() || !selectedTranche().sbDepenses().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public NSArray<EOOrgan> organs() {
        if (organs == null) {
            EOStructure centreResp = selectedTranche().contrat().centreResponsabilite();
            Integer utl = applicationUser().getUtilisateur().utlOrdre();
            NSMutableArray<EOOrgan> organsTmp = FinderOrgan.fetchOrgansForStructureAndUtl(
                            ecForBudget(), centreResp.cStructure(), getExercice(), utl, selectedTranche().contrat().niveauOrganMin(),
                            selectedTranche().contrat().isModeRA()).mutableClone();
            EOOrgan organComposante = selectedTranche().contrat().organComposante();
            if (organComposante != null && !organsTmp.containsObject(organComposante)) {
                organsTmp.addObject(organComposante);
            }
            for(EOOrgan organ : organsSupplementaires()) {
            	if(!organsTmp.containsObject(organ)) {
            		organsTmp.add(organ);
            	}
            }
            organs = organsTmp.immutableClone();
        }
        return organs;
    }
    
    public NSArray<EOOrgan> organsSupplementaires() { 
    	if(organsSupplementaires == null) {
    		organsSupplementaires = Contrat.autresOrgansBudget(ecForBudget());
    	}
    	return organsSupplementaires;
    }
    
    public String groupeOrgan() {
    	if(!organsSupplementaires().contains(getCurrentOrgan())) {
    		return "ORGANSUP";
    	} else {
    		return "ORGANSERV";
    	}
    }
    
    public String groupeOrganLabel() {
    	if(!organsSupplementaires().contains(getCurrentOrgan())) {
    		return "Supplémentaires";
    	} else {
    		return "Service gestionnaire";
    	}
    }
    
    public Tab getDepensesTab() {
        return depensesTab;
    }
    
    public Tab getRecettesTab() {
        return recettesTab;
    }

    public Tab getReportTab() {
        return reportTab;
    }
    
    public VDepensesTranche getCurrentDepense() {
        return currentDepense;
    }
    
    public void setCurrentDepense(VDepensesTranche currentDepense) {
        this.currentDepense = currentDepense;
    }
    
    public VCreditsPositionnes getCurrentCreditPositionne() {
        return currentCreditPositionne;
    }
    
    public void setCurrentCreditPositionne(VCreditsPositionnes currentCreditPositionne) {
        this.currentCreditPositionne = currentCreditPositionne;
    }
    
    public EOOrgan getCurrentOrgan() {
        return currentOrgan;
    }
    
    public void setCurrentOrgan(EOOrgan currentOrgan) {
        this.currentOrgan = currentOrgan;
    }
    
    public TrancheBudget getNewTrancheBudget() {
        return newTrancheBudget;
    }

    public void setNewTrancheBudget(TrancheBudget newTrancheBudget) {
        this.newTrancheBudget = newTrancheBudget;
    }
    
    public String organsForDepenses() {
        NSArray<String> organs = (NSArray<String>) selectedDepenses().organsForDepenses().valueForKey(EOOrgan.LONG_STRING_KEY);
        return organs.componentsJoinedByString("<br/>");
    }
    
    public BigDecimal resteAPositionner() {
        return selectedDepenses().montant().subtract(selectedDepenses().montantCreditsPositionnes());
    }
    
    public BigDecimal newTrancheBudgetPct() {
        BigDecimal result = BigDecimal.ZERO;
        if (getNewTrancheBudget() != null) {
            BigDecimal montantDepensesTranche = selectedDepenses().montant();
            if (montantDepensesTranche.compareTo(BigDecimal.ZERO) != 0) {
                result = getNewTrancheBudget().tbMontant().divide(montantDepensesTranche, 5, RoundingMode.HALF_UP);
                result = result.multiply(BigDecimal.valueOf(100));
            }
        }
        return result;
    }
    
    public void setNewTrancheBudgetPct(BigDecimal pct) {
        BigDecimal montantAPositionne = selectedDepenses().montant().multiply(pct).divide(BigDecimal.valueOf(100));
        getNewTrancheBudget().setTbMontant(montantAPositionne);
    }
    
    public NSArray<EOTypeCredit> getTypesCreditDepense() {
        if (typesCreditDepense == null) {
            String pcoNum = null;
            if (selectedDepenses() != null && selectedDepenses().planco() != null)
                pcoNum = selectedDepenses().planco().pcoNum();
            typesCreditDepense = FinderBudgetMasqueCredit.findTypesCreditExecFromNature(ecForBudget(), getExercice(), pcoNum, EOTypeCredit.TCD_TYPE_DEPENSE);
        }
        return typesCreditDepense;
    }
    
    public NSArray<EOTypeCredit> getTypesCreditRecette() {
        if (typesCreditRecette == null) {
            String pcoNum = null;
            if (selectedRecettes() != null && selectedRecettes().planco() != null)
                pcoNum = selectedRecettes().planco().pcoNum();
            typesCreditRecette = FinderBudgetMasqueCredit.findTypesCreditExecFromNature(ecForBudget(), getExercice(), pcoNum, EOTypeCredit.TCD_TYPE_RECETTE);
        }
        return typesCreditRecette;
    }
    
    public EOTypeCredit getCurrentTypeCredit() {
        return currentTypeCredit;
    }
    
    public void setCurrentTypeCredit(EOTypeCredit currentTypeCredit) {
        this.currentTypeCredit = currentTypeCredit;
    }
    
    public TrancheBudget getCurrentTrancheBudget() {
        return currentTrancheBudget;
    }
    
    public void setCurrentTrancheBudget(TrancheBudget currentTrancheBudget) {
        this.currentTrancheBudget = currentTrancheBudget;
    }
    
    public boolean isTabDepensesVisible() {
        return selectedTranche() != null && (selectedTranche().isNatureDepense() || selectedTranche().isNatureDepenseRecette());
    }
    
    public boolean isTabRecettesVisible() {
        return selectedTranche() != null && (selectedTranche().isNatureRecette() || selectedTranche().isNatureDepenseRecette());
    }
    
    public boolean isTabReportVisible() {
        return selectedTranche() != null && selectedTranche().contrat().tranches().count() > 1;
    }
    
    /** Implementation budget positionné en recette */
    public ERXDisplayGroup<VRecettesTranche> dgRecettes() {
        if (dgRecettes==null) {
            dgRecettes = new ERXDisplayGroup<VRecettesTranche>();
            dgRecettes.setSelectsFirstObjectAfterFetch(true);
            Tranche tranche = dgTranchesAnnuelles().selectedObject();
            if (tranche != null) {
                dgRecettes.setObjectArray(tranche.sommeRecettesByNature());
            }
        }
        return dgRecettes;
    }
    
    public ERXDisplayGroup<VCreditsPositionnesRec> dgCreditsRecettes() {
        if (dgCreditsRecettes==null) {
            dgCreditsRecettes = new ERXDisplayGroup<VCreditsPositionnesRec>();
            dgCreditsRecettes.setSelectsFirstObjectAfterFetch(true);
            VRecettesTranche recetteTranche = dgRecettes().selectedObject();
            if (recetteTranche != null) {
                dgCreditsRecettes.setObjectArray(recetteTranche.creditsPositionnesRec());
            }
        }
        return dgCreditsRecettes;
    }
    
    /**
     * @return the dicoDepenses
     */
    public NSDictionary dicoRecettes() {
        if (dicoRecettes == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionTrancheSbRecettesLight.plist", null, NSArray.EmptyArray));
            dicoRecettes = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoRecettes;
    }
    
    /**
     * @return the dicoDepenses
     */
    public NSDictionary dicoCreditsPositionnesRec() {
        if (dicoCreditsRec == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionCreditsPositionnesRecLight.plist", null, NSArray.EmptyArray));
            dicoCreditsRec = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoCreditsRec;
    }
    
    /**
     * @return the dicoDepenses
     */
    public NSDictionary dicoCreditsPositionnesRecRecap() {
        if (dicoCreditsPositionnesRecRecap == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionCreditsPositionnesRecRecapLight.plist", null, NSArray.EmptyArray));
            dicoCreditsPositionnesRecRecap = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
        }
        return dicoCreditsPositionnesRecRecap;
    }
    
    public VCreditsPositionnesRec selectedCreditsRecettesPos() {
        return dgCreditsRecettes().selectedObject();
    }
    
    public WOActionResults changerTranche() {
        resetCachesAllDepenses();
        resetCachesAllRecettes();
        return null;
    }
    
    public WOActionResults refreshRecettes() {
        this.dgRecettes = null;
        return null;
    }
    
    public WOActionResults refreshCreditsRecettes() {
        this.dgCreditsRecettes = null;
        return null;
    }

    public WOActionResults ajouterCreditRecette() {
        try {
            // Est-ce utile ?
//            if (selectedTranche().translateOrgansRecettes()) {
//                session().addSimpleInfoMessage("Cocolight", "Les recettes ont été faites sur une ligne budgétaire d'un exercice antérieur." +
//                                "Après enregistrement de la modification, les recettes seront faites sur la ligne budgétaire identique mais de l'exercice " + 
//                                selectedTranche().exerciceCocktail().exeExercice());
//            }
            newTrancheBudgetRec = TrancheBudgetRec.creerTrancheBudget(selectedTranche(), 
                                                                application().getDernierExerciceOuvertOuEnPreparation(), 
                                                                applicationUser().getUtilisateur().utlOrdre());
            // On positionne le reste à positionner sur le montant par défaut
            newTrancheBudgetRec.setTbrMontant(resteAPositionnerRec());
            resetNomenclatures();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    public WOActionResults annulerAjoutCreditRec() {
        ecForBudget().revert();
        CktlAjaxWindow.close(context());
        return null;
    }
    
    public WOActionResults validerAjoutCreditRec() {
        // Bon le déroulement de cette sauvegarde est très particulier... 
        if (getNewTrancheBudgetRec() != null) {
            // - On fait manuellement la validation :
            try {
                getNewTrancheBudgetRec().validateForSave();
                HistoCreditPositionneRec.checkPreCreation(selectedRecettes(), getNewTrancheBudgetRec().tbrMontant());
            }
            catch (ValidationException e) {
                session().addSimpleErrorMessage("Cocolight", e.getMessage());
                return null;
            }
            // Merge
            getNewTrancheBudgetRec().mergeAvecExistant();
            // Si ça passe on enregistre la journalisation et on merge 
            // On crée l'enregistrement d'historisation juste avant
            EOPlanComptableExer planco = null;
            if (selectedTranche().contrat().isModeBudgetAvance()) {
                planco = selectedRecettes() != null ? selectedRecettes().planco() : null;
            }
            HistoCreditPositionneRec.creerHistoCreditPositionne(
                            ecForBudget(), selectedTranche(), planco, 
                            getNewTrancheBudgetRec().typeCredit(),
                            getNewTrancheBudgetRec().organ(),
                            getNewTrancheBudgetRec().tbrMontant());
            // Maintenant on fait le saveChanges, mais s'il y a une erreur de validation, ou autre, c'est un bug,
            // on balance une erreur rude
            saveChangesAjoutBudget();
        }
        return null;
    }
    
    protected void resetCachesAllRecettes() {
        this.dgCreditsRecettes = null;
        this.dgCreditsRecettesRecap = null;
    }
    
    public WOActionResults supprimerCreditRecette() {
        try {
            TrancheBudgetRec.supprimerLigneCreditRecette(ecForBudget(), application().getDernierExerciceOuvertOuEnPreparation(), 
                                                      selectedCreditsRecettesPos());
            saveChanges("Suppression de ligne de crédit enregistrée avec succès");
            resetCachesAllRecettes();
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    public BigDecimal resteAPositionnerRec() {
        return selectedRecettes().montant().subtract(selectedRecettes().montantCreditsPositionnes());
    }
    
    public TrancheBudgetRec selectedTrancheBudgetRec() {
        return dgCreditsRecettesRecap().selectedObject();
    }
    
    public VRecettesTranche selectedRecettes() {
        return dgRecettes().selectedObject();
    }
    
    public String organsForRecettes() {
        NSArray<String> organs = (NSArray<String>) selectedRecettes().organsForRecettes().valueForKey(EOOrgan.LONG_STRING_KEY);
        return organs.componentsJoinedByString("<br/>");
    }
    
    public BigDecimal newTrancheBudgetRecPct() {
        BigDecimal result = BigDecimal.ZERO;
        if (getNewTrancheBudgetRec() != null) {
            BigDecimal montantRecettesTranche = selectedRecettes().montant();
            if (montantRecettesTranche.compareTo(BigDecimal.ZERO) != 0) {
                result = getNewTrancheBudgetRec().tbrMontant().divide(montantRecettesTranche, 5, RoundingMode.HALF_UP);
                result = result.multiply(BigDecimal.valueOf(100));
            }
        }
        return result;
    }
    
    public void setNewTrancheBudgetRecPct(BigDecimal pct) {
        BigDecimal montantAPositionne = selectedRecettes().montant().multiply(pct).divide(BigDecimal.valueOf(100));
        getNewTrancheBudgetRec().setTbrMontant(montantAPositionne);
    }
    
    public ERXDisplayGroup<TrancheBudgetRec> dgCreditsRecettesRecap() {
        if (dgCreditsRecettesRecap==null) {
            dgCreditsRecettesRecap = new ERXDisplayGroup<TrancheBudgetRec>();
            dgCreditsRecettesRecap.setSelectsFirstObjectAfterFetch(true);
            if (selectedTranche() != null) {
                dgCreditsRecettesRecap.setObjectArray(selectedTranche().trancheBudgetsRecNonSupprimes());
            }
        }
        return dgCreditsRecettesRecap;
    }
    
    
    public VRecettesTranche getCurrentRecette() {
        return currentRecette;
    }
    
    public void setCurrentRecette(VRecettesTranche currentRecette) {
        this.currentRecette = currentRecette;
    }
    
    public VCreditsPositionnesRec getCurrentCreditPositionneRec() {
        return currentCreditPositionneRec;
    }
    
    public void setCurrentCreditPositionneRec(VCreditsPositionnesRec currentCreditPositionneRec) {
        this.currentCreditPositionneRec = currentCreditPositionneRec;
    }
    
    public TrancheBudgetRec getNewTrancheBudgetRec() {
        return newTrancheBudgetRec;
    }
    
    public void setNewTrancheBudgetRec(TrancheBudgetRec newTrancheBudgetRec) {
        this.newTrancheBudgetRec = newTrancheBudgetRec;
    }
    
    public TrancheBudgetRec getCurrentTrancheBudgetRec() {
        return currentTrancheBudgetRec;
    }
    
    public void setCurrentTrancheBudgetRec(TrancheBudgetRec currentTrancheBudgetRec) {
        this.currentTrancheBudgetRec = currentTrancheBudgetRec;
    }
    
}