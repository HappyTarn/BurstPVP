package com.happytarn.burstPVP.util;

import java.util.Comparator;

import com.happytarn.burstPVP.PVPD;

public class PVPComparatorKD implements Comparator<PVPD>{

	@Override
	public int compare(PVPD o1, PVPD o2) {

		double no1 = o1.getKd();
		double no2 = o2.getKd();

		if(no1 < no2 ){
			return 1;
		}else if(no1 == no2){
			return 0;
		}else{
			return -1;
		}
	}

}
