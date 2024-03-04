package remoteApplication;

import environment.CellStats;
import environment.Coordinate;
import environment.Direction;
import game.Game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class BoardJComponent extends JComponent implements KeyListener {

	private static final long serialVersionUID = 1L;

	private CellStats[][] gameCells = new CellStats[Game.DIMX][Game.DIMY];

	private Image obstacleImage = new ImageIcon("obstacle.png").getImage();
	private Image humanPlayerImage = new ImageIcon("abstract-user-flat.png").getImage();
	private Direction lastPressedDirection = null;
	private final boolean alternativeKeys;

	public BoardJComponent(boolean alternativeKeys) {
		this.alternativeKeys = alternativeKeys;
		setFocusable(true);
		addKeyListener(this);
	}

	public void setGameCells(CellStats[][] gameCells) {
		this.gameCells = gameCells;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		double cellHeight = getHeight() / (double) Game.DIMX;
		double cellWidth = getWidth() / (double) Game.DIMY;

		for (int y = 1; y < Game.DIMY; y++) {
			g.drawLine(0, (int) (y * cellHeight), getWidth(), (int) (y * cellHeight));
		}
		for (int x = 1; x < Game.DIMX; x++) {
			g.drawLine((int) (x * cellWidth), 0, (int) (x * cellWidth), getHeight());
		}
		for (int x = 0; x < Game.DIMX; x++)
			for (int y = 0; y < Game.DIMY; y++) {
				Coordinate p = new Coordinate(x, y);
				CellStats cellstats = gameCells[p.x][p.y];
				if (cellstats != null) {
					// Fill yellow if there is a dead player
					if (cellstats.getStrength() == 0) {
						g.setColor(Color.YELLOW);
						g.fillRect((int) (p.x * cellWidth), (int) (p.y * cellHeight), (int) (cellWidth), (int) (cellHeight));
						g.drawImage(obstacleImage, (int) (p.x * cellWidth), (int) (p.y * cellHeight), (int) (cellWidth), (int) (cellHeight), null);
						// if player is dead, don'd draw anything else?
						continue;
					}
					// Fill green if it is a human player
					if (cellstats.isHumanPlayer()) {
						g.setColor(Color.GREEN);
						g.fillRect((int) (p.x * cellWidth), (int) (p.y * cellHeight), (int) (cellWidth), (int) (cellHeight));
						// Custom icon?
						g.drawImage(humanPlayerImage, (int) (p.x * cellWidth), (int) (p.y * cellHeight), (int) (cellWidth), (int) (cellHeight), null);
					}
					g.setColor(new Color(cellstats.getPlayerID() * 1000));
					((Graphics2D) g).setStroke(new BasicStroke(5));
					Font font = g.getFont().deriveFont((float) cellHeight);
					g.setFont(font);
					String strengthMarking = (cellstats.getStrength() >= 10 ? "X" : "" + cellstats.getStrength());
					g.drawString(strengthMarking, (int) ((p.x + .2) * cellWidth), (int) ((p.y + .9) * cellHeight));
				}

			}
	}

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		if (alternativeKeys) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				lastPressedDirection = environment.Direction.LEFT;
				break;
			case KeyEvent.VK_D:
				lastPressedDirection = environment.Direction.RIGHT;
				break;
			case KeyEvent.VK_W:
				lastPressedDirection = environment.Direction.UP;
				break;
			case KeyEvent.VK_S:
				lastPressedDirection = environment.Direction.DOWN;
				break;
			}
		} else {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				lastPressedDirection = environment.Direction.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				lastPressedDirection = environment.Direction.RIGHT;
				break;
			case KeyEvent.VK_UP:
				lastPressedDirection = environment.Direction.UP;
				break;
			case KeyEvent.VK_DOWN:
				lastPressedDirection = environment.Direction.DOWN;
				break;
			}
		}
		notify();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		clearLastPressedDirection();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Ignored...
	}

	public Direction getLastPressedDirection() {

		return lastPressedDirection;

	}

	public synchronized void clearLastPressedDirection() {
		lastPressedDirection = null;
		notify();
	}

}
