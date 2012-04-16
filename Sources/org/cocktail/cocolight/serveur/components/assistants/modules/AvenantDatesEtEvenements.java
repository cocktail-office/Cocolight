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

import org.cocktail.cocowork.server.metier.convention.AvenantEvenement;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumn;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumnAssociation;
import org.cocktail.fwkcktlevenement.serveur.modele.EOEvenement;
import org.cocktail.fwkcktlevenementguiajax.serveur.components.EvenementForm.PersonnesTbvHelper;
import org.cocktail.fwkcktlevenementguiajax.serveur.components.EvenementsUI.Delegate;
import org.cocktail.fwkcktlpersonne.common.metier.EOAssociation;
import org.cocktail.fwkcktlpersonne.common.metier.EOIndividu;
import org.cocktail.fwkcktlpersonne.common.metier.EORepartAssociation;
import org.cocktail.fwkcktlpersonne.common.metier.EORepartStructure;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;
import org.cocktail.fwkcktlpersonne.common.metier.IPersonne;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.eof.ERXEOControlUtilities;

public class AvenantDatesEtEvenements extends ModuleAssistant {

    private static final long serialVersionUID = 1500617339986468811L;
    private  ERXDisplayGroup<EOEvenement> dg;
    private Delegate delegate = new MyDelegate();
    private PersonnesContactTbvHelper personnesContactTbvHelper = new PersonnesContactTbvHelper();

    public AvenantDatesEtEvenements(WOContext context) {
        super(context);
    }

    public String disabledTFScript() {
        String disabledTFScript = "";

        if (disabled()) {
            disabledTFScript = "$('FormAvenantDatesEtEvenements').disable();";
        }

        return disabledTFScript;
    }

    public ERXDisplayGroup<EOEvenement> dg() {
        if (dg == null) {
            dg = new ERXDisplayGroup<EOEvenement>();
        }
        dg.setObjectArray(avenant().evenements());
        return dg;
    }

    public Delegate getDelegate() {
        return delegate;
    }
    
    public PersonnesContactTbvHelper getPersonnesContactTbvHelper() {
        return personnesContactTbvHelper;
    }
    
    public class MyDelegate implements Delegate {
        public void evenementCreated(EOEvenement evenement) {
            if (evenement != null) {
                EOEditingContext ec = avenant().editingContext();
                EOEvenement ev = evenement.localInstanceIn(ec);
                AvenantEvenement avEv = AvenantEvenement.create(ec, null, null);
                avEv.setAvenantRelationship(avenant());
                avEv.setEvenementRelationship(ev);
                avenant().addToAvenantEvenementsRelationship(avEv);
                dg = null;
            }
        }

        public void deleteEvenement(EOEvenement evenement) {
            if (evenement != null) {
                EOEditingContext ec = avenant().editingContext();
                EOEvenement ev = evenement.localInstanceIn(ec);
                //EOEvenement.supprimerEvenement(ev, ec);
                avenant().removeFromEvenementsRelationship(ev);
                dg = null;
            }
        }

        public void evenementInserted(EOEvenement evenement) {
            if (evenement != null) {
                EOEditingContext ec = evenement.editingContext();
                EOStructure groupePartenaires = avenant().contrat().groupePartenaire();
                if (groupePartenaires != null) {
                    NSArray<EORepartStructure> partenairesEtContacts = groupePartenaires.toRepartStructuresElts();
                    NSMutableArray<IPersonne> individus = new NSMutableArray<IPersonne>();
                    for (EORepartStructure rep : partenairesEtContacts) {
                        EOIndividu indiv = (EOIndividu)rep.toIndividuElts().lastObject();
                        if (indiv != null)
                            individus.addObject(indiv);
                    }
                    evenement.addToPersonnes(ERXEOControlUtilities.localInstancesOfObjects(ec, individus));
                }
            }
        }
    }
    
    public class PersonnesContactTbvHelper extends PersonnesTbvHelper {
        @Override
        public NSArray<CktlAjaxTableViewColumn> getColonnesPersonnes() {
                if (colonnesPersonnes == null) {
                    String numKey = CurrentPersonneKey + IPersonne.NUMERO_KEY;
                    String nomPrenomKey = CurrentPersonneKey + IPersonne.NOM_PRENOM_AFFICHAGE_KEY;
                    String roleContactKey = HelperKey + "roleContact";
                    colonnesPersonnes = new NSMutableArray<CktlAjaxTableViewColumn>();
                    CktlAjaxTableViewColumn col1 = new CktlAjaxTableViewColumn();
                    col1.setOrderKeyPath(IPersonne.NUMERO_KEY);
                    col1.setLibelle("Numéro");
                    CktlAjaxTableViewColumnAssociation ass1 = new CktlAjaxTableViewColumnAssociation(numKey, "");
                    col1.setAssociations(ass1);
                    colonnesPersonnes.addObject(col1);
                    CktlAjaxTableViewColumn col2 = new CktlAjaxTableViewColumn();
                    col2.setOrderKeyPath(IPersonne.NOM_PRENOM_AFFICHAGE_KEY);
                    col2.setLibelle("Nom");
                    CktlAjaxTableViewColumnAssociation ass2 = new CktlAjaxTableViewColumnAssociation(nomPrenomKey, "");
                    col2.setAssociations(ass2);
                    colonnesPersonnes.addObject(col2);
                    CktlAjaxTableViewColumn col3 = new CktlAjaxTableViewColumn();
                    col3.setOrderKeyPath(null);
                    col3.setLibelle("Rôles dans la convention");
                    CktlAjaxTableViewColumnAssociation ass3 = new CktlAjaxTableViewColumnAssociation(roleContactKey, "");
                    col3.setAssociations(ass3);
                    colonnesPersonnes.addObject(col3);
                }
                return colonnesPersonnes;
        }
        
        public String getRoleContact() {
            EOStructure groupePartenaires = avenant().contrat().groupePartenaire();
            if (groupePartenaires != null) {
                NSArray<EORepartStructure> partenairesEtContacts = groupePartenaires.toRepartStructuresElts();
                for (EORepartStructure rep : partenairesEtContacts) {
                    EOIndividu indiv = (EOIndividu)rep.toIndividuElts().lastObject();
                    if (indiv != null && indiv.persId() != null && indiv.persId().equals(getCurrentPersonne().persId())) {
                        NSArray<EORepartAssociation> repsAssoc = rep.toRepartAssociations();
                        NSArray<String> roles = (NSArray<String>) repsAssoc.valueForKey(EORepartAssociation.TO_ASSOCIATION_KEY + "." + EOAssociation.ASS_LIBELLE_KEY);
                        return roles.componentsJoinedByString(", ");
                    }
                }
                
            }
            return "";
        }
        
    }
    
}
