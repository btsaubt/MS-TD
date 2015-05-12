import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Testering extends Canvas {
	public BufferedImage[] maps = new BufferedImage[3];
	public BufferedImage[] paths = new BufferedImage[3];
	public BufferedImage[] sprites = new BufferedImage[3];

	public int[][] direction = new int[][] { new int[] { 1, 3, 0, 3, 1 },
			new int[] { 1, 3, 1, 2, 1, 3, 1, 2, 1 },
			new int[] { 1, 3, 0, 2, 1, 3, 0, 2 } };
	public double[][] lengths = new double[][] {
			new double[] { 8.5, 3.5, 7, 3.5, 8.5 },
			new double[] { 2, 7, 2, 7, 2, 7, 2, 7, 2 },
			new double[] { 8.5, 7, 7, 5, 5, 3, 3, 1 } };
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
	public Polygon pathPoly = new Polygon();

	public void paint(Graphics g) {
		// g.fillRect(0, 0, 30000, 30000);
		// g.drawImage(sprites[0], 0, 0, null);
		// g.drawImage(sprites[1], 100, 0, null);
		// g.drawImage(sprites[2], 200, 0, null);
		g.fillRect(0, 0, 800, 800);
		g.setColor(Color.green);
		pathPoly = new Polygon(path3Coords[0], path3Coords[1],
				path3Coords[0].length);
		g.fillPolygon(pathPoly);
	}

	public static void main(String[] args) {
		Frame f = new Frame();
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.setSize(1100, 1000);
		Testering mtd = new Testering();
		f.add(mtd);
		f.show();
	}

	public Testering() {
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
			temp = new BufferedImage(80, (int) (80.0 * 130 / 114),
					BufferedImage.TYPE_4BYTE_ABGR_PRE);
			temp.getGraphics().drawImage(sprites[1], 0, 0, temp.getWidth(),
					temp.getHeight(), 0, 0, sprites[1].getWidth(),
					sprites[1].getHeight(), null);
			sprites[1] = temp;

			sprites[2] = sprites[2].getSubimage(5, 5, 175, 150);
			temp = new BufferedImage(100, (int) (100.0 * 145 / 170),
					BufferedImage.TYPE_4BYTE_ABGR_PRE);
			temp.getGraphics().drawImage(sprites[2], 0, 0, temp.getWidth(),
					temp.getHeight(), 0, 0, sprites[2].getWidth(),
					sprites[2].getHeight(), null);
			sprites[2] = temp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void update(Graphics g) {
		paint(g);
	}
}
