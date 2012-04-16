package org.cocktail.cocolight.serveur.components.assistants.modules;

import java.util.Enumeration;

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
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;

import er.ajax.AjaxUploadProgress;

public class ConventionDocuments extends ModuleAssistant {

	public AvenantDocument unAvenantDocument,unAvenantDocumentSelectionne;
	private WODisplayGroup dgDocuments;
	private boolean refreshDocuments;
	private NSMutableDictionary dicoDocuments;
	
	public NSData _data;
	public String _finalFilePath, _filePath;
	public AjaxUploadProgress _uploadProgress;
	private Boolean withImmediateSave = Boolean.FALSE;

	public ConventionDocuments(WOContext context) {
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
 		if (dgDocuments == null || refreshDocuments == true) {
 			if (dgDocuments ==  null) {
 				dgDocuments = new WODisplayGroup();
 			}
 			refreshDocuments = false;
			if (contrat() != null) {
		        EOArrayDataSource ds = new EOArrayDataSource(EOClassDescription.classDescriptionForClass(AvenantDocument.class),editingContext());
		        NSMutableArray<AvenantDocument> avenantDocuments = new NSMutableArray<AvenantDocument>();
		        Enumeration<Avenant> enumAvenants = contrat().avenants().objectEnumerator();
		        while (enumAvenants.hasMoreElements()) {
					Avenant avenant = (Avenant) enumAvenants.nextElement();
					avenantDocuments.addObjectsFromArray(avenant.avenantDocuments());
				}
		        ds.setArray(avenantDocuments);
		        dgDocuments.setDataSource(ds);
		        dgDocuments.fetch();
		        dgDocuments.setSelectsFirstObjectAfterFetch(false);
			}
 		}
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
			NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionDocuments.plist", null, NSArray.EmptyArray));
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
		nextPage.setIsChoixAvenantPossible(true);
		nextPage.setWindowId("AddDocumentModalBox");
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
		AvenantDocument leDocumentAArchiver = (AvenantDocument)dgDocuments().selectedObject();

		if (leDocumentAArchiver != null) {
			FactoryAvenant fa = new FactoryAvenant(leDocumentAArchiver.editingContext(),application().isModeDebug());
			try {
				fa.supprimerDocument(editingContext(), leDocumentAArchiver, applicationUser(), Integer.valueOf(application().config().intForKey("ROOT_GED_GROUPE_PARTENAIRE")));
				setDgDocuments(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	public WOActionResults visualiserUnDocument() {
		return null;
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

}