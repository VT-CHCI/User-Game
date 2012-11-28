package edu.vt.cs.research.usergame.ui.components;

import java.util.Random;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.space.CastingTupleSpace;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.ControlTuple;
import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;
import edu.vt.cs.research.usergame.ui.controllers.MomentCardController;

public class MomentCard extends Composite implements PaintListener
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(MomentCard.class.getName());
    public int temp;
    private Game game;
    int randomInt;
    private MomentCardTuple tuple;
    private MomentCardController controller;

    // Convenience references
    private URL sourceURL;
    private Image sourceImage;
    private Rectangle sourceImageBounds;

    private boolean frozen;

    public static final int CARD_WIDTH = 220;
    public static final int CARD_HEIGHT = 165;
    public static final int CARD_BORDER_THICKNESS = 5;

    private Label signLabel;
    public int localX;//should add getters and setters.
    public int localY;
    public MomentCard oppositeCard;
    public MomentCard getOtherCard()
    {
    	return oppositeCard;
    }
    
    public MomentCard(Composite parent, int style, Game game, MomentCardTuple tuple,MomentCard otherMoment) throws MalformedURLException, TupleSpaceException
    {
        super(parent, style);
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(100);
        oppositeCard=otherMoment;
        localX=tuple.getX();
        localY=tuple.getY();
        this.game = game;
        this.tuple = tuple;
        controller = new MomentCardController(this, game);

        setBounds(tuple.getX(), tuple.getY(), CARD_WIDTH, CARD_HEIGHT);

        signLabel = new Label(this, SWT.BORDER);
        signLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        signLabel.setLocation(CARD_BORDER_THICKNESS * 2, CARD_BORDER_THICKNESS * 2);
        if (tuple.getSignCard() == null || tuple.getSignCard().equals(""))
        {
            signLabel.setVisible(false);
        }
        else
        {
            signLabel.setText(tuple.getSignCard());
            signLabel.setSize(signLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            signLabel.setVisible(true);
        }
        DragSource source = new DragSource(signLabel, DND.DROP_MOVE);
        source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        source.addDragListener(game.getSignCardController());

        // Set the visibility of this UI component based on the Moment Card Tuple's location (in the stage space or board space)
        if (parent == game.getStage() && tuple.getSpace().equals(MomentCardTuple.BOARD_SPACE))
        {
            setVisible(false);
        }
        else if (parent == game.getBoard() && tuple.getSpace().equals(MomentCardTuple.STAGE_SPACE))
        {
            setVisible(false);
        }

        addPaintListener(this);
    }

    public MomentCardTuple getTuple()
    {
        return tuple;
    }

    public URL getSourceURL()
    {
        return sourceURL;
    }

    public Image getSourceImage()
    {
        return sourceImage;
    }

    public boolean isFrozen()
    {
        return frozen;
    }

    public void setImage(URL url, Image image) throws TupleSpaceException
    {
        sourceURL = url;
        sourceImage = image;
        sourceImageBounds = sourceImage.getBounds();

        tuple.setSource(url.toString());
    }

    public void setSpace(String string) throws TupleSpaceException
    {
        logger.finest("Changing space from '" + tuple.getSpace() + "' to '" + string + "'");
        tuple.setSpace(string);

        if (string.equals(MomentCardTuple.STAGE_PHANTOM_SPACE) || string.equals(MomentCardTuple.BOARD_PHANTOM_SPACE))
        {
            frozen = true;
        }
        else
        {
            frozen = false;
        }

        if (getParent() == game.getBoard())
        {
            if (string.equals(MomentCardTuple.BOARD_SPACE) || string.equals(MomentCardTuple.BOARD_PHANTOM_SPACE))
            {
                System.out.println("setVisible 1");
                setVisible(true);
            }
            else
            {
                setVisible(false);
            }
        }
        else if (getParent() == game.getStage())
        {
            if (string.equals(MomentCardTuple.STAGE_SPACE) || string.equals(MomentCardTuple.STAGE_PHANTOM_SPACE))
            {Random rand = new Random();
            
            	//setStageLocation(20,20);
            System.out.println("setVisible 2");
            if (temp==1){
            temp =0;}
            else{
                setVisible(true);
            }
            }
            else
            {
                setVisible(false);
            }
        }

        //redraw();
    }

    public void setSignCard(String sign) throws TupleSpaceException
    {
        tuple.setSignCard(sign);
        if (sign == null || sign.equals(""))
        {
            signLabel.setText("");
            signLabel.setSize(0, 0);
            signLabel.setVisible(false);
        }
        else
        {
            signLabel.setText(sign);
            signLabel.setSize(signLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            signLabel.setVisible(true);
        }
    }

    /**
     * Set the board location of this Moment Card
     * @param point This point should be specified in game coordinates. If <code>null</code>, then both the x and y values of the board location are set to null
     * @throws TupleSpaceException If an error occurred while contacting the Tuple space
     */
    public void setBoardLocation(Point point) throws TupleSpaceException
    {
        if (point == null)
        {
            tuple.setBoardX(null);
            tuple.setBoardY(null);
        }
        else
        {
            setBoardLocation(point.x, point.y);
        }
    }

    public void setBoardLocation(int x, int y) throws TupleSpaceException
    {
        tuple.setBoardX(x);
        tuple.setBoardY(y);
    }
    public void setStageLocation(Point point)
    {
        setStageLocation(point.x, point.y);
    }

    public void setStageLocation(int x, int y)
    {
        logger.finest("Changing location from " + "(" + getLocation().x + ", " + getLocation().y + ") to: (" + x + ", " + y + ")");
        try
        {

        	tuple.setX(x);
            tuple.setY(y);
            System.out.println("At the setStageLocation, Local X is"+localX+"Local Y is"+localY);
            super.setLocation(localX, localY);
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
    }
    /*
     * The Mac OS X implementation of SWT does not simply pass the parameters of this method onto the method setLocation(int x, int y)
     */
    @Override
    public void setLocation(Point point)
    {
        setLocation(point.x, point.y);
    }

    @Override
    public void setLocation(int x, int y)
    {
        logger.finest("Changing location from " + "(" + getLocation().x + ", " + getLocation().y + ") to: (" + x + ", " + y + ")");
        try
        {

        	tuple.setX(x);
            tuple.setY(y);
            System.out.println("At the setLocation, Local X is"+localX+"Local Y is"+localY);
            super.setLocation(x, y);
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
    }

    public void paintControl(PaintEvent e)
    {
        Display display = Display.getCurrent();
        //System.out.println(display);
        Point controlSize = ((Control)e.getSource()).getSize();
        GC gc = e.gc;

        if (sourceImage != null)
        {
            if (isFrozen())
            // Draw the frozen version of the card
            {
                // Draw the card background
                gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
                gc.fillRectangle(0, 0, controlSize.x, controlSize.y);
            }
            else
            // Draw the unfrozen version of the card
            {
                // Draw the card background
                gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
                gc.fillRectangle(0, 0, controlSize.x, controlSize.y);
            }

            // Draw the border
            gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
            gc.drawRectangle(0, 0, controlSize.x - 1, controlSize.y - 1);

            gc.drawImage(sourceImage, 0, 0, sourceImageBounds.width, sourceImageBounds.height, CARD_BORDER_THICKNESS, CARD_BORDER_THICKNESS, controlSize.x - (2 * CARD_BORDER_THICKNESS), controlSize.y - (2 * CARD_BORDER_THICKNESS));
        }
    }

    public void addSignCard(String sign) throws TupleSpaceException
    {
        logger.finer("Adding Sign Card " + sign + " to Moment Card " + tuple);

        CastingTupleSpace tupleSpace = game.getTupleSpace();
        tupleSpace.waitToTake(ControlTuple.SIGN_CONTROL_TUPLE);

        SignCardTuple signCardTuple = new SignCardTuple();
        signCardTuple.setText(sign);

        SignCardTuple sharedSignCardTuple = (SignCardTuple)tupleSpace.take(signCardTuple);
        if (sharedSignCardTuple != null)
        {
            tupleSpace.waitToTake(ControlTuple.MOMENT_CONTROL_TUPLE);

            MomentCardTuple momentCardTemplate = new MomentCardTuple();
            momentCardTemplate.setSource(tuple.getSource());
            MomentCardTuple sharedMomentCardTuple = (MomentCardTuple)tupleSpace.take(momentCardTemplate);

            if (sharedMomentCardTuple != null)
            {
                sharedMomentCardTuple.setSignCard(sign);
                sharedSignCardTuple.setMomentCard(tuple.getSource());

                tupleSpace.write(sharedMomentCardTuple);
                tupleSpace.write(sharedSignCardTuple);
            }

            tupleSpace.write(ControlTuple.MOMENT_CONTROL_TUPLE);
        }

        tupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);
    }

    public void moveSignCardTo(MomentCard momentCard, String sign) throws TupleSpaceException
    {
        logger.finer("Moving Sign Card " + sign + " from Moment Card " + tuple + " to Moment Card " + momentCard.getTuple());

        CastingTupleSpace tupleSpace = game.getTupleSpace();
        tupleSpace.waitToTake(ControlTuple.SIGN_CONTROL_TUPLE);

        SignCardTuple template = new SignCardTuple();
        template.setText(sign);

        SignCardTuple sharedSignCardTuple = (SignCardTuple)tupleSpace.take(template);
        if (sharedSignCardTuple != null)
        {
            tupleSpace.waitToTake(ControlTuple.MOMENT_CONTROL_TUPLE);

            MomentCardTuple sharedMomentCardSourceTuple = (MomentCardTuple)tupleSpace.take(tuple);
            MomentCardTuple sharedMomentCardSinkTuple = (MomentCardTuple)tupleSpace.take(momentCard.getTuple());

            if (sharedMomentCardSourceTuple != null)
            {
                sharedMomentCardSourceTuple.setSignCard("");
                sharedMomentCardSinkTuple.setSignCard(sign);
                sharedSignCardTuple.setMomentCard(momentCard.getTuple().getSource());

                tupleSpace.write(sharedMomentCardSourceTuple);
                tupleSpace.write(sharedMomentCardSinkTuple);
                tupleSpace.write(sharedSignCardTuple);
            }

            tupleSpace.write(ControlTuple.MOMENT_CONTROL_TUPLE);
        }

        tupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);
    }

    public void removeSignCard() throws TupleSpaceException
    {
        logger.finer("Removing Sign Card from Moment Card " + tuple);

        String sign = tuple.getSignCard();

        CastingTupleSpace tupleSpace = game.getTupleSpace();
        tupleSpace.waitToTake(ControlTuple.SIGN_CONTROL_TUPLE);

        SignCardTuple template = new SignCardTuple();
        template.setText(sign);

        SignCardTuple sharedSignCardTuple = (SignCardTuple)tupleSpace.take(template);
        if (sharedSignCardTuple != null)
        {
            tupleSpace.waitToTake(ControlTuple.MOMENT_CONTROL_TUPLE);

            MomentCardTuple momentCardTemplate = new MomentCardTuple();
            momentCardTemplate.setSource(tuple.getSource());
            MomentCardTuple sharedMomentCardTuple = (MomentCardTuple)tupleSpace.take(momentCardTemplate);

            if (sharedMomentCardTuple != null)
            {
                sharedMomentCardTuple.setSignCard("");
                sharedSignCardTuple.setMomentCard("");

                tupleSpace.write(sharedMomentCardTuple);
                tupleSpace.write(sharedSignCardTuple);
            }

            tupleSpace.write(ControlTuple.MOMENT_CONTROL_TUPLE);
        }

        tupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);
    }

    public boolean hasSignCard() throws TupleSpaceException
    {
        String sign = tuple.getSignCard();
        if (sign == null || sign.equals(""))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
