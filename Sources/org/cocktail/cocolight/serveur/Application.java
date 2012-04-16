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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.cocktail.cocowork.server.metier.convention.TypeClassificationContrat;
import org.cocktail.cocowork.server.metier.convention.TypeStat;
import org.cocktail.fwkcktlajaxwebext.serveur.CocktailAjaxApplication;
import org.cocktail.fwkcktlevenement.FwkCktlEvenementPrincipal;
import org.cocktail.fwkcktljefyadmin.common.finder.FinderExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTva;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeEtat;
import org.cocktail.fwkcktlpersonne.common.metier.EODomaineScientifique;
import org.cocktail.fwkcktlpersonne.common.metier.EOGrhumParametres;
import org.cocktail.fwkcktlpersonne.common.metier.EONaf;
import org.cocktail.fwkcktlwebapp.common.CktlLog;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.server.CktlConfig;
import org.cocktail.fwkcktlwebapp.server.CktlMailBus;
import org.cocktail.fwkcktlwebapp.server.util.EOModelCtrl;
import org.cocktail.fwkcktlwebapp.server.version.A_CktlVersion;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCContext;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXMessageEncoding;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXProperties;

public class Application extends CocktailAjaxApplication {

	public static final String TYPE_APP_STR = "COCONUT"; // ID de l'application
															// dans JefyAdmin
	private static NSMutableArray utilisateurs; // Liste des emails des
												// utilisateurs connectés
	private static final String CONFIG_FILE_NAME = VersionMe.APPLICATIONINTERNALNAME
			+ ".config";
	private static final String CONFIG_TABLE_NAME = "FwkCktlWebApp_GrhumParametres";
	private static final String MAIN_MODEL_NAME = "Cocowork";
	/**
	 * Liste des parametres obligatoires (dans fichier de config ou table
	 * grhum_parametres) pour que l'application se lance. Si un des parametre
	 * n'est pas initialise, il y a une erreur bloquante.
	 */
	public static final String[] MANDATORY_PARAMS = new String[] {
			"ROOT_GED_GROUPE_PARTENAIRE", "GRHUM_HOST_MAIL", "ADMIN_MAIL" };

	/**
	 * Liste des parametres optionnels (dans fichier de config ou table
	 * grhum_parametres). Si un des parametre n'est pas initialise, il y a un
	 * warning.
	 */
	public static final String[] OPTIONAL_PARAMS = new String[] {};
	
	/**
	 * ID du container permettant d'afficher les messages via pnotify
	 */
	private static final String MESSAGE_CONTAINER_ID = "Cocolight_MessageContainer";

	/**
	 * Fonction JS permettant l'update du container de messages 
	 */
	private static final String ON_FAILURE_MESSAGE = "function () {"+MESSAGE_CONTAINER_ID+"Update();}";

	/**
	 * Mettre a true pour que votre application renvoie les informations de
	 * collecte au serveur de collecte de Cocktail.
	 */
	public static boolean APP_SHOULD_SEND_COLLECTE = false;

	public static String ServerBdIds;
	
	/**
	 * boolean qui indique si on se trouve en mode developpement ou non. Permet
	 * de desactiver l'envoi de mail lors d'une exception par exemple
	 */
	private Boolean isModeDebug = false;
	private Version _appVersion;

	public static NSTimeZone ntz = null;
	/**
	 * Formatteur a deux decimales e utiliser pour les données numériques non
	 * monétaires.
	 */
	public NSNumberFormatter app2DecimalesFormatter;
	/**
	 * Formatteur a 5 decimales a utiliser pour les pourcentages dans la
	 * repartition.
	 */
	public NSNumberFormatter app5DecimalesFormatter;

	/**
	 * Formatteur de dates.
	 */
	public NSTimestampFormatter appDateFormatter;

	private Integer racineGedi;

	private EOExercice dernierExerciceOuvertOuEnPreparation;
	
