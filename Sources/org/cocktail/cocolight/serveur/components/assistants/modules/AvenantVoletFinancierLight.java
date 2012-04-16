/*
 * Copyright COCKTAIL (www.cocktail.org), 1995, 2010 This software 
 * is governed by the CeCILL license under French law and abiding by the
 * rules of distribution of free software. You can use, modify and/or 
 * redistribute the software under the terms of the CeCILL license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 * As a counterpart to the access to the source code and rights to copy, modify 
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. In this 
 * respect, the user's attention is drawn to the risks associated with loading,
 * using, modifying and/or developing or reproducing the software by the user 
 * in light of its specific status of free software, that may mean that it
 * is complicated to manipulate, and that also therefore means that it is 
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the 
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally, 
 * to use and operate it in the same conditions as regards security. The
 * fact that you are presently reading this means that you have had knowledge 
 * of the CeCILL license and that you accept its terms.
 */
package org.cocktail.cocolight.serveur.components.assistants.modules;

import org.cocktail.cocolight.serveur.components.controlers.AvenantVoletFinancierLightCtrl;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Parametre;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

public class AvenantVoletFinancierLight extends ModuleAssistant {
    
    private AvenantVoletFinancierLightCtrl ctrl = null;
    private EOTva unTauxDeTVA;
    private Tranche currentTrancheAnnuelle;
    
    public AvenantVoletFinancierLight(WOContext context) {
        super(context);
    }

    public AvenantVoletFinancierLightCtrl ctrl() {
        if (ctrl == null) {
            ctrl = new AvenantVoletFinancierLightCtrl(this);
            Class[] notificationArray = new Class[] { NSNotification.class };
            NSNotificationCenter.defaultCenter().addObserver(ctrl, new NSSelector("refreshTranches",notificationArray), "refreshTranchesNotification", null);
        }
        return ctrl;
    }

    public boolean isCBAvenantLucratifDisabled() {
        if (disabled())
            return true;
        boolean isCBAvenantLucratifDisabled = false;
        Avenant avenant = avenant();
        NSArray tranches = avenant.contrat().tranches();
        if (tranches.isEmpty()==false) {
            isCBAvenantLucratifDisabled = true;
        }
        return isCBAvenantLucratifDisabled;
    }

    public boolean isCBCreditsLimitatifsDisabled() {
        if (ctrl().montantDisabled())
            return true;
        boolean isCBCreditsLimitatifsDisabled = false;
        
        if (contrat() != null && contrat().modeDeGestion() != null && contrat().modeDeGestion().mgLibelleCourt().equalsIgnoreCase("RA")) {
            isCBCreditsLimitatifsDisabled = true;
        }
        return isCBCreditsLimitatifsDisabled;
    }

    public boolean isTrancheBudgetSelected() {
        Tranche selectedTranche = (Tranche)ctrl.dgTranchesAnnuelles().selectedObject();
        return selectedTranche != null && selectedTranche.traNatureMontant() != null;
    }
    
    /**
     * @return the unTauxDeTVA
     */
    public EOTva unTauxDeTVA() {
        return unTauxDeTVA;
    }

    /**
     * @param unTauxDeTVA the unTauxDeTVA to set
     */
    public void setUnTauxDeTVA(EOTva unTauxDeTVA) {
        this.unTauxDeTVA = unTauxDeTVA;
    }

    public String updateContainerIDsForTVTranchesAnnuelles() {
        String updateContainerIDsForTVTranchesAnnuelles = null;
        WODisplayGroup dgTranchesAnnuelles = ctrl().dgTranchesAnnuelles();
        if (dgTranchesAnnuelles != null && dgTranchesAnnuelles.selectedObject() != null) {
            updateContainerIDsForTVTranchesAnnuelles = "TableViewTranchesAnnuelles_colonne_"+dgTranchesAnnuelles.allObjects().indexOfIdenticalObject(dgTranchesAnnuelles.selectedObject())+"_2";
            updateContainerIDsForTVTranchesAnnuelles += ",ContainerMontantDisponible"; 
            updateContainerIDsForTVTranchesAnnuelles += ",ContainerDetailTranche"; 
        }
        return updateContainerIDsForTVTranchesAnnuelles;
    }
    public String updateContainerIDsForTVContributeurs() {
        String updateContainerIDsForTVContributeurs = "ContainerMontantDisponible";
        WODisplayGroup dgTranchesAnnuelles = ctrl().dgTranchesAnnuelles();
        if (dgTranchesAnnuelles != null && dgTranchesAnnuelles.selectedObject() != null) {
            updateContainerIDsForTVContributeurs += ",TableViewTranchesAnnuelles";
        }
        return updateContainerIDsForTVContributeurs;
    }
    
    public Tranche getCurrentTrancheAnnuelle() {
        return currentTrancheAnnuelle;
    }
    
    public void setCurrentTrancheAnnuelle(Tranche currentTrancheAnnuelle) {
        this.currentTrancheAnnuelle = currentTrancheAnnuelle;
    }
    
}
