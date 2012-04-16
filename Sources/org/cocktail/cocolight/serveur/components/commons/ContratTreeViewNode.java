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
import org.cocktail.cocowork.server.metier.convention.Contrat;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxTreeModel;

public class ContratTreeViewNode {
	
	private ContratTreeViewNode parentNode;
	public NSMutableArray<ContratTreeViewNode> _children;

	private Contrat contrat;
	private Avenant avenant;
	private AvenantDocument document;
	private String libelle;
	private String type;
	private boolean refresh;
	
	protected static final String LIBELLE_REP_DOCUMENTS = "Documents";
	protected static final String LIBELLE_REP_AVENANTS = "Avenants";
	protected static final String LIBELLE_VIDE = "< aucun avenant >";
	protected static final String TYPE_CONTRAT = "Contrat";
	protected static final String TYPE_AVENANT = "Avenant";
	protected static final String TYPE_DOCUMENT = "Document";
	protected static final String TYPE_REPERTOIRE = "Repertoire";
	protected static final String TYPE_VIDE = "Vide";
	
	public ContratTreeViewNode() {
		super();
	}
	public ContratTreeViewNode(Contrat unContrat) {
		super();
		this.contrat = unContrat;
		this.libelle = unContrat.numeroContrat();
		this.parentNode = null;
		this.avenant = null;
		this.document = null;
		this.type = null;
		this.refresh = false;
	}
	
	public boolean isContrat() {
		boolean isContrat = false;
		
		if (type() != null && TYPE_CONTRAT.equalsIgnoreCase(type())) {
			isContrat = true;
		}
		return isContrat;
	}
	public boolean isRepertoireDocuments() {
		boolean isRepertoireDocuments = false;
		
		if (libelle() != null && libelle().equalsIgnoreCase(LIBELLE_REP_DOCUMENTS)) {
			isRepertoireDocuments = true;
		}
		return isRepertoireDocuments;
	}
	public boolean isRepertoireAvenants() {
		boolean isRepertoireAvenants = false;
		
		if (libelle() != null && libelle().equalsIgnoreCase(LIBELLE_REP_AVENANTS)) {
			isRepertoireAvenants = true;
		}
		return isRepertoireAvenants;
	}
	public boolean isRepertoire() {
		boolean isRepertoire = false;

		if (type() != null && TYPE_REPERTOIRE.equalsIgnoreCase(type())) {
			isRepertoire = true;
		}
		return isRepertoire;
	}
	public boolean isAvenant() {
		boolean isAvenant = false;
		
		if (type() != null && TYPE_AVENANT.equalsIgnoreCase(type())) {
			isAvenant = true;
		}
		return isAvenant;
	}
	public boolean isDocument() {
		boolean isDocument = false;
		
		if (type() != null && TYPE_DOCUMENT.equalsIgnoreCase(type())) {
			isDocument = true;
		}
		return isDocument;
	}

