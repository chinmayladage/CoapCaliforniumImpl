
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

class CoapClientImpl {
	public static void main(String[] args) {
		new CoapClientImpl();
	}

	CoapClientImpl() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			CoapClient client;
			String uri = "";
			String[] sandboxURIs = new String[3];

			System.out.println("Select CoapServer URI:");
			System.out.println("1. coap://localhost [default]\n2. coap://coap.me\n3. coap://vs0.inf.ethz.ch");
			int urinumber = 0;
			try {
				urinumber = Integer.parseInt(br.readLine());
			} catch (Exception e) {
			}

			sandboxURIs[0] = "coap://localhost";
			sandboxURIs[1] = "coap://coap.me";
			sandboxURIs[2] = "coap://vs0.inf.ethz.ch";

			switch (urinumber) {
			case 1:
				uri = sandboxURIs[0];
				break;
			case 2:
				uri = sandboxURIs[1];
				break;
			case 3:
				uri = sandboxURIs[2];
				break;
			default:
				break;
			}

			String discovery = "/.well-known/core";

			System.out.println("Discovery? y/n");
			String discoverychoice = "";
			try {
				discoverychoice = br.readLine();
			} catch (IOException e1) {
			}
			if (discoverychoice.equals("y") || discoverychoice.equals("Y")) {
				client = new CoapClient(uri + discovery);
			} else {
				client = new CoapClient(uri);
			}

			CoapResponse response = client.get();

			if (response != null) {
				System.out.println(response.getCode());
				System.out.println(response.getOptions());
				System.out.println(response.getResponseText());
			} else {
				System.out.println("Request failed");
			}

			System.out.println("Enter resource to observe(if available):");
			String obsresource = "";
			try {
				obsresource = br.readLine();
			} catch (IOException e1) {
			}
			if (obsresource.length() > 1) {
				client = new CoapClient(uri + "/" + obsresource);
				new ObserverThread(client).start();

			}

			try {
				br.readLine();
			} catch (IOException e) {
			}
		}
	}

	class ObserverThread extends Thread {
		CoapClient client;

		public ObserverThread(CoapClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			System.out.println("Observer started");
			CoapHandler handler = new CoapHandler() {
				public void onLoad(CoapResponse response) {
					System.out.println("Update:" + response.getResponseText());
				}

				public void onError() {
					System.err.println("Error in observe");
				}
			};
			client.observe(handler);
			while (true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}