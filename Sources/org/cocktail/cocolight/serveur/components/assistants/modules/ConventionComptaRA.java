package org.cocktail.cocolight.serveur.components.assistants.modules;

import java.math.BigDecimal;

import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktlcompta.common.metier.EOBrouillard;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class ConventionComptaRA extends ModuleAssistant {
    
    public static final String BINDING_CONTRAT = "contrat";
    private BigDecimal montant;
    private NSArray<EOOrgan> organs;
    private EOOrgan currentOrgan;
    private EOOrgan selectedOrgan;
    private EOExercice exercice;
    private EOEditingContext ecForComptaRA;
    private WODisplayGroup dgBrouillardsDetail;
    private WODisplayGroup dgBrouillards;
    
    public ConventionComptaRA(WOContext context) {
        super(context);
        ecForComptaRA = ERXEC.newEditingContext();
    }
    
    @Override
    public Contrat contrat() {
        return super.contrat().localInstanceIn(ecForComptaRA);
    }
    
    public WOActionResults creerBrouillard() {
        try {
            EOOrgan localOrgan = getSelectedOrgan() != null ? getSelectedOrgan().localInstanceIn(ecForComptaRA) : null;
            contrat().creerBrouillard(ecForComptaRA, getExercice(), montant, localOrgan, applicationUser());
            session().addSimpleInfoMessage("Cocolight", "Brouillard créé avec succès");
            dgBrouillardsDetail = null;
            dgBrouillards = null;
        } catch (Exception e) {
            e.printStackTrace();
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
    }

    public boolean hasBrouillardsEnAttente() {
        return !contrat().brouillardsEnAttente().isEmpty();
    }
    
    public WODisplayGroup getDgBrouillardsDetail() {
        if (dgBrouillardsDetail == null) {
            dgBrouillardsDetail = new WODisplayGroup();
        }
        return dgBrouillardsDetail;
    }

    public WODisplayGroup getDgBrouillards() {
        if (dgBrouillards == null) {
            dgBrouillards = new WODisplayGroup();
            NSArray<EOBrouillard> res = contrat().brouillards(null, new NSArray(new Object[] {
                    EOBrouillard.SORT_BRO_NUMERO_ASC
                }));
            dgBrouillards.setObjectArray(res);
        }
        return dgBrouillards;
    }
    
    public EOExercice getExercice() {
        if (exercice == null)
            exercice = EOExercice.getExerciceOuvert(ecForComptaRA);
        return exercice;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
    
    public NSArray<EOOrgan> organs() {
        if (organs == null) {
            EOStructure centreResp = contrat().centreResponsabilite();
            Integer utl = applicationUser().getUtilisateur().utlOrdre();
            organs = FinderOrgan.fetchOrgansForStructureAndUtl(ecForComptaRA, centreResp.cStructure(), getExercice(), utl, contrat().niveauOrganMin(), false);
        }
        return organs;
    }
    
    public boolean hasNoOrgan() {
        return organs().count() == 0;
    }
    
    public EOOrgan getSelectedOrgan() {
        return selectedOrgan;
    }
    
    public void setSelectedOrgan(EOOrgan selectedOrgan) {
        this.selectedOrgan = selectedOrgan;
    }
 
    public EOOrgan getCurrentOrgan() {
        return currentOrgan;
    }
    
    public void setCurrentOrgan(EOOrgan currentOrgan) {
        this.currentOrgan = currentOrgan;
    }
    
}