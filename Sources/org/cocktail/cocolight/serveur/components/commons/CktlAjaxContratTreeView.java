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

import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.AvenantDocument;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktlajaxwebext.serveur.CktlAjaxWOComponent;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.eof.ERXEC;

public class CktlAjaxContratTreeView extends CktlAjaxWOComponent {

	private static final String BDG_EDC = "editingContext";
	protected static final String BDG_CONTRAT = "contrat";
	protected static final String BDG_AVENANT = "avenant";
	protected static final String BDG_DOCUMENT = "document";
	public static final String BDG_onSuccessSelect = "onSuccessSelect";
	public static final String BDG_REFRESH_TREEVIEW = "refreshTreeView";
	
	private CktlAjaxContratTreeViewCtrl ctrl;
	private ERXEC edc;
	protected Contrat contrat;
	protected Avenant avenant;
	protected AvenantDocument document;
	protected Boolean refreshTreeView = Boolean.FALSE;
	
	public CktlAjaxContratTreeView(WOContext context) {
        super(context);
    }
    
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (hasBinding(BDG_REFRESH_TREEVIEW)) {
			if (Boolean.TRUE.equals(valueForBinding(BDG_REFRESH_TREEVIEW))) {
				ctrl().refreshContratTreeView();
				if (avenant() != null || document() != null) {
					ctrl.expandSelectedNode(ctrl().rootNode(),avenant(),document());
				}
			}
			if (canSetValueForBinding(BDG_REFRESH_TREEVIEW)) {
				setValueForBinding(Boolean.FALSE, BDG_REFRESH_TREEVIEW);
			}
		}
		super.appendToResponse(response, context);

	}

	public ERXEC edc() {
    	if (edc == null) {
    		if (hasBinding(CktlAjaxContratTreeView.BDG_EDC)) {
            edc = (ERXEC)valueForBinding(CktlAjaxContratTreeView.BDG_EDC);
    		} else {
    			edc = (ERXEC)session().defaultEditingContext();
    		}
    	}
		return edc;
	}
	public void setEdc(ERXEC edc) {
		this.edc = edc;
	}

	/**
	 * @return the ctrl
	 */
	public CktlAjaxContratTreeViewCtrl ctrl() {
    	if (ctrl == null) {
    		ctrl = new CktlAjaxContratTreeViewCtrl(this, edc());        
    	}
		return ctrl;
	}


	/**
	 * @param ctrl the ctrl to set
	 */
	public void setCtrl(CktlAjaxContratTreeViewCtrl ctrl) {
		this.ctrl = ctrl;
	}

	/**
	 * @return the contrat
	 */
	public Contrat contrat() {
		if (contrat == null) {
			contrat = (Contrat)valueForBinding(BDG_CONTRAT);
		}
		return contrat;
	}

	/**
	 * @param contrat the contrat to set
	 */
	public void setContrat(Contrat contrat) {
		this.contrat = contrat;
	}

	/**
	 * @return the avenant
	 */
	public Avenant avenant() {
		avenant = (Avenant)valueForBinding(BDG_AVENANT);
		return avenant;
	}

	/**
	 * @param avenant the avenant to set
	 */
	public void setAvenant(Avenant avenant) {
		this.avenant = avenant;
	}

	/**
	 * @return the document
	 */
	public AvenantDocument document() {
		if (document == null) {
			document = (AvenantDocument)valueForBinding(BDG_DOCUMENT);
		}
		return document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(AvenantDocument document) {
		this.document = document;
	}

	public String containerAjaxTreeID() {
		return getComponentId()+"_containerAjaxTree";
	}

    public String unAjaxTreeID() {
		return getComponentId()+"_ajaxTree";
	}

    public String onSuccessSelect() {
    	return (String) valueForBinding(BDG_onSuccessSelect);
    }

	public String onClickLinkSelection() {
		String onClickLinkSelection = "";
		onClickLinkSelection += "var selections = $$('#"+unAjaxTreeID()+" li a.selected'); if (selections.size()>0){selections.first().removeClassName('selected');};";
		onClickLinkSelection += "this.addClassName('selected');";

		return onClickLinkSelection;
	}

	public String classContratLink() {
		String classContratLink = "";
		if (contrat() != null && avenant() == null && document() == null) {
			classContratLink = "selected";
		}
		return classContratLink;
	}
	public String classAvenantLink() {
		String classAvenantLink = "";
		if (contrat() != null && avenant() != null && document() == null && avenant().equals(ctrl().node().avenant())) {
			classAvenantLink = "selected";
		}
		return classAvenantLink;
	}
	public String classDocumentLink() {
		String classDocumentLink = "";
		if (contrat() != null && avenant() != null && document() != null && document().equals(ctrl().node().document())) {
			classDocumentLink = "selected";
		}
		return classDocumentLink;
	}

	public Boolean refreshTreeView() {
		return (Boolean)valueForBinding(BDG_REFRESH_TREEVIEW);
	}
	public void setRefreshTreeView(Boolean reset) {
		refreshTreeView = reset;
	}
}