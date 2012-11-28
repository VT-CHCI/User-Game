package edu.vt.cs.research.usergame.ui.controllers;

import java.util.logging.Logger;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.SRITuplesException;
import com.sri.tuples.common.space.CastingTupleSpace;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.ControlTuple;
import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;
import edu.vt.cs.research.usergame.ui.components.MomentCard;
import edu.vt.cs.research.usergame.ui.components.Space;
import edu.vt.cs.research.usergame.ui.components.Stage;

public class MomentCardController implements MouseListener, MouseMoveListener
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(MomentCardController.class.getName());

    private static final int LMB = 1;
    private static final int RMB = 2;
    private static final int MMB = 3;
    private static final int MAX_MOUSE_BUTTONS = 4;

    private MomentCard momentCard;
    private Game game;

    // Mouse state variables
    private boolean[] mbDown = new boolean[MAX_MOUSE_BUTTONS];
    private Point dragStartCardLocation;
    private Point dragStartCursorLocation;
    private Space dragSpace;

    public MomentCardController(MomentCard momentCard, Game game)
    {
        this.momentCard = momentCard;
        this.game = game;

        initListeners();
    }

    public void initListeners()
    {
        momentCard.addMouseListener(this);
        momentCard.addMouseMoveListener(this);

        DropTarget target = new DropTarget(momentCard, DND.DROP_DEFAULT | DND.DROP_MOVE);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(game.getSignCardController());
    }

    public void mouseDoubleClick(MouseEvent e)
    {
        // Nothing to do
    }

    public void mouseDown(MouseEvent e)
    {
        try
        {
            if (e.button < MAX_MOUSE_BUTTONS)
            {
                if (!momentCard.isFrozen())
                {
                    mbDown[e.button] = true;

                    if (e.button == LMB)
                    {
                        if (momentCard.getParent() == game.getStage())
                        {
                            dragSpace = game.getStage();
                        }
                        else if (momentCard.getParent() == game.getBoard())
                        {
                            dragSpace = game.getBoard();
                        }

                        if (!possess())
                        {
                            mbDown[e.button] = false;
                            return;
                        }

                        dragStartCardLocation = momentCard.getLocation(); // Relative to the parent container
                        dragStartCursorLocation = momentCard.getParent().toControl(momentCard.toDisplay(e.x, e.y)); // Relative to the parent container
                        logger.finer("dragStartCursorLocation: " + dragStartCursorLocation);
                    }
                }
            }
        }
        catch (TupleSpaceException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Takes the tuple corresponding to this Moment Card from the public space and replaces it with a phantom Moment Card
     * @throws TupleSpaceException
     */
    private boolean possess() throws TupleSpaceException
    {
        game.getTupleSpace().waitToTake(ControlTuple.MOMENT_CONTROL_TUPLE);

        try
        {
            MomentCardTuple template = new MomentCardTuple();
            template.setSource(momentCard.getTuple().getSource());
            MomentCardTuple sharedTuple = (MomentCardTuple)game.getTupleSpace().take(template);
            if (sharedTuple == null)
            {
                game.getTupleSpace().write(ControlTuple.MOMENT_CONTROL_TUPLE);
                return false;
            }

            // Move the card to the phantom space
            if (sharedTuple.getSpace().equals(MomentCardTuple.STAGE_SPACE))
            {
                momentCard.setSpace(MomentCardTuple.STAGE_PHANTOM_SPACE);
                momentCard.setTupleLocation(sharedTuple.getX(), sharedTuple.getY());
                game.getTupleSpace().write(momentCard.getTuple());
                game.getStage().moveCardToTop(momentCard);

                dragSpace = game.getStage();

                MomentCard dragCard;

                dragCard = game.getStage().getDragCard();
                dragCard.setSignCard(sharedTuple.getSignCard());
                dragCard.setSpace(MomentCardTuple.STAGE_SPACE);
                dragCard.setTupleLocation(sharedTuple.getX(), sharedTuple.getY());
                dragCard.setImage(momentCard.getSourceURL(), momentCard.getSourceImage());
                game.getStage().moveCardToTop(dragCard);
                dragCard.setVisible(true);

                dragCard = game.getBoard().getDragCard();
                dragCard.setSignCard(sharedTuple.getSignCard());
                dragCard.setSpace(MomentCardTuple.BOARD_SPACE);
                dragCard.setImage(momentCard.getSourceURL(), momentCard.getSourceImage());
                game.getBoard().moveCardToTop(dragCard);
                dragCard.setVisible(false);
            }
            else if (sharedTuple.getSpace().equals(MomentCardTuple.BOARD_SPACE))
            {
                momentCard.setSpace(MomentCardTuple.BOARD_PHANTOM_SPACE);
                momentCard.setTupleLocation(sharedTuple.getX(), sharedTuple.getY());
                game.getTupleSpace().write(momentCard.getTuple());
                game.getBoard().moveCardToTop(momentCard);

                dragSpace = game.getBoard();

                MomentCard dragCard;

                dragCard = game.getBoard().getDragCard();
                dragCard.setSignCard(sharedTuple.getSignCard());
                dragCard.setSpace(MomentCardTuple.BOARD_SPACE);
                dragCard.setTupleLocation(sharedTuple.getX(), sharedTuple.getY());
                dragCard.setImage(momentCard.getSourceURL(), momentCard.getSourceImage());
                game.getBoard().moveCardToTop(dragCard);
                dragCard.setVisible(true);

                dragCard = game.getStage().getDragCard();
                dragCard.setSignCard(sharedTuple.getSignCard());
                dragCard.setSpace(MomentCardTuple.STAGE_SPACE);
                dragCard.setImage(momentCard.getSourceURL(), momentCard.getSourceImage());
                game.getStage().moveCardToTop(dragCard);
                dragCard.setVisible(false);
            }

            game.getTupleSpace().write(ControlTuple.MOMENT_CONTROL_TUPLE);
            return true;
        }
        catch (TupleSpaceException e)
        {
            game.getTupleSpace().write(ControlTuple.MOMENT_CONTROL_TUPLE);
            throw e;
        }
    }

    public void mouseUp(MouseEvent e)
    {
        try
        {
            if (e.button < MAX_MOUSE_BUTTONS)
            {
                if (mbDown[e.button])
                {
                    mbDown[e.button] = false;

                    if (e.button == LMB)
                    {
                        game.getTupleSpace().waitToTake(ControlTuple.MOMENT_CONTROL_TUPLE);

                        MomentCardTuple template = new MomentCardTuple();
                        template.setSource(momentCard.getTuple().getSource());
                        MomentCardTuple sharedTuple = (MomentCardTuple)game.getTupleSpace().take(template);

                        if (momentCard.getParent() == game.getBoard())
                        // This card was originally on the board
                        {
                            if (dragSpace == game.getBoard())
                            // This card is still on the board
                            {
                                Point absoluteCursorLocation = momentCard.toDisplay(new Point(e.x, e.y));
                                placeBoardMomentOnBoard(sharedTuple, absoluteCursorLocation);
                            }
                            else if (dragSpace == game.getStage())
                            // This card is now on the stage
                            {
                                placeBoardMomentOnStage();
                            }
                        }
                        else if (momentCard.getParent() == game.getStage())
                        // This card was originally on the stage
                        {
                            if (dragSpace == game.getBoard())
                            // This card is now on the board
                            {
                                Point absoluteCursorLocation = momentCard.toDisplay(new Point(e.x, e.y));
                                placeStageMomentOnBoard(sharedTuple, absoluteCursorLocation);
                            }
                            else if (dragSpace == game.getStage())
                            // This card is still on the stage
                            {
                            	game.getStage().getLocation()
                                placeStageMomentOnStage(e.x,e.y,momentCard.toDisplay(new Point(e.x, e.y).x,momentCard.toDisplay(new Point(e.x, e.y).y);
                            }
                        }
                        game.getBoard().hoverOverLocation(null);
                        dragSpace.getDragCard().setVisible(false);

                        game.getTupleSpace().write(ControlTuple.MOMENT_CONTROL_TUPLE);
                    }
                }
            }
        }
        catch (TupleSpaceException exception)
        {
            exception.printStackTrace();
        }
    }

    private void placeBoardMomentOnBoard(MomentCardTuple sharedTuple, Point absoluteCursorLocation) throws TupleSpaceException
    {
        game.getTupleSpace().waitToTake(ControlTuple.BOARD_CONTROL_TUPLE);

        try
        {
            MomentCard dragCard = dragSpace.getDragCard();
            Point boardCursorLocation = game.getBoard().toControl(absoluteCursorLocation);

            if (game.getBoard().isLocationTaken(boardCursorLocation))
            {
                System.out.println("Board location is already taken!");
                sharedTuple.setSpace(MomentCardTuple.BOARD_SPACE);
                game.getTupleSpace().write(sharedTuple);
            }
            else
            {
                System.out.println("Board location is not yet taken");
                game.getBoard().updateBoardLocation(dragCard, boardCursorLocation);
                game.getTupleSpace().write(dragCard.getTuple());
            }
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
        catch (SRITuplesException e)
        {
            e.printStackTrace();
        }
        finally
        {
            game.getTupleSpace().write(ControlTuple.BOARD_CONTROL_TUPLE);
        }
    }

    private void placeBoardMomentOnStage() throws TupleSpaceException
    {
        CastingTupleSpace tupleSpace = game.getTupleSpace();
        tupleSpace.waitToTake(ControlTuple.BOARD_CONTROL_TUPLE);

        try
        {
            MomentCard dragCard = dragSpace.getDragCard();
            game.getBoard().removeCard(dragCard);
            game.getStage().moveCardToTop(game.getStage().getMomentCard(dragCard.getSourceURL()));
            if (dragCard.hasSignCard())
            {
                tupleSpace.waitToTake(ControlTuple.SIGN_CONTROL_TUPLE);
                
                SignCardTuple template = new SignCardTuple();
                template.setText(dragCard.getTuple().getSignCard());

                SignCardTuple sharedSignCardTuple = (SignCardTuple)tupleSpace.take(template);
                if (sharedSignCardTuple != null)
                {
                    dragCard.setSignCard("");
                    sharedSignCardTuple.setMomentCard("");

                    tupleSpace.write(sharedSignCardTuple);
                }
                
                tupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);
            }
            tupleSpace.write(dragCard.getTuple());
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
        finally
        {
            tupleSpace.write(ControlTuple.BOARD_CONTROL_TUPLE);
        }
    }

    private void placeStageMomentOnBoard(MomentCardTuple sharedTuple, Point absoluteCursorLocation) throws TupleSpaceException
    {
        game.getTupleSpace().waitToTake(ControlTuple.BOARD_CONTROL_TUPLE);

        try
        {
            MomentCard dragCard = dragSpace.getDragCard();
            Point boardCursorLocation = game.getBoard().toControl(absoluteCursorLocation);

            if (game.getBoard().isLocationTaken(boardCursorLocation))
            {
                System.out.println("Board location is already taken!");
                sharedTuple.setSpace(MomentCardTuple.STAGE_SPACE);
                game.getTupleSpace().write(sharedTuple);
            }
            else
            {
                System.out.println("Board location is not yet taken");
                game.getBoard().updateBoardLocation(dragCard, boardCursorLocation);
                game.getTupleSpace().write(dragCard.getTuple());
            }
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
        catch (SRITuplesException e)
        {
            e.printStackTrace();
        }
        finally
        {
            game.getTupleSpace().write(ControlTuple.BOARD_CONTROL_TUPLE);
        }
    }

    private void placeStageMomentOnStage() throws TupleSpaceException
    {
        game.getTupleSpace().waitToTake(ControlTuple.BOARD_CONTROL_TUPLE);

        try
        {
            MomentCard dragCard = dragSpace.getDragCard();
            game.getTupleSpace().write(dragCard.getTuple());
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
        finally
        {
            game.getTupleSpace().write(ControlTuple.BOARD_CONTROL_TUPLE);
        }
    }

    public void mouseMove(MouseEvent e)
    {
        try
        {
            if (mbDown[LMB])
            {
                Point absoluteCursorLocation = momentCard.toDisplay(e.x, e.y);
                logger.finest("absoluteCursorLocation: " + absoluteCursorLocation);

                ScrolledComposite boardScrolledComposite = (ScrolledComposite)game.getBoard().getParent();
                Rectangle boardClientArea = boardScrolledComposite.getClientArea();
                Point boardLocation = boardScrolledComposite.toDisplay(new Point(boardClientArea.x, boardClientArea.y)); // In absolute coordinates
                Rectangle visibleBoardBounds = new Rectangle(boardLocation.x, boardLocation.y, boardClientArea.width, boardClientArea.height); // In absolute coordinates
                logger.finest("visibleBoardBounds: " + visibleBoardBounds);

                ScrolledComposite stageScrolledComposite = (ScrolledComposite)game.getStage().getParent();
                Rectangle stageClientArea = stageScrolledComposite.getBounds();
                Point stageLocation = stageScrolledComposite.toDisplay(new Point(stageClientArea.x, stageClientArea.y)); // In absolute coordinates
                Rectangle visibleStageBounds = new Rectangle(stageLocation.x, stageLocation.y, stageClientArea.width, stageClientArea.height); // In absolute coordinates
                logger.finest("visibleStageBounds: " + visibleStageBounds);

                if (visibleBoardBounds.contains(absoluteCursorLocation))
                // The user is dragging this card over the board
                {
                    logger.finest("Dragging in board space");
                    dragSpace = game.getBoard();
                    cardDraggedOnBoard(absoluteCursorLocation);
                }
                else if (visibleStageBounds.contains(absoluteCursorLocation))
                // The user is dragging this card over the stage
                {
                    logger.finest("Dragging in stage space");
                    dragSpace = game.getStage();
                    cardDraggedOnStage(absoluteCursorLocation);
                }
            }
        }
        catch (TupleSpaceException exception)
        {
            exception.printStackTrace();
        }
    }

    public void cardDraggedOnStage(Point absoluteCursorLocation) throws TupleSpaceException
    {
        Point cursorOffset = new Point(dragStartCursorLocation.x - dragStartCardLocation.x, dragStartCursorLocation.y - dragStartCardLocation.y);
        Point currentCursorLocation = game.getStage().toControl(absoluteCursorLocation);

        Point stageCursorLocation = game.getStage().toControl(absoluteCursorLocation);
        Point newLocationOnStage = new Point(stageCursorLocation.x - cursorOffset.x, stageCursorLocation.y - cursorOffset.y);

        newLocationOnStage.x = newLocationOnStage.x < 0 ? 0 : newLocationOnStage.x;
        newLocationOnStage.y = newLocationOnStage.y < 0 ? 0 : newLocationOnStage.y;
        newLocationOnStage.x = newLocationOnStage.x > Stage.STAGE_WIDTH - MomentCard.CARD_WIDTH ? Stage.STAGE_WIDTH - MomentCard.CARD_WIDTH : newLocationOnStage.x;
        newLocationOnStage.y = newLocationOnStage.y > Stage.STAGE_HEIGHT - MomentCard.CARD_HEIGHT ? Stage.STAGE_HEIGHT - MomentCard.CARD_HEIGHT : newLocationOnStage.y;
        
//        ScrolledComposite stageScrolledComposite = (ScrolledComposite)game.getStage().getParent();
//        Point stageOrigin = stageScrolledComposite.getOrigin();
//
//        Point newStageOrigin = new Point(stageOrigin.x, stageOrigin.y);
//        if (newLocationOnStage.x - stageOrigin.x < Space.AUTOSCROLL_THRESHOLD)
//        {
//            newStageOrigin.x -= Space.AUTOSCROLL_THRESHOLD;
//        }
//        else if (newLocationOnStage.x + MomentCard.CARD_WIDTH - stageOrigin.x > stageScrolledComposite.getClientArea().width - Space.AUTOSCROLL_THRESHOLD)
//        {
//            newStageOrigin.x += Space.AUTOSCROLL_THRESHOLD;            
//        }
//        if (newLocationOnStage.y - stageOrigin.y < Space.AUTOSCROLL_THRESHOLD)
//        {
//            newStageOrigin.y -= Space.AUTOSCROLL_THRESHOLD;
//        }
//        else if (newLocationOnStage.y + MomentCard.CARD_HEIGHT - stageOrigin.y > stageScrolledComposite.getClientArea().height - Space.AUTOSCROLL_THRESHOLD)
//        {
//            newStageOrigin.y += Space.AUTOSCROLL_THRESHOLD;
//        }
//        
//        if (!newStageOrigin.equals(stageOrigin))
//        {
//            stageScrolledComposite.setOrigin(newStageOrigin);
//        }
        
        game.getBoard().hoverOverLocation(null);

        MomentCard alterDragCard = game.getBoard().getDragCard();
        alterDragCard.setVisible(false);

        MomentCard dragCard = game.getStage().getDragCard();
        dragCard.setTupleLocation(newLocationOnStage);
        logger.finest("Parent = Stage, Dragged = Stage, newLocationOnStage: " + newLocationOnStage);
        dragCard.setVisible(true);

        if (momentCard.getParent() == game.getBoard())
        // The user started dragging this card on the board, but is now dragging this card on the stage
        {

        }
        else if (momentCard.getParent() == game.getStage())
        // The user started dragging this card on the stage and is continuing to do so
        {

        }
    }

    public void cardDraggedOnBoard(Point absoluteCursorLocation) throws TupleSpaceException
    {
        Point cursorOffset = new Point(dragStartCursorLocation.x - dragStartCardLocation.x, dragStartCursorLocation.y - dragStartCardLocation.y);
        Point currentCursorLocation = game.getStage().toControl(absoluteCursorLocation);

        Point boardCursorLocation = game.getBoard().toControl(absoluteCursorLocation);
        Point newLocationOnBoard = new Point(boardCursorLocation.x - cursorOffset.x, boardCursorLocation.y - cursorOffset.y);

        newLocationOnBoard.x = newLocationOnBoard.x < 0 ? 0 : newLocationOnBoard.x;
        newLocationOnBoard.y = newLocationOnBoard.y < 0 ? 0 : newLocationOnBoard.y;
        newLocationOnBoard.x = newLocationOnBoard.x > game.getBoard().getSize().x - MomentCard.CARD_WIDTH ? game.getBoard().getSize().x - MomentCard.CARD_WIDTH : newLocationOnBoard.x;
        newLocationOnBoard.y = newLocationOnBoard.y > game.getBoard().getSize().y - MomentCard.CARD_HEIGHT ? game.getBoard().getSize().y - MomentCard.CARD_HEIGHT : newLocationOnBoard.y;
        
//        ScrolledComposite boardScrolledComposite = (ScrolledComposite)game.getBoard().getParent();
//        Point boardOrigin = boardScrolledComposite.getOrigin();
//
//        Point newBoardOrigin = new Point(boardOrigin.x, boardOrigin.y);
//        if (newLocationOnBoard.x - boardOrigin.x < Space.AUTOSCROLL_THRESHOLD)
//        {
//            newBoardOrigin.x -= Space.AUTOSCROLL_THRESHOLD;
//        }
//        else if (newLocationOnBoard.x + MomentCard.CARD_WIDTH - boardOrigin.x > boardScrolledComposite.getClientArea().width - Space.AUTOSCROLL_THRESHOLD)
//        {
//            newBoardOrigin.x += Space.AUTOSCROLL_THRESHOLD;            
//        }
//        if (newLocationOnBoard.y - boardOrigin.y < Space.AUTOSCROLL_THRESHOLD)
//        {
//            newBoardOrigin.y -= Space.AUTOSCROLL_THRESHOLD;
//        }
//        else if (newLocationOnBoard.y + MomentCard.CARD_HEIGHT - boardOrigin.y > boardScrolledComposite.getClientArea().height - Space.AUTOSCROLL_THRESHOLD)
//        {
//            newBoardOrigin.y += Space.AUTOSCROLL_THRESHOLD;
//        }
//        
//        if (!newBoardOrigin.equals(boardOrigin))
//        {
//            boardScrolledComposite.setOrigin(newBoardOrigin);
//        }
        
        game.getBoard().hoverOverLocation(boardCursorLocation);

        MomentCard alterDragCard = game.getStage().getDragCard();
        alterDragCard.setVisible(false);

        MomentCard dragCard = game.getBoard().getDragCard();
        dragCard.setTupleLocation(newLocationOnBoard);
        logger.finest("Parent = Board, Dragged = Board, newLocationOnBoard: " + newLocationOnBoard);
        dragCard.setVisible(true);

        if (momentCard.getParent() == game.getBoard())
        // The user started dragging this card on the board and is continuing to do so
        {

        }
        else if (momentCard.getParent() == game.getStage())
        // The user started dragging this card on the stage, but is now dragging this card on the board
        {

        }
    }
}