	public static void main(String[] argv) {
	    FwkCktlEvenementPrincipal.setUpFramework(null);
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		super();

		setAllowsConcurrentRequestHandling(false);
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
		setPageRefreshOnBacktrackEnabled(true);
		WOMessage.setDefaultEncoding("UTF-8");
		WOMessage.setDefaultURLEncoding("UTF-8");
		ERXMessageEncoding.setDefaultEncoding("UTF8");
		ERXMessageEncoding.setDefaultEncodingForAllLanguages("UTF8");
		utilisateurs = new NSMutableArray();
		// setupDatabaseChannelCloserTimer();
		// ERXEC.setDefaultFetchTimestampLag(2000);
		dernierExerciceOuvertOuEnPreparation = FinderExercice.getDernierExerciceOuvertOuEnPreparation(
		        EOSharedEditingContext.defaultSharedEditingContext());
		if (dernierExerciceOuvertOuEnPreparation == null)
		    throw new IllegalStateException("Aucun exercice ouvert trouvé dans JEFY_ADMIN.EXERCICE");
		// On charge le chemin vers la conf jasper...
		URL urlToJasperProperties = resourceManager().pathURLForResourceNamed("Reports/ConventionsGeneralites/jasperreports.properties", "app", null);
		System.setProperty("net.sf.jasperreports.properties", urlToJasperProperties.toString());
	}

	public void initApplication() {
		System.out.println("Lancement de l'application serveur " + this.name() + "...");
		super.initApplication();

		CktlLog.rawLog("WOFrameworksBaseURL: "+frameworksBaseURL());
		// Afficher les infos de connexion des modeles de donnees
		rawLogModelInfos();
		// Verifier la coherence des dictionnaires de connexion des modeles de donnees
		boolean isInitialisationErreur = !checkModel();

		isModeDebug = (Application.isDevelopmentModeSafe() || config().booleanForKey("MODE_DEBUG"));
		Application.APP_SHOULD_SEND_COLLECTE = !Application.isDevelopmentModeSafe();
		setPageCacheSize(10);
	}

	/**
	 * Execute les operations au demarrage de l'application, juste apres
	 * l'initialisation standard de l'application.
	 */
	public void startRunning() {
		EODatabaseContext.setDefaultDelegate(this);
		initFormatters();
		initTimeZones();
		this.appDateFormatter = new NSTimestampFormatter();
		this.appDateFormatter.setPattern("%d/%m/%Y");

		// Prefetch dans le sharedEditingContext des nomenclatures communes a
		// toute l'appli
		EOSharedEditingContext sedc = EOSharedEditingContext.defaultSharedEditingContext();
		EOSortOrdering cNafSortOrdering = EOSortOrdering.sortOrderingWithKey(EONaf.C_NAF_KEY, EOSortOrdering.CompareAscending);
		EOFetchSpecification nafSpecFetch = new EOFetchSpecification(EONaf.ENTITY_NAME, null, new NSArray(cNafSortOrdering));
		nafSpecFetch.setUsesDistinct(true);
		sedc.bindObjectsWithFetchSpecification(nafSpecFetch, "FetchAll");
		EOFetchSpecification fetchSpec = ERXFetchSpecification.fetchSpecificationNamed("FetchAll",TypeClassificationContrat.ENTITY_NAME);
		sedc.bindObjectsWithFetchSpecification(fetchSpec, "FetchAll");
		EOFetchSpecification tsfetchSpec = ERXFetchSpecification.fetchSpecificationNamed("FetchAll",TypeStat.ENTITY_NAME);
		sedc.bindObjectsWithFetchSpecification(tsfetchSpec, "FetchAll");
		EOSortOrdering tvaSortOrdering = EOSortOrdering.sortOrderingWithKey(EOTva.TVA_TAUX_KEY, EOSortOrdering.CompareAscending);
        EOFetchSpecification typeEtatsFs = new ERXFetchSpecification(EOTypeEtat.ENTITY_NAME, null, null);
        sedc.objectsWithFetchSpecification(typeEtatsFs);
        EOQualifier valides = ERXQ.equals(EOTva.TYPE_ETAT_KEY + "." +EOTypeEtat.TYET_LIBELLE_KEY, EOTypeEtat.ETAT_VALIDE);
		EOFetchSpecification tvafetchSpec = new ERXFetchSpecification(EOTva.ENTITY_NAME, valides, new NSArray(tvaSortOrdering));
		sedc.objectsWithFetchSpecification(tvafetchSpec, sedc);
		EODomaineScientifique.fetchAll(sedc, ERXS.ascs(EODomaineScientifique.DS_CODE_KEY));
	}
	

	public EOExercice getDernierExerciceOuvertOuEnPreparation() {
        return dernierExerciceOuvertOuEnPreparation;
    }
	
