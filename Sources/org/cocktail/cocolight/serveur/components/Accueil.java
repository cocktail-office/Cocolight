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

import org.cocktail.cocowork.common.exception.ExceptionFinder;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ModeGestion;
import org.cocktail.cocowork.server.metier.convention.TypeClassificationContrat;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryConvention;
import org.cocktail.cocowork.server.metier.convention.finder.core.FinderConvention;
import org.cocktail.cocowork.server.metier.gfc.finder.core.FinderExerciceCocktail;
import org.cocktail.fwkcktlpersonne.common.PersonneApplicationUser;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXRedirect;
import er.extensions.eof.ERXS;

public class Accueil extends MyWOComponent {

    private NSArray lesTypesClassification;
    private TypeClassificationContrat unTypeClassification, leTypeClassification;
    private Integer exerciceConvention;
    private Integer numeroConvention;
    private Boolean isOpenFenetreException = Boolean.FALSE;
    private WODisplayGroup dgContrats;
    private NSDictionary<String, Object> currentContrat;

    public Accueil(WOContext context) {
        super(context);
    }

    private GestionConvention creerConvention(boolean isRA) {
        GestionConvention nextPage = (GestionConvention)pageWithName(GestionConvention.class.getName());
        EOEditingContext ec = context().session().defaultEditingContext();
        FactoryConvention fc = new FactoryConvention(ec,application().isModeDebug());
        Contrat contrat;
        try {
            // Ajout en tant que partenaire interne principal de l'etablissement du createur
            PersonneApplicationUser persAppUser = new PersonneApplicationUser(ec,applicationUser().getPersId());
            NSArray services = persAppUser.getServices();
            if (services!=null) {
                NSArray etablissements = persAppUser.getEtablissementsAffectation();
                if (etablissements != null && etablissements.count()==1) {
                    contrat = fc.creerConventionVierge(applicationUser().getUtilisateur(), (EOStructure)etablissements.lastObject(), (EOStructure)services.lastObject());
                    if (isRA) {
                        NSArray lesModesDeGestion = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(ModeGestion.ENTITY_NAME);
                        EOKeyValueQualifier qual = new EOKeyValueQualifier(ModeGestion.MG_LIBELLE_COURT_KEY, EOQualifier.QualifierOperatorEqual, "RA");
                        ModeGestion modeRA = (ModeGestion)EOQualifier.filteredArrayWithQualifier(lesModesDeGestion,qual).lastObject();
                        contrat.avenantZero().setModeGestionRelationship(modeRA);
                    }
                } else {
                    throw new IllegalStateException("Impossible de détecter votre établissement d'affectation");                
                }
                session().setContrat(contrat);
                nextPage.setContrat(contrat);
            } else {
                contrat = fc.creerConventionVierge(session().applicationUser().getUtilisateur());
                session().setContrat(contrat);
                nextPage.setContrat(contrat);
            }
        } catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        return nextPage;
    }

    public WOActionResults creerUneConvention() {
        return creerConvention(false);
    }

    public WOActionResults creerUneConventionRA() {
        return creerConvention(true);
    }

    public WOActionResults creerUnContrat() {
        return null;
    }
    
    public WOActionResults rechercherUneConvention() {
        Convention nextPage = (Convention)pageWithName(Convention.class.getName());
        if (numeroConvention() != null) {			
            FinderConvention fc = new FinderConvention(session().defaultEditingContext());
            fc.setTypeClassificationContrat(leTypeClassification());
            fc.setExerciceCreation(exerciceConvention());
            fc.setContratIndex(numeroConvention());
            NSArray result;
            Contrat convention=null;
            try {
                result = fc.find();
                convention = (Contrat)result.lastObject();
            } catch (ExceptionFinder e) {
                session().setMessageErreur(e.getMessage());
                e.printStackTrace();
            }

            if (convention != null && convention.isConsultablePar(session().applicationUser().getUtilisateur())) {
                nextPage.setConvention(convention);
                session().setContrat(convention);
                session().setPageConvention(nextPage);
            } else {
                if (convention == null) {
                    session().setMessageErreur("Aucune convention trouvée");
                } else {
                    session().setMessageErreur("Vous n'ètes pas autorisé(e) à consulter cette convention");
                }
                nextPage = null;
            }
        } else {
            session().setMessageErreur("Vous devez saisir un numéro");
            nextPage = null;
        }
        return nextPage;
    }

    public WOActionResults submit() {
        return null;
    }

