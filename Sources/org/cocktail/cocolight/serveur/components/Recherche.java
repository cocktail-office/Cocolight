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
package org.cocktail.cocolight.serveur.components;

import java.math.BigDecimal;

import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.TypeClassificationContrat;
import org.cocktail.cocowork.server.metier.convention.finder.core.FinderConvention;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.appserver.ERXRedirect;
import er.extensions.eof.ERXS;

public class Recherche extends MyWOComponent {
	
	private WODisplayGroup dgContrats;
	private NSMutableDictionary dicoContrats;
	private NSDictionary unDicoContrat;
	private NSMutableDictionary bindings;

	private NSArray lesTypesClassification;
	private TypeClassificationContrat unTypeClassification, leTypeClassification;
	
	private NSDictionary<String, Object> currentService;
	private NSArray<NSDictionary<String, Object>> lesServices;
	private NSDictionary<String, Object> currentDiscipline;
	private NSArray<NSDictionary<String, Object>> lesDisciplines;
	private NSDictionary<String, Object> currentTypeContrat;
	private NSArray<NSDictionary<String, Object>> lesTypesContrat;
	private NSDictionary<String, Object> currentModeDeGestion;
	private NSArray<NSDictionary<String, Object>> lesModesDeGestion;
	
	
    public Recherche(WOContext context) {
        super(context);
        rechercher();
        setFiltres();
    }

	public WOActionResults rechercher() {
		NSArray resultats = null;
			// Traitement des bindings
			if (bindings().containsKey("ModeDeGestion")) {
			    NSDictionary<String, Object> leModeDeGestion = (NSDictionary<String, Object>)bindings.objectForKey("ModeDeGestion");
			    Integer mgOrdre = ((BigDecimal) leModeDeGestion.objectForKey("MG_ORDRE")).intValue();
				bindings().setObjectForKey(mgOrdre, "modeGest");
			} else {
				bindings().removeObjectForKey("modeGest");
			}
			if (bindings().containsKey("Discipline")) {
			    NSDictionary<String, Object> laDiscipline = (NSDictionary<String, Object>)bindings.objectForKey("Discipline");
				Integer discOrdre = ((BigDecimal) laDiscipline.objectForKey("DISC_ORDRE")).intValue();
				bindings().setObjectForKey(discOrdre, "discOrdre");
			} else {
				bindings().removeObjectForKey("discOrdre");
			}
			if (bindings().containsKey("TypeContrat")) {
			    NSDictionary<String, Object> leTypeContrat = (NSDictionary<String, Object>)bindings.objectForKey("TypeContrat");
				Integer tyconId = ((BigDecimal)leTypeContrat.objectForKey("TYCON_ID")).intValue();
				bindings().setObjectForKey(tyconId, "conNature");
			} else {
				bindings().removeObjectForKey("conNature");
			}
			if (bindings().containsKey("ServiceGestionnaire")) {
			    NSDictionary<String, Object> service = (NSDictionary<String, Object>)bindings.objectForKey("ServiceGestionnaire");
			    String cStructure = (String)service.objectForKey(EOStructure.C_STRUCTURE_COLKEY);
			    bindings().setObjectForKey(cStructure, "conCr");
			} else {
			    bindings().removeObjectForKey("conCr");
			}
            bindings().setObjectForKey(session().applicationUser().hasDroitSuperAdmin(), "admin");
			resultats = FinderConvention.getRawRowConventionForUser(edc(), bindings());
			dgContrats().setObjectArray(resultats);
		return null;
	}
	
	private void setFiltres() {
	    // On met à jour les filtres
	    lesServices();
	    lesDisciplines();
	    lesTypesContrat();
	    lesModesDeGestion();
	}
	
	public WOActionResults nettoyer() {
		bindings().removeAllObjects();
		bindings().setObjectForKey(session().applicationUser().getUtilisateur().utlOrdre(), "utlOrdre");
		bindings().setObjectForKey("CONV", "codeClassification");
		dgContrats = null;
		return rechercher();
	}

