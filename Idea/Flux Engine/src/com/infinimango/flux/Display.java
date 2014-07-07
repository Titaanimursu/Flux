package com.infinimango.flux;

import com.infinimango.flux.input.Keyboard;
import com.infinimango.flux.input.Mouse;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class Display implements Runnable {
	private static final double FLUX_VERSION = 3.0;
	private static boolean MOTDShown = false;

	public static final int UNLIMITED = -1;

	private static JFrame frame;
	private static Canvas canvas;
	private Game game;

	private BufferedImage scaleBuffer;

	private static int width;
	private static int height;
	private static int scale;
	private boolean fullscreen;

	private String title;

	private int targetUPS;
	private int targetFPS;
	private boolean limitUPS = true;
	private boolean limitFPS = false;

	private static boolean running;

	private boolean showFPS;

	public Display(Game game) {
		this.game = game;
		if(!MOTDShown) {
			MOTDShown = true;
			Debug.print("========== FLUX v." + FLUX_VERSION + " RUNNING ==========");
		}
	}

	public void setSize(int width, int height){
		Display.width = width;
		Display.height = height;
	}

	public void setFullscreen(boolean fullscreen){
		this.fullscreen = fullscreen;
	}

	public void setScaling(int scale){
		Display.scale = scale;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public void setTargetUPS(int targetUPS){
		if(targetUPS == UNLIMITED) {
			limitUPS = false;
			return;
		}
		this.targetUPS = targetUPS;
	}

	public void setTargetFPS(int targetFPS){
		if(targetFPS == UNLIMITED) {
			limitFPS = false;
			return;
		}
		this.targetFPS = targetFPS;
	}

	public void showFPS(boolean showFPS){
		this.showFPS = showFPS;
	}

	public void create(){
		long timer = System.currentTimeMillis();
		Debug.out("Creating display...");
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setResizable(false);

		Dimension size = new Dimension(width * scale, height * scale);
		canvas = new Canvas();
		canvas.setMinimumSize(size);
		canvas.setMaximumSize(size);
		canvas.setPreferredSize(size);

		if(fullscreen){
			frame.setUndecorated(true);
			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			gd.setFullScreenWindow(frame);
		}

		canvas.addKeyListener(new Keyboard());
		canvas.addMouseListener(new Mouse(scale));
		canvas.addMouseMotionListener(new Mouse(scale));
		canvas.addMouseWheelListener(new Mouse(scale));

		frame.add(canvas);
		frame.pack();

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		canvas.requestFocus();
		Debug.out("Display created in " + (System.currentTimeMillis() - timer) + "ms!");

		start();
	}

	private synchronized void start() {
		Debug.out("Starting thread");
		Display.running = true;
		new Thread(this, frame.getTitle()).start();
	}

	private synchronized void stop() {
		Debug.out("Stopping thread");
		Display.running = false;
	}

	public void run() {
		long last = System.nanoTime();
		long now;

		int frames = 0;
		int updates = 0;

		long timer = System.currentTimeMillis();

		double deltaUPS = 0;
		double deltaFPS = 0;

		while (running) {
			game.updateClock();

			now = System.nanoTime();

			if(limitUPS) deltaUPS += (now - last) / (1000.0 * 1000.0 * 1000.0 / targetUPS);
			if(limitFPS) deltaFPS += (now - last) / (1000.0 * 1000.0 * 1000.0 / targetFPS);

			last = now;

			if (!limitUPS || deltaUPS >= 1) {
				game.update();
				updates++;
				deltaUPS--;
			}

			if (!limitFPS || deltaFPS >= 1) {
				render();
				frames++;
				deltaFPS--;
			}

			if (System.currentTimeMillis() - timer >= 1000) {
				timer = System.currentTimeMillis();

				String msg = updates + " ups - " + frames + " fps";
				Debug.print(msg);

				if (showFPS) frame.setTitle(title + " - " + msg);

				updates = 0;
				frames = 0;
			}
		}
		stop();
	}

	protected void render() {
		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		if (bufferStrategy == null) {
			canvas.createBufferStrategy(3);
			return;
		}
		if (scaleBuffer == null){
			scaleBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}

		if (scale > 1) {
			Graphics rawGraphics = bufferStrategy.getDrawGraphics();

			{
				Graphics2D g = scaleBuffer.createGraphics();

				g.setColor(Color.BLACK);
				g.fillRect(0, 0, width, height);

				game.render(g);
			}

			rawGraphics.drawImage(scaleBuffer, 0, 0, width * scale, height * scale, null);
			bufferStrategy.show();
		} else {
			Graphics g = bufferStrategy.getDrawGraphics();

			g.setColor(Color.black);
			g.fillRect(0, 0, width, height);

			game.render(g);

			bufferStrategy.show();
		}
	}

	public static int getWidth() {
		return width;
	}

	public static int getScaledWidth() {
		return width * scale;
	}

	public static int getHeight() {
		return height;
	}

	public static int getScaledHeight() {
		return height * scale;
	}

	public static int getScale() {
		return scale;
	}

	public static boolean hasFocus() {
		return canvas.hasFocus();
	}

	public static void setIcon(BufferedImage icon) {
		frame.setIconImage(icon);
	}

	public static void setCursor(BufferedImage cursorImage) {
		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "Flux - Custom cursor");
		frame.setCursor(cursor);
	}

	public static boolean isRunning(){
		return running;
	}
}
