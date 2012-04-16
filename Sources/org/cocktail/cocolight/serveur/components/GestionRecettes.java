package org.cocktail.cocolight.serveur.components;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ContratPartenaire;
import org.cocktail.cocowork.server.metier.convention.EORepartPartenaireTranche;
import org.cocktail.cocowork.server.metier.convention.IRepartMontant;
import org.cocktail.cocowork.server.metier.convention.RepartSbRecetteCodeAnalytique;
import org.cocktail.cocowork.server.metier.convention.RepartSbRecettePlanComptable;
import org.cocktail.cocowork.server.metier.convention.SbRecette;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlbibasse.serveur.finder.FinderBudgetMasqueGestion;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOTypeAction;
import org.cocktail.fwkcktlcompta.common.metier.EOPlanComptable;
import org.cocktail.fwkcktlcompta.common.metier.EOPlanComptableExer;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOLolfNomenclatureRecette;
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

public class GestionRecettes extends MyWOComponent {

	private Tranche tranche;
	private SbRecette laRecette;
	private EOTva unTauxDeTVARecette;
	private String libelleRecette;
    private NSArray<EOOrgan> organs;
    private NSArray<EOOrgan> organsSupplementaires;
    private EOOrgan currentOrgan;
    private NSArray<EOLolfNomenclatureRecette> lolfs;
    private EOLolfNomenclatureRecette currentLolf;
    private NSArray<EOPlanComptableExer> plancos;
    private EOPlanComptableExer currentPlanco;
	private Integer currentContributeurPersId;
	private NSMutableDictionary<Integer, IPersonne> contributeurs;
    private boolean modeNature;
    
