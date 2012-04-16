package org.cocktail.cocolight.serveur.components;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ContratPartenaire;
import org.cocktail.cocowork.server.metier.convention.EORepartPartenaireTranche;
import org.cocktail.cocowork.server.metier.convention.IRepartMontant;
import org.cocktail.cocowork.server.metier.convention.RepartSbDepenseCodeAnalytique;
import org.cocktail.cocowork.server.metier.convention.RepartSbDepensePlanComptable;
import org.cocktail.cocowork.server.metier.convention.SbDepense;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetMasqueGestion;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOTypeAction;
import org.cocktail.fwkcktlcompta.common.metier.EOPlanComptable;
import org.cocktail.fwkcktlcompta.common.metier.EOPlanComptableExer;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOLolfNomenclatureDepense;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;
import org.cocktail.fwkcktlpersonne.common.metier.EOIndividu;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;
import org.cocktail.fwkcktlpersonne.common.metier.IPersonne;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.ajax.AjaxUtils;
import er.extensions.eof.qualifiers.ERXInQualifier;

public class GestionDepenses extends MyWOComponent {

	private Tranche tranche;
	private SbDepense laDepense;
	private EOTva unTauxDeTVADepense;
	private String libelleDepense;
	private EOOrgan currentOrgan;
	private NSArray<EOOrgan> organs;
    private NSArray<EOOrgan> organsSupplementaires;
	private NSArray<EOLolfNomenclatureDepense> lolfs;
	private NSArray<EOPlanComptableExer> plancos;
	private EOPlanComptableExer currentPlanco;
	private EOLolfNomenclatureDepense currentLolf;
    private Integer currentContributeurPersId;
    private NSMutableDictionary<Integer, IPersonne> contributeurs;
	private boolean modeNature;
    
