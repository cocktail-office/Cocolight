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

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * Composant présentant les reparts associés à un objet.
 * 
 * @binding nomenclatureLabelKey la clef (String) correspondant au libellé de la nomenclature
 * @binding montantTotal le montant total (BigDecimal) à répartir
 * @binding eoPere l'objet (EOEnterpriseObject) possédant les repartitions (ex: SbRecette, SbDepense)
 * @binding toRepartKey la clef pour récupérer les reparts à partir de l'eoPere
 * 
 * @see CktlAjaxRepartMultiSelect
 * 
 * @author Alexis TUAL <alexis.tual at cocktail.org>
 *
 */
public class CktlAjaxRepartShow extends MyWOComponent {
    
    private static final long serialVersionUID = 6181088218139344645L;
    private static final String BINDING_EO_PERE = "eoPere";
    private static final String BINDING_TO_REPART_KEY = "toRepartKey";
    private static final String BINDING_MONTANT_TOTAL = "montantTotal";
    private static final String BINDING_NOMENCLATURE_LABEL_KEY = "nomenclatureLabelKey";
    private int index;

    private IRepartMontant currentRepart;
    
    public CktlAjaxRepartShow(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public String toRepartKey() {
        return (String)valueForBinding(BINDING_TO_REPART_KEY);
    }
    
    public EOEnterpriseObject eoPere() {
        return (EOEnterpriseObject)valueForBinding(BINDING_EO_PERE);
    }
    
    public BigDecimal montantTotal() {
        return (BigDecimal)valueForBinding(BINDING_MONTANT_TOTAL);
    }
    
    public String cssForRow() {
        return getIndex() % 2 == 0 ? "even" : "odd";
    }
    
    @SuppressWarnings("unchecked")
    public NSArray<IRepartMontant> reparts() {
        return (NSArray<IRepartMontant>) eoPere().valueForKey(toRepartKey());
    }
    
    public String nomenclatureLabelKey() {
        return (String)valueForBinding(BINDING_NOMENCLATURE_LABEL_KEY);
    }
    
    public String nomenclatureLabelForCurrentRepart() {
        return (String)currentRepart.nomenclature().valueForKey(nomenclatureLabelKey());
    }
    
    public BigDecimal pctForCurrentRepart() {
        if (BigDecimal.ZERO.compareTo(montantTotal()) < 0) {
            return currentRepart.montantHt().divide(montantTotal(), RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100));
        }
        return null;
    }
    
    public IRepartMontant getCurrentRepart() {
        return currentRepart;
    }
    
    public void setCurrentRepart(IRepartMontant currentRepart) {
        this.currentRepart = currentRepart;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
}