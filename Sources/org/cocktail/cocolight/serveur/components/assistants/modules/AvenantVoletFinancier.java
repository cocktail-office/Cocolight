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

import java.math.BigDecimal;

import org.cocktail.cocolight.serveur.components.controlers.AvenantVoletFinancierCtrl;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.SbDepense;
import org.cocktail.cocowork.server.metier.convention.SbRecette;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktljefyadmin.common.metier.EOLolfNomenclatureDepense;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

public class AvenantVoletFinancier extends ModuleAssistant {
	
	public AvenantVoletFinancierCtrl ctrl = null;
	private String selectedTab;
	private EOTva unTauxDeTVA, unTauxDeTVADepense, unTauxDeTVARecette;
	private EOLolfNomenclatureDepense leCodeLolf;
	
	public AvenantVoletFinancier(WOContext context) {
        super(context);
		selectedTab = "Contributeurs";
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


	public void setCtrl(AvenantVoletFinancierCtrl ctrl) {
		this.ctrl = ctrl;
	}

	public WOActionResults initialiserOnglets() {
		if (selectedTab == null) {
			selectedTab = "Contributeurs";
		} else {
			if (selectedTab.equals("Recettes")) {
				Tranche trancheAnnuelleSelectionnee = (Tranche)ctrl().dgTranchesAnnuelles().selectedObject();
				if (trancheAnnuelleSelectionnee != null && trancheAnnuelleSelectionnee.traNatureMontant().equals("DEPENSE")) {
					selectedTab = "Contributeurs";
				}
			} else if (selectedTab.equals("Depenses")) {
				Tranche trancheAnnuelleSelectionnee = (Tranche)ctrl().dgTranchesAnnuelles().selectedObject();
				if (trancheAnnuelleSelectionnee != null && trancheAnnuelleSelectionnee.traNatureMontant().equals("RECETTE")) {
					selectedTab = "Contributeurs";
				}
			}
		}
		return null;
	}

	public String titleForNewRecette() {
	    return "Ajouter une nouvelle recette sur la tranche " + ((Tranche)ctrl().dgTranchesAnnuelles().selectedObject()).exerciceCocktail().exeExercice();
	}
	
	public String titleForNewDepense() {
	    return "Ajouter une nouvelle dépense sur la tranche " + ((Tranche)ctrl().dgTranchesAnnuelles().selectedObject()).exerciceCocktail().exeExercice();
	}
	
    public void setIsTabContributeursSelected(boolean isSelected) {
    	if (isSelected) selectedTab = "Contributeurs";
    }
    public boolean isTabContributeursSelected() {
		return selectedTab == "Contributeurs";
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

	public boolean isTrancheBudgetSelected() {
	    Tranche selectedTranche = (Tranche)ctrl.dgTranchesAnnuelles().selectedObject();
	    return selectedTranche != null && selectedTranche.traNatureMontant() != null;
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
	    if (disabled())
	        return true;
		boolean isCBCreditsLimitatifsDisabled = false;
		
		if (contrat() != null && contrat().modeDeGestion() != null && contrat().modeDeGestion().mgLibelleCourt().equalsIgnoreCase("RA")) {
			isCBCreditsLimitatifsDisabled = true;
		}
		return isCBCreditsLimitatifsDisabled;
	}

	public WOActionResults refreshContainerAvenantVoletFinancier() {
		if (ctrl.dgTranchesAnnuelles().selectedObject() != null) {
			Tranche trancheSelectionnee = (Tranche)ctrl.dgTranchesAnnuelles().selectedObject();
			if ("DEPENSE".equals(trancheSelectionnee.traNatureMontant())) {
				selectedTab = "Dépenses";
			} else if ("RECETTE".equals(trancheSelectionnee.traNatureMontant())) {
				selectedTab = "Recettes";
			}
		} else {
			selectedTab = "Contributeurs";
		}
		return null;
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

	/**
	 * @return the unTauxDeTVADepense
	 */
	public EOTva unTauxDeTVADepense() {
		return unTauxDeTVADepense;
	}

	/**
	 * @param unTauxDeTVADepense the unTauxDeTVADepense to set
	 */
	public void setUnTauxDeTVADepense(EOTva unTauxDeTVADepense) {
		this.unTauxDeTVADepense = unTauxDeTVADepense;
	}

	/**
	 * @return the unTauxDeTVARecette
	 */
	public EOTva unTauxDeTVARecette() {
		return unTauxDeTVARecette;
	}

	/**
	 * @param unTauxDeTVARecette the unTauxDeTVARecette to set
	 */
	public void setUnTauxDeTVARecette(EOTva unTauxDeTVARecette) {
		this.unTauxDeTVARecette = unTauxDeTVARecette;
	}

	public BigDecimal montantTTCDepense() {
		BigDecimal montantTTCDepense = BigDecimal.valueOf(0.00);
		if (laDepenseSelectionnee() != null) {
			SbDepense laDepense = laDepenseSelectionnee();
			BigDecimal montantHT = laDepense.sdMontantHt();
			if (montantHT != null) {
				EOTva tva = laDepense.tva();
				BigDecimal taux = BigDecimal.valueOf(0);
				if (tva != null) {
					taux = tva.tvaTaux();
				}
				montantTTCDepense = montantHT.multiply(BigDecimal.valueOf(1).add(taux.divide(BigDecimal.valueOf(100))));
			}
		}
		return montantTTCDepense;
	}

	public SbDepense laDepenseSelectionnee() {
		return (SbDepense)ctrl.dgDepenses().selectedObject();
	}

	public BigDecimal montantTTCRecette() {
		BigDecimal montantTTCRecette = BigDecimal.valueOf(0.00);
		if (laRecetteSelectionnee() != null) {
			SbRecette laRecette = laRecetteSelectionnee();
			BigDecimal montantHT = laRecette.srMontantHt();
			if (montantHT != null) {
				EOTva tva = laRecette.tva();
				BigDecimal taux = BigDecimal.valueOf(0);
				if (tva != null) {
					taux = tva.tvaTaux();
				}
				montantTTCRecette = montantHT.multiply(BigDecimal.valueOf(1).add(taux.divide(BigDecimal.valueOf(100))));
			}
		}
		return montantTTCRecette;
	}

	public SbRecette laRecetteSelectionnee() {
		return (SbRecette)ctrl.dgRecettes().selectedObject();
	}

	public String currentExercice() {
		return String.valueOf(((Tranche)ctrl.dgTranchesAnnuelles().selectedObject()).exerciceCocktail().exeExercice().intValue());
	}

	/**
	 * @return the leCodeLolf
	 */
	public EOLolfNomenclatureDepense leCodeLolf() {
		return leCodeLolf;
	}

	/**
	 * @param leCodeLolf the leCodeLolf to set
	 */
	public void setLeCodeLolf(EOLolfNomenclatureDepense leCodeLolf) {
		this.leCodeLolf = leCodeLolf;
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

	public String cssForBalance() {
	    if (contrat().montantDepenses().doubleValue() > contrat().montantRecettes().doubleValue())
	        return "balanceNeg";
	    else
	        return "balancePos";
	}
	
}
