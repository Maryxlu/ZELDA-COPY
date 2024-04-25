package game;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import world.World;

public class Menu {

	public String[] options = {"New Game","Load Game","Sair"};
	
	public int currentOption = 0;
	public int maxOption = options.length - 1;
	
	public boolean up,down,enter;
	
	public static boolean pause = false;
	
	public static boolean saveExists = false;
	public static boolean saveGame = false;
	
	public void tick() {
		File file = new File("save.txt");
		if(file.exists()) {
			saveExists = true;
		}else {
			saveExists = false;
		}
		if(up) {
			up=false;
			currentOption--;
			if(currentOption <0) {
				currentOption = maxOption;
			}
		}

		if(down) {
			down=false;
			currentOption++;
			if(currentOption > maxOption) {
				currentOption = 0;
			}
		}
		if(enter) {
			// MUSIC SUM MUSIC UNDER HERE!! OwO
			//Sound.music.play();
			enter = false;
			if(options[currentOption] == "New Game") {
				Game.gameState = "NORMAL";
				enter = false;
				file = new File("save.txt");
				file.delete();
			}else if(options[currentOption] == "Load Game") {
				file = new File("save.txt");
				if(file.exists()) {
					String saver = loadGame(10);
					applySave(saver);
				}
			}else if(options[currentOption] == "Sair") {
				System.exit(1);
			}
		}
	}
	
	public static void applySave(String str) {
		String[] spl1 = str.split("/");
		for(int i = 0; i < spl1.length;i++) {
			String[] spl2 = spl1[i].split(":");
			switch(spl2[0]) {
			case "level":
				World.restartGame("level"+spl2[1]+".png");
				Game.gameState = "NORMAL";
				pause = false;
				break;
			case "vida":
				Game.player.playerLife = Integer.parseInt(spl2[1]);
				break;
			}
		}
	}
	
	public static String loadGame(int encode) {
		String line = "";
		File file = new File("save.txt");
		if(file.exists()) {
			try {
				String singleLine = null;
				BufferedReader reader = new BufferedReader(new FileReader("save.txt"));
				try {
					while((singleLine = reader.readLine()) != null) {
						String[] trans = singleLine.split(":");
						char[] val = trans[1].toCharArray();
						trans[1] = "";
						for(int i = 0; i < val.length;i++) {
							val[i]-=encode;
							trans[1]+=val[i];
						}
						line+=trans[0];
						line+=":";
						line+=trans[1];
						line+="/";
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
			}catch(FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return line;
	}
	
	public static void saveGame(String[] val1, int[] val2, int encode) {
		BufferedWriter write = null;
		try {
			write = new BufferedWriter(new FileWriter("save.txt"));
		}catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < val1.length;i++) {
			String current =val1[i];
			current += ":";
			char[] value = Integer.toString(val2[i]).toCharArray();
			for(int n = 0; n < value.length; n++) {
				value[n]+=encode;
				current+=value[n];
			}
			try {
				write.write(current);
				if(i < val1.length - 1) {
					write.newLine();
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		try {
			write.flush();
			write.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color(0,0,0,205));
		g.fillRect(0, 0, Game.WIDTH*Game.SCALE, Game.HEIGHT*Game.SCALE);
		g.setColor(Color.BLUE);	
		g.setFont(new Font("arial", Font.BOLD, 30));
		g.drawString(">|ZELDA COPY|<", (Game.WIDTH*Game.SCALE)/2-110, (Game.HEIGHT*Game.SCALE)/2-125);
		//Op�oes
		g.setColor(Color.MAGENTA);
		/*
		if(!pause) {
			g.drawString("New Game",(Game.WIDTH*Game.SCALE)/2-350, (Game.HEIGHT*Game.SCALE)/2-200);
			g.drawString("Load Map",(Game.WIDTH*Game.SCALE)/2-150, (Game.HEIGHT*Game.SCALE)/2-200);
			g.drawString("Sair",(Game.WIDTH*Game.SCALE)/2+50, (Game.HEIGHT*Game.SCALE)/2-200);
			if(options[currentOption]== "New Game") {
				g.setColor(Color.white);
				g.drawString("New Game",(Game.WIDTH*Game.SCALE)/2-350, (Game.HEIGHT*Game.SCALE)/2-200);
			}else if(options[currentOption] == "Load Game") {
				g.setColor(Color.white);
				g.drawString("Load Map",(Game.WIDTH*Game.SCALE)/2-150, (Game.HEIGHT*Game.SCALE)/2-200);
			}else if(options[currentOption] == "Sair") {
				g.setColor(Color.white);
				g.drawString("Sair",(Game.WIDTH*Game.SCALE)/2+50, (Game.HEIGHT*Game.SCALE)/2-200);
			}
		}else if(pause) {
			g.drawString("Continue",(Game.WIDTH*Game.SCALE)/2-350, (Game.HEIGHT*Game.SCALE)/2-200);
			g.drawString("Load Map",(Game.WIDTH*Game.SCALE)/2-150, (Game.HEIGHT*Game.SCALE)/2-200);
			g.drawString("Exit",(Game.WIDTH*Game.SCALE)/2+50, (Game.HEIGHT*Game.SCALE)/2-200);
			if(options[currentOption]== "New Game") {
				g.setColor(Color.white);
				g.drawString("Continue",(Game.WIDTH*Game.SCALE)/2-350, (Game.HEIGHT*Game.SCALE)/2-200);
			}else if(options[currentOption] == "Load Game") {
				g.setColor(Color.white);
				g.drawString("Load Map",(Game.WIDTH*Game.SCALE)/2-150, (Game.HEIGHT*Game.SCALE)/2-200);
			}else if(options[currentOption] == "Sair") {
				g.setColor(Color.white);
				g.drawString("Exit",(Game.WIDTH*Game.SCALE)/2+50, (Game.HEIGHT*Game.SCALE)/2-200);
			}
			*/
			if(pause == false)
				g.drawString("New Game", (Game.WIDTH*Game.SCALE) / 2 - 65, 160);
			else
				g.drawString("Continue", (Game.WIDTH*Game.SCALE) / 2 - 65, 160);
			g.drawString("Load Map", (Game.WIDTH*Game.SCALE) / 2 - 65, 200);
			g.drawString("Sair", (Game.WIDTH*Game.SCALE) / 2 - 65, 240);
			
			if(options[currentOption] == "New Game") {
				g.drawString(">", (Game.WIDTH*Game.SCALE) / 2 - 90, 160);
			}else if(options[currentOption] == "Load Game") {
				g.drawString(">", (Game.WIDTH*Game.SCALE) / 2 - 90, 200);
			}else if(options[currentOption] == "Sair") {
				g.drawString(">", (Game.WIDTH*Game.SCALE) / 2 - 90, 240);
			}
		}
	}