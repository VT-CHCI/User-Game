package edu.vt.cs.research.usergame.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.ibm.tspaces.Callback;
import com.ibm.tspaces.SuperTuple;
import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.space.CastingTupleSpace;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.ui.components.MomentCard;

public class MomentSynchronizer implements Runnable
{
    private class Event
    {
        public String type;
        public MomentCardTuple momentCardTuple;

        public Event(String type, MomentCardTuple momentCardTuple)
        {
            this.type = type;
            this.momentCardTuple = momentCardTuple;
        }
    }

    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(MomentSynchronizer.class.getName());

    private Game game;

    private int registrationID;
    private Callback momentCardWriteHandler;
    private Queue<Event> momentCardEvents = new LinkedList<Event>();
    private boolean enabled = false;

    public MomentSynchronizer(CastingTupleSpace tupleSpace, Game game) throws TupleSpaceException
    {
        this.game = game;

        initMomentCardWriteHandler();

        MomentCardTuple momentCardTemplate = new MomentCardTuple();
        registrationID = tupleSpace.eventRegister(CastingTupleSpace.WRITE, momentCardTemplate.getTuple(), momentCardWriteHandler, true);
    }
    
    public void dispose() throws TupleSpaceException
    {
        game.getTupleSpace().eventDeRegister(registrationID);
    }

    private void initMomentCardWriteHandler()
    {
        momentCardWriteHandler = new Callback()
        {
            public boolean call(String eventName, String tSpaceName, int sequenceNumber, SuperTuple tuple, boolean isException)
            {
                try
                {
                    if (!isException)
                    {
                        MomentCardTuple momentCardTuple = new MomentCardTuple();
                        momentCardTuple.loadTuple(tuple);
                        logger.finest("Moment Card Tuple written: " + momentCardTuple);
                        MomentSynchronizer.this.momentCardEvents.add(new Event(CastingTupleSpace.WRITE, momentCardTuple));
                        Display.getDefault().asyncExec(MomentSynchronizer.this);
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

    protected void updateWrittenMomentCard(MomentCardTuple momentCardTuple) throws TupleSpaceException, MalformedURLException
    {
        String source = momentCardTuple.getSource();
        URL sourceURL = new URL(source);
        MomentCard momentCardStageVersion = game.getStage().getMomentCard(sourceURL);
        MomentCard momentCardBoardVersion = game.getBoard().getMomentCard(sourceURL);
        MomentCardTuple momentCardTupleStageVersion = momentCardStageVersion.getTuple();
        MomentCardTuple momentCardTupleBoardVersion = momentCardBoardVersion.getTuple();

        momentCardTupleStageVersion.setBoardX(momentCardTuple.getBoardX());
        momentCardTupleBoardVersion.setBoardX(momentCardTuple.getBoardX());

        momentCardTupleStageVersion.setBoardY(momentCardTuple.getBoardY());
        momentCardTupleBoardVersion.setBoardY(momentCardTuple.getBoardY());

        momentCardStageVersion.setTupleLocation(momentCardTuple.getX(), momentCardTuple.getY());
        momentCardBoardVersion.setTupleLocation(momentCardTuple.getX(), momentCardTuple.getY());
        
        momentCardStageVersion.setSpace(momentCardTuple.getSpace());
        momentCardBoardVersion.setSpace(momentCardTuple.getSpace());
        
        momentCardStageVersion.setSignCard(momentCardTuple.getSignCard());
        momentCardBoardVersion.setSignCard(momentCardTuple.getSignCard());
        
        game.getStage().cardMoved(momentCardStageVersion);
        game.getBoard().cardMoved(momentCardBoardVersion);
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
                while (!momentCardEvents.isEmpty())
                {
                    Event event = momentCardEvents.remove();
                    if (event.type == CastingTupleSpace.WRITE)
                    {
                        updateWrittenMomentCard(event.momentCardTuple);
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
