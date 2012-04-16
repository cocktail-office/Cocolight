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

public class EditionsSuiviComptable {

    static final public Logger LOG = Logger.getLogger(EditionsSuiviComptable.class);
    
    /**
     * Point d'acces pour la creation de l'edition des generalites des conventions.
     */
    static public CktlAbstractReporter editionConventionsSuiviComptable(
            NSArray<Integer> conOrdres, Integer utlOrdre, IJrxmlReportListener listener) {
        String masterJasperFileName = "SuiviComptable.jasper";
        JrxmlReporter jr = null;
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("CONORDRES", conOrdres.componentsJoinedByString(", "));
            params.put("UTLORDRE", utlOrdre);
            Connection connection = ((Application)ERXApplication.application()).getJDBCConnection();
            jr = new JrxmlReporter();
            jr.printWithThread("Suivi comptable de la convention", connection, null, pathForReportSuiviComptable(masterJasperFileName), params, CktlAbstractReporter.EXPORT_FORMAT_PDF, true, listener);
        }
        catch (Throwable e) {
            LOG.error("Une erreur s'est produite durant l'edition du suivi comptable de la convention", e);
        }
        //retourne le resultat au client :
        return jr;
    }
    
    private static String pathForReportSuiviComptable(String reportName) {
        ERXResourceManager rsm = (ERXResourceManager) ERXApplication.application().resourceManager();
        URL url = rsm.pathURLForResourceNamed("Reports/SuiviComptable/" + reportName, "app", null);
        return url.getFile();
    }
    
}
