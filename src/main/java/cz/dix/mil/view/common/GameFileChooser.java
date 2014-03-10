package cz.dix.mil.view.common;

import cz.dix.mil.model.game.Game;
import cz.dix.mil.model.game.GameCreationException;
import cz.dix.mil.model.game.GameFactory;
import cz.dix.mil.model.game.validation.GameValidation;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * File chooser that is used for importing and exporting game files.
 *
 * @author Zdenek Obst, zdenek.obst-at-gmail.com
 */
public class GameFileChooser extends JFileChooser {

    private static final File DEFAULT_DIR = new File("../games");
    private final GameValidation gameValidation;

    public GameFileChooser(GameValidation gameValidation) {
        super(DEFAULT_DIR);
        this.gameValidation = gameValidation;
        setDialogTitle("Game File Selection");
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setMultiSelectionEnabled(false);
        setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
    }

    /**
     * Imports the game from XML file provided by user.
     *
     * @return game instance or null if load failed (or closed)
     */
    public Game importGame() {
        setApproveButtonText("Import");
        if (showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Game importedGame = GameFactory.newGame(getSelectedFile(), gameValidation);
                JOptionPane.showMessageDialog(this, "Game imported successfully!", "",
                        JOptionPane.PLAIN_MESSAGE, new ImageIcon(getClass().getResource("/imgs/approved.png")));
                return importedGame;
            } catch (GameCreationException e) {
                JOptionPane.showMessageDialog(this, "Game import failed!\n" + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }

    /**
     * Exports the game from given game instance to file provided by user.
     *
     * @param game game to be saved
     */
    public void exportGame(Game game) {
        setApproveButtonText("Export");
        if (showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = getSelectedFile();
                if (!selectedFile.getAbsolutePath().endsWith(".xml")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".xml");
                }
                GameFactory.exportToXml(game, gameValidation, selectedFile);
                JOptionPane.showMessageDialog(this, "Game exported successfully!", "",
                        JOptionPane.PLAIN_MESSAGE, new ImageIcon(getClass().getResource("/imgs/approved.png")));
            } catch (GameCreationException e) {
                JOptionPane.showMessageDialog(this, "Game export failed!\n" + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
