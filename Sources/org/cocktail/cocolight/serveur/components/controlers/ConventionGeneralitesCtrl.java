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
package org.cocktail.cocolight.serveur.components.controlers;

import java.math.BigDecimal;

import org.cocktail.cocolight.serveur.Session;
import org.cocktail.cocolight.serveur.components.assistants.modules.ConventionGeneralites;
import org.cocktail.cocowork.common.exception.ExceptionUtilisateur;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ModeGestion;
import org.cocktail.cocowork.server.metier.convention.TypeStat;
import org.cocktail.cocowork.server.metier.grhum.Discipline;
import org.cocktail.fwkcktlaccordsguiajax.components.controlers.CtrlModule;
import org.cocktail.fwkcktldroitsutils.common.util.MyStringCtrl;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;
import org.cocktail.fwkcktlpersonne.common.metier.EODomaineScientifique;
import org.cocktail.fwkcktlpersonne.common.metier.EONaf;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;

import com.webobjects.appserver.WOComponent;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXArrayUtilities;

public class ConventionGeneralitesCtrl extends CtrlModule<ConventionGeneralites> {

	private NSArray lesEtablissements = null;
	private EOStructure unEtablissement;
	private EOStructure etablissementGestionnaire;

	private NSArray lesCentres = null;
	private EOStructure unCentre;
	private EOStructure leCentreGestionnaire;
	
	private EOStructure unCentreGestionnaire;
	private static EOOrQualifier QUAL_SERVICES = new EOOrQualifier(new NSArray(new EOQualifier[]{EOStructure.QUAL_STRUCTURES_AS_SERVICES}));
	private static EOQualifier QUAL_COMPOSANTES = new EOOrQualifier(new NSArray(new EOQualifier[]{EOStructure.QUAL_STRUCTURES_AS_COMPOSANTES}));
	private NSDictionary filtres = null;
	
	private static NSArray TAUX_DE_TVA = new NSArray(new String[]{"0","5.5","19.6"});
	private EOTva unTauxDeTVA;
	private String leTauxDeTVASelectionne;

	private NSArray lesModesDeGestion;
	private ModeGestion unModeDeGestion;
	
    private NSArray lesTypesOptionnels;
	private TypeStat unTypeOptionnel;
	
	private NSArray<EODomaineScientifique> lesDomainesScientifiques;
	private EODomaineScientifique currentDomaineScientifique;
	
	private NSArray lesDisciplines;
	private Discipline uneDiscipline;

	public NSArray lesCodesNafFiltres = null;
	public EONaf unNaf;
	public String filtNaf,lastFiltNaf;
	private EONaf codeNafSelectionne;

    public ConventionGeneralitesCtrl(ConventionGeneralites component) {
        super(component);
    }
	
    public NSArray <EOStructure>lesEtablissements() {
        if (lesEtablissements == null) {
            NSMutableArray<EOStructure> tmp = new NSMutableArray<EOStructure>();
            NSArray<Contrat> contrats = null;
            if (leCentreGestionnaire() != null) {
                contrats = EOUtilities.objectsMatchingKeyAndValue(editingContext(), Contrat.ENTITY_NAME, Contrat.CENTRE_RESPONSABILITE_KEY, leCentreGestionnaire());
            } else {
                contrats = EOUtilities.objectsForEntityNamed(editingContext(), Contrat.ENTITY_NAME);
            }
            contrats = ERXArrayUtilities.arrayWithoutDuplicateKeyValue(contrats, Contrat.ETABLISSEMENT_KEY);
            if (contrats != null && contrats.count()>0) {
                tmp.addObjectsFromArray((NSArray)contrats.valueForKey(Contrat.ETABLISSEMENT_KEY));
            }
            NSArray<EOStructure> etablissements = ((Session)wocomponent().session()).lesEtablissementsAffectation();
            tmp.addObjectsFromArray(ERXEOControlUtilities.localInstancesOfObjects(editingContext(), etablissements));
            if (!tmp.contains(etablissementGestionnaire())) {
                tmp.addObject(etablissementGestionnaire());
            }
            if (tmp != null && tmp.count()>0) {
                ERXArrayUtilities.sortArrayWithKey(tmp, EOStructure.LL_STRUCTURE_KEY);
                lesEtablissements = ERXArrayUtilities.arrayWithoutDuplicates(tmp);
            }
        }
        return lesEtablissements;
    }

	public void setLesEtablissements(NSArray lesEtablissements) {
		this.lesEtablissements = lesEtablissements;
	}

	public EOStructure unEtablissement() {
		return unEtablissement;
	}

	public void setUnEtablissement(EOStructure unEtablissement) {
		this.unEtablissement = unEtablissement;
	}

	public void setEtablissementGestionnaire(EOStructure etablissementGestionnaire) {
		Contrat contrat = wocomponent().contrat();
		if (contrat != null) {
			contrat.setEtablissementRelationship(etablissementGestionnaire);
			lesCentres = null;
		}
	}
	public EOStructure etablissementGestionnaire() {
		Contrat contrat = wocomponent().contrat();
		if (contrat != null) {
			etablissementGestionnaire = contrat.etablissement();
		}
		return etablissementGestionnaire;
	}
	