    public GestionRecettes(WOContext context) {
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
    public void reset() {
    	tranche = null;
    	laRecette = null;
    	unTauxDeTVARecette = null;
    	super.reset();
    }

    @Override
    public EOEditingContext edc() {
    	if (laRecette() != null) {
    		return laRecette().editingContext();
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
			NSNotificationCenter.defaultCenter().postNotification("refreshRecettesNotification", laRecette(), userInfo);
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
			NSNotificationCenter.defaultCenter().postNotification("refreshRecettesNotification", laRecette(), userInfo);
			SbRecette nouvelleRecette = SbRecette.instanciate(edc());
			edc().insertObject(nouvelleRecette);
			nouvelleRecette.setExerciceCocktailRelationship(tranche.exerciceCocktail());
	        tranche.addToSbRecettesRelationship(nouvelleRecette);
	        nouvelleRecette.setDefaultValues();
			setLaRecette(nouvelleRecette);			
        } catch (ValidationException e) {
            context().response().setStatus(500);
            session().setMessageErreur(e.getMessage());
        } catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
		return result;
	}

	public IRepartMontant creerRepartCodeAnalytique() {
        RepartSbRecetteCodeAnalytique repart = RepartSbRecetteCodeAnalytique.create(tranche().editingContext(), null, null);
        repart.setSbRecetteRelationship(laRecette());
        return repart;
    }
    
    public IRepartMontant creerRepartPlanComptable() {
        RepartSbRecettePlanComptable repart = RepartSbRecettePlanComptable.create(tranche().editingContext(), null, null);
        repart.setSbRecetteRelationship(laRecette());
        return repart;
    }
    
	/**
	 * @return the tranche
	 */
	public Tranche tranche() {
		if (tranche == null && laRecette() != null) {
			tranche = laRecette().tranche();
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
	 * @return the laRecette
	 */
	public SbRecette laRecette() {
		return laRecette;
	}

	/**
	 * @param laRecette the laRecette to set
	 */
	public void setLaRecette(SbRecette laRecette) {
		this.laRecette = laRecette;
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

	public void setMontantTTCDepense(BigDecimal value) {
		if (value != null && laRecette() != null) {
			BigDecimal montantHT = value;
			EOTva tva = laRecette().tva();			
			BigDecimal taux = BigDecimal.ZERO;
			if (tva != null) {
				taux = tva.tvaTaux();
			}
			montantHT = montantHT.divide(new BigDecimal(1.0 + taux.doubleValue() / 100.0),2, RoundingMode.HALF_UP);
			laRecette().setSrMontantHt(montantHT);
		}
	}
	
	public BigDecimal montantTTCRecette() {
		BigDecimal montantTTCRecette = BigDecimal.valueOf(0.00);
		montantTTCRecette.setScale(2, BigDecimal.ROUND_HALF_UP);
		if (laRecette() != null) {
			SbRecette laRecette = laRecette();
			BigDecimal montantHT = laRecette.srMontantHt();
			if (montantHT != null) {
				EOTva tva = laRecette.tva();
				BigDecimal taux = BigDecimal.valueOf(0);
				if (tva != null) {
					taux = tva.tvaTaux();
				}
				BigDecimal coeffTva = new BigDecimal(1.0 + taux.doubleValue() / 100.0);
				montantTTCRecette = montantHT.multiply(coeffTva);
			}
		}
		return montantTTCRecette;
	}

	/**
	 * @return the libelleRecette
	 */
	public String libelleRecette() {
		if (laRecette() != null) {
			libelleRecette = laRecette().srLibelle();
		}
		return libelleRecette;
	}

	/**
	 * @param libelleRecette the libelleRecette to set
	 */
	public void setLibelleRecette(String libelleRecette) {
		if (libelleRecette != null && libelleRecette.length()>50) {
			libelleRecette = libelleRecette.substring(0, 50);
		}
		this.libelleRecette = libelleRecette;
		if (laRecette() != null) {
			laRecette().setSrLibelle(libelleRecette);
		}
		
	}
	public BigDecimal valeurTVARecette() {
		BigDecimal valeurTVARecette = BigDecimal.ZERO;
		if (laRecette() != null && laRecette().srMontantHt() != null) {
			valeurTVARecette = montantTTCRecette().subtract(laRecette().srMontantHt());			
		}
		valeurTVARecette.setScale(2, BigDecimal.ROUND_HALF_UP);
		return valeurTVARecette;
	}

	public NSArray<EOOrgan> organs() {
	    if (organs == null) {
	        EOStructure centreResp = tranche().contrat().centreResponsabilite();
	        EOExercice exo = tranche().exercice();
            Integer utl = applicationUser().getUtilisateur().utlOrdre();
            organs = FinderOrgan.fetchOrgansForStructureAndUtl(
                            edc(), centreResp.cStructure(), exo, utl, 
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
	public NSArray<EOLolfNomenclatureRecette> lolfs() {
	    if (lolfs == null) {
	        NSArray<EOTypeAction> typeActions = 
	            FinderBudgetMasqueGestion.findTypeAction(edc(), getExercice(), laRecette().typeCredit());
	        NSArray<Integer> lolfIds = (NSArray<Integer>)typeActions.valueForKey(EOTypeAction.TYAC_ID_KEY);
	        EOQualifier qual = new ERXInQualifier(EOLolfNomenclatureRecette.LOLF_ID_KEY, lolfIds);
	        lolfs = EOLolfNomenclatureRecette.fetchAll(
	                edc(), qual, new NSArray<EOSortOrdering>(EOLolfNomenclatureRecette.SORT_LOLF_ORDRE_AFFICHAGE_ASC));
	    }
	    return lolfs;
	}
	
	public NSArray<EOPlanComptableExer> plancos() {
	    if (plancos == null) {
	        Tranche tranche = laRecette().tranche();
	        EOExercice exo = tranche.exercice();
	        plancos = EOPlanComptableExer.fetchWithExercice(edc(), exo, EOPlanComptable.PCO_NATURE_RECETTE);
	    } 
	    return plancos;
	}
	
	public EOExercice getExercice() {
	    return tranche().exercice();
	}

	public boolean hasNoOrgan() {
	    return organs().count() == 0;
	}
	
	public EOOrgan getCurrentOrgan() {
        return currentOrgan;
    }
	
	public void setCurrentOrgan(EOOrgan currentOrgan) {
        this.currentOrgan = currentOrgan;
    }

    public EOLolfNomenclatureRecette getCurrentLolf() {
        return currentLolf;
    }
    
    public void setCurrentLolf(EOLolfNomenclatureRecette currentLolf) {
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
                    if (structure.toFournis() != null && structure.toFournis().isTypeClient());
                        contributeurs.setObjectForKey(contrib, contrib.persId());
                } else if (contrib.isIndividu()) {
                    EOIndividu individu = (EOIndividu)contrib;
                    if (individu.toFournis() != null && individu.toFournis().isTypeClient())
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