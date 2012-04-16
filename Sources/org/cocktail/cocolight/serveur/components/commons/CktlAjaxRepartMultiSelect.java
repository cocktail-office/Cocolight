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
package org.cocktail.cocolight.serveur.components.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.cocktail.cocolight.serveur.components.MyWOComponent;
import org.cocktail.cocowork.server.metier.convention.IRepartMontant;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXWOContext;

/**
 * 
 * Composant générique (limité à Cocolight) de saisie de repartitions de nomenclatures avec montant.
 * Les objets repart doivent implémenter {@link IRepartMontant}.
 * 
 * @binding formName le formulaire englobant
 * @binding montantTotal le montant total (BigDecimal) à répartir
 * @binding eoPere l'objet (EOEnterpriseObject) possédant les repartitions (ex: SbRecette, SbDepense)
 * @binding newRepart méthode renvoyant une nouvelle repart (IRepartMontant)
 * @binding nomenclatureHelper un objet de type {@link NomenclatureHelper} permettant d'afficher des nomenclatures
 * 
 * @author Alexis TUAL <alexis.tual at cocktail.org>
 *
 */
public class CktlAjaxRepartMultiSelect extends MyWOComponent {
    private static final long serialVersionUID = 6923666370606670409L;
    private static final String BINDING_FORM_NAME = "formName";
    private static final String BINDING_NOMENCLATURE_HELPER = "nomenclatureHelper";

    private static final String BINDING_MONTANT_TOTAL = "montantTotal";
    private static final String BINDING_EO_PERE = "eoPere";
    private static final String BINDING_TO_REPART_KEY = "toRepartKey";
    private static final String BINDING_NEW_REPART = "newRepart";

    private IRepartMontant currentRepart;
    private EOEnterpriseObject currentNomenclature;
    private EOEnterpriseObject selectedNomenclatureForNew;
    private BigDecimal montantHtForNew;
    private String repartMontantPctId;
    private String repartMontantId;
    private String repartPopUpId;
    private String repartId;
    private String containerId;
    private String filterForCurrentRepart;
    private int index;
    private BigDecimal _montantDispo;
    
    public CktlAjaxRepartMultiSelect(WOContext context) {
        super(context);
    }
    
    @Override
    public void awake() {
        _montantDispo = null;
        super.awake();
    }
    
