package net.jtreemer.labolink;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;

public class BBConnectionListener implements ConnectionListener {

	public void connectionDiscarded(Connection arg0) {
		System.out.println("Lost connection from: "+arg0.getRemoteAddress().toString());
	}

	public void connectionReceived(Connection arg0) {
		System.out.println("New connection from: "+arg0.getRemoteAddress().toString());
	}

}
