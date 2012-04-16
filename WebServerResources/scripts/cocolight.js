// Namespace Cocolight
var cocolight = {};

cocolight.focusedEltMemento = {

	retainEltWithFocus : function(elt) {
	    this.eltHadFocus = elt.name;
	},
	
	focusRetainedElt : function() {
	    var elt = document.getElementsByName(this.eltHadFocus)[0];
	    if (elt != null) {
	        elt.focus();
	    }
	}

}

cocolight.getSelectionId = function(text, li) {
	if (typeof(li)!='undefined') {
		var chaine = li.innerText;
	  if (typeof(chaine)=='undefined') {
			chaine = li.textContent;
		}
	
		switch(text.id) {
	   case 'CodeNaf_field' :
	 	 var libelle_code_naf = chaine;
		 $('CodeNaf_field').value = libelle_code_naf;
	       break;
	  }
	
		var updateContainer = text.getAttribute("updatecontainerid");
		if (typeof(updateContainer)!='undefined' && updateContainer!=null && updateContainer.length > 0) {
			eval(updateContainer+'Update()');
		}
	}
}