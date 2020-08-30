package com.gess.entities;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.gess.main.Game;
import com.gess.main.Sound;
import com.gess.world.Astart;
import com.gess.world.Camera;
import com.gess.world.Vector2i;
import com.gess.world.World;

public class Enemy extends Entity{ // duplicate and rename this to add more different enemies

	public double speed = 0.5;
	
	private int frames = 0, maxFrames = 20, index = 0, maxIndex = 1;
	private BufferedImage[] sprites;
	
	// Damage Stuff
	private boolean isDamaged = false;
	private int damageFrames = 10, damageCurrent = 0;
	private int enemyLife = 100;
	
	public Enemy(int x,int y, int width,int height, BufferedImage sprite) {
		super(x,y,width,height,sprite);
		
		sprites = new BufferedImage[2];
		sprites[0] = Game.spriteSheet.getSprite(7*16, 16, World.TILE_SIZE, World.TILE_SIZE);
		sprites[1] = Game.spriteSheet.getSprite(8*16, 16, World.TILE_SIZE, World.TILE_SIZE);

	}
	
	public void tick() {
		/*
		if((this.calculateDistance(this.getX(), this.getY(), Game.player.getX(), Game.player.getY()) < 70)) {
			if(!isCollidingWithPlayer()) {
			//if(Game.rand.nextInt(100 ) < 50) { // apenas usar para randomizar as experiencias 
			if((int) x < Game.player.getX() && World.isFree((int)(x+speed),this.getY(),z)
					&& !isColliding((int)(x+speed),this.getY())) {
				x+=speed;
			} else if((int)x > Game.player.getX() && World.isFree((int)(x-speed),this.getY(),z)
					&& !isColliding((int)(x-speed),this.getY())) {
				x-=speed;
			}
			
			if((int)y > Game.player.getY() && World.isFree(this.getX(),(int)(y-speed),z)
					&& !isColliding(this.getX(),(int)(y-speed))) {
				y-=speed;
			} else if((int)y < Game.player.getY() && World.isFree(this.getX(),(int)(y+speed),z)
					&& !isColliding(this.getX(),(int)(y+speed))) {
				y+=speed;
			}
			} else {
				//we are colliding
				if(Game.rand.nextInt(100) < 10) {
					Sound.hurtSFX.play();
					Game.player.playerLife -= Game.rand.nextInt(5);
					Game.player.isDamaged = true;
					//System.out.println("Life: " + Game.player.playerLife);
					if(Game.player.playerLife <= 0) {
						//System.out.println("Você perdeu o jogo"); 
						//System.exit(1);
						
					}
				}
			}
		}else {
			//Inactive enemy
		}
		*/
		maskx = 5;
		masky = 5;
		mwidth =8;
		mheight = 8;
		if(!isCollidingWithPlayer()) {
			if(path == null || path.size() == 0) {
				Vector2i start = new Vector2i((int)(x/16),(int)(y/16));
				Vector2i end = new Vector2i((int)(Game.player.x/16),(int)(Game.player.y/16));
				path = Astart.findPath(Game.world, start, end);
			}
		}else {
			if(Game.rand.nextInt(100) < 10) {
				Sound.hurtSFX.play();
				Game.player.playerLife -= Game.rand.nextInt(5);
				Game.player.isDamaged = true;
				//System.out.println("Life: " + Game.player.playerLife);
				if(Game.player.playerLife <= 0) {
					//System.out.println("Você perdeu o jogo"); 
					//System.exit(1);	
				}
			}
		}
		
		if(Game.rand.nextInt(100) < 55) { // Enemy Speed
			followPath(path);	
		}
		
		if(Game.rand.nextInt(100) < 5) {
				Vector2i start = new Vector2i((int)(x/16),(int)(y/16));
				Vector2i end = new Vector2i((int)(Game.player.x/16),(int)(Game.player.y/16));
				path = Astart.findPath(Game.world, start, end);
		}
		
		frames++;
		if(frames == maxFrames) {
			frames=0;
			index++;
			if(index > maxIndex) {
				index=0;
			}
		}
	
		collidingBullet();
				
		if(enemyLife <= 10) {
			Game.enemies.remove(this);
			destroySelf();
			return;
		}	
	
		if(isDamaged) {
			damageCurrent++;
			if(damageCurrent >= damageFrames) {
				damageCurrent = 0;
				isDamaged = false;
				return;
			}
		}
			
	}	
	//}
	
	public void destroySelf() {
		Game.entities.remove(this);
	}
	
	public void collidingBullet() {
		for(int i = 0;i<Game.bullets.size();i++) {
			Entity e = Game.bullets.get(i);
				if(Entity.isColliding(this, e)) {
					isDamaged = true;
					if(Game.rand.nextInt(100) < 70) {
					enemyLife-= (Game.rand.nextInt(30 -10) + 10);
					}
					Game.bullets.remove(i);
					//System.out.println("Colision!");
					return;
			}
		}
	}
	
	public boolean isCollidingWithPlayer() {
		Rectangle enemyCurrent = new Rectangle(this.getX() + maskx, this.getY() + masky, mwidth, mheight);
		Rectangle player = new Rectangle(Game.player.getX(), Game.player.getY(),16,16);
		
		
		return enemyCurrent.intersects(player);
	}
	
	public void render(Graphics g) {
		super.render(g);
		if(!isDamaged) {
		g.drawImage(sprites[index], this.getX() - Camera.x, this.getY() - Camera.y, null);
		//g.setColor(Color.blue);
		//g.fillRect(this.getX() + maskx - Camera.x, this.getY() + masky - Camera.y, maskw, maskh);
		}else {
			g.drawImage(Entity.ENEMY_DAGAME, this.getX() - Camera.x, this.getY() - Camera.y, null);
		}
	}
}
