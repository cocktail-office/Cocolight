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
package org.cocktail.cocolight.serveur.components.assistants;

import org.cocktail.cocolight.serveur.components.Accueil;
import org.cocktail.cocolight.serveur.components.Convention;
import org.cocktail.cocolight.serveur.components.MyWOComponent;
import org.cocktail.cocowork.server.metier.convention.Contrat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

public class ApresEnregistrement extends MyWOComponent {
	
	private Contrat contrat;
	
    public ApresEnregistrement(WOContext context) {
        super(context);
    }

	/**
	 * @return the contrat
	 */
	public Contrat contrat() {
		return contrat;
	}

	/**
	 * @param contrat the contrat to set
	 */
	public void setContrat(Contrat contrat) {
		this.contrat = contrat;
	}

	public WOActionResults consulter() {
		Convention nextPage = (Convention)pageWithName(Convention.class.getName());
		nextPage.setConvention(contrat());
        session().setPageConvention(nextPage);
		return nextPage;
	}

	public WOActionResults accueil() {
		Accueil nextPage = (Accueil)pageWithName(Accueil.class.getName());
		session().reset();
		return nextPage;
	}
}
