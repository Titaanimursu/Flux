package com.infinimango.flux;

import com.infinimango.flux.input.Keyboard;
import com.infinimango.flux.input.Mouse;
import com.infinimango.flux.state.State;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class Game extends Canvas implements Runnable {
	// Window, its dimensions and pixel scaling amount
	public static JFrame screen;
	public static int width, height;
	private static int scale;
	private static String title;

	// Runtime and timing variables
	private static boolean running = false;
	private static boolean showFPS = false;
	private static long targetUPS = 60;
	private static long time;
	public static boolean focused;
	private boolean sync = false;
	private static boolean shouldRender;
	private static boolean pauseWhenUnfocused = true;

	// Rendering buffers
	private static BufferStrategy bufferStrategy;
	private static BufferedImage scaleBuffer;

	// Current game state
	private static State state;

	// Flux engine version number
	public static final double FLUX_VERSION = 2.9;

	/**
	 * Creates a new game into a JFrame and displays it. Game must be started using start() and a new state must be created.
	 *
	 * @param width  Width of the game display
	 * @param height Height of the game display
	 * @param scale  Pixel scaling amount
	 * @param title  Title message of the frame
	 */
	public Game(int width, int height, int scale, String title) {
		Debug.print("========== FLUX v." + FLUX_VERSION + " RUNNING ==========");
		Debug.out("Creating display...");
		long timeCounter = System.currentTimeMillis();

		scaleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Game.scale = scale;
		Game.width = width;
		Game.height = height;
		Game.title = title;

		screen = new JFrame(title);

		screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		screen.setResizable(false);

		Dimension size = new Dimension(width * scale, height * scale);
		setSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);

		addKeyListener(new Keyboard());
		addMouseListener(new Mouse(scale));
		addMouseMotionListener(new Mouse(scale));
		addMouseWheelListener(new Mouse(scale));

		screen.add(this);
		screen.pack();

		screen.setLocationRelativeTo(null);

		screen.setVisible(true);

		requestFocus();
		focused = hasFocus();

		time = System.currentTimeMillis();
		Debug.out("Display created in " + (System.currentTimeMillis() - timeCounter) + "ms!");
	}

	/**
	 * Starts a new game thread
	 */
	public synchronized void start() {
		Debug.out("Starting thread");
		running = true;
		new Thread(this, screen.getTitle()).start();
	}

	/**
	 * Stops the game from running
	 */
	public synchronized void stop() {
		running = false;
	}

	/**
	 * Main method containing the game loop, fps and ups measuring and delta calculating.
	 */
	public void run() {
		long last = System.nanoTime();
		long now;

		int frames = 0;
		int updates = 0;

		long timer = System.currentTimeMillis();

		double delta = 0;
		double target = 1000.0 * 1000.0 * 1000.0 / targetUPS;

		while (running) {
			time = System.currentTimeMillis();
			focused = hasFocus();

			now = System.nanoTime();
			delta += (now - last) / target;

			last = now;

			shouldRender = !sync;

			if (delta >= 1) {
				if (state != null) state.update();
				updates++;
				shouldRender = true;
				delta--;
			}

			if (shouldRender) {
				render();
				frames++;
			}

			if (System.currentTimeMillis() - timer >= 1000) {
				timer = System.currentTimeMillis();

				String msg = updates + " ups - " + frames + " fps";

				Debug.print(msg);
				if (showFPS) screen.setTitle(title + " - " + msg);

				updates = 0;
				frames = 0;
			}
		}
	}

	/**
	 * Renders the game graphics and scales them accordingly.
	 */
	private void render() {
		bufferStrategy = getBufferStrategy();
		if (bufferStrategy == null) {
			createBufferStrategy(3);
			return;
		}

		if (scale != 1) {
			Graphics rawGraphics = bufferStrategy.getDrawGraphics();
			Graphics g = scaleBuffer.createGraphics();

			{
				g.setColor(Color.black);
				g.fillRect(0, 0, width, height);

				if (state != null) state.render(g);
			}

			rawGraphics.drawImage(scaleBuffer, 0, 0, width * scale, height * scale, null);
			bufferStrategy.show();
		} else {
			Graphics g = bufferStrategy.getDrawGraphics();

			{
				g.setColor(Color.black);
				g.fillRect(0, 0, width, height);

				if (state != null) state.render(g);
			}

			bufferStrategy.show();
		}
	}


	public void setTargetUPS(long targetUPS) {
		this.targetUPS = targetUPS;
	}


	public static void setState(State state) {
		Game.state = state;
	}


	public static void showFPS(boolean showFPS) {
		Game.showFPS = showFPS;
	}


	public static long getTime() {
		return time;
	}



	public static boolean isFocused() {
		return focused;
	}


	public void setSyncing(boolean sync) {
		this.sync = sync;
	}


	public static void setPauseWhenUnfocused(boolean pauseWhenUnfocused) {
		Game.pauseWhenUnfocused = pauseWhenUnfocused;
	}

	public static void setIcon(BufferedImage icon) {
		screen.setIconImage(icon);
	}

	public static void setCursor(BufferedImage cursor) {
		Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(0, 0), "Flux - Custom cursor");
		screen.setCursor(c);
	}
}
