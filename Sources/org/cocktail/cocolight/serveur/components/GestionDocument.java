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

import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryAvenant;
import org.cocktail.fwkcktlajaxwebext.serveur.CktlResourceProvider;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlged.serveur.metier.EODocument;
import org.cocktail.fwkcktlged.serveur.metier.EOTypeDocument;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;

import er.ajax.AjaxUploadProgress;
import er.ajax.AjaxUtils;

public class GestionDocument extends MyWOComponent {

	public static String BINDING_choixAvenant = "choixAvenant";
	public NSData _data;
	private String url;
	public String _finalFilePath, _filePath;
	public AjaxUploadProgress _uploadProgress;
	private Contrat contrat;
	private Boolean withImmediateSave = Boolean.FALSE;
	private boolean isRBFichierChecked = true;
	private boolean isRBURLChecked = false;
	private NSArray<EOTypeDocument> types;
	private EOTypeDocument unType, leTypeSelectionne;
	private NSArray<Avenant> lesAvenants;
	private Avenant unAvenant;
	private String objet;
	private String motsCles;
	private String commentaire;
	private Avenant avenantSelectionne;
	private EODocument document;
	private boolean isChoixAvenantPossible;
	private String windowId;
	
	
	public GestionDocument(WOContext context) {
        super(context);
    }
	public GestionDocument(WOContext context, Boolean withImmediateSave) {
        super(context);
        setWithImmediateSave(withImmediateSave);
    }

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		AjaxUtils.addScriptResourceInHead(context, response, "FwkCktlAjaxWebExt.framework", "scripts/cktlwonder.js");
        // On rajoute les ressources Web du composant parent
        if (context.page() != null && context.page() instanceof CktlResourceProvider) {
            ((CktlResourceProvider)context.page()).injectResources(response, context);
        }
	}