	public NSArray lesTauxDeTVA() {
		return (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(EOTva.ENTITY_NAME);
	}
	public String configFileName() {
		return CONFIG_FILE_NAME;
	}

	public String configTableName() {
		return CONFIG_TABLE_NAME;
	}

	public String[] configMandatoryKeys() {
		return MANDATORY_PARAMS;
	}

	public String[] configOptionalKeys() {
		return OPTIONAL_PARAMS;
	}

	public boolean appShouldSendCollecte() {
		return APP_SHOULD_SEND_COLLECTE;
	}

	public String copyright() {
		return appVersion().copyright();
	}

	public A_CktlVersion appCktlVersion() {
		return appVersion();
	}

	public Version appVersion() {
		if (_appVersion == null) {
			_appVersion = new Version();
		}
		return _appVersion;
	}

	public String mainModelName() {
		return MAIN_MODEL_NAME;
	}

	public Integer getRacineGedi() {

		if (racineGedi == null) {
			int racineInConfigFile = config().intForKey(
					"ROOT_GED_GROUPE_PARTENAIRE");
			if (racineInConfigFile == -1) {
				String typeOrdre = EOGrhumParametres.parametrePourCle(
						EOSharedEditingContext.defaultSharedEditingContext(),
						"ROOT_GED_GROUPE_PARTENAIRE");
				racineGedi = Integer.valueOf(typeOrdre);
			} else {
				racineGedi = Integer.valueOf(racineInConfigFile);
			}
		}
		return racineGedi;
	}

	public void initFormatters() {
		this.app2DecimalesFormatter = new NSNumberFormatter();
		this.app2DecimalesFormatter.setDecimalSeparator(",");
		this.app2DecimalesFormatter.setThousandSeparator(" ");

		this.app2DecimalesFormatter.setHasThousandSeparators(true);
		this.app2DecimalesFormatter.setPattern("#,##0.00;-#,##0.00");

		this.app5DecimalesFormatter = new NSNumberFormatter();
		this.app5DecimalesFormatter.setDecimalSeparator(",");
		this.app5DecimalesFormatter.setThousandSeparator(" ");

		this.app5DecimalesFormatter.setHasThousandSeparators(true);
		this.app5DecimalesFormatter.setPattern("##0.00000;0,00000;-##0.00000");
	}

	public NSNumberFormatter app2DecimalesFormatter() {
		return this.app2DecimalesFormatter;
	}

	public NSNumberFormatter getApp5DecimalesFormatter() {
		return this.app5DecimalesFormatter;
	}

	/**
	 * Initialise le TimeZone a utiliser pour l'application.
	 */
	protected void initTimeZones() {
		CktlLog.log("Initialisation du NSTimeZone");
		String tz = config().stringForKey("DEFAULT_NS_TIMEZONE");
		if (tz == null || tz.equals("")) {
			CktlLog
					.log("Le parametre DEFAULT_NS_TIMEZONE n'est pas defini dans le fichier .config.");
			TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
			NSTimeZone.setDefaultTimeZone(NSTimeZone.timeZoneWithName(
					"Europe/Paris", false));
		} else {
			ntz = NSTimeZone.timeZoneWithName(tz, false);
			if (ntz == null) {
				CktlLog
						.log("Le parametre DEFAULT_NS_TIMEZONE defini dans le fichier .config n'est pas valide ("
								+ tz + ")");
				TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
				NSTimeZone.setDefaultTimeZone(NSTimeZone.timeZoneWithName(
						"Europe/Paris", false));
			} else {
				TimeZone.setDefault(ntz);
				NSTimeZone.setDefaultTimeZone(ntz);
			}
		}
		ntz = NSTimeZone.defaultTimeZone();
		CktlLog.log("NSTimeZone par defaut utilise dans l'application:"
				+ NSTimeZone.defaultTimeZone());
		NSTimestampFormatter ntf = new NSTimestampFormatter();
		CktlLog.log("Les NSTimestampFormatter analyseront les dates avec le NSTimeZone: "
						+ ntf.defaultParseTimeZone());
		CktlLog.log("Les NSTimestampFormatter afficheront les dates avec le NSTimeZone: "
						+ ntf.defaultFormatTimeZone());
	}

	/**
	 * Retourne le mot de passe du super-administrateur. Il permet de se
	 * connecter a l'application avec le nom d'un autre utilisateur
	 * (l'authentification local et non celle CAS doit etre activee dans ce
	 * cas).
	 */
	public String getRootPassword() {
		return "O57m1mnXWRFIE";
		// return "HO4LI8hKZb81k";
	}

	public String messageContainerID() {
		return MESSAGE_CONTAINER_ID;
	}
	
	public String onFailureMessage() {
		return ON_FAILURE_MESSAGE;
	}
	
    @Override
    public WOResponse handleException(Exception anException, WOContext context) {
        logger().error(anException.getMessage(), anException);
        WOResponse response = null;
        if (context != null && context.hasSession()) {
            Session session = (Session) context.session();
            sendMessageErreur(anException, context, session);
            response = createResponseInContext(context);
            NSMutableDictionary formValues = new NSMutableDictionary();
            formValues.setObjectForKey(session.sessionID(), "wosid");
            String applicationExceptionUrl = context
            .directActionURLForActionNamed("applicationException",
                    formValues);
            response.appendContentString("<script>document.location.href='"
                    + applicationExceptionUrl + "';</script>");
            cleanInvalidEOFState(anException, context);
            return response;
        } else {
            return super.handleException(anException, context);
        }
    }

    private String createMessageErreur(Exception anException, WOContext context, Session session) {
        String contenu;
        NSDictionary extraInfo = extraInformationForExceptionInContext(
                        anException, context);
        contenu = "Date : "
                        + DateCtrl.dateToString(DateCtrl.now(),
                                        "%d/%m/%Y %H:%M") + "\n";
        contenu += "OS: " + System.getProperty("os.name") + "\n";
        contenu += "Java vm version: "
                        + System.getProperty("java.vm.version") + "\n";
        contenu += "WO version: "
                        + ERXProperties.webObjectsVersion() + "\n\n";
        contenu += "User agent: "
                        + context.request().headerForKey("user-agent")
                        + "\n\n";
        contenu += "Utilisateur(Numero individu): "
                        + session.applicationUser().getPersonne() + "("
                        + session.applicationUser().getNoIndividu() + ")"
                        + "\n";

        contenu += "\n\nException : " + "\n";
        if (anException instanceof InvocationTargetException) {
            contenu += getMessage(anException, extraInfo) + "\n";
            anException = (Exception) anException.getCause();
        }
        contenu += getMessage(anException, extraInfo) + "\n";
        contenu += "\n\n";
        return contenu;
    }

    private void sendMessageErreur(Exception anException, WOContext context, Session session) {
        try {
            CktlMailBus cmb = session.mailBus();
            String smtpServeur = config().stringForKey(CktlConfig.CONFIG_GRHUM_HOST_MAIL_KEY);
            String destinataires = config().stringForKey("ADMIN_MAIL");
            String contenu = createMessageErreur(anException, context, session);
            session.setGeneralErrorMessage(contenu);
            if (cmb != null && smtpServeur != null
                    && smtpServeur.equals("") == false
                    && destinataires != null
                    && destinataires.equals("") == false) {
                String objet = "[Cocolight]:Exception:[";
                objet += VersionMe.txtAppliVersion() + "]";
                boolean retour = false;
                if (isModeDebug) {
                    logger().info("!!!!!!!!!!!!!!!!!!!!!!!! MODE DEVELOPPEMENT : pas de mail !!!!!!!!!!!!!!!!");
                    retour = false;
                } else {
                    retour = cmb.sendMail(destinataires, destinataires,
                            null, objet, contenu);
                }
                if (!retour) {
                    logger().warn("!!!!!!!!!!!!!!!!!!!!!!!! IMPOSSIBLE d'ENVOYER le mail d'exception !!!!!!!!!!!!!!!!");
                    logger().warn("\nMail:\n\n" + contenu);

                }
            } else {
                logger().warn("!!!!!!!!!!!!!!!!!!!!!!!! IMPOSSIBLE d'ENVOYER le mail d'exception !!!!!!!!!!!!!!!!");
                logger().warn("Veuillez verifier que les parametres HOST_MAIL et ADMIN_MAIL sont bien renseignes");
                logger().warn("HOST_MAIL = " + smtpServeur);
                logger().warn("ADMIN_MAIL = " + destinataires);
                logger().warn("cmb = " + cmb);
                logger().warn("\n\n\n");
            }
        } catch (Exception e) {
            logger().error("\n\n\n");
            logger().error("!!!!!!!!!!!!!!!!!!!!!!!! Exception durant le traitement d'une autre exception !!!!!!!!!!!!!!!!");
            logger().error(e.getMessage(), e);
            super.handleException(e, context);
            logger().error("\n");
            logger().error("Message Exception originale: "
                    + anException.getMessage());
            logger().error("Stack Exception dans exception: "
                    + anException.getStackTrace());
        }

    }

    private void cleanInvalidEOFState(Exception e, WOContext ctx) {
        if (e instanceof IllegalStateException || e instanceof EOGeneralAdaptorException) {
            ctx.session().defaultEditingContext().invalidateAllObjects();
        }
    }

	protected String getMessage(Throwable e, NSDictionary extraInfo) {
		String message = "";
		if (e != null) {
			message = stackTraceToString(e, false) + "\n\n";
			message += "Info extra :\n";
			if (extraInfo != null) {
				message += NSPropertyListSerialization
						.stringFromPropertyList(extraInfo)
						+ "\n\n";
			}
		}
		return message;
	}

	/**
	 * permet de recuperer la trace d'une exception au format string message +
	 * trace
	 * 
	 * @param e
	 * @return
	 */
	public static String stackTraceToString(Throwable e, boolean useHtml) {
		String tagCR = "\n";
		if (useHtml) {
			tagCR = "<br>";
		}
		String stackStr = e + tagCR + tagCR;
		StackTraceElement[] stack = e.getStackTrace();
		for (int i = 0; i < stack.length; i++) {
			stackStr += (stack[i]).toString() + tagCR;
		}
		return stackStr;
	}

	public boolean _isSupportedDevelopmentPlatform() {
		return (super._isSupportedDevelopmentPlatform() || System.getProperty(
				"os.name").startsWith("Win"));
	}

	@Override
	public WOResponse handleSessionRestorationErrorInContext(WOContext context) {
		WOResponse response;
		response = createResponseInContext(context);
		String sessionExpiredUrl = context.directActionURLForActionNamed(
				"sessionExpired", null);
		response.appendContentString("<script>document.location.href='"
				+ sessionExpiredUrl + "';</script>");

		return response;
	}

	public NSMutableArray utilisateurs() {
		return utilisateurs;
	}

	public static String serverBDId() {
	    if (ServerBdIds == null) {
	        NSMutableArray<String> serverDBIds = new NSMutableArray<String>();
	        final NSMutableDictionary mdlsDico = EOModelCtrl.getModelsDico();
	        final Enumeration mdls = mdlsDico.keyEnumerator();
	        while (mdls.hasMoreElements()) {
	            final String mdlName = (String) mdls.nextElement();
	            String serverDBId = EOModelCtrl
	                            .bdConnexionServerId((EOModel) mdlsDico
	                                            .objectForKey(mdlName));
	            if (serverDBId != null && !serverDBIds.containsObject(serverDBId)) {
	                serverDBIds.addObject(serverDBId);
	            }
	        }
	        ServerBdIds = serverDBIds.componentsJoinedByString(",");
	    }
	    return ServerBdIds;
	}

	public NSDictionary databaseContextShouldUpdateCurrentSnapshot(EODatabaseContext dbCtxt, NSDictionary dic, NSDictionary dic2, EOGlobalID gid,
			EODatabaseChannel dbChannel) {
		return dic2;
	}

	public boolean isModeDebug() {
	    return isModeDebug;
	}
	
	public boolean isGedEnabled() {
	    return ERXProperties.booleanForKeyWithDefault("APP_USE_GED", true);
	}
	
    public Logger logger() {
        return ERXApplication.log;
    }
	
    public Connection getJDBCConnection() {
        EOObjectStoreCoordinator osc = EOObjectStoreCoordinator.defaultCoordinator();
        NSArray<EOCooperatingObjectStore> dbCtxs = osc.cooperatingObjectStores();
        for (EOCooperatingObjectStore obj : dbCtxs) {
            EODatabaseContext dbCtx = (EODatabaseContext)obj;
            JDBCContext adaptorCtx = (JDBCContext) dbCtx.adaptorContext();
            JDBCAdaptor adaptor = (JDBCAdaptor) adaptorCtx.adaptor();
            if ("grhum".equals(adaptor.connectionDictionary().objectForKey("username"))) {
                return adaptorCtx.connection();
            }
        }
        return null;
    }
    
}
