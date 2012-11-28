package edu.vt.cs.research.usergame.ui;

import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.ibm.tspaces.Field;
import com.ibm.tspaces.Tuple;
import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.SRITuplesException;
import com.sri.tuples.common.space.CastingTupleSpace;

import edu.vt.cs.research.usergame.Game;
import edu.vt.cs.research.usergame.tuples.ControlTuple;
import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.ui.components.Board;
import edu.vt.cs.research.usergame.ui.components.MomentCard;
import edu.vt.cs.research.usergame.ui.components.Space;
import edu.vt.cs.research.usergame.ui.components.Stage;
import edu.vt.cs.research.usergame.ui.controllers.SignCardController;

public class MainWindow extends ApplicationWindow
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(MainWindow.class.getName());

    /**
     * The game that this window is displaying
     */
    private Game game;
    private ImageRegistry imageRegistry;
    /**
     * The UI component for the staging area
     */
    private Stage stage;
    /**
     * The UI component for the playing area
     */
    private Board board;
    /**
     * The UI component for the list of signs
     */
    private List signCardList;
    private SignCardController signCardController;

    public MainWindow(Game game)
    {
        super(null);

        this.game = game;

        addMenuBar();
    }

    @Override
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("User Game");
        shell.setSize(800, 600);

        imageRegistry = new ImageRegistry();
        URL url;
        ImageDescriptor imageDescriptor; 
        
        url = MainWindow.class.getResource("resources/graphics/icons/" + Game.IMAGE_USER_GAME_16x16);
        logger.finer("Adding image to registry: " + url);
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(Game.IMAGE_USER_GAME_16x16, imageDescriptor);
        
        url = MainWindow.class.getResource("resources/graphics/icons/" + Game.IMAGE_USER_GAME_32x32);
        logger.finer("Adding image to registry: " + url);
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(Game.IMAGE_USER_GAME_32x32, imageDescriptor);
        
        url = MainWindow.class.getResource("resources/graphics/icons/" + Game.IMAGE_USER_GAME_64x64);
        logger.finer("Adding image to registry: " + url);
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(Game.IMAGE_USER_GAME_64x64, imageDescriptor);
        
        url = MainWindow.class.getResource("resources/graphics/icons/" + Game.IMAGE_USER_GAME_256x256);
        logger.finer("Adding image to registry: " + url);
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(Game.IMAGE_USER_GAME_256x256, imageDescriptor);
        
        url = MainWindow.class.getResource("resources/graphics/cursors/" + Game.CURSOR_HAND);
        logger.finer("Adding image to registry: " + url);
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(Game.CURSOR_HAND, imageDescriptor);
        
        url = MainWindow.class.getResource("resources/graphics/cursors/" + Game.CURSOR_GRAB);
        logger.finer("Adding image to registry: " + url);
        imageDescriptor = ImageDescriptor.createFromURL(url);
        imageRegistry.put(Game.CURSOR_GRAB, imageDescriptor);
        
        Image[] applicationImages = {imageRegistry.get(Game.IMAGE_USER_GAME_16x16), imageRegistry.get(Game.IMAGE_USER_GAME_32x32), imageRegistry.get(Game.IMAGE_USER_GAME_64x64), imageRegistry.get(Game.IMAGE_USER_GAME_256x256)};
        shell.setImages(applicationImages);
    }

    @Override
    protected MenuManager createMenuManager()
    {
        MenuManager menuManager = new MenuManager();

        MenuManager gameMenu = new MenuManager("Game");
        menuManager.add(gameMenu);

        gameMenu.add(new Action()
        {
            public Action initialize()
            {
                setText("&Game Manager...@" + Game.hotkeyModifier + "+G");
                return this;
            }

            @Override
            public void run()
            {
                game.showGameManagerDialog();
            }
        }.initialize());

        MenuManager debugMenu = new MenuManager("Debug");
        menuManager.add(debugMenu);

        debugMenu.add(new Action()
        {
            public Action initialize()
            {
                setText("&Fix Locks@" + Game.hotkeyModifier + "+L");
                return this;
            }

            @Override
            public void run()
            {
                CastingTupleSpace tupleSpace = game.getTupleSpace();

                try
                {
                    logger.info("Fixing control tuples");
                    tupleSpace.take(ControlTuple.BOARD_CONTROL_TUPLE);
                    tupleSpace.take(ControlTuple.MOMENT_CONTROL_TUPLE);
                    tupleSpace.take(ControlTuple.SIGN_CONTROL_TUPLE);

                    MomentCardTuple template;
                    Tuple moments;
                    MomentCardTuple[] momentCardTuples;
                    int numTuples;
                    
                    logger.info("Fixing locked board phantom cards");
                    template = new MomentCardTuple();
                    template.setSpace(MomentCardTuple.BOARD_PHANTOM_SPACE);
                    moments = tupleSpace.multiTake(template);
                    
                    numTuples = moments.numberOfFields();
                    momentCardTuples = new MomentCardTuple[numTuples];
                    for (int i = 0; i < numTuples; ++i)
                    {
                        Field field = moments.getField(i);
                        momentCardTuples[i] = (MomentCardTuple)field.getValue();
                        momentCardTuples[i].setSpace(MomentCardTuple.BOARD_SPACE);
                        logger.info("Fixing board moment card: " + momentCardTuples[i].getSource());
                        tupleSpace.write(momentCardTuples[i]);
                    }
                    
                    logger.info("Fixing locked stage phantom cards");
                    template = new MomentCardTuple();
                    template.setSpace(MomentCardTuple.STAGE_PHANTOM_SPACE);
                    moments = tupleSpace.multiTake(template);
                    
                    numTuples = moments.numberOfFields();
                    momentCardTuples = new MomentCardTuple[numTuples];
                    for (int i = 0; i < numTuples; ++i)
                    {
                        Field field = moments.getField(i);
                        momentCardTuples[i] = (MomentCardTuple)field.getValue();
                        momentCardTuples[i].setSpace(MomentCardTuple.STAGE_SPACE);
                        logger.info("Fixing stage moment card: " + momentCardTuples[i].getSource());
                        tupleSpace.write(momentCardTuples[i]);
                    }
                    
                    tupleSpace.write(ControlTuple.BOARD_CONTROL_TUPLE);
                    tupleSpace.write(ControlTuple.MOMENT_CONTROL_TUPLE);
                    tupleSpace.write(ControlTuple.SIGN_CONTROL_TUPLE);
                }
                catch (TupleSpaceException e)
                {
                    e.printStackTrace();
                }
                catch (SRITuplesException e)
                {
                    e.printStackTrace();
                }
            }
        }.initialize());

        debugMenu.add(new Action()
        {
            public Action initialize()
            {
                setText("&Print Tuple Space Status@" + Game.hotkeyModifier + "+P");
                return this;
            }

            @Override
            public void run()
            {
                try
                {
                    CastingTupleSpace tupleSpace = MainWindow.this.game.getTupleSpace();
                    Tuple tuples = tupleSpace.readAll();
                    MainWindow.this.logger.info(tuples.dump());
                }
                catch (TupleSpaceException e)
                {
                    logger.warning("Unable to read Tuples from Tuple space");
                }
            }
        }.initialize());

        return menuManager;
    }

    protected Control createContents(Composite parent)
    {
        GridData layoutData;

        // No idea where this random label came from (Windows version), but it needs to be removed
        Control[] children = parent.getChildren();
        if (children.length > 0)
        {
            Label label = (Label)children[0];
            label.setVisible(false);
            label.dispose();
        }

        parent.setLayout(new GridLayout());

        SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
        layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        sashForm.setLayoutData(layoutData);

        ScrolledComposite boardScrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        boardScrolledComposite.setAlwaysShowScrollBars(true);
        boardScrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        board = new Board(boardScrolledComposite, SWT.NONE, game);
        boardScrolledComposite.setContent(board);
        boardScrolledComposite.getHorizontalBar().setIncrement(MomentCard.CARD_WIDTH / Space.SCROLL_INCREMENT_DIVIDEN);
        boardScrolledComposite.getHorizontalBar().setPageIncrement(MomentCard.CARD_WIDTH);
        boardScrolledComposite.getVerticalBar().setIncrement(MomentCard.CARD_HEIGHT / Space.SCROLL_INCREMENT_DIVIDEN);
        boardScrolledComposite.getVerticalBar().setPageIncrement(MomentCard.CARD_WIDTH);

        Composite cardComposite = new Composite(sashForm, SWT.BORDER);
        cardComposite.setLayout(new GridLayout(2, false));

        ScrolledComposite stageScrolledComposite = new ScrolledComposite(cardComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        stageScrolledComposite.setAlwaysShowScrollBars(true);
        stageScrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        stage = new Stage(stageScrolledComposite, SWT.NONE, game);
        stageScrolledComposite.setContent(stage);
        stageScrolledComposite.getHorizontalBar().setIncrement(MomentCard.CARD_WIDTH / Space.SCROLL_INCREMENT_DIVIDEN);
        stageScrolledComposite.getHorizontalBar().setPageIncrement(MomentCard.CARD_WIDTH);
        stageScrolledComposite.getVerticalBar().setIncrement(MomentCard.CARD_HEIGHT / Space.SCROLL_INCREMENT_DIVIDEN);
        stageScrolledComposite.getVerticalBar().setPageIncrement(MomentCard.CARD_WIDTH);

        signCardList = new List(cardComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        signCardController = new SignCardController(game, signCardList);
        layoutData = new GridData(GridData.FILL_VERTICAL);
        layoutData.widthHint = 150;
        signCardList.setLayoutData(layoutData);

        stage.setSize(Stage.STAGE_WIDTH, Stage.STAGE_HEIGHT);

        game.initialize(imageRegistry, board, stage, signCardList, signCardController);

        return board;
    }

    public Board getBoard()
    {
        return board;
    }

    public Stage getStage()
    {
        return stage;
    }
}