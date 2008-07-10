
package bingo.game;

import bingo.shared.*;

interface BagOfBalls {
    BingoBall getNext() throws NoMoreBallsException;
}
