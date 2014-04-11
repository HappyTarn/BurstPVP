package com.happytarn.burstPVP.util;

import java.util.Comparator;

import com.happytarn.burstPVP.PVPD;

public class PVPComparatorWin implements Comparator<PVPD>{

	@Override
	public int compare(PVPD o1, PVPD o2) {

		int no1 = o1.getWin();
		int no2 = o2.getWin();

		if(no1 < no2 ){
			return 1;
		}else if(no1 == no2){
			return 0;
		}else{
			return -1;
		}
	}

}
