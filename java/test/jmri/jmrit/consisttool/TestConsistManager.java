// TestConsistManager.java
package jmri.jmrit.consisttool;

import java.util.ArrayList;
import java.util.HashMap;
import jmri.Consist;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.ConsistManager;
import jmri.DccLocoAddress;

/**
 * Consist Manager used for consist tool tests.
 *
 * @author Paul Bender Copyright (C) 2015
 * @version $Revision$
 */
public class TestConsistManager extends jmri.implementation.AbstractConsistManager {

    public TestConsistManager() {
        super();
    }

    /**
     * Add a new Consist with the given address to the consistTable/consistList
     */
    protected Consist addConsist(DccLocoAddress address){
         // no operation for now.
         return null;
    }

    /**
     * Does this implementation support a command station consist?
     */
    @Override
    public boolean isCommandStationConsistPossible(){
        return false;
    }

    /**
     * Does a CS consist require a separate consist address? (or is the lead
     * loco to be used for the consist address)
     */
    @Override
    public boolean csConsistNeedsSeperateAddress(){
        return false;
    }

    @Override
    public void requestUpdateFromLayout() {
    }

}