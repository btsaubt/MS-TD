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
	public int timer = 0; // self-timer for next attack
	public int[] coords = new int[] { 0, 0 }; // coordinates of center of tower
	public double range = 1; // radius of attack (* 60 pixels)
	public static final String[] names = new String[] { "Blaze Wizard",
			"Wind Archer", "Night Walker", "Dawn Warrior", "Thunder Breaker" }; // names
																				// of
																				// each
																				// tower
	public int attackN = 0; // number of times you have attacked
	public Ellipse2D tRange; // range of tower
	public boolean animate = false; // do attack animation?
	public int towerLevel = 1; // current tower level
	private int[] costs = new int[] { 100, 50, 75, 150, 125 }; // costs for each
																// tower
	public int upgradeCost = 0; // how much it costs to upgrade this tower

	// constructor for tower
	public Tower(int t, int x, int y) {
		coords = new int[] { x, y };
		type = t;
		upgradeCost = (int) (costs[t] * 1.5);
		if (t == 0) {
			damage = 5;
			attack = 1;
			fireRate = 95;
			range = 5;
			attackN = 1;
		}
		if (t == 1) {
			damage = 1.5;
			attack = 0;
			fireRate = 45;
			range = 7;
			attackN = 2;
		}
		if (t == 2) {
			damage = 1.2;
			attack = 0;
			fireRate = 20;
			range = 5;
			attackN = 1;
		}
		if (t == 3) {
			damage = 5;
			attack = 1;
			fireRate = 60;
			range = 3.5;
			attackN = 1;
		}
		if (t == 4) {
			damage = 2;
			attack = 1;
			fireRate = 20;
			range = 3;
			attackN = 1;
		}
		tRange = new Ellipse2D.Double(coords[0] - range * 40, coords[1] - range
				* 40, range * 80, range * 80);
	}

	// upgrade the tower
	public void upgrade() {
		towerLevel++;
		damage *= 1.3;
		fireRate *= .93;
		range *= 1.2;
		upgradeCost *= 1.5;
	}

	// attack all mobs in range
	public void doAttack(ArrayList<Mob> m, int wid, int[] d,
			ArrayList<Projectile> projs) {
		if (timer != 0) // do not start the attack timer until an attack has
						// already been achieved
			timer++;
		int[] c = new int[] { 0, 0 };
		int numA = 0;
		if (timer % fireRate > fireRate / 3 || m.isEmpty()) // attack animation
															// will only last
															// for 1/3 of the
															// fire rate
			animate = false;
		if (timer % fireRate == 0)
			for (int x = 0; x < m.size(); x++) {
				c = m.get(x).coords;
				Rectangle2D t = new Rectangle2D.Double(c[0] - wid / 2, c[1],
						40, 40); // mob hitboxes
				if (tRange.intersects(t) && numA < attackN) { // if the mobs are
																// within range,
																// and the
																// attack limit
																// has not been
																// reached yet,
																// attack!
					timer++;
					animate = true;
					if (attack == 0) { // if projectile attack
						numA++;
						projs.add(new Projectile(damage, m.get(x), new int[] {
								coords[0], coords[1] }, type - 1));// create a
																	// new
																	// projectile
																	// object
																	// and add
																	// it
					} else if (attack == 1) { // aoe attack all in range
						m.get(x).isHit(damage);
					}
					if (numA >= attackN) // stop the loop if attack limit is
											// reached
						return;
				}
			}
	}

	// return the coordinates of this tower
	public int[] getCoords() {
		return coords;
	}
}
