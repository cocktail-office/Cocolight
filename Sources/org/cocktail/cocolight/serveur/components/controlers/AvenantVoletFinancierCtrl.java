/*
 * Copyright COCKTAIL (www.cocktail.org), 1995, 2011 This software 
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
package org.cocktail.cocolight.serveur.components.controlers;

import java.math.BigDecimal;

import org.cocktail.cocolight.serveur.Application;
import org.cocktail.cocolight.serveur.Session;
import org.cocktail.cocolight.serveur.components.GestionDepenses;
import org.cocktail.cocolight.serveur.components.GestionRecettes;
import org.cocktail.cocolight.serveur.components.assistants.modules.ConventionDepensesRecettes;
import org.cocktail.cocowork.common.tools.Constantes;
import org.cocktail.cocowork.server.CocoworkApplicationUser;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.EOContratPartenaire;
import org.cocktail.cocowork.server.metier.convention.EORepartPartenaireTranche;
import org.cocktail.cocowork.server.metier.convention.Parametre;
import org.cocktail.cocowork.server.metier.convention.RepartPartenaireTranche;
import org.cocktail.cocowork.server.metier.convention.SbDepense;
import org.cocktail.cocowork.server.metier.convention.SbRecette;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryTranche;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.modules.ModuleAssistant;
import org.cocktail.fwkcktlaccordsguiajax.components.controlers.CtrlModule;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktldroitsutils.common.metier.EOUtilisateur;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExerciceCocktail;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSValidation.ValidationException;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXProperties;

public class AvenantVoletFinancierCtrl extends CtrlModule<ModuleAssistant> {

	private static NSArray TAUX_DE_TVA = new NSArray(new String[]{"0","5.5","19.6"});
	private String unTauxDeTVA;
	private String leTauxDeTVASelectionne;

	private WODisplayGroup dgTranchesAnnuelles;
	private NSMutableDictionary dicoTranchesAnnuelles;
	
	private Tranche uneTrancheAnnuelle;
	private boolean refreshTranches;
	
	NSArray lesNatures = new NSArray(new String[]{"DEPENSE", "RECETTE", "DEPENSE_RECETTE"});
	String uneNature;
	
	private WODisplayGroup dgRepartPartenaireTranches;
	private NSMutableDictionary dicoRepartPartenaireTranches;
	
	private RepartPartenaireTranche uneRepartPartenaireTranche;
	private boolean refreshRepartPartenaireTranches;
	
	private WODisplayGroup dgRecettes;
	private NSMutableDictionary dicoRecettesDestination;
	private NSMutableDictionary dicoRecettesNature;
	
	private SbRecette uneRecette;
	private boolean refreshRecettes;
	
	private WODisplayGroup dgDepenses;
	private NSMutableDictionary dicoDepensesDestination;
	private NSMutableDictionary dicoDepensesNature;
	
	private SbDepense uneDepense;
	private boolean refreshDepenses;
	
	private EOEditingContext editingContextForEdition;
	
	private NSArray<EOExerciceCocktail> exercicesCocktail;
	private EOExerciceCocktail currentExercice;
	private EOExerciceCocktail selectedExercice;
	
	public AvenantVoletFinancierCtrl(ModuleAssistant component) {
	    super(component);
	}
	
    public boolean montantDisabled() {
        if (wocomponent().avenant() != null && wocomponent().avenant().contrat() != null && wocomponent().avenant().contrat().isSigne()) {
            return !Parametre.paramBooleanForKey(Parametre.GENERALITES_MODIFIABLES_APRES_VALIDATION);
        } else {
            return wocomponent().disabled();
        }
    }
	
    public boolean isGestionTrancheAuto() {
        return ERXProperties.booleanForKeyWithDefault(Constantes.GESTION_TRANCHE_AUTO_PARAM, true);
    }
	
    public boolean creerTrancheDisabled() {
        CocoworkApplicationUser user = ((Session)wocomponent().session()).applicationUser();
        return wocomponent().disabled() || !user.hasDroitCreationTranches(); 
    }
    
    public boolean supprimerTrancheDisabled() {
        CocoworkApplicationUser user = ((Session)wocomponent().session()).applicationUser();
        return wocomponent().disabled() || !user.hasDroitSuppressionTranches(); 
    }
    
    public WOActionResults ouvrirAjoutTranche() {
        exercicesCocktail = null;
        selectedExercice = exercicesCocktail().lastObject();
        return null;
    }
    
    public WOActionResults supprimerTranche() {
        if (selectedTranche() != null) {
            wocomponent().contrat().removeFromTranches(selectedTranche());
            selectedTranche().delete();
            refreshTranches = true;
        }
        return null;
    }
    
    public WOActionResults validerAjoutTranche() throws Exception {
        FactoryTranche factory = new FactoryTranche(editingContext());
        EOUtilisateur utilisateur = ((Session)wocomponent().session()).applicationUser().getUtilisateur().localInstanceIn(editingContext());
        factory.creerTranche(wocomponent().contrat(), selectedExercice, BigDecimal.ZERO, BigDecimal.ZERO, Tranche.NATURE_DEPENSE_RECETTE, utilisateur);
        refreshTranches = true;
        CktlAjaxWindow.close(wocomponent().context());
        return null;
    }
    
    public WOActionResults annulerAjoutTranche() {
        // editingContext().revert();
        CktlAjaxWindow.close(wocomponent().context());
        return null;
    }
    
	public WOComponent submit() {
		Avenant avenant = wocomponent().avenant();
		BigDecimal mtHT = avenant.avtMontantHt();
		if (avenant != null &&  mtHT!= null) {
			BigDecimal aValue = mtHT;
			if (leTauxDeTVASelectionne != null) {
				BigDecimal taux = new BigDecimal(leTauxDeTVASelectionne);
				taux = taux.add(new BigDecimal(100.00));
				aValue = aValue.multiply(taux);
				aValue = aValue.divide(new BigDecimal(100.00));
			}
			avenant.setAvtMontantTtc(aValue);
		}
		return null;
	}
	
	public NSArray<EOExerciceCocktail> exercicesCocktail() {
	    if (exercicesCocktail == null) {
	        // On prend l'exercice courant auquel on ajoute et retranche 5 ans... ça devrait suffire
	        Long currentExo = ((Application)wocomponent().application()).getDernierExerciceOuvertOuEnPreparation().exeExercice();
	        EOQualifier qual = ERXQ.between(EOExerciceCocktail.EXE_ORDRE_KEY, currentExo - 5, currentExo + 5);
	        exercicesCocktail = EOExerciceCocktail.fetchAll(editingContext(), qual);
	        NSArray<EOExerciceCocktail> exosExistants = (NSArray<EOExerciceCocktail>) dgTranchesAnnuelles().allObjects().valueForKey(Tranche.EXERCICE_COCKTAIL_KEY);;
	        NSMutableArray<EOExerciceCocktail> exercicesCocktailTmp = exercicesCocktail.mutableClone();
	        exercicesCocktailTmp.removeObjectsInArray(exosExistants);
	        exercicesCocktail = exercicesCocktailTmp.immutableClone();
	    }
	    return exercicesCocktail;
	}
	
	public NSArray tauxDeTVA() {
		return TAUX_DE_TVA;
	}

	/**
	 * @return the unTauxDeTVA
	 */
	public String unTauxDeTVA() {
		return unTauxDeTVA;
	}

	/**
	 * @param unTauxDeTVA the unTauxDeTVA to set
	 */
	public void setUnTauxDeTVA(String unTauxDeTVA) {
		this.unTauxDeTVA = unTauxDeTVA;
	}

	/**
	 * @return the leTauxDeTVASelectionne
	 */
	public String leTauxDeTVASelectionne() {
		if (leTauxDeTVASelectionne == null) {
			Avenant avenant = wocomponent().avenant();
			BigDecimal montantHT = avenant.avtMontantHt();
			BigDecimal montantTTC = avenant.avtMontantTtc();
			if (montantHT != null && montantHT.doubleValue()>0 && montantTTC != null) {
				double taux = (montantTTC.doubleValue()-montantHT.doubleValue())/montantHT.doubleValue();
				leTauxDeTVASelectionne = String.valueOf(taux);
			}
		}
		return leTauxDeTVASelectionne;
	}

	/**
	 * @param leTauxDeTVASelectionne the leTauxDeTVASelectionne to set
	 */
	public void setLeTauxDeTVASelectionne(String leTauxDeTVASelectionne) {
		this.leTauxDeTVASelectionne = leTauxDeTVASelectionne;
		double taux = Double.valueOf(leTauxDeTVASelectionne).doubleValue();
		Avenant avenant = wocomponent().avenant();
		BigDecimal montantHT = avenant.avtMontantHt();
		if (montantHT != null) {
			double ht = montantHT.doubleValue();
			double ttc = ht*(1+taux/100);
			avenant.setAvtMontantTtc(BigDecimal.valueOf(ttc));
		} else {
			avenant.setAvtMontantTtc(null);
		}
	}

	public String typeComposante() {
		return FinderOrgan.ORGAN_BUDGET;
	}
	public String typeComposanteDepense() {
		return FinderOrgan.ORGAN_DEPENSE;
	}
	public String typeComposanteRecette() {
		return FinderOrgan.ORGAN_RECETTE;
	}
	
	public WOActionResults selectionnerUneTrancheAnnuelle() {
		refreshDepenses = true;
		refreshRecettes = true;
		return null;
	}
	
	public void refreshTranches(NSNotification notification) {
		if (notification.userInfo() != null && notification.userInfo().containsKey("edc")) {
			EOEditingContext ed = (EOEditingContext)notification.userInfo().objectForKey("edc");
			if (ed.equals(editingContext())) {
				refreshTranches = true;
			}
		} else {
			refreshTranches = true;
		}
	}

	public NSMutableDictionary dicoTranchesAnnuelles() {
		if (dicoTranchesAnnuelles == null) {
			NSData data = new NSData(wocomponent().application().resourceManager().bytesForResourceNamed("AvenantTranchesAnnuelles.plist", null, NSArray.EmptyArray));
			dicoTranchesAnnuelles = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
		}
		return dicoTranchesAnnuelles;
	}
	
	/**
	 * @return the dgTranchesAnnuelles
	 */
	public WODisplayGroup dgTranchesAnnuelles() {
		if (dgTranchesAnnuelles==null || refreshTranches) {
			refreshTranches = false;
			if (dgTranchesAnnuelles == null) {
				dgTranchesAnnuelles = new WODisplayGroup();
				dgTranchesAnnuelles.setSelectsFirstObjectAfterFetch(true);
			}
			Avenant avenant = wocomponent().avenant();
			if (avenant != null) {
				Contrat contrat = avenant.contrat();
		        EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(Tranche.class), editingContext());
		        ds.setArray(contrat.tranches());
		        NSArray<EOSortOrdering> sortOrderingExercice = Tranche.EXERCICE_COCKTAIL.dot(EOExerciceCocktail.EXE_EXERCICE_KEY).ascs();
		        dgTranchesAnnuelles.setSortOrderings(sortOrderingExercice);
		        dgTranchesAnnuelles.setDataSource(ds);
		        dgTranchesAnnuelles.fetch();
			}
//		} else {
//			Avenant avenant = ((AvenantVoletFinancier)wocomponent()).avenant();
//	        ((EOArrayDataSource)(dgTranchesAnnuelles.dataSource())).setArray(avenant.contrat().tranches());
//	        dgTranchesAnnuelles.setSelectsFirstObjectAfterFetch(true);
//	        dgTranchesAnnuelles.fetch();
		}
		return dgTranchesAnnuelles;
	}

	/**
	 * @param dgTranchesAnnuelles the dgTranchesAnnuelles to set
	 */
	public void setDgTranchesAnnuelles(WODisplayGroup dgTranchesAnnuelles) {
		this.dgTranchesAnnuelles = dgTranchesAnnuelles;
	}

	/**
	 * @return the uneTrancheAnnuelle
	 */
	public Tranche uneTrancheAnnuelle() {
		return uneTrancheAnnuelle;
	}

	/**
	 * @param uneTrancheAnnuelle the uneTrancheAnnuelle to set
	 */
	public void setUneTrancheAnnuelle(Tranche uneTrancheAnnuelle) {
		this.uneTrancheAnnuelle = uneTrancheAnnuelle;
	}

	/**
	 * @return the lesNatures
	 */
	public NSArray lesNatures() {
		return lesNatures;
	}

	/**
	 * @param lesNatures the lesNatures to set
	 */
	public void setLesNatures(NSArray lesNatures) {
		this.lesNatures = lesNatures;
	}

	/**
	 * @return the uneNature
	 */
	public String uneNature() {
		return uneNature;
	}

	/**
	 * @param uneNature the uneNature to set
	 */
	public void setUneNature(String uneNature) {
		this.uneNature = uneNature;
	}

	
	/**
	 * @return the dgRepartPartenaireTranches
	 */
	public WODisplayGroup dgRepartPartenaireTranches() {
        String orderKeyPath = EORepartPartenaireTranche.CONTRAT_PARTENAIRE_KEY + "." + EOContratPartenaire.CP_PRINCIPAL_KEY; 
		if (dgRepartPartenaireTranches==null || refreshRepartPartenaireTranches) {
			refreshRepartPartenaireTranches = false;
			dgRepartPartenaireTranches = new WODisplayGroup();
			Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
			if (tranche != null) {
		        EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(RepartPartenaireTranche.class), editingContext());
		        ds.setArray(tranche.toRepartPartenaireTranches(null, ERXS.descs(orderKeyPath), false));
//		        EOSortOrdering sortOrderingExercice = new EOSortOrdering(Tranche.EXERCICE_COCKTAIL_KEY,EOSortOrdering.CompareAscending); 
//		        NSArray orderings = new NSArray(sortOrderingExercice);
//		        dgRepartPartenaireTranches.setSortOrderings(orderings);
		        dgRepartPartenaireTranches.setDataSource(ds);
		        dgRepartPartenaireTranches.setSelectsFirstObjectAfterFetch(false);
		        dgRepartPartenaireTranches.fetch();
			}
		} else {
			Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
	        ((EOArrayDataSource)(dgRepartPartenaireTranches.dataSource())).setArray(tranche.toRepartPartenaireTranches(null, ERXS.descs(orderKeyPath), false));
	        dgRepartPartenaireTranches.setSelectsFirstObjectAfterFetch(true);
	        dgRepartPartenaireTranches.fetch();
		}
		return dgRepartPartenaireTranches;
	}

	/**
	 * @param dgRepartPartenaireTranches the dgRepartPartenaireTranches to set
	 */
	public void setDgRepartPartenaireTranches(
			WODisplayGroup dgRepartPartenaireTranches) {
		this.dgRepartPartenaireTranches = dgRepartPartenaireTranches;
	}

	/**
	 * @return the dicoRepartPartenaireTranches
	 */
	public NSMutableDictionary dicoRepartPartenaireTranches() {
		if (dicoRepartPartenaireTranches == null) {
			NSData data = new NSData(wocomponent().application().resourceManager().bytesForResourceNamed("ConventionRepartPartenaireTranches.plist", null, NSArray.EmptyArray));
			dicoRepartPartenaireTranches = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
		}
		return dicoRepartPartenaireTranches;
	}

	/**
	 * @return the uneRepartPartenaireTranche
	 */
	public RepartPartenaireTranche getUneRepartPartenaireTranche() {
		return uneRepartPartenaireTranche;
	}

	/**
	 * @param uneRepartPartenaireTranche the uneRepartPartenaireTranche to set
	 */
	public void setUneRepartPartenaireTranche(
			RepartPartenaireTranche uneRepartPartenaireTranche) {
		this.uneRepartPartenaireTranche = uneRepartPartenaireTranche;
	}

	public void refreshRepartPartenaireTranches(NSNotification notification) {
		if (notification.userInfo() != null && notification.userInfo().containsKey("edc")) {
			EOEditingContext ed = (EOEditingContext)notification.userInfo().objectForKey("edc");
			if (ed.equals(editingContext())) {
				refreshRepartPartenaireTranches = true;
			}
		} else {
			refreshRepartPartenaireTranches = true;
		}
	}

	/**
	 * @return the dgRecettes
	 */
	public WODisplayGroup dgRecettes() {
		if (dgRecettes==null || refreshRecettes) {
			refreshRecettes = false;
			if (dgRecettes == null) {
				dgRecettes = new WODisplayGroup();
		        dgRecettes.setSelectsFirstObjectAfterFetch(true);
			}
			Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
			if (tranche != null) {
		        EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(SbRecette.class), editingContext());
		        ds.setArray(tranche.sbRecettes());
		        EOSortOrdering sortOrderingDatePrev = new EOSortOrdering(SbRecette.SR_DATE_PREV_KEY,EOSortOrdering.CompareAscending); 
		        EOSortOrdering sortOrderingMontantHT = new EOSortOrdering(SbRecette.SR_MONTANT_HT_KEY,EOSortOrdering.CompareAscending); 
		        NSArray orderings = new NSArray(new EOSortOrdering[]{sortOrderingDatePrev, sortOrderingMontantHT});
		        dgRecettes.setSortOrderings(orderings);
		        dgRecettes.setDataSource(ds);
		        dgRecettes.updateDisplayedObjects();
		        dgRecettes.fetch();
			}
		}
		return dgRecettes;
	}

	/**
	 * @param dgRecettes the dgRecettes to set
	 */
	public void setDgRecettes(WODisplayGroup dgRecettes) {
		this.dgRecettes = dgRecettes;
	}

	public void refreshRecettes(NSNotification notification) {
	    refreshRecettes = true;
	    if (notification.object() != null) {
	        dgRecettes().setSelectedObject(notification.object());
	    }
	}
	
	/**
	 * @return the dicoRecettes
	 */
	public NSMutableDictionary dicoRecettesNature() {
		if (dicoRecettesNature == null) {
			NSData data = new NSData(wocomponent().application().resourceManager().bytesForResourceNamed("ConventionTrancheSbRecettesNature.plist", null, NSArray.EmptyArray));
			dicoRecettesNature = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
		}
		return dicoRecettesNature;
	}
	
	/**
	 * @return the dicoRecettes
	 */
	public NSMutableDictionary dicoRecettesDestination() {
	    if (dicoRecettesDestination == null) {
	        NSData data = new NSData(wocomponent().application().resourceManager().bytesForResourceNamed("ConventionTrancheSbRecettesDestination.plist", null, NSArray.EmptyArray));
	        dicoRecettesDestination = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
	    }
	    return dicoRecettesDestination;
	}

	/**
	 * @return the uneRecette
	 */
	public SbRecette getUneRecette() {
		return uneRecette;
	}

	/**
	 * @param uneRecette the uneRecette to set
	 */
	public void setUneRecette(SbRecette uneRecette) {
		this.uneRecette = uneRecette;
	}

	public GestionRecettes ajouterUneRecette() {
		GestionRecettes nextPage = (GestionRecettes)wocomponent().pageWithName(GestionRecettes.class.getName());
		SbRecette nouvelleRecette = null;
		try {
		    // On revert l'ec d'édition
		    getEditingContextForEdition().revert();
			nouvelleRecette = SbRecette.instanciate(getEditingContextForEdition());
            getEditingContextForEdition().insertObject(nouvelleRecette);
			Tranche tranche = ERXEOControlUtilities.localInstanceOfObject(getEditingContextForEdition(), 
			        (Tranche)dgTranchesAnnuelles().selectedObject());
			if (tranche.exerciceCocktail() != null)
			    nouvelleRecette.setExerciceCocktailRelationship(tranche.exerciceCocktail().localInstanceIn(getEditingContextForEdition()));
	        tranche.addToSbRecettesRelationship(nouvelleRecette);
	        nouvelleRecette.setDefaultValues();
			nextPage.setTranche(tranche);
			nextPage.setLaRecette(nouvelleRecette);
		} catch (Exception e) {
			((Session)wocomponent().session()).setMessageErreur(e.getMessage());
			wocomponent().context().response().setStatus(500);
			nextPage = null;
		}
		return nextPage;
	}
	
	public GestionRecettes modifierUneRecette() {
	    // On revert l'ec d'édition
	    getEditingContextForEdition().revert();
	    GestionRecettes nextPage = (GestionRecettes)wocomponent().pageWithName(GestionRecettes.class.getName());
	    nextPage.setLaRecette(ERXEOControlUtilities.localInstanceOfObject(getEditingContextForEdition(), (SbRecette)dgRecettes().selectedObject()));
//	    try {
//	        if (selectedRecette().translateOrgan())
//	            wocomponent().session().addSimpleInfoMessage("Cocolight", "La recette a été faite sur une ligne budgétaire d'un exercice antérieur." +
//	                            "Après enregistrement de la modification, la recette sera faite sur la ligne budgétaire identique mais de l'exercice " + 
//	                            selectedTranche().exerciceCocktail().exeExercice());
//	    } catch (ValidationException e) {
//	        wocomponent().session().addSimpleErrorMessage("Cocolight", e.getMessage());
//	    }
	    return nextPage;
	}
	
	public WOActionResults supprimerUneRecette() {
	    try {
	        SbRecette recette = (SbRecette)dgRecettes().selectedObject();
	        Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
	        recette.checkMontantAvecCreditsPositionnes(recette.montantHt());
	        tranche.removeFromSbRecettesRelationship(recette);
	        editingContext().deleteObject(recette);
	        refreshRecettes = true;
	        NSMutableDictionary userInfo = new NSMutableDictionary();
	        userInfo.setObjectForKey(recette.editingContext(), "edc");
	        NSNotificationCenter.defaultCenter().postNotification("refreshRecettesNotification", recette, userInfo);
	    }
	    catch (ValidationException e) {
	        wocomponent().session().addSimpleErrorMessage("Cocolight", e.getMessage());
	    }
	    return null;
	}

	/**
	 * @return the dgDepenses
	 */
	public WODisplayGroup dgDepenses() {
		if (dgDepenses==null || refreshDepenses) {
			refreshDepenses = false;
			if (dgDepenses == null) {
				dgDepenses = new WODisplayGroup();
		        dgDepenses.setSelectsFirstObjectAfterFetch(true);
			}
			Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
			if (tranche != null) {
		        EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(SbDepense.class), editingContext());
		        ds.setArray(tranche.sbDepenses());
		        EOSortOrdering sortOrderingDatePrev = new EOSortOrdering(SbDepense.SD_DATE_PREV_KEY,EOSortOrdering.CompareAscending); 
		        EOSortOrdering sortOrderingMontantHT = new EOSortOrdering(SbDepense.SD_MONTANT_HT_KEY,EOSortOrdering.CompareAscending); 
		        NSArray orderings = new NSArray(new EOSortOrdering[]{sortOrderingDatePrev, sortOrderingMontantHT});
		        dgDepenses.setSortOrderings(orderings);
		        dgDepenses.setDataSource(ds);
		        dgDepenses.updateDisplayedObjects();
		        dgDepenses.fetch();
			}
		}
		return dgDepenses;
	}

	/**
	 * @param dgDepenses the dgDepenses to set
	 */
	public void setDgDepenses(WODisplayGroup dgDepenses) {
		this.dgDepenses = dgDepenses;
	}

	public void refreshDepenses(NSNotification notification) {
	    refreshDepenses = true;
	    if (notification.object() != null) {
	        dgDepenses().setSelectedObject(notification.object());
	    }
	}
	
	/**
	 * @return the dicoDepenses
	 */
	public NSMutableDictionary dicoDepensesDestination() {
		if (dicoDepensesDestination == null) {
			NSData data = new NSData(wocomponent().application().resourceManager().bytesForResourceNamed("ConventionTrancheSbDepensesDestination.plist", null, NSArray.EmptyArray));
			dicoDepensesDestination = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
		}
		return dicoDepensesDestination;
	}
	
	/**
	 * @return the dicoDepenses
	 */
	public NSMutableDictionary dicoDepensesNature() {
	    if (dicoDepensesNature == null) {
	        NSData data = new NSData(wocomponent().application().resourceManager().bytesForResourceNamed("ConventionTrancheSbDepensesNature.plist", null, NSArray.EmptyArray));
	        dicoDepensesNature = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
	    }
	    return dicoDepensesNature;
	}

	/**
	 * @return the uneDepense
	 */
	public SbDepense getUneDepense() {
		return uneDepense;
	}

	/**
	 * @param uneDepense the uneDepense to set
	 */
	public void setUneDepense(SbDepense uneDepense) {
		this.uneDepense = uneDepense;
	}

	public GestionDepenses ajouterUneDepense() {
	    GestionDepenses nextPage = (GestionDepenses)wocomponent().pageWithName(GestionDepenses.class.getName());
	    SbDepense nouvelleDepense = null;
	    // Revet du contexte d'edition
	    getEditingContextForEdition().revert();
	    nouvelleDepense = SbDepense.create(getEditingContextForEdition(), null, null, null, null, null);
	    getEditingContextForEdition().insertObject(nouvelleDepense);
	    Tranche tranche = ERXEOControlUtilities.localInstanceOfObject(getEditingContextForEdition(), 
	                    (Tranche)dgTranchesAnnuelles().selectedObject());
	    nouvelleDepense.setExerciceCocktailRelationship(tranche.exerciceCocktail());
	    tranche.addToSbDepensesRelationship(nouvelleDepense);
	    nouvelleDepense.setDefaultValues();
	    nextPage.setTranche(tranche);
	    nextPage.setLaDepense(nouvelleDepense);
	    return nextPage;
	}
	
	public GestionDepenses modifierUneDepense() {
        getEditingContextForEdition().revert();
		GestionDepenses nextPage = (GestionDepenses)wocomponent().pageWithName(GestionDepenses.class.getName());
		nextPage.setLaDepense(selectedDepense().localInstanceIn(getEditingContextForEdition()));
//		try {
//		    if (selectedDepense().translateOrgan())
//		        wocomponent().session().addSimpleInfoMessage("Cocolight", "La dépense a été faite sur une ligne budgétaire d'un exercice antérieur." +
//		        		"Après enregistrement de la modification, la depense sera faite sur la ligne budgétaire identique mais de l'exercice " + 
//		                selectedTranche().exerciceCocktail().exeExercice());
//		} catch (ValidationException e) {
//		    wocomponent().session().addSimpleErrorMessage("Cocolight", e.getMessage());
//		}
		return nextPage;
	}
	
	public WOActionResults supprimerUneDepense() {
        try {
            SbDepense depense = (SbDepense)dgDepenses().selectedObject();
            Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
            depense.checkMontantAvecCreditsPositionnes(depense.montantHt());
            tranche.removeFromSbDepensesRelationship(depense);
            editingContext().deleteObject(depense);
            refreshDepenses = true;
            NSMutableDictionary userInfo = new NSMutableDictionary();
            userInfo.setObjectForKey(depense.editingContext(), "edc");
            NSNotificationCenter.defaultCenter().postNotification("refreshDepensesNotification", depense, userInfo);
        }
        catch (ValidationException e) {
            wocomponent().session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
        return null;
	}
	
	public EOEditingContext getEditingContextForEdition() {
	    if (editingContextForEdition == null) {
	        editingContextForEdition = ERXEC.newEditingContext(editingContext());
	        editingContextForEdition.setRetainsRegisteredObjects(true);
	    }
        return editingContextForEdition;
    }

	public Tranche selectedTranche() {
	    return (Tranche)dgTranchesAnnuelles().selectedObject();
	}
	
	public SbDepense selectedDepense() {
	    return (SbDepense)dgDepenses().selectedObject();
	}
	
	public SbRecette selectedRecette() {
	    return (SbRecette)dgRecettes().selectedObject();
	}
	
	/**
	 * @return true si pas les droits de modif ou la tranche a une prevision budgetaire validée ou en attente de validation.
	 */
    public boolean nePeutPasEditerTranche() {
	    return !((Session)wocomponent().session()).applicationUser().hasDroitModificationTranches();
	}
	
    public boolean nePeutPasEditerCurrentTranche() {
        return !((Session)wocomponent().session()).applicationUser().hasDroitModificationTranches();
    }
    
    public boolean nePeutPasEditerDepensesOuRecettes() {
        return nePeutPasEditerTranche();
    }
    
    public boolean nePeutPasAjouterDepensesNature() {
        Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
        return nePeutPasEditerDepensesOuRecettes() || tranche.exercice() == null || (tranche != null && tranche.isMontantTrancheLtOrEqSommeDepensesNature());
    }
    
    public boolean nePeutPasAjouterDepensesDestination() {
        Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
        return nePeutPasEditerDepensesOuRecettes() || tranche.exercice() == null || (tranche != null && tranche.isMontantTrancheLtOrEqSommeDepensesDest());
    }
    
    public boolean nePeutPasAjouterRecetteNature() {
        Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
        return nePeutPasEditerDepensesOuRecettes() || tranche.exercice() == null || (tranche != null && tranche.isMontantTrancheLtOrEqSommeRecettesNature()) ;
    }
    
    public boolean nePeutPasAjouterRecetteDestination() {
        Tranche tranche = (Tranche)dgTranchesAnnuelles().selectedObject();
        return nePeutPasEditerDepensesOuRecettes() || tranche.exercice() == null || (tranche != null && tranche.isMontantTrancheLtOrEqSommeRecettesDest()) ;
    }
      
    public EOExerciceCocktail getCurrentExercice() {
        return currentExercice;
    }
    
    public void setCurrentExercice(EOExerciceCocktail currentExercice) {
        this.currentExercice = currentExercice;
    }
    
    public EOExerciceCocktail getSelectedExercice() {
        return selectedExercice;
    }
    
    public void setSelectedExercice(EOExerciceCocktail selectedExercice) {
        this.selectedExercice = selectedExercice;
    }
    
}
