package edu.vt.cs.research.usergame.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.ibm.tspaces.TupleSpaceException;

import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.ui.components.MomentCard;

public class MomentCardImageLoader implements IRunnableWithProgress
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(MomentCard.class.getName());

    private MomentCardTuple[] momentCardTuples;
    private Image[] images;

    public MomentCardImageLoader(MomentCardTuple[] momentCardTuples)
    {
        this.momentCardTuples = momentCardTuples;
    }

    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
            monitor.beginTask("Caching Moment Cards images...", momentCardTuples.length);
            images = new Image[momentCardTuples.length];
            for (int i = 0; i < momentCardTuples.length; ++i)
            {
                monitor.subTask(String.valueOf(i + 1) + " of " + momentCardTuples.length);
                MomentCardTuple tuple = momentCardTuples[i];

                String source = tuple.getSource();
                URL sourceURL = new URL(source);
                Image sourceImage;
                if (source.startsWith("file://")) // This check is needed to allow relative local paths (for testing purposes)
                {
                    source = source.replaceFirst("^file\\:\\/\\/", "");
                    logger.fine("Loading image from local resource: " + source);
                    sourceImage = new Image(Display.getCurrent(), source);
                    logger.fine("...loaded.");
                }
                else
                {
                    logger.fine("Loading image from remote resource: " + sourceURL);
                    InputStream inputStream = sourceURL.openStream();
                    sourceImage = new Image(Display.getCurrent(), inputStream);
                    logger.fine("...loaded.");
                }

                images[i] = sourceImage;
                monitor.worked(1);
            }
            monitor.done();
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
            images = null;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            images = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            images = null;
        }
    }

    public Image[] getImages()
    {
        return images;
    }
}
