package edu.vt.cs.research.usergame.ui.dialogs;

import java.io.File;
import java.util.logging.Logger;

import net.miginfocom.swt.MigLayout;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.tspaces.TupleSpaceException;

import edu.vt.cs.research.usergame.Game;

public class GameManagerDialog extends TitleAreaDialog
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(GameManagerDialog.class.getName());

    public static final String CREATE_GAME_LABEL = "Create Game";
    public static final String END_GAME_LABEL = "End Game";
    public static final String JOIN_GAME_LABEL = "Join Game";

    enum Mode
    {
        CREATE, JOIN, END
    };

    private Game game;

    private Text serverAddressText;
    private Text gameNameText;
    private Text momentCardFileURLText;
    private Text signCardFileURLText;

    private Button chooseMomentCardFileButton;
    private Button chooseSignCardFileButton;

    private Mode mode;
    private Button acceptButton;

    private String serverAddress = "";
    private String gameName = "";
    private String momentCardFileURL = "";
    private String signCardFileURL = "";

    public GameManagerDialog(Shell shell, Game game)
    {
        super(shell);
        this.game = game;
    }

    @Override
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Game Manager");
    }

    @Override
    protected Control createDialogArea(Composite composite)
    {
        ImageRegistry imageRegistry = game.getImageRegistry();
        Image image = imageRegistry.get(Game.IMAGE_USER_GAME_64x64);
        setTitleImage(image);

        Composite parent = (Composite)super.createDialogArea(composite);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        MigLayout layout = new MigLayout("fillx", "[right]rel[grow,fill][right]", "[]10");
        container.setLayout(layout);

        Button button;
        Label label;
        Text text;

        button = new Button(container, SWT.RADIO);
        button.setText("Join an existing game");
        button.setLayoutData("growx, left");
        button.setSelection(true);
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                setMode(Mode.JOIN);
            }
        });

        button = new Button(container, SWT.RADIO);
        button.setText("Create a new game");
        button.setLayoutData("spanx 3, split 3, growx, left");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                setMode(Mode.CREATE);
            }
        });

        button = new Button(container, SWT.RADIO);
        button.setText("End an existing game");
        button.setLayoutData("growx, left");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                setMode(Mode.END);
            }
        });

        label = new Label(container, SWT.NONE);
        label.setText("Server address:");
        label.setLayoutData("newline");

        text = new Text(container, SWT.BORDER);
        text.setText(serverAddress);
        text.setLayoutData("spanx 2, growx 100");
        serverAddressText = text;

        label = new Label(container, SWT.NONE);
        label.setText("Game name:");
        label.setLayoutData("newline");

        text = new Text(container, SWT.BORDER);
        text.setText(gameName);
        text.setLayoutData("spanx 2, growx 100");
        gameNameText = text;

        label = new Label(container, SWT.NONE);
        label.setText("Moment Card file:");
        label.setLayoutData("newline");

        text = new Text(container, SWT.BORDER);
        text.setText(momentCardFileURL);
        text.setLayoutData("");
        momentCardFileURLText = text;

        button = new Button(container, SWT.PUSH);
        button.setText("Choose...");
        button.setLayoutData("");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
                dialog.setFilterNames(new String[] {"Moment Card Files (*.moments)"});
                dialog.setFilterExtensions(new String[] {"*.moments"});
                String fileName = dialog.open();
                if (fileName != null)
                {
                    momentCardFileURLText.setText(fileName);
                }
                else
                {
                    momentCardFileURLText.setText("");
                }
            }
        });
        chooseMomentCardFileButton = button;

        label = new Label(container, SWT.NONE);
        label.setText("Sign Card file:");
        label.setLayoutData("newline");

        text = new Text(container, SWT.BORDER);
        text.setText(signCardFileURL);
        text.setLayoutData("");
        signCardFileURLText = text;

        button = new Button(container, SWT.PUSH);
        button.setText("Choose...");
        button.setLayoutData("");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
                dialog.setFilterNames(new String[] {"Sign Card Files (*.signs)"});
                dialog.setFilterExtensions(new String[] {"*.signs"});
                String fileName = dialog.open();
                if (fileName != null)
                {
                    signCardFileURLText.setText(fileName);
                }
                else
                {
                    signCardFileURLText.setText("");
                }
            }
        });
        chooseSignCardFileButton = button;

        setTitle("Game Manager");
        setMessage("Fill out the fields below");

        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
            acceptButton = createButton(parent, IDialogConstants.OK_ID, CREATE_GAME_LABEL, true); // CREATE_GAME_LABEL is the longest label (this ensures that every label we use will fit on this button)
        }
        else
        {
            acceptButton = createButton(parent, IDialogConstants.OK_ID, CREATE_GAME_LABEL, true); // CREATE_GAME_LABEL is the longest label (this ensures that every label we use will fit on this button)
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
        }

        serverAddressText.setFocus();
    }

    @Override
    public void create()
    {
        super.create();
        setMode(Mode.JOIN);
    }

    private void setMode(Mode mode)
    {
        this.mode = mode;
        if (mode == Mode.CREATE)
        {
            acceptButton.setText(CREATE_GAME_LABEL);

            momentCardFileURLText.setEnabled(true);
            chooseMomentCardFileButton.setEnabled(true);

            signCardFileURLText.setEnabled(true);
            chooseSignCardFileButton.setEnabled(true);
        }
        else if (mode == Mode.JOIN)
        {
            acceptButton.setText(JOIN_GAME_LABEL);

            momentCardFileURLText.setEnabled(false);
            chooseMomentCardFileButton.setEnabled(false);

            signCardFileURLText.setEnabled(false);
            chooseSignCardFileButton.setEnabled(false);
        }
        else if (mode == Mode.END)
        {
            acceptButton.setText(END_GAME_LABEL);

            momentCardFileURLText.setEnabled(false);
            chooseMomentCardFileButton.setEnabled(false);

            signCardFileURLText.setEnabled(false);
            chooseSignCardFileButton.setEnabled(false);
        }
    }

    @Override
    protected void cancelPressed()
    {
        serverAddress = serverAddressText.getText();
        gameName = gameNameText.getText();
        momentCardFileURL = momentCardFileURLText.getText();
        signCardFileURL = signCardFileURLText.getText();

        super.cancelPressed();
    }

    @Override
    protected void okPressed()
    {
        serverAddress = serverAddressText.getText();
        gameName = gameNameText.getText();
        momentCardFileURL = momentCardFileURLText.getText();
        signCardFileURL = signCardFileURLText.getText();

        if (mode == Mode.CREATE)
        {
            Display.getCurrent().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (game.createGame(gameName, serverAddress, new File(momentCardFileURL), new File(signCardFileURL)))
                    {
                        setMessage("Game created.");
                    }
                    else
                    {
                        setMessage("Unabled to create game");
                    }
                }
            });
        }
        else if (mode == Mode.JOIN)
        {
            Display.getCurrent().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (game.joinGame(gameName, serverAddress))
                    {
                        acceptButton.setEnabled(true);
                        close();
                    }
                }
            });
        }
        else if (mode == Mode.END)
        {
            Display.getCurrent().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (game.endGame(gameName, serverAddress))
                    {
                        setMessage("Game ended.");
                    }
                }
            });
        }
    }
}
