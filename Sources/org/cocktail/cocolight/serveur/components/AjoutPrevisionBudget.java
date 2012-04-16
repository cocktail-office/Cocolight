package org.cocktail.cocolight.serveur.components;

import org.cocktail.cocolight.serveur.components.assistants.modules.ConventionBudget;
import org.cocktail.cocowork.server.CocoworkApplicationUser;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetParametre;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPrevisionBudget;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderTypeApplication;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeApplication;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeEtat;
import org.cocktail.fwkcktljefyadmin.common.metier.EOUtilisateur;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXSelectorUtilities;

public class AjoutPrevisionBudget extends MyWOComponent {

    private static final long serialVersionUID = -5089986589769702770L;
    public static final String BINDING_TRANCHE = "tranche";
    public static final String BINDING_USER_JEFY = "userJefy";
    public static final String BINDING_ON_CREATED_PREV = "onCreatedPrevision";
    
    private Tranche tranche;
    private EOOrgan selectedOrgan;
    private EOOrgan selectedOrganForRA;
    private Integer niveauBibasse;
    private EOEditingContext editingContextForAjoutPrev;
    
    public AjoutPrevisionBudget(WOContext context) {
        super(context);
        NSNotificationCenter.defaultCenter().addObserver(
                this, ERXSelectorUtilities.notificationSelector("willReOpen"), ConventionBudget.WillOpenAjoutPrevision, null);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public int niveauBibasse() {
        if (niveauBibasse == null)
            niveauBibasse = FinderBudgetParametre.getParametreOrganNiveauSaisie(
                    editingContextForAjoutPrev(), tranche().exerciceCocktail().exercice());
        return niveauBibasse;
    }
    
    public String labelForNiveauBibasse() {
        return (String) EOOrgan.NIV_LIB_MAP.get(niveauBibasse);
    }
    
    public EOEditingContext editingContextForAjoutPrev() {
        if (editingContextForAjoutPrev == null) {
            Tranche trancheFromParent = (Tranche)valueForBinding(BINDING_TRANCHE);
            editingContextForAjoutPrev = ERXEC.newEditingContext(trancheFromParent.editingContext());
        }
        return editingContextForAjoutPrev;
    }
    
    public void willReOpen(NSNotification notif) {
        this.niveauBibasse = null;
        this.editingContextForAjoutPrev = null;
        this.tranche = null;
        this.selectedOrgan = null;
        this.selectedOrganForRA = null;
    }
    
    public WOActionResults creerPrevisionRA() {
        // On crée d'abord la ligne budgétaire pour la conv RA si besoin
        try {
            tranche().contrat().creerCrForRA(getSelectedOrganForRA());
            setSelectedOrgan(tranche().contrat().organComposante());
        } catch (Exception e) {
            NSForwardException._runtimeExceptionForThrowable(e);
        }
        return null;
    }
    
    public WOActionResults creerPrevision() {
        if (selectedOrgan != null) {
            EOEditingContext ec = editingContextForAjoutPrev();
            // Après sélection on rajoute une prevision
            EOTypeApplication typeApp = FinderTypeApplication.getTypeApplication(ec, CocoworkApplicationUser.TYAP_STR_ID_COCONUT);
            EOTypeEtat typeEtat = EOTypeEtat.getTypeEtat(ec, EOTypeEtat.ETAT_EN_ATTENTE);
            Tranche tranche = tranche().localInstanceIn(ec);
            EOExercice exo = tranche().exerciceCocktail().exercice().localInstanceIn(ec);
            EOUtilisateur userJefyAdmin = ((EOUtilisateur)valueForBinding(BINDING_USER_JEFY)).localInstanceIn(ec);
            EOOrgan localOrgan = selectedOrgan.localInstanceIn(ec);
            EOPrevisionBudget prev = 
                tranche.getOrCreatePrevisionBudget(exo, typeApp, typeEtat, localOrgan, userJefyAdmin, NSArray.EmptyArray);
            try {
                // Si la prévision récupérée n'est pas en attente ou  rejetée, on lève une exception
                if (!prev.isEnAttente())
                    throw new ValidationException("Une prévision existe déjà pour cette ligne budgétaire");
                ec.saveChanges();
                if (hasBinding(BINDING_ON_CREATED_PREV)) {
                    valueForBinding(BINDING_ON_CREATED_PREV);
                }
                CktlAjaxWindow.close(context());
            } catch (ValidationException e) {
                session().addSimpleErrorMessage("Cocolight", e.getMessage());
            } catch (Exception e) {
                throw new NSForwardException(e);
            }
        } else {
            session().addSimpleErrorMessage("Cocolight", "Veuillez sélectionner une ligne budgétaire");
        }
        return null;
    }
    
    public Tranche tranche() {
        if (tranche == null) {
            Tranche trancheFromParent = (Tranche)valueForBinding(BINDING_TRANCHE);
            tranche = trancheFromParent.localInstanceIn(editingContextForAjoutPrev());
        }
        return tranche;
    }
    
    public EOOrgan getSelectedOrgan() {
        return selectedOrgan;
    }
    
    public void setSelectedOrgan(EOOrgan selectedOrgan) {
        this.selectedOrgan = selectedOrgan;
        // On creer la prevision directement 
        creerPrevision();
    }
    
    public EOOrgan getSelectedOrganForRA() {
        return selectedOrganForRA;
    }
    
    public void setSelectedOrganForRA(EOOrgan selectedOrganForRA) {
        this.selectedOrganForRA = selectedOrganForRA;
        // Appelé lors du click sur l'arbre des organs, on crée la prev RA à ce moment là
        creerPrevisionRA();
    }
    
}