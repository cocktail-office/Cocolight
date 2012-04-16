package org.cocktail.cocolight.serveur.util;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.Application;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSNumberFormatter;

/**
 * Classe fournissant des methodes statiques qui retournent les editions en PDF.
 *
 * 1/ A partir des criteres envoyes par le client, la base est interrogee pour generer un fichier
 * de donnees XML sur le disque du serveur.
 * 2/ Une "maquette" (un ou plusieurs fichiers .jasper) doit etre fournie a la moulinette JasperReports
 * qui "nourrit" celle-ci avec les donnees contenues dans le fichier xml.
 */
public class Editions
{
    //constantes natures de convention
    final static public int ORDRE_RESS_AFFECTEE = 1;
    final static public int ORDRE_CONV_SIMPLE = 2;
    //symbole euro en unicode
    final static public String MONNAIE_EURO = "\u20AC";
    //formatteurs
    final static public NSNumberFormatter indexFormatter = new NSNumberFormatter("0000;-0000");
    final static public NSNumberFormatter moneyFormatter = new NSNumberFormatter("#,##0.00;-#,##0.00");
    //pointeur vers l'application serveur :
    static private Application woApplication = (Application) WOApplication.application();
    static final public int exerciceEnCours = woApplication.getDernierExerciceOuvertOuEnPreparation().exeExercice().intValue();
    static final public Logger LOG = Logger.getLogger(Editions.class);
    
    
    /*****************************************< Utiles >**************************************************/
    
    /**
     * Retourne le chemin absolu du repertoire contenant les ressources de l'appli.
     */
    static private String getAppResourcesDir() {
        String resourcesDir = WOApplication.application().path() + "/Contents/Resources/";
        LOG.debug("Repertoire des ressources de l'application : " + resourcesDir);
        return resourcesDir;
    }
    
    /**
     * Sauvegarde d'un fichier dans le repertoire temporaire du systeme (il est remplace s'il existe deja)
     */
    static public String saveInTempFile(final StringWriter sw, final String fileName) throws IOException {
        String temporaryDir = getLocalTempDirectoryPath();
        
        //si le fichier existe deja, il est remplace sauvagement :
        File f = new File(temporaryDir + fileName);
        if (f.exists()) {
            f.delete();
        }
        if (f.createNewFile()) {
            new FileWriter(f);
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(sw.toString());
            out.close();            
        }
        return f.getAbsolutePath();
    }
    
    /**
     * Recuperation du chemin complet du repertoire temporaire du systeme courant.
     */
    static private String getLocalTempDirectoryPath() {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        if (!temporaryDir.endsWith(File.separator)) {
            temporaryDir = temporaryDir + File.separator;
        }
        LOG.info("Repertoire temporaire de ce systeme : " + temporaryDir);
        return temporaryDir;
    }
    
    /**
     * Retourne une string ou divers caract�res qui posent probl�me sont remplaces par leur code 
     * @param str ou obj chaine ou objet a traiter
     * @return chaine "netoyee"
     */
    static public String cleanedString(String str) {
        return str.replaceAll("&","&#38;").replaceAll("<","&#60;").replaceAll(">","&#62;").replaceAll("\r\n","\n");//.replaceAll("\n"," - ");
    }
    
    static public String cleanedString(Object obj) {
        if (obj == null) return "";
        return obj.toString().replaceAll("&","&#38;").replaceAll("<","&#60;").replaceAll(">","&#62;").replaceAll("\r\n","\n");//.replaceAll("\n"," - ");
    }

}

