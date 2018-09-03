import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.Math;

public class Bullet extends GameObject {

	private Handler handler;
	public float speed = 10;
	private float d;
	public Bullet(int x, int y, ID id, Handler handler, int mx, int my, SpriteSheet ss) {
		super(x, y, id, ss);
		this.handler = handler;
	    d = (float)Math.sqrt((mx - x)*(mx - x) + (my - y)*(my - y)); 
		velX = speed * (mx - x) / d;
		velY = speed * (my - y) / d;
	}

	public void tick() {
		x += velX;
		y += velY;
		
		for(int i = 0; i < handler.object.size(); i++){
			GameObject tempObject = handler.object.get(i);
			
			if(tempObject.getId() == ID.Block){
				if(getBounds().intersects(tempObject.getBounds())){
					handler.removeObject(this);
				}
			}
		}
		
	}

	public void render(Graphics g) {
		g.setColor(Color.green);
		g.fillOval(x, y, 8, 8);
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, 8, 8);
	}

}
