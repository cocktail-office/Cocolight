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

import org.cocktail.cocolight.serveur.components.GestionDocument;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.AvenantDocument;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryAvenant;
import org.cocktail.fwkcktlged.serveur.metier.EODocument;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;

import er.ajax.AjaxUploadProgress;

public class AvenantDocuments extends ModuleAssistant {

	public AvenantDocument unAvenantDocument,unAvenantDocumentSelectionne;
	private WODisplayGroup dgDocuments;
	private boolean refreshDocuments;
	private NSMutableDictionary dicoDocuments;
	
	public NSData _data;
	public String _finalFilePath, _filePath;
	public AjaxUploadProgress _uploadProgress;
	private Boolean withImmediateSave = Boolean.FALSE;

	public AvenantDocuments(WOContext context) {
        super(context);
        refreshDocuments = false;
        Class[] notificationArray = new Class[] { NSNotification.class };
        NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("refreshDocuments",notificationArray), "refreshDocumentsNotification", null);
        setWithImmediateSave(withImmediateSave);
    }

	/**
	 * @return the dgDocuments
	 */
	public WODisplayGroup dgDocuments() {
	    if (dgDocuments == null) {
	        dgDocuments = new WODisplayGroup();
	    }
	    dgDocuments.setObjectArray(avenant().avenantDocuments());
	    return dgDocuments;
	}

	/**
	 * @param dgDocuments the dgDocuments to set
	 */
	public void setDgDocuments(WODisplayGroup dgDocuments) {
		this.dgDocuments = dgDocuments;
	}

	public NSMutableDictionary dicoDocuments() {
		if (dicoDocuments == null) {
			NSData data = new NSData(application().resourceManager().bytesForResourceNamed("AvenantDocuments.plist", null, NSArray.EmptyArray));
			dicoDocuments = new NSMutableDictionary((NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8"));
		}
		return dicoDocuments;
	}

	public void refreshDocuments(NSNotification notification) {
		if (notification.userInfo() != null && notification.userInfo().containsKey("edc")) {
			EOEditingContext ed = (EOEditingContext)notification.userInfo().objectForKey("edc");
			if (ed.equals(editingContext())) {
				refreshDocuments = true;
				dgDocuments().setSelectedObject(notification.object());
			}
		} else {
			refreshDocuments = true;
		}
	}

	public WOActionResults ajouterUnDocument() {
		GestionDocument nextPage = (GestionDocument)pageWithName(GestionDocument.class.getName());
		nextPage.setContrat(contrat());
		EOSortOrdering sortOrderingIndex = EOSortOrdering.sortOrderingWithKey(Avenant.AVT_INDEX_KEY, EOSortOrdering.CompareAscending);
		NSArray<Avenant> lesAvenants = contrat().avenants(null,new NSArray<EOSortOrdering>(sortOrderingIndex),false);
		nextPage.setLesAvenants(lesAvenants);
		nextPage.setAvenantSelectionne(avenant());
		nextPage.setIsChoixAvenantPossible(false);
		nextPage.setWindowId("AddDocumentModalBox1");
		return nextPage;
	}
	
	public String onClickAjouterDocument() {
		String onClickAjouterDocument = null;
		onClickAjouterDocument = "openWinAjouterDocument(this.href,'Ajouter un document');return false;";
		
		return onClickAjouterDocument;
	}

	public boolean isSupprimerUnDocumentEnabled() {
		return dgDocuments() != null && dgDocuments().selectedObject() != null ? true:false;
	}
	
	public boolean isSupprimerUnDocumentDisabled() {
		return !isSupprimerUnDocumentEnabled();
	}
	
	public WOActionResults supprimerUnDocument() {
	    AvenantDocument leDocumentASupprimer = (AvenantDocument)dgDocuments().selectedObject();

	    if (leDocumentASupprimer != null) {
	        FactoryAvenant fa = new FactoryAvenant(leDocumentASupprimer.editingContext(),application().isModeDebug());
	        fa.supprimerDocument(editingContext(), leDocumentASupprimer, applicationUser(), Integer.valueOf(application().config().intForKey("ROOT_GED_GROUPE_PARTENAIRE")));
	        setDgDocuments(null);
	    }

	    return null;
	}
	
	public String onClickSupprimerPartenaire() {
		String onClickAjouterPartenaire = null;
		onClickAjouterPartenaire = "openWinAjouterPartenaire(this.href,'Ajouter un partenaire');return false;";
		
		return onClickAjouterPartenaire;
	}


	/**
	 * @return the unAvenantDocument
	 */
	public AvenantDocument unAvenantDocument() {
		return unAvenantDocument;
	}

	/**
	 * @param unAvenantDocument the unAvenantDocument to set
	 */
	public void setUnAvenantDocument(AvenantDocument unAvenantDocument) {
		this.unAvenantDocument = unAvenantDocument;
	}

	public WOActionResults afficherDocument() {
		return null;
	}

	/**
	 * @return the unAvenantDocumentSelectionne
	 */
	public AvenantDocument unAvenantDocumentSelectionne() {
		return (AvenantDocument)dgDocuments().selectedObject();
	}

	/**
	 * @param unAvenantDocumentSelectionne the unAvenantDocumentSelectionne to set
	 */
	public void setUnAvenantDocumentSelectionne(AvenantDocument unAvenantDocumentSelectionne) {
		this.unAvenantDocumentSelectionne = unAvenantDocumentSelectionne;
	}

	public EODocument documentAttache() {
		    return unAvenantDocument().document();
	}
	
/*	public String unAvenantDocumentSelectionneHRef() {
		ReferenceLien rfl = null;
		
		rfl = (ReferenceLien)unAvenantDocumentSelectionne().document().referenceLiens().lastObject();
		
//		return rfl.rflLien();

	    ERAttachment attachment = (ERAttachment) rfl.attachment();
	    String attachmentUrl = "#";
	    if (attachment != null) {
	      attachmentUrl = ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context().request(), context());
	      if (!attachment.available().booleanValue()) {
	      }
	      else {
	          try {
	            ERXMutableURL attachmentMutableUrl = new ERXMutableURL(attachmentUrl);
	            attachmentMutableUrl.addQueryParameter("attachment", "true");
	            attachmentUrl = attachmentMutableUrl.toExternalForm();
	          }
	          catch (MalformedURLException e) {
	            throw new RuntimeException("Failed to create attachment URL.", e);
	          }
	      }
	    }
	    return attachmentUrl;
}
	public String failedFunction() {
		String failedFunction = null;
		failedFunction = "alert('Pour une raison technique, le serveur de gestion de documents est actuellement indisponible.Veuillez re-essayer plus tard.')";
		return failedFunction;
	}


	public WOActionResults visualiserUnDocument() {
		return null;
	}
*/

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

}