	public WOActionResults consulterContrat() {
		Convention nextPage = null;
		NSDictionary leDicoContrat = (NSDictionary)dgContrats().selectedObject();
		if (leDicoContrat != null) {
			Contrat contrat = (Contrat)EOUtilities.objectWithPrimaryKeyValue(edc(), Contrat.ENTITY_NAME, leDicoContrat.objectForKey("CONORDRE"));
			nextPage = (Convention)pageWithName(Convention.class.getName());
			nextPage.setPageDeRecherche(this);
			nextPage.setConvention(contrat);
			session().setContrat(contrat);
			session().setPageConvention(nextPage);
		}
        ERXRedirect redirect = (ERXRedirect)pageWithName(ERXRedirect.class.getName());
        redirect.setComponent(nextPage);
        return redirect;
	}
	
	public WOActionResults submit() {
		return null;
	}

	/**
	 * @return the bindings
	 */
	public NSMutableDictionary bindings() {
		if (bindings == null) {
			bindings = new NSMutableDictionary();
			bindings.setObjectForKey(session().applicationUser().getUtilisateur().utlOrdre(), "utlOrdre");
			bindings.setObjectForKey("CONV", "codeClassification");
		}
		return bindings;
	}

	/**
	 * @param bindings the bindings to set
	 */
	public void setBindings(NSMutableDictionary bindings) {
		this.bindings = bindings;
	}

	/**
	 * @return the dgContrats
	 */
	public WODisplayGroup dgContrats() {
		if (dgContrats == null) {
			dgContrats = new WODisplayGroup();
		    // EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(NSDictionary.class),edc);
//	        ds.setArray(null);
//	        dgContrats.setDataSource(ds);
            dgContrats().setSortOrderings(ERXS.descs("CONINDEX"));
	        dgContrats.setSelectsFirstObjectAfterFetch(false);
		}
		return dgContrats;
	}

	/**
	 * @param dgContrats the dgContrats to set
	 */
	public void setDgContrats(WODisplayGroup dgContrats) {
		this.dgContrats = dgContrats;
	}
	
	public NSMutableDictionary dicoContrats() {
		if (dicoContrats == null) {
			NSData data = new NSData(application().resourceManager().bytesForResourceNamed("Contrats.plist", null, NSArray.EmptyArray));
			dicoContrats = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
		}
		return dicoContrats;
	}

	/**
	 * @return the unDicoContrat
	 */
	public NSDictionary unDicoContrat() {
		return unDicoContrat;
	}

	/**
	 * @param unDicoContrat the unDicoContrat to set
	 */
	public void setUnDicoContrat(NSDictionary unDicoContrat) {
		this.unDicoContrat = unDicoContrat;
	}

	/**
	 * @return the lesTypesClassification
	 */
	public NSArray lesTypesClassification() {
		lesTypesClassification = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(TypeClassificationContrat.ENTITY_NAME);
//		if (lesTypesClassification == null) {
//			lesTypesClassification = EOUtilities.objectsForEntityNamed(edc, TypeClassificationContrat.ENTITY_NAME);
//		}
		return lesTypesClassification;
	}

	/**
	 * @param lesTypesClassification the lesTypesClassification to set
	 */
	public void setLesTypesClassification(NSArray lesTypesClassification) {
		this.lesTypesClassification = lesTypesClassification;
	}

	/**
	 * @return the unTypeClassification
	 */
	public TypeClassificationContrat unTypeClassification() {
		return unTypeClassification;
	}

	/**
	 * @param unTypeClassification the unTypeClassification to set
	 */
	public void setUnTypeClassification(
			TypeClassificationContrat unTypeClassification) {
		this.unTypeClassification = unTypeClassification;
	}

	/**
	 * @return the leTypeClassification
	 */
	public TypeClassificationContrat leTypeClassification() {
		return leTypeClassification;
	}

