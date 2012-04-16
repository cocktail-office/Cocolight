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
package org.cocktail.cocolight.serveur;

import org.cocktail.cocolight.serveur.components.Convention;
import org.cocktail.cocowork.server.CocoworkApplicationUser;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.fwkcktlajaxwebext.serveur.CocktailAjaxSession;
import org.cocktail.fwkcktlajaxwebext.serveur.component.CktlAjaxWindow;
import org.cocktail.fwkcktlpersonne.common.PersonneApplicationUser;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;


public class Session extends CocktailAjaxSession {
	
	private static final long serialVersionUID = 1L;
	private static final int DatabaseChannelCountMax = 3;
	
	private NSDictionary exceptionInfos;
	private String messageErreur;
    private String generalErrorMessage;

	private CocoworkApplicationUser applicationUser=null;
	
	private Convention pageConvention = null;
	
	private Contrat contrat = null;
	private NSArray lesEtablissementsAffectation = null;
	private NSArray lesServicesAffectation = null;
	private Integer indexModuleActifCreationConvention = null;
	private NSArray modulesCreationConvention;
	private Integer indexModuleActifCreationAvenant = null;
	private NSArray modulesCreationAvenant;
	private Integer indexModuleActifGestionPartenaire = null;
	private NSArray modulesGestionPartenaire;
	private Integer indexModuleActifGestionContact = null;
	private NSArray modulesGestionContact;
	
	private NSTimestamp dateSignatureTmp;
	private NSTimestamp dateSignatureAvenantTmp;
	
