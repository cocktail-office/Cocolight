function formatterTelephone(textfield) {
    var digCount = 0;
    var phone = "";
    var str = textfield.value;
    str = Trim(str);
    for(var i = 0; i < str.length; i++) {
	    if(digCount == 2 && phone.length < 14) {
	        phone = phone.concat(".");
	        digCount = 0;
	    }
	    if(isDigit(str.charAt(i))) {
	        phone = phone.concat(str.charAt(i));
	        digCount++;
	    }
    }

    textfield.value = phone;
    
    return phone;
}

function formatterDateDeNaissance(textfield) {
  var dateDeNaissance = "";
  var str = textfield.value;
  str = Trim(str);
    for(var i = 0; i < str.length; i++) {
	    if(isDigit(str.charAt(i))) {
	        dateDeNaissance = dateDeNaissance.concat(str.charAt(i));
	    }
    }

  if (dateDeNaissance.length>=6 && dateDeNaissance.length<=8) {
      var dd = dateDeNaissance.substr(0,2);
      var mm = dateDeNaissance.substr(2,2);
      var aaaa = dateDeNaissance.substr(4,4);
      if (parseInt(dd)>0 && parseInt(dd)<=31) {
        var mmInt = parseInt(mm);
        var maxJ = 29;
        switch(mmInt) {
          case 2: maxJ=29;break;
          case 4:
          case 6:
          case 9:
          case 11: maxJ=30;break;
          default: maxJ=31;
        }
        if (parseInt(dd)>maxJ) {
          alert('Date de naissance incorrecte.');
          return false;
        }
        if (aaaa.length==2) {
          aaaa = "19"+aaaa;
        }
        
        textfield.value = dd+"/"+mm+"/"+aaaa;
      }
  } else {
    alert('La date de naissance doit-etre saisie au format jj/mm/aaaa.');
    return false;
  }
  
  return true;
}