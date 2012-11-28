package edu.vt.cs.research.usergame.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.graphics.Image;

import com.ibm.tspaces.TupleSpaceException;

import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.util.ArrayConverter;

/**
 * TODO: Don't load duplicate URLs
 * @author Nefaur Khandker
 */
public class MomentCardFileManager
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(MomentCardFileManager.class.getName());

    private static ProgressMonitorDialog monitorDialog;

    public static MomentCardTuple[] read(File file) throws FileNotFoundException, ParseException
    {
        logger.fine("Reading moment card file: " + file);

        Scanner scanner = new Scanner(file);
        List<MomentCardTuple> tuples = new LinkedList<MomentCardTuple>();

        int lineNumber = 1;
        String url = "";

        try
        {
            while (scanner.hasNext())
            {
                url = scanner.nextLine();
                if (!url.equals(""))
                {
                    MomentCardTuple tuple = new MomentCardTuple();
                    tuple.setSource(url);
                    tuple.setSignCard("");
                    tuples.add(tuple);
                }
                ++lineNumber;
            }
        }
        catch (TupleSpaceException e)
        {
            throw new ParseException("Unable to load file. Problem with this line: " + url, lineNumber);
        }

        return ArrayConverter.convertToMomentCardTupleArray(tuples.toArray());
    }

    public static Image[] loadMomentCardImages(MomentCardTuple[] momentCardTuples) throws IOException
    {
        MomentCardImageLoader imageLoader = new MomentCardImageLoader(momentCardTuples);

        try
        {
            monitorDialog.run(true, false, imageLoader);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        Image[] images = imageLoader.getImages();
        if (images == null)
        {
            throw new IOException();
        }
        return imageLoader.getImages();
    }

    public static void setMonitorDialog(ProgressMonitorDialog monitorDialog)
    {
        MomentCardFileManager.monitorDialog = monitorDialog;
    }
}
