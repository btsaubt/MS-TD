import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Mob {
	public double hp = 0;
	public int speed; // pixels per second
	public double pathPos; // position on the path's grid of 80x80 squares
	public int level;
	public boolean exist = false;
	public int[] coords = new int[]{-9999,-9999};

	public Mob(int wave) {
		hp = wave * wave;
		speed = 3;
		pathPos = 0;
		exist = true;
	}

	public void isHit(double damage) {
		hp -= damage;
		if (hp <= 0) {
			exist = false;
		}
	}

	public int[] getCoords() {
		return coords;
	}

	// returns coordinates (top center position on path) for drawing
	public int[] calcCoords(int[] d, double[] l, int length) {
		coords = new int[] { 120, -40 };
		double tempL = pathPos;
		int t = 0;
		boolean agin = true;
		do {
			if (t < l.length)
				if (tempL > l[t]) {
					tempL -= l[t];
					if (d[t] == 0)
						coords[1] -= l[t] * 80;
					else if (d[t] == 1)
						coords[1] += l[t] * 80;
					else if (d[t] == 2)
						coords[0] -= l[t] * 80;
					else
						coords[0] += l[t] * 80;
				} else {
					if (d[t] == 0)
						coords[1] -= tempL * 80;
					else if (d[t] == 1)
						coords[1] += tempL * 80;
					else if (d[t] == 2)
						coords[0] -= tempL * 80;
					else
						coords[0] += tempL * 80;
					agin = false;
				}
			else
				agin = false;
			t++;
		} while (agin);
		if (!exist)
			return new int[] { 9999, 9999 };
		return coords;
	}

	public boolean getExist() {
		return exist;
	}

	// returns damage dealt to your health
	public int move(int pathL) {
		pathPos += 1.0 * speed / 80;
		if (exist && pathPos > pathL) {
			exist = false;
			return (int) Math.round(hp);
		}
		return 0;
	}
}
