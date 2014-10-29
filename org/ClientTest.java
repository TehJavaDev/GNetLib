package org.gnet;

import org.gnet.client.*;
import org.gnet.packet.Packet;

public class ClientTest {

	/**
	 * Main entry point into the application.
	 * 
	 * @param args
	 *            Arguments passed to the application.
	 */
	public static void main(final String[] args) {

		// Host to connect to:
		final String host = "127.0.0.1";

		// Port # to connect to the host on:
		final int port = 43594;

		// Setup our client.
		final GNetClient networkedClient = new GNetClient(host, port);

		// Add our event listener to manage events.
		networkedClient.addEventListener(new ClientEventListener() {

			@Override
			protected void clientConnected(final ServerModel server) {
				// We've connected to the server, send a test packet.
				final Packet packet = new Packet("TestPacket", 1);
				packet.addEntry("name", "TehJavaDev");
				server.sendPacket(packet);

			}

			@Override
			protected void clientDisconnected(final ServerModel server) {
			}

			@Override
			protected void packetReceived(final ServerModel server, final Packet packet) {
			}

			@Override
			protected void debugMessage(final String msg) {
			}

			@Override
			protected void errorMessage(final String msg) {
			}
		});

		// Attempt to bind the client.
		networkedClient.bind();

		// Once binded, finally start our client.
		networkedClient.start();
	}
}