import com.infinimango.flux.Display;

public class Game extends com.infinimango.flux.Game {
	public static void main(String[] args) {
		Display display = new Display(new Game());
		display.setSize(1366, 768);
		display.setScaling(1);
		display.setFullscreen(true);
		display.setTitle("Game test");
		display.showFPS(true);
		display.create();
	}

	public Game() {

	}
}
