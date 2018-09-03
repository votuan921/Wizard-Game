import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.util.Random;

import javax.swing.JFrame;

public class Game extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	private boolean isRunning = false;
	private Thread thread;
	private Handler handler;
	private Camera camera;
	private SpriteSheet ss;
	private BufferedImage level = null;
	private BufferedImage sprite_sheet = null;
	private BufferedImage floor = null;
	private Boss boss = null;
	private Enemy mini = null;
	private JFrame frame;
	public int wizardAmmo = 100;
	public int width = 1000;
	public int height = 563;
	public int wizardHp = 100;
	public int score = 0;
	public int enemyAmount = 0;
	public int time = 0;
	public int numberStage = 4;
	public boolean isFinalStage = true;
	public boolean isVictory = false;
	public int stage = 0;
	public Game(int stage) {
		//if (isStarted) 
		//new Window(1000, 563, "Wizard Game", this);
		frame = new JFrame("Wizard Game Stage" + stage);
		
		frame.setPreferredSize(new Dimension(width, height));
		frame.setMaximumSize(new Dimension(width, height));
		frame.setMinimumSize(new Dimension(width, height));
		
		frame.add(this);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		this.stage = stage;
		start();
		handler = new Handler();
		camera = new Camera(0, 0);
		this.addKeyListener(new KeyInput(handler));
		BufferedImageLoader loader = new BufferedImageLoader();
		//Random r = new Random();
		level = loader.loadImage("/wizard_level_" + stage + ".png");
		sprite_sheet = loader.loadImage("/sprite_sheet.png");
		ss = new SpriteSheet(sprite_sheet);
		floor = ss.grabImage(4, 2, 32, 32);
		this.addMouseListener(new MouseInput(handler, camera, this, ss));
		loadLevel(level);
		//System.out.println(enemyAmount);
		
	}
	
	public void start() {
		isRunning = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		isRunning = false;
		//thread.stop();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while(isRunning) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1) {
				tick();
				delta--;
			}
			render();
			frames++;
			if(System.currentTimeMillis() - timer > 500) {
				timer += 500;
				time++;
				frames = 0;
				if (boss != null) {
					//System.out.println("aa");
					//System.out.println(boss.getX());
					mini = new Enemy(boss.getX(), boss.getY(), ID.Enemy, handler, ss, this);
					handler.addObject(mini);
					enemyAmount++;
				}
			}
			
			if(wizardHp <0 || time >= 61) {
				isRunning = false;
			}
			if(score == enemyAmount && score > 0) {
				isRunning = false;
				isVictory = true;
			}
			if(boss != null)
				if(boss.hp <= 0) {
					System.out.println("AAA");
					isRunning = false;
					isVictory = true;
				}
		}
		//System.out.println(isVictory);
		render();
		if (stage < numberStage && isVictory) {
			frame.setVisible(false);
			new Game(stage + 1);
		}
		stop();	
		
	}
	public void tick() {	
		for(int i = 0; i < handler.object.size(); i++) {
			if(handler.object.get(i).getId() == ID.Player) {
				camera.tick(handler.object.get(i));
			}
		}
		handler.tick();
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();
		Graphics2D g2d = (Graphics2D) g;
		///////////////////////////////////
		
		g2d.translate(-camera.getX(), -camera.getY());
		
		for(int xx = 0; xx < 30*72; xx+=32) {
			for(int yy = 0; yy < 30*72; yy+=32) {
				g.drawImage(floor, xx, yy, null);
			}
		}
		
		handler.render(g);
		
		g2d.translate(camera.getX(), camera.getY());
		
		g.setColor(Color.gray);
		g.fillRect(5, 5, 200, 32);
		g.setColor(Color.green);
		g.fillRect(5, 5, wizardHp*2, 32);
		g.setColor(Color.black);
		g.drawRect(5, 5, 200, 32);
		if(boss != null) {
			g.setColor(Color.gray);
			g.fillRect(800, 5, 200, 32);
			g.setColor(Color.red);
			g.fillRect(800, 5, (int)(boss.hp*0.1), 32);
			g.setColor(Color.black);
			g.drawRect(800, 5, 200, 32);
		}
		
		g.setColor(Color.white);
		g.drawString("Ammo: " + wizardAmmo, 5, 50);
		g.drawString("Score: " + score, 5, 70);
		g.drawString("Time: " + (int)(time/2), 5, 90);
		int temp = this.enemyAmount - this.score;
		g.drawString("Enemy: " + temp, 5, 110);
		if(wizardHp < 0 || time >= 60) {
			g.setColor(Color.red);
			g.setFont(new Font("Double Feature", Font.PLAIN, 30));
			g.drawString("GAME OVER", 410, 261);
		}
		if(wizardAmmo == 0 && enemyAmount > 0) {
			g.setColor(Color.red);
			g.setFont(new Font("Double Feature", Font.PLAIN, 30));
			g.drawString("Out of Bullet", 410, 261);
		}
		//System.out.println(isVictory);
		if(boss != null) {
			if(boss.hp <= 0) {
				//System.out.println("AAAAA");
				g.setColor(Color.yellow);
				g.setFont(new Font("FoughtKnight Victory", Font.PLAIN, 30));
				g.drawString("Victory", 410, 261);
				g.setColor(Color.green);
				g.setFont(new Font("FoughtKnight Victory", Font.PLAIN, 20));
				g.drawString("Score: " + (100 + score), 400, 280);
			}
		}
		//////////////////////////////////
		g.dispose();
		bs.show();	
		
	}
	
	//loading the level
	private void loadLevel(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		
		for(int xx = 0; xx < w; xx++) {
			for(int yy = 0; yy < h; yy++) {
				int pixel = image.getRGB(xx, yy);
				int red = (pixel >> 16) & 0xff;
				int green = (pixel >> 8) & 0xff;
				int blue = (pixel) & 0xff;
				
				if(red == 255 && green == 0 && blue == 0)
					handler.addObject(new Block(xx*32, yy*32, ID.Block, ss));
				
				if(blue == 255 && green == 0 && red == 0)
					handler.addObject(new Wizard(xx*32, yy*32, ID.Player, handler, this, ss));
				
				if(green == 255 && blue == 0 && red == 0) {
					handler.addObject(new Enemy(xx*32, yy*32, ID.Enemy, handler, ss, this));
					enemyAmount++;
				}	
				if(red == 255 && blue == 255 && green == 0) {
					boss = new Boss(xx*32, yy*32, ID.Boss, handler, ss, this);
					handler.addObject(boss);
				}
				if(green == 255 && blue == 255 && red == 0)
					handler.addObject(new Crate(xx*32, yy*32, ID.Crate, ss));
			}
		}
	}
	
	
	
	public static void main(String args[]) {
		new Game(4);
	}
	
}
