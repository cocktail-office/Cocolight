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

import org.cocktail.cocolight.serveur.components.Accueil;
import org.cocktail.cocolight.serveur.components.Login;
import org.cocktail.cocolight.serveur.components.Timeout;
import org.cocktail.cocowork.common.exception.ExceptionFinder;
import org.cocktail.cocowork.server.CocoworkApplicationUser;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ContratPartContact;
import org.cocktail.cocowork.server.metier.convention.ContratPartenaire;
import org.cocktail.cocowork.server.metier.convention.Projet;
import org.cocktail.cocowork.server.metier.convention.finder.core.FinderConvention;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.ContactModalWin;
import org.cocktail.fwkcktlaccordsguiajax.components.assistants.PartenaireModalWin;
import org.cocktail.fwkcktlpersonne.common.metier.IPersonne;
import org.cocktail.fwkcktlpersonne.common.metier.PersonneDelegate;
import org.cocktail.fwkcktlwebapp.common.CktlLog;
import org.cocktail.fwkcktlwebapp.common.CktlUserInfo;
import org.cocktail.fwkcktlwebapp.common.database.CktlUserInfoDB;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;
import org.cocktail.fwkcktlwebapp.server.CktlWebAction;
import org.cocktail.fwkcktlwebapp.server.components.CktlAlertPage;
import org.cocktail.fwkcktlwebapp.server.components.CktlLogin;
import org.cocktail.fwkcktlwebapp.server.components.CktlLoginResponder;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class DirectAction extends CktlWebAction {

	private String loginComment;

	public DirectAction(WORequest request) {
		super(request);
	}

	protected Session laSession() {
		Session cngSession = (Session) existingSession();
		if (cngSession == null) {
			cngSession = (Session) session();
		}
		return cngSession;
	}

	public WOActionResults defaultAction() {
		if (useCasService())
			return loginCASPage();
		else
			return loginNoCasPage(null);
	}

	public WOActionResults mytestAction() {
		WOResponse result = new WOResponse();
		WORequest request = context().request();
		String persId = (String)request.formValueForKey("persId");
		String conId = (String)request.formValueForKey("conId");
		Session session = (Session)session();
		EOEditingContext ec = (EOEditingContext)session.defaultEditingContext();
		String content = "";
		Projet projet = Projet.projetForPjtId(ec, new Integer("1"));
		if (projet != null) {
			content += "Projet:"+projet.pjtLibelle();
			projet.projetContrats().count();
			content += "\n\t"+projet.eoDescription();
			content += "\n";
		}
		if (persId != null) {
			IPersonne personne = PersonneDelegate.fetchPersonneByPersId(ec, new Integer(persId));
			content += "Personne:"+personne.eoShallowDescription()+"\n"+"NOM: "+personne.getNomPrenomAffichage();
			content += "\n";
		}
		if (conId != null) {
			try {
				Contrat contrat = new FinderConvention(ec).findWithConOrdre(new Integer(conId));
				content += "Contrat:"+contrat.numeroContrat()+"\n"+"Description:"+contrat.eoDescription();
				content += "\n";
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExceptionFinder e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		result.setContent(content);
		return result.generateResponse();
	}
	
	public WOActionResults sessionExpiredAction() {
		// ((Session) session()).addNotification("Your session expired.");
		Timeout nextPage = (Timeout)pageWithName(Timeout.class.getName());
		return nextPage;
	}

	public WOActionResults applicationExceptionAction() {
		Accueil nextPage = (Accueil)pageWithName(Accueil.class.getName());		
		nextPage.setIsOpenFenetreException(true);
		return nextPage;
	}

	/**
	 * CAS : traitement authentification OK
	 */
	public WOActionResults loginCasSuccessPage(String netid) {
		try {
			CktlLog.log("loginCasSuccessPage : " + netid);
			ERXEC edc = (ERXEC)laSession().defaultEditingContext();
			IPersonne pers = PersonneDelegate.fetchPersonneForLogin(edc, netid);
			if (pers == null) {
				throw new Exception("Impossible de recuperer un objet personne associe au login " + netid);
			}
			CktlLog.log("loginCasSuccessPage : " + pers.persLibelle() + " " + pers.persLc());
			CocoworkApplicationUser appUser = new CocoworkApplicationUser(edc,Application.TYPE_APP_STR, Integer.valueOf(pers.persId().intValue()));
			laSession().setApplicationUser(appUser);
			laSession().setConnectedUserInfo(new CktlUserInfoDB(cktlApp.dataBus()));
			Accueil nextPage = (Accueil) pageWithName(Accueil.class.getName());
			return nextPage;

		} catch (Exception e) {
			e.printStackTrace();
			return getErrorPage(e.getMessage());
		}

	}
	public WOActionResults loginCasSuccessPage(String arg0, NSDictionary arg1) {
		return loginCasSuccessPage(arg0);
	}

	/**
	 * CAS : traitement authentification en echec
	 */
	public WOActionResults loginCasFailurePage(String errorMessage, String arg1) {
		CktlLog.log("loginCasFailurePage : " + errorMessage + " (" + arg1 + ")");
		StringBuffer msg = new StringBuffer();
		msg.append("Une erreur s'est produite lors de l'authentification de l'uilisateur&nbsp;:<br><br>");
		if (errorMessage != null)
			msg.append("&nbsp;:<br><br>").append(errorMessage);
		return getErrorPage(msg.toString());
	}

	@Override
	public WOActionResults loginNoCasPage(NSDictionary actionParams) {
		Login page = (Login)pageWithName(Login.class);
	    page.registerLoginResponder(getNewLoginResponder(actionParams));
	    return page;
	}
	
	public WOComponent getErrorPage(String errorMessage) {
		CktlAlertPage page = (CktlAlertPage) cktlApp.pageWithName("CktlAlertPage", context());
		page.showMessage(null, cktlApp.name() + " : ERREUR", errorMessage,
				null, null, null, CktlAlertPage.ERROR, null);
		return page;
	}
	public WOActionResults loginCASPage() {
		String url = DirectAction.getLoginActionURL(this.context(), false, null, true, null);
		WORedirect page = (WORedirect) pageWithName(WORedirect.class.getName());
		page.setUrl(url);
		return page;
	}

	
	public WOActionResults assistantPartenaireAction() {
		PartenaireModalWin nextPage = null;
		WORequest request = context().request();
		String conOrdre = (String)request.formValueForKey("conOrdre");
		String conPartOrdre = (String)request.formValueForKey("conPartOrdre");
		String persId = (String)request.formValueForKey("persId");
		Session session = (Session)session();
		if (persId != null && conOrdre != null) {
			CocoworkApplicationUser appUser = new CocoworkApplicationUser(session.defaultEditingContext(),Application.TYPE_APP_STR, Integer.valueOf(persId));
			try {
				appUser.checkUtilisateurValide(DateCtrl.now());
				if (appUser.getAllowedFonctions(null).count()==0) throw new Exception("Aucune fonctionnalité ouverte pour cet utilisateur.");
				session.setApplicationUser(appUser);
			} catch (Exception e) {
				// messageErreur = "Vous n'ètes pas autorisé(e) à utiliser cette application";
			}
			Contrat contrat = (Contrat)EOUtilities.objectWithPrimaryKeyValue(session.defaultEditingContext(), Contrat.ENTITY_NAME, Integer.valueOf(conOrdre));
			NSArray gids = ERXEOControlUtilities.globalIDsForObjects(new NSArray(contrat));
			session.defaultEditingContext().invalidateObjectsWithGlobalIDs(gids);
			nextPage = (PartenaireModalWin)pageWithName(PartenaireModalWin.class.getName());	
			session.setContrat(contrat);
			if (conPartOrdre != null) {
				ContratPartenaire partenaire = (ContratPartenaire)EOUtilities.objectWithPrimaryKeyValue(session.defaultEditingContext(), ContratPartenaire.ENTITY_NAME, Integer.valueOf(conPartOrdre));
				nextPage.setPartenaire(partenaire);
			} else {
				nextPage.setPartenaire(null);
			}
			session.setIndexModuleActifGestionPartenaire(null);
		}
		return nextPage;
	}
	
	public WOActionResults assistantContactAction() {
		ContactModalWin nextPage = null;
		WORequest request = context().request();
		String conOrdre = (String)request.formValueForKey("conOrdre");
		String conPartOrdre = (String)request.formValueForKey("conPartOrdre");
		String conPartContactOrdre = (String)request.formValueForKey("conPartContactOrdre");
		String persId = (String)request.formValueForKey("persId");
		Session session = (Session)session();
		if (persId != null && conOrdre != null && conPartOrdre != null) {
			CocoworkApplicationUser appUser = new CocoworkApplicationUser(session.defaultEditingContext(),Application.TYPE_APP_STR, Integer.valueOf(persId));
			try {
				appUser.checkUtilisateurValide(DateCtrl.now());
				if (appUser.getAllowedFonctions(null).count()==0) throw new Exception("Aucune fonctionnalité ouverte pour cet utilisateur.");
				session.setApplicationUser(appUser);
			} catch (Exception e) {
				// messageErreur = "Vous n'ètes pas autorisé(e) à utiliser cette application";
			}
			Contrat contrat = (Contrat)EOUtilities.objectWithPrimaryKeyValue(session.defaultEditingContext(), Contrat.ENTITY_NAME, Integer.valueOf(conOrdre));
            NSArray gids = ERXEOControlUtilities.globalIDsForObjects(new NSArray(contrat));
            session.defaultEditingContext().invalidateObjectsWithGlobalIDs(gids);
			ContratPartenaire partenaire = (ContratPartenaire)EOUtilities.objectWithPrimaryKeyValue(session.defaultEditingContext(), ContratPartenaire.ENTITY_NAME, Integer.valueOf(conPartOrdre));
			nextPage = (ContactModalWin)pageWithName(ContactModalWin.class.getName());	
			nextPage.setPartenaire(partenaire);
			session.setContrat(contrat);
			if (conPartContactOrdre != null) {
				ContratPartContact contact = (ContratPartContact)EOUtilities.objectWithPrimaryKeyValue(session.defaultEditingContext(), ContratPartContact.ENTITY_NAME, Integer.valueOf(conPartContactOrdre));
				nextPage.setContact(contact);
			} else {
				nextPage.setContact(null);
			}
			session.setIndexModuleActifGestionContact(null);
		}
		return nextPage;
	}
	
	public String loginComment() {
		return loginComment; 
	}

	public void setLoginComment(String comment) {
		loginComment = comment;
	}

	public String casLoginLink() {
		return null;
	}

	public WOActionResults validerLoginAction() {
		WOActionResults page = null;
		WORequest request = context().request();
		String login = StringCtrl.normalize((String)request.formValueForKey("identifiant"));
		String password = StringCtrl.normalize((String)request.formValueForKey("mot_de_passe"));
		String messageErreur = null;
		Session session = (Session)context().session();

		CktlLoginResponder loginResponder = getNewLoginResponder(null);
		CktlUserInfo loggedUserInfo = new CktlUserInfoDB(cktlApp.dataBus());
		if (login.length() == 0) {
			messageErreur = "Vous devez renseigner l'identifiant.";
		} else if (!loginResponder.acceptLoginName(login)) {
			messageErreur = "Vous n'ètes pas autorisé(e) à utiliser cette application";
		} else {
			if (password == null) password = "";
			loggedUserInfo.setRootPass(loginResponder.getRootPassword());
			loggedUserInfo.setAcceptEmptyPass(loginResponder.acceptEmptyPassword());
			loggedUserInfo.compteForLogin(login, password, true);
			if (loggedUserInfo.errorCode() != CktlUserInfo.ERROR_NONE) {
				if (loggedUserInfo.errorMessage() != null)
					messageErreur = loggedUserInfo.errorMessage();
				CktlLog.rawLog(">> Erreur | "+loggedUserInfo.errorMessage());
			}
		}

		if (messageErreur == null) {
			session.setConnectedUserInfo(loggedUserInfo);
			String erreur = session.setConnectedUser(loggedUserInfo.login());
			if (erreur != null) {
				messageErreur = erreur;
			} else {
//			    CocolightApplicationUser appUser2 = new CocolightApplicationUser(loggedUserInfo.persId().intValue());
				CocoworkApplicationUser appUser = new CocoworkApplicationUser(session.defaultEditingContext(),Application.TYPE_APP_STR, Integer.valueOf(loggedUserInfo.persId().intValue()));
				try {
					appUser.checkUtilisateurValide(DateCtrl.now());
					if (appUser.getAllowedFonctions(null).count()==0) throw new Exception("Aucune fonctionnalité ouverte pour cet utilisateur.");
					session.setApplicationUser(appUser);
//					 if (!appUser2.getCocoworkAutorisationCache().hasDroitConsultationContrat())
//					     throw new RuntimeException();
				} catch (Exception e) {
					messageErreur = "Vous n'ètes pas autorisé(e) à utiliser cette application";
				}
			}
		}

		if (messageErreur != null) {
			page = (Login)pageWithName(Login.class.getName());
			((Login)page).setMessageErreur(messageErreur);
			// if (session != null) session.terminate();
			return page;
		}

		return loginResponder.loginAccepted(null);

	}

	public WOActionResults mailUsersAction() {
		String destinataire = cktlApp.config().stringForKey("ADMIN_MAIL");
		WORequest req = request();
		if (req.formValueForKey("dest")!=null) {
			destinataire = (String)req.formValueForKey("dest");
		}
		cktlApp.mailBus().sendMail(destinataire,
				cktlApp.config().stringForKey("ADMIN_MAIL"),null,
				"["+cktlApp.name()+"]Utilisateur connectés à l'application ",
				"Liste des emails : \n"+((Application)WOApplication.application()).utilisateurs().componentsJoinedByString(","));
		return defaultAction();
	}

	public CktlLoginResponder getNewLoginResponder(NSDictionary actionParams) {
		return new DefaultLoginResponder(actionParams);
	}

	public class DefaultLoginResponder implements CktlLoginResponder {
		private NSDictionary actionParams;

		public DefaultLoginResponder(NSDictionary actionParams) {
			this.actionParams = actionParams;
		}

		public NSDictionary actionParams() {
			return actionParams;
		}

		public WOComponent loginAccepted(CktlLogin loginComponent) {
			WOComponent nextPage = null;
			nextPage = cktlApp.pageWithName(Accueil.class.getName(), context());
			return nextPage;
		}

		public boolean acceptLoginName(String loginName) {
			return cktlApp.acceptLoginName(loginName);
		}

		public boolean acceptEmptyPassword() {
			return cktlApp.config().booleanForKey("ACCEPT_EMPTY_PASSWORD");
		}

		public String getRootPassword() {
			return cktlApp.getRootPassword();
		}
	}
}
