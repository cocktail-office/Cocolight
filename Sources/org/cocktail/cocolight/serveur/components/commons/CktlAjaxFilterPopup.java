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
package org.cocktail.cocolight.serveur.components.commons;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxComponent;
import er.ajax.AjaxUtils;

/**
 * 
 * Composant qui permet de filtrer une liste (balise html SELECT). 
 * Ajouter le composant et insérer la liste à l'intérieur. 
 * @author rprin
 */
public class CktlAjaxFilterPopup extends AjaxComponent {

    private static final long serialVersionUID = -8698193617799941713L;
    public String filterText;

    public CktlAjaxFilterPopup(WOContext ctx) {
        super(ctx);
    }
    
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    public boolean isStateless() {
        return true;
    }
    public void reset() {
        filterText=null;
    }
    
    public String filtre() {
        StringBuffer str = new StringBuffer();

        str.append("if ( document.getElementById('"+selectName()+"')!=null ) {\n");
        str.append("filt");
        str.append(selectName());
        str.append("=new filterlist(document.getElementById('");
        str.append(selectName());
        str.append("'));\n");
        str.append("} else {\n");
        str.append("filt");
        str.append(selectName());
        str.append("=new filterlist(document."+formName()+".");
        str.append(selectName());
        str.append(");\n");
        str.append("}\n");
        str.append("filt");
        str.append(selectName());
        str.append(".set(document.getElementById('");
        str.append(fieldName());
        str.append("').value);");
        return String.valueOf(str);
    }
    public String onKeyUp() {
        // return "javascript:filt"+ selectName() +".set(this.value);";
        return "filt" + selectName() +".set(this.value);";
    }
    
    public String fieldName() {
        return (String) valueForBinding("fieldName");
    }
    public String formName() {
        return (String) valueForBinding("formName");
    }
    public String selectName() {
        return (String) valueForBinding("selectName");
    }

    @Override
    protected void addRequiredWebResources(WOResponse res) {
        AjaxUtils.addScriptResourceInHead(context(), res, "app", "scripts/filter.js");
    }

    @Override
    public WOActionResults handleRequest(WORequest request, WOContext context) {
        return null;
    }
    
}
