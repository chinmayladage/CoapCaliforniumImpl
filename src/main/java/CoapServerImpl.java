
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoapServerImpl extends CoapServer {

	public static void main(String[] args) {
		try {
			CoapServerImpl server = new CoapServerImpl();
			server.start();

		} catch (SocketException e) {
			System.err.println("Failed to start server: " + e.getMessage());
		}
	}

	public CoapServerImpl() throws SocketException {
		add(new TimerResource());
		add(new CoapResource("hello-world") {
			public void handleGET(CoapExchange exchange) {
				exchange.respond(ResponseCode.CONTENT, "hello world");
			}
		});
	}

	class TimerResource extends CoapResource {
		int timer = 0;

		public TimerResource() {
			super("10SecTimer");
			this.setObservable(true);
			this.setObserveType(Type.CON);

			new Thread() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(10000);
							timer++;
							changed();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}

		@Override
		public void handleGET(CoapExchange exchange) {
			exchange.respond(ResponseCode.CONTENT, "timer count: " + timer);
		}
	}
}
