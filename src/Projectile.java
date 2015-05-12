public class Projectile {
	public double damage;
	public int velocity = 3; //pixels per tic (pixels per 1/30 of a second)
	private Mob target;
	public int[] targetLoc, currentLoc;
	public boolean exist = false;
	public int type; //0 for arrow, 1 for star

	public Projectile(double damage2, Mob t, int[] c, int ty) {
		damage = damage2;
		target = t;
		targetLoc = t.coords;
		currentLoc = c;
		exist = true;
		type = ty;
	}
	
	public void move(){
		targetLoc = target.coords;
		int d = dist(currentLoc, targetLoc);
		
		//hit and then damage
		if(d <= velocity){
			target.isHit(damage);
			exist = false;
		}
		
		//movement
		else{
			double t = 1.0*velocity/d;
			currentLoc[0]+=(int)(t*(targetLoc[0]-currentLoc[0]));
			currentLoc[1]+=(int)(t*(targetLoc[1]-currentLoc[1]));
		}
	}
	
	public int dist(int[] a, int[] b) {
		return (int) Math.sqrt(Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]));
	}
}
