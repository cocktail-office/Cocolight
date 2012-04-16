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

import org.cocktail.cocolight.serveur.components.controlers.AvenantGeneralitesCtrl;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.TypeAvenant;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.Assistant;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

public class AvenantGeneralites extends ModuleAssistant {

    private static final long serialVersionUID = 6135058736459389427L;
    public static final String BINDING_ON_ENREGISTRER = "onEnregistrer";
    public static final String BINDING_ON_ANNULER = "onAnnuler";
    
    public AvenantGeneralitesCtrl ctrl = null;

	public AvenantGeneralitesCtrl ctrl() {
		if (ctrl == null) {
	        ctrl = new AvenantGeneralitesCtrl(this);
		}
		return ctrl;
	}

	public void setCtrl(AvenantGeneralitesCtrl ctrl) {
		this.ctrl = ctrl;
	}

	public AvenantGeneralites(WOContext context) {
        super(context);
    }

	public boolean financierDisabled() {
	    return disabled() || (avenant().typeAvenant() != null && !TypeAvenant.CODE_TYPE_AVENANT_FINANCIER.equals(avenant().typeAvenant().taCode()));
	}
	
	@Override
	public boolean disabled() {
	    return avenant() == null || avenant().isSigne() || ((!applicationUser().hasDroitModificationContratsEtAvenants() ||
               !contrat().isModifiablePar(applicationUser().getUtilisateur())) && !applicationUser().hasDroitSuperAdmin());
	}
	
	public boolean valider() {
		boolean validate = false;
		Avenant avenant = avenant();
		
		if (avenant != null && avenant.typeAvenant() != null && 
				avenant.avtObjet() != null && avenant.avtObjet().length() > 0 ) {
			validate = true;
			((Assistant)parent()).setFailureMessage(null);
		} else {
			((Assistant)parent()).setFailureMessage("Vous devez au moins renseigner le type et l'objet de l'avenant.");
		}
    	return validate;
    }

	public WOComponent submit() {
//		Avenant avenant = avenant();
//		BigDecimal mtHT = avenant.avtMontantHt();
//		if (avenant != null &&  mtHT!= null) {
//			BigDecimal aValue = mtHT;
//			if (ctrl().leTauxDeTVASelectionne() != null) {
//				BigDecimal taux = new BigDecimal(ctrl().leTauxDeTVASelectionne());
//				taux = taux.add(new BigDecimal(100.00));
//				aValue = aValue.multiply(taux);
//				aValue = aValue.divide(new BigDecimal(100.00));
//			}
//			avenant.setAvtMontantTtc(aValue);
//		}
		return null;
	}

	public NSTimestamp avtDateDeb() {
	    return avenant().avtDateDeb();
	}
	
	public void setAvtDateDeb(NSTimestamp date) {
	    try {
            avenant().setAvtDateDeb(date);
        }
        catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        }
	}
	
	public NSTimestamp avtDateFin() {
	    return avenant().avtDateFin();
	}
	
	public void setAvtDateFin(NSTimestamp date) {
	    try {
	        avenant().setAvtDateFin(date);
	    }
	    catch (ValidationException e) {
	        session().addSimpleErrorMessage("Cocolight", e.getMessage());
	    }
	}
	
	@Override
	public WOActionResults enregistrer() {
	    super.enregistrer();
	    if (hasBinding(BINDING_ON_ENREGISTRER)) {
	        return (WOActionResults) valueForBinding(BINDING_ON_ENREGISTRER);
	    } else {
	        return null;
	    }
	}
	
	public WOActionResults annuler() {
	    if (hasBinding(BINDING_ON_ANNULER)) {
	        return (WOActionResults) valueForBinding(BINDING_ON_ANNULER);
	    } else {
	        return accueil();
	    }
	}
}
