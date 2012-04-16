/*
 * Copyright COCKTAIL (www.cocktail.org), 1995, 2011 This software
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
package org.cocktail.cocolight.serveur.util;

import org.apache.log4j.Logger;
import org.cocktail.cocolight.serveur.Session;
import org.cocktail.cocowork.server.metier.convention.EOAvenantDocument;
import org.cocktail.fwkcktlged.serveur.BasicRequestHandlerDelegate;
import org.cocktail.fwkcktlged.serveur.metier.EODocument;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.model.ERAttachment;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXQ;

/**
 * 
 * Délégué pour vérifier que l'utilisateur a le droit de voir le document demandé.
 * 
 * @author Alexis TUAL <alexis.tual at cocktail.org>
 *
 */
public class ConventionRequestHandlerDelegate extends BasicRequestHandlerDelegate {

    public static final Logger LOG = Logger.getLogger(ConventionRequestHandlerDelegate.class);
    
    public boolean attachmentVisible(ERAttachment attachment, WORequest request, WOContext context) {
        boolean isAuth = false;
        if (super.attachmentVisible(attachment, request, context)) {
            // On voit déjà si ce monsieur à les droits de voir les documents
            Session session = (Session)context.session();
            EOEditingContext ec = ERXEC.newEditingContext();
            if (session.applicationUser().hasDroitConsultationDocuments()) {
                // Si c'est un attachement temporaire..., on laisse passer
                if (attachment.isNewObject()) {
                    isAuth = true;
                } else {
                    try {
                        // Ensuite on regarde s'il a les droits sur la convention correspondant à l'attachment...
                        String key = EOAvenantDocument.DOCUMENT_KEY + "." + EODocument.DOC_EXTERNAL_ID_KEY;
                        EOAvenantDocument avtDoc = 
                            EOAvenantDocument.fetch(ec, ERXQ.equals(key, ERXEOControlUtilities.primaryKeyObjectForObject(attachment)));
                        if (avtDoc != null && avtDoc.avenant() != null && avtDoc.avenant().contrat() != null) {
                            isAuth = avtDoc.avenant().contrat().isConsultablePar(session.applicationUser().getUtilisateur());
                        }
                    } catch (Exception e) {
                        LOG.error("Une erreur s'est produite lors de la vérification des droits sur l'attachment "+attachment, e);
                    } finally {
                        ec.dispose();
                        ec = null;
                    }
                }
            }
        }
        return isAuth;
    }

}
