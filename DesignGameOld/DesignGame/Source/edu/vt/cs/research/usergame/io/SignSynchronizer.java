package edu.vt.cs.research.usergame.io;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

import com.ibm.tspaces.Callback;
import com.ibm.tspaces.SuperTuple;
import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.space.CastingTupleSpace;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;

public class SignSynchronizer implements Runnable
{
    private class Event
    {
        public String type;
        public SignCardTuple signCardTuple;

        public Event(String type, SignCardTuple signCardTuple)
        {
            this.type = type;
            this.signCardTuple = signCardTuple;
        }
    }

    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(SignSynchronizer.class.getName());

    private Game game;

    private int registrationID;
    private Callback signCardWriteHandler;
    private Queue<Event> signCardEvents = new LinkedList<Event>();
    private boolean enabled = false;

    public SignSynchronizer(CastingTupleSpace tupleSpace, Game game) throws TupleSpaceException
    {
        this.game = game;

        initSignCardWriteHandler();

        SignCardTuple signCardTemplate = new SignCardTuple();
        registrationID = tupleSpace.eventRegister(CastingTupleSpace.WRITE, signCardTemplate.getTuple(), signCardWriteHandler, true);
    }
    
    public void dispose() throws TupleSpaceException
    {
        game.getTupleSpace().eventDeRegister(registrationID);
    }

    private void initSignCardWriteHandler()
    {
        signCardWriteHandler = new Callback()
        {
            public boolean call(String eventName, String tSpaceName, int sequenceNumber, SuperTuple tuple, boolean isException)
            {
                try
                {
                    if (!isException)
                    {
                        SignCardTuple signCardTuple = new SignCardTuple();
                        signCardTuple.loadTuple(tuple);
                        logger.finest("Sign Card Tuple written: " + signCardTuple);
                        SignSynchronizer.this.signCardEvents.add(new Event(CastingTupleSpace.WRITE, signCardTuple));
                        Display.getDefault().asyncExec(SignSynchronizer.this);
                    }
                    else
                    {
                        // TODO: Handle this exception properly
                    }
                }
                catch (TupleSpaceException e)
                {
                    e.printStackTrace();
                }

                return false;
            }
        };
    }

    protected void updateWrittenSignCard(SignCardTuple signCardTuple) throws TupleSpaceException, MalformedURLException
    {
        List signCardList = game.getSignCardList();
        String sign = signCardTuple.getText();

        if (signCardTuple.getMomentCard() == null || signCardTuple.getMomentCard().equals(""))
        {
            String[] freeSigns = signCardList.getItems();
            for (String freeSign : freeSigns)
            {
                if (freeSign.equals(sign))
                {
                    return;
                }
            }
            signCardList.add(sign);
        }
        else
        {
            String[] freeSigns = signCardList.getItems();
            for (String freeSign : freeSigns)
            {
                if (freeSign.equals(sign))
                {
                    signCardList.remove(sign);
                    return;
                }
            }
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void enable()
    {
        enabled = true;
        Display.getDefault().asyncExec(this);
    }

    public void run()
    {
        try
        {
            if (enabled)
            {
                while (!signCardEvents.isEmpty())
                {
                    Event event = signCardEvents.remove();
                    if (event.type == CastingTupleSpace.WRITE)
                    {
                        updateWrittenSignCard(event.signCardTuple);
                    }
                }
            }
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}
