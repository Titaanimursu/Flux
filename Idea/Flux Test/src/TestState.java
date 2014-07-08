import com.infinimango.flux.Display;
import com.infinimango.flux.State;

import java.awt.*;

public class TestState extends State {
	Color color;

	int x = 5;
	int y = 5;
	int xs = 1;
	int ys = 1;
	int si = 32;

	public TestState() {
		color = new Color(0xAAFF00);
	}

	public void update() {
		if (x + si > Display.getWidth() && xs > 0) xs = -1;
		if (x < 0 && xs < 0) xs = 1;

		if (y + si > Display.getHeight() && ys > 0) ys = -1;
		if (y < 0 && ys < 0) ys = 1;

		x += xs;
		y += ys;

		System.out.println("MEM - " + Runtime.getRuntime().totalMemory());
	}

	public void render(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, si, si);
	}
}
