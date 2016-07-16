package com.example.bluetooth.bean;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

@Table(name = "xueya")
public class HistoryXueYa {

	private double hxueya;
	private double lxueya;
	private String id;
	private String time;
	private double mailv;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getHxueya() {
		return hxueya;
	}

	public void setHxueya(double hxueya) {
		this.hxueya = hxueya;
	}

	public double getLxueya() {
		return lxueya;
	}

	public void setLxueya(double lxueya) {
		this.lxueya = lxueya;
	}

	public double getMailv() {
		return mailv;
	}

	public void setMailv(double mailv) {
		this.mailv = mailv;
	}

}
