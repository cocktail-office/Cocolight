package org.cocktail.cocolight.serveur.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.VersionMe;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.cocowork.server.metier.convention.TrancheBudget;
import org.cocktail.cocowork.server.metier.convention.TrancheBudgetRec;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExerciceCocktail;
import org.cocktail.fwkcktljefyadmin.common.metier.EOOrgan;
import org.cocktail.fwkcktljefyadmin.common.metier.EOTypeCredit;
import org.cocktail.fwkcktlwebapp.common.util.CktlXMLWriter;
import org.cocktail.reporting.CktlAbstractReporter;
import org.cocktail.reporting.jrxml.IJrxmlReportListener;
import org.cocktail.reporting.jrxml.JrxmlReporterWithXmlDataSource;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.eof.ERXEOControlUtilities;

public class EditionsCreditsPositionnesParLigneCredits {

    static final public Logger LOG = Logger.getLogger(EditionsCreditsPositionnesParLigneCredits.class);
    
    /**
     * Point d'acces pour la creation de l'edition des credits des conventions.
     */
    static public CktlAbstractReporter editionCreditsPositionnesParLigneCredits(
            final NSDictionary criteres, 
            final Object convGlobalIds, 
            final EOEditingContext ec,
            IJrxmlReportListener listener) {
        
        String masterJasperFileName =       "CreditsPositionnesParLigneDeCreditsMainReport.jasper";
        String subReportJasperFileName =    "CreditsPositionnesParLigneDeCreditsSubReport.jasper";
        String subSubReportJasperFileName = "CreditsPositionnesParLigneDeCreditsSubSubReport.jasper";
        //point d'entree dans l'arborescence decrite dans le fichier de donnees XML
        String recordPath = "/lignescredits";
        JrxmlReporterWithXmlDataSource jr = null;
        try {
            //generation et ecriture du fichier XML produit :
            StringWriter xmlString = createXmlEditionCreditsPositionnesParLigneCredits(criteres, (NSArray) convGlobalIds, ec);
            // Affichage du xml en mode debug uniquement
            LOG.debug(xmlString);
            //flux fichier associe au fichier XML :
            InputStream xmlFileStream = new ByteArrayInputStream(xmlString.toString().getBytes());
            //parametres $P{...} passes au report principal : par ex titres, chemins vers les sous reports, etc.
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("SubReportFilePath", pathForReportCreditsPositionnesParLigneCredits(subReportJasperFileName));
            parameters.put("SubSubReportFilePath",  pathForReportCreditsPositionnesParLigneCredits(subSubReportJasperFileName));
            jr = new JrxmlReporterWithXmlDataSource();
            jr.printWithThread("Généralités de la convention", xmlFileStream, recordPath, pathForReportCreditsPositionnesParLigneCredits(masterJasperFileName), parameters, CktlAbstractReporter.EXPORT_FORMAT_PDF, true, listener);
        }
        catch (Throwable e) {
            LOG.error("Une erreur s'est produite durant l'edition des conventions (generalites)", e);
        }
        return jr;
    }
    
    private static String pathForReportCreditsPositionnesParLigneCredits(String reportName) {
        ERXResourceManager rsm = (ERXResourceManager) ERXApplication.application().resourceManager();
        URL url = rsm.pathURLForResourceNamed("Reports/CreditsPositionnesParLigneCredits/" + reportName, "app", null);
        return url.getFile();
    }
    
    /**
     * Interroge la base pour generer le fichier de donnees XML qui sera utilise pour "nourrir" l'edition.
     * (Utilisation de la bibliotheque du CRI de La Rochelle)
     */
    static public StringWriter createXmlEditionCreditsPositionnesParLigneCredits(
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
        w.startElement("lignescredits");
        w.writeElement("entetegauche", "Module Convention "+VersionMe.appliVersion());
        w.writeElement("entetecentre", "");
        w.writeElement("datetime", Editions.cleanedString(formatter.format(new NSTimestamp(Calendar.getInstance().getTime()))));

        w.writeElement("exeencours", Editions.cleanedString(new Integer(Editions.exerciceEnCours).toString()));
        w.writeElement("libellescriteres", Editions.cleanedString(libelles.toString()));
        w.writeElement("valeurscriteres", Editions.cleanedString(valeurs.toString()));
        
        // Recherche des conventions :
        NSArray listeConv;
        EOSortOrdering sort = EOSortOrdering.sortOrderingWithKey("conIndex", EOSortOrdering.CompareCaseInsensitiveAscending);
        listeConv = ERXEOControlUtilities.objectsForGlobalIDs(ec, convGlobalIds);
        System.out.println("Nb conv trouvees = "+listeConv.count());
        System.out.println(listeConv.valueForKey("conIndex"));
        
        if (listeConv.count() < 1) {
            w.writeElement(
                    "editionvide", 
                    "Aucune convention n'a \u00E9t\u00E9 trouv\u00E9e dans la base. " +
                    "Verifiez \u00E9ventuellement vos criteres de s\u00E9lection des conventions...");
        }
        else {
            /**
             * DEPENSES
             */
            w.startElement("depense");
            //-------------- recherche des tranches budgetaires -----------------
            //construction du qualifier global :
            NSMutableArray listeQualifiersTrancheBudgetContrats = new NSMutableArray();
            for (int i=0; i<listeConv.count(); i++) { 
                listeQualifiersTrancheBudgetContrats.addObject(EOQualifier.qualifierWithQualifierFormat("tranche.contrat = %@", 
                        new NSArray(new Object[] { (Contrat) listeConv.objectAtIndex(i) })));
            } 
            EOOrQualifier qualifierTrancheBudgetContrats = new EOOrQualifier(listeQualifiersTrancheBudgetContrats);
            
            NSMutableArray listeQualifiersTrancheBudget = new NSMutableArray();
            listeQualifiersTrancheBudget.addObject(
                    qualifierTrancheBudgetContrats);
            listeQualifiersTrancheBudget.addObject(
                    TrancheBudget.TB_MONTANT.ne(BigDecimal.ZERO));
            listeQualifiersTrancheBudget.addObject(
                    TrancheBudget.TRANCHE.dot(Tranche.TRA_SUPPR).eq(Tranche.TRA_SUPPR_NON));
            listeQualifiersTrancheBudget.addObject(
                    TrancheBudget.TRANCHE.dot(Tranche.EXERCICE_COCKTAIL).dot(EOExerciceCocktail.EXE_EXERCICE_KEY).eq(Editions.exerciceEnCours));
            EOAndQualifier globalQualifierTrancheBudget = new EOAndQualifier(listeQualifiersTrancheBudget);
            
            LOG.info("Requete...");
            EOSortOrdering sortOrgPere =    EOSortOrdering.sortOrderingWithKey(TrancheBudget.ORGAN.dot(EOOrgan.ORG_PERE_KEY).key(), EOSortOrdering.CompareAscending);
            EOSortOrdering sortOrgUb =      EOSortOrdering.sortOrderingWithKey(TrancheBudget.ORGAN.dot(EOOrgan.ORG_UB_KEY).key(), EOSortOrdering.CompareAscending);
            EOSortOrdering sortOrgCr =      EOSortOrdering.sortOrderingWithKey(TrancheBudget.ORGAN.dot(EOOrgan.ORG_CR_KEY).key(), EOSortOrdering.CompareAscending);
            EOSortOrdering sortOrgSouscr =  EOSortOrdering.sortOrderingWithKey(TrancheBudget.ORGAN.dot(EOOrgan.ORG_SOUSCR_KEY).key(), EOSortOrdering.CompareAscending);
            EOSortOrdering sortTcdCode =    EOSortOrdering.sortOrderingWithKey(TrancheBudget.TYPE_CREDIT.dot(EOTypeCredit.TCD_CODE_KEY).key(), EOSortOrdering.CompareAscending);
//            EOFetchSpecification fs = new EOFetchSpecification(
//                    "TrancheBudget", 
//                    globalQualifierTrancheBudget, 
//                    new NSArray(new Object[] { sortOrgPere, sortOrgUb, sortOrgCr, sortOrgSouscr, sortTcdCode }));
            NSArray sorts = new NSArray(new Object[] { sortOrgPere, sortOrgUb, sortOrgCr, sortOrgSouscr, sortTcdCode });
            NSArray tranchesbudgets = TrancheBudget.fetchAll(ec, globalQualifierTrancheBudget, sorts);
            
            //algorithme CORIG pour creer le XML :
            int dmr=1;  //[dernier mouvement sur ORG_RAT_ORDRE] = oui
            int dmo=1;  //[                  sur     ORG_ORDRE] = oui
            int dmt=1;  //[                  sur      TCD_CODE] = oui
            int pmr;    //[premier mouvement sur ORG_RAT_ORDRE]
            int pmo;    //[                  sur     ORG_ORDRE]
            int pmt;    //[                  sur      TCD_CODE]
            TrancheBudget recDepense=null, recavDepense=null;
            double totalParOrganismeDeRattachement=0, totalParTypeDeCredit=0;
            
            for (int i=0; i<tranchesbudgets.count(); i++)
            {
                recDepense = (TrancheBudget) tranchesbudgets.objectAtIndex(i);
                
                if(i < tranchesbudgets.count()-1)
                {
                    recavDepense = (TrancheBudget) tranchesbudgets.objectAtIndex(i+1);
                }
                pmr=dmr;
                pmo=dmo;
                pmt=dmt;
                dmr=0;
                dmo=0;
                dmt=0;
                
                if(i == tranchesbudgets.count()-1)
                {
                    dmr=1;
                    dmo=1;
                    dmt=1;
                }
                else
                {
                    EOOrgan rat    = (EOOrgan) recDepense.organ();
                    EOOrgan ratsuiv = (EOOrgan) recavDepense.organ();
                    //s'il y a une rupture sur l'organisme de rattachement (CR)
                    if(rat.orgEtab().compareTo(ratsuiv.orgEtab())!=0 || //anc unit
                            rat.orgUb().compareTo(ratsuiv.orgUb())!=0 || //anc comp
                            rat.orgCr().compareTo(ratsuiv.orgCr())!=0)// anc lbud
                    {//dernier organisme de rattachement avant rupture
                        dmr=1;
                        dmo=1;
                        dmt=1;
                    }
                    
                    EOOrgan org    = recDepense.organ();
                    EOOrgan orgsuiv = recavDepense.organ();
                    //s'il y a une rupture sur l'UC
                    if(org.orgEtab().compareTo(orgsuiv.orgEtab())!=0 || //anc unit
                            org.orgUb().compareTo(orgsuiv.orgUb())!=0 || //anc comp
                            org.orgCr().compareTo(orgsuiv.orgCr())!=0 || // anc lbud
                            org.orgSouscr().compareTo(orgsuiv.orgSouscr())!=0)// anc uc
                    {//derniere ligne de credit avant rupture
                        
                        dmo=1;
                        dmt=1;
                    }
                    
                    String tcd     = recDepense.typeCredit().toString();
                    String tcdsuiv = recavDepense.typeCredit().toString();
                    if(tcd.compareTo(tcdsuiv) != 0) //s'il y a une rupture sur le type de credit
                    {//dernier type de credit avant rupture
                        dmt=1;
                    }
                }
                
                
                if(pmr==1)  //un nouvel organisme de rattachement (CR) (donc apres rupture)
                {
                    w.startElement("lignecredit");
                    String etab=recDepense.organ().orgEtab().toString();
                    String ub=recDepense.organ().orgUb().toString();
                    String libelle=recDepense.organ().orgLibelle().toString();
                    String orgCr =recDepense.organ().orgCr().toString();
                    
                    if(recDepense.organ().orgSouscr()!=null) {
                        etab=recDepense.organ().organPere().orgEtab().toString();
                        ub=recDepense.organ().organPere().orgUb().toString();
                        libelle=recDepense.organ().organPere().orgLibelle().toString();
                        orgCr =recDepense.organ().organPere().orgCr().toString();
                    }
                    
                    w.writeElement("unite", Editions.cleanedString(etab));//anc unit
                    w.writeElement("composante", Editions.cleanedString(ub));//anc comp
                    w.writeElement("libelle", Editions.cleanedString(libelle));
                    w.writeElement("lignebudget", Editions.cleanedString(orgCr));//anc lbud
                    
                    //init du total de niveau 3
                    totalParOrganismeDeRattachement = 0;
                    
                    w.startElement("tableau");
                }
                if(pmo==1)  //un nouvel UC (donc apres rupture)
                {
                    
                }
                if(pmt==1)  //un nouveau type de credit (donc apres rupture)
                {
                    w.startElement("ligne");
                    String uc = " ";
                    if(recDepense.organ().orgSouscr()!=null)
                        uc=recDepense.organ().orgSouscr().toString();
                    w.writeElement("uc", Editions.cleanedString(uc));//anc uc
                    w.writeElement("typecredit", Editions.cleanedString(recDepense.typeCredit().tcdCode().toString()));
                    w.writeElement("libelle", Editions.cleanedString(recDepense.organ().orgLibelle().toString()));
                    
                    //init du total de niveau 4
                    totalParTypeDeCredit = 0;
                }
                
                
                //pour chaque ligne du select : mise à jour des totaux
                totalParOrganismeDeRattachement += ((Number) recDepense.tbMontant()).doubleValue();
                totalParTypeDeCredit += ((Number) recDepense.tbMontant()).doubleValue();
                //NB: ils seront mis à zero correctement en fonction des premiers mouvements rencontres...
                
                
                if(dmt==1)  //dernier type de credit avant rupture
                {
                    //inscription du total du niveau 4 dans le xml
                    w.writeElement("montant", Editions.cleanedString((new Double(totalParTypeDeCredit)).toString()));
                    
                    w.endElement(); //</ligne>
                }
                if(dmo==1)  //derniere ligne de credit avant rupture
                {
                    
                }
                if(dmr==1)  //dernier organisme de rattachement avant rupture
                {
                    w.endElement(); //</tableau>
                    
                    //inscription du total du niveau 3 dans le xml
                    w.writeElement("total", Editions.cleanedString((new Double(totalParOrganismeDeRattachement)).toString()));
                    
                    w.endElement(); //</lignecredit>
                }
            }
            
            //si aucune tranche budget DEPENSE n'est trouvee, on ajoute un message dans l'edition
            if (tranchesbudgets.count() < 1) {
                w.writeElement(
                        "editionvide", 
                        "Les conventions s\u00E9lectionn\u00E9es n'ont pas de cr\u00E9dits positionn\u00E9s en d\u00E9pense " +
                        "sur l'exercice en cours.\n"+
                        "Verifiez \u00E9ventuellement vos criteres de s\u00E9lection des conventions...");
            }
            
            w.endElement();// depense
            
            
            /**
             * RECETTES
             */
            w.startElement("recette");
            //-------------- recherche des tranches budgetaires -----------------
            //construction du qualifier global :
            listeQualifiersTrancheBudgetContrats = new NSMutableArray();
            for (int i=0; i<listeConv.count(); i++) { 
                listeQualifiersTrancheBudgetContrats.addObject(EOQualifier.qualifierWithQualifierFormat("tranche.contrat = %@", 
                        new NSArray(new Object[] { (Contrat) listeConv.objectAtIndex(i) })));
            } 
            qualifierTrancheBudgetContrats = new EOOrQualifier(listeQualifiersTrancheBudgetContrats);
            
            listeQualifiersTrancheBudget = new NSMutableArray();
            listeQualifiersTrancheBudget.addObject(
                    qualifierTrancheBudgetContrats);
            listeQualifiersTrancheBudget.addObject(
                            TrancheBudgetRec.TBR_MONTANT.ne(BigDecimal.ZERO));
                    listeQualifiersTrancheBudget.addObject(
                            TrancheBudgetRec.TRANCHE.dot(Tranche.TRA_SUPPR).eq(Tranche.TRA_SUPPR_NON));
                    listeQualifiersTrancheBudget.addObject(
                            TrancheBudgetRec.TRANCHE.dot(Tranche.EXERCICE_COCKTAIL).dot(EOExerciceCocktail.EXE_EXERCICE_KEY).eq(Editions.exerciceEnCours));
            
            
            globalQualifierTrancheBudget = new EOAndQualifier(listeQualifiersTrancheBudget);
            
            LOG.info("Requete...");
            sortOrgPere =    EOSortOrdering.sortOrderingWithKey(TrancheBudgetRec.ORGAN.dot(EOOrgan.ORG_PERE_KEY).key(), EOSortOrdering.CompareAscending);
            sortOrgUb =      EOSortOrdering.sortOrderingWithKey(TrancheBudgetRec.ORGAN.dot(EOOrgan.ORG_UB_KEY).key(), EOSortOrdering.CompareAscending);
            sortOrgCr =      EOSortOrdering.sortOrderingWithKey(TrancheBudgetRec.ORGAN.dot(EOOrgan.ORG_CR_KEY).key(), EOSortOrdering.CompareAscending);
            sortOrgSouscr =  EOSortOrdering.sortOrderingWithKey(TrancheBudgetRec.ORGAN.dot(EOOrgan.ORG_SOUSCR_KEY).key(), EOSortOrdering.CompareAscending);
            sortTcdCode =    EOSortOrdering.sortOrderingWithKey(TrancheBudgetRec.TYPE_CREDIT.dot(EOTypeCredit.TCD_CODE_KEY).key(), EOSortOrdering.CompareAscending);
            
            
//            fs = new EOFetchSpecification(
//                    "TrancheBudgetRec", 
//                    globalQualifierTrancheBudget, 
//                    new NSArray(new Object[] { sortOrgPere, sortOrgUb, sortOrgCr, sortOrgSouscr, sortTcdCode }));
            sorts = new NSArray(new Object[] { sortOrgPere, sortOrgUb, sortOrgCr, sortOrgSouscr, sortTcdCode });
            tranchesbudgets = TrancheBudgetRec.fetchAll(ec, globalQualifierTrancheBudget, sorts);
            
            //algorithme CORIG pour creer le XML :
            dmr=1;  //[dernier mouvement sur ORG_RAT_ORDRE] = oui
            dmo=1;  //[                  sur     ORG_ORDRE] = oui
            dmt=1;  //[                  sur      TCD_CODE] = oui
            TrancheBudgetRec recRecette=null;
            TrancheBudgetRec recavRecette=null;
            totalParOrganismeDeRattachement=0;
            totalParTypeDeCredit=0;
            
            for (int i=0; i<tranchesbudgets.count(); i++)
            {
                recRecette = (TrancheBudgetRec) tranchesbudgets.objectAtIndex(i);
                
                if(i < tranchesbudgets.count()-1)
                {
                    recavRecette = (TrancheBudgetRec) tranchesbudgets.objectAtIndex(i+1);
                }
                pmr=dmr;
                pmo=dmo;
                pmt=dmt;
                dmr=0;
                dmo=0;
                dmt=0;
                
                if(i == tranchesbudgets.count()-1)
                {
                    dmr=1;
                    dmo=1;
                    dmt=1;
                }
                else
                {
                    EOOrgan rat    = (EOOrgan) recRecette.organ();
                    EOOrgan ratsuiv = (EOOrgan) recavRecette.organ();
                    
                    
                    //s'il y a une rupture sur l'organisme de rattachement (CR)
                    if(rat.orgEtab().compareTo(ratsuiv.orgEtab())!=0 || //anc unit
                            rat.orgUb().compareTo(ratsuiv.orgUb())!=0 || //anc comp
                            rat.orgCr().compareTo(ratsuiv.orgCr())!=0)// anc lbud
                    {//dernier organisme de rattachement avant rupture
                        //System.out.println("Recette : rupture cr");
                        dmr=1;
                        dmo=1;
                        dmt=1;
                    }
                    
                    EOOrgan org    = recRecette.organ();
                    EOOrgan orgsuiv = recavRecette.organ();
                    //s'il y a une rupture sur l'UC
                    if(org.orgEtab().compareTo(orgsuiv.orgEtab())!=0 || //anc unit
                            org.orgUb().compareTo(orgsuiv.orgUb())!=0 || //anc comp
                            org.orgCr().compareTo(orgsuiv.orgCr())!=0 || // anc lbud
                            org.orgSouscr().compareTo(orgsuiv.orgSouscr())!=0)// anc uc
                    {//derniere ligne de credit avant rupture
                    //System.out.println("Recette : rupture souscr");
                        //dmr=1;
                        dmo=1;
                        dmt=1;
                    }
                    
                    String tcd     = recRecette.typeCredit().toString();
                    String tcdsuiv = recavRecette.typeCredit().toString();
                    if(tcd.compareTo(tcdsuiv) != 0) //s'il y a une rupture sur le type de credit
                    {//dernier type de credit avant rupture
                        //System.out.println("Recette : rupture typecred");
                        dmt=1;
                    }
                }
                
                
                if(pmr==1)  //un nouvel organisme de rattachement (CR) (donc apres rupture)
                {
                    //System.out.println("Recette : nouveau cr");
                    w.startElement("lignecredit");
                    
                    String etab=recRecette.organ().orgEtab().toString();
                    String ub=recRecette.organ().orgUb().toString();
                    String libelle=recRecette.organ().orgLibelle().toString();
                    String orgCr =recRecette.organ().orgCr().toString();
                    
                    if(recRecette.organ().orgSouscr()!=null) {
                        etab=recRecette.organ().organPere().orgEtab().toString();
                        ub=recRecette.organ().organPere().orgUb().toString();
                        libelle=recRecette.organ().organPere().orgLibelle().toString();
                        orgCr =recRecette.organ().organPere().orgCr().toString();
                    }
                    
                    w.writeElement("unite", Editions.cleanedString(etab));//anc unit
                    w.writeElement("composante", Editions.cleanedString(ub));//anc comp
                    w.writeElement("libelle", Editions.cleanedString(libelle));
                    w.writeElement("lignebudget", Editions.cleanedString(orgCr));//anc lbud
                    
                    //init du total de niveau 3
                    totalParOrganismeDeRattachement = 0;
                    
                    w.startElement("tableau");
                }
                if(pmo==1)  //un nouvel UC (donc apres rupture)
                {
                    //System.out.println("Recette : nouveau souscr");
                }
                if(pmt==1)  //un nouveau type de credit (donc apres rupture)
                {
                    //System.out.println("Recette : nouveau tcd");
                    w.startElement("ligne");
                    String uc=" ";
                    
                    if(recRecette.organ().orgSouscr()!=null)
                        uc=recRecette.organ().orgSouscr().toString();
                    w.writeElement("uc", Editions.cleanedString(uc));//anc uc
                    w.writeElement("typecredit", Editions.cleanedString(recRecette.typeCredit().tcdCode().toString()));
                    w.writeElement("libelle", Editions.cleanedString(recRecette.organ().orgLibelle().toString()));
                    
                    //init du total de niveau 4
                    totalParTypeDeCredit = 0;
                }
                
                
                //pour chaque ligne du select : mise à jour des totaux
                totalParOrganismeDeRattachement += ((Number) recRecette.tbrMontant()).doubleValue();
                totalParTypeDeCredit += ((Number) recRecette.tbrMontant()).doubleValue();
                //NB: ils seront mis à zero correctement en fonction des premiers mouvements rencontres...
                
                
                if(dmt==1)  //dernier type de credit avant rupture
                {
                    //inscription du total du niveau 4 dans le xml
                    w.writeElement("montant", Editions.cleanedString((new Double(totalParTypeDeCredit)).toString()));
                    
                    w.endElement(); //</ligne>
                }
                if(dmo==1)  //derniere ligne de credit avant rupture
                {
                    
                }
                if(dmr==1)  //dernier organisme de rattachement avant rupture
                {
                    w.endElement(); //</tableau>
                    
                    //inscription du total du niveau 3 dans le xml
                    w.writeElement("total", Editions.cleanedString((new Double(totalParOrganismeDeRattachement)).toString()));
                    
                    w.endElement(); //</lignecredit>
                }
            }
            
            //si aucune tranche budget RECETTE n'est trouvee, on ajoute un message dans l'edition
            if (tranchesbudgets.count() < 1) {
                w.writeElement(
                        "editionvide", 
                        "Les conventions s\u00E9lectionn\u00E9es n'ont pas de cr\u00E9dits positionn\u00E9s en d\u00E9pense " +
                        "dans l'exercice en cours.\n"+
                        "Verifiez \u00E9ventuellement vos criteres de s\u00E9lection des conventions...");
            }
            
            w.endElement();//recette
        }
        //ajout de la balise </lignescredits> :
        w.endElement();
        //fin du document XML :
        w.endDocument();
        w.close();
        
        return sw;
    }
    
}
