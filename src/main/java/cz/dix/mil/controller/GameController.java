package cz.dix.mil.controller;

import cz.dix.mil.cmd.CmdOptions;
import cz.dix.mil.model.game.Answer;
import cz.dix.mil.model.runtime.GameModel;
import cz.dix.mil.ui.GameView;

/**
 * Main controller of the game that is responsible for passing events to the {@link GameModel}
 * and notifying appropriate UI components.
 * </p>
 * It also controls {@link SoundsController} that is responsible for playing sounds.
 *
 * @author Zdenek Obst, zdenek.obst-at-gmail.com
 * @see SoundsController
 */
public class GameController {

    private final CmdOptions settings;
    private final GameModel model;
    private final SoundsController soundsController;
    private GameView view;

    public GameController(GameModel model, CmdOptions settings) {
        this.model = model;
        this.settings = settings;
        this.soundsController = new SoundsController(model);
    }

    public void setView(GameView view) {
        this.view = view;
    }

    /**
     * Moderator starts the new game.
     */
    public void startGame() {
        soundsController.startGame(new ChainedAction() {
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
        view.showRevealAnswerDialog();
        soundsController.selectAnswer();
    }

    /**
     * Moderator shows correct answer.
     */
    public void revealCorrectAnswer() {
        view.revealAnswer(new ChainedAction() {
            @Override
            public void toNextAction() {
                soundsController.revealAnswer(new ChainedAction() {
                    @Override
                    public void toNextAction() {
                        switch (model.getPlayerProgress()) {
                            case IN_GAME:
                                if (model.hasNextQuestion()) {
                                    model.toNextQuestion();
                                    soundsController.nextQuestion(new ChainedAction() {
                                        @Override
                                        public void toNextAction() {
                                            view.updateMainFrame();
                                        }
                                    });
                                }
                                break;
                            case GAVE_UP:
                            case AFTER_INCORRECT_ANSWER:
                            case WON_GAME:
                            default:
                                view.showFinalRewardDialog();
                                break;
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
        view.disableMainFrame();
        view.showAudienceVotingDialog();
        soundsController.askAudience(new ChainedAction() {
            @Override
            public void toNextAction() {
                view.hideAudienceVotingDialog();
                if (settings.isManualAudience()) {
                    view.showAudienceResultDialog();
                } else {
                    model.generateAudienceResults();
                    view.updateMainFrame();
                }
            }
        });
    }

    /**
     * Sets data for audience hint results.
     *
     * @param counts counts for each answer
     */
    public void setAudienceHintResults(int[] counts) {
        model.setAudienceResults(counts);
        view.updateMainFrame();
    }

    /**
     * Player asks for 50-50 hint.
     */
    public void useFiftyFiftyHint() {
        model.useFiftyFifty();
        soundsController.fiftyFifty(new ChainedAction() {
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
        view.disableMainFrame();
        view.showPhoneFriendDialog();
        soundsController.phoneFriend(new ChainedAction() {
            @Override
            public void toNextAction() {
                view.updateMainFrame();
            }
        });
    }
}
