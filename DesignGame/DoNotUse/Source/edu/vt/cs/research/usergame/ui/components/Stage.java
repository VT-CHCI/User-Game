package edu.vt.cs.research.usergame.ui.components;

import org.eclipse.swt.widgets.Composite;

import edu.vt.cs.research.usergame.Game;

public class Stage extends Space
{
    public static final int STAGE_WIDTH = 1200;
    public static final int STAGE_HEIGHT = 1200;
    
    public Stage(Composite parent, int style, Game game)
    {
        super(parent, style, game);
    }
}
