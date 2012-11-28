package edu.vt.cs.research.usergame.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.ibm.tspaces.TupleSpaceException;

import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;
import edu.vt.cs.research.usergame.util.ArrayConverter;

/**
 * TODO: Don't load duplicate words
 * @author Nefaur Khandker
 */
public class SignCardFileManager
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(SignCardFileManager.class.getName());
    
    public static SignCardTuple[] read(File file) throws FileNotFoundException, ParseException
    {
        logger.fine("Reading sign card file: " + file);
        
        Scanner scanner = new Scanner(file);
        List<SignCardTuple> tuples = new LinkedList<SignCardTuple>();

        int lineNumber = 1;
        String text = "";

        try
        {
            while (scanner.hasNext())
            {
                text = scanner.nextLine();
                if (!text.equals(""))
                {
                    SignCardTuple tuple = new SignCardTuple();
                    tuple.setText(text);
                    tuple.setMomentCard("");
                    tuples.add(tuple);
                }
                ++lineNumber;
            }
        }
        catch (TupleSpaceException e)
        {
            throw new ParseException("Unable to load file. Problem with this line: " + text, lineNumber);
        }

        return ArrayConverter.convertToSignCardTupleArray(tuples.toArray());
    }

    public static void write(File file, MomentCardTuple[] momentCards)
    {

    }
}
