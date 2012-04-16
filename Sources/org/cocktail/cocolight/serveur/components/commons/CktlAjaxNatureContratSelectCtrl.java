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

import org.cocktail.cocowork.common.exception.ExceptionFinder;
import org.cocktail.cocowork.server.metier.convention.EOTypeContrat;
import org.cocktail.cocowork.server.metier.convention.TypeContrat;
import org.cocktail.cocowork.server.metier.convention.finder.core.FinderTypeContrat;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlgfcguiajax.server.controler.CktlAjaxControler;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.ajax.AjaxTreeModel;

public class CktlAjaxNatureContratSelectCtrl extends CktlAjaxControler {

	private Object _delegate;
	private TypeContrat rootNatureContrat;
	private TypeContrat uneNatureContrat;
	
	public CktlAjaxNatureContratSelectCtrl(CktlAjaxNatureContratSelect component) {
		this(component, null);
	}
	public CktlAjaxNatureContratSelectCtrl(CktlAjaxNatureContratSelect component,EOEditingContext edc) {
		super(component, edc);
		wocomponent = (CktlAjaxNatureContratSelect)super.wocomponent;
	}
	

	public WOActionResults afficherNatureContratSelectionnee() {
		TypeContrat natureContratNode = uneNatureContrat();
		if (natureContratNode != null) {
			((CktlAjaxNatureContratSelect)wocomponent).setSelection(natureContratNode);
		}
		CktlAjaxWindow.close(wocomponent.context(),"NatureContratTreeViewID");
		return null;
	}

	public TypeContrat rootNatureContrat() {
		if (rootNatureContrat == null) {
			FinderTypeContrat ftc = new FinderTypeContrat(edc);
			try {
				rootNatureContrat = ftc.findWithTyconIdInterne("RACINE");
			} catch (ExceptionFinder e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rootNatureContrat;
	}

	/**
	 * @param rootOrgan the rootNatureContrat to set
	 */
	public void setRootNatureContrat(TypeContrat rootNatureContrat) {
		this.rootNatureContrat = rootNatureContrat;
	}

	/*
	/**
	 * @return the TypeContrat
	 */
	public TypeContrat uneNatureContrat() {
		return uneNatureContrat;
	}

	/**
	 * @param unOrgan the unOrgan to set
	 */
	public void setUneNatureContrat(TypeContrat uneNatureContrat) {
		this.uneNatureContrat = uneNatureContrat;
	}


	public Object delegate() {
		if (_delegate == null) {
			_delegate = new CktlAjaxNatureContratSelectCtrl.Delegate();
		}
		return _delegate;

	}
	public static class Delegate implements AjaxTreeModel.Delegate {

		public NSArray childrenTreeNodes(Object node) {
			NSArray childrenTreeNodes = null;
			TypeContrat treeNode = (TypeContrat)node;
			childrenTreeNodes = treeNode.fils();
			EOSortOrdering libelleOrdering = EOSortOrdering.sortOrderingWithKey(EOTypeContrat.TYCON_LIBELLE_KEY, EOSortOrdering.CompareAscending); 
			childrenTreeNodes = EOSortOrdering.sortedArrayUsingKeyOrderArray(childrenTreeNodes, new NSArray(libelleOrdering));
			return childrenTreeNodes;
		}

		public boolean isLeaf(Object node) {
			boolean isLeaf = true;
			TypeContrat treeNode = (TypeContrat)node;
			NSArray fils = treeNode.fils();
			if (fils!=null && fils.count()>0) {
				isLeaf = false;
			}
			return isLeaf;
		}

		public Object parentTreeNode(Object node) {
			TypeContrat pere = null;
			TypeContrat treeNode = (TypeContrat)node;
			if (treeNode != null && treeNode.pere() != null) {
				pere = treeNode.pere();
			}
			
			return pere;
		}
	}
}