    public WOComponent retourAccueil() {
        Accueil accueil = (Accueil)session().getSavedPageWithName(Accueil.class.getName());
        session().reset();
        //accueil.setOnloadJS(null);
        accueil.setIsOpenFenetreException(Boolean.FALSE);
        return accueil;
    }

    /**
     * @return the exerciceConvention
     */
    public Integer exerciceConvention() {
        if (exerciceConvention == null) {
            try {
                exerciceConvention = Integer.valueOf(new FinderExerciceCocktail(edc()).findExerciceCocktailCourant().exeExercice().intValue());
            } catch (ExceptionFinder e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return exerciceConvention;
    }
    public void setExerciceConvention(Integer exerciceConvention) {
        this.exerciceConvention = exerciceConvention;
    }
    /**
     * @return the numeroConvention
     */
    public Integer numeroConvention() {
        return numeroConvention;
    }

    /**
     * @param numeroConvention the numeroConvention to set
     */
    public void setNumeroConvention(Integer numeroConvention) {
        this.numeroConvention = numeroConvention;
    }

    public WOActionResults afficherRechercheAvancee() {
        Recherche nextPage = (Recherche)pageWithName(Recherche.class.getName());
        nextPage.setBindings(null);
        return nextPage;
    }

    public WODisplayGroup dgContrats() {
        if (dgContrats == null) {
            dgContrats = new WODisplayGroup();
            dgContrats.setSelectsFirstObjectAfterFetch(false);
            NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
            bindings.setObjectForKey(session().applicationUser().getUtilisateur().utlOrdre(), "utlOrdre");
            bindings.setObjectForKey("CONV", "codeClassification");
            bindings.setObjectForKey(session().applicationUser().hasDroitSuperAdmin(), "admin");
            NSArray<NSDictionary<Object, Object>> resultats = FinderConvention.getRawRowConventionForUser(edc(), bindings);
            dgContrats.setSortOrderings(ERXS.descs("CONORDRE"));
            dgContrats.setObjectArray(resultats);
        }
        return dgContrats;
    }

    public WOActionResults consulterContrat() {
        Convention nextPage = null;
        NSDictionary leDicoContrat = (NSDictionary)dgContrats().selectedObject();
        if (leDicoContrat != null) {
            Contrat contrat = (Contrat)EOUtilities.objectWithPrimaryKeyValue(edc(), Contrat.ENTITY_NAME, leDicoContrat.objectForKey("CONORDRE"));
            nextPage = (Convention)pageWithName(Convention.class.getName());
            nextPage.setConvention(contrat);
            session().setContrat(contrat);
            session().setPageConvention(nextPage);
        }
        ERXRedirect redirect = (ERXRedirect)pageWithName(ERXRedirect.class.getName());
        redirect.setComponent(nextPage);
        return redirect;
    }

    public boolean isAfficherDetailDisabled() {
        return !isAfficherDetailEnabled();
    }
    
    public boolean isAfficherDetailEnabled() {
        boolean isAfficherDetailEnabled = dgContrats != null && dgContrats.selectedObject() != null ? true:false;
        return isAfficherDetailEnabled;
    }
    
    /**
     * @return the lesTypesClassification
     */
    public NSArray lesTypesClassification() {
        if (lesTypesClassification == null) {
            lesTypesClassification = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(TypeClassificationContrat.ENTITY_NAME);
            lesTypesClassification = ERXS.sorted(lesTypesClassification, TypeClassificationContrat.TCC_LIBELLE.asc());
        }
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


    public boolean isCreerUneConventionDisabled() {
        return !applicationUser().hasDroitCreationContratsEtAvenants();
    }


    /**
     * @return the isOpenFenetreException
     */
    public Boolean isOpenFenetreException() {
        return isOpenFenetreException;
    }


    /**
     * @param isOpenFenetreException the isOpenFenetreException to set
     */
    public void setIsOpenFenetreException(Boolean isOpenFenetreException) {
        this.isOpenFenetreException = isOpenFenetreException;
    }


    public boolean isRechercherDisabled() {
        return numeroConvention()==null || exerciceConvention() == null;
    }

    public String cssClassForCurrentContrat() {
        String css = "";
        Object date = currentContrat.objectForKey("DATEVALID");
        if (date != null && !date.equals(NSKeyValueCoding.NullValue)) {
            css = "valid";
        }
        return css;
    }
    
    public NSDictionary<String, Object> getCurrentContrat() {
        return currentContrat;
    }
    
    public void setCurrentContrat(NSDictionary<String, Object> currentContrat) {
        this.currentContrat = currentContrat;
    }

}
