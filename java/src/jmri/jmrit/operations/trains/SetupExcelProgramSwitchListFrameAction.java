// SetupExcelProgramSwitchListFrameAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create a SetupExcelProgramSwitchListFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class SetupExcelProgramSwitchListFrameAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -569912978566932883L;

    public SetupExcelProgramSwitchListFrameAction(String s) {
        super(s);
    }

    SetupExcelProgramSwitchListFrame f = null;

    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new SetupExcelProgramSwitchListFrame();
        f.initComponents();
        f.setExtendedState(Frame.NORMAL);
    }
}

/* @(#)SetupExcelProgramSwitchListFrameAction.java */
