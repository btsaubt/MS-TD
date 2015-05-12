import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;

public class MapleTD extends Canvas {
	private Image bgBuffer; // background buffer
	private int wave = 1;
	private int money = 100;
	private int life = 100;
	private int framerate = 30;
	private int timer = 0;
	private int mousex, mousey;
	private int whichMap;
	private int phase = 3; // 0 for standby 1 for wave 2 for gameover 3 for main
							// menu 4 for instruction
	private int purchaseSelect = 5; // 0 for bw, 1 for wa, 2 for nw, 3 for dw, 4
									// for tb
	private int towerSelect = -1; // which already placed tower are you looking
									// at
	public String[] consoleOutput = new String[] { "", "", "", "", "" };

	public Map map = new Map(1, 0);;
	private int[] costs = new int[] { 100, 50, 75, 150, 125 };

	private Thread t = new Thread(new Runnable() {
		public void run() {
			while (Thread.currentThread() == t) {
				if (phase == 1)
					tic();
				try {
					Thread.sleep(1000 / framerate);
				} catch (InterruptedException e) {
					System.out.println("Main thread interrupted");
				}
				repaint();
			}
		}
	});

	public static void main(String[] args) {
		Frame f = new Frame();
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

	public MapleTD() {
		super();
		init();
		t.start();
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int kc = e.getKeyCode();
				switch (kc) {
				case KeyEvent.VK_ESCAPE:
					towerSelect = -1;
					purchaseSelect = 5;
					break;
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				mousex = e.getX();
				mousey = e.getY();
				if (phase == 0) {
					if (e.getX() >= 840 && e.getX() <= 900 && e.getY() >= 880
							&& e.getY() <= 940) {
						framerate = 30;
						phase = 1;
					} else if (e.getX() >= 915 && e.getX() <= 975
							&& e.getY() >= 880 && e.getY() <= 940) {
						framerate = 60;
						phase = 1;
					}
				}
				if (phase == 1) {
					if (e.getX() >= 840 && e.getX() <= 900 && e.getY() >= 880
							&& e.getY() <= 940) {
						framerate = 30;
					} else if (e.getX() >= 915 && e.getX() <= 975
							&& e.getY() >= 880 && e.getY() <= 940) {
						framerate = 60;
					} else if (e.getX() >= 990 && e.getX() <= 1050
							&& e.getY() >= 880 && e.getY() <= 940) {
						phase = 0;
					}
				}
				if (phase == 2) {
					if (e.getX() >= 300 && e.getX() <= 500 && e.getY() >= 500
							&& e.getY() <= 600)
						phase = 3;
				}
				if (phase == 3) { // level selection
					if (e.getX() >= 150 && e.getX() <= 350 && e.getY() >= 600
							&& e.getY() <= 700) {
						whichMap = 0;
						phase = 0;
						writeToOutput("Map 1 selected");
						init();
					} else if (e.getX() >= 450 && e.getX() <= 650
							&& e.getY() >= 600 && e.getY() <= 700) {
						whichMap = 1;
						phase = 0;
						writeToOutput("Map 2 selected");
						init();
					} else if (e.getX() >= 750 && e.getX() <= 950
							&& e.getY() >= 600 && e.getY() <= 700) {
						whichMap = 2;
						phase = 0;
						writeToOutput("Map 3 selected");
						init();
					} else if (e.getX() >= 300 && e.getX() <= 500
							&& e.getY() >= 725 && e.getY() <= 825) {
						phase = 4;
					} else if (e.getX() >= 600 && e.getX() <= 800
							&& e.getY() >= 725 && e.getY() <= 825) {
						System.exit(0);
					}
				}
				if (phase == 4) { // information screen
					if (e.getX() >= 450 && e.getX() <= 650 && e.getY() >= 800
							&& e.getY() <= 1000)
						phase = 3;
				}
				if (phase < 2) {
					if (e.getX() >= 820 && e.getX() <= 1065 && e.getY() >= 810
							&& e.getY() <= 860)
						phase = 3;

					if (e.getX() <= 800 && e.getY() <= 800)
						towerSelect = map.towerClicked(e.getX(), e.getY());

					// upgrade tower
					if (e.getX() >= 840 && e.getX() <= 1065 && e.getY() >= 630
							&& e.getY() <= 730 && towerSelect > -1) {
						if (money < map.towers.get(towerSelect).upgradeCost)
							writeToOutput("Insufficient funds to upgrade "
									+ Tower.names[map.towers.get(towerSelect).type]);
						else {
							map.towers.get(towerSelect).upgrade();
							money -= map.towers.get(towerSelect).upgradeCost;
						}
					}

					// buy a new tower
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

	public void writeToOutput(String s) {
		// String temp = consoleOutput[0];
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

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {

		createBGBuffer();
		paintOntoSomethingElse(bgBuffer.getGraphics());// paint onto background
		// buffer
		g.drawImage(bgBuffer, 0, 0, null); // copy background to primary buffer
	}

	public void paintOntoSomethingElse(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight()); // clear buffer
		if (phase == 1)
			tic();
		else if (phase == 2) {
			Graphics2D g2D = (Graphics2D) (g);
			g2D.fillRect(250, 350, 600, 300);
			g2D.setFont(new Font("Comic Sans", Font.BOLD, 69));
			g2D.drawString("You lost", 300, 300);
		}
		paint2(g);
	}

	public void paint2(Graphics g) {
		Graphics2D g2 = (Graphics2D) (g);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		if (phase >= 3) {
			mainMenu(g2);
		}
		if (phase < 3) {
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

			if (towerSelect > -1) {
				Tower temp = map.towers.get(towerSelect);
				g2.setColor(new Color(255, 255, 255, 50));
				g2.fill(temp.tRange);
			}

			map.drawMobs(g2);
			map.drawTower(g2);
			if (phase < 3 && phase != 0)
				map.drawProjectiles(g2);

			if (phase == 2) {
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

	public void mainMenu(Graphics2D g) {
		try {
			g.drawImage(ImageIO.read(new File("MSBackground.png")), 0, 0, 1100,
					1000, null);
			g.drawImage(ImageIO.read(new File("Maplestory_Title.png")), 150,
					00, 800, 300, null);
		} catch (IOException e) {
			System.out.println("Background image not found");
		}

		g.setColor(Color.BLACK);
		g.setFont(new Font("Comic Sans MS", Font.BOLD, 100));
		String title = "Tower Defense";
		g.drawString(title, 550 - g.getFontMetrics().stringWidth(title) / 2,
				400);

		if (phase == 3) {
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

			g.drawImage(map.mapThumbs[0], 175, 425, 150, 150, null);
			g.drawImage(map.mapThumbs[1], 475, 425, 150, 150, null);
			g.drawImage(map.mapThumbs[2], 775, 425, 150, 150, null);
			g.setColor(Color.orange);
			g.drawRect(175, 425, 150, 150);
			g.drawRect(475, 425, 150, 150);
			g.drawRect(775, 425, 150, 150);

			g.setColor(Color.CYAN);
			g.fillRect(300, 725, 200, 100);
			g.setColor(Color.white);
			g.drawString("Help",
					400 - g.getFontMetrics().stringWidth("Help") / 2, 785);

			g.setColor(Color.RED);
			g.fillRect(600, 725, 200, 100);
			g.setColor(Color.white);
			g.drawString("Exit",
					700 - g.getFontMetrics().stringWidth("Exit") / 2, 785);
		} else {
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

		// right column
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

		// purchase information
		g.drawRect(815, 455, 255, 340);
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
			// g.setColor(new Color(255, 255, 255, 50));
			// g.fill(temp.tRange);
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

	public void init() {
		map = new Map(1, whichMap);
		life = 100;
		money = 100;
		wave = 1;
		timer = 0;
	}

	public void tic() {
		if (life <= 0) {
			writeToOutput("Game over");
			phase = 2;
		}
		if (timer >= wave * 75 && map.getMobs().isEmpty()) {
			writeToOutput("Level " + wave);
			timer = 0;
			money += wave * 10;
			wave++;
			map.updateLevel(wave);
			phase = 0;
		} else if ((!map.getMobs().isEmpty() && phase != 0) || phase == 1) {
			life -= map.tic(timer);
			money += map.checkMoney();
			timer++;
		}
	}
}
