import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Tower {
	// towers will be 60x60 pixels in size

	public int type; // 0 for blaze wizard, 1 for wind archer, 2 for night
						// walker
	public double damage = 0;
	public int attack = 0; // 0 for simple projectile, 1 for area
	public int fireRate = 30; // number of frames (30 fps) until next attack
	public int timer = 0;
	public int[] coords = new int[] { 0, 0 }; // coordinates of center of tower
	public double range = 1; // radius of attack (* 60 pixels)
	public static final String[] names = new String[] { "Blaze Wizard",
			"Wind Archer", "Night Walker", "Dawn Warrior", "Thunder Breaker" };
	public Ellipse2D tRange;
	public boolean animate = false;
	public int towerLevel = 1;
	private int[] costs = new int[] { 100, 50, 75, 150, 125 };
	public int upgradeCost;

	public Tower(int t, int x, int y) {
		coords = new int[] { x, y };
		type = t;
		upgradeCost = (int) (costs[t] * 1.5);
		if (t == 0) {
			damage = 1;
			attack = 1;
			fireRate = 95;
			range = 4;
		}
		if (t == 1) {
			damage = 2;
			attack = 0;
			fireRate = 45;
			range = 6;
		}
		if (t == 2) {
			damage = 1.2;
			attack = 0;
			fireRate = 20;
			range = 4;
		}
		if (t == 3) {
			damage = 5;
			attack = 1;
			fireRate = 60;
			range = 3.5;
		}
		if (t == 4) {
			damage = 3;
			attack = 1;
			fireRate = 30;
			range = 3;
		}
		tRange = new Ellipse2D.Double(coords[0] - range * 40, coords[1] - range
				* 40, range * 80, range * 80);
	}

	public void upgrade() {
		towerLevel++;
		damage *= 1.3;
		fireRate *= .93;
		range *= 1.2;
		upgradeCost *= 1.5;
	}

	public void doAttack(ArrayList<Mob> m, int wid, int[] d,
			ArrayList<Projectile> projs) {
		if (timer != 0)
			timer++;
		int[] c = new int[] { 0, 0 };
		boolean attacked = false;
		if (timer % fireRate > fireRate / 3 || m.isEmpty())
			animate = false;
		if (timer % fireRate == 0)
			for (int x = 0; x < m.size(); x++) {
				c = m.get(x).coords;
				Rectangle2D t = new Rectangle2D.Double(c[0] - wid / 2, c[1],
						40, 40);
				if (tRange.intersects(t) && !attacked) {
					timer++;
					animate = true;
					if (attack == 0) {
						attacked = true;
						projs.add(new Projectile(damage, m.get(x), new int[] {
								coords[0], coords[1] }, type - 1));
					} else if (attack == 1) {
						m.get(x).isHit(damage);
					}
					if (attacked)
						return;
				}
			}
	}

	public int[] getCoords() {
		return coords;
	}
}
