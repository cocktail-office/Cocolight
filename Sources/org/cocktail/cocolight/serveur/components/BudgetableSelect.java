package org.cocktail.cocolight.serveur.components;

import org.cocktail.cocowork.server.metier.convention.Budgetable;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktlajaxwebext.serveur.component.tableview.column.CktlAjaxTableViewColumn;
import org.cocktail.fwkcktlbibasse.serveur.metier.EOPrevisionBudget;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.appserver.ERXDisplayGroup;

public class BudgetableSelect extends MyWOComponent {
    
    public static final String BINDING_TRANCHE = "tranche";
    public static final String BINDING_PREVISION = "prevision";
    public static final String BINDING_MODE_DEPENSE = "modeDepense";
    public static final String BINDING_ON_SELECT = "onSelect";
    
    private NSArray<CktlAjaxTableViewColumn> colonnes;
    private ERXDisplayGroup<Budgetable> budgetablesDg;
    private Budgetable currentBudgetable;
    
    public BudgetableSelect(WOContext context) {
        super(context);
    }
    
    public WOActionResults creerPrevisionsBudgetNatLolf() {
        tranche().createPrevisionBudgetNatureLolfs(prevision(), budgetablesDg.selectedObjects());
        budgetablesDg = null;
        return (WOActionResults) valueForBinding(BINDING_ON_SELECT);
    }
    
    public NSArray<CktlAjaxTableViewColumn> colonnes() {
        if (colonnes == null) {
            NSData data = new NSData(application().resourceManager().bytesForResourceNamed("ConventionBudgetables.plist", null, NSArray.EmptyArray));
            NSDictionary desc = (NSDictionary) NSPropertyListSerialization.propertyListFromData(data, "UTF-8");
            colonnes = (NSArray<CktlAjaxTableViewColumn>)desc.valueForKey("colonnes");
        }
        return colonnes;
    }
    
    public ERXDisplayGroup<Budgetable> budgetablesDg() {
        if (budgetablesDg == null) {
            budgetablesDg = new ERXDisplayGroup<Budgetable>();
            budgetablesDg.setObjectArray(budgetables());
        }
        return budgetablesDg;
    }
    
    private NSArray<? extends Budgetable> budgetables() {
        if (modeDepense())
            return tranche().depensesSansPrevision();
        else
            return tranche().recettesSansPrevision();
    }
    
    public EOPrevisionBudget prevision() {
        return (EOPrevisionBudget)valueForBinding(BINDING_PREVISION);
    }
    
    public Tranche tranche() {
        return (Tranche)valueForBinding(BINDING_TRANCHE);
    }
    
    public boolean modeDepense() {
        return booleanValueForBinding(BINDING_MODE_DEPENSE, true);
    }
    
    public Budgetable getCurrentBudgetable() {
        return currentBudgetable;
    }
    
    public void setCurrentBudgetable(Budgetable currentBudgetable) {
        this.currentBudgetable = currentBudgetable;
    }
    
}