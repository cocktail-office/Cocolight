package org.cocktail.cocolight.serveur.components.commons;

import java.math.BigDecimal;

import org.cocktail.cocolight.serveur.components.MyWOComponent;
import org.cocktail.cocowork.server.metier.convention.RepartPartenaireTranche;
import org.cocktail.cocowork.server.metier.convention.ReportInfo;
import org.cocktail.cocowork.server.metier.convention.Tranche;
import org.cocktail.fwkcktljefyadmin.common.metier.EOExercice;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class ReportCredits extends MyWOComponent {
    
    private static final long serialVersionUID = 4695219421091974699L;
    public static final String BINDING_TRANCHE_SOURCE = "trancheSource";
    
    private Tranche currentTrancheDest;
    private Tranche selectedTrancheDest;
    private boolean inclureEngage = true;
    private Tranche _trancheSource;
    private BigDecimal _montantConsomme;
    private BigDecimal _montantReportable;
    private NSMutableDictionary<String, NSArray<ReportInfo>> _reportInfosForTranche = new NSMutableDictionary<String, NSArray<ReportInfo>>();
    private ReportInfo currentReportInfo;
    
    public ReportCredits(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        _trancheSource = null;
        _montantConsomme = null;
        _montantReportable = null;
        super.appendToResponse(response, context);
    }
    
    public WOActionResults reporter() {
        if (getSelectedTrancheDest() != null) {
            try {
                trancheSource().contrat().reporterCredits(reportInfos());
                trancheSource().editingContext().saveChanges();
                _reportInfosForTranche.clear();
                session().addSimpleInfoMessage("Cocolight", "Le report de crédits a été effectué");
            } catch (ValidationException e) {
                session().addSimpleErrorMessage("Cocolight", e.getMessage());
            } catch (Exception e) {
                trancheSource().editingContext().invalidateAllObjects();
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
        }
        return null;
    }

    public boolean canReport() {
        return getSelectedTrancheDest() != null && isMontantTotalAjoutDestinationBalanced() && isMontantTotalRetraitSourceBalanced();
    }
    
    public NSArray<Tranche> tranchesDestination() {
        NSMutableArray<Tranche> tranches = 
                        trancheSource().contrat().tranches(EOExercice.EXE_ETAT_OUVERT, EOExercice.EXE_ETAT_PREPARATION).mutableClone();
        tranches.removeObject(trancheSource());
        if (!tranches.isEmpty()) {
            setSelectedTrancheDest(tranches.lastObject());
        }
        return tranches.immutableClone();
    }
    
    public Tranche trancheSource() {
        if (_trancheSource == null) {
            _trancheSource = (Tranche) valueForBinding(BINDING_TRANCHE_SOURCE);
        }
        return _trancheSource;
    }

    public Tranche getCurrentTrancheDest() {
        return currentTrancheDest;
    }
    
    public BigDecimal montantConsomme() {
        if (_montantConsomme == null) {
            _montantConsomme = trancheSource().totalConsomme(isInclureEngage());
        }
        return _montantConsomme;
    }
    
    public BigDecimal montantReportable() {
        if (_montantReportable == null) {
            _montantReportable = trancheSource().montantReportable(montantConsomme()).subtract(_montantConsomme);
    }
        return _montantReportable;
    }
    
    public BigDecimal montantTotalRetraitSource() {
        return (BigDecimal) reportInfos().valueForKey("@sum."+ReportInfo.RETRAIT_REPART_SOURCE_KEY);
    }
    
    public BigDecimal montantTotalAjoutDestination() {
        return (BigDecimal) reportInfos().valueForKey("@sum."+ReportInfo.AJOUT_REPART_DESTINATION_KEY);
    }
    
    private boolean isMontantTotalAjoutDestinationBalanced() {
        return montantReportable().compareTo(montantTotalAjoutDestination()) == 0;
    }
    
    private boolean isMontantTotalRetraitSourceBalanced() {
        return montantReportable().compareTo(montantTotalRetraitSource()) == 0;
    }
    
    public String cssForAjoutDest() {
       return isMontantTotalAjoutDestinationBalanced() ? "balancePos" : "balanceNeg";
    }
    
    public String cssForRetraitSource() {
       return isMontantTotalRetraitSourceBalanced() ? "balancePos" : "balanceNeg";
    }
    
    public void setCurrentTrancheDest(Tranche currentTrancheDest) {
        this.currentTrancheDest = currentTrancheDest;
    }
    
    public Tranche getSelectedTrancheDest() {
        return selectedTrancheDest;
    }
    
    public void setSelectedTrancheDest(Tranche selectedTrancheDest) {
        this.selectedTrancheDest = selectedTrancheDest;
    }

    public boolean isInclureEngage() {
        return inclureEngage;
    }
    
    public void setInclureEngage(boolean inclureEngage) {
        this.inclureEngage = inclureEngage;
    }
    
    public NSArray<ReportInfo> reportInfos() {
        NSArray<ReportInfo> reportInfos = _reportInfosForTranche.objectForKey(trancheSource().primaryKey());
        if (reportInfos == null) {
            NSMutableArray<ReportInfo> reportInfosTmp = new NSMutableArray<ReportInfo>();
            for (RepartPartenaireTranche repartSource : trancheSource().toRepartPartenaireTranches()) {
                reportInfosTmp.addObject(new ReportInfo(repartSource, getSelectedTrancheDest()));
            }
            reportInfos = reportInfosTmp.immutableClone();
            _reportInfosForTranche.setObjectForKey(reportInfos, trancheSource().primaryKey());
        }
        return reportInfos;
    }
    
    public ReportInfo getCurrentReportInfo() {
        return currentReportInfo;
    }
    
    public void setCurrentReportInfo(ReportInfo currentReportInfo) {
        this.currentReportInfo = currentReportInfo;
    }
    
}