package com.infinimango.flux;

import com.infinimango.flux.State;

import java.awt.*;

public abstract class Game2 implements Runnable {
	private State state;

	private static long time;

	public void updateClock(){
		Game2.time = System.currentTimeMillis();
	}

	public static long getTime(){
		return time;
	}

	public void update(Display d){
		state.update(d);
	}

	public void render(Display d, Graphics g){
		state.render(d, g);
	}
}
