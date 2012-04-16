package org.cocktail.cocolight.serveur.components;

import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumn;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumnAssociation;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetMasqueCredit;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPropositionBudget;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPropositionPositionnement;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeCredit;
import org.cocktail.fwkcktljefyadmin.common.metier.EOUtilisateur;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.extensions.eof.ERXEC;

public class GestionBudgetPositionnements extends MyWOComponent {
    
    public static final String BINDING_PROPOSITION = "proposition";
    public static final String BINDING_UTL = "utilisateur";
    
    private WODisplayGroup displayGroup;
    private EOPropositionPositionnement currentPos;
    private NSArray<CktlAjaxTableViewColumn> colonnes;
    private NSArray<EOTypeCredit> typesCredit;
    private EOTypeCredit currentTypeCredit;
    private EOOrgan currentOrgan;
    private EOPropositionPositionnement newPropPositionnement;
    private EOEditingContext nestedEcForAjout;

    public GestionBudgetPositionnements(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public WOActionResults ajouterPositionnement() {
        EOEditingContext ec = nestedEcForAjout();
        newPropPositionnement = EOPropositionPositionnement.creerPropositionPositionnement(
                proposition().localInstanceIn(ec), utilisateur().localInstanceIn(ec));
        return null;
    }
    
    public EOEditingContext nestedEcForAjout() {
        if (nestedEcForAjout == null && proposition() != null)
            nestedEcForAjout = ERXEC.newEditingContext(proposition().editingContext());
        return nestedEcForAjout;
    }
    
    public WOActionResults enregistrer() {
        try {
            nestedEcForAjout().saveChanges();
            displayGroup = null;
            CktlAjaxWindow.close(context());
        } catch (NSValidation.ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }
    
    public NSArray<CktlAjaxTableViewColumn> colonnes() {
        if (colonnes == null) {
            // Type de credit
            CktlAjaxTableViewColumn col0 = new CktlAjaxTableViewColumn();
            CktlAjaxTableViewColumnAssociation asso = new CktlAjaxTableViewColumnAssociation(
                    "currentPos."+EOPropositionPositionnement.TYPE_CREDIT_KEY + "." + EOTypeCredit.TCD_CODE_LIB_ABREGE_KEY, null);
            col0.setAssociations(asso);
            col0.setLibelle("Type de crédit");
            // Organ
            CktlAjaxTableViewColumn col1 = new CktlAjaxTableViewColumn();
            asso = new CktlAjaxTableViewColumnAssociation(
                    "currentPos."+EOPropositionPositionnement.ORGAN_KEY + "." + EOOrgan.LONG_STRING_KEY, null);
            col1.setAssociations(asso);
            col1.setLibelle("Ligne budgétaire");
            // Date de proposition
            CktlAjaxTableViewColumn col2 = new CktlAjaxTableViewColumn();
            asso = new CktlAjaxTableViewColumnAssociation(
                    "currentPos."+EOPropositionPositionnement.PRPO_DATE_KEY, null);
            asso.setDateformat(application().appDateFormatter.pattern());
            col2.setAssociations(asso);
            col2.setLibelle("Date de proposition");
            // Montant proposé
            CktlAjaxTableViewColumn col3 = new CktlAjaxTableViewColumn();
            asso = new CktlAjaxTableViewColumnAssociation(
                    "currentPos."+EOPropositionPositionnement.PRPO_MONTANT_PREVISIONNEL_KEY, null);
            asso.setNumberformat(application().app2DecimalesFormatter.pattern());
            col3.setAssociations(asso);
            col3.setLibelle("Montant proposé");
            // Montant ouvert
            CktlAjaxTableViewColumn col4 = new CktlAjaxTableViewColumn();
            asso = new CktlAjaxTableViewColumnAssociation(
                    "currentPos."+EOPropositionPositionnement.PRPO_MONTANT_OUVERT_KEY, null);
            asso.setNumberformat(application().app2DecimalesFormatter.pattern());
            col4.setAssociations(asso);
            col4.setLibelle("Montant ouvert");
            colonnes = new NSArray<CktlAjaxTableViewColumn>(col0, col1, col2, col3, col4);
        }
        return colonnes;
    }
    
    public EOUtilisateur utilisateur() {
        return (EOUtilisateur)valueForBinding(BINDING_UTL);
    }
    
    public EOPropositionBudget proposition() {
        return (EOPropositionBudget)valueForBinding(BINDING_PROPOSITION);
    }
    
    @SuppressWarnings("unchecked")
    public WODisplayGroup positionsDg() {
        if (displayGroup == null) {
            displayGroup = new WODisplayGroup();
            displayGroup.setObjectArray(proposition().propositionPositionnements());
        }
        return displayGroup;
    }
    
    public NSArray<EOTypeCredit> getTypesCredit() {
        if (typesCredit == null) {
            typesCredit = FinderBudgetMasqueCredit.findTypesCredit(proposition().editingContext(), 
                    proposition().exercice(), null, EOTypeCredit.TCD_TYPE_DEPENSE);
        }
        return typesCredit;
    }
    
    public EOTypeCredit getSelectedTypeCredit() {
        return newPropPositionnement.typeCredit();
    }
    
    public void setSelectedTypeCredit(EOTypeCredit typeCredit) {
        if (typeCredit != null)
            newPropPositionnement.setTypeCreditRelationship(typeCredit.localInstanceIn(nestedEcForAjout()));
        else
            newPropPositionnement.setTypeCreditRelationship(null);
    }
    
    public EOOrgan getSelectedSsCR() {
        return newPropPositionnement.organ();
    }
    
    public void setSelectedSsCR(EOOrgan organ) {
        if (organ != null)
            newPropPositionnement.setOrganRelationship(organ.localInstanceIn(nestedEcForAjout()));
        else
            newPropPositionnement.setOrganRelationship(null);
    }
    
    public EOPropositionPositionnement getCurrentPos() {
        return currentPos;
    }
    
    public void setCurrentPos(EOPropositionPositionnement currentPos) {
        this.currentPos = currentPos;
    }
    
    public EOTypeCredit getCurrentTypeCredit() {
        return currentTypeCredit;
    }
    
    public void setCurrentTypeCredit(EOTypeCredit currentTypeCredit) {
        this.currentTypeCredit = currentTypeCredit;
    }
    
    public EOPropositionPositionnement getNewPropPositionnement() {
        return newPropPositionnement;
    }
    
    public EOOrgan getCurrentOrgan() {
        return currentOrgan;
    }
    
    public void setCurrentOrgan(EOOrgan currentOrgan) {
        this.currentOrgan = currentOrgan;
    }
    
}