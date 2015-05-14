public class Projectile {
	public double damage; // how much damage this projectile will do
	public int velocity = 3; // pixels per tic (pixels per 1/30 of a second)
	private Mob target; // target mob
	public int[] targetLoc, currentLoc; // target and current coordinates
	public boolean exist = false; // does this projectile exist?
	public int type; // 0 for arrow, 1 for star

	// constructor, initialize values
	public Projectile(double damage2, Mob t, int[] c, int ty) {
		damage = damage2;
		target = t;
		targetLoc = t.coords;
		currentLoc = c;
		exist = true;
		type = ty;
	}

	// move this projectile towards the target for x=velocity pixels
	public void move() {
		targetLoc = target.coords; // account for the moving of the mob
		int d = dist(currentLoc, targetLoc); //distance between the two locations

		// hit and then damage
		if (d <= velocity) {
			target.isHit(damage);
			exist = false;
		}

		// movement
		else {
			double t = 1.0 * velocity / d;
			currentLoc[0] += (int) (t * (targetLoc[0] - currentLoc[0]));
			currentLoc[1] += (int) (t * (targetLoc[1] - currentLoc[1]));
		}
	}

	//return the distance between two sets of coordinates
	public int dist(int[] a, int[] b) {
		return (int) Math.sqrt(Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]));
	}
}
