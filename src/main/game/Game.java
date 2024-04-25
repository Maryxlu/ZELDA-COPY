package game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import entities.BulletShoot;
import entities.Enemy;
import entities.Entity;
import entities.Player;
import graficos.SpriteSheet;
import graficos.UI;
import world.World;


public class Game extends Canvas implements Runnable,KeyListener,MouseListener,MouseMotionListener{

	private static final long serialVersionUID = 1L;
	public static JFrame frame;
	private Thread thread;
	private boolean isRunning;
	public final static int WIDTH = 240;
	public final static int HEIGHT = 160;
	public final static int SCALE = 3; // Adjust to high to pixelate more
	
	private BufferedImage image;
	//fontes personalizadas
	//public InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("pixel.ttf");
	//public Font newFont;
	
	//Personalized URL's
	
	//lists
	public static List<Entity> entities;
	public static List<Enemy> enemies;
	public static List<BulletShoot> bullets;
	//Mouse motion variables
	public int mx,my;
	
	private int CUR_LEVEL = 1, MAX_LEVEL = 2;
	
	public static SpriteSheet spriteSheet;
	public static Player player;
	public static World world;
	public UI ui;
	
	//Manipular pixels
	public static int[] pixels;
	public BufferedImage lightmap;
	public int[] lightMapPixels;
	
	//game over aqui abaixo
	public static String gameState = "MENU";
	private boolean showMessageGameOver = false;
	private int framesGameOver=0;
	private boolean restartGame = false;

	public static Random rand;
	public Menu menu;
	
	public boolean saveGame = false;
	
	public static void main(String[] args) {
		Game game = new Game();
		game.start();
	}
	
