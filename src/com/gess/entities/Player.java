package com.gess.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.util.ArrayList;

import com.gess.graficos.SpriteSheet;
import com.gess.main.Game;
import com.gess.world.Camera;
import com.gess.world.World;

public class Player extends Entity{

	 public boolean right,left,up,down;
	 private int right_Dir = 0, left_Dir = 1;
	 private int dir = right_Dir;
	 
	 public double speed = 1.4;
	
	 //animação
	 private int frames = 0, maxFrames = 5, index = 0, maxIndex = 3;
	 private boolean moved = false;
	 private BufferedImage[] rightPlayer;
	 private BufferedImage[] leftPlayer;
	 private BufferedImage playerDamage;
	 
	 //mouse x and y
	 public int mx,my;
	 
	 //dano
	 public boolean isDamaged = false;
	 public int damageFrame=0;
	 public double playerLife = 100, playerMaxLife = 100;
	 //arma
	 public int ammo = 0;
	 private boolean hasGun = false;
	 public boolean shoot = false, mouseShoot = false;
	 
	 //fake jump
	 public boolean jump = false;
	 public boolean isJumping = false;
	 public static int z = 0;
	 public int jumpFrames = 50, jumpCur = 0;
	 public boolean jumpUp = false, jumpDown = false;
	 public int jumpSpd = 2;
	 
	 
	public Player(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y,width, height, sprite);
		
		rightPlayer = new BufferedImage[4];
		leftPlayer = new BufferedImage[4];
		
		playerDamage = Game.spriteSheet.getSprite(0, 16, 16, 16);
		
