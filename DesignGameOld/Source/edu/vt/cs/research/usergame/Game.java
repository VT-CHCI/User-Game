package edu.vt.cs.research.usergame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.SRITuplesException;
import com.sri.tuples.common.space.CastingTupleSpace;

import edu.vt.cs.research.usergame.io.MomentCardFileManager;
import edu.vt.cs.research.usergame.io.MomentSynchronizer;
import edu.vt.cs.research.usergame.io.SignCardFileManager;
import edu.vt.cs.research.usergame.io.SignSynchronizer;
import edu.vt.cs.research.usergame.tuples.ControlTuple;
import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;
import edu.vt.cs.research.usergame.ui.MainWindow;
import edu.vt.cs.research.usergame.ui.components.Board;
import edu.vt.cs.research.usergame.ui.components.MomentCard;
import edu.vt.cs.research.usergame.ui.components.Stage;
import edu.vt.cs.research.usergame.ui.controllers.SignCardController;
import edu.vt.cs.research.usergame.ui.dialogs.GameManagerDialog;

/**
 * The class containing all of the game-related information
 * @author nefaurk
 */
public class Game
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(Game.class.getName());

    /**
     * The number of times to attempt a connection
     */
    private static final int CONNECTION_TRIES = 5;
    /**
     * The number of milliseconds to wait while attempting to make a connection
     */
    private static final int CONNECTION_WAIT_TIME = 15000;
    /**
     * The number of milliseconds to wait while attempting to execute a command
     */
    private static final int COMMAND_WAIT_TIME = 15000;

    public static final String IMAGE_USER_GAME_16x16 = "User Game 16x16.png";
    public static final String IMAGE_USER_GAME_32x32 = "User Game 32x32.png";
    public static final String IMAGE_USER_GAME_64x64 = "User Game 64x64.png";
    public static final String IMAGE_USER_GAME_256x256 = "User Game 256x256.png";

    /**
     * The name of the resource for the cursor that is displayed to indicate that the user can can across the space the cursor is hovering over
     */
    public static final String CURSOR_HAND = "hand.ico";
    /**
     * The name of the resource for the cursor that is displayed to indicate that the user is panning across a space
     */
    public static final String CURSOR_GRAB = "grab.ico";

    /**
     * The x-coordinate value of the hot spot for the hand cursor
     */
    public static final int CURSOR_HAND_HOT_SPOT_X = 12;
    /**
     * The y-coordinate value of the hot spot for the hand cursor
     */
    public static final int CURSOR_HAND_HOT_SPOT_Y = 12;
    /**
     * The x-coordinate value of the hot spot for the grab cursor
     */
    public static final int CURSOR_GRAB_HOT_SPOT_X = 12;
    /**
     * The y-coordinate value of the hot spot for the grab cursor
     */
    public static final int CURSOR_GRAB_HOT_SPOT_Y = 12;

    /**
     * The hand cursor
     */
    private Cursor handCursor;
    /**
     * The grab cursor
     */
    private Cursor grabCursor;

    /**
     * On operating systems such as Windows and Linux, the "Ctrl" is the primary modifier used for keyboard shortcuts. For Mac OS X, the primary modifier is the "Command" key.
     */
    public static String hotkeyModifier = "Ctrl";

    /**
     * The Tuple space where this game's public information is stored
     */
    private CastingTupleSpace tupleSpace;
    /**
     * The object that mediates Moment Card changes between the Tuple space and this client
     */
    private MomentSynchronizer momentSynchronizer;
    /**
     * The object that mediates Sign Card changes between the Tuple space and this client
     */
    private SignSynchronizer signSynchronizer;
    /**
     * The window that displays the game
     */
    private ImageRegistry imageRegistry;
    /**
     * The window displaying all the controls for the game
     */
    private MainWindow mainWindow;
    /**
     * The dialog that allows users to create, join, and end games
     */
    private GameManagerDialog gameManagerDialog;
    /**
     * The staging area
     */
    private Stage stage;
    /**
     * The playing area
     */
    private Board board;
    /**
     * The list of signs
     */
    private List signCardList;
    /**
     * The controller object that manages the dragging of sign cards
     */
    private SignCardController signCardController;
    /**
     * The progress meter to which the status of long operations will be displayed
     */
    private IProgressMonitor monitor;

    static
    {
        CastingTupleSpace.setConnectionTries(CONNECTION_TRIES);
        CastingTupleSpace.setConnectionWaitTime(CONNECTION_WAIT_TIME);
    }

    /**
     * Creates the game window
     */
    public Game()
    {
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            hotkeyModifier = "Command";
        }

        mainWindow = new MainWindow(this);
    }

    /**
     * Starts the game client
     */
    public void startClient()
    {
        mainWindow.setBlockOnOpen(true);
        mainWindow.open();
        Display.getCurrent().dispose();
    }

    /**
     * This method is automatically called by mainWindow after the controls have been created
     * @param imageRegistry The imageRegistry that contains the images loaded by mainWindow
     * @param board The board control created by mainWindow
     * @param stage The stage control created by mainWindow
     * @param signCardList The sign card list control created by mainWindow
     * @param signCardController The sign card drag controller created by mainWindow
     */
    public void initialize(ImageRegistry imageRegistry, Board board, Stage stage, List signCardList, SignCardController signCardController)
    {
        this.imageRegistry = imageRegistry;
        this.board = board;
        this.stage = stage;
        this.signCardList = signCardList;
        this.signCardController = signCardController;

        // Load the custom cursors
        ImageData imageData;

        imageData = imageRegistry.get(Game.CURSOR_HAND).getImageData();
        handCursor = new Cursor(Display.getCurrent(), imageData, CURSOR_HAND_HOT_SPOT_X, CURSOR_HAND_HOT_SPOT_Y);

        imageData = imageRegistry.get(Game.CURSOR_GRAB).getImageData();
        grabCursor = new Cursor(Display.getCurrent(), imageData, CURSOR_GRAB_HOT_SPOT_X, CURSOR_GRAB_HOT_SPOT_Y);

        // Create the game manager dialog
        gameManagerDialog = new GameManagerDialog(mainWindow.getShell(), this);

        try
        {
            // Clear the client's state
            clearCurrentGame();
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Displays the game manager dialog (this is a modal dialog)
     */
    public void showGameManagerDialog()
    {
        gameManagerDialog.open();
    }

    /**
     * Ends the specified game
     * @param gameName The name of the game
     * @param serverAddress The address of the Tuple space server that is hosting the game
     * @return Whether or not the game has successfully been ended
     */
    public boolean endGame(String gameName, String serverAddress)
    {
        try
        {
            logger.fine("Tuple Space exists. Deleting...");

            if (tupleSpace != null)
            {
                String tupleSpaceServerAddress = tupleSpace.getServerID().replaceAll("\\:\\d*", ""); // Needed to strip off the port number
                if (gameName.equals(tupleSpace.getName()) && serverAddress.equals(tupleSpaceServerAddress))
                {
                    logger.fine("(Ending the current game)");
                    clearCurrentGame();
                }
            }
            else
            {
                logger.fine("(Ending a different game)");
                // TODO: Other clients that are connected to the game need to be somehow notified that this game is ending
            }

            CastingTupleSpace tupleSpaceToEnd = new CastingTupleSpace(gameName, serverAddress);
            tupleSpaceToEnd.destroy();
            logger.fine("Deleted.");

            return true;
        }
        catch (TupleSpaceException e)
        {
            logger.warning("Unable to end game " + gameName + " at server " + serverAddress);
            return false;
        }
    }

    /**
     * Creates the specified game
     * @param gameName The name of the game
     * @param serverAddress The address of the Tuple space server on which the game should be hosted
     * @param momentCardFile The file specifying the moment cards for this game
     * @param signCardFile The fike specifying the sign cards to use for this game
     * @return Whether the game was created
     */
    public boolean createGame(String gameName, String serverAddress, File momentCardFile, File signCardFile)
    {
        logger.finer("Game does not yet exist");
        try
        {
            logger.fine("Creating Tuple space...");
            CastingTupleSpace newTupleSpace = new CastingTupleSpace(gameName, serverAddress);
            logger.fine("done.");
            newTupleSpace.setCommandWaitTime(COMMAND_WAIT_TIME);

            newTupleSpace.deleteAll();
            logger.fine("Creating moment cards");
            createMomentCards(momentCardFile, newTupleSpace);
            createSignCards(signCardFile, newTupleSpace);
            // Create the control tuples
            newTupleSpace.write(ControlTuple.BOARD_CONTROL_TUPLE);
            newTupleSpace.write(ControlTuple.MOMENT_CONTROL_TUPLE);
            newTupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);

            return true;
        }
        catch (TupleSpaceException e)
        {
            return false;
        }
    }

    /**
     * Creates the moment cards for a game given a moment card file to load the cards from
     * @param momentCardFile The moment card file
     * @param tupleSpace The Tuple space to write the moment cards to
     * @return Whether the moment cards could be created
     */
    private boolean createMomentCards(File momentCardFile, CastingTupleSpace tupleSpace)
    {
        try
        {
            // Assume that there are no Moment Card Tuples currently in the Tuple space
            MomentCardTuple[] momentCardTuples = MomentCardFileManager.read(momentCardFile);

            MomentCardTuple momentCardTuple;
            for (int i = 0; i < momentCardTuples.length; ++i)
            {
                momentCardTuple = momentCardTuples[i];
                // Place the cards at random positions on the stage
                momentCardTuple.setSpace(MomentCardTuple.STAGE_SPACE);
                momentCardTuple.setX((int)(Math.random() * Stage.STAGE_WIDTH) % (Stage.STAGE_WIDTH - MomentCard.CARD_WIDTH));
                momentCardTuple.setY((int)(Math.random() * Stage.STAGE_HEIGHT) % (Stage.STAGE_HEIGHT - MomentCard.CARD_HEIGHT));
                tupleSpace.write(momentCardTuple);
            }

            return true;
        }
        catch (FileNotFoundException e)
        {
            logger.severe("Unable to read Moment Card file: " + momentCardFile);
            return false;
        }
        catch (TupleSpaceException e)
        {
            logger.severe("Unable to create game");
            return false;
        }
        catch (ParseException e)
        {
            logger.severe("Unable to read Moment Card file: " + momentCardFile + " (Problem parsing on line " + e.getErrorOffset() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates the sign cards for the game given the sign card file to load the signs from
     * @param signCardFile The sign card to load the signs from
     * @param tupleSpace The Tuple space to write the sign cards to
     * @return Whether the sign cards were successfully created
     */
    private boolean createSignCards(File signCardFile, CastingTupleSpace tupleSpace)
    {
        try
        {
            // Assume that there are no Sign Card Tuples currently in the Tuple space
            SignCardTuple[] signCardTuples = SignCardFileManager.read(signCardFile);

            SignCardTuple signCardTuple;
            for (int i = 0; i < signCardTuples.length; ++i)
            {
                signCardTuple = signCardTuples[i];
                tupleSpace.write(signCardTuple);
            }

            return true;
        }
        catch (FileNotFoundException e)
        {
            logger.severe("Unable to read Sign Card file: " + signCardFile);
            return false;
        }
        catch (TupleSpaceException e)
        {
            logger.severe("Unable to create game");
            return false;
        }
        catch (ParseException e)
        {
            logger.severe("Unable to read Sign Card file: " + signCardFile + " (Problem parsing on line " + e.getErrorOffset() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears the client's state
     * @throws TupleSpaceException If an error occurred while de-registering the Tuple space callbacks
     */
    private void clearCurrentGame() throws TupleSpaceException
    {
        if (momentSynchronizer != null)
        {
            momentSynchronizer.dispose();
        }
        if (signSynchronizer != null)
        {
            signSynchronizer.dispose();
        }
        stage.reset();
        board.reset();
        signCardList.removeAll();
    }

    /**
     * Joins a game
     * @param gameName The name of the game
     * @param serverAddress The Tuple space server on which the game is hosted
     * @return Whether the game was successfully joined
     */
    public boolean joinGame(String gameName, String serverAddress)
    {
        try
        {
            // Clear the state of the client before continuing
            clearCurrentGame();

            tupleSpace = new CastingTupleSpace(gameName, serverAddress);
            tupleSpace.setCommandWaitTime(COMMAND_WAIT_TIME);

            // To ensure that we don't miss any moment cards as they are being modified, take the moment control
            tupleSpace.waitToTake(ControlTuple.MOMENT_CONTROL_TUPLE);
            MomentCardTuple[] momentCardTuples = readMomentsInTupleSpace();
            momentSynchronizer = new MomentSynchronizer(tupleSpace, this);
            tupleSpace.write(ControlTuple.MOMENT_CONTROL_TUPLE);

            // To ensure that we don't miss any sign cards as they are being modified, take the sign control
            tupleSpace.waitToTake(ControlTuple.SIGN_CONTROL_TUPLE);
            SignCardTuple[] signCardTuples = readSignsInTupleSpace();
            signSynchronizer = new SignSynchronizer(tupleSpace, this);
            tupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);

            // Create the moment cards from the information gathered (this operation may take a while, so we need a progress monitor)
            ProgressMonitorDialog monitor = new ProgressMonitorDialog(mainWindow.getShell());
            MomentCardFileManager.setMonitorDialog(monitor);
            Image[] momentCardImages = MomentCardFileManager.loadMomentCardImages(momentCardTuples); // This operation uses the Game object's monitor that we just created
            createMomentCards(momentCardTuples, momentCardImages);
            createSignCards(signCardTuples);

            // Start the Tuple space callback handlers
            momentSynchronizer.enable();
            signSynchronizer.enable();

            return true;
        }
        catch (TupleSpaceException e)
        {
            logger.severe("Unable to join game " + gameName + " on server " + serverAddress);
            return false;
        }
        catch (SRITuplesException e)
        {
            logger.severe("Unable to join game " + gameName + " on server " + serverAddress);
            return false;
        }
        catch (IOException e)
        {
            logger.severe("Unable to join game " + gameName + " on server " + serverAddress + ": The Moment Card images could not be loaded");
            return false;
        }
    }

    /**
     * Gathers information about all the moment cards currently in the Tuple space (you should not call this unless you first have the moment control)
     * @return Tuples for the moment cards that are in the Tuple space 
     * @throws TupleSpaceException If an error occurred while reading the Tuples
     * @throws SRITuplesException If an error occurred while reading the Tuples
     */
    private MomentCardTuple[] readMomentsInTupleSpace() throws TupleSpaceException, SRITuplesException
    {
        MomentCardTuple[] momentCardTuples;
        MomentCardTuple template = new MomentCardTuple();
        Tuple moments = tupleSpace.multiRead(template);
        int numTuples = moments.numberOfFields();
        momentCardTuples = new MomentCardTuple[numTuples];
        for (int i = 0; i < numTuples; ++i)
        {
            Field field = moments.getField(i);
            momentCardTuples[i] = (MomentCardTuple)field.getValue();
        }

        return momentCardTuples;
    }

    /**
     * Gathers information about all the sign cards currently in the Tuple space (you should not call this unless you first have the sign control)
     * @return Tuples for the sign cards that are in the Tuple space
     * @throws TupleSpaceException If an error occurred while reading the Tuples
     * @throws SRITuplesException If an error occurred while reading the Tuples
     */
    private SignCardTuple[] readSignsInTupleSpace() throws TupleSpaceException, SRITuplesException
    {
        SignCardTuple[] signCardTuples;
        SignCardTuple template = new SignCardTuple();
        template.setMomentCard("");
        Tuple moments = tupleSpace.multiRead(template);
        int numTuples = moments.numberOfFields();
        signCardTuples = new SignCardTuple[numTuples];
        for (int i = 0; i < numTuples; ++i)
        {
            Field field = moments.getField(i);
            signCardTuples[i] = (SignCardTuple)field.getValue();
        }

        return signCardTuples;
    }

    /**
     * Creates two versions of the moment cards: 1 set for the board and 1 set for the stage
     * @param momentCardTuples The Tuples describing the moment cards
     * @param momentCardImages The images to display on the moment cards
     * @throws MalformedURLException If one of the moment card URLs is invalid
     * @throws TupleSpaceException If an error occurred while reading the moment card information from the moment card Tuples
     */
    private void createMomentCards(MomentCardTuple[] momentCardTuples, Image[] momentCardImages) throws MalformedURLException, TupleSpaceException
    {
        int numMomentCards = momentCardTuples.length;

        MomentCard[] stageMomentCards = new MomentCard[numMomentCards];
        MomentCard[] boardMomentCards = new MomentCard[numMomentCards];

        for (int i = 0; i < numMomentCards; ++i)
        {
            MomentCardTuple stageMomentCardTuple = (MomentCardTuple)momentCardTuples[i].clone();
            MomentCardTuple boardMomentCardTuple = (MomentCardTuple)momentCardTuples[i].clone();

            stageMomentCards[i] = new MomentCard(stage, SWT.NONE, this, stageMomentCardTuple);
            stageMomentCards[i].setImage(new URL(momentCardTuples[i].getSource()), momentCardImages[i]);

            boardMomentCards[i] = new MomentCard(board, SWT.NONE, this, boardMomentCardTuple);
            boardMomentCards[i].setImage(new URL(momentCardTuples[i].getSource()), momentCardImages[i]);
        }

        stage.setMomentCards(stageMomentCards);
        board.setMomentCards(boardMomentCards);

        // Initialize the drag card for the stage
        MomentCardTuple stageDragCardTuple = new MomentCardTuple();
        stageDragCardTuple.setSpace(MomentCardTuple.STAGE_SPACE);
        stageDragCardTuple.setX(0);
        stageDragCardTuple.setY(0);
        MomentCard stageDragCard = new MomentCard(stage, SWT.NONE, this, stageDragCardTuple);
        stage.setDragCard(stageDragCard);

        // Initialize the drag card for the board
        MomentCardTuple boardDragCardTuple = new MomentCardTuple();
        boardDragCardTuple.setSpace(MomentCardTuple.BOARD_SPACE);
        boardDragCardTuple.setX(0);
        boardDragCardTuple.setY(0);
        MomentCard boardDragCard = new MomentCard(board, SWT.NONE, this, boardDragCardTuple);
        board.setDragCard(boardDragCard);
    }

    /**
     * Initializes the sign card controller
     * @param signCardTuples The Tuples for the sign cards
     * @throws TupleSpaceException If an error occurred while reading information from the Tuples
     */
    private void createSignCards(SignCardTuple[] signCardTuples) throws TupleSpaceException
    {
        signCardController.setSignCards(signCardTuples);
    }

    public void setMonitor(IProgressMonitor monitor)
    {
        this.monitor = monitor;
    }

    public ImageRegistry getImageRegistry()
    {
        return imageRegistry;
    }

    public CastingTupleSpace getTupleSpace()
    {
        return tupleSpace;
    }

    public Board getBoard()
    {
        return board;
    }

    public Stage getStage()
    {
        return stage;
    }

    public List getSignCardList()
    {
        return signCardList;
    }

    public SignCardController getSignCardController()
    {
        return signCardController;
    }

    public Cursor getGrabCursor()
    {
        return grabCursor;
    }

    public Cursor getHandCursor()
    {
        return handCursor;
    }
}