	/**
	 * @param leTypeClassification the leTypeClassification to set
	 */
	public void setLeTypeClassification(
			TypeClassificationContrat leTypeClassification) {
		this.leTypeClassification = leTypeClassification;
	}
	/**
	 * @return the lesModesDeGestion
	 */
	public NSArray lesModesDeGestion() {
	    if (lesModesDeGestion == null) {
	        lesModesDeGestion = Contrat.modesGestionAsRawRows(edc(), conOrdres());
	    }
		return lesModesDeGestion;
	}
	/**
	 * @param lesModesDeGestion the lesModesDeGestion to set
	 */
	public void setLesModesDeGestion(NSArray lesModesDeGestion) {
		this.lesModesDeGestion = lesModesDeGestion;
	}

	/**
	 * @return the lesDisciplines
	 */
	public NSArray lesDisciplines() {
	    if (lesDisciplines == null) {
	        lesDisciplines = Contrat.disciplinesAsRawRows(edc(), conOrdres());
	    }
	    return lesDisciplines;
	}
	/**
	 * @param lesDisciplines the lesDisciplines to set
	 */
	public void setLesDisciplines(NSArray lesDisciplines) {
		this.lesDisciplines = lesDisciplines;
	}

	public boolean isAfficherDetailDisabled() {
		return !isAfficherDetailEnabled();
	}
	public boolean isAfficherDetailEnabled() {
		boolean isAfficherDetailEnabled = dgContrats != null && dgContrats.selectedObject() != null ? true:false;
		return isAfficherDetailEnabled;
	}

	public WOActionResults accueil() {
		Accueil nextPage = (Accueil)pageWithName(Accueil.class.getName());
		session().reset();
		return nextPage;
	}

	private NSArray<Integer> conOrdres() {
        NSArray<Integer> conOrdres = (NSArray<Integer>) dgContrats().allObjects().valueForKey("CONORDRE");
        return conOrdres;
	}
	
	public NSArray<NSDictionary<String, Object>> lesTypesContrat() {
	    if (lesTypesContrat == null) {
	        // On va chercher les services correspondants à toutes les conventions
	        lesTypesContrat = Contrat.typesContratAsRawRows(edc(), conOrdres());
	    }
	    return lesTypesContrat;
	}
	
	public NSArray<NSDictionary<String, Object>> lesServices() {
	    if (lesServices == null) {
	        // On va chercher les services correspondants à toutes les conventions
	        lesServices = Contrat.centresResponsabiliteAsRawRows(edc(), conOrdres());
	    }
	    return lesServices;
	}
	
	public String labelForCurrentService() {
	    return (String)getCurrentService().objectForKey(EOStructure.LL_STRUCTURE_COLKEY);
	}
	
	public NSDictionary<String, Object> getCurrentService() {
        return currentService;
    }
	
	public void setCurrentService(NSDictionary<String, Object> currentService) {
        this.currentService = currentService;
    }

	public String labelForCurrentDiscipline() {
	    return (String)getCurrentDiscipline().objectForKey("DISC_LIBELLE_LONG");
	}

	public NSDictionary<String, Object> getCurrentDiscipline() {
        return currentDiscipline;
    }
	
	public void setCurrentDiscipline(NSDictionary<String, Object> currentDiscipline) {
        this.currentDiscipline = currentDiscipline;
    }

	public String labelForCurrentTypeContrat() {
	    return (String)getCurrentTypeContrat().objectForKey("TYCON_LIBELLE");
	}
	
	public NSDictionary<String, Object> getCurrentTypeContrat() {
        return currentTypeContrat;
    }
	
	public void setCurrentTypeContrat(NSDictionary<String, Object> currentTypeContrat) {
        this.currentTypeContrat = currentTypeContrat;
    }
	
	public String labelForCurrentModeDeGestion() {
	    return (String)getCurrentModeDeGestion().objectForKey("MG_LIBELLE");
	}
	
	public NSDictionary<String, Object> getCurrentModeDeGestion() {
        return currentModeDeGestion;
    }
	
	public void setCurrentModeDeGestion(NSDictionary<String, Object> currentModeDeGestion) {
        this.currentModeDeGestion = currentModeDeGestion;
    }
	
	public String cssClassForCurrentContrat() {
	    String css = "";
	    Object date = unDicoContrat.objectForKey("DATEVALID");
	    if (date != null && !date.equals(NSKeyValueCoding.NullValue)) {
	        css = "valid";
	    }
	    return css;
	}
	
}