		for(int i = 0; i < 4; i++) {
		rightPlayer[i] = Game.spriteSheet.getSprite(32+(i*16), 0, 16, 16);
		}
		for(int i = 0; i < 4; i++) {
		leftPlayer[i] = Game.spriteSheet.getSprite(32+(i*16), 16, 16, 16);
		}

	}
	
	public void tick() {
		
		if(jump) {
			if(isJumping == false) {
				jump = false;
				isJumping = true;
				jumpUp = true;
			}
		}
		
		if(isJumping) {
				if(jumpUp) {
					jumpCur+=jumpSpd;
				} else  if(jumpDown) {
					jumpCur-=jumpSpd;
					if(jumpCur <= 0) {
						isJumping =false;
						jumpDown = false;
						jumpUp = false;
					}
				}
				z = jumpCur;
				if(jumpCur >= jumpFrames) {
					jumpUp = false;
					jumpDown = true;
					//System.out.println("Cehgou a altura máxima!");
				}
		}
		
		moved  = false;
		if(right && World.isFree((int)(x+speed),this.getY(),z)) {
			moved = true;
			dir = right_Dir;
			x+=speed;
		}
		else if(left && World.isFree((int)(x-speed),this.getY(),z)) {
			moved = true;
			dir = left_Dir;
			x-=speed;
		}
		if(up && World.isFree(this.getX(),(int)(y-speed),z)) {
			moved = true;
			y-=speed;
		}
		else if(down && World.isFree(this.getX(),(int)(y+speed),z)) {
			moved = true;
			y+=speed;
		}
		
		if(moved) {
			frames++;
			if(frames == maxFrames) {
				frames=0;
				index++;
				if(index > maxIndex) {
					index=0;
				}
			}
		}
		checkCollisionLifePack();
		checkCollisionAmmo();
		checkCollisionGun();
		
		if(isDamaged) {
			this.damageFrame++;
			if(this.damageFrame == 15) {
				damageFrame = 0;
				this.isDamaged = false;
			}
		}
		
		if(shoot) {
			shoot = false;
			if(hasGun && ammo > 0) {
			ammo--;
			
			//System.out.println("Shooting!");
			int dx = 0;
			int px = 0;
			int py = 9;
			if(dir == right_Dir) {
				px = 10;
				dx = 1;
			} else {
				px = -1;
				dx = -1;
			}
			BulletShoot bullet = new BulletShoot(this.getX()+px,this.getY()+ py,3,3,null,dx,0);
			Game.bullets.add(bullet);
			}
		}
		
		//MOUSE SHOOOT BOOM BOOM
		if(mouseShoot) {
			//System.out.println("mouse Shooted!");
			mouseShoot = false;
			//System.out.println(angle);
			if(hasGun && ammo > 0) {
			ammo--;
			
			//System.out.println("Shooting!");
			int px = 0,py=9;
			double angle = 0;
			if(dir == right_Dir) {
				px = 10;
				angle = Math.atan2( my - (this.getY()+py - Camera.y),mx - (this.getX()+px - Camera.x));
			} else {
				px = -1;
				angle = Math.atan2( my - (this.getY()+py - Camera.y),mx - (this.getX()+px - Camera.x));
			}
			double dx = Math.cos(angle);
			double dy = Math.sin(angle);

			BulletShoot bullet = new BulletShoot(this.getX()+px,this.getY()+ py,3,3,null,dx,dy);
			Game.bullets.add(bullet);
			
			}
			
		}
		
		if(playerLife<=0) {
			//Game over!
			playerLife=0;
			Game.gameState = "GAME_OVER";
			
		}
		
		updateCamera();

	}
	
	public void updateCamera() {
		Camera.x = Camera.clamp(this.getX() - (Game.WIDTH/2),0,World.WIDTH*16 - Game.WIDTH);
		Camera.y = Camera.clamp(this.getY() - (Game.HEIGHT/2),0,World.HEIGHT*16 - Game.HEIGHT);
	}
	
	
	public void checkCollisionGun() {
		for(int i=0; i < Game.entities.size();i++) {
			Entity actual = Game.entities.get(i);
			if(actual instanceof Weapon) {
				if(Entity.isColliding(this, actual)) {
					hasGun=true; // count ammunition
					//System.out.println("Contagem de balas: " + ammo);
					//System.out.println("Pegou a arma");
					Game.entities.remove(i);
					return;
				}
			}
		}

	}
	
	public void checkCollisionAmmo() {
		for(int i=0; i < Game.entities.size();i++) {
			Entity actual = Game.entities.get(i);
			if(actual instanceof Bullet) {
				if(Entity.isColliding(this, actual)) {
					ammo+=10; // count ammunition
					//System.out.println("Contagem de balas: " + ammo);
					Game.entities.remove(i);
					return;
				}
			}
		}
	}
	
	
	public void checkCollisionLifePack() {
		for(int i=0; i < Game.entities.size();i++) {
			Entity actual = Game.entities.get(i);
			if(actual instanceof Lifepack) {
				if(Entity.isColliding(this, actual)) {
					playerLife+=15;
					if(playerLife >=100) {
						playerLife =100;
					}
					Game.entities.remove(i);
					return;
				}
			}
		}
	}
	
	

	public void render(Graphics g) {
		if(!isDamaged) {
		if(dir==right_Dir) {
			g.drawImage(rightPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
			if(hasGun) {
				//gun to right
				g.drawImage(Entity.WEAPON_RIGHT, this.getX()+1 - Camera.x, this.getY()+1 - Camera.y - z, null);
			}
		}else if(dir == left_Dir) {
			g.drawImage(leftPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
			if(hasGun) {
				//gun to left
				g.drawImage(Entity.WEAPON_LEFT, this.getX()-1 - Camera.x, this.getY()+1 - Camera.y - z, null);

			}
		}
		//g.setColor(Color.BLUE);
		//g.fillRect(this.getX() - Camera.x, this.getY() - Camera.y, this.getWidth(), this.getHeight());
		}else {
			if(dir==right_Dir) {
				g.drawImage(rightPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
				if(hasGun) {
					//gun to right
					g.drawImage(Entity.WEAPON_DAMAGE_RIGHT, this.getX()+1 - Camera.x, this.getY()+1 - Camera.y - z, null);
					g.drawImage(playerDamage, this.getX() - Camera.x, this.getY() - Camera.y - z, null);

				}
			}else if(dir == left_Dir) {
				g.drawImage(leftPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y- z, null);
				if(hasGun) {
					//gun to left
					g.drawImage(Entity.WEAPON_DAMAGE_LEFT, this.getX()-1 - Camera.x, this.getY()+1 - Camera.y - z, null);
					g.drawImage(playerDamage, this.getX() - Camera.x, this.getY() - Camera.y - z, null);

				}
			}
		}
	}
}
