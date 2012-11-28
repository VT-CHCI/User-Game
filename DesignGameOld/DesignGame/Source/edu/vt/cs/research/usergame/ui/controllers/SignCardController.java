package edu.vt.cs.research.usergame.ui.controllers;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.ibm.tspaces.TupleSpaceException;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;
import edu.vt.cs.research.usergame.ui.components.MomentCard;

public class SignCardController implements DragSourceListener, DropTargetListener
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(SignCardController.class.getName());

    private Game game;
    private List list;
    private Set<String> signCards = new HashSet<String>();

    private Object source;
    private Object sink;

    public SignCardController(Game game, List list)
    {
        this.game = game;
        this.list = list;

        DragSource source = new DragSource(list, DND.DROP_MOVE);
        source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        source.addDragListener(this);

        DropTarget target = new DropTarget(list, DND.DROP_MOVE);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(this);
    }

    public void setSignCards(SignCardTuple[] signCardTuples) throws TupleSpaceException
    {
        int numSignCards = signCardTuples.length;
        String[] signCardArray = new String[numSignCards];
        for (int i = 0; i < numSignCards; ++i)
        {
            signCardArray[i] = signCardTuples[i].getText();
            signCards.add(signCardArray[i]);
        }

        list.setItems(signCardArray);
    }

    // Drag Source Methods

    public void dragStart(DragSourceEvent e)
    {
        DragSource dragSource = (DragSource)e.widget;
        Control parent = dragSource.getControl();

        logger.finest("source parent: " + parent.getClass());

        if (parent instanceof Label)
        // If this Sign Card is being dragged from a Moment Card
        {
            if (parent.isEnabled())
            {
                e.doit = true;
                source = parent.getParent();
            }
            else
            {
                e.doit = false;
            }
        }
        else if (parent instanceof List)
        // If this Sign Card is being dragged from the Sign Card list
        {
            if (list.getSelection().length > 0)
            {
                e.doit = true;
                source = parent;
            }
            else
            {
                e.doit = false;
            }
        }
    }

    public void dragSetData(DragSourceEvent e)
    {
        DragSource dragSource = (DragSource)e.widget;
        Control parent = dragSource.getControl();

        if (parent instanceof Label)
        // If this Sign Card is being dragged from a Moment Card
        {
            Label label = (Label)parent;
            e.data = label.getText();
        }
        else if (parent instanceof List)
        // If this Sign Card is being dragged from the Sign Card list
        {
            String[] selectedSigns = list.getSelection();
            if (selectedSigns.length > 0)
            {
                e.data = selectedSigns[0];
            }
        }
    }

    public void dragFinished(DragSourceEvent e)
    {
        // Nothing to do
    }

    // Drop Target Methods

    public void dragEnter(DropTargetEvent e)
    {
        if (e.detail == DND.DROP_DEFAULT)
        {
            DropTarget dropTarget = (DropTarget)e.widget;
            Control parent = dropTarget.getControl();

            if (parent instanceof MomentCard)
            {
                try
                {
                    MomentCard momentCard = (MomentCard)parent;
                    if (momentCard.getParent() == game.getBoard() && !momentCard.isFrozen() && !momentCard.hasSignCard())
                    {
                        e.detail = DND.DROP_MOVE;
                    }
                    else
                    {
                        e.detail = DND.DROP_NONE;
                    }
                }
                catch (TupleSpaceException exception)
                {
                    exception.printStackTrace();
                }
            }
            else if (parent instanceof List)
            {
                e.detail = DND.DROP_MOVE;
            }

            if ((e.operations & DND.DROP_MOVE) == 0)
            {
                e.detail = DND.DROP_NONE;
            }
        }

        for (int i = 0; i < e.dataTypes.length; ++i)
        {
            if (TextTransfer.getInstance().isSupportedType(e.dataTypes[i]))
            {
                e.currentDataType = e.dataTypes[i];
            }
        }
    }

    public void dragLeave(DropTargetEvent e)
    {
        // Nothing to do
    }

    public void dragOperationChanged(DropTargetEvent e)
    {
        // Nothing to do
    }

    public void dragOver(DropTargetEvent e)
    {
        e.feedback = DND.FEEDBACK_SELECT;
    }

    public void drop(DropTargetEvent e)
    {
        DropTarget dropTarget = (DropTarget)e.widget;
        Control parent = dropTarget.getControl();

        sink = parent;

        logger.finest("sink parent: " + parent.getClass());

        if (source instanceof MomentCard && sink instanceof MomentCard)
        {
            logger.finer("Dragging from Moment Card to Moment Card");
            if (source != sink)
            {
                MomentCard sourceMomentCard = (MomentCard)source;
                MomentCard sinkMomentCard = (MomentCard)sink;
                try
                {
                    sourceMomentCard.moveSignCardTo(sinkMomentCard, (String)e.data);
                }
                catch (TupleSpaceException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else if (source instanceof MomentCard && sink instanceof List)
        {
            logger.finer("Dragging from Moment Card to list");
            MomentCard momentCard = (MomentCard)source;
            try
            {
                momentCard.removeSignCard();
            }
            catch (TupleSpaceException exception)
            {
                exception.printStackTrace();
            }
        }
        else if (source instanceof List && sink instanceof MomentCard)
        {
            logger.finer("Dragging from list to Moment Card");
            MomentCard momentCard = (MomentCard)sink;
            try
            {
                momentCard.addSignCard((String)e.data);
            }
            catch (TupleSpaceException exception)
            {
                exception.printStackTrace();
            }
        }
        else if (source instanceof List && sink instanceof List)
        {
            logger.finer("Dragging from list to list");
            // Nothing to do
        }
        source = null;
        sink = null;
    }

    public void dropAccept(DropTargetEvent e)
    {
        // Nothing to do
    }
}
