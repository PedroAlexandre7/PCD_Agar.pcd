package game;

import environment.Coordinate;
import environment.Direction;

/**
 * Class to demonstrate a player being added to the game.
 * 
 * @author luismota
 *
 */
public class HumanPlayer extends Player {

	private Direction currentDirection = Direction.UP;

	public HumanPlayer(Game game, byte strength) {
		super(game, strength);
	}

	public boolean isHumanPlayer() {
		return true;
	}

	public void setCurrentDirection(Direction dir) {
		currentDirection = dir;
	}

	@Override
	public Coordinate getNextMove() { // TODO Fase 6
		if (currentDirection != null) {
			Coordinate newDirection = currentDirection.getVector();
			currentDirection = null;
			return getCurrentCell().getPosition().translate(newDirection);
		}
		return null;
	}
}
