import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class Map {
	public int wave;

	public BufferedImage[] maps = new BufferedImage[3];
	public BufferedImage[] paths = new BufferedImage[3];
	public BufferedImage[] sprites = new BufferedImage[3];
	public BufferedImage[] towerImages = new BufferedImage[5];
	public BufferedImage[] projectileImages = new BufferedImage[2];
	public BufferedImage[] attackImages = new BufferedImage[5];
	public BufferedImage[] mapThumbs = new BufferedImage[3];
	public BufferedImage cygnus;

	public ArrayList<Mob> mobs = new ArrayList<Mob>();
	public ArrayList<Tower> towers = new ArrayList<Tower>();
	public ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
	public int pathLength = 0; // total travel length,
	public int moneyAdd = 0;

	// 0 for up, 1 for down, 2 for left, 3 for right
	public int[][] direction = new int[][] { { 1, 3, 0, 3, 1 },
			{ 1, 3, 1, 2, 1, 3, 1, 2, 1 }, { 1, 3, 0, 2, 1, 3, 0, 2 } };
	public double[][] lengths = new double[][] { { 8.5, 3.5, 7, 3.5, 9.5 },
			{ 2, 7, 2, 7, 2, 7, 2, 7, 1.5 }, { 8.5, 7, 7, 5, 5, 3, 3, 1 } };

	public int[][] ends = new int[][] { new int[] { 640, 720 },
			new int[] { 80, 720 }, new int[] { 400, 240 } };

	public int[][] path1Coords = new int[][] {
			{ 80, 80, 440, 440, 640, 640, 720, 720, 360, 360, 160, 160 },
			{ 0, 720, 720, 160, 160, 800, 800, 80, 80, 640, 640, 0 } };
	public int[][] path2Coords = new int[][] {
			{ 80, 80, 640, 640, 80, 80, 640, 640, 80, 80, 160, 160, 720, 720,
					160, 160, 720, 720, 160, 160 },
			{ 0, 200, 200, 280, 280, 520, 520, 600, 600, 800, 800, 680, 680,
					440, 440, 360, 360, 120, 120, 0 } };
	public int[][] path3Coords = new int[][] {
			{ 80, 80, 720, 720, 240, 240, 560, 560, 400, 400, 480, 480, 320,
					320, 640, 640, 160, 160 },
			{ 0, 720, 720, 80, 80, 560, 560, 240, 240, 320, 320, 480, 480, 160,
					160, 640, 640, 0 } };
	public int[][][] pathCoords = new int[][][] { path1Coords, path2Coords,
			path3Coords };
	public Polygon pathPoly = new Polygon();
	public int whichMap = 0; // map 0, 1, 2

	public Map(int l, int p) {
		wave = l;
		whichMap = p;
		pathLength = 0;
		for (int x = 0; x < lengths[whichMap].length; x++) {
			pathLength += lengths[whichMap][x];
		}
		reader();
		pathPoly = new Polygon(pathCoords[whichMap][0],
				pathCoords[whichMap][1], pathCoords[whichMap][0].length);

		// TEMPORARY TESTING
		// towerPurchase(2, 450, 400);
	}

	public void drawJob(Graphics2D g, int t, int x, int y, int w, int h) {
		g.drawImage(towerImages[t], x, y, w, h, null);
	}

	public void drawMobs(Graphics2D g) {
		Mob t;
		g.setColor(Color.green);
		for (int x = 0; x < mobs.size(); x++) {
			t = mobs.get(x);
			int[] c = t.calcCoords(direction[whichMap], lengths[whichMap],
					pathLength);
			g.fillRect(c[0] - 30, c[1],
					(int) (60.0 * (1.0 * Math.round(t.hp) / (wave * wave))), 8); // health
																					// bar
			g.drawImage(sprites[whichMap], c[0] - sprites[whichMap].getWidth()
					/ 2, c[1], null);
		}
	}

	// add a tower to the arraylist of towers
	public void towerPurchase(int t, int x, int y) {
		towers.add(new Tower(t, x, y));
	}

	// returns if a tower can be placed at the center of given coords; must not
	// intersect path or any other towers
	public boolean canPlaceTower(int x, int y) {
		boolean temp = (!pathPoly.intersects(new Rectangle2D.Double(x - 30,
				y - 30, 60, 60))) && x >= 0 && y >= 0 && x <= 770 && y <= 770;
		for (int z = 0; z < towers.size(); z++)
			temp = temp
					&& !new Rectangle2D.Double(
							towers.get(z).getCoords()[0] - 30, towers.get(z)
									.getCoords()[1] - 30, 60, 60)
							.intersects(new Rectangle2D.Double(x - 30, y - 30,
									60, 60));
		return temp;
	}

	public int checkMoney() {
		int temp = moneyAdd;
		moneyAdd = 0;
		return temp;
	}

	public int towerClicked(int x, int y) {
		for (int z = 0; z < towers.size(); z++)
			if (new Rectangle2D.Double(towers.get(z).getCoords()[0] - 30,
					towers.get(z).getCoords()[1] - 30, 60, 60)
					.intersects(new Rectangle2D.Double(x - 30, y - 30, 60, 60)))
				return z;
		return -1;
	}

	public int tic(int timer) {
		int damage = 0;

		if (timer == 0)
			moneyAdd = 0;

		// add a new mob
		if (timer % 30 == 0 && timer < 150 * wave) {
			mobs.add(new Mob(wave));
		}

		// mob movement
		for (int x = 0; x < mobs.size(); x++) {
			if (mobs.get(x).getExist()){
				int t = mobs.get(x).move(pathLength);
				if(t!=0){
					damage+=t;
					mobs.remove(x);
				}
			}
			else {
				mobs.remove(x);

				// add money
				moneyAdd += wave * 2;
			}
		}

		// projectile movement
		for (int x = 0; x < projectiles.size(); x++) {
			if (!projectiles.get(x).exist)
				projectiles.remove(x);
			else
				projectiles.get(x).move();
		}

		// tower attacking
		for (int x = 0; x < towers.size(); x++) {
			towers.get(x).doAttack(mobs, sprites[whichMap].getWidth(),
					direction[whichMap], projectiles);
		}
		return damage;
	}

	public void drawTower(Graphics2D g) {
		Tower t;
		int[] c;
		for (int x = 0; x < towers.size(); x++) {
			t = towers.get(x);
			c = t.coords;
			g.drawImage(towerImages[t.type], c[0] - 30, c[1] - 30, 60, 60, null);
			// g.fill(t.tRange);
			if (t.animate)
				g.drawImage(attackImages[t.type], c[0] - 60, c[1] - 30, 120,
						120, null);
		}
	}

	public void updateLevel(int w) {
		wave = w;
	}

	public void drawProjectiles(Graphics2D g) {
		for (int x = 0; x < projectiles.size(); x++) {
			Projectile p = projectiles.get(x);
			if (p.exist)
				g.drawImage(projectileImages[p.type], p.currentLoc[0],
						p.currentLoc[1], null);
		}
	}

	public void drawPath(Graphics2D g, int wm) {
		g.setColor(new Color(234, 206, 106));
		g.drawImage(maps[wm], 0, 0, 400, 400, null);
		g.drawImage(maps[wm], 400, 0, 400, 400, null);
		g.drawImage(maps[wm], 0, 400, 400, 400, null);
		g.drawImage(maps[wm], 400, 400, 400, 400, null);
		g.fillPolygon(pathPoly);

		g.drawImage(cygnus, ends[whichMap][0], ends[whichMap][1], 80, 80, null);
	}

	public ArrayList<Mob> getMobs() {
		return mobs;
	}

	public void reader() {
		try {
			BufferedImage temp;

			maps[0] = ImageIO.read(new File("sector_map.png"));
			maps[1] = ImageIO.read(new File("pyramid_map.png"));
			maps[2] = ImageIO.read(new File("leafre_map.png"));

			paths[0] = ImageIO.read(new File("sector_path.png"));
			paths[1] = ImageIO.read(new File("pyramid_path.png"));
			paths[2] = ImageIO.read(new File("leafre_path.png"));

			sprites[0] = ImageIO.read(new File("Orange_Mushroom.gif"));
			sprites[1] = ImageIO.read(new File("Commander_Skeleton.png"));
			sprites[2] = ImageIO.read(new File("Stone_Golem.png"));
			sprites[1] = sprites[1].getSubimage(5, 5, 119, 135);
			temp = new BufferedImage(80, 80, BufferedImage.TYPE_4BYTE_ABGR_PRE);
			temp.getGraphics().drawImage(sprites[1], 0, 0, temp.getWidth(),
					temp.getHeight(), 0, 0, sprites[1].getWidth(),
					sprites[1].getHeight(), null);
			sprites[1] = temp;
			sprites[2] = sprites[2].getSubimage(5, 5, 175, 150);
			temp = new BufferedImage(80, 80, BufferedImage.TYPE_4BYTE_ABGR_PRE);
			temp.getGraphics().drawImage(sprites[2], 0, 0, temp.getWidth(),
					temp.getHeight(), 0, 0, sprites[2].getWidth(),
					sprites[2].getHeight(), null);
			sprites[2] = temp;

			attackImages[0] = ImageIO.read(new File("FireAttack.png"));
			attackImages[1] = ImageIO.read(new File("ArcherAttack.png"));
			attackImages[2] = ImageIO.read(new File("ThiefAttack.png"));
			attackImages[3] = ImageIO.read(new File("SwordAttack.png"));
			attackImages[4] = ImageIO.read(new File("BreakerAttack.png"));

			projectileImages[0] = ImageIO.read(new File("Arrow.png"));
			projectileImages[1] = ImageIO.read(new File("Star.png"));

			towerImages[0] = ImageIO.read(new File("BlazeWizard.png"));
			towerImages[1] = ImageIO.read(new File("WindArcher.png"));
			towerImages[2] = ImageIO.read(new File("NightWalker.png"));
			towerImages[3] = ImageIO.read(new File("DawnWarrior.png"));
			towerImages[4] = ImageIO.read(new File("ThunderBreaker.png"));

			cygnus = ImageIO.read(new File("Empress_Cygnus.png"));
			
			for(int x=0; x<3; x++){
				mapThumbs[x] = new BufferedImage(800, 800, BufferedImage.TYPE_4BYTE_ABGR);
				pathPoly = new Polygon(pathCoords[x][0],
						pathCoords[x][1], pathCoords[x][0].length);
				drawPath((Graphics2D)(mapThumbs[x].getGraphics()), x);
			}
		} catch (IOException e) {
			System.out.println("Map file not found");
		}
	}
}
