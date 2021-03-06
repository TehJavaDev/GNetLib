package org.gnet.server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class ServerMonitor {

	private final JFrame frame;
	private JPanel gfxPanel;
	private int updatesPerSecond;
	private int framesPerSecond;
	private double delta;
	private boolean monitorRunning;
	private final GNetServer server;
	private final Font monitorFont;
	private long freeMem; // current free memory
	private long usedMem; // current used memory

	private final long initialMem; // initial memory limit
	private long allocatedMem; // current allocated mem
	private ArrayList<String> debugText;
	private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
	private static final Runtime RUNTIME = Runtime.getRuntime();
	private static final long MB = 1024 * 1024;

	public ServerMonitor(final GNetServer server) {
		this.server = server;
		frame = new JFrame("GNetLib Server Monitor");
		frame.setPreferredSize(new Dimension(800, 380));
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setupGFXPanel();
		frame.getContentPane().add(gfxPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		monitorFont = new Font("Serif", Font.ROMAN_BASELINE, 16);
		initialMem = RUNTIME.totalMemory() / MB;
	}

	public void setDefaultCloseOperation(int option){
		frame.setDefaultCloseOperation(option);
	}
	
	private void render(final Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, gfxPanel.getWidth(), gfxPanel.getHeight());
		g.setColor(Color.yellow);
		g.drawString("FPS: " + framesPerSecond, 20, 20);
		g.drawString("UPS: " + updatesPerSecond, 20, 40);
		g.setColor(Color.white);
		final int xOffset = 20;
		final int yOffset = 50;
		g.drawRect(xOffset, yOffset, gfxPanel.getWidth() - xOffset * 2,
				gfxPanel.getHeight() - yOffset * 2);

		debugText = generateDebugText();

		g.setFont(monitorFont);
		final int textX = xOffset + 30;
		final int textY = yOffset + 20;
		final int xDistance = 530;
		int i2 = 0;
		for (int i = 0; i < debugText.size(); i++) {
			if (i < 9) {
				if (i == 0) {
					g.drawString(debugText.get(i), textX, textY); // left
																	// header.
				} else {
					g.drawString(debugText.get(i), textX, textY + 20 + 20 * i); // left
																				// contents
				}
			} else if (i > 8) {
				if (i == 9) {
					g.drawString(debugText.get(i), textX + xDistance, textY); // right
																				// header.
					i2++;
				} else {
					g.drawString(debugText.get(i), textX + xDistance, textY
							+ 20 + 20 * i2); // right contents.
					i2++;
				}
			}
		}

	}

	private ArrayList<String> generateDebugText() {
		debugText = new ArrayList<String>();
		debugText.add("SERVER STATISTICS:");
		debugText.add("Server host: " + server.getHost());
		debugText.add("Server port: " + server.getPort());
		debugText.add("Server bound: " + server.tcpBound);
		debugText.add("Server accepting clients: " + server.connectNewClients);
		debugText.add("Online clients: " + server.getOnlineClients());
		debugText.add("Client list size: " + server.clients.size());
		debugText.add("Sent packets: " + server.sentPackets);
		debugText.add("Received packets: " + server.recievedPackets);

		debugText.add("MEMORY STATISTICS:");
		debugText.add("Initial megabytes: " + initialMem);
		debugText.add("Allocated megabytes: " + allocatedMem);
		debugText.add("Free megabytes: " + freeMem);
		debugText.add("Used megabytes: " + usedMem);
		return debugText;
	}

	private void render(final long l) {
		delta = l;
		gfxPanel.repaint();
		TOOLKIT.sync();
	}

	private void update() {
		updateMemUsage();
	}

	private void updateMemUsage() {
		freeMem = RUNTIME.freeMemory() / MB;
		allocatedMem = RUNTIME.totalMemory() / MB;
		usedMem = allocatedMem - freeMem;
	}

	private void setupGFXPanel() {
		gfxPanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			public void paint(final Graphics g) {
				final Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
						RenderingHints.VALUE_DITHER_ENABLE);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST,
						new Integer(100));
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
						RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
						RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
						RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);
				render(g2d);
			}
		};
		gfxPanel.setPreferredSize(frame.getSize());
	}

	public void show() {
		frame.setVisible(true);
	}

	public void hide() {
		frame.setVisible(false);
	}

	public void start() {
		if (server.isBindingComplete()) {
			monitorRunning = true;
			new Thread() {
				@Override
				public void run() {
					ServerMonitor.this.run(60, 60, 1);
				};
			}.start();
		}

	}

	private void run(final int desiredUps, final int desiredFps,
			final int frameSkip) {
		framesPerSecond = desiredFps;
		updatesPerSecond = desiredUps;
		long now = System.nanoTime();
		long logicTime = System.nanoTime();
		long renderTime = System.nanoTime();

		long fpsStart = System.currentTimeMillis();
		long upsStart = System.currentTimeMillis();
		final long frameLogicTime = 1000000000 / desiredUps;
		final long frameRenderTime = 1000000000 / desiredFps;

		int skippedFrames = 0;
		int frames = 0;
		int ups = 0;

		while (monitorRunning) {

			now = System.nanoTime();
			skippedFrames = 0;

			while (logicTime + frameLogicTime < now
					&& skippedFrames < frameSkip) {
				update();
				// UPS counter implementation.
				ups++;
				if (System.currentTimeMillis() - upsStart >= 1000) {
					updatesPerSecond = ups;
					ups = 0;
					upsStart = System.currentTimeMillis();
				}
				skippedFrames++;
				logicTime += frameLogicTime;

			}

			while (renderTime + frameRenderTime < now) {

				render(frameLogicTime / 1000);
				// FPS counter implementation.
				frames++;
				if (System.currentTimeMillis() - fpsStart >= 1000) {
					framesPerSecond = frames;
					frames = 0;
					fpsStart = System.currentTimeMillis();
				}
				renderTime += frameRenderTime;

			}
		}
	}

}
