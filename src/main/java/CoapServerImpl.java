
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
		add(new CustomTimerResource());
		add(new CoapResource("hello-world") {
			public void handleGET(CoapExchange exchange) {
				exchange.respond(ResponseCode.CONTENT, "hello world");
			}
		});
	}

	class TimerResource extends CoapResource {
		int timerCount = 0;

		public TimerResource(String s) {
			super(s);
		}

		public TimerResource() {
			super("10SecTimer");
			this.setObservable(true);
			this.setObserveType(Type.CON);
			getAttributes().setObservable();
			getAttributes().setTitle("Observable timer updates every 10 seconds");

			new Thread() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(10000);
							timerCount++;
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
			exchange.respond(ResponseCode.CONTENT, "timer count: " + timerCount);
		}
	}

	class CustomTimerResource extends TimerResource {

		public CustomTimerResource() {
			super("CustomTimer");
			this.setObservable(true);
			this.setObserveType(Type.CON);
			getAttributes().setObservable();
			getAttributes().setTitle("Observable custom timer taking seconds in POST");
			getAttributes().setAttribute("", "");
		}

		class CusTimer extends Thread {
			int timerValue = 10000;

			CusTimer(int timerValue) {
				this.timerValue = timerValue;
			}

			public void run() {
				while (true) {
					try {
						Thread.sleep(timerValue);
						timerCount++;
						changed();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void handlePOST(CoapExchange exchange) {
			String payload = new String(exchange.getRequestPayload());
			int newTimerValue = -1;
			try {
				newTimerValue = Integer.parseInt(payload);
				timerCount = 0;
				CusTimer ct = new CusTimer(newTimerValue * 1000);
				ct.start();
				exchange.respond(ResponseCode.CONTENT,
						"timer count: " + timerCount + "; Timer value changed to " + newTimerValue + "seconds");
			} catch (Exception e) {
				exchange.respond(ResponseCode.CONTENT,
						"timer count: " + timerCount + "; Timer value expected to be int, but found " + payload);
			}
		}
	}
}