	public Session() {
		  super();
	      NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("registerNewDatabaseChannel", new Class[] { NSNotification.class } ),EODatabaseContext.DatabaseChannelNeededNotification, null);
	      // Initialisation du theme applique a toutes les fenetres gerees via CktlAjaxWindow
	      setWindowsClassName(CktlAjaxWindow.WINDOWS_CLASS_NAME_BLUELIGHTING);
//	      defaultEditingContext().setDelegate(this);
	}

	public void registerNewDatabaseChannel(NSNotification notification) {
        EODatabaseContext databaseContext = (EODatabaseContext)notification.object();
        if (databaseContext.registeredChannels().count() < DatabaseChannelCountMax) {
            EODatabaseChannel channel = new EODatabaseChannel(databaseContext);
            databaseContext.registerChannel(channel);
        }
    }

	
	public void terminate() {
		NSMutableArray users = ((Application) WOApplication.application()).utilisateurs();
		if (users != null && users.count() > 0) {
			if (applicationUser() != null && applicationUser().getEmails() != null) {
				users.removeObjectsInArray(applicationUser().getEmails());
			}
		}
		super.terminate();
	}
	public void setApplicationUser(CocoworkApplicationUser appUser) {
		this.applicationUser=appUser;	
		if (appUser != null) {
			NSArray emails = appUser.getEmails();
			if (emails != null && emails.count()>0) {
				((Application)application()).utilisateurs().addObjectsFromArray(emails);
			}
		}
	}
	public CocoworkApplicationUser applicationUser() {
		return applicationUser;
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

	public NSArray lesEtablissementsAffectation() {
		if (lesEtablissementsAffectation == null) {
			PersonneApplicationUser persAppUser = new PersonneApplicationUser(defaultEditingContext(),applicationUser().getPersId());
			lesEtablissementsAffectation = persAppUser.getEtablissementsAffectation();
		}
		return lesEtablissementsAffectation;
		
	}

	public NSArray lesServicesAffectation() {
		if (lesServicesAffectation == null) {
			PersonneApplicationUser persAppUser = new PersonneApplicationUser(defaultEditingContext(),applicationUser().getPersId());
			lesServicesAffectation = persAppUser.getServices();
		}
		return lesServicesAffectation;
		
	}

	/**
	 * @return the indexModuleActifCreationConvention
	 */
	public Integer indexModuleActifCreationConvention() {
		if (indexModuleActifCreationConvention == null) {
			indexModuleActifCreationConvention = new Integer(0);
		}
		return indexModuleActifCreationConvention;
	}

	/**
	 * @param indexModuleActifCreationConvention the indexModuleActifCreationConvention to set
	 */
	public void setIndexModuleActifCreationConvention(Integer indexModuleActifCreationConvention) {
		this.indexModuleActifCreationConvention = indexModuleActifCreationConvention;
	}

	/**
	 * @return the modulesCreationConvention
	 */
	public NSArray modulesCreationConvention() {
		return modulesCreationConvention;
	}

	/**
	 * @param modulesCreationConvention the modulesCreationConvention to set
	 */
	public void setModulesCreationConvention(NSArray modulesCreationConvention) {
		this.modulesCreationConvention = modulesCreationConvention;
	}
	
	/**
	 * @return the indexModuleActifCreationAvenant
	 */
	public Integer indexModuleActifCreationAvenant() {
		if (indexModuleActifCreationAvenant == null) {
			indexModuleActifCreationAvenant = new Integer(0);
		}
		return indexModuleActifCreationAvenant;
	}

	/**
	 * @param indexModuleActifCreationAvenant the indexModuleActifCreationAvenant to set
	 */
	public void setIndexModuleActifCreationAvenant(Integer indexModuleActifCreationAvenant) {
		this.indexModuleActifCreationAvenant = indexModuleActifCreationAvenant;
	}

	/**
	 * @return the modulesCreationAvenant
	 */
	public NSArray modulesCreationAvenant() {
		return modulesCreationAvenant;
	}

	/**
	 * @param modulesCreationAvenant the modulesCreationAvenant to set
	 */
	public void setModulesCreationAvenant(NSArray modulesCreationAvenant) {
		this.modulesCreationAvenant = modulesCreationAvenant;
	}

	/**
	 * @return the indexModuleActifGestionPartenaire
	 */
	public Integer indexModuleActifGestionPartenaire() {
		if (indexModuleActifGestionPartenaire == null) {
			indexModuleActifGestionPartenaire = new Integer(0);
		}
		return indexModuleActifGestionPartenaire;
	}

	/**
	 * @param indexModuleActifGestionPartenaire the indexModuleActifGestionPartenaire to set
	 */
	public void setIndexModuleActifGestionPartenaire(Integer indexModuleActifGestionPartenaire) {
		this.indexModuleActifGestionPartenaire = indexModuleActifGestionPartenaire;
	}

	/**
	 * @return the modulesGestionPartenaire
	 */
	public NSArray modulesGestionPartenaire() {
		return modulesGestionPartenaire;
	}

	/**
	 * @param modulesGestionPartenaire the modulesGestionPartenaire to set
	 */
	public void setModulesGestionPartenaire(NSArray modulesGestionPartenaire) {
		this.modulesGestionPartenaire = modulesGestionPartenaire;
	}
	
	/**
	 * @return the indexModuleActifGestionContact
	 */
	public Integer indexModuleActifGestionContact() {
		if (indexModuleActifGestionContact == null) {
			indexModuleActifGestionContact = new Integer(0);
		}
		return indexModuleActifGestionContact;
	}

	/**
	 * @param indexModuleActifGestionContact the indexModuleActifGestionContact to set
	 */
	public void setIndexModuleActifGestionContact(Integer indexModuleActifGestionContact) {
		this.indexModuleActifGestionContact = indexModuleActifGestionContact;
	}

	/**
	 * @return the modulesGestionContact
	 */
	public NSArray modulesGestionContact() {
		return modulesGestionContact;
	}

	/**
	 * @param modulesGestionContact the modulesGestionContact to set
	 */
	public void setModulesGestionContact(NSArray modulesGestionContact) {
		this.modulesGestionContact = modulesGestionContact;
	}
	
	public WOActionResults onQuitter() {
		return goToMainSite();
	}

	public void reset() {
		if (defaultEditingContext() != null) {
			defaultEditingContext().revert();
		}
		contrat = null;
		exceptionInfos = null;
		messageErreur = null;
		indexModuleActifCreationConvention = null;		
		indexModuleActifGestionContact = null;		
		indexModuleActifGestionPartenaire = null;	
		pageConvention = null;
		// on clean le cache 
		// cleanPageReplacementCacheWithAHammer();
	}
	
	
	public NSDictionary exceptionInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param exceptionInfos the exceptionInfos to set
	 */
	public void setExceptionInfos(NSDictionary exceptionInfos) {
		this.exceptionInfos = exceptionInfos;
	}

	/**
	 * @return the messageErreur
	 */
	public String messageErreur() {
		if (messageErreur != null) {
			messageErreur = StringCtrl.trimText(messageErreur);
		}
		return messageErreur;
	}

	/**
	 * @param messageErreur the messageErreur to set
	 */
	public void setMessageErreur(String messageErreur) {
		this.messageErreur = messageErreur;
		super.addSimpleErrorMessage("Cocolight", messageErreur);
	}

	/**
	 * @return the pageConvention
	 */
	public Convention pageConvention() {
		return pageConvention;
	}

	/**
	 * @param pageConvention the pageConvention to set
	 */
	public void setPageConvention(Convention pageConvention) {
		this.pageConvention = pageConvention;
	}
	
	public String getGeneralErrorMessage() {
        return generalErrorMessage;
    }
	
	public void setGeneralErrorMessage(String generalErrorMessage) {
        this.generalErrorMessage = generalErrorMessage;
    }
	
}
