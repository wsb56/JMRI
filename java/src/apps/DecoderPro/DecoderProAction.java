package apps.DecoderPro;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Create a new DecoderPro start window
 *
 * @author	Bob Jacobsen (C) 2014
 * @version $Revision$
 */
public class DecoderProAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 938739183249391676L;

    public DecoderProAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DecoderProAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public DecoderProAction(String s) {
        super(s);
    }

    public DecoderProAction() {
        this("DecoderPro");
    }

    apps.AppsLaunchFrame frame = null;

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
        if (frame == null) {
            frame = new apps.AppsLaunchFrame(new DecoderProPane(), "DecoderPro");
        }
        frame.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
