package cz.dix.mil.controller;

import cz.dix.mil.model.game.Answer;
import cz.dix.mil.model.state.GameModel;
import cz.dix.mil.model.state.PlayersProgress;
import cz.dix.mil.sound.SoundsPlayer;
import cz.dix.mil.ui.GameView;

/**
 * Main controller of the game that is responsible for passing events to the {@link GameModel}
 * and notifying appropriate UI components (GUI or sound player).
 *
 * @author Zdenek Obst, zdenek.obst-at-gmail.com
 */
public class GameController {

    private GameModel model;
    private GameView view;
    private SoundsPlayer soundsPlayer;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void setView(GameView view) {
        this.view = view;
    }

    public void setSoundsPlayer(SoundsPlayer soundsPlayer) {
        this.soundsPlayer = soundsPlayer;
    }

    /**
     * Moderator starts the new game.
     */
    public void startGame() {
        soundsPlayer.startGame(new ChainedAction() {
            @Override
            public void toNextAction() {
                model.toNextQuestion();
                view.updateMainFrame();
                view.showMainFrame();
            }
        });
    }

    /**
     * Player answers the question.
     *
     * @param answer answer selected by player
     */
    public void answerQuestion(Answer answer) {
        view.disableMainFrame();
        model.answerQuestion(answer);
        view.showRevealAnswerFrame();
        soundsPlayer.selectAnswer();
    }

    /**
     * Moderator shows correct answer.
     */
    public void revealCorrectAnswer() {
        view.revealAnswer(new ChainedAction() {
            @Override
            public void toNextAction() {
                soundsPlayer.revealAnswer(new ChainedAction() {
                    @Override
                    public void toNextAction() {
                        if (PlayersProgress.IN_GAME.equals(model.getState())) {
                            if (model.hasNextQuestion()) {
                                model.toNextQuestion();
                                soundsPlayer.nextQuestion(new ChainedAction() {
                                    @Override
                                    public void toNextAction() {
                                        view.updateMainFrame();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Player asks for audience hint.
     */
    public void useAudienceHint() {
        soundsPlayer.askAudience(new ChainedAction() {
            @Override
            public void toNextAction() {
                // show some frame and pass results from it to model
                model.useAudience(25, 22, 10, 44);

                // will be covered by updateMainFrame by adding some panel in the middle
                System.out.println(model.getAudienceResult().getPercentsForA() + "% " +
                        model.getAudienceResult().getPercentsForB() + "% " +
                        model.getAudienceResult().getPercentsForC() + "% " +
                        model.getAudienceResult().getPercentsForD() + "%");

                view.updateMainFrame();
            }
        });
    }

    /**
     * Player asks for 50-50 hint.
     */
    public void useFiftyFiftyHint() {
        model.useFiftyFifty();
        soundsPlayer.fiftyFifty(new ChainedAction() {
            @Override
            public void toNextAction() {
                view.updateMainFrame();
            }
        });
    }

    /**
     * Player asks for phone friend hint.
     */
    public void usePhoneFriendHint() {
        model.usePhoneFriend();
        soundsPlayer.phoneFriend(new ChainedAction() {
            @Override
            public void toNextAction() {
                view.updateMainFrame();
            }
        });
    }
}