    @Override
    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
        _montantDispo = null;
        super.takeValuesFromRequest(worequest, wocontext);
    }
    
    @Override
    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
        _montantDispo = null;
        return super.invokeAction(worequest, wocontext);
    }
    
    @Override
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        _montantDispo = null;
        super.appendToResponse(woresponse, wocontext);
    }
    
    @Override
    public void sleep() {
        super.sleep();
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public WOActionResults supprimer() {
        eoPere().removeObjectFromBothSidesOfRelationshipWithKey(currentRepart, toRepartKey());
        eoPere().editingContext().deleteObject(currentRepart);
        // forcer le recalcul du montant dispo
        setMontantHtForNew(null);
        return null;
    }
    
    public WOActionResults ajouter() {
        if (getSelectedNomenclatureForNew() != null && getMontantHtForNew() != null) {
            if (getMontantHtForNew().compareTo(BigDecimal.ZERO) == 0) {
                session().addSimpleErrorMessage("Cocolight", "Vous devez saisir un montant supérieur à 0");
                return null;
            }
            if (isMontantTooLarge()) {
                session().addSimpleErrorMessage("Cocolight", "Le montant de cette répartition dépasse le montant de la recette");
                return null;
            }
            if (aRepartWithSelectedNomenclatureExists()) {
                session().addSimpleErrorMessage("Cocolight", "Une répartition existe déjà pour " + 
                        getSelectedNomenclatureForNew().valueForKey(nomenclatureHelper().nomenclatureLabelKey()));
                return null;
            }
            IRepartMontant newRepart = (IRepartMontant)valueForBinding(BINDING_NEW_REPART);
            newRepart.setNomenclature(getSelectedNomenclatureForNew());
            newRepart.setMontantHt(getMontantHtForNew());
            // On remet à null les variables d'ajout
            setSelectedNomenclatureForNew(null);
            setMontantHtForNew(null);
        }
        return null;
    }
    
    private boolean aRepartWithSelectedNomenclatureExists() {
        for (IRepartMontant repart : reparts()) {
            EOEnterpriseObject nomenclature = repart.nomenclature();
            if (nomenclature.equals(getSelectedNomenclatureForNew()))
                return true;
        }
        return false;
    }
    
    private boolean isMontantTooLarge() {
        if (getMontantHtForNew() != null) {
            BigDecimal sommeReparts = (BigDecimal)reparts().valueForKey("@sum.montantHt");
            return getMontantHtForNew().add(sommeReparts).doubleValue() > montantTotal().doubleValue();
        }
        return false;
    }
    
    private BigDecimal montantRestantDisponible() {
        if (_montantDispo == null) {
            if (montantTotal() != null) {
                BigDecimal sommeReparts = (BigDecimal)reparts().valueForKey("@sum.montantHt");
                _montantDispo = montantTotal().subtract(sommeReparts);
            }
        }
        return _montantDispo;
    }
    
    public boolean isAucunMontantRestantDispo() {
        return montantRestantDisponible().doubleValue() <= 0;
    }
    
    public WOActionResults refresh() {
        return null;
    }
    
    public NomenclatureHelper nomenclatureHelper() {
        return (NomenclatureHelper)valueForBinding(BINDING_NOMENCLATURE_HELPER);
    }
    
    public String toRepartKey() {
        return (String)valueForBinding(BINDING_TO_REPART_KEY);
    }
    
    public EOEnterpriseObject eoPere() {
        return (EOEnterpriseObject)valueForBinding(BINDING_EO_PERE);
    }
    
    @SuppressWarnings("unchecked")
    public NSArray<IRepartMontant> reparts() {
        return (NSArray<IRepartMontant>) eoPere().valueForKey(toRepartKey());
    }
    
    public String cssForRow() {
        return getIndex() % 2 == 0 ? "even" : "odd";
    }
    
    public String formName() {
        return (String)valueForBinding(BINDING_FORM_NAME);
    }
    
    public BigDecimal montantTotal() {
        return (BigDecimal)valueForBinding(BINDING_MONTANT_TOTAL);
    }
    
    public String nomenclatureLabelForCurrentRepart() {
        return (String)currentRepart.nomenclature().valueForKey(nomenclatureHelper().nomenclatureLabelKey());
    }
    
    public BigDecimal pctForCurrentRepart() {
        if (getMontantHtForNew() != null && montantTotal() != null) {
            if (montantTotal().compareTo(BigDecimal.ZERO) == 0)
                return BigDecimal.ZERO;
            else
                return currentRepart.montantHt().divide(montantTotal(), RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        }
        return null;
    }
    
    public BigDecimal pctForNew() {
        if (getMontantHtForNew() != null && montantTotal() != null) {
            if (montantTotal().compareTo(BigDecimal.ZERO) == 0)
                return BigDecimal.ZERO;
            else
                return getMontantHtForNew().divide(montantTotal(), RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        }
        return null;
    }
    
    public void setPctForNew(BigDecimal value) {
        // Calculer le montant correspondant MontantTotal x value / 100
        if (value != null)
            setMontantHtForNew(montantTotal().multiply(value.divide(BigDecimal.valueOf(100))));
    }
    
    public String refreshContainerJs() {
        return "function (oC){"+containerId() + "Update();}";
    }
    
    public String containerId() {
        if (containerId == null)
            containerId = "Cont_" + ERXWOContext.safeIdentifierName(context(), true);
        return containerId;
    }
    
    public String repartId() {
        if (repartId == null)
            repartId = "Rep_" + ERXWOContext.safeIdentifierName(context(), true);
        return repartId;
    }
    
    public String repartPopUpId() {
        if (repartPopUpId == null)
            repartPopUpId = "RepPop_" + ERXWOContext.safeIdentifierName(context(), true);
        return repartPopUpId;
    }
    
    public String repartMontantId() {
        if (repartMontantId == null)
            repartMontantId = "RepMont_" + ERXWOContext.safeIdentifierName(context(), true);
        return repartMontantId;
    }
    
    public String repartMontantPctId() {
        if (repartMontantPctId == null)
            repartMontantPctId = "RepMontPct_" + ERXWOContext.safeIdentifierName(context(), true);
        return repartMontantPctId;
    }
    
    public void setCurrentRepart(IRepartMontant currentRepart) {
        this.currentRepart = currentRepart;
    }
    
    public IRepartMontant getCurrentRepart() {
        return currentRepart;
    }
    
    public EOEnterpriseObject getCurrentNomenclature() {
        return currentNomenclature;
    }
    
    public void setCurrentNomenclature(EOEnterpriseObject currentNomenclature) {
        this.currentNomenclature = currentNomenclature;
    }
    
    public String getFilterForCurrentRepart() {
        return filterForCurrentRepart;
    }
    
    public void setFilterForCurrentRepart(String filterForCurrentRepart) {
        this.filterForCurrentRepart = filterForCurrentRepart;
    }
    
    public EOEnterpriseObject getSelectedNomenclatureForNew() {
        return selectedNomenclatureForNew;
    }
    
    public void setSelectedNomenclatureForNew(EOEnterpriseObject selectedNomenclatureForNew) {
        this.selectedNomenclatureForNew = selectedNomenclatureForNew;
    }
    
    public BigDecimal getMontantHtForNew() {
        BigDecimal restant = montantRestantDisponible();
        // Pour le montant initial
        if (montantHtForNew == null || (BigDecimal.ZERO.compareTo(restant) != 0 && BigDecimal.ZERO.compareTo(montantHtForNew) == 0)) {
            setMontantHtForNew(restant);
        }
        return montantHtForNew;
    }
    
    public void setMontantHtForNew(BigDecimal montantHtForNew) {
        this.montantHtForNew = montantHtForNew;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public interface NomenclatureHelper<T> {
        
        public String nomenclatureLabelKey();
        public NSArray<T> nomenclatures();
        public T getCurrentNomenclature();
        public void setCurrentNomenclature(T currentNomenclature);
        public String currentNomenclatureCss();
        public String currentNomenclatureLabel();
        public String currentNomenclatureGroup();
        public String currentNomenclatureGroupLabel();
        public String noSelectionString();
        public boolean mustGroup();
        
    }
    
}