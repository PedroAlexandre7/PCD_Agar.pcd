package game;

import environment.Coordinate;
import environment.Direction;

public class BotPlayer extends Player {

	public BotPlayer(Game game, byte strength) {
		super(game, strength);
	}

	public BotPlayer(Game game) {
		super(game);
	}

	@Override
	public boolean isHumanPlayer() {
		return false;
	}

	@Override
	public Coordinate getNextMove() {
		Direction dir = Direction.randomDirection();
		return getCurrentCell().getPosition().translate(dir.getVector());
	}

}