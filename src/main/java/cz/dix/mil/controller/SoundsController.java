package cz.dix.mil.controller;

import cz.dix.mil.model.runtime.GameModel;
import cz.dix.mil.sound.Sound;
import cz.dix.mil.sound.SoundsFactory;
import cz.dix.mil.sound.jmf.JmsSoundsFactory;

/**
 * Controller that is responsible for playing appropriate sounds for game events.
 *
 * @author Zdenek Obst, zdenek.obst-at-gmail.com
 */
public class SoundsController {

    private final GameModel model;
    private final SoundsFactory soundsFactory = new JmsSoundsFactory();
    private Sound actualSound;
    private boolean isAnswerRevealed = false;

    public SoundsController(GameModel model) {
        this.model = model;
    }

    /**
     * Plays a starting sound (blocked), starts sound for easy questions and passes processing to chained action.
     *
     * @param chainedAction action to be fired after start sound is played
     */
    public void startGame(final ChainedAction chainedAction) {
        soundsFactory.start().play(new ChainedAction() {
            @Override
            public void toNextAction() {
                playLoopedAndStoreSound(soundsFactory.easyQuestion());
                chainedAction.toNextAction();
            }
        });
    }

    /**
     * In case of harder than easy questions, plays awaiting sound (until moderator reveals answers).
     */
    public void selectAnswer() {
        switch (model.getActualQuestionDifficulty()) {
            case EASY:
                // do nothing
                break;
            case MID:
            case HARD:
                stopActualSound();
                isAnswerRevealed = false;
                playAndStoreSoundChained(soundsFactory.answerWaitStart(), new ChainedAction() {
                    @Override
                    public void toNextAction() {
                        stopActualSound();
                        if (!isAnswerRevealed) { // avoid situation when answerStart did not finish and answer was revealed
                            playLoopedAndStoreSound(soundsFactory.answerWaitContinue());
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * Plays appropriate sounds (correct/incorrect answer or checkpoint)
     * and passes processing to chained action.
     *
     * @param chainedAction action to be fired after sounds is played
     */
    public void revealAnswer(final ChainedAction chainedAction) {
        isAnswerRevealed = true;
        switch (model.getActualQuestionDifficulty()) {
            case EASY:
                if (isAnswerCorrect()) {
                    if (isOrdinaryQuestion(chainedAction)) {
                        pauseActualSound();
                        soundsFactory.answerEasyCorrect().play(chainedAction);
                        continueActualSound();
                    }
                } else {
                    stopActualSound();
                    soundsFactory.answerIncorrect().play(chainedAction);
                }
                break;
            case MID:
            case HARD:
                if (isAnswerCorrect()) {
                    if (isOrdinaryQuestion(chainedAction)) {
                        stopActualSound();
                        soundsFactory.answerWaitCorrect().play(chainedAction);
                    }
                } else {
                    stopActualSound();
                    soundsFactory.answerIncorrect().play(chainedAction);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Plays a sound for next question and passes processing to chained action.
     *
     * @param chainedAction action to be fired after sounds is played
     */
    public void nextQuestion(final ChainedAction chainedAction) {
        switch (model.getActualQuestionDifficulty()) {
            case EASY:
                chainedAction.toNextAction();
                break;
            case MID:
                stopActualSound();
                soundsFactory.question().play(new ChainedAction() {
                    @Override
                    public void toNextAction() {
                        playLoopedAndStoreSound(soundsFactory.midQuestion());
                        chainedAction.toNextAction();
                    }
                });
                break;
            case HARD:
                stopActualSound();
                soundsFactory.question().play(new ChainedAction() {
                    @Override
                    public void toNextAction() {
                        playLoopedAndStoreSound(soundsFactory.hardQuestion());
                        chainedAction.toNextAction();
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * Plays a sound for audience hint and passes processing to chained action.
     *
     * @param chainedAction action to be fired after sounds is played
     */
    public void askAudience(final ChainedAction chainedAction) {
        pauseActualSound();
        soundsFactory.askAudience().play(new ChainedAction() {
            @Override
            public void toNextAction() {
                useHint(chainedAction, soundsFactory.askAudienceEnd());
            }
        });
    }

    /**
     * Plays a sound for 50-50 hint and passes processing to chained action.
     *
     * @param chainedAction action to be fired after sounds is played
     */
    public void fiftyFifty(final ChainedAction chainedAction) {
        useHint(chainedAction, soundsFactory.fiftyFifty());
    }

    /**
     * Plays a sound for phone friend hint and passes processing to chained action.
     *
     * @param chainedAction action to be fired after sounds is played
     */
    public void phoneFriend(final ChainedAction chainedAction) {
        useHint(chainedAction, soundsFactory.phoneFriend());
    }

    /**
     * Checks whether the question is ordinary (is not checkpoint) and plays checkpoint sound if not.
     *
     * @return true if actual question is ordinary, otherwise false
     */
    private boolean isOrdinaryQuestion(final ChainedAction chainedAction) {
        if (model.isCheckpoint(model.getActualQuestion())) {
            stopActualSound();
            soundsFactory.checkpoint().play(chainedAction);
            return false;
        } else {
            return true;
        }
    }

    private void useHint(final ChainedAction chainedAction, Sound hintSound) {
        pauseActualSound();
        hintSound.play(new ChainedAction() {
            @Override
            public void toNextAction() {
                continueActualSound();
                chainedAction.toNextAction();
            }
        });
    }

    private void pauseActualSound() {
        if (actualSound != null) {
            actualSound.pausePlaying();
        }
    }

    private void stopActualSound() {
        if (actualSound != null) {
            actualSound.stop();
        }
    }

    private void continueActualSound() {
        if (actualSound != null) {
            actualSound.continuePlaying();
        }
    }

    private void playLoopedAndStoreSound(Sound sound) {
        actualSound = sound;
        actualSound.playLooped();
    }

    private void playAndStoreSoundChained(Sound sound, ChainedAction chainedAction) {
        actualSound = sound;
        actualSound.play(chainedAction);
    }


    private boolean isAnswerCorrect() {
        return model.getSelectedAnswer().isCorrect();
    }
}
