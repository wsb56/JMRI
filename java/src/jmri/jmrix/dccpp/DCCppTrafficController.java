// DCCppTrafficController.java
package jmri.jmrix.dccpp;

import java.util.Hashtable;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import java.io.BufferedReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for implementations of DCCppInterface.
 * <P>
 * This provides just the basic interface, plus the "" static method for
 * locating the local implementation.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2004-2010
 * @author      Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on XNetTrafficController by Bob Jacobsen and Paul Bender
 */
public abstract class DCCppTrafficController extends AbstractMRTrafficController implements DCCppInterface {

    protected Hashtable<DCCppListener, Integer> mListenerMasks;

    /**
     * static function returning the TrafficController instance to use.
     *
     * @return The registered TrafficController instance for general use, if
     *         need be creating one.
     */
    @Deprecated
    static public DCCppTrafficController instance() {
        return self;
    }

    /**
     * static function setting this object as the TrafficController instance to
     * use.
     */
    @Deprecated
    protected void setInstance() {
        if (self == null) {
            self = this;
        }
    }

    static DCCppTrafficController self = null;

    /**
     * Must provide a DCCppCommandStation reference at creation time
     *
     * @param pCommandStation reference to associated command station object,
     *                        preserved for later.
     */
    DCCppTrafficController(DCCppCommandStation pCommandStation) {
        mCommandStation = pCommandStation;
        setAllowUnexpectedReply(true);
        mListenerMasks = new Hashtable<DCCppListener, Integer>();
        HighPriorityQueue = new java.util.concurrent.LinkedBlockingQueue<DCCppMessage>();
        HighPriorityListeners = new java.util.concurrent.LinkedBlockingQueue<DCCppListener>();
	log.debug("DCCppTrafficController created.");
    }

    // Abstract methods for the DCCppInterface
    abstract public boolean status();

    /**
     * Forward a preformatted DCCppMessage to the actual interface.
     *
     * @param m Message to send; will be updated with CRC
     */
    abstract public void sendDCCppMessage(DCCppMessage m, DCCppListener reply);

    protected int lengthOfByteStream(AbstractMRMessage m) {
        int len = m.getNumDataElements();
        return len + 2;
    }

    /**
     * Forward a preformatted DCCppMessage to a specific listener interface.
     *
     * @param m Message to send;
     */
    public void forwardMessage(AbstractMRListener reply, AbstractMRMessage m) {
        ((DCCppListener) reply).message((DCCppMessage) m);
    }

