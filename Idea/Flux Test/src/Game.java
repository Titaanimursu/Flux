import com.infinimango.flux.Display;
import com.infinimango.flux.net.GameClient;
import com.infinimango.flux.net.GameServer;

public class Game extends com.infinimango.flux.Game {
	public static void main(String[] args) {
		Display display = new Display(new Game());
		display.setSize(320, 280);
		display.setScaling(1);
		display.setTargetUPS(60);
		display.setTargetFPS(60);

		display.setAutoSleep(true);

		display.setTitle("Game test");
		display.showFPS(true);
		display.create();

//		boolean serv = JOptionPane.showConfirmDialog(null, "Flux engine debug", "Start server?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

//		if (serv) {
//			GameServer server = new GameServer() {
//				@Override
//				public void update() {
//
//				}
//
//				@Override
//				protected void parse(byte[] bytes) {
//					String msg = new String(bytes);
//
//				}
//			};
//		}

		GameClient client = new GameClient() {
			@Override
			public void update() {

			}

			@Override
			protected void parse(byte[] bytes) {

			}
		};

		client.connectTo("127.0.0.1", GameServer.DEFAULT_PORT);
		client.ping();

		Game.setState(new TestState());
	}
}
