import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import javafx.embed.swing.JFXPanel;
import javafx.scene.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.imageio.ImageIO;
import javax.swing.*;

import sun.audio.*;

public class MapleTD extends Canvas {
	private Image bgBuffer; // background buffer
	private int wave = 1; // current wave; each subsequent wave is harder than
							// the previous
	private int money = 100; // current money; use money to purchase towers or
								// upgrade current ones
	private int life = 100; // amount of life Empress Cygnus has left; you lose
							// when it reaches 0
	private int framerate = 30; // current framerate, increases to 60 for 2x
								// speed
	private int timer = 0; // current timer; counts the number of 1/30 seconds
							// (tics) that have passed since the wave started
	private int mousex, mousey; // mouse position, currently unused
	private MediaPlayer mp; // music
	private int whichMap; // which map will be played on
	private int phase = 3; // 0 for standby 1 for wave 2 for gameover 3 for main
							// menu 4 for instruction
	private int prephase = -1; // previous phase
	private int purchaseSelect = 5; // 0 for bw, 1 for wa, 2 for nw, 3 for dw, 4
									// for tb
	private int towerSelect = -1; // which already placed tower are you looking
									// at
	public String[] consoleOutput = new String[] { "", "", "", "", "" }; // output
																			// that
																			// will
																			// be
																			// shown
																			// to
																			// the
																			// player
	private File mayper; // music for main menu
	private File[] bgm = new File[3]; // music for each map

	public Map map = new Map(1, 0); // map object, main hub for running of the
									// game
	private int[] costs = new int[] { 100, 50, 75, 150, 125 }; // costs of each
																// tower,
																// respectively

	// current thread - runs each tic
	private Thread t = new Thread(new Runnable() {
		public void run() {
			// while this is the current thread
			while (Thread.currentThread() == t) {
				if (phase != prephase) // change music if there is a transition
										// between screens
					music();
				prephase = phase;
				if (phase == 1) // tic if the wave is ongoing
					tic();
				try {
					Thread.sleep(1000 / framerate); // framerate 30 fps (or 60
													// fps)
				} catch (InterruptedException e) {
					System.out.println("Main thread interrupted");
				}
				repaint(); // update the canvas
			}
		}
	});

	// read files
	public void reader() {
		mayper = new File("maypersutory.mp3");
		bgm[0] = new File("sleepywood_music.mp3");
		bgm[1] = new File("pyramid_music.mp3");
		bgm[2] = new File("sector_music.mp3");
	}

	// main class, starts everything
	public static void main(String[] args) {
		Frame f = new Frame();
		final JFXPanel fxPanel = new JFXPanel(); // just to avoid
													// illegalstateexception

		// close program when window closes
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.setSize(1100, 1000);
		MapleTD mtd = new MapleTD();
		f.setName("Maplestory Tower Defense");
		mtd.setName("Maplestory Tower Defense");
		f.add(mtd);
		f.show();
	}

	// change music if needed, otherwise start it
	public void music() {
		if (phase == 3 || phase == 4) {
			if (mp != null)
				mp.stop();
			mp = new MediaPlayer(new Media(mayper.toURI().toString()));
			mp.play();
		} else if (phase == 0 || phase == 1 || phase == 2) {
			if (mp != null)
				mp.stop();
			mp = new MediaPlayer(new Media(bgm[whichMap].toURI().toString()));
			mp.play();
		}
	}

