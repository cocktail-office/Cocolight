package org.cocktail.cocolight.serveur.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.VersionMe;
import org.cocktail.cocowork.server.metier.convention.Avenant;
import org.cocktail.cocowork.server.metier.convention.Contrat;
import org.cocktail.cocowork.server.metier.convention.ContratPartenaire;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlevenement.common.util.DateUtilities.DateDiff;
import org.cocktail.fwkcktlpersonne.common.metier.IPersonne;
import org.cocktail.fwkcktlwebapp.common.util.CktlXMLWriter;
import org.cocktail.reporting.CktlAbstractReporter;
import org.cocktail.reporting.jrxml.IJrxmlReportListener;
import org.cocktail.reporting.jrxml.JrxmlReporterWithXmlDataSource;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
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

public class EditionsGeneralitesConvention {

    static final public Logger LOG = Logger.getLogger(EditionsGeneralitesConvention.class);
    
    /**
     * Point d'acces pour la creation de l'edition des generalites des
     * conventions.
     */
    static public CktlAbstractReporter editionConventionsGeneralites(final NSDictionary criteres, NSArray<EOGlobalID> convGlobalIds, final Integer typeFormat, final EOEditingContext ec, IJrxmlReportListener listener) {
        String masterJasperFileName = "ConventionsGeneralitesMainReport.jasper";
        String subReportJasperFileName = "ConventionsGeneralitesSubReport.jasper";
        String subSubReportJasperFileName = "ConventionsGeneralitesSubSubReport.jasper";
        // point d'entree dans l'arborescence decrite dans le fichier de donnees
        // XML
        String recordPath = "/conventions";
        JrxmlReporterWithXmlDataSource jr = null;
        try {
            // generation et ecriture du fichier XML produit :
            StringWriter xmlString = createXmlEditionConventionsGeneralites(criteres, (NSArray) convGlobalIds, ec);
            // Affichage du xml en mode debug uniquement
            LOG.debug(xmlString);
            // flux fichier associe au fichier XML :
            InputStream xmlFileStream = new ByteArrayInputStream(xmlString.toString().getBytes());
            // parametres $P{...} passes au report principal : par ex titres,
            // chemins vers les sous reports, etc.
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("SubReportFilePath", pathForReportGeneralites(subReportJasperFileName));
            parameters.put("SubSubReportFilePath", pathForReportGeneralites(subSubReportJasperFileName));
            jr = new JrxmlReporterWithXmlDataSource();
            jr.printWithThread("Généralités de la convention", xmlFileStream, recordPath, pathForReportGeneralites(masterJasperFileName), parameters, CktlAbstractReporter.EXPORT_FORMAT_PDF, true, listener);
        }
        catch (Throwable e) {
            LOG.error("Une erreur s'est produite durant l'edition des conventions (generalites)", e);
        }
        // retourne le resultat au client :
        return jr;
    }

    private static String pathForReportGeneralites(String reportName) {
        ERXResourceManager rsm = (ERXResourceManager) ERXApplication.application().resourceManager();
        URL url = rsm.pathURLForResourceNamed("Reports/ConventionsGeneralites/" + reportName, "app", null);
        return url.getFile();
    }

