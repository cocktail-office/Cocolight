package org.cocktail.cocolight.serveur.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.VersionMe;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.SbDepense;
import org.cocktail.cocowork.server.metier.convention.SbRecette;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.cocowork.server.metier.convention.VCreditsPositionnes;
import org.cocktail.cocowork.server.metier.convention.VCreditsPositionnesRec;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeCredit;
import org.cocktail.fwkcktlwebapp.common.util.CktlXMLWriter;
import org.cocktail.reporting.CktlAbstractReporter;
import org.cocktail.reporting.jrxml.IJrxmlReportListener;
import org.cocktail.reporting.jrxml.JrxmlReporterWithXmlDataSource;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.eof.ERXEOControlUtilities;

public class EditionsCreditsPositionnesParConvention {

    static final public Logger LOG = Logger.getLogger(EditionsCreditsPositionnesParConvention.class);
    
    /**
     * Point d'acces pour la creation de l'edition des credits des conventions.
     */
    static public CktlAbstractReporter editionCreditsPositionnesParConvention(
            final NSDictionary criteres, 
            final Object convGlobalIds, 
            final EOEditingContext ec,
            IJrxmlReportListener listener) {
        
        String masterJasperFileName =           "CreditsPositionnesParConventionMainReport.jasper";
        String subReportJasperFileName =        "CreditsPositionnesParConventionSubReport.jasper";
        String subSubReport1JasperFileName =    "CreditsPositionnesParConventionSubSubReport1.jasper";
        String subSubReport2JasperFileName =    "CreditsPositionnesParConventionSubSubReport2.jasper";
        //point d'entree dans l'arborescence decrite dans le fichier de donnees XML
        String recordPath = "/conventions";
        JrxmlReporterWithXmlDataSource jr = null;
        try {
            //generation et ecriture du fichier XML produit :
            StringWriter xmlString = createXmlEditionCreditsPositionnesParConvention(criteres, (NSArray) convGlobalIds, ec);
            // Affichage du xml en mode debug uniquement
            LOG.debug(xmlString);
            //flux fichier associe au fichier XML :
            InputStream xmlFileStream = new ByteArrayInputStream(xmlString.toString().getBytes());
            //parametres $P{...} passes au report principal : par ex titres, chemins vers les sous reports, etc.
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("SubReportFilePath", pathForReportCreditsPositionnesParConvention(subReportJasperFileName));
            parameters.put("SubSubReport1FilePath",  pathForReportCreditsPositionnesParConvention(subSubReport1JasperFileName));
            parameters.put("SubSubReport2FilePath",  pathForReportCreditsPositionnesParConvention(subSubReport2JasperFileName));
            jr = new JrxmlReporterWithXmlDataSource();
            jr.printWithThread("Généralités de la convention", xmlFileStream, recordPath, pathForReportCreditsPositionnesParConvention(masterJasperFileName), parameters, CktlAbstractReporter.EXPORT_FORMAT_PDF, true, listener);
        }
        catch (Throwable e) {
            LOG.error("Une erreur s'est produite durant l'edition des conventions (generalites)", e);
        }
        return jr;
    }
    
    private static String pathForReportCreditsPositionnesParConvention(String reportName) {
        ERXResourceManager rsm = (ERXResourceManager) ERXApplication.application().resourceManager();
        URL url = rsm.pathURLForResourceNamed("Reports/CreditsPositionnesParConvention/" + reportName, "app", null);
        return url.getFile();
    }
    
    /**
     * Interroge la base pour generer le fichier de donnees XML qui sera utilise pour "nourrir" l'edition.
     * (Utilisation de la bibliotheque du CRI de La Rochelle)
     */
    static public StringWriter createXmlEditionCreditsPositionnesParConvention(
            final NSDictionary criteres, 
            final NSArray convGlobalIds, 
            final EOEditingContext ec) throws Exception {
        
        StringWriter sw = new StringWriter();
        CktlXMLWriter w = new CktlXMLWriter(sw);
        
        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, Locale.FRANCE);
        //df.setTimeZone(TimeZone.getTimeZone(woApplication.timeZoneName));
        NSTimestampFormatter formatter = new NSTimestampFormatter("%d/%m/%Y - %H:%M:%S");
        
