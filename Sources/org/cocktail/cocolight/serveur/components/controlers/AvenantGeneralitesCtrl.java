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

import org.cocktail.cocolight.serveur.components.assistants.modules.AvenantGeneralites;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.TypeAvenant;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.modules.IModuleAssistant;
import org.cocktail.fwkcktlaccordsguiajax.components.controlers.CtrlModule;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;

import com.webobjects.foundation.NSArray;

public class AvenantGeneralitesCtrl extends CtrlModule {

    private AvenantGeneralites wocomponent;

    private TypeAvenant unTypeAvenant;
    private NSArray<TypeAvenant> lesTypesAvenant;

    private EOTva unTauxDeTVA;
    private String leTauxDeTVASelectionne;
    
    public AvenantGeneralitesCtrl(IModuleAssistant component) {
        super(component);
    }

    /**
     * @return the unTypeAvenant
     */
    public TypeAvenant unTypeAvenant() {
        return unTypeAvenant;
    }

    /**
     * @param unTypeAvenant
     *            the unTypeAvenant to set
     */
    public void setUnTypeAvenant(TypeAvenant unTypeAvenant) {
        this.unTypeAvenant = unTypeAvenant;
    }

    /**
     * @return the lesTypesAvenant
     */
    public NSArray<TypeAvenant> lesTypesAvenant() {
        if (lesTypesAvenant == null) {
            lesTypesAvenant = TypeAvenant.fetchAllWithoutInitial(editingContext());
        }
        return lesTypesAvenant;
    }

    /**
     * @param lesTypesAvenant
     *            the lesTypesAvenant to set
     */
    public void setLesTypesAvenant(NSArray<TypeAvenant> lesTypesAvenant) {
        this.lesTypesAvenant = lesTypesAvenant;
    }

    /**
     * @return the unTauxDeTVA
     */
    public EOTva unTauxDeTVA() {
        return unTauxDeTVA;
    }

    /**
     * @param unTauxDeTVA
     *            the unTauxDeTVA to set
     */
    public void setUnTauxDeTVA(EOTva unTauxDeTVA) {
        this.unTauxDeTVA = unTauxDeTVA;
    }

    /**
     * @return the leTauxDeTVASelectionne
     */
    public String leTauxDeTVASelectionne() {
        if (leTauxDeTVASelectionne == null) {
            Avenant avenant = wocomponent.avenant();
            BigDecimal montantHT = avenant.avtMontantHt();
            BigDecimal montantTTC = avenant.avtMontantTtc();
            if (montantHT != null && montantHT.doubleValue() > 0 && montantTTC != null) {
                double taux = (montantTTC.doubleValue() - montantHT.doubleValue()) / montantHT.doubleValue();
                leTauxDeTVASelectionne = String.valueOf(taux);
            }
        }
        return leTauxDeTVASelectionne;
    }

    /**
     * @param leTauxDeTVASelectionne
     *            the leTauxDeTVASelectionne to set
     */
    public void setLeTauxDeTVASelectionne(String leTauxDeTVASelectionne) {
        this.leTauxDeTVASelectionne = leTauxDeTVASelectionne;
        double taux = Double.valueOf(leTauxDeTVASelectionne).doubleValue();
        Avenant avenant = wocomponent.avenant();
        BigDecimal montantHT = avenant.avtMontantHt();
        if (montantHT != null) {
            double ht = montantHT.doubleValue();
            double ttc = ht * (1 + taux / 100);
            avenant.setAvtMontantTtc(BigDecimal.valueOf(ttc));
        }
        else {
            avenant.setAvtMontantTtc(null);
        }
    }

}
