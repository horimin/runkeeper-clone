package jp.horimislime.sample.runkeeper.client;


public class Gps{
	long time;
	double lat;
	double lng;
	double acc;
	float speed;

	
	public double getAcc() {return acc;	}
	public double getLat() {return lat;	}
	public double getLng() {return lng;	}
	public long getTime()  {return time;}
	public float getSpeed() {return speed;}
	
	
	public void setAcc(double acc){this.acc=acc;}
	public void setLat(double lat){this.lat=lat;}
	public void setLng(double lng){this.lng=lng;}
	public void setTime(long time){this.time=time;}
	public void setSpeed(float speed){this.speed=speed;}
}