	// constructor class, initialize variables
	public MapleTD() {
		super();
		reader();
		music();
		init();
		t.start();

		// keyboard control
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int kc = e.getKeyCode();
				switch (kc) {
				case KeyEvent.VK_ESCAPE: // close some extra information windows
					towerSelect = -1;
					purchaseSelect = 5;
					break;
				}
			}
		});

		// mouse control
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				mousex = e.getX();
				mousey = e.getY();

				// if in standby phase
				if (phase == 0) {
					if (e.getX() >= 840 && e.getX() <= 900 && e.getY() >= 880
							&& e.getY() <= 940) { // start wave at normal speed
						framerate = 30;
						phase = 1;
						prephase = phase;
					} else if (e.getX() >= 915 && e.getX() <= 975
							&& e.getY() >= 880 && e.getY() <= 940) { // start
																		// wave
																		// at 2x
																		// speed
						framerate = 60;
						phase = 1;
						prephase = phase;
					}
				}

				// if in wave phase
				if (phase == 1) {
					if (e.getX() >= 840 && e.getX() <= 900 && e.getY() >= 880
							&& e.getY() <= 940) { // set to normal speed
						framerate = 30;
					} else if (e.getX() >= 915 && e.getX() <= 975
							&& e.getY() >= 880 && e.getY() <= 940) { // set to
																		// 2x
																		// speed
						framerate = 60;
					} else if (e.getX() >= 990 && e.getX() <= 1050
							&& e.getY() >= 880 && e.getY() <= 940) { // pause
																		// the
																		// game
						phase = 0;
						prephase = phase;
					}
				}

				// if gameover
				if (phase == 2) {
					if (e.getX() >= 300 && e.getX() <= 500 && e.getY() >= 500
							&& e.getY() <= 600) { // back to main menu after
													// game over
						prephase = phase;
						phase = 3;
					}
				}

				if (phase == 3) { // level selection and main menu
					if (e.getX() >= 150 && e.getX() <= 350 && e.getY() >= 600
							&& e.getY() <= 700) { // select the first map after
													// clicking on first button
						whichMap = 0;
						prephase = phase;
						phase = 0;
						writeToOutput("Map 1 selected");
						init();
					} else if (e.getX() >= 450 && e.getX() <= 650
							&& e.getY() >= 600 && e.getY() <= 700) {// select
																	// the
																	// second
																	// map after
						// clicking on second button
						whichMap = 1;
						prephase = phase;
						phase = 0;
						writeToOutput("Map 2 selected");
						init();
					} else if (e.getX() >= 750 && e.getX() <= 950
							&& e.getY() >= 600 && e.getY() <= 700) {// select
																	// the third
																	// map after
						// clicking on third button
						whichMap = 2;
						prephase = phase;
						phase = 0;
						writeToOutput("Map 3 selected");
						init();
					} else if (e.getX() >= 300 && e.getX() <= 500
							&& e.getY() >= 725 && e.getY() <= 825) { // go to
																		// help
																		// menu
						phase = 4;
						prephase = phase;
					} else if (e.getX() >= 600 && e.getX() <= 800
							&& e.getY() >= 725 && e.getY() <= 825) { // exit the
																		// game
						System.exit(0);
					}
				}

				if (phase == 4) { // information screen
					if (e.getX() >= 450 && e.getX() <= 650 && e.getY() >= 800
							&& e.getY() <= 1000) { // return to main menu
						phase = 3;
						prephase = phase;
					}
				}

				// if you are in standby or wave; and using game gui
				if (phase < 2) {

					if (e.getX() >= 820 && e.getX() <= 1065 && e.getY() >= 810
							&& e.getY() <= 860) { // return to main menu from
													// game
						prephase = phase;
						phase = 3;
					}

					// select an existing tower for stats and upgrades
					if (e.getX() <= 800 && e.getY() <= 800)
						towerSelect = map.towerClicked(e.getX(), e.getY());

					// upgrade tower if you can
					if (e.getX() >= 840 && e.getX() <= 1065 && e.getY() >= 630
							&& e.getY() <= 730 && towerSelect > -1) {
						if (money < map.towers.get(towerSelect).upgradeCost)
							writeToOutput("Insufficient funds to upgrade "
									+ Tower.names[map.towers.get(towerSelect).type]);
						else {
							money -= map.towers.get(towerSelect).upgradeCost;
							map.towers.get(towerSelect).upgrade();
						}
					}

					// buy and place the new tower
					if (purchaseSelect != 5 && e.getX() <= 800
							&& e.getY() <= 800) {
						if (!map.canPlaceTower(e.getX(), e.getY())) {
							writeToOutput("Cannot place tower there at X "
									+ e.getX() + ", Y " + e.getY());
							purchaseSelect = 5;
						} else if (money < costs[purchaseSelect]) {
							writeToOutput("Insufficient funds for a "
									+ Tower.names[purchaseSelect]);
							purchaseSelect = 5;
						} else { // purchase and place
							money -= costs[purchaseSelect];
							map.towerPurchase(purchaseSelect, e.getX(),
									e.getY());
							purchaseSelect = 5;
						}
					} else
						purchaseSelect = 5;

					// select your tower for purchase or placement
					if (e.getX() >= 820 && e.getX() <= 1085 && e.getY() >= 10
							&& e.getY() <= 95) {
						purchaseSelect = 0;
						towerSelect = -1;
					} else if (e.getX() >= 820 && e.getX() <= 1085
							&& e.getY() >= 100 && e.getY() <= 185) {
						purchaseSelect = 1;
						towerSelect = -1;
					} else if (e.getX() >= 820 && e.getX() <= 1085
							&& e.getY() >= 190 && e.getY() <= 270) {
						purchaseSelect = 2;
						towerSelect = -1;
					} else if (e.getX() >= 820 && e.getX() <= 1085
							&& e.getY() >= 280 && e.getY() <= 360) {
						purchaseSelect = 3;
						towerSelect = -1;
					} else if (e.getX() >= 820 && e.getX() <= 1085
							&& e.getY() >= 370 && e.getY() <= 450) {
						purchaseSelect = 4;
						towerSelect = -1;
					}
				}
			}
		});
	}

	// write to the "console"
	public void writeToOutput(String s) {
		for (int x = 4; x > 0; x--) {
			consoleOutput[x] = consoleOutput[x - 1];
		}
		consoleOutput[0] = s;
	}

	public void createBGBuffer() {
		// create new buffer if none exists or wrong size
		if (bgBuffer == null || bgBuffer.getWidth(null) != getWidth()
				|| bgBuffer.getHeight(null) != getHeight())
			bgBuffer = createImage(getWidth(), getHeight());
	}

	// update panel
	public void update(Graphics g) {
		paint(g);
	}

	// paint class
	public void paint(Graphics g) {
		createBGBuffer();
		paintOntoSomethingElse(bgBuffer.getGraphics());// paint onto background
		// buffer
		g.drawImage(bgBuffer, 0, 0, null); // copy background to primary buffer
	}

	// painting
	public void paintOntoSomethingElse(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight()); // clear buffer
		if (phase == 1)
			tic();
		paint2(g);
	}

	// main paint class
	public void paint2(Graphics g) {
		Graphics2D g2 = (Graphics2D) (g);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // antialiasing
		if (phase >= 3) { // if you are in main menu or help menu
			mainMenu(g2);
		}
		if (phase < 3) { // if you are in the game
			gui(g2);
			map.drawPath(g2, whichMap);

			// // grid
			// g2.setStroke(new BasicStroke(1));
			// g2.setColor(new Color(0, 0, 0, 100));
			// for (int x = 0; x < 10; x++) {
			// g2.drawLine(x * 80, 0, x * 80, 800);
			// }
			// for (int x = 0; x < 10; x++) {
			// g2.drawLine(0, x * 80, 800, x * 80);
			// }

			if (towerSelect > -1) { // if you clicked on an existing tower, show
									// its range
				Tower temp = map.towers.get(towerSelect);
				g2.setColor(new Color(255, 255, 255, 50));
				g2.fill(temp.tRange);
			}

			map.drawMobs(g2);
			map.drawTower(g2);
			if (phase < 3 && phase != 0) // draw projectiles if you are actively
											// in a wave
				map.drawProjectiles(g2);

			if (phase == 2) { // if you lose, draw this stuff
				g2.setFont(new Font("Comic Sans MS", Font.BOLD, 100));
				g.setColor(Color.red);
				g2.drawString("YOU LOSE", 400 - g2.getFontMetrics()
						.stringWidth("YOU LOSE") / 2, 400);
				g2.fillRect(300, 500, 200, 100);
				g2.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
				g2.setColor(Color.white);
				g2.drawString("Main Menu", 400 - g2.getFontMetrics()
						.stringWidth("Main Menu") / 2, 560);
			}
		}
	}

	// main menu and help menu
	public void mainMenu(Graphics2D g) {
		try { // import background image and title
			g.drawImage(ImageIO.read(new File("MSBackground.png")), 0, 0, 1100,
					1000, null);
			g.drawImage(ImageIO.read(new File("Maplestory_Title.png")), 150,
					00, 800, 300, null);
		} catch (IOException e) {
			System.out.println("Background image not found");
		}
		consoleOutput = new String[] { "", "", "", "", "" }; // reset the
																// console

		// title
		g.setColor(Color.BLACK);
		g.setFont(new Font("Comic Sans MS", Font.BOLD, 100));
		String title = "Tower Defense";
		g.drawString(title, 550 - g.getFontMetrics().stringWidth(title) / 2,
				400);

		// main menu
		if (phase == 3) {
			// buttons to select levels
			g.setColor(Color.orange);
			g.fillRect(150, 600, 200, 100);
			g.fillRect(450, 600, 200, 100);
			g.fillRect(750, 600, 200, 100);
			g.setColor(Color.white);
			g.setFont(new Font("Comic Sans MS", Font.PLAIN, 32));
			g.drawString("Level 1",
					250 - g.getFontMetrics().stringWidth("Level 1") / 2, 660);
			g.drawString("Level 2",
					550 - g.getFontMetrics().stringWidth("Level 2") / 2, 660);
			g.drawString("Level 3",
					850 - g.getFontMetrics().stringWidth("Level 3") / 2, 660);

			// draw thumbnail of each map above its respective button
			g.drawImage(map.mapThumbs[0], 175, 425, 150, 150, null);
			g.drawImage(map.mapThumbs[1], 475, 425, 150, 150, null);
			g.drawImage(map.mapThumbs[2], 775, 425, 150, 150, null);
			g.setColor(Color.orange);
			g.drawRect(175, 425, 150, 150);
			g.drawRect(475, 425, 150, 150);
			g.drawRect(775, 425, 150, 150);

			// help button
			g.setColor(Color.CYAN);
			g.fillRect(300, 725, 200, 100);
			g.setColor(Color.white);
			g.drawString("Help",
					400 - g.getFontMetrics().stringWidth("Help") / 2, 785);

			// exit game button
			g.setColor(Color.RED);
			g.fillRect(600, 725, 200, 100);
			g.setColor(Color.white);
			g.drawString("Exit",
					700 - g.getFontMetrics().stringWidth("Exit") / 2, 785);
		} else { // help screen
			// help information
			String[] helpM = new String[] {
					"Welcome to Maplestory Tower Defense!",
					"Here you will learn the basic mechanics of the game.",
					"In the main menu, you can select a map to play on by clicking its corresponding button.",
					"Your goal is to prevent the enemies from reaching Empress Cygnus, who is located at the end",
					"of the path.",
					"Once inside the game, you can select a tower from the left-hand side of the screen and place it",
					"anywhere, as long as it is not on the track.",
					"Click the start button on the bottom right to start a wave.",
					"Once you have started the wave, you can go at 2x speed or pause for a moment.",
					"The information for each tower will be shown in on the left-hand side, under the towers.",
					"Kill more enemies to upgrade your towers, or buy new ones!",
					"However, with each wave, enemies will grow more powerful, but also give more money.",
					"Watch out for enemies near the end of the path, as they can do damage to Empress Cygnus!" };
			g.setColor(Color.GRAY);
			g.fillRect(200, 450, 700, 300);
			g.setColor(Color.orange);
			g.drawRect(200, 450, 700, 300);
			g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
			for (int x = 0; x < helpM.length; x++)
				g.drawString(helpM[x],
						550 - g.getFontMetrics().stringWidth(helpM[x]) / 2,
						480 + x * 20);

			// return to main menu
			g.setColor(Color.blue);
			g.fillRect(450, 800, 200, 100);
			g.setColor(Color.white);
			g.setFont(new Font("Comic Sans MS", Font.PLAIN, 32));
			g.drawString("Back",
					550 - g.getFontMetrics().stringWidth("Back") / 2, 860);
		}
	}

	public void gui(Graphics2D g) {

		// background and outline
		g.setColor(Color.gray);
		g.fillRect(0, 0, 8000, 8000);
		g.setColor(Color.orange);
		g.fillRect(0, 0, 805, 805);

		// console
		g.setStroke(new BasicStroke(5));
		g.setColor(Color.white);
		g.fillRect(300, 820, 500, 115);
		g.setColor(Color.black);
		g.drawRect(300, 820, 500, 115);
		g.setFont(new Font("Consolas", Font.PLAIN, 16));
		g.drawString(consoleOutput[4], 310, 840);
		g.drawString(consoleOutput[3], 310, 860);
		g.drawString(consoleOutput[2], 310, 880);
		g.drawString(consoleOutput[1], 310, 900);
		g.drawString(consoleOutput[0], 310, 920);

		// right column; towers
		g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
		g.setColor(Color.black); // upgrade towers
		g.setStroke(new BasicStroke(5));
		g.drawRect(815, 5, 255, 90);
		g.drawRect(815, 95, 255, 90);
		g.drawRect(815, 185, 255, 90);
		g.drawRect(815, 275, 255, 90);
		g.drawRect(815, 365, 255, 90);
		g.drawString("Blaze Wizard", 930, 30);
		g.drawString("Wind Archer", 930, 120);
		g.drawString("Night Walker", 930, 210);
		g.drawString("Dawn Warrior", 930, 300);
		g.drawString("Thunder Breaker", 930, 390);
		map.drawJob(g, 0, 820, 10, 70, 80);
		map.drawJob(g, 1, 820, 100, 70, 80);
		map.drawJob(g, 2, 820, 190, 70, 80);
		map.drawJob(g, 3, 820, 280, 70, 80);
		map.drawJob(g, 4, 820, 370, 70, 80);

		// user information
		g.setFont(new Font("Comic Sans MS", Font.PLAIN, 32));
		g.setColor(Color.green);
		g.drawString("Life: " + life, 5, 840);
		g.setColor(new Color(200, 200, 120));
		g.drawString("Money: $" + money, 5, 880);
		g.setColor(Color.black);
		g.drawString("Wave: " + wave, 5, 920);

		g.drawRect(815, 455, 255, 340);

		// purchase information
		if (purchaseSelect < 5) {
			double[] damages = new double[] { 1, 2, 1.2, 5, 3 };
			int[] fireRate = new int[] { 95, 45, 20, 60, 30 };
			double[] range = new double[] { 4, 6, 4, 3.5, 3 };
			String[] projs = new String[] { "AOE", "Projectile", "Projectile",
					"AOE", "AOE" };
			g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
			g.drawString("Tower: " + Tower.names[purchaseSelect], 820, 480);
			g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
			g.drawString("Damage: " + damages[purchaseSelect], 820, 505);
			g.drawString("Fire Rate: " + fireRate[purchaseSelect], 820, 525);
			g.drawString("Range: " + range[purchaseSelect] + " tiles", 820, 545);
			g.drawString("Attack Type: " + projs[purchaseSelect], 820, 565);
			g.drawString("Cost: $" + costs[purchaseSelect], 820, 585);
		}

		// upgrade information, when a tower is clicked
		if (towerSelect > -1) {
			Tower temp = map.towers.get(towerSelect);
			g.setColor(Color.black);
			g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
			g.drawString("Level " + temp.towerLevel + " "
					+ Tower.names[temp.type], 820, 480);
			g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
			g.drawString("Damage: " + temp.damage, 820, 505);
			g.drawString("Fire Rate: " + temp.fireRate, 820, 525);
			g.drawString("Range: " + temp.range, 820, 545);
			g.drawString("Damage: " + temp.damage, 820, 565);
			g.drawString("Cost to upgrade: " + temp.upgradeCost, 820, 585);
			g.setColor(Color.blue);
			g.fillRect(840, 630, 205, 100);
			g.setColor(Color.black);
			g.drawRect(840, 630, 205, 100);
			g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
			g.drawString("Upgrade", (int) (942.5 - g.getFontMetrics()
					.getStringBounds("Upgrade", g).getWidth() / 2), 685);
		}

		// return to main menu
		g.setColor(Color.blue);
		g.fillRect(820, 810, 245, 50);
		g.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
		g.setColor(Color.white);
		g.drawString("Main Menu", 860, 845);

		// start, 2x speed, and pause button
		if (phase == 0)
			g.setColor(Color.green);
		else
			g.setColor(Color.DARK_GRAY);
		if (framerate == 60)
			g.setColor(Color.green);
		g.fillRect(840, 880, 60, 60);
		if (framerate == 30)
			g.setColor(Color.green);
		else if (phase != 0)
			g.setColor(Color.DARK_GRAY);
		g.fillRect(915, 880, 60, 60);
		g.setColor(Color.white);
		g.fillPolygon(new int[] { 855, 890, 855 }, new int[] { 890, 910, 930 },
				3);
		g.fillPolygon(new int[] { 925, 950, 925 }, new int[] { 890, 910, 930 },
				3);
		g.fillPolygon(new int[] { 945, 970, 945 }, new int[] { 890, 910, 930 },
				3);
		if (phase == 1)
			g.setColor(Color.green);
		else
			g.setColor(Color.DARK_GRAY);
		g.fillRect(990, 880, 60, 60);
		g.setColor(Color.white);
		g.fillRect(1007, 890, 10, 40);
		g.fillRect(1023, 890, 10, 40);
	}

	// initialize your user information at the start of each new game
	public void init() {
		map = new Map(1, whichMap);
		life = 100;
		money = 100;
		wave = 1;
		timer = 0;
	}

	// each "tick" of time, updates the game
	public void tic() {
		if (life <= 0) { // if you lose
			writeToOutput("Game over");
			prephase = phase;
			phase = 2;
		}
		if (timer >= wave * 75 && map.getMobs().isEmpty()) { // if you have
																// finished a
																// wave
			writeToOutput("Level " + wave);
			timer = 0;
			money += wave * 10;
			wave++;
			map.updateLevel(wave);
			phase = 0;
			prephase = phase;
		} else if ((!map.getMobs().isEmpty() && phase != 0) || phase == 1) { // if
																				// you
																				// are
																				// currently
																				// in
																				// a
																				// wave
			life -= map.tic(timer);
			money += map.checkMoney();
			timer++;
		}
	}
}
