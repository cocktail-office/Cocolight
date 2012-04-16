package org.cocktail.cocolight.serveur.components.assistants.modules;

import org.apache.log4j.Logger;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumn;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumnAssociation;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPrevisionBudget;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPropositionBudget;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderUtilisateur;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeEtat;
import org.cocktail.fwkcktljefyadmin.common.metier.EOUtilisateur;
import org.cocktail.fwkcktlpersonneguiajax.serveur.controleurs.MyCRIMailBus;
import org.cocktail.fwkcktlwebapp.server.CktlConfig;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
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
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestampFormatter;

import er.ajax.AjaxUpdateContainer;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXStringUtilities;

public class ConventionBudget extends ModuleAssistant {
    
    public final static Logger LOG = Logger.getLogger(ConventionBudget.class); 
    public static final String WillOpenAjoutPrevision = "WillOpenAjoutPrevision";
    
    private NSDictionary dicoTranchesAnnuelles;
    private ERXDisplayGroup<Tranche> dgTranchesAnnuelles;
    private Tranche currentTrancheAnnuelle;
    private String tabSelected = "Depenses";
    private ERXDisplayGroup<EOPrevisionBudget> dgPrevisions;
    private EOPrevisionBudget currentPrevision;
    private NSMutableArray<CktlAjaxTableViewColumn> colonnesPrev;
    private EOEditingContext ecForBudget;
    private EOUtilisateur userJefyAdmin;

    public ConventionBudget(WOContext context) {
        super(context);
        ecForBudget = ERXEC.newEditingContext();
        setIsTabDepensesSelected(true);
        NSNotificationCenter.defaultCenter().addObserver(
                this, 
                new NSSelector("rafraichirTranches",new Class[] { NSNotification.class }), 
                "refreshTranchesNotification", null);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        if (selectedTranche() != null && !selectedTranche().budgetablesSansPrevisionAvecOrgan().isEmpty()) {
            selectedTranche().createPrevisionsBudgetAuto(selectedTranche().exerciceCocktail().exercice(), userJefyAdmin());
        }
        super.appendToResponse(response, context);
    }
    
    private EOEditingContext ecForBudget() {
        return ecForBudget;
    }
    
    public EOUtilisateur userJefyAdmin() {
        if (userJefyAdmin == null)
            userJefyAdmin = FinderUtilisateur.getUtilisateur(ecForBudget(), applicationUser().getUtilisateur().utlOrdre());
        return userJefyAdmin;
    }
    
