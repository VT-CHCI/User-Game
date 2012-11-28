package edu.vt.cs.research.usergame.ui.components;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.SRITuplesException;
import com.sri.tuples.common.tuple.PrefabTuple;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.MomentCardTuple;

public class Board extends Space
{
    public Canvas indicator;

    public Board(Composite parent, int style, Game game)
    {
        super(parent, style, game);

        indicator = new Canvas(this, SWT.NONE);
        indicator.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION));
        indicator.setSize(MomentCard.CARD_WIDTH, MomentCard.CARD_HEIGHT);
        indicator.setVisible(false);
    }

    @Override
    public void setMomentCards(MomentCard[] momentCards) throws TupleSpaceException
    {
        super.setMomentCards(momentCards);
        int numMomentCards = momentCards.length;

        Point size = new Point(MomentCard.CARD_WIDTH * numMomentCards, MomentCard.CARD_HEIGHT * numMomentCards);
        setSize(size);

        Point origin = new Point(MomentCard.CARD_WIDTH * (numMomentCards / 2), MomentCard.CARD_HEIGHT * (numMomentCards / 2));
        ScrolledComposite scrolledComposite = (ScrolledComposite)getParent();
        scrolledComposite.setOrigin(origin);
    }

    public void setIndicatorSpot(Point spot)
    {
        indicator.setTupleLocation(spot.x * MomentCard.CARD_WIDTH, spot.y * MomentCard.CARD_HEIGHT);
    }

    private Point convertLocationToSpot(Point location)
    {
        if (location.x < 0 || location.y < 0 || location.x > getSize().x || location.y > getSize().y)
        {
            return null;
        }
        else
        {
            return new Point(location.x / MomentCard.CARD_WIDTH, location.y / MomentCard.CARD_HEIGHT);
        }
    }

    private Point convertSpotToLocation(Point spot)
    {
        return new Point(spot.x * MomentCard.CARD_WIDTH, spot.y * MomentCard.CARD_HEIGHT);
    }

    public boolean isLocationTaken(Point location) throws TupleSpaceException, SRITuplesException
    {
        Point spot = convertLocationToSpot(location);
        if (spot == null)
        {
            return true;
        }

        MomentCardTuple template = new MomentCardTuple();
        template.setBoardX(spot.x);
        template.setBoardY(spot.y);

        PrefabTuple tuple = game.getTupleSpace().read(template);
        if (tuple == null)
        {
            return false;
        }
        return true;
    }

    public void updateBoardLocation(MomentCard momentCard, Point location) throws TupleSpaceException
    {
        Point spot = convertLocationToSpot(location);
        momentCard.setBoardLocation(spot);
        momentCard.setTupleLocation(convertSpotToLocation(spot));
    }

    public void removeCard(MomentCard momentCard) throws TupleSpaceException
    {
        momentCard.setBoardLocation(null);
    }

    public void hoverOverLocation(Point location) throws TupleSpaceException
    {
        if (location != null)
        {
            logger.finest("Hovering over location: " + location);

            MomentCardTuple template = new MomentCardTuple();
            Point spot = convertLocationToSpot(location);
            if (spot == null)
            {
                return;
            }
            template.setBoardX(spot.x);
            template.setBoardY(spot.y);

            logger.finest("Hovering over spot: " + spot);

            PrefabTuple tuple = game.getTupleSpace().read(template);
            if (tuple == null)
            {
                setIndicatorSpot(spot);
                indicator.setVisible(true);
                return;
            }
        }

        indicator.setVisible(false);
    }

    private List<Point> getAdjacentSpots(Point spot)
    {
        List<Point> result = new LinkedList<Point>();

        result.add(new Point(spot.x - 1, spot.y));
        result.add(new Point(spot.x, spot.y + 1));
        result.add(new Point(spot.x + 1, spot.y));
        result.add(new Point(spot.x, spot.y - 1));

        return result;
    }
}
