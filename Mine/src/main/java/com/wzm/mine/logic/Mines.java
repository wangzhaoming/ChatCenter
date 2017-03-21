package com.wzm.mine.logic;

import java.util.Random;

public class Mines {
	private int [][]mines;
	private int column;
	private int row;
	
	
	public int[][] getMines() {
		return mines;
	}

	public Mines(int column,int row,int mineNum) {
		this.column=column;
		this.row=row;
		mines=new int[column][row];
		Random r=new Random();
		
		int num=0;
		while(true){
			int x=r.nextInt(column);
			int y=r.nextInt(row);
			if(mines[x][y]==-1){
				continue;
			}
			mines[x][y]=-1;
			if(++num==mineNum){
				break;
			}
		}
		
		for(int i=0;i<column;i++){
			for(int j=0;j<row;j++){
				mines[i][j]=getMines(i,j);
			}
		}
	}
	
	private int getMines(int x,int y){
		if(mines[x][y]!=-1){
			return getMine(x-1,y-1)+getMine(x-1,y)+getMine(x-1,y+1)+getMine(x,y-1)+getMine(x,y+1)+getMine(x+1,y-1)+getMine(x+1,y)+getMine(x+1,y+1);
		}
		return -1;
	}
	
	private int getMine(int x,int y){
		if(x>=0&&x<column&&y>=0&&y<row){
			if(mines[x][y]==-1){
				return 1;
			}
		}
		return 0;
	}
}