        //depiautage des libelles/valeurs des criteres de selection des conventions :
        StringBuffer libelles = new StringBuffer();
        StringBuffer valeurs = new StringBuffer();
        Enumeration enumerator = criteres.keyEnumerator();
        while (enumerator.hasMoreElements()) {
            String libelle = (String) enumerator.nextElement();
            String valeur = (String) criteres.objectForKey(libelle);
            System.out.println("  "+libelle+" = "+valeur);
            libelles.append(libelle+" :\n");
            valeurs.append(valeur+"\n");
        }
        System.out.println("  \nlibelles =\n"+libelles.toString());
        System.out.println("  \nvaleurs =\n"+valeurs.toString());
        
        //commencement du document XML :
        w.startDocument();
        w.startElement("conventions");
        w.writeElement("entetegauche", "Module Convention "+VersionMe.appliVersion());
        w.writeElement("entetecentre", "");
        w.writeElement("datetime", Editions.cleanedString(formatter.format(new NSTimestamp(Calendar.getInstance().getTime()))));

        w.writeElement("exeencours", Editions.cleanedString(new Integer(Editions.exerciceEnCours).toString()));
        w.writeElement("libellescriteres", Editions.cleanedString(libelles.toString()));
        w.writeElement("valeurscriteres", Editions.cleanedString(valeurs.toString()));
        
        //Recherche des conventions :
        NSArray listeConv;
        EOSortOrdering sort = EOSortOrdering.sortOrderingWithKey("conIndex", EOSortOrdering.CompareCaseInsensitiveAscending);
        listeConv = ERXEOControlUtilities.objectsForGlobalIDs(ec, convGlobalIds);
        if (listeConv.count() < 1) {
            w.writeElement("editionvide", "Aucune donn\u00E9e utile n'a \u00E9t\u00E9 trouv\u00E9e dans la base. Verifiez \u00E9ventuellement vos crit\u00E8res"
                    + " de s\u00E9lection des conventions...\n\nSachez que les conventions qui n'ont aucune tranche pour l'exercice en cours"
                    + " ne sont pas retenues dans cette \u00E9dition.");
        }
        System.out.println("Nb conv trouvees = "+listeConv.count());
        System.out.println(listeConv.valueForKey("conIndex"));
        
