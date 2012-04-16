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

import java.util.Enumeration;

import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.AvenantDocument;
import org.cocktail.fwkcktlgfcguiajax.server.controler.CktlAjaxControler;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.foundation.NSArray;

import er.ajax.AjaxTreeModel;
import er.extensions.eof.ERXEC;

public class CktlAjaxContratTreeViewCtrl extends CktlAjaxControler {

	private CktlAjaxContratTreeView wocomponent;
	private Object _delegate;
	private ContratTreeViewNode rootNode;
	private ContratTreeViewNode node;
	public AjaxTreeModel myTreeModel = new AjaxTreeModel();

	public CktlAjaxContratTreeViewCtrl(CktlAjaxContratTreeView component) {
		this(component, null);
	}
	public CktlAjaxContratTreeViewCtrl(CktlAjaxContratTreeView component,ERXEC edc) {
		super(component, edc);
		wocomponent = (CktlAjaxContratTreeView)super.wocomponent;
	}

	public WOActionResults onSelection() {
		ContratTreeViewNode node = node();
		if (node != null) {
			wocomponent.setValueForBinding(node.avenant(), wocomponent.BDG_AVENANT);
			wocomponent.setAvenant(node.avenant());
			wocomponent.setValueForBinding(node.document(), wocomponent.BDG_DOCUMENT);
			wocomponent.setDocument(node.document());
		}
		return null;
	}
	/**
	 * @return the rootNode
	 */
	public ContratTreeViewNode rootNode() {
		if (rootNode == null) {
			rootNode = new ContratTreeViewNode(wocomponent.contrat());
			rootNode.setType(ContratTreeViewNode.TYPE_CONTRAT);
			wocomponent.setRefreshTreeView(Boolean.FALSE);
		}
		return rootNode;
	}
	/**
	 * @param rootNode the rootNode to set
	 */
	public void setRootNode(ContratTreeViewNode rootNode) {
		this.rootNode = rootNode;
	}
	/**
	 * @return the node
	 */
	public ContratTreeViewNode node() {
		return node;
	}
	/**
	 * @param node the node to set
	 */
	public void setNode(ContratTreeViewNode node) {
		this.node = node;
	}

	public Object delegate() {
		if (_delegate == null) {
			_delegate = new ContratTreeViewNode.Delegate();
		}
		return _delegate;

	}

	public void refreshContratTreeView() {
		rootNode()._children = null;
	}

	public ContratTreeViewNode expandSelectedNode(ContratTreeViewNode node, Avenant avenant, AvenantDocument document) {
		ContratTreeViewNode selectedNode = null;
		
		if ((document != null && document.equals(node.document())) || (avenant != null && avenant.equals(node.avenant()))) {
			selectedNode = node;
		} else {
			NSArray childrens = node.childrenTreeNodes();
			Enumeration<ContratTreeViewNode> enumChildrens = childrens.objectEnumerator();
			while (selectedNode == null && enumChildrens.hasMoreElements()) {
				ContratTreeViewNode contratTreeViewNode = (ContratTreeViewNode) enumChildrens.nextElement();
				selectedNode = expandSelectedNode(contratTreeViewNode, avenant, document);
			}
		}
				
		if (selectedNode != null && selectedNode.parentNode() != null) {
			myTreeModel.setExpanded(selectedNode.parentNode(), true);
			if (selectedNode.isAvenant()) {
				myTreeModel.setExpanded(selectedNode, true);
			}
		}
		
		return selectedNode;
	}
}