	public NSArray lesCentres() {
	    if (lesCentres == null) {
	        NSMutableArray<EOStructure> tmp = new NSMutableArray<EOStructure>();
	        NSArray<Contrat> contrats = null;
	        if (wocomponent().contrat().etablissement() != null) {
	            contrats = EOUtilities.objectsMatchingKeyAndValue(editingContext(), Contrat.ENTITY_NAME, Contrat.ETABLISSEMENT_KEY, wocomponent().contrat().etablissement());
	        } else {
	            contrats = EOUtilities.objectsForEntityNamed(editingContext(), Contrat.ENTITY_NAME);
	        }
	        contrats = ERXArrayUtilities.arrayWithoutDuplicateKeyValue(contrats, Contrat.CENTRE_RESPONSABILITE_KEY);
	        if (contrats != null && contrats.count()>0) {
	            tmp.addObjectsFromArray((NSArray<EOStructure>) contrats.valueForKey(Contrat.CENTRE_RESPONSABILITE_KEY));
	        }
	        NSArray<EOStructure> services = ((Session)wocomponent().session()).lesServicesAffectation();
	        tmp.addObjectsFromArray(ERXEOControlUtilities.localInstancesOfObjects(editingContext(), services));
	        EOStructure centreGestionnaire = leCentreGestionnaire();
	        if (centreGestionnaire != null && !tmp.contains(centreGestionnaire)) {
	            tmp.addObject(leCentreGestionnaire());

	        }
	        if (tmp != null && tmp.count()>0) {
	            ERXArrayUtilities.sortArrayWithKey(tmp, EOStructure.LL_STRUCTURE_KEY);
	            lesCentres = ERXArrayUtilities.arrayWithoutDuplicates(tmp);
	        }
	    }
	    return lesCentres;
	}

	public void setLesCentres(NSArray lesCentres) {
		this.lesCentres = lesCentres;
	}

	public EOStructure unCentre() {
		return unCentre;
	}
	public void setUnCentre(EOStructure unCentre) {
		this.unCentre = unCentre;
	}

	public void setLeCentreGestionnaire(EOStructure leCentreGestionnaire) {
		Contrat contrat = wocomponent().contrat();
		if (contrat != null) {
			contrat.setCentreResponsabiliteRelationship(leCentreGestionnaire);
			lesCentres = null;
		}
	}
	
	public EOStructure leCentreGestionnaire() {
		Contrat contrat = wocomponent().contrat();
		if (contrat != null) {
			leCentreGestionnaire = contrat.centreResponsabilite();
			if (leCentreGestionnaire == null) {
				if (contrat.organComposante() != null) {
					leCentreGestionnaire = FinderOrgan.getStructureForOrgan(contrat.organComposante(), EOOrgan.ORG_NIV_2);
					contrat.setCentreResponsabiliteRelationship(leCentreGestionnaire);
				}
			}
		}
		return leCentreGestionnaire;
	}

	/**
	 * @return the lesModesDeGestion
	 */
	public NSArray lesModesDeGestion() {
		lesModesDeGestion = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(ModeGestion.ENTITY_NAME);
//		if (lesModesDeGestion == null) {
//			lesModesDeGestion = FinderModeGestion.find(edc, ModeGestion.ENTITY_NAME, null, null);
//		}
		return lesModesDeGestion;
	}
	/**
	 * @param lesModesDeGestion the lesModesDeGestion to set
	 */
	public void setLesModesDeGestion(NSArray lesModesDeGestion) {
		this.lesModesDeGestion = lesModesDeGestion;
	}
	/**
	 * @return the unModeDeGestion
	 */
	public ModeGestion unModeDeGestion() {
		return unModeDeGestion;
	}
	/**
	 * @param unModeDeGestion the unModeDeGestion to set
	 */
	public void setUnModeDeGestion(ModeGestion unModeDeGestion) {
		this.unModeDeGestion = unModeDeGestion;
	}
	/**
	 * @return the lesTypesOptionnels
	 */
	public NSArray domainesScientifiques() {
	    if (lesDomainesScientifiques == null)
	        lesDomainesScientifiques = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(EODomaineScientifique.ENTITY_NAME);
		return lesDomainesScientifiques;
	}
	
	public NSArray lesTypesOptionnels() {
		lesTypesOptionnels = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(TypeStat.ENTITY_NAME);
		return lesTypesOptionnels;
	}
	
	/**
	 * @param lesTypesOptionnels the lesTypesOptionnels to set
	 */
	public void setLesTypesOptionnels(NSArray lesTypesOptionnels) {
	    this.lesTypesOptionnels = lesTypesOptionnels;
	}
	
	/**
	 * @return the unTypeOptionnel
	 */
	public TypeStat unTypeOptionnel() {
	    return unTypeOptionnel;
	}
	
	/**
	 * @param unTypeOptionnel the unTypeOptionnel to set
	 */
	public void setUnTypeOptionnel(TypeStat unTypeOptionnel) {
	    this.unTypeOptionnel = unTypeOptionnel;
	}

