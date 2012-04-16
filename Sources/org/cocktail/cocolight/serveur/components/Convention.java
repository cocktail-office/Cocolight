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

import java.util.Enumeration;

import org.cocktail.cocolight.serveur.util.EditionsCreditsPositionnesParConvention;
import org.cocktail.cocolight.serveur.util.EditionsCreditsPositionnesParLigneCredits;
import org.cocktail.cocolight.serveur.util.EditionsGeneralitesConvention;
import org.cocktail.cocolight.serveur.util.EditionsSuiviComptable;
import org.cocktail.cocolight.serveur.util.EditionsSuiviExec;
import org.cocktail.cocowork.common.exception.ExceptionFinder;
import org.cocktail.cocowork.common.exception.ExceptionUtilisateur;
import org.cocktail.cocowork.server.CocoworkApplicationUser;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.AvenantDocument;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.Parametre;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryAvenant;
import org.cocktail.cocowork.server.metier.convention.factory.FactoryConvention;
import org.cocktail.fwkcktldroitsutils.common.metier.EOUtilisateur;
import org.cocktail.fwkcktlpersonne.common.metier.EORepartAssociation;
import org.cocktail.fwkcktlpersonne.common.metier.EORepartStructure;
import org.cocktail.fwkcktlpersonne.common.metier.EOStructure;
import org.cocktail.fwkcktlreportingguiajax.serveur.CktlAbstractReporterAjaxProgress;
import org.cocktail.reporting.CktlAbstractReporter;
import org.cocktail.reporting.jrxml.IJrxmlReportListener;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class Convention extends MyWOComponent {

	private Contrat convention;
	private Avenant avenant;
	
	private Avenant unAvenant;
	private AvenantDocument unDocument, leDocumentSelectionne;

	private Recherche pageDeRecherche = null;
	private boolean refreshContratTreeView = false;
	private CktlAbstractReporter reporter;
	private ReporterAjaxProgress reporterProgress;
	private String reportFilename;
	private String messageValiderConvention;
	
	public Convention(WOContext context) {
        super(context);
    }
	
	/**
	 * @return the convention
	 */
	public Contrat convention() {
		return convention;
	}

	/**
	 * @param convention the convention to set
	 */
	public void setConvention(Contrat convention) {
		this.convention = convention;
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
	 * @return the isAfficherDetailConvention
	 */
	public boolean isAfficherDetailConvention() {
		return convention != null && avenant == null && leDocumentSelectionne == null;
	}

	public WOComponent afficherDetailConvention() {
		setAvenant(null);
		setLeDocumentSelectionne(null);
		
		return null;
	}
	/**
	 * @return the isAfficherDetailAvenant
	 */
	public boolean isAfficherDetailAvenant() {
		return convention != null && avenant != null && leDocumentSelectionne == null;
	}
	public WOComponent afficherDetailAvenant() {
		setAvenant(unAvenant());
		setLeDocumentSelectionne(null);
		
		return null;
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
	 * @return the unDocument
	 */
	public AvenantDocument unDocument() {
		return unDocument;
	}

	/**
	 * @param unDocument the unDocument to set
	 */
	public void setUnDocument(AvenantDocument unDocument) {
		this.unDocument = unDocument;
	}

	/**
	 * @return the leDocumentSelectionne
	 */
	public AvenantDocument leDocumentSelectionne() {
		return leDocumentSelectionne;
	}

	/**
	 * @param leDocumentSelectionne the leDocumentSelectionne to set
	 */
	public void setLeDocumentSelectionne(AvenantDocument leDocumentSelectionne) {
		this.leDocumentSelectionne = leDocumentSelectionne;
	}

	public boolean nePeutValiderAvenant() {
	    return avenant().avtDateSignature() == null || avenant().isSigne() 
	    		|| (!session().applicationUser().hasDroitValidationContratsEtAvenants()
	    		&& !session().applicationUser().hasDroitSuperAdmin());
	}

	public WOActionResults supprimerConvention() {
	    WOActionResults page = null;
        try {
            new FactoryConvention(convention().editingContext(), false).supprimerConvention(convention());
            convention().editingContext().saveChanges();
            page = pageWithName(Accueil.class.getName());
            session().addSimpleInfoMessage("Cocolight", "Convention supprimée avec succès");
        } catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        } catch (Exception e) {
            throw new NSForwardException(e);
        }
        return page;
    }
	
	public WOActionResults validerAvenant() {
        avenant().setAvtDateValidAdm(new NSTimestamp());
        try {
            convention().editingContext().saveChanges();
            session().addSimpleInfoMessage("Cocolight", "Avenant validé avec succès");
        } catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        } catch (Exception e) {
            throw new NSForwardException(e);
        }
        return null;
    }
	
	public String onClickAjouterUnAvenant() {
		String onClickAjouterUnAvenant = null;
		onClickAjouterUnAvenant = "openWinAjouterAvenant(this.href,'Ajouter un avenant');return false;";
		return onClickAjouterUnAvenant;
	}
	
	public WOActionResults creerUnNouvelAvenant() {
	    // On annule avant de faire notre cuisine
	    convention().editingContext().revert();
	    GestionAvenantLite nextPage = (GestionAvenantLite)pageWithName(GestionAvenantLite.class.getName());
	    FactoryAvenant fc = new FactoryAvenant(convention().editingContext(),application().isModeDebug());
	    Avenant newAvenant = null;
	    EOUtilisateur utilisateur = session().applicationUser().getUtilisateur();
	    try {
            newAvenant = fc.creerAvenantVierge(convention, null, null, utilisateur.localInstanceIn(convention().editingContext()));
        }
        catch (ExceptionUtilisateur e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        catch (ExceptionFinder e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
	    nextPage.setAvenant(newAvenant);
	    session().setIndexModuleActifCreationAvenant(null);
	    session().pageConvention().setRefreshContratTreeView(true);
	    return nextPage;
	}

	public String conventionLinkCssClass() {
		String conventionLinkCssClass = null;
		
		if (convention() != null && avenant() == null && leDocumentSelectionne() == null) {
			conventionLinkCssClass = "selected";
		}
		return conventionLinkCssClass;
	}

	public String avenantLinkCssClass() {
		String avenantLinkCssClass = null;
		
		if (convention() != null && avenant() != null && avenant().equals(unAvenant())) {
			avenantLinkCssClass = "selected";
		}
		return avenantLinkCssClass;
	}

	public boolean isAjouterAvenantDisabled() {
		boolean isAjouterAvenantDisabled = false;
		CocoworkApplicationUser user = session().applicationUser();
		if (convention() == null || !convention().isSigne() || leDocumentSelectionne() != null || 
		        ((!user.hasDroitCreationContratsEtAvenants() || !convention().isModifiablePar(user.getUtilisateur()))
		        && !session().applicationUser().hasDroitSuperAdmin())) {
			isAjouterAvenantDisabled = true;
		}
		return isAjouterAvenantDisabled;
	}
	
	public boolean isImprimerConventionDisabled() {
	    boolean isImprimerConvDisabled = false;
	    CocoworkApplicationUser user = session().applicationUser();
	    if (convention() == null || (!user.hasDroitImpressionGeneralites() && !session().applicationUser().hasDroitSuperAdmin())) {
	        isImprimerConvDisabled = true;
	    }
	    return isImprimerConvDisabled;
	}

	public String avenantIndex() {
		String avenantIndex = null;
		if (unAvenant() != null && unAvenant().avtIndex() != null) {
			if (unAvenant().avtIndex().intValue() == 0) {
				avenantIndex = "initial";
			} else {
				if (unAvenant().avtRefExterne() != null) {
					avenantIndex = unAvenant().avtRefExterne();
				} else {
					avenantIndex = unAvenant().avtIndex().toString();
				}
			}
		}
		return avenantIndex;
	}

	public boolean isNotAvenantInitial() {
		boolean isNotAvenantInitial = true;
		if (unAvenant().avtIndex().intValue() == 0) {
			isNotAvenantInitial = false;
		}
		return isNotAvenantInitial;
	}


	public WOActionResults afficherDetailDocument() {
		setAvenant(null);
		setLeDocumentSelectionne(unDocument());
		return null;
	}

	public boolean isAfficherDetailDocument() {
		return convention != null && leDocumentSelectionne != null;
	}

    public boolean nePeutValiderConvention() {
        return convention().avenantZero().avtDateSignature() == null || convention().isSigne() || 
                        (!session().applicationUser().hasDroitValidationContratsEtAvenants()
                        && !session().applicationUser().hasDroitSuperAdmin());
    }
    
    public boolean nePeutSupprimerConventionEtAvenants() {
        return convention().isValidAdm() || (!session().applicationUser().hasDroitSuppressionContratsEtAvenants()
        		&& !session().applicationUser().hasDroitSuperAdmin());
    }
    
    public boolean nePeutSupprimerAvenants() {
        return !session().applicationUser().hasDroitSuppressionContratsEtAvenants()
        		&& !session().applicationUser().hasDroitSuperAdmin();
    }
	
	public WOActionResults validerConvention() {
	    convention().setConDateValidAdm(new NSTimestamp());
        try {
            convention().editingContext().saveChanges();
            session().addSimpleInfoMessage("Cocolight", "Convention validée avec succès");
        } catch (ValidationException e) {
            session().addSimpleErrorMessage("Cocolight", e.getMessage());
        } catch (Exception e) {
            throw new NSForwardException(e);
        }
        return null;
    }
	
	public WOActionResults imprimerGeneralitesContrat() {
	    NSArray<EOGlobalID> ids = ERXEOControlUtilities.globalIDsForObjects(new NSArray(convention()));
        reporterProgress = new ReporterAjaxProgress(100);
        reportFilename = "ConventionsGeneralites.pdf";
        reporter = EditionsGeneralitesConvention.editionConventionsGeneralites(NSDictionary.EmptyDictionary, ids, 0, ERXEC.newEditingContext(), reporterProgress);
        return null;
    }
	
	public WOActionResults imprimerSuiviExecution() {
	    NSArray<Integer> ids = new NSArray(ERXEOControlUtilities.primaryKeyObjectForObject(convention()));
	    Integer exeOrdre = application().getDernierExerciceOuvertOuEnPreparation().exeExercice().intValue();
	    Integer utlOrdre = session().applicationUser().getUtilisateur().utlOrdre();
	    reporterProgress = new ReporterAjaxProgress(100);
	    reportFilename = "ConventionsExecution.pdf";
	    reporter = EditionsSuiviExec.editionConventionsSuiviExecution(ids, exeOrdre, utlOrdre, reporterProgress);
	    return null;
	}
	
	public WOActionResults imprimerSuiviComptable() {
	    NSArray<Integer> ids = new NSArray(ERXEOControlUtilities.primaryKeyObjectForObject(convention()));
	    Integer utlOrdre = session().applicationUser().getUtilisateur().utlOrdre();
	    reporterProgress = new ReporterAjaxProgress(100);
	    reportFilename = "ConventionsSuiviComptable.pdf";
	    reporter = EditionsSuiviComptable.editionConventionsSuiviComptable(ids, utlOrdre, reporterProgress);
	    return null;
	}
	
	public WOActionResults imprimerCreditsPositionnesParConvention() {
        NSArray<EOGlobalID> ids = ERXEOControlUtilities.globalIDsForObjects(new NSArray(convention()));
	    Integer utlOrdre = session().applicationUser().getUtilisateur().utlOrdre();
	    reporterProgress = new ReporterAjaxProgress(100);
	    reportFilename = "CreditsPositionnesParConvention.pdf";
	    reporter = EditionsCreditsPositionnesParConvention.editionCreditsPositionnesParConvention(NSDictionary.EmptyDictionary, ids, ERXEC.newEditingContext(), reporterProgress);
	    return null;
	}
	
	public WOActionResults imprimerCreditsPositionnesParLigneCredit() {
	    NSArray<EOGlobalID> ids = ERXEOControlUtilities.globalIDsForObjects(new NSArray(convention()));
	    Integer utlOrdre = session().applicationUser().getUtilisateur().utlOrdre();
	    reporterProgress = new ReporterAjaxProgress(100);
	    reportFilename = "CreditsPositionnesParLigneCredit.pdf";
	    reporter = EditionsCreditsPositionnesParLigneCredits.editionCreditsPositionnesParLigneCredits(NSDictionary.EmptyDictionary, ids, ERXEC.newEditingContext(), reporterProgress);
	    return null;
	}
	
	public WOActionResults accueil() {
		WOActionResults nextPage = pageWithName(Accueil.class.getName());
		session().reset();
		return nextPage;
	}

	public WOActionResults modifier() {

		try {
			convention().setUtilisateurModifRelationship(session().applicationUser().getUtilisateur());
			String libelleGroupe = "Partenaires de l'acte " + convention().numeroContrat();
			EOStructure groupePartenaires = convention().groupePartenaire();
			// Renommer le groupe partenaire
			groupePartenaires.setStrAffichage(libelleGroupe);
			// Maj des dates des differents roles des differents partenaires et contacts
			NSArray<EORepartStructure> partenairesEtContacts = groupePartenaires.toRepartStructuresElts();
			Enumeration<EORepartStructure> enumPartenairesEtContacts = partenairesEtContacts.objectEnumerator();
			while (enumPartenairesEtContacts.hasMoreElements()) {
				EORepartStructure eoRepartStructure = (EORepartStructure) enumPartenairesEtContacts.nextElement();
				NSArray<EORepartAssociation> lesRoles = eoRepartStructure.toRepartAssociations(null);
				Enumeration<EORepartAssociation> enumLesRoles = lesRoles.objectEnumerator();
				while (enumLesRoles.hasMoreElements()) {
					EORepartAssociation eoRepartAssociation = (EORepartAssociation) enumLesRoles.nextElement();
					eoRepartAssociation.setRasDOuverture(convention().dateDebut());
					eoRepartAssociation.setRasDFermeture(convention().dateFin());
				}
			}
			convention().editingContext().saveChanges();
		} catch (ValidationException e2) {
			context().response().setStatus(500);
			session().setMessageErreur(e2.getMessage());
		} catch (Throwable e1) {
			context().response().setStatus(500);
			edc().revert();
			throw NSForwardException._runtimeExceptionForThrowable(e1);
		}
		return null;
	}

	public boolean isRechercheDisabled() {
		return pageDeRecherche() == null;
	}
	public boolean isRechercheEnabled() {
		return !isRechercheDisabled();
	}


	public void setPageDeRecherche(Recherche pageDeRecherche) {
		this.pageDeRecherche = pageDeRecherche;		
	}
	public Recherche pageDeRecherche() {
		return pageDeRecherche;
	}

	public WOActionResults ajouterUnDocument() {
		GestionDocument nextPage = (GestionDocument)pageWithName(GestionDocument.class.getName());
		nextPage.setWithImmediateSave(Boolean.TRUE);
		Avenant avt = avenant();
		if (avt == null) {
			NSArray avenants = convention().avenants();
			avt = (Avenant)avenants.objectAtIndex(0);
//			avt = convention().avenantZero();
		}
		nextPage.setContrat(convention());
		nextPage.setUnAvenant(avt);
		session().pageConvention().setRefreshContratTreeView(true);
		return nextPage;
	}

	public boolean isAjouterDocumentDisabled() {
		return !isAjouterDocumentEnabled();
	}
	public boolean isAjouterDocumentEnabled() {
		boolean isAjouterDocumentEnabled = convention().isModifiablePar(session().applicationUser().getUtilisateur()) || session().applicationUser().hasDroitSuperAdmin();
		return isAjouterDocumentEnabled;
	}

	public boolean isVisualiserDocumentDisabled() {
		return !isVisualiserDocumentEnabled();
	}
	public boolean isVisualiserDocumentEnabled() {
		return session().applicationUser().hasDroitSuperAdmin() || convention().isModifiablePar(session().applicationUser().getUtilisateur()) || leDocumentSelectionne() != null;
	}

	public boolean isContratSelectionne() {		
		return convention() != null && avenant() == null && leDocumentSelectionne() == null;
	}

	public boolean isAvenantSelectionne() {		
		return avenant() != null && leDocumentSelectionne() == null && !avenant().equals(convention().avenantZero());
	}

	public WOActionResults supprimerUnAvenant() throws Exception {
		EOEditingContext ec = convention().editingContext();
		FactoryAvenant fc = new FactoryAvenant(ec, application().isModeDebug());
		try {
			fc.supprimerAvenant(avenant());
			ec.saveChanges();
			setAvenant(null);
			ec.refreshObject(convention);
			setRefreshContratTreeView(true);			
		} catch (Exception e) {
			e.printStackTrace();
			context().response().setStatus(500);
			session().setMessageErreur(e.getMessage());
		}
		return null;
	}

	public WOActionResults annulerCreationAvenant() {
	    convention().editingContext().revert();
        return null;
    }
	
	public boolean isDocumentSelectionne() {		
		return leDocumentSelectionne() != null;
	}

	public WOActionResults supprimerUnDocument() throws Exception {
		EOEditingContext ec = convention().editingContext();
		FactoryAvenant fc = new FactoryAvenant(ec, application().isModeDebug());
		try {
			fc.supprimerDocument(ec, leDocumentSelectionne(), session().applicationUser(), Integer.valueOf(application.config().intForKey("ROOT_GED_GROUPE_PARTENAIRE")));
			ec.saveChanges();
			setLeDocumentSelectionne(null);
			ec.refreshObject(convention);
			setRefreshContratTreeView(true);			
		} catch (Exception e) {
			e.printStackTrace();
			context().response().setStatus(500);
			session().setMessageErreur(e.getMessage());
		}
		return null;
	}

	public boolean isSupprimerDocumentDisabled() {
		return !isSupprimerDocumentEnabled();
	}

	public boolean isSupprimerDocumentEnabled() {
		return session().applicationUser().hasDroitSuperAdmin() || convention().isModifiablePar(session().applicationUser().getUtilisateur()) || session().applicationUser().hasDroitSuppressionContratsEtAvenants();
	}

	public String onClickAccueil() {
		String onClickAccueil = null;
		onClickAccueil = "return confirm('Attention, si des données ont été modifiées, elles ne seront pas enregistrées.\\nVoulez-vous vraiment retourner sur l\\'accueil ?');";
		
//		if (convention().editingContext().hasChanges()) {
//			onClickAccueil = "return confirm('Attention, si des données ont été modifiées, elles ne seront pas enregistrées.\\nVoulez-vous vraiment retourner sur l\\'accueil ?');";
//		}
		return onClickAccueil;
	}

	public boolean isModifierDisabled() {
		return convention() != null && !convention().isModifiablePar(session().applicationUser().getUtilisateur());
	}

	public WOActionResults annuler() {
		
		return accueil();
	}


	public boolean auMoinsUnAvenantSupplementaire() {
		boolean auMoinsUnAvenantSupplementaire = false;
		
		if (convention() != null && convention().avenants().count()>1) {
			auMoinsUnAvenantSupplementaire = true;
		}
		
		return auMoinsUnAvenantSupplementaire;
	}

	/**
	 * @return the refreshContratTreeView
	 */
	public boolean refreshContratTreeView() {
		return refreshContratTreeView;
	}

	/**
	 * @param refreshContratTreeView the refreshContratTreeView to set
	 */
	public void setRefreshContratTreeView(boolean refreshContratTreeView) {
		this.refreshContratTreeView = refreshContratTreeView;
	}

	public CktlAbstractReporter getReporter() {
        return reporter;
    }

	public CktlAbstractReporterAjaxProgress getReporterProgress() {
        return reporterProgress;
    }
	
	public static class ReporterAjaxProgress extends CktlAbstractReporterAjaxProgress implements IJrxmlReportListener {

        public ReporterAjaxProgress(int maximum) {
            super(maximum);
        }
	    
	}
	
	public String getReportFilename() {
        return reportFilename;
    }
	
	public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

	public String messageValiderConvention() {
	    if (messageValiderConvention == null) {
	        String message = "return confirm('";
	        if (Parametre.paramBooleanForKey(Parametre.GENERALITES_MODIFIABLES_APRES_VALIDATION)) {
	            message = message + "Êtes-vous sûr de vouloir valider cette convention ?";
	        } else {
	            message = message + "Après validation de la convention, les généralités ne seront plus modifiables, " +
	                                "êtes-vous sûr de vouloir continuer ?";
	        }
	        message = message + "')";
	        messageValiderConvention = message;
	    }
	    return messageValiderConvention;
	}
	
}
