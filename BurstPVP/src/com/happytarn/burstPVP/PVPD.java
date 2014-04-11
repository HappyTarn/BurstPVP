package com.happytarn.burstPVP;

import java.math.BigDecimal;

public class PVPD {

	private String name;
	private int win;
	private int lose;
	private double kd;

	public PVPD(String namme, String win, String lose) {
		this.name = namme;
		this.win = Integer.parseInt(win);
		this.lose = Integer.parseInt(lose);

		if(this.lose == 0){
			this.kd = new BigDecimal(win).doubleValue();
		}else{
			this.kd = new BigDecimal(win).divide(new BigDecimal(lose),2,BigDecimal.ROUND_HALF_UP).setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWin() {
		return win;
	}

	public void setWin(int win) {
		this.win = win;
	}

	public int getLose() {
		return lose;
	}

	public void setLose(int lose) {
		this.lose = lose;
	}

	public double getKd() {
		return kd;
	}

	public void setKd(double kd) {
		this.kd = kd;
	}


//	public static void main (String ...args){
//		PVPD pvp = new PVPD("test", 3, 0);
//		System.out.println(pvp.getKd());
//	}


}