        //si aucune convention selectionnee n'a de tranche, ce flag permettra d'ajouter un message informatif sur l'edition
        boolean desTranchesTrouvees = false;
        
        
        for (int i=0; i<listeConv.count(); i++) {
            
            //convention courante
            Contrat contrat = (Contrat) listeConv.objectAtIndex(i);
            //tranches de cette convention
            NSArray tranches = contrat.tranches();
            System.out.println("Nb tranches = "+tranches.count());
            
            //une convention sans entree dans la table Tranche ne nous interesse pas
            if (tranches.count() < 1) continue;//iteration suivante du for
            desTranchesTrouvees = true;
            
            //recherche de la tranche correspondant a l'exercice en cours (il peut ne pas y en avoir) :
            Tranche tranche=null;
            int j;
            for(j=0; j<tranches.count(); j++) {
                tranche = ((Tranche) tranches.objectAtIndex(j));
                if (tranche.exerciceCocktail().exeExercice().intValue() == Editions.exerciceEnCours) {
                    break;  //trouve! fin du for
                }
                tranche = null; //remise a null (tranche sera null si aucune tranche correcte n'est trouvee)
            }
            //si aucune tranche concernant l'exerice en cours n'a ete trouvee pour cette convention alors convention suivante...
            if (tranche == null) continue;

            //System.out.println("Annee courante = "+exerciceEnCours);
            
            //----------------------------------------- infos sur la convention courante ---------------------------------------
            w.startElement("convention");
            
            w.writeElement("exercice", Editions.cleanedString(contrat.exerciceCocktail().exeExercice().toString()));
            w.writeElement("numero", Editions.cleanedString(Editions.indexFormatter.format((Integer) contrat.conIndex()).toString()));
            if (contrat.conObjet()!=null) {
                w.writeElement("objet", Editions.cleanedString(contrat.conObjet().toString()));
            }
            else w.writeElement("objet", "(Non renseign\u00E9)");
            
            if (contrat.conReferenceExterne()!=null) {
                w.writeElement("refexterne", Editions.cleanedString(contrat.conReferenceExterne().toString()));
            }
            else w.writeElement("refexterne", "(Non renseign\u00E9e)");
            
            //infos sur la tranche trouvee
            w.writeElement("montanttranche", tranche.traMontant().toString());
            if (tranche.traValide() != null) {
                if (tranche.traValide().compareToIgnoreCase("O") == 0) {
                    w.writeElement("tranchevalidee", "Oui");
                }
                else
                    w.writeElement("tranchevalidee", "Non");
            }
            else 
                w.writeElement("tranchevalidee", "Non");
            
            /**
             * DEPENSE
             */
            w.startElement("depense");
            //----------------------- tableau "Montants previsionnels par nature" -------------------------
            NSArray depenses = tranche.sbDepenses();
            if (depenses.count() < 1)
                w.writeElement("tableau1vide", "N\u00E9ant");
            else {
                w.startElement("tableau1");
                //parcours des montants previsionnels des depenses par nature :
                for (j=0; j<depenses.count(); j++) {
                    SbDepense sbdepense = (SbDepense) depenses.objectAtIndex(j);
                    //on ne s'interesse qu'aux montants PAR NATURE
                    if (sbdepense.planco()==null || NSKeyValueCoding.NullValue.equals(sbdepense.planco())) continue;
                    w.startElement("ligne");
                    w.writeElement("codenature", Editions.cleanedString(sbdepense.planco().pcoNum().toString()));
                    w.writeElement("description", Editions.cleanedString(sbdepense.planco().pcoLibelle().toString()));
                    w.writeElement("montant", Editions.cleanedString(sbdepense.sdMontantHt().toString()));
                    w.endElement(); //ligne
                }   
                w.endElement(); //tableau1
            }
            //------------------- tableau "Montants positionnes par UC / Type de credit" ----------------
            //selection des credits positionnes correspondant
//            NSMutableArray sorts = new NSMutableArray();
//            sorts.addObject(EOSortOrdering.sortOrderingWithKey("typcred.tcdCode", EOSortOrdering.CompareAscending));
//            sorts.addObject(EOSortOrdering.sortOrderingWithKey("organ.orgNiv", EOSortOrdering.CompareAscending));
//            EOQualifier qualifier = EOQualifier.qualifierWithQualifierFormat("tranche = %@ and montant <> 0", new NSArray(tranche));
            NSArray sorts = VCreditsPositionnes.TYPE_CREDIT.dot(EOTypeCredit.TCD_CODE_KEY).asc().then(
                            VCreditsPositionnes.ORGAN.dot(EOOrgan.ORG_NIVEAU_KEY).asc());
            EOQualifier qualifier = VCreditsPositionnes.TRANCHE.eq(tranche).and(VCreditsPositionnes.MONTANT.ne(BigDecimal.ZERO));
            NSArray creditspositionnes = VCreditsPositionnes.fetchAll(ec, qualifier, sorts);
            if (creditspositionnes.count() < 1)
                w.writeElement("tableau2vide", "N\u00E9ant");
            else {
                w.startElement("tableau2");
                //parcours des credits positionnes pour la tranche courante :
                for (j=0; j<creditspositionnes.count(); j++) {
                    w.startElement("ligne");
                    EOOrgan organ = ((VCreditsPositionnes) creditspositionnes.objectAtIndex(j)).organ();
                    
                    String etab = "";
                    String ub = "";
                    String cr = "";
                    String sousCr =" ";
                    
                    if(organ.orgEtab()!=null)
                        etab+=organ.orgEtab().toString();
                        
                    if(organ.orgUb()!=null)
                        ub+=organ.orgUb().toString();
                    
                    if(organ.orgCr()!=null)
                        cr+=organ.orgCr().toString();
                    
                    if(organ.orgSouscr()!=null)
                        sousCr+=organ.orgSouscr().toString();
                    
                    w.writeElement("unite", Editions.cleanedString(
                            etab));//anc unit
                    w.writeElement("composante", Editions.cleanedString(
                            ub));//anc comp
                    w.writeElement("lignebudget", Editions.cleanedString(
                            cr));//anc lbud
                    w.writeElement("uc", Editions.cleanedString(
                            sousCr));//anc uc

                    w.writeElement("typecredit", Editions.cleanedString(
                            ((VCreditsPositionnes) creditspositionnes.objectAtIndex(j)).typeCredit().tcdCode().toString()));
                    w.writeElement("montant", Editions.cleanedString(
                            ((VCreditsPositionnes) creditspositionnes.objectAtIndex(j)).montant().toString()));
                    w.endElement(); //ligne
                }
                w.endElement(); //tableau2
            }
            w.endElement(); //depense
            
            
            /**
             * RECETTE
             */
            w.startElement("recette");
            //----------------------- tableau "Montants previsionnels par nature" -------------------------
            NSArray recettes = tranche.sbRecettes();
            if (recettes.count() < 1)
                w.writeElement("tableau1vide", "N\u00E9ant");
            else {
                w.startElement("tableau1");
                //parcours des montants previsionnels des recettes par nature :
                for (j=0; j<recettes.count(); j++) {
                    SbRecette sbrecette = (SbRecette) recettes.objectAtIndex(j);
                    //on ne s'interesse qu'aux montants PAR NATURE
                    if (sbrecette.planco()==null || NSKeyValueCoding.NullValue.equals(sbrecette.planco())) continue;
                    w.startElement("ligne");
                    w.writeElement("codenature", Editions.cleanedString(sbrecette.planco().pcoNum().toString()));
                    w.writeElement("description", Editions.cleanedString(sbrecette.planco().pcoLibelle().toString()));
                    w.writeElement("montant", Editions.cleanedString(sbrecette.srMontantHt().toString()));
                    w.endElement(); //ligne
                }   
                w.endElement(); //tableau1
            }
            //------------------- tableau "Montants positionnes par UC / Type de credit" ----------------
            //selection des credits positionnes correspondant
//            sorts = new NSMutableArray();
//            sorts.addObject(EOSortOrdering.sortOrderingWithKey("typcred.tcdCode", EOSortOrdering.CompareAscending));
//            sorts.addObject(EOSortOrdering.sortOrderingWithKey("organ.orgNiv", EOSortOrdering.CompareAscending));
//            qualifier = EOQualifier.qualifierWithQualifierFormat("tranche = %@ and montant <> 0", new NSArray(tranche));
            sorts = VCreditsPositionnesRec.TYPE_CREDIT.dot(EOTypeCredit.TCD_CODE_KEY).asc().then(
                            VCreditsPositionnesRec.ORGAN.dot(EOOrgan.ORG_NIVEAU_KEY).asc());
            qualifier = VCreditsPositionnesRec.TRANCHE.eq(tranche).and(VCreditsPositionnesRec.MONTANT.ne(BigDecimal.ZERO));
            creditspositionnes = VCreditsPositionnesRec.fetchAll(ec, qualifier, sorts);
            if (creditspositionnes.count() < 1)
                w.writeElement("tableau2vide", "N\u00E9ant");
            else {
                w.startElement("tableau2");
                //parcours des credits positionnes pour la tranche courante :
                for (j=0; j<creditspositionnes.count(); j++) {
                    w.startElement("ligne");
                    EOOrgan organ = ((VCreditsPositionnesRec) creditspositionnes.objectAtIndex(j)).organ();
                    
                    String etab = "";
                    String ub = "";
                    String cr = "";
                    String sousCr =" ";
                    
                    if(organ.orgEtab()!=null)
                        etab+=organ.orgEtab().toString();
                        
                    if(organ.orgUb()!=null)
                        ub+=organ.orgUb().toString();
                    
                    if(organ.orgCr()!=null)
                        cr+=organ.orgCr().toString();
                    
                    if(organ.orgSouscr()!=null)
                        sousCr+=organ.orgSouscr().toString();
                    
                    w.writeElement("unite", Editions.cleanedString(
                            etab));//anc unit
                    w.writeElement("composante", Editions.cleanedString(
                            ub));//anc comp
                    w.writeElement("lignebudget", Editions.cleanedString(
                            cr));//anc lbud
                    w.writeElement("uc", Editions.cleanedString(
                            sousCr));//anc uc
                    w.writeElement("typecredit", Editions.cleanedString(
                            ((VCreditsPositionnesRec) creditspositionnes.objectAtIndex(j)).typeCredit().tcdCode().toString()));
                    w.writeElement("montant", Editions.cleanedString(
                            ((VCreditsPositionnesRec) creditspositionnes.objectAtIndex(j)).montant().toString()));
                    w.endElement(); //ligne
                }
                w.endElement(); //tableau2
            }
            w.endElement();//recette
            
            w.endElement(); //convention
        }
        
        if (!desTranchesTrouvees) {
            w.writeElement(
                    "editionvide", 
                    "Aucune donn\u00E9e utile n'a \u00E9t\u00E9 trouv\u00E9e dans la base. Verifiez \u00E9ventuellement vos criteres"
                    + " de s\u00E9lection des conventions...\n\nSachez que les conventions qui n'ont aucune tranche pour l'exercice en cours"
                    + " ne sont pas retenues dans cette \u00E9dition.");
        }
        
        //ajout de la balise </conventions> :
        w.endElement();
        
        //fin du document XML :
        w.endDocument();
        w.close();
        
        return sw;
    }
    
}