    /**
     * Forward a preformatted DCCppMessage to the registered DCCppListeners. NOTE:
     * this drops the packet if the checksum is bad.
     *
     * @param m Message to send # @param client is the client getting the
     *          message
     */
    public void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        // check parity
	try {
	    // NOTE: For now, just forward ALL messages without filtering
	    ((DCCppListener) client).message((DCCppReply) m);
	    // NOTE: For now, all listeners should register for DCCppInterface.ALL
	    /*
	    int mask = (mListenerMasks.get(client)).intValue();
	    if (mask == DCCppInterface.ALL) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.COMMINFO)
		       == DCCppInterface.COMMINFO
		       && (((DCCppReply) m).getElement(0)
			   == DCCppConstants.LI_MESSAGE_RESPONSE_HEADER)) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.CS_INFO)
		       == DCCppInterface.CS_INFO
		       && (((DCCppReply) m).getElement(0)
			   == DCCppConstants.CS_INFO
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.CS_SERVICE_MODE_RESPONSE
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.CS_REQUEST_RESPONSE
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.BC_EMERGENCY_STOP)) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.FEEDBACK)
		       == DCCppInterface.FEEDBACK
		       && (((DCCppReply) m).isFeedbackMessage()
			   || ((DCCppReply) m).isFeedbackBroadcastMessage())) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.THROTTLE)
		       == DCCppInterface.THROTTLE
		       && ((DCCppReply) m).isThrottleMessage()) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.CONSIST)
		       == DCCppInterface.CONSIST
		       && ((DCCppReply) m).isConsistMessage()) {
		((DCCppListener) client).message((DCCppReply) m);
	    } else if ((mask & DCCppInterface.INTERFACE)
		       == DCCppInterface.INTERFACE
		       && (((DCCppReply) m).getElement(0)
			   == DCCppConstants.LI_VERSION_RESPONSE
			   || ((DCCppReply) m).getElement(0)
			   == DCCppConstants.LI101_REQUEST)) {
		((DCCppListener) client).message((DCCppReply) m);
	    }
		*/
	} catch (NullPointerException e) {
	    // catch null pointer exceptions, caused by a client
	    // that sent a message without being a registered listener
	    ((DCCppListener) client).message((DCCppReply) m);
	}
    }

    // We use the pollMessage routines for high priority messages.
    // This means responses to time critical messages (turnout off 
    // messages).  
    java.util.concurrent.LinkedBlockingQueue<DCCppMessage> HighPriorityQueue = null;
    java.util.concurrent.LinkedBlockingQueue<DCCppListener> HighPriorityListeners = null;

    public void sendHighPriorityDCCppMessage(DCCppMessage m, DCCppListener reply) {
        try {
            HighPriorityQueue.put(m);
            HighPriorityListeners.put(reply);
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while adding High Priority Message to Queue");
        }
    }

    protected AbstractMRMessage pollMessage() {
        try {
            if (HighPriorityQueue.peek() == null) {
                return null;
            } else {
                return HighPriorityQueue.take();
            }
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while removing High Priority Message from Queue");
        }
        return null;
    }

    protected AbstractMRListener pollReplyHandler() {
        try {
            if (HighPriorityListeners.peek() == null) {
                return null;
            } else {
                return HighPriorityListeners.take();
            }
        } catch (java.lang.InterruptedException ie) {
            log.error("Interupted while removing High Priority Message Listener from Queue");
        }
        return null;
    }

    public synchronized void addDCCppListener(int mask, DCCppListener l) {
        addListener(l);
        // This is adds all the mask information.  A better way to do
        // this would be to allow updating individual bits
        mListenerMasks.put(l, Integer.valueOf(mask));
    }

    public synchronized void removeDCCppListener(int mask, DCCppListener l) {
        removeListener(l);
        // This is removes all the mask information.  A better way to do 
        // this would be to allow updating of individual bits
        mListenerMasks.remove(l);
    }

    /**
     * enterProgMode(); has to be available, even though it doesn't do anything
     * on lenz
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return null;
    }

    /**
     * enterNormalMode() returns the value of getExitProgModeMsg();
     */
    @Override
    protected AbstractMRMessage enterNormalMode() {
        //return DCCppMessage.getExitProgModeMsg();
	return null;
    }

    /**
     * programmerIdle() checks to see if the programmer associated with this
     * interface is idle or not.
     */
    @Override
    protected boolean programmerIdle() {
        if (mMemo == null) {
            return true;
        }
        return !(((jmri.jmrix.dccpp.DCCppProgrammer) mMemo.getProgrammerManager().getGlobalProgrammer()).programmerBusy());
    }

    @Override
    // endOfMessage() not really used in DCC++ .. it's handled in the Packetizer.
    protected boolean endOfMessage(AbstractMRReply msg) {
	if (msg.getElement(msg.getNumDataElements()-1) == '>')
	    return true;
	else
	    return false;
    }

    protected AbstractMRReply newReply() {
        return new DCCppReply();
    }

    /**
     * Get characters from the input source, and file a message.
     * <P>
     * Returns only when the message is complete.
     * <P>
     * Only used in the Receive thread.
     *
     * @param msg     message to fill
     * @param istream character source.
     * @throws java.io.IOException when presented by the input source.
     */
    @Override
    protected void loadChars(AbstractMRReply msg, java.io.DataInputStream istream) throws java.io.IOException {
        int i;
	boolean found_start = false;
        if (log.isDebugEnabled()) {
            log.debug("loading characters from port");
        }

	// Spin waiting for start-of-frame '<' character (and toss it)
	byte char1;
	found_start = false;
	while (!found_start) {
	    char1 = readByteProtected(istream);
	    if ((char1 & 0xFF) == '<') {
		found_start = true;
		log.debug("Found starting < ");
		break; // A bit redundant with setting the loop condition true (false)
	    } else {
		char1 = readByteProtected(istream);
	    }
	}
	// Now, suck in the rest of the message...
        for (i = 0; i < msg.maxSize(); i++) {
            char1 = readByteProtected(istream);
	    if (char1 == '>') {
		log.debug("msg found > ");
		// Don't store the >
		break;
	    } else {
		log.debug("msg read byte {}", char1);
		msg.setElement(i, char1 & 0xFF);
	    }
	}
	// TODO: Still need to strip leading and trailing whitespace.
	log.debug("Complete message = {}", msg.toString());
    }

    protected void handleTimeout(AbstractMRMessage msg, AbstractMRListener l) {
        super.handleTimeout(msg, l);
        if (l != null) {
            ((DCCppListener) l).notifyTimeout((DCCppMessage) msg);
        }
    }

    /**
     * Reference to the command station in communication here
     */
    DCCppCommandStation mCommandStation;

    /**
     * Get access to communicating command station object
     *
     * @return associated Command Station object
     */
    public DCCppCommandStation getCommandStation() {
        return mCommandStation;
    }

    /**
     * Reference to the system connection memo *
     */
    DCCppSystemConnectionMemo mMemo = null;

    /**
     * Get access to the system connection memo associated with this traffic
     * controller
     *
     * @return associated systemConnectionMemo object
     */
    public DCCppSystemConnectionMemo getSystemConnectionMemo() {
        return (mMemo);
    }

    /**
     * Set the system connection memo associated with this traffic controller
     *
     * @param m associated systemConnectionMemo object
     */
    public void setSystemConnectionMemo(DCCppSystemConnectionMemo m) {
        mMemo = m;
    }

    private DCCppTurnoutReplyCache _TurnoutReplyCache = null;

    /**
     * return an DCCppTurnoutReplyCache object associated with this traffic
     * controller.
     */
    public DCCppTurnoutReplyCache getTurnoutReplyCache() {
        if (_TurnoutReplyCache == null) {
            _TurnoutReplyCache = new DCCppTurnoutReplyCache(this);
        }
        return _TurnoutReplyCache;
    }
    static Logger log = LoggerFactory.getLogger(DCCppTrafficController.class.getName());
}


/* @(#)DCCppTrafficController.java */
