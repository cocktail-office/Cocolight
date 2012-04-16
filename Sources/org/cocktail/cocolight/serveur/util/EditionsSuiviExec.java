package org.cocktail.cocolight.serveur.util;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.Application;
import org.cocktail.reporting.CktlAbstractReporter;
import org.cocktail.reporting.jrxml.IJrxmlReportListener;
import org.cocktail.reporting.jrxml.JrxmlReporter;

import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXResourceManager;

public class EditionsSuiviExec {

    static final public Logger LOG = Logger.getLogger(EditionsSuiviExec.class);
    
    /**
     * Point d'acces pour la creation de l'edition des generalites des conventions.
     */
    static public CktlAbstractReporter editionConventionsSuiviExecution(
            NSArray<Integer> conOrdres, Integer exeOrdre, Integer utlOrdre, IJrxmlReportListener listener) {
        String masterJasperFileName = "SuiviDetailExec.jasper";
        JrxmlReporter jr = null;
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("CONORDRES", conOrdres.componentsJoinedByString(", "));
            params.put("EXEORDRE", exeOrdre);
            params.put("UTLORDRE", utlOrdre);
            Connection connection = ((Application)ERXApplication.application()).getJDBCConnection();
            jr = new JrxmlReporter();
            jr.printWithThread("Suivi d'exécution de la convention", connection, null, pathForReportSuiviExec(masterJasperFileName), params, CktlAbstractReporter.EXPORT_FORMAT_PDF, true, listener);
        }
        catch (Throwable e) {
            LOG.error("Une erreur s'est produite durant l'edition du suivi d'exécution de la convention", e);
        }
        //retourne le resultat au client :
        return jr;
    }
    
    private static String pathForReportSuiviExec(String reportName) {
        ERXResourceManager rsm = (ERXResourceManager) ERXApplication.application().resourceManager();
        URL url = rsm.pathURLForResourceNamed("Reports/SuiviDetailExec/" + reportName, "app", null);
        return url.getFile();
    }
    
}