	public boolean isVide() {
		boolean isVide = false;

		if (type() != null && TYPE_VIDE.equalsIgnoreCase(type())) {
			isVide = true;
		}
		return isVide;
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

	/**
	 * @return the parentNode
	 */
	public ContratTreeViewNode parentNode() {
		return parentNode;
	}
	/**
	 * @param parentNode the parentNode to set
	 */
	public void setParentNode(ContratTreeViewNode parentNode) {
		this.parentNode = parentNode;
	}
	/**
	 * @return the avenant
	 */
	public Avenant avenant() {
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
		return document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(AvenantDocument document) {
		this.document = document;
	}

	/**
	 * @return the libelle
	 */
	public String libelle() {
		return libelle;
	}

	/**
	 * @param libelle the libelle to set
	 */
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	/**
	 * @return the type
	 */
	public String type() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the refresh
	 */
	public boolean refresh() {
		return refresh;
	}
	/**
	 * @param refresh the refresh to set
	 */
	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public synchronized NSArray childrenTreeNodes() {
		if (_children == null) {
			_children = new NSMutableArray<ContratTreeViewNode>();

			if (isContrat()) {
				NSArray avenants = contrat().avenants();
				avenants = EOSortOrdering.sortedArrayUsingKeyOrderArray(avenants, new NSArray(new Object[] {Avenant.SORT_INDEX_ASC}));
				
				Enumeration<Avenant>enumAvenants = avenants.objectEnumerator();
				while (enumAvenants.hasMoreElements()) {
					Avenant avenant = (Avenant) enumAvenants.nextElement();
					if (avenant.avtIndex().intValue() > 0) {
						ContratTreeViewNode nodeAvenant = new ContratTreeViewNode(contrat());
						nodeAvenant.setAvenant(avenant);
						nodeAvenant.setLibelle("Avt "+avenant.avtIndex());
						nodeAvenant.setParentNode(this);
						nodeAvenant.setType(ContratTreeViewNode.TYPE_AVENANT);
						_children.addObject(nodeAvenant);
					}
				}
				if (_children.count()==0) {
					ContratTreeViewNode nodeAvenant = new ContratTreeViewNode(contrat());
					nodeAvenant.setLibelle(ContratTreeViewNode.LIBELLE_VIDE);
					nodeAvenant.setParentNode(this);
					nodeAvenant.setType(ContratTreeViewNode.TYPE_VIDE);
					_children.addObject(nodeAvenant);
				}
			}
/*			if (isRepertoireAvenants()) {
					NSArray avenants = contrat().avenants();
					avenants = EOSortOrdering.sortedArrayUsingKeyOrderArray(avenants, new NSArray(new Object[] {Avenant.SORT_INDEX_ASC}));
					
					Enumeration<Avenant>enumAvenants = avenants.objectEnumerator();
					while (enumAvenants.hasMoreElements()) {
						Avenant avenant = (Avenant) enumAvenants.nextElement();
						if (avenant.avtIndex().intValue() > 0) {
							ContratTreeViewNode nodeAvenant = new ContratTreeViewNode(contrat());
							nodeAvenant.setAvenant(avenant);
							nodeAvenant.setLibelle("Avt "+avenant.avtIndex());
							nodeAvenant.setParentNode(this);
							nodeAvenant.setType(ContratTreeViewNode.TYPE_AVENANT);
							_children.addObject(nodeAvenant);
						}
					}
					if (_children.count()==0) {
						ContratTreeViewNode nodeAvenant = new ContratTreeViewNode(contrat());
						nodeAvenant.setLibelle(ContratTreeViewNode.LIBELLE_VIDE);
						nodeAvenant.setParentNode(this);
						nodeAvenant.setType(ContratTreeViewNode.TYPE_VIDE);
						_children.addObject(nodeAvenant);
					}
				} else if (isRepertoireDocuments()) {
					if (avenant().avenantDocuments().count()>0) {
						Enumeration<AvenantDocument>enumDocuments = avenant().avenantDocuments().objectEnumerator();
						while (enumDocuments.hasMoreElements()) {
							AvenantDocument avenantDocument = (AvenantDocument) enumDocuments.nextElement();
							ContratTreeViewNode nodeDocument = new ContratTreeViewNode(contrat());
							nodeDocument.setAvenant(avenant());
							nodeDocument.setDocument(avenantDocument);
							nodeDocument.setLibelle(avenantDocument.document().couObjet());
							nodeDocument.setParentNode(this);
							nodeDocument.setType(ContratTreeViewNode.TYPE_DOCUMENT);
							_children.addObject(nodeDocument);
						}
					} else {
						ContratTreeViewNode nodeDocument = new ContratTreeViewNode(contrat());
						nodeDocument.setAvenant(avenant());
						nodeDocument.setLibelle(ContratTreeViewNode.LIBELLE_VIDE);
						nodeDocument.setParentNode(this);
						nodeDocument.setType(ContratTreeViewNode.TYPE_VIDE);
						_children.addObject(nodeDocument);
					}
				} else if (isAvenant()) {
					ContratTreeViewNode nodeRepertoireDocuments = new ContratTreeViewNode(contrat());
					nodeRepertoireDocuments.setLibelle(ContratTreeViewNode.LIBELLE_REP_DOCUMENTS);
					nodeRepertoireDocuments.setAvenant(avenant());
					nodeRepertoireDocuments.setParentNode(this);
					nodeRepertoireDocuments.setType(ContratTreeViewNode.TYPE_REPERTOIRE);
					_children.addObject(nodeRepertoireDocuments);
				} else if (isContrat()) {
					ContratTreeViewNode nodeRepertoireDocuments = new ContratTreeViewNode(contrat());
					nodeRepertoireDocuments.setLibelle(ContratTreeViewNode.LIBELLE_REP_DOCUMENTS);
					nodeRepertoireDocuments.setAvenant(contrat().avenantZero());
					nodeRepertoireDocuments.setParentNode(this);
					nodeRepertoireDocuments.setType(ContratTreeViewNode.TYPE_REPERTOIRE);
					_children.addObject(nodeRepertoireDocuments);
					ContratTreeViewNode nodeRepertoireAvenants = new ContratTreeViewNode(contrat());
					nodeRepertoireAvenants.setLibelle(ContratTreeViewNode.LIBELLE_REP_AVENANTS);
					nodeRepertoireAvenants.setParentNode(this);
					nodeRepertoireAvenants.setType(ContratTreeViewNode.TYPE_REPERTOIRE);
					_children.addObject(nodeRepertoireAvenants);
				}
*/			}
			return _children;
	}

	public static class Delegate implements AjaxTreeModel.Delegate {

		public NSArray childrenTreeNodes(Object node) {
			ContratTreeViewNode treeNode = (ContratTreeViewNode)node;
			return treeNode.childrenTreeNodes();

		}

		public boolean isLeaf(Object node) {
			ContratTreeViewNode treeNode = (ContratTreeViewNode)node;
//			return treeNode.childrenTreeNodes() == null;
			return !treeNode.isContrat() && !treeNode.isAvenant() && !treeNode.isRepertoireAvenants() && !treeNode.isRepertoireDocuments();
		}

		public Object parentTreeNode(Object node) {
			ContratTreeViewNode parentTreeNode = null;
			ContratTreeViewNode treeNode = (ContratTreeViewNode)node;
			if (treeNode != null) {
				parentTreeNode = treeNode.parentNode();
			}
			return parentTreeNode;
		}
		
	}


}
