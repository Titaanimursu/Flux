package com.infinimango.flux;

import java.awt.*;

public abstract class State {

	public State(){}

	public abstract void update(Display d);
	public abstract void render(Display d, Graphics g);
}
