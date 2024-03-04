package game;

import java.util.concurrent.ThreadLocalRandom;

import environment.Cell;
import environment.Coordinate;

/**
 * Represents a player.
 * 
 * @author luismota
 *
 */
public abstract class Player extends Thread {

	protected Game game;
	private int id;
	private Coordinate coord; // usada para saber a célula e para inicialmente verificar se o jogado
	private byte currentStrength;
	protected byte originalStrength;

	private static int nextId = 1;

	private static int getNextId() {
		return nextId++;
	}

	public Player(Game game, byte strength) { // TODO <- Não usado diretamente
		super();
		this.id = getNextId();
		this.game = game;
		currentStrength = strength;
		originalStrength = strength;
	}

	public Player(Game game) {
		this(game, (byte) ThreadLocalRandom.current().nextInt(1, Game.MAX_INITIAL_STRENGTH + 1));
	}

	public void setCoordinate(Coordinate coord) {
		this.coord = coord;
	}

	public boolean isActive() {
		return currentStrength != 0 && currentStrength != Game.MAX_STRENGTH;
	}

	public Cell getCurrentCell() {
		if (coord == null)
			return null;
		return game.getCell(coord);
	}

	public byte getCurrentStrength() {
		return currentStrength;
	}

	public int getIdentification() {
		return id;
	}

	public abstract boolean isHumanPlayer();

	public abstract Coordinate getNextMove();

	public int compareStrength(Player other) {
		return getCurrentStrength() > other.getCurrentStrength() ? 1 : getCurrentStrength() == other.getCurrentStrength() ? 0 : -1;
	}

	public void addStrength(int add) {
		currentStrength = (byte) Math.min(Game.MAX_STRENGTH, getCurrentStrength() + add);
		if (currentStrength == Game.MAX_STRENGTH)
			game.countDown();

//		System.err.println(" + " + add + " = " + currentStrength);
	}

	@Override
	public void run() {
		spawn();
		game.waitForStart();
		while (isActive() && !game.isOver()) {
			Coordinate newCoord = getNextMove();
			if (newCoord != null)
				tryToMove(newCoord);
		}
	}

	protected void spawn() {
		if (!isActive())
			throw new IllegalStateException("Player shouldn't be dead."); // <----TODO remover depois
		try {
			while (coord == null)
				game.getRandomCell().spawnPlayer(this);
		} catch (InterruptedException e) {// para quando o jogo acabar ele não tentar continuar vivo
//			System.out.println(this + " o jogo acabou e nem consegui spawnar");
			return;
		}
	}

	protected void tryToMove(Coordinate newCoord) {
		if (Game.isWithinBounds(newCoord)) {
			Cell currentCell = getCurrentCell();
			Cell newCell = game.getCell(newCoord);
			try {
				if (currentCell.tryLock() && newCell.tryLock()) {
					if (newCell.isOccupied()) {
						Player other = newCell.getPlayer();
						if (other.isActive()) {
							fight(other);
						} else {
							newCell.unlock();
							currentCell.unlock();
							if (!isHumanPlayer())
								knockout();
						}
					} else {
						newCell.setPlayer(this);
						currentCell.setPlayer(null);
					}
				}
			} catch (InterruptedException e) {
				return;
			} finally {
				newCell.unlock();
				currentCell.unlock();
			}
		}

		try {
			game.notifyChange();
			Thread.sleep(Game.REFRESH_INTERVAL * originalStrength); // <- Perguntar ao stor!
		} catch (InterruptedException e) {
		}
	}

	private void fight(Player other) {
		int compare = compareStrength(other);
		if (compare == 1 || (compare == 0 && ThreadLocalRandom.current().nextBoolean())) {
			addStrength(other.getCurrentStrength());
			other.die();
		} else {
			other.addStrength(currentStrength);
			die();
		}
	}

	protected synchronized void knockout() {
		try {
			new AlarmThread().start();
			wait();
		} catch (InterruptedException e) {
		}
	}

	public void die() {
		currentStrength = 0;
		interrupt();
	}

	private class AlarmThread extends Thread { // <- Perguntar ao stor se é preciso criar esta classe.
		@Override
		public void run() {
			try {
				Thread.sleep(Game.MAX_WAITING_TIME_FOR_MOVE);
				Player.this.interrupt();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public String toString() {
		return "Player [id=" + id + ", strength=" + currentStrength + ", cell=" + getCurrentCell() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}
}