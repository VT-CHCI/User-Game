package edu.vt.cs.research.usergame;

import java.io.File;
import java.util.logging.Logger;

/**
 * The entry point for this application. Used to launch a client for the User Game
 * @author nefaurk
 */
public class UserGame
{
    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(UserGame.class.getName());
    
    public static void main(String[] args)
    {
        // Print some information for environment configuration debugging
        logger.config("Current working directory: " + new File(".").getAbsolutePath());
        logger.config("java.class.path: " + System.getProperty("java.class.path"));
        logger.config("java.library.path: " + System.getProperty("java.library.path"));
        logger.config("java.util.logging.config.file: " + System.getProperty("java.util.logging.config.file"));
        
        // Create and start the client
        Game game = new Game();
        game.startClient();
    }
}