    private void saveChanges(String successMessage) {
        try {
            ecForBudget().saveChanges();
            session().addSimpleInfoMessage("Cocolight", successMessage);
        } catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        } catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }
    
    public void rafraichirTranches(NSNotification notif) {
        dgTranchesAnnuelles = null;
    }
    
    public WOActionResults openAjouterPrev() {
        NSNotificationCenter.defaultCenter().postNotification(WillOpenAjoutPrevision, null);
        return null;
    }
    
    public WOActionResults rafraichir() {
        dgPrevisions = null;
        dgPrevisions().selectObject(dgPrevisions().displayedObjects().lastObject());
        AjaxUpdateContainer.updateContainerWithID("ContainerAvenantBudget", context());
        return null;
    }
    
    public WOActionResults proposerPrevision() {
        EOTypeEtat enAttente = EOTypeEtat.getTypeEtat(ecForBudget(), EOTypeEtat.ETAT_A_VALIDER);
        selectedPrevision().setTypeEtatRelationship(enAttente);
        saveChanges("La prévision budgétaire a été proposéé et est en attente de validation");
        return null;
    }
    
    public WOActionResults validerPrevision() {
        EOTypeEtat valide = EOTypeEtat.getTypeEtat(ecForBudget(), EOTypeEtat.ETAT_VALIDE);
        selectedPrevision().setTypeEtatRelationship(valide);
        saveChanges("La prévision budgétaire a été validée, une proposition budgétaire peut être maintenant établie");
        return null;
    }
    
    public WOActionResults rejeterPrevision() {
        selectedTranche().rejeterPrevision(selectedPrevision(), true);
        saveChanges("La prévision budgétaire a été rejetée");
        // Envoie d'un mail si effectivement rejetée
        boolean hasSend = sendEmailRejetPrevision(selectedPrevision());
        if (!hasSend)
            LOG.error("Une erreur est survenue lors de la tentative d'envoie de mail");
        return null;
    }
    
    public WOActionResults supprimerPrevision() {
        if (selectedPrevision() != null && selectedPrevision().isEnAttente()) {
            selectedTranche().supprimerPrevision(selectedPrevision());
            saveChanges("Prévision supprimée avec succès");
            dgPrevisions = null;
        }
        return null;
    }
    
    public WOActionResults creerProposition() {
        EOPropositionBudget.creerProposition(userJefyAdmin(), selectedPrevision());
        saveChanges("La proposition budgétaire est en attente de validation dans Bibasse");
        return null;
    }
    
    private boolean sendEmailRejetPrevision(EOPrevisionBudget prevision) {
        String sujet = "Rejet du budget de la convention : " + selectedTranche().contrat().exerciceEtIndex();
        NSTimestampFormatter tf = new NSTimestampFormatter("%d/%m/%Y");
        String contenu = "Le budget prévisionnel du " + tf.format(prevision.pvbuDate()) + " pour l'exercice " + 
                              prevision.exercice().exeExercice() + " de la convention " +
                              selectedTranche().contrat().exerciceEtIndex() + " a été rejeté";
        if (!ERXStringUtilities.stringIsNullOrEmpty(prevision.pvbuCommentaire())) {
            contenu = contenu + " avec le commentaire suivant : \n" + prevision.pvbuCommentaire();
        } else {
            contenu = contenu + ".";
        }
        String toAddress = prevision.utilisateur().getEmail();
        if (toAddress != null) {
            CktlConfig conf = application().config();
            MyCRIMailBus mailBus = new MyCRIMailBus(conf);
            return mailBus.sendMail("noreply@cocktail.org", toAddress, null, sujet, contenu);
        }
        return false;
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
    
    public Tranche getCurrentTrancheAnnuelle() {
        return currentTrancheAnnuelle;
    }
    
    public void setCurrentTrancheAnnuelle(Tranche currentTrancheAnnuelle) {
        this.currentTrancheAnnuelle = currentTrancheAnnuelle;
    }

    
    public ERXDisplayGroup<Tranche> dgTranchesAnnuelles() {
        if (dgTranchesAnnuelles==null) {
                dgTranchesAnnuelles = new ERXDisplayGroup<Tranche>();
                dgTranchesAnnuelles.setSelectsFirstObjectAfterFetch(true);
            Avenant avenant = avenant();
            if (avenant != null) {
                Contrat contrat = avenant.contrat().localInstanceIn(ecForBudget());
                EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(Tranche.class), editingContext());
                ds.setArray(contrat.tranches());
                EOSortOrdering sortOrderingExercice = new EOSortOrdering(Tranche.EXERCICE_COCKTAIL_KEY,EOSortOrdering.CompareAscending); 
                NSArray orderings = new NSArray(sortOrderingExercice);
                dgTranchesAnnuelles.setSortOrderings(orderings);
                dgTranchesAnnuelles.setDataSource(ds);
                dgTranchesAnnuelles.fetch();
            }
        }
        return dgTranchesAnnuelles;
    }
 
    public Tranche selectedTranche() {
        return dgTranchesAnnuelles().selectedObject();
    }
    
    public EOExercice getExercice() {
        return selectedTranche().exerciceCocktail().exercice();
    }
    
    public boolean isExerceOuvertOuPreparation() {
        return getExercice() != null && (getExercice().estOuvert() || getExercice().estPreparation());
    }
    
    public ERXDisplayGroup<EOPrevisionBudget> dgPrevisions() {
        if (dgPrevisions==null) {
            dgPrevisions = new ERXDisplayGroup<EOPrevisionBudget>();
            dgPrevisions.setSelectsFirstObjectAfterFetch(true);
            Tranche tranche = selectedTranche();
            if (tranche != null) {
                dgPrevisions.setObjectArray(tranche.previsionsBudget());
                dgPrevisions.setSortOrderings(ERXS.ascs(EOPrevisionBudget.PVBU_DATE_KEY));
            }
        }
        return dgPrevisions;
    }
    
    public NSArray<CktlAjaxTableViewColumn> colonnesPrev() {
        if (colonnesPrev == null) {
            colonnesPrev = new NSMutableArray<CktlAjaxTableViewColumn>();
            CktlAjaxTableViewColumn col1 = new CktlAjaxTableViewColumn();
            col1.setLibelle("CR");
            col1.setRowCssClass("alignToLeft");
            String organ = EOPrevisionBudget.ORGAN_KEY + "." + EOOrgan.LONG_STRING_WITH_LIB_KEY;
            CktlAjaxTableViewColumnAssociation ass1 = new CktlAjaxTableViewColumnAssociation("currentPrevision" + "." + organ, " ");
            col1.setAssociations(ass1);
            CktlAjaxTableViewColumn col2 = new CktlAjaxTableViewColumn();
            col2.setLibelle("Date");
            col2.setHeaderCssClass("alignToCenter");
            String date = EOPrevisionBudget.PVBU_DATE_KEY;
            CktlAjaxTableViewColumnAssociation ass2 = new CktlAjaxTableViewColumnAssociation("currentPrevision" + "." + date, " ");
            col2.setAssociations(ass2);
            ass2.setDateformat(application().appDateFormatter.pattern());
            CktlAjaxTableViewColumn col3 = new CktlAjaxTableViewColumn();
            col3.setLibelle("Etat");
            col3.setHeaderCssClass("alignToCenter");
            String etat = EOPrevisionBudget.TYPE_ETAT_KEY + "." + EOTypeEtat.TYET_LIBELLE_KEY;
            CktlAjaxTableViewColumnAssociation ass3 = new CktlAjaxTableViewColumnAssociation("currentPrevision" + "." + etat, " ");
            col3.setAssociations(ass3);
            colonnesPrev.addObjects(col1, col2, col3);
        }
        return colonnesPrev.immutableClone();
    }
   
    public boolean hasRecettesOuDepenses() {
        return !selectedTranche().sbRecettes().isEmpty() || !selectedTranche().sbDepenses().isEmpty();
    }

    public EOPrevisionBudget selectedPrevision() {
        return dgPrevisions().selectedObject();
    }
    
    
    public void setIsTabRecettesSelected(boolean isSelected) {
        if (isSelected) tabSelected = "Recettes";
    }
    public boolean isTabRecettesSelected() {
        return tabSelected == "Recettes";
    }
    
    public void setIsTabDepensesSelected(boolean isSelected) {
        if (isSelected) tabSelected = "Depenses";
    }
    public boolean isTabDepensesSelected() {
        return tabSelected == "Depenses";
    }
    
    public void setCurrentPrevision(EOPrevisionBudget currentPrevision) {
        this.currentPrevision = currentPrevision;
    }
    
    public EOPrevisionBudget getCurrentPrevision() {
        return currentPrevision;
    }
    
    public boolean selectedPrevisionNotEnAttente() {
        return selectedPrevision() != null && !selectedPrevision().isEnAttente();
    }
    
}