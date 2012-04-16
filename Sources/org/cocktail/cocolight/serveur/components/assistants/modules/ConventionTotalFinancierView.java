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

import org.cocktail.cocolight.serveur.components.MyWOComponent;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

/**
 * Affichage du total financier de la convention :
 * HT et TTC de l'avenant initial + HT et TTC des éventuels avenants valides.
 * 
 * @binding contrat la convention
 * 
 * @author Alexis TUAL <alexis.tual at cocktail.org>
 *
 */
public class ConventionTotalFinancierView extends MyWOComponent {

    private static final long serialVersionUID = 5025361383198767013L;
    private static final String BINDING_CONTRAT = "contrat";
    private static final String BINDING_UPDATE_CONTAINER_ID = "updateContainerID";
    private static final String DEFAULT_UPDATE_CONTAINER_ID = "ContainerDonneesFinancieres";
    
    private Avenant currentAvenant;
    private EOTva currentTva;
    
    public ConventionTotalFinancierView(WOContext context) {
        super(context);
    }
    
    @Override
    public void validationFailedWithException(Throwable e, Object obj, String key) {
        session().addSimpleErrorMessage("Cocolight", e.getMessage());
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public WOActionResults refresh() {
        return null;
    }
    
    public Contrat contrat() {
        return (Contrat)valueForBinding(BINDING_CONTRAT);
    }
    
    public String tvaId() {
        return "Tva_" + getComponentId();
    }
    
    public String tvaRecupId() {
        return "TvaRecup_" + getComponentId();
    }
    
    public String montantHtId() {
        return "MontantHT_" + getComponentId();
    }
    
    public String montantTtcId() {
        return "MontantTTC_" + getComponentId();
    }
    
    public String updateContainerID() {
        String updateContainerID = DEFAULT_UPDATE_CONTAINER_ID;
        if (hasBinding(BINDING_UPDATE_CONTAINER_ID))
            updateContainerID = (String)valueForBinding(BINDING_UPDATE_CONTAINER_ID);
        return updateContainerID;
    }
    
    public Avenant getCurrentAvenant() {
        return currentAvenant;
    }
    
    public void setCurrentAvenant(Avenant currentAvenant) {
        this.currentAvenant = currentAvenant;
    }
 
    public EOTva getCurrentTva() {
        return currentTva;
    }
    
    public void setCurrentTva(EOTva currentTva) {
        this.currentTva = currentTva;
    }
    
}