//	public boolean synchronizesVariablesWithBindings() {
//		return true;
//	}
	
	public WOActionResults ajouterUnDocument() {
	    if (avenantSelectionne() != null) {
	        FactoryAvenant fa = new FactoryAvenant(editingContext(),Boolean.TRUE);
	        fa.ajouterDocument(editingContext(), session.applicationUser(), avenantSelectionne(), document());
	        if (withImmediateSave) {
	            editingContext().saveChanges();
	        }
	        CktlAjaxWindow.close(context());
	    }
	    return null;
	}

	public WOActionResults annuler() {
		editingContext().revert();
		if (windowId() != null) {
			CktlAjaxWindow.close(context(),windowId());
		}
		return null;
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

	public String failedFunction() {
		String failedFunction = null;
		failedFunction = "alert('Pour une raison technique, le serveur de gestion de documents est actuellement indisponible.Veuillez re-essayer plus tard.')";
		return failedFunction;
	}
	/**
	 * @return the withImmediateSave
	 */
	public Boolean withImmediateSave() {
		return withImmediateSave;
	}
	/**
	 * @param withImmediateSave the withImmediateSave to set
	 */
	public void setWithImmediateSave(Boolean withImmediateSave) {
		this.withImmediateSave = withImmediateSave;
	}

	/**
	 * @return the url
	 */
	public String url() {
		if (url == null) {
			url = "http://";
		}
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the objet
	 */
	public String objet() {
		return objet;
	}
	/**
	 * @param objet the objet to set
	 */
	public void setObjet(String objet) {
		this.objet = objet;
	}

	/**
	 * @return the motsCles
	 */
	public String motsCles() {
		if (motsCles == null) {
			if (document() == null) {
				if (avenantSelectionne() != null) {
					motsCles = avenantSelectionne().modeGestion().mgLibelle()+";"+avenantSelectionne().contrat().typeContrat().tyconLibelle()+";";
					if (avenantSelectionne().discipline() != null) {
						motsCles += avenantSelectionne().discipline().discLibelleLong();
					}	
				}
			} else {
				motsCles = document().motsClefs();
			}
		}
		return motsCles;
	}
	/**
	 * @param motsCles the motsCles to set
	 */
	public void setMotsCles(String motsCles) {
		this.motsCles = motsCles;
	}

	/**
	 * @return the commentaire
	 */
	public String commentaire() {
		return commentaire;
	}
	/**
	 * @param commentaire the commentaire to set
	 */
	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	/**
	 * @return the types
	 */
	public NSArray<EOTypeDocument> types() {
		if (types == null) {
			if (hasBinding("types")) {
				types = (NSArray)valueForBinding("types");
			} else {
				types = EOTypeDocument.fetchAll(editingContext());
			}
		}
		return types;
	}
	/**
	 * @param types the types to set
	 */
	public void setTypes(NSArray<EOTypeDocument> types) {
		this.types = types;
	}

	/**
	 * @return the unType
	 */
	public EOTypeDocument unType() {
		return unType;
	}
	/**
	 * @param unType the unType to set
	 */
	public void setUnType(EOTypeDocument unType) {
		this.unType = unType;
	}

	/**
	 * @return the leTypeSelectionne
	 */
	public EOTypeDocument leTypeSelectionne() {
		return leTypeSelectionne;
	}
	/**
	 * @param leTypeSelectionne the leTypeSelectionne to set
	 */
	public void setLeTypeSelectionne(EOTypeDocument leTypeSelectionne) {
		this.leTypeSelectionne = leTypeSelectionne;
	}

	/**
	 * @return the isRBFichierChecked
	 */
	public boolean isRBFichierChecked() {
		return isRBFichierChecked;
	}
	public boolean isRBURLChecked() {
		return isRBURLChecked;
	}
	public boolean isSupportFichier() {
		return isRBFichierChecked;
	}
	public boolean isSupportURL() {
		return isRBURLChecked;
	}
	public WOActionResults selectionnerFichier() {
		isRBURLChecked = false;
		isRBFichierChecked = true;
		return null;
	}
	public WOActionResults selectionnerURL() {
		isRBURLChecked = true;
		isRBFichierChecked = false;
		return null;
	}
	/**
	 * @return the lesAvenants
	 */
	public NSArray<Avenant> lesAvenants() {
		if (lesAvenants == null) {
			EOSortOrdering sortOrderingIndex = EOSortOrdering.sortOrderingWithKey(Avenant.AVT_INDEX_KEY, EOSortOrdering.CompareAscending);
			lesAvenants = contrat().avenants(null,new NSArray<EOSortOrdering>(sortOrderingIndex),false);
			// setAvenantSelectionne(lesAvenants.objectAtIndex(0));
		}
		return lesAvenants;
	}
	/**
	 * @param lesAvenants the lesAvenants to set
	 */
	public void setLesAvenants(NSArray<Avenant> lesAvenants) {
		this.lesAvenants = lesAvenants;
	}
	/**
	 * @return the unAvenant
	 */
	public Avenant unAvenant() {
		return unAvenant;
	}
	/**
	 * @param unAvenant the unAvenant to set
	 */
	public void setUnAvenant(Avenant unAvenant) {
		this.unAvenant = unAvenant;
	}
	/**
	 * @return the avenantSelectionne
	 */
	public Avenant avenantSelectionne() {
		if (avenantSelectionne == null && lesAvenants() != null) {
			avenantSelectionne = contrat().avenantZero();
		}
		return avenantSelectionne;
	}
	/**
	 * @param avenantSelectionne the avenantSelectionne to set
	 */
	public void setAvenantSelectionne(Avenant avenantSelectionne) {
		this.avenantSelectionne = avenantSelectionne;
	}
	/**
	 * @return the document
	 */
	public EODocument document() {
		return document;
	}
	/**
	 * @param document the document to set
	 */
	public void setDocument(EODocument document) {
		this.document = document;
	}
	public Integer racineGedi() {
		return Integer.valueOf(application.config().intForKey("ROOT_GED_GROUPE_PARTENAIRE"));
	}
	public String repertoireGedi() {
		String repertoireGedi = null;
		if (contrat().numeroContrat().endsWith("????")) {
			// Le contrat n'a pas encore ete enregistre ==> il ne possede pas encore de numero
			repertoireGedi = "TEMPO";
		} else {
			repertoireGedi = contrat.numeroContrat()+"/";
			if (avenantSelectionne().avtIndex().intValue() == 0) {
				repertoireGedi += "Documents principaux";
			} else {
				repertoireGedi += "Avenant nº "+avenantSelectionne().avtIndex();
			}
		}
		
		return repertoireGedi;
	}
	public String reference() {
		String reference = contrat().conReferenceExterne();
		if (reference == null) {
			if (contrat().numeroContrat().endsWith("????")) {
				reference = "ABORT/"+applicationUser.getLogin();
			} else {
				reference = contrat().numeroContrat();
			}
		}
		
		return reference;
	}
	/**
	 * @return the isChoixAvenantPossible
	 */
	public boolean isChoixAvenantPossible() {
		return isChoixAvenantPossible;
	}
	/**
	 * @param isChoixAvenantPossible the isChoixAvenantPossible to set
	 */
	public void setIsChoixAvenantPossible(boolean isChoixAvenantPossible) {
		this.isChoixAvenantPossible = isChoixAvenantPossible;
	}
	/**
	 * @return the windowId
	 */
	public String windowId() {
		return windowId;
	}
	/**
	 * @param windowId the windowId to set
	 */
	public void setWindowId(String windowId) {
		this.windowId = windowId;
	}
	
	public EOEditingContext editingContext() {
	    return avenantSelectionne().editingContext();
	}
	
	
}
