package ref;
	
import org.junit.Test;
	
public class Data {
	
	//生成数据
	public int[][] tw1;
	public int[][] tw2;
	public double[][] driveTime;
	public int[] loadTime;
	public double[] packageTime;
	public int IE;
	public int OE;
	public int IF;
	public int OF;
	public int portNum;
	public int stockNum;
	public int[] truckNum;
	public int loadCar;
	public int taskNum;
	public int tMax;
	
	public Data(int x){
		if(x==1){
			int[][] tw11 = { { 225, 97, 221, 205, 75, 82 },
					{ 310, 237, 339, 315, 138, 120 } };
			this.tw1 = tw11;
			int[][] tw22 = { { 226, 107, 107 }, 
					{ 446, 241, 390 } };
			this.tw2 = tw22;
			double[][] driveTime1 = { 
					{ 0, 47, 35, 54, 42 }, 
					{ 47, 0, 13, 98, 55 },
					{ 35, 13, 0, 88, 43 },
					{ 54, 98, 88, 0, 89 },
					{ 42, 55, 43, 89, 0 } };
			this.driveTime = driveTime1;
			int[] loadTime = { 13, 16, 13 };
			this.loadTime = loadTime;
			this.IE = 1;
			this.OE = 2;
			this.IF = 1;
			this.OF = 2;
			this.portNum = 1;
			this.stockNum = 1;
			int[] truckNum = { 2 };
			this.truckNum = truckNum;
			this.loadCar = 5;
			this.taskNum = 6;
			this.tMax = 480;
			
		}
		if(x==501){
			double[][] driveTime1 = { 
					{ 0, 47, 35, 54, 42 }, 
					{ 47, 0, 13, 98, 55 },
					{ 35, 13, 0, 88, 43 },
					{ 54, 98, 88, 0, 89 },
					{ 42, 55, 43, 89, 0 } };
			this.driveTime = driveTime1;
			int[] loadTime = { 13, 16, 13 ,20};
			this.loadTime = loadTime;
			this.IE = 0;
			this.OE = 0;
			this.IF = 2;
			this.OF = 2;
			this.portNum = 1;
			this.stockNum = 1;
			int[] truckNum = { 4 };
			this.truckNum = truckNum;
			this.loadCar = 5;
			this.taskNum = 4;
			this.tMax = 480;
			
		}
	}
	public int[][] getTw1() {
		return tw1;
	}
	public void setTw1(int[][] tw1) {
		this.tw1 = tw1;
	}
	public int[][] getTw2() {
		return tw2;
	}
	public void setTw2(int[][] tw2) {
		this.tw2 = tw2;
	}
	public double[][] getDriveTime() {
		return driveTime;
	}
	public void setDriveTime(double[][] driveTime) {
		this.driveTime = driveTime;
	}
	public int[] getLoadTime() {
		return loadTime;
	}
	public void setLoadTime(int[] loadTime) {
		this.loadTime = loadTime;
	}
	public double[] getPackageTime() {
		return packageTime;
	}
	public void setPackageTime(double[] packageTime) {
		this.packageTime = packageTime;
	}
	public int getIE() {
		return IE;
	}
	public void setIE(int iE) {
		IE = iE;
	}
	public int getOE() {
		return OE;
	}
	public void setOE(int oE) {
		OE = oE;
	}
	public int getIF() {
		return IF;
	}
	public void setIF(int iF) {
		IF = iF;
	}
	public int getOF() {
		return OF;
	}
	public void setOF(int oF) {
		OF = oF;
	}
	public int getPortNum() {
		return portNum;
	}
	public void setPortNum(int portNum) {
		this.portNum = portNum;
	}
	public int getStockNum() {
		return stockNum;
	}
	public void setStockNum(int stockNum) {
		this.stockNum = stockNum;
	}
	public int[] getTruckNum() {
		return truckNum;
	}
	public void setTruckNum(int[] truckNum) {
		this.truckNum = truckNum;
	}
	public int getLoadCar() {
		return loadCar;
	}
	public void setLoadCar(int loadCar) {
		this.loadCar = loadCar;
	}
	public int getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(int taskNum) {
		this.taskNum = taskNum;
	}
	public int gettMax() {
		return tMax;
	}
	public void settMax(int tMax) {
		this.tMax = tMax;
	}
	public Data(){}
	
	
}	
