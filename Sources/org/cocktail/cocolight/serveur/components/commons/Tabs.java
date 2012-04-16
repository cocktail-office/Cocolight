package org.cocktail.cocolight.serveur.components.commons;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;

public class Tabs {

    private NSMutableDictionary<String, Tab> indexedTabs = new NSMutableDictionary<String, Tab>();
    private NSMutableArray<Tab> _tabs = new NSMutableArray<Tabs.Tab>();
    private Tab selectedTab;
    private TabsDelegate tabsDelegate;
    
    public Tabs(Tab firstTab, Tab...otherTabs) {
        selectTab(firstTab);
        addTab(firstTab);
        for (Tab tab : otherTabs) {
            addTab(tab);
        }
    }
    
    public void selectTab(Tab tab) {
        if (selectedTab != tab) {
            if (tabsDelegate == null || tabsDelegate.shouldChangeSelectedTab(selectedTab, tab)) {
                this.selectedTab = tab;
            }
        }
    }
    
    public void addTab(Tab tab) {
        if (tab != null) {
            indexedTabs.setObjectForKey(tab, tab.getId());
            _tabs.addObject(tab);
            tab.setTabs(this);
        }
    }

    public NSArray<Tab> tabs() {
        return _tabs;
    }
    
    public void setTabsDelegate(TabsDelegate tabsDelegate) {
        this.tabsDelegate = tabsDelegate;
    }
    
    public Tab selectedTab() {
        return selectedTab;
    }
    
    public void setSelectedTab(Tab selectedTab) {
        selectTab(selectedTab);
    }
    
    public static class Tab {
        
        private EOEditingContext editingContext;
        private String libelle;
        private String id;
        private Tabs tabs;
        
        public Tab(String libelle, String id) {
            this.libelle = libelle;
            this.id = id;
        }

        public String getId() {
            return id;
        }
        
        public String getLibelle() {
            return libelle;
        }
        
        public EOEditingContext getEditingContext() {
            if (editingContext == null)
                editingContext = ERXEC.newEditingContext();
            return editingContext;
        }
     
        public boolean isSelected() {
            return tabs.selectedTab != null && tabs.selectedTab == this;
        }
        
        public void setSelected(boolean willBeSelected) {
            if (willBeSelected) {
                tabs.selectTab(this);
            }
        }
        
        protected void setTabs(Tabs tabs) {
            this.tabs = tabs;
        }
        
    }
    
    public static interface TabsDelegate {
        public boolean shouldChangeSelectedTab(Tab selected, Tab willBeSelected);
    }
    
    
    
}

