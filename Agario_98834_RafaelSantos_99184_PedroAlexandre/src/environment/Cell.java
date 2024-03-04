package environment;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import game.Game;
import game.Player;

public class Cell {

	private final Coordinate position;
	private ReentrantLock lock = new ReentrantLock();
	private Condition isFree = lock.newCondition();
	private Player player = null;

	public Cell(Coordinate position, Game g) {
		this.position = position;
//		this.game = g;
	}

	public boolean tryLock() throws InterruptedException {
		return lock.tryLock(Game.DEADLOCK_WAITING_TIME, TimeUnit.MILLISECONDS);
	}

	public void unlock() {
		if (lock.isLocked() && lock.isHeldByCurrentThread())
			lock.unlock();
	}

// --------------------- ALTERNATIVA: ---------------------------
//	public void unlock() {
//		try {
//			if (lock.isLocked())
//				lock.unlock();
//		} catch (IllegalMonitorStateException e) {
//			System.out.println("Tentei dar unlock a um cadeado que já não me pertence, mas não faz mal!");
//		}
//	}

	public Coordinate getPosition() {
		return position;
	}

	public boolean isOccupied() {
		return player != null;
	}

	public Player getPlayer() {
		return player;
	}

	public void spawnPlayer(Player player) throws InterruptedException {
		// lock.lockInterruptibly() talvez seja útil para verificar se o jogo acabou no
		// futuro se o game interromper os jogadores
		try {
			lock.lock();
			while (isOccupied()) {
//				System.err.println(player + " is trying to spawn in " + this + ", but it's currently occupied!");
				isFree.await(Game.MAX_WAITING_TIME_FOR_MOVE, TimeUnit.MILLISECONDS);
				if (isOccupied() && !this.player.isActive())
					return;
			}
			setPlayer(player);
		} finally {
			lock.unlock();
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (player != null)
			player.setCoordinate(position);
		else
			isFree.signal();
	}

	public CellStats convert() {
		Player p = player;
		if (p == null)
			return null;
		return new CellStats(p.getCurrentStrength(), p.isHumanPlayer(), p.getIdentification());
	}

	@Override
	public String toString() {
		return "Cell " + position.toString();
	}

}
