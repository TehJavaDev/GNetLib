package org.gnet.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ShutdownChannelGroupException;

import org.gnet.packet.Packet;

public class ClientModel implements Runnable {

	private final GNetServer server;
	private final Socket clientSocket;
	int uuid;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private boolean connected;

	public ClientModel(final GNetServer server, final Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		try {
			// Init client object output stream.
			oos = new ObjectOutputStream(clientSocket.getOutputStream());

			// Run initial oos flush. (Prevents hanging)
			oos.flush();

			// Init client object input stream.
			ois = new ObjectInputStream(clientSocket.getInputStream());
			connected = true;
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {
		while (connected) {
			// Try to fetch incoming data.
			Object incoming = null;
			try {
				if ((incoming = ois.readObject()) != null) {
					if (incoming instanceof Packet) {
						final Packet p = (Packet) incoming;
						if (p.getPacketName().equals("ShuttingDown")) {
							server.recievedPackets++;
							server.debug("Incoming shutown packet from ["
									+ uuid + "]: " + p.getPacketName());
							cleanUp();
							continue;
						} else {
							server.recievedPackets++;
							server.serverEventListener.packetReceived(this, p);
							server.debug("Incoming packet from [" + uuid
									+ "]: " + p.getPacketName());
							continue;
						}

					} else {
						server.debug("Incoming object from [" + uuid + "]: "
								+ incoming);
						continue;
					}

				} else {

					// No incoming objects atm.
					continue;
				}

			} catch (final EOFException e) {
				connected = false;
				e.printStackTrace();
				continue;
			} catch (final SocketException e) {
				if (e.getLocalizedMessage().equals("Connection reset")) {
					cleanUp();

				} else {
					e.printStackTrace();
				}

			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
				connected = false;
				continue;
			} catch (final IOException e) {
				e.printStackTrace();
				connected = false;
				continue;
			}
		}
	}

	private void cleanUp() {

		if (server.getClients().contains(this)) {
			server.getClients().remove(this);
			server.onlineClients -= 1;

			connected = false;
			try {
				if (oos != null) {
					oos.close();
				}
				if (oos != null) {
					ois.close();
				}
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
			server.debug("A client [" + uuid + "] has disconnected! (online: "
					+ server.onlineClients + ")");
			server.serverEventListener.clientDisconnected(this);

		}

	}

	public void sendPacket(final Packet packet) {
		if (oos != null && connected) {
			try {
				oos.writeObject(packet);
				oos.flush();
				server.debug("Packet sent to client [" + uuid + "]: "
						+ packet.getPacketName());
				server.sentPackets++;
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public int getUuid() {
		return uuid;
	}

	public boolean isConnected() {
		return connected;
	}

}