	public String unTypeOptionnelLibelle() {
	    String unTypeOptionnelLibelle = null;

	    if (unTypeOptionnel() != null) {
	        unTypeOptionnelLibelle = unTypeOptionnel().tsLibelle();
	    }

	    return unTypeOptionnelLibelle;
	}
	
	/**
	 * @return the lesDisciplines
	 */
	public NSArray lesDisciplines() {
		lesDisciplines = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(Discipline.ENTITY_NAME);
//		if (lesDisciplines == null) {
//			lesDisciplines = FinderDiscipline.find(edc, Discipline.ENTITY_NAME, null, null);
//		}
		return lesDisciplines;
	}
	/**
	 * @param lesDisciplines the lesDisciplines to set
	 */
	public void setLesDisciplines(NSArray lesDisciplines) {
		this.lesDisciplines = lesDisciplines;
	}
	/**
	 * @return the uneDiscipline
	 */
	public Discipline uneDiscipline() {
		return uneDiscipline;
	}
	/**
	 * @param uneDiscipline the uneDiscipline to set
	 */
	public void setUneDiscipline(Discipline uneDiscipline) {
		this.uneDiscipline = uneDiscipline;
	}

	public String getLlNaf() {
		return (wocomponent().contrat().codeNaf() != null ? wocomponent().contrat().codeNaf().llNaf() : "");
	}

	public NSArray getFilteredNaf() {
		if (filtNaf != null && filtNaf.length() > 0) {
			filtNaf = filtNaf.trim();
			filtNaf = MyStringCtrl.chaineSansAccents(filtNaf, "?");
			EOQualifier qual = EOQualifier.qualifierWithQualifierFormat(EONaf.C_NAF_KEY + " caseInsensitiveLike '" + filtNaf + "*' or " + EONaf.LL_NAF_KEY + " caseInsensitiveLike '*" + filtNaf + "*' ", null);
			if (lesCodesNafFiltres != null && lastFiltNaf != null && filtNaf.startsWith(lastFiltNaf)) {
				lesCodesNafFiltres = EOQualifier.filteredArrayWithQualifier(lesCodesNafFiltres, qual);
				lastFiltNaf = filtNaf;
			} else {
				// lesCodesNafFiltres = EONaf.fetchAll(edc, qual, new NSArray(new Object[] { EONaf.SORT_C_NAF }));
				lesCodesNafFiltres = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(EONaf.ENTITY_NAME);
				lesCodesNafFiltres = EOQualifier.filteredArrayWithQualifier(lesCodesNafFiltres, qual);
				lastFiltNaf = new String(filtNaf);
			}
			if (lesCodesNafFiltres != null && lesCodesNafFiltres.count()==0) {
				if (filtNaf.length() > 6) {
					EOQualifier qual1 = new EOKeyValueQualifier(EONaf.C_NAF_KEY,EOKeyValueQualifier.QualifierOperatorEqual,filtNaf.substring(0, 5));
					lesCodesNafFiltres = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(EONaf.ENTITY_NAME);
					lesCodesNafFiltres = EOQualifier.filteredArrayWithQualifier(lesCodesNafFiltres, qual1);
//					lesCodesNafFiltres = EONaf.fetchAll(edc, qual1, new NSArray(new Object[] { EONaf.SORT_C_NAF }));
				}
			}
		}
		return lesCodesNafFiltres;		
	}	
	
	/**
	 * @return the filtNaf
	 */
	public String getFiltNaf() {
		return filtNaf;
	}
	/**
	 * @param filtNaf the filtNaf to set
	 */
	public void setFiltNaf(String filtNaf) {
		this.filtNaf = filtNaf;
	}
	/**
	 * @return the codeNafSelectionne
	 */
	public EONaf codeNafSelectionne() {
		return wocomponent().contrat().codeNaf();
	}
	/**
	 * @param codeNafSelectionne the codeNafSelectionne to set
	 */
	public void setCodeNafSelectionne(EONaf codeNafSelectionne) {
		this.codeNafSelectionne = codeNafSelectionne;
		wocomponent().contrat().setCodeNafRelationship(codeNafSelectionne);
		if (codeNafSelectionne == null) {
			lastFiltNaf = null;
			lesCodesNafFiltres = null;
		} else {
			setFiltNaf(codeNafSelectionne.cNafEtLib());
		}
	}
	
	public WOComponent recalculerLesDatesEtLaDuree() {
		Contrat contrat = wocomponent().contrat();
		Avenant avenant = wocomponent().avenant();
		if (contrat != null && avenant != null && contrat.avenants().count()==1) {
			try {
				contrat.initDatesEtDuree(avenant.avtDateDeb(), avenant.avtDateFin(), contrat.conDuree());
			} catch (ExceptionUtilisateur e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;		
	}

	public NSArray tauxDeTVA() {
		return TAUX_DE_TVA;
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

	public EODomaineScientifique getCurrentDomaineScientifique() {
        return currentDomaineScientifique;
    }
	
	public void setCurrentDomaineScientifique(EODomaineScientifique currentDomaineScientifique) {
        this.currentDomaineScientifique = currentDomaineScientifique;
    }
	
}
