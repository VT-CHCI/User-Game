package edu.vt.cs.research.usergame.ui.components;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.ibm.tspaces.TupleSpaceException;

import edu.vt.cs.research.usergame.Game;

public abstract class Space extends Composite implements MouseListener, MouseMoveListener, MouseTrackListener
{
    /**
     * The logger for this class
     */
    protected static Logger logger = Logger.getLogger(Space.class.getName());

    protected Game game;

    /**
     * The Moment cards displayed on this UI component
     */
    private Map<String, MomentCard> momentCards = new TreeMap<String, MomentCard>();
    /**
     * The UI component used to render the Moment card that is being dragged
     */
    private MomentCard dragCard;

    private static final int LMB = 1;
    private static final int RMB = 2;
    private static final int MMB = 3;
    private static final int MAX_MOUSE_BUTTONS = 4;

    public static final int SCROLL_INCREMENT_DIVIDEN = 10;

    public static final int AUTOSCROLL_THRESHOLD = 25;

    private boolean[] mbDown = new boolean[MAX_MOUSE_BUTTONS];
    Point lastCursorLocation = new Point(0, 0);

    public Space(Composite parent, int style, Game game)
    {
        super(parent, style);
        setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        this.game = game;

        addMouseListener(this);
        addMouseMoveListener(this);
        addMouseTrackListener(this);
    }

    public void reset()
    {
        setEnabled(false);

        Collection<MomentCard> allMomentCards = momentCards.values();
        for (MomentCard momentCard : allMomentCards)
        {
            momentCard.dispose();
        }

        momentCards.clear();
        if (dragCard != null)
        {
            dragCard.dispose();
            dragCard = null;
        }

        setEnabled(true);
    }

    public void setMomentCards(MomentCard[] momentCards) throws TupleSpaceException
    {
        this.momentCards.clear();
        for (int i = 0; i < momentCards.length; ++i)
        {
            this.momentCards.put(momentCards[i].getSourceURL().toString(), momentCards[i]);
        }
    }

    public MomentCard getDragCard()
    {
        return dragCard;
    }

    /**
     * @note This method should not be called more than once. After the drag card is set, you can modify it by using the reference returned by{@link #getDragCard()}/
     * @param momentCard
     */
    public void setDragCard(MomentCard momentCard)
    {
        dragCard = momentCard;
        dragCard.setVisible(false);
    }

    public void cardMoved(MomentCard momentCard)
    {
        updateScrolledCompositeBounds();
    }

    private void updateScrolledCompositeBounds()
    {
        ScrolledComposite scrolledComposite = (ScrolledComposite)getParent();
        Rectangle clientArea = scrolledComposite.getClientArea();

        // Point sizeHint = computeSize(clientArea.width, clientArea.height);
        // logger.finest("Updating bounds of space to: " + sizeHint);
        // setSize(sizeHint);
        // Rectangle bounds = computeBounds(clientArea.width, clientArea.height);
        // logger.finest("Updating bounds of space to: " + bounds);
        // setBounds(bounds);
    }

    public Rectangle computeBounds(int wHint, int hHint)
    {
        Point min = new Point(0, 0);
        Point max = new Point(0, 0);
        Control[] controls = getChildren();
        for (Control control : controls)
        {
            if (control != dragCard && control.isVisible())
            {
                Rectangle bounds = control.getBounds();
                if (bounds.x < min.x)
                {
                    min.x = bounds.x;
                }
                if (bounds.x + bounds.width > max.x)
                {
                    max.x = bounds.x + bounds.width;
                }
                if (bounds.y < min.y)
                {
                    min.y = bounds.y;
                }
                if (bounds.y + bounds.height > max.y)
                {
                    max.y = bounds.y + bounds.height;
                }
            }
        }

        int width = max.x - min.x > wHint ? max.x - min.x : wHint;
        int height = max.y - min.y > hHint ? max.y - min.y : hHint;

        Rectangle rectangle = new Rectangle(min.x, min.y, width, height);

        return rectangle;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed)
    {
        Point min = new Point(0, 0);
        Point max = new Point(0, 0);
        Control[] controls = getChildren();
        for (Control control : controls)
        {
            if (control != dragCard && control.isVisible())
            {
                Rectangle bounds = control.getBounds();
                if (bounds.x < min.x)
                {
                    min.x = bounds.x;
                }
                if (bounds.x + bounds.width > max.x)
                {
                    max.x = bounds.x + bounds.width;
                }
                if (bounds.y < min.y)
                {
                    min.y = bounds.y;
                }
                if (bounds.y + bounds.height > max.y)
                {
                    max.y = bounds.y + bounds.height;
                }
            }
        }
        int width = max.x - min.x > wHint ? max.x - min.x : wHint;
        int height = max.y - min.y > hHint ? max.y - min.y : hHint;

        return (new Point(width, height));
    }

    public void moveCardToTop(MomentCard momentCard)
    {
        Control[] controls = getChildren();
        momentCard.moveAbove(controls[0]);
    }

    public MomentCard getMomentCard(URL sourceURL)
    {
        return (momentCards.get(sourceURL.toString()));
    }

    public void finalizeCardLocation(MomentCard momentCard)
    {
        // Nothing to do
    }

    public void mouseDoubleClick(MouseEvent e)
    {
        // Nothing to do
    }

    public void mouseDown(MouseEvent e)
    {
        if (e.button == LMB && !mbDown[LMB])
        {
            mbDown[LMB] = true;
            setCursor(game.getGrabCursor());
            
            lastCursorLocation.x = e.x;
            lastCursorLocation.y = e.y;
        }
    }

    public void mouseUp(MouseEvent e)
    {
        if (e.button == LMB && mbDown[LMB])
        {
            mbDown[LMB] = false;
            setCursor(game.getHandCursor());
        }
    }

    public void mouseMove(MouseEvent e)
    {
        if (mbDown[LMB])
        {
            ScrolledComposite scrolledComposite = (ScrolledComposite)getParent();
            Point origin = scrolledComposite.getOrigin();

            int dX = e.x - lastCursorLocation.x;
            int dY = e.y - lastCursorLocation.y;

            int x = origin.x - dX;
            int y = origin.y - dY;

            scrolledComposite.setOrigin(x, y);
        }
    }

    public void mouseEnter(MouseEvent e)
    {
        setCursor(game.getHandCursor());
    }

    public void mouseExit(MouseEvent e)
    {
        setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW));
    }

    public void mouseHover(MouseEvent e)
    {
        // Nothing to do
    }
}
