package gui;

import java.util.Observable;
import java.util.Observer;

import game.Game;

import javax.swing.JFrame;

@SuppressWarnings("deprecation")
public class GameGuiMain implements Observer {

	private JFrame frame = new JFrame("PCD.io");
	private BoardJComponent boardGui;
	private Game game;

	public GameGuiMain() {
		super();
		game = new Game();
		game.addObserver(this);
		buildGui();
	}

	private void buildGui() {
		boardGui = new BoardJComponent(game, false);
		frame.add(boardGui);
//		frame.setSize(800, 800);//TODO escolher tamanho fixo.
		frame.setSize(Game.CELL_SIZE*Game.DIMX, Game.CELL_SIZE*Game.DIMY);
		frame.setLocation(0, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void init() throws InterruptedException {
		frame.setVisible(true);
		game.startGame();
	}

	@Override
	public void update(Observable o, Object arg) {
		boardGui.repaint();
	}

	public static void main(String[] args) {
		GameGuiMain game = new GameGuiMain();
		try {
			game.init();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