	public Game() {
		//random USE AND ABUSE
		rand = new Random();
		//key listener
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		//Window
		this.setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE)); 
		initFrame();
		// Initialising objects
		ui = new UI();
		
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		try {
			lightmap = ImageIO.read(getClass().getResource("/lightmap.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		lightMapPixels = new int[lightmap.getWidth() * lightmap.getHeight()];
		lightmap.getRGB(0, 0, lightmap.getWidth(), lightmap.getHeight(), lightMapPixels, 0, lightmap.getWidth());
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		entities = new ArrayList<Entity>();
		enemies = new ArrayList<Enemy>();
		bullets = new ArrayList<BulletShoot>();
		spriteSheet = new SpriteSheet("/spritesheet.png");
		player = new Player(0, 0, 16, 16 , spriteSheet.getSprite(32, 0, 16, 16)); 
		entities.add(player);
		world = new World("/level1.png");
		/*
		try {
			newFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(25f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		menu = new Menu();

	}
	
	public void initFrame() {
		frame = new JFrame("Zelda Copy");
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public synchronized void start() {
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}
	
	public synchronized void stop() {
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("O jogo Parou inesperadamente"); // added byw Joao Carlos
			e.printStackTrace();
		}
	}
	
	public void tick() {
		//game logic
		if(gameState=="NORMAL") {			
			if(this.saveGame) {
				this.saveGame = false;
				String[] opt1 = {"level","life"};
				int[] opt2 = {this.CUR_LEVEL, (int) player.playerLife};
				Menu.saveGame(opt1, opt2, 10);
				System.out.println("Game saved successfully!");
			}
			this.restartGame = false;
			for(int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				e.tick();
			}
			for(int i = 0; i < bullets.size(); i++) {
				bullets.get(i).tick();
			}
			
			if(enemies.size() == 0) {
				CUR_LEVEL++;
				if(CUR_LEVEL > MAX_LEVEL) {
					CUR_LEVEL =1;
				}
				String newWorld = "level"+CUR_LEVEL+".png";
				World.restartGame(newWorld);
			}
			
		}else if(gameState == "GAME_OVER") {
			this.framesGameOver++;
			if(framesGameOver == 26) {
				framesGameOver =0;
				if(showMessageGameOver) 
					showMessageGameOver = false;
				else 
					showMessageGameOver =true;
			}
			
			if(restartGame) {
				this.restartGame=false;
				Game.gameState="NORMAL";
				CUR_LEVEL =1;
				String newWorld = "level"+CUR_LEVEL+".png";
				World.restartGame(newWorld);
			}
		}else if(gameState=="MENU") {
			player.updateCamera();
			menu.tick();
		}
	}
	
	//always if hexadecimal number color represented in Java with "0xff" prefix
	
	/* PIXEL manipulation class
	public void drawRectangleRxample(int xoff, int yoff) {
		for(int xx = 0; xx < 32;xx++) {
			for(int yy = 0 ; yy < 32; yy++) {
				int xOff = xx + xoff;
				int yOff = yy + yoff;
				if(xOff < 0 || yOff < 0 || xOff >= WIDTH || yOff>= HEIGHT)
					continue;
				pixels[xOff+(yOff*WIDTH)] = 0x00FF00;
			}
		}
	}
	*/

	public void applyLight() {
		/*
		for(int xx =0; xx < Game.WIDTH;xx++) {
			for(int yy =0; yy < Game.HEIGHT;yy++) {
				if(lightMapPixels[xx+(yy * Game.WIDTH)] == 0xffffffff) {
					pixels[xx+(yy*Game.WIDTH)] = 0;
				}
			}
		}*/
	}

	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics g = image.getGraphics();
		//down from here you change the background colour
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		// up from here you change the background colour
		
		/*Game Rendering  OwO */
		//Graphics2D g2 = (Graphics2D) g;
		world.render(g);

		for(int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.render(g);
		}
		for(int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(g);
		}
		
		applyLight();
		ui.render(g);
		/**/
		g.dispose(); // clean up memory
		g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, WIDTH*SCALE, HEIGHT*SCALE,null);
		g.setFont(new Font("arial", Font.BOLD,20));
		g.setColor(Color.white);
		g.drawString("Municao: " + player.ammo, 510, 40);
		//fontes  personalizadas
		/*
		g.setFont(newFont);
		g.drawString("Nova Fonte", 500, 100);
		*/

		if(gameState == "GAME_OVER") {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color(0,0,0,100));
			g2.fillRect(0, 0, WIDTH*SCALE, HEIGHT*SCALE);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("arial",Font.BOLD,30));
			g2.drawString("Game Over", (WIDTH*SCALE)/2-50, (HEIGHT*SCALE)/2 );
			if(showMessageGameOver) {
			g2.drawString("> Press Enter to restart <", (WIDTH*SCALE)/2-150, (HEIGHT*SCALE)/2 +100 );
			}
		}else if(gameState=="MENU") {
			menu.render(g);
			
		}
		//rotacionamento
		/*
		Graphics2D g2 = (Graphics2D) g;
		double angleMouse = Math.atan2(200+25 - my, 200+25 - mx);
		g2.rotate(angleMouse, 200+25, 200+25);
		g2.setColor(Color.RED);
		g2.fillRect(200, 200, 50, 50);
		*/
		bs.show();
	}
	
	
	
	@Override
	public void run() {
		//Game Loop
		requestFocus();
		long lastTime = System.nanoTime();
		double ammountOfTicks = 60.0;
		double ns = 1000000000/ ammountOfTicks;
		double delta = 0;
		//frames count for the FPS debugging
		int frames = 0;
		double timer = System.currentTimeMillis();
		//get time in milisecs
		while(isRunning) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if(delta >= 1) {
				tick();
				render();
				frames++;
				delta--;
			}
			// FPS DEBUG
			if(System.currentTimeMillis() - timer >= 1000) {
				System.out.println("FPS: "+ frames); // print out the frames quantity
				frames = 0;
				timer+=1000;
			}
			
		}
		stop();
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_Z) {
			player.jump = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_RIGHT||
				e.getKeyCode() == KeyEvent.VK_D) {
			player.right = true;
			if(gameState=="MENU") {
				menu.down=true;
			}
			
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT||
				e.getKeyCode() == KeyEvent.VK_A) {
			player.left = true;
			if(gameState=="MENU") {
				menu.up=true;
			}
		}
		
		
		if(e.getKeyCode() == KeyEvent.VK_UP||
				e.getKeyCode() == KeyEvent.VK_W) {
			player.up = true;
			if(gameState=="MENU") {
				menu.up=true;
			}
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN||
				e.getKeyCode() == KeyEvent.VK_S) {
			player.down = true;
			if(gameState=="MENU") {
				menu.down=true;
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_X) {
			player.shoot = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			this.restartGame = true;
			if(gameState=="MENU") {
				menu.enter = true;
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gameState = "MENU";
			
		}
		
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			if(Game.gameState == "NORMAL") {
				Menu.pause = true;
				this.saveGame = true;	
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_RIGHT||
				e.getKeyCode() == KeyEvent.VK_D) {
			player.right = false;
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT||
				e.getKeyCode() == KeyEvent.VK_A) {
			player.left = false;
		}
		
		
		if(e.getKeyCode() == KeyEvent.VK_UP||
				e.getKeyCode() == KeyEvent.VK_W) {
			player.up = false;
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN||
				e.getKeyCode() == KeyEvent.VK_S) {
			player.down = false;
		}
		
		
	}
	
	/*
	  MOUSES INPUT
	 */

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//here
		Game.player.mouseShoot = true;
		Game.player.mx = e.getX() / SCALE;
		Game.player.my = e.getY() / SCALE;
		//System.out.println(Game.player.mx);
		//System.out.println(Game.player.my);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/*
	MOUSE MOTION LISTENER
	*/
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		this.mx = e.getX();
		this.my = e.getY();
	}
	
}