    public GestionDepenses(WOContext context) {
        super(context);
    }
    
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);

		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/default.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/alert.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "themes/lighting.css");

		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlThemes.framework", "scripts/window.js");
		AjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/cocolight.js");

		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "css/CktlCommon.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "FwkCktlThemes.framework", "css/CktlCommonBleu.css");
		AjaxUtils.addStylesheetResourceInHead(context, response, "app", "styles/cocolight.css");
		
        addScriptResource(response, "jscript/wz_tooltip.js", null, "FwkCktlWebApp.framework", 
                RESOURCE_TYPE_JSCRIPT, RESOURCE_DEST_END_BODY, RESOURCE_FROM_WEB_SERVER_RESOURCES);
	}
	
    @Override
    public EOEditingContext edc() {
    	if (laDepense() != null) {
    		return laDepense().editingContext();
    	}
    	return super.edc();
    }
    
	public WOActionResults annuler() {
	    edc().revert();
		CktlAjaxWindow.close(context());
		return null;
	}
	
	public WOActionResults terminer() {
		WOActionResults result = null;
		try {
		    edc().saveChanges();
		    CktlAjaxWindow.close(context());
			NSMutableDictionary userInfo = new NSMutableDictionary();
			userInfo.setObjectForKey(tranche().editingContext(), "edc");
			NSNotificationCenter.defaultCenter().postNotification("refreshDepensesNotification", laDepense(), userInfo);
		} catch (ValidationException e) {
			context().response().setStatus(500);
			session().setMessageErreur(e.getMessage());
		} catch (Exception e) {
		    throw NSForwardException._runtimeExceptionForThrowable(e);
	    }
		return result;
	}
    
	public WOActionResults terminerEtAjouter() {
		WOActionResults result = null;
		try {
		    edc().saveChanges();
			NSMutableDictionary userInfo = new NSMutableDictionary();
			Tranche tranche = tranche();
			userInfo.setObjectForKey(tranche.editingContext(), "edc");
			NSNotificationCenter.defaultCenter().postNotification("refreshDepensesNotification", laDepense(), userInfo);
			SbDepense nouvelleDepense = SbDepense.instanciate(edc());
			edc().insertObject(nouvelleDepense);
			nouvelleDepense.setExerciceCocktailRelationship(tranche.exerciceCocktail());
	        tranche.addToSbDepensesRelationship(nouvelleDepense);
	        nouvelleDepense.setDefaultValues();
			setLaDepense(nouvelleDepense);			
        } catch (ValidationException e) {
            context().response().setStatus(500);
            session().setMessageErreur(e.getMessage());
        } catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
		return result;
	}
	
	public IRepartMontant creerRepartCodeAnalytique() {
        RepartSbDepenseCodeAnalytique repart = RepartSbDepenseCodeAnalytique.create(tranche().editingContext(), null, null);
        repart.setSbDepenseRelationship(laDepense());
        return repart;
    }
    
    public IRepartMontant creerRepartPlanComptable() {
        RepartSbDepensePlanComptable repart = RepartSbDepensePlanComptable.create(tranche().editingContext(), null, null);
        repart.setSbDepenseRelationship(laDepense());
        return repart;
    }

	/**
	 * @return the tranche
	 */
	public Tranche tranche() {
		if (tranche == null && laDepense() != null) {
			tranche = laDepense().tranche();
		}
		return tranche;
	}

	/**
	 * @param tranche the tranche to set
	 */
	public void setTranche(Tranche tranche) {
		this.tranche = tranche;
	}
	/**
	 * @return the laDepense
	 */
	public SbDepense laDepense() {
		return laDepense;
	}

	/**
	 * @param laDepense the laDepense to set
	 */
	public void setLaDepense(SbDepense laDepense) {
		this.laDepense = laDepense;
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

	public void setMontantTTCDepense(BigDecimal value) {
		if (value != null && laDepense() != null) {
			BigDecimal montantHT = value;
			EOTva tva = laDepense().tva();			
			BigDecimal taux = BigDecimal.ZERO;
			if (tva != null) {
				taux = tva.tvaTaux();
			}
			montantHT = montantHT.divide(new BigDecimal(1.0 + taux.doubleValue() / 100.0),2, RoundingMode.HALF_UP);
			laDepense().setSdMontantHt(montantHT);
		}
	}
	
	public BigDecimal montantTTCDepense() {
		BigDecimal montantTTCDepense = BigDecimal.valueOf(0.00);
		montantTTCDepense.setScale(2, BigDecimal.ROUND_HALF_UP);
		if (laDepense() != null) {
			SbDepense laDepense = laDepense();
			BigDecimal montantHT = laDepense.sdMontantHt();
			if (montantHT != null) {
				EOTva tva = laDepense.tva();
				BigDecimal taux = BigDecimal.valueOf(0);
				if (tva != null) {
					taux = tva.tvaTaux();
				}
				BigDecimal coeffTva = new BigDecimal(1.0 + taux.doubleValue() / 100.0);
				montantTTCDepense = montantHT.multiply(coeffTva);
			}
		}
		return montantTTCDepense;
	}

	/**
	 * @return the libelleDepense
	 */
	public String libelleDepense() {
		if (laDepense() != null) {
			libelleDepense = laDepense().sdLibelle();
		}
		return libelleDepense;
	}

	/**
	 * @param libelleDepense the libelleDepense to set
	 */
	public void setLibelleDepense(String libelleDepense) {
		if (libelleDepense != null && libelleDepense.length()>50) {
			libelleDepense = libelleDepense.substring(0, 50);
		}
		this.libelleDepense = libelleDepense;
		if (laDepense() != null) {
			laDepense().setSdLibelle(libelleDepense);
		}
		
	}

	public BigDecimal valeurTVADepense() {
		BigDecimal valeurTVADepense = BigDecimal.ZERO;
		if (laDepense() != null && laDepense().sdMontantHt() != null) {
			valeurTVADepense = montantTTCDepense().subtract(laDepense().sdMontantHt());			
		}
		valeurTVADepense.setScale(2, BigDecimal.ROUND_HALF_UP);
		return valeurTVADepense;
	}
	
	public NSArray<EOOrgan> organs() {
	    if (organs == null) {
	        EOStructure centreResp = tranche().contrat().centreResponsabilite();
	        Integer utl = applicationUser().getUtilisateur().utlOrdre();
	        organs = FinderOrgan.fetchOrgansForStructureAndUtl(
	                        edc(), centreResp.cStructure(), getExercice(), utl, 
	                        tranche().contrat().niveauOrganMin(), tranche().contrat().isModeRA());
	        NSMutableArray<EOOrgan> organsTmp = organs.mutableClone();
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
    		organsSupplementaires = Contrat.autresOrgansBudget(edc());
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
	
	
	@SuppressWarnings("unchecked")
    public NSArray<EOLolfNomenclatureDepense> lolfs() {
	    if (lolfs == null) {
	        NSArray<EOTypeAction> typeActions = 
	            FinderBudgetMasqueGestion.findTypeAction(edc(), getExercice(), null);
	        NSArray<Integer> lolfIds = (NSArray<Integer>)typeActions.valueForKey(EOTypeAction.TYAC_ID_KEY);
	        EOQualifier qual = new ERXInQualifier(EOLolfNomenclatureDepense.LOLF_ID_KEY, lolfIds);
	        lolfs = EOLolfNomenclatureDepense.fetchAll(
	                edc(), qual, new NSArray<EOSortOrdering>(EOLolfNomenclatureDepense.SORT_LOLF_ORDRE_AFFICHAGE_ASC));
	    }
	    return lolfs;
	}
	
	public NSArray<EOPlanComptableExer> plancos() {
	    if (plancos == null) {
	        Tranche tranche = laDepense().tranche();
            EOExercice exo = tranche.exercice();
            plancos = EOPlanComptableExer.fetchWithExercice(edc(), exo, EOPlanComptable.PCO_NATURE_DEPENSE);
	    } 
	    return plancos;
	}
	
	public EOExercice getExercice() {
	    return tranche().exercice();
	}
	
	public boolean hasNoOrganOrIsModeRA() {
	    return organs().count() == 0 || tranche().contrat().isModeRA();
	}
	
	public EOOrgan getCurrentOrgan() {
        return currentOrgan;
    }
	
	public void setCurrentOrgan(EOOrgan currentOrgan) {
        this.currentOrgan = currentOrgan;
    }
	
	public EOLolfNomenclatureDepense getCurrentLolf() {
        return currentLolf;
    }
	
	public void setCurrentLolf(EOLolfNomenclatureDepense currentLolf) {
        this.currentLolf = currentLolf;
    }
	
    public Integer getCurrentContributeurPersId() {
        return currentContributeurPersId;
    }
    
    public void setCurrentContributeurPersId(Integer currentContributeurPersId) {
        this.currentContributeurPersId = currentContributeurPersId;
    }
	
	public NSDictionary<Integer, IPersonne> getContributeurs() {
        if (contributeurs == null) {
            contributeurs = new NSMutableDictionary<Integer, IPersonne>();
            String keyPath = EORepartPartenaireTranche.CONTRAT_PARTENAIRE_KEY + "." + ContratPartenaire.PARTENAIRE_KEY;
            NSArray<IPersonne> contribs = (NSArray<IPersonne>) tranche().toRepartPartenaireTranches().valueForKeyPath(keyPath);
            // On ne prend que les contributeurs de type fournisseurs...
            for (IPersonne contrib : contribs) {
                if (contrib.isStructure()) {
                    EOStructure structure = (EOStructure)contrib;
                    if (structure.toFournis() != null && structure.toFournis().isTypeFournisseur());
                        contributeurs.setObjectForKey(contrib, contrib.persId());
                } else if (contrib.isIndividu()) {
                    EOIndividu individu = (EOIndividu)contrib;
                    if (individu.toFournis() != null && individu.toFournis().isTypeFournisseur())
                        contributeurs.setObjectForKey(contrib, contrib.persId());
                }
            }
        }
        return contributeurs.immutableClone();
    }
    
    public String contributeurLabel() {
        return getContributeurs().objectForKey(getCurrentContributeurPersId()).persLibelleAffichage();
    }

    public EOPlanComptableExer getCurrentPlanco() {
        return currentPlanco;
    }
    
    public void setCurrentPlanco(EOPlanComptableExer currentPlanco) {
        this.currentPlanco = currentPlanco;
    }

    public boolean isModeNature() {
        return modeNature;
    }
    
    public void setModeNature(boolean modeNature) {
        this.modeNature = modeNature;
    }
    
    public boolean modeDestination() {
        return !isModeNature();
    }
    

}