    /**
     * Interroge la base pour generer le fichier de donnees XML qui sera utilise
     * pour "nourrir" l'edition.
     * (Utilisation de la bibliotheque du CRI de La Rochelle)
     */
    static public StringWriter createXmlEditionConventionsGeneralites(final NSDictionary criteres, final NSArray convGlobalIds, final EOEditingContext ec) throws Exception {

        StringWriter sw = new StringWriter();
        CktlXMLWriter w = new CktlXMLWriter(sw);

        NSTimestampFormatter formatter = new NSTimestampFormatter("%d/%m/%Y - %H:%M:%S");

        // depiautage des libelles/valeurs des criteres de selection des
        // conventions :
        StringBuffer libelles = new StringBuffer();
        StringBuffer valeurs = new StringBuffer();
        Enumeration enumerator = criteres.keyEnumerator();
        while (enumerator.hasMoreElements()) {
            String libelle = (String) enumerator.nextElement();
            String valeur = (String) criteres.objectForKey(libelle);
            LOG.info("  " + libelle + " = " + valeur);
            libelles.append(libelle + " :\n");
            valeurs.append(valeur + "\n");
        }
        LOG.info("  \nlibelles =\n" + libelles.toString());
        LOG.info("  \nvaleurs =\n" + valeurs.toString());

        // commencement du document XML :
        w.startDocument();
        w.startElement("conventions");
        w.writeElement("entetegauche", "Module Convention " + VersionMe.appliVersion());
        w.writeElement("entetecentre", "");
        w.writeElement("datetime", Editions.cleanedString(formatter.format(new NSTimestamp(Calendar.getInstance().getTime()))));

        w.writeElement("exeencours", Editions.cleanedString(new Integer(Editions.exerciceEnCours).toString()));
        w.writeElement("libellescriteres", Editions.cleanedString(libelles.toString()));
        w.writeElement("valeurscriteres", Editions.cleanedString(valeurs.toString()));

        // Recherche des conventions :
        NSArray listeConv;
        EOSortOrdering sort = EOSortOrdering.sortOrderingWithKey("conIndex", EOSortOrdering.CompareCaseInsensitiveAscending);
        listeConv = ERXEOControlUtilities.objectsForGlobalIDs(ec, convGlobalIds);
        LOG.info("Nb conv trouvees = " + listeConv.count());
        LOG.info(listeConv.valueForKey("conIndex"));

        // message si aucune convention trouvee
        if (listeConv.count() < 1) {
            w.writeElement("editionvide", "Aucune convention satisfaisant aux crit\u00E8res n'a \u00E9t\u00E9 trouv\u00E9e dans la base. Verifiez \u00E9ventuellement vos criteres" + " de s\u00E9lection...");
            // edition vide
            w.startElement("convention");
            w.endElement();
        }

        // LOG.info("nb conv trouvees = "+listeConv.count());
        for (int i = 0; i < listeConv.count(); i++) {
            // convention courante
            Contrat contrat = (Contrat) listeConv.objectAtIndex(i);

            // ----------------------------------------- infos sur la convention
            // courante ---------------------------------------
            w.startElement("convention");

            // monnaie
            w.writeElement("monnaie", Editions.MONNAIE_EURO);

            // tranches et LB de cette convention
            NSArray tranches = contrat.tranches();
            LOG.info("Nb tranches trouvees = " + tranches.count());
            StringBuffer bufTranches = new StringBuffer();
            StringBuffer bufLigneBud = new StringBuffer();
            NSMutableArray lbs = new NSMutableArray();
            Editions.moneyFormatter.setLocalizesPattern(true);
            for (int x = 0; x < tranches.count(); x++) {
                Tranche tranche = (Tranche) tranches.objectAtIndex(x);
                bufTranches.append(tranche.exerciceCocktail() != null ? tranche.exerciceCocktail().exeExercice().toString() : "???");
                bufTranches.append(" :  ");
                bufTranches.append(tranche.traMontant() != null ? Editions.moneyFormatter.format(tranche.traMontant()) : "???");
                bufTranches.append("\n");
            }
            w.writeElement("tranches", Editions.cleanedString(bufTranches.toString()));
            w.writeElement("lb", Editions.cleanedString(bufLigneBud.toString()));

            // exercice
            if (contrat.exerciceCocktail().exeExercice() != null)
                w.writeElement("exercice", Editions.cleanedString(contrat.exerciceCocktail().exeExercice().toString()));
            else
                w.writeElement("exercice", "????");

            // objet
            w.writeElement("numero", Editions.cleanedString(Editions.indexFormatter.format((Integer) contrat.conIndex()).toString()));

            // si la date de la validation du contract des != de null
            // alors on l'imprime !!!!
            LOG.info("Date de validation = " + contrat.valueForKey("conDateValidAdm"));
            if (contrat.valueForKey("conDateValidAdm") != null) {
                String str = formatter.format(contrat.valueForKey("conDateValidAdm"));
                w.writeElement("datevalidation", str);
            }
            else
                w.writeElement("datevalidation", " ");

            if (contrat.conObjet() != null) {
                w.writeElement("objet", Editions.cleanedString(contrat.conObjet().toString()));
            }
            else
                w.writeElement("objet", "(Non renseign\u00E9)");

            // observations
            if (contrat.conObservations() != null) {
                w.writeElement("observations", Editions.cleanedString(contrat.conObservations().toString()));
            }
            else
                w.writeElement("observations", "(Non renseign\u00E9es)");

            // ref externe
            if (contrat.conReferenceExterne() != null) {
                w.writeElement("refexterne", Editions.cleanedString(contrat.conReferenceExterne().toString()));
            }
            else
                w.writeElement("refexterne", "(Non renseign\u00E9e)");

            // etablissement gestionnaire
            if (contrat.etablissement() != null) {
                w.writeElement("etablissement", Editions.cleanedString(contrat.etablissement().llStructure()));
            }
            else
                w.writeElement("etablissement", "(Non renseign\u00E9)");

            // cr
            if (contrat.centreResponsabilite() != null) {
                w.writeElement("cr", Editions.cleanedString(contrat.centreResponsabilite().llStructure()));
            }
            else
                w.writeElement("cr", "(Non renseign\u00E9)");

            // composante
            if (contrat.organComposante() != null) {
                w.writeElement("composante", Editions.cleanedString(contrat.organComposante()));
            }
            else
                w.writeElement("composante", "(Non renseign\u00E9e)");

            // type optionnel
            if (contrat.avenantZero().typeOptionnel() != null) {
                w.writeElement("typeoptionnel", Editions.cleanedString(contrat.avenantZero().typeOptionnel().tsLibelle()));
            }
            else
                w.writeElement("typeoptionnel", "(Non renseign\u00E9)");

            // determination de l'avenant initial :
            NSArray avenants = contrat.avenants();
            EOQualifier qualifier = EOQualifier.qualifierWithQualifierFormat("avtIndex = 0", null);
            NSArray avenantsInit = EOQualifier.filteredArrayWithQualifier(avenants, qualifier);
            Avenant avenantInitial = null;
            if (avenantsInit.count() == 1) {// il ne peut y en avoir qu'un
                avenantInitial = (Avenant) avenantsInit.objectAtIndex(0);
            }
            if (avenantInitial == null)
                continue;

            // lucrativite
            if (avenantInitial.avtLucrativite() != null) {
                w.writeElement("lucrative", Editions.cleanedString("O".equals(avenantInitial.avtLucrativite()) ? "Oui" : "Non"));
            }
            else
                w.writeElement("lucrative", "?");

            // recup tva
            if (avenantInitial.avtRecupTva() != null) {
                w.writeElement("tvadeductible", Editions.cleanedString("O".equals(avenantInitial.avtRecupTva()) ? "Oui" : "Non"));
            }
            else
                w.writeElement("tvadeductible", "?");

            // mode de gestion
            if (avenantInitial.modeGestion() != null) {
                w.writeElement("modegestion", Editions.cleanedString(avenantInitial.modeGestion().mgLibelle()));
            }
            else
                w.writeElement("modegestion", "(Non renseign\u00E9)");

            // dates
            formatter = new NSTimestampFormatter("%d/%m/%Y");
            if (avenantInitial.avtDateDeb() != null) {
                w.writeElement("datedebut", formatter.format(avenantInitial.avtDateDeb()));
            }
            else
                w.writeElement("datedebut", "-");
            if (avenantInitial.avtDateFin() != null) {
                w.writeElement("datefin", formatter.format(avenantInitial.avtDateFin()));
            }
            else
                w.writeElement("datefin", "-");
            if (avenantInitial.avtDateSignature() != null) {
                w.writeElement("datesignature", formatter.format(avenantInitial.avtDateSignature()));
            }
            else
                w.writeElement("datesignature", "-");
            if (avenantInitial.avtDateDebExec() != null) {
                w.writeElement("datedebutexec", formatter.format(avenantInitial.avtDateDebExec()));
            }
            else
                w.writeElement("datedebutexec", "-");
            if (avenantInitial.avtDateFinExec() != null) {
                w.writeElement("datefinexec", formatter.format(avenantInitial.avtDateFinExec()));
            }
            else
                w.writeElement("datefinexec", "-");
            if (contrat.conDateCloture() != null) {
                w.writeElement("datecloture", formatter.format(contrat.conDateCloture()));
            }
            else
                w.writeElement("datecloture", "-");

            // duree
            String dureeStr;
            if (avenantInitial.avtDateDeb() != null && avenantInitial.avtDateFin() != null) {
                try {
                    DateDiff duree = new DateDiff(formatter.format(avenantInitial.avtDateDeb()), formatter.format(avenantInitial.avtDateFin()));
                    duree.calculateDifference();
                    dureeStr = duree.toString();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    dureeStr = "?";
                }
            }
            else {
                dureeStr = "?";
            }
            w.writeElement("duree", dureeStr);

            // montant total
            if (avenantInitial.avtMontantTtc() != null) {
                w.writeElement("montanttotalttc", avenantInitial.avtMontantTtc().toString());
            }
            else
                w.writeElement("montanttotalttc", "0");
            // ht
            if (avenantInitial.avtMontantHt() != null) {
                w.writeElement("montanttotalht", avenantInitial.avtMontantHt().toString());
            }
            else
                w.writeElement("montanttotalht", "0");

            // nature de la convention
            if (contrat.typeContrat() != null) {
                w.writeElement("nature", Editions.cleanedString(contrat.typeContrat().tyconLibelle()));
            }
            else
                w.writeElement("nature", "(Non renseign\u00E9e)");

            // creee par
            if (avenantInitial.utilisateurCreation() != null) {
                w.writeElement("agentcreation", Editions.cleanedString(avenantInitial.utilisateurCreation().getNomAndPrenom()));
            }
            else
                w.writeElement("agentcreation", "?");

            // modifiee par
            if (avenantInitial.utilisateurModif() != null) {
                w.writeElement("agentmodif", Editions.cleanedString(avenantInitial.utilisateurModif().getNomAndPrenom()));
            }
            else
                w.writeElement("agentmodif", "-");

            // responsable administratif
            NSArray<IPersonne> respAdmin = contrat.responsablesAdministratifs();
            if (respAdmin.count() > 0) {
                StringBuffer buf = new StringBuffer();
                for (IPersonne personne : respAdmin) {
                    buf.append("- ");
                    buf.append(personne.getNomPrenomAffichage());
                    buf.append("\n");
                }
                buf.delete(buf.length() - 1, buf.length());// enleve le dernier
                                                           // \n
                w.writeElement("respadmin", Editions.cleanedString(buf.toString()));
            }
            else
                w.writeElement("respadmin", "Aucun");

            // responsable scientifique
            NSArray<IPersonne> respSci = contrat.responsablesScientifiques();
            if (respSci.count() > 0) {
                StringBuffer buf = new StringBuffer();
                for (IPersonne personne : respSci) {
                    buf.append("- ");
                    buf.append(personne.getNomPrenomAffichage());
                    buf.append("\n");
                }
                buf.delete(buf.length() - 1, buf.length());// enleve le dernier
                                                           // \n
                w.writeElement("respsci", Editions.cleanedString(buf.toString()));
            }
            else
                w.writeElement("respsci", "Aucun");

            // tous les partenaires
            NSMutableArray partenaires = (NSMutableArray) contrat.contratPartenaires();

            // partenaires principaux
            NSArray partPrincipaux = contrat.partenairesPrincipaux();
            if (partPrincipaux.count() > 0) {
                StringBuffer buf = new StringBuffer();
                for (int n = 0; n < partPrincipaux.count(); n++) {
                    ContratPartenaire ap = (ContratPartenaire) partPrincipaux.objectAtIndex(n);
                    buf.append("- ");
                    buf.append(ap.partenaire().persLibelleAffichage());
                    if (ap.cpDateSignature() != null) {
                        buf.append(". Sign\u00E9: ");
                        buf.append(formatter.format(ap.cpDateSignature()));
                    }
                    if (ap.cpReferenceExterne() != null && !"".equals(ap.cpReferenceExterne())) {
                        buf.append(". R\u00E9f conv: '");
                        buf.append(ap.cpReferenceExterne());
                        buf.append("'");
                    }
                    buf.append("\n");
                }
                buf.delete(buf.length() - 1, buf.length());// enleve le dernier
                                                           // \n
                w.writeElement("partenprinc", Editions.cleanedString(buf.toString()));
            }
            else
                w.writeElement("partenprinc", "Aucun");

            // partenaires non principaux
            NSArray partNonPrincipaux = contrat.partenairesNonPrincipaux();
            if (partNonPrincipaux.count() > 0) {
                StringBuffer buf = new StringBuffer();
                for (int n = 0; n < partNonPrincipaux.count(); n++) {
                    ContratPartenaire ap = (ContratPartenaire) partNonPrincipaux.objectAtIndex(n);
                    buf.append("- ");
                    buf.append(ap.partenaire().persLibelleAffichage());
                    if (ap.cpDateSignature() != null) {
                        buf.append(". Sign\u00E9: ");
                        buf.append(formatter.format(ap.cpDateSignature()));
                    }
                    if (ap.cpReferenceExterne() != null && !"".equals(ap.cpReferenceExterne())) {
                        buf.append(". R\u00E9f conv: '");
                        buf.append(ap.cpReferenceExterne());
                        buf.append("'");
                    }
                    buf.append("\n");
                }
                buf.delete(buf.length() - 1, buf.length());// enleve le dernier
                                                           // \n
                w.writeElement("partennonprinc", Editions.cleanedString(buf.toString()));
            }
            else
                w.writeElement("partennonprinc", "Aucun");

            // avenants
            w.startElement("avenants");

            // liste triee des avenants sans l'avenant initial qui est le
            // contrat
            qualifier = EOQualifier.qualifierWithQualifierFormat("avtIndex <> 0", null);
            sort = EOSortOrdering.sortOrderingWithKey("avtIndex", EOSortOrdering.CompareAscending);
            avenants = EOQualifier.filteredArrayWithQualifier(EOSortOrdering.sortedArrayUsingKeyOrderArray(avenants, new NSArray(sort)), qualifier);

            // parcours des avenants a ce contrat
            for (int n = 0; n < avenants.count(); n++) {
                w.startElement("avenant");

                // avenant courant
                Avenant avenant = (Avenant) avenants.objectAtIndex(n);

                // objet
                w.writeElement("numero", Editions.cleanedString(Editions.indexFormatter.format((Integer) avenant.avtIndex()).toString()));
                if (avenant.avtObjet() != null) {
                    w.writeElement("objet", Editions.cleanedString(avenant.avtObjet().toString()));
                }
                else
                    w.writeElement("objet", "(Non renseign\u00E9)");

                // observations
                if (avenant.avtObservations() != null) {
                    w.writeElement("observations", Editions.cleanedString(avenant.avtObservations().toString()));
                }
                else
                    w.writeElement("observations", "(Non renseign\u00E9es)");

                // ref externe
                if (avenant.avtRefExterne() != null) {
                    w.writeElement("refexterne", Editions.cleanedString(avenant.avtRefExterne().toString()));
                }
                else
                    w.writeElement("refexterne", "(Non renseign\u00E9e)");

                // dates
                if (avenant.avtDateDeb() != null) {
                    w.writeElement("datedebut", formatter.format(avenant.avtDateDeb()));
                }
                else
                    w.writeElement("datedebut", "-");
                if (avenant.avtDateFin() != null) {
                    w.writeElement("datefin", formatter.format(avenant.avtDateFin()));
                }
                else
                    w.writeElement("datefin", "-");
                if (avenant.avtDateSignature() != null) {
                    w.writeElement("datesignature", formatter.format(avenant.avtDateSignature()));
                }
                else
                    w.writeElement("datesignature", "-");
                if (avenant.avtDateDebExec() != null) {
                    w.writeElement("datedebutexec", formatter.format(avenant.avtDateDebExec()));
                }
                else
                    w.writeElement("datedebutexec", "-");
                if (avenant.avtDateFinExec() != null) {
                    w.writeElement("datefinexec", formatter.format(avenant.avtDateFinExec()));
                }
                else
                    w.writeElement("datefinexec", "-");

                // duree
                if (avenant.avtDateDeb() != null && avenant.avtDateFin() != null) {
                    try {
                        DateDiff duree = new DateDiff(formatter.format(avenant.avtDateDeb()), formatter.format(avenant.avtDateFin()));
                        duree.calculateDifference();
                        dureeStr = duree.toString();
                    }
                    catch (Exception e) {
                        dureeStr = "?";
                    }
                }
                else {
                    dureeStr = "?";
                }
                w.writeElement("duree", dureeStr);

                // montant total
                if (avenant.avtMontantTtc() != null) {
                    w.writeElement("montanttotalttc", avenant.avtMontantTtc().toString());
                }
                else
                    w.writeElement("montanttotalttc", "0");
                // ht
                if (avenant.avtMontantHt() != null) {
                    w.writeElement("montanttotalht", avenant.avtMontantHt().toString());
                }
                else
                    w.writeElement("montanttotalht", "0");

                // type d'avenant (financier ou admin)
                if (avenant.typeAvenant() != null) {
                    w.writeElement("type", Editions.cleanedString(avenant.typeAvenant().taLibelle()));
                }
                else
                    w.writeElement("type", "(Non renseign\u00E9)");

                w.endElement(); // avenant
            }
            w.endElement(); // avenants

            w.endElement(); // convention
        }

        // ajout de la balise </conventions> :
        w.endElement();

        // fin du document XML (fermeture eventuelle des balises XML ouvertes) :
        w.endDocument();
        w.close();
        return sw;
    }

}
