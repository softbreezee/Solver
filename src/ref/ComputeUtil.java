package ref;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ComputeUtil {
	
	private ComputeUtil(){}

	/**
	 * 获取一个数组集合的所有子集
	 * 
	 * @param set
	 *            要求集合的数组
	 * @return
	 */
	public static Set<Set<Integer>> getSubSet(int[] set) {
		Set<Set<Integer>> result = new HashSet<Set<Integer>>(); // 用来存放子集的集合，如{{},{1},{2},{1,2}}
		int length = set.length;
		int num = length == 0 ? 0 : 1 << (length); // 2的n次方，若集合set为空，num为0；若集合set有4个元素，那么num为16.

		// 从0到2^n-1（[00...00]到[11...11]）
		for (int i = 0; i < num; i++) {
			Set<Integer> subSet = new HashSet<Integer>();

			int index = i;
			for (int j = 0; j < length; j++) {
				if ((index & 1) == 1) { // 每次判断index最低位是否为1，为1则把集合set的第j个元素放到子集中
					subSet.add(set[j]);
				}
				index >>= 1; // 右移一位
			}

			result.add(subSet); // 把子集存储起来
		}
		return result;
	}

	/**
	 * 模型五的数据生成
	 */
	public static Map<String, Object> Twk5Condition(Data d) {
		Map<String,Object> map = new HashMap<String,Object>();
		int tasks = d.taskNum * 2;
		int ifNum = d.IF;
		int ofNum = d.OF;
		// 顺序是D,I1,O1,I2,O2
		// D,C,C（第一阶段，第二阶段）
		// 每一阶段是进口，出口
		/*
		 * 顶点时间处理
		 * 进口任务第一阶段进去重挂，开箱卸货时间t,第二阶段时间为零
		 * 出口任务第一阶段为0，第二阶段时间为t
		 * 
		for(int i = 0;i<tasks;i++){
			
		}
		 */
		
		
		//时间矩阵(对应实际的地理位置)
		double[][] driveTimes = d.driveTime;
		/*
		 * 顶点转换时间
		 */
		double[][] tij = new double[d.stockNum+tasks][d.stockNum+tasks];
		for (int i = 0; i < d.stockNum + tasks; i++) {
			//i属于堆场
			if (i < d.stockNum) {
				for(int j = 0;j<d.stockNum + tasks;j++){
					//j属于堆场
					if (j < d.stockNum) {
						tij[i][j] = 0;
					}
					//j属于I1、O1
					else if (j >= d.stockNum && j < d.stockNum + d.taskNum) {
						tij[i][j] = driveTimes[i][j];
					} 
					//j属于I2、O2
					else {
						tij[i][j] = driveTimes[i][j-d.taskNum];
					} 
				}

			}
			//i属于I1、O1
			else if (i >= d.stockNum && i < d.stockNum + ifNum + ofNum) {
				for(int j = 0;j<d.stockNum + tasks;j++){
					//j属于堆场
					if (j < d.stockNum) {
						tij[i][j] = driveTimes[i][j];
					}
					//j属于I1,O1
					else if (j >= d.stockNum && j < d.stockNum+ifNum+ofNum) {
						double x = driveTimes[i][0] + driveTimes[0][j];
						for(int k = 0;k<d.stockNum;k++){
							if(driveTimes[i][k] + driveTimes[k][j]<=x){
								x= driveTimes[i][k] + driveTimes[k][j];
							}
						}
						tij[i][j] = x;
					} 
					//j属于I2,O2
					else {
						tij[i][j] = driveTimes[i][j-d.taskNum];
					} 
				}
			}
			//i属于I2
			else if (i >= d.stockNum + ifNum + ofNum && i < d.stockNum + ifNum + ofNum + ifNum) {
				for(int j = 0;j<d.stockNum + tasks;j++){
					//j属于堆场
					if (j < d.stockNum) {
						tij[i][j] = driveTimes[i-d.taskNum][j];
					}
					//j属于I1
					else if (j >= d.stockNum && j < d.stockNum +ifNum) {
						double x = driveTimes[i-d.taskNum][0] + driveTimes[0][j];
						for(int k = 0;k<d.stockNum;k++){
							if(driveTimes[i-d.taskNum][k] + driveTimes[k][j]<=x){
								x= driveTimes[i-d.taskNum][k] + driveTimes[k][j];
							}
						}
						tij[i][j] = x;
					} 
					//j属于O1
					else if (j >= d.stockNum +ifNum && j < d.stockNum +ifNum+ofNum) {
						tij[i][j] = driveTimes[i-d.taskNum][j];
					} 
					//j属于I2、O2
					else {
						double x = driveTimes[i-d.taskNum][0] + driveTimes[0][j-d.taskNum];
						for(int k = 0;k<d.stockNum;k++){
							if(driveTimes[i-d.taskNum][k] + driveTimes[k][j-d.taskNum]<=x){
								x= driveTimes[i-d.taskNum][k] + driveTimes[k][j-d.taskNum];
							}
						}
						tij[i][j] = x;
					} 
				}
			} 
			//i属于O2
			else {
				for(int j = 0;j<d.stockNum + tasks;j++){
					//j属于堆场
					if (j < d.stockNum) {
						tij[i][j] = driveTimes[i-d.taskNum][j];
					}
					//j属于I1、O1
					else if (j >= d.stockNum && j < d.stockNum + d.taskNum) {
						double x = driveTimes[i-d.taskNum][0] + driveTimes[0][j];
						for(int k = 0;k<d.stockNum;k++){
							if(driveTimes[i-d.taskNum][k] + driveTimes[k][j]<=x){
								x= driveTimes[i-d.taskNum][k] + driveTimes[k][j];
							}
						}
						tij[i][j] = x;
					} 
					//j属于I2、O2
					else {
						double x = driveTimes[i-d.taskNum][0] + driveTimes[0][j-d.taskNum];
						for(int k = 0;k<d.stockNum;k++){
							if(driveTimes[i-d.taskNum][k] + driveTimes[k][j-d.taskNum]<=x){
								x= driveTimes[i-d.taskNum][k] + driveTimes[k][j-d.taskNum];
							}
						}
						tij[i][j] = x;
					} 
				}
			}
		}
		
		map.put("driveTimes", driveTimes);
		map.put("tij", tij);
		return map;
	}

	private static double findMin(double[] x) {
		double y = x[0];
		for(int i = 0;i<x.length;i++){
			if(x[i]<=y){
				y = x[i];
			}
		}
		return y;
	}
	
	
	@Test
	public void test(){
		double[] x = {6,7.1,3,15,9,19,11.2,5.3};
		System.out.println(findMin(x));
	}
	
	
	

	/**
	 * 对条件的封装
	 * 
	 * @param d
	 *            返回一个map，里面封装了创建好的图上顶点的时间窗。。。
	 * @return
	 */
	public static Map<String, Object> condition(Data d) {

		double[] serviceTime = new double[d.taskNum];
		// 任务顶点活动时间
		double[][] nodeTW = new double[2][d.taskNum];
		// 弧转换时间
		double[][] transTime = new double[d.taskNum + d.stockNum][d.taskNum
				+ d.stockNum];
		condition(d.taskNum, d.IE, d.OE, d.IF, d.portNum, d.stockNum,
				d.loadCar, d.loadTime, d.tw1, d.tw2, d.driveTime, serviceTime,
				nodeTW, transTime);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("serviceTime", serviceTime);
		map.put("nodeTW", nodeTW);
		map.put("transTime", transTime);
		return map;

	}

	/**
	 * 模型二条件的判断
	 * 
	 * 
	 * @param taskNum
	 * @param IENum
	 * @param OENum
	 * @param IFNum
	 * @param portNum
	 * @param stockNum
	 * @param LOADTIME
	 * @param loadTime2
	 * @param tw1
	 * @param tw2
	 * @param driveTime
	 * @param serviceTime
	 * @param nodeTW
	 * @param transTime
	 * 
	 *            double[] serviceTime = new double[taskNum]; // 任务顶点活动时间
	 *            double[][] nodeTW = new double[2][taskNum]; // 弧转换时间
	 *            double[][] transTime = new double[taskNum + stockNum][taskNum
	 *            + stockNum];
	 */
	public static void condition(int taskNum, int IENum, int OENum, int IFNum,
			int portNum, int stockNum, final int LOADTIME, int[] loadTime2,
			int[][] tw1, int[][] tw2, double[][] driveTime,
			double[] serviceTime, double[][] nodeTW, double[][] transTime) {
		/**
		 * 服务时间
		 */
		for (int i = 0; i < taskNum; i++) {
			// i属于IE+OE
			if (i < (IENum + OENum)) {
				serviceTime[i] = LOADTIME;
			}
			// i属于IF
			if (i >= (IENum + OENum) && i < (IFNum + IENum + OENum)) {
				serviceTime[i] = (Math.max(
						(tw2[0][i - IENum - OENum] - tw1[1][i]),
						// 第一个客户就是if0对应的
						(LOADTIME + driveTime[0][i + stockNum + portNum - IENum
								- OENum]))
						+ loadTime2[i - IENum - OENum] + LOADTIME);
			}
			// i属于OF
			if (i >= (IFNum + IENum + OENum) && i < taskNum) {
				serviceTime[i] = (Math.max(
						(tw2[0][i - IENum - OENum] - tw1[1][i]), (loadTime2[i
								- IENum - OENum]
								+ LOADTIME + driveTime[i + stockNum + portNum
								- IENum - OENum][0])) + LOADTIME);
			}
		}

		/**
		 * 任务顶点的时间窗的开始时刻与结束时刻
		 * 
		 */
		for (int i = 0; i < taskNum; i++) {
			// i属于IE+OE
			if (i < (IENum + OENum)) {
				nodeTW[0][i] = tw1[0][i];
				nodeTW[1][i] = tw1[1][i];
			}
			// i属于IF
			if (i >= (IENum + OENum) && i < (IFNum + IENum + OENum)) {
				nodeTW[0][i] = Math.min(
						Math.max(tw1[0][i], tw2[0][i - IENum - OENum]
								- LOADTIME
								- driveTime[0][i - IENum - OENum + portNum
										+ stockNum]), tw1[1][i]);

				nodeTW[1][i] = Math.min(tw1[1][i], tw2[1][i - IENum - OENum]
						- LOADTIME
						- driveTime[0][i - IENum - OENum + portNum + stockNum]);
			}
			// i属于OF
			if (i >= (IFNum + IENum + OENum) && i < taskNum) {

				nodeTW[0][i] = Math.min(
						Math.max(tw1[0][i], tw2[0][i - IENum - OENum]
								- LOADTIME
								- driveTime[i + portNum + stockNum - IENum
										- OENum][0]
								- loadTime2[i - IENum - OENum]), tw1[1][i]);

				nodeTW[1][i] = Math.min(tw1[1][i], tw2[1][i - IENum - OENum]
						- LOADTIME
						- driveTime[i + portNum + stockNum - IENum - OENum][0]
						- loadTime2[i - IENum - OENum]);
			}
		}

		/**
		 * 装换时间Tij 从零开始先堆场，后任务
		 */
		double[] drive0 = Arrays.copyOfRange(driveTime[0], portNum, stockNum
				+ portNum);
		Arrays.sort(drive0);

		for (int i = 0; i < taskNum + stockNum; i++) {
			// i 属于 堆场
			if (i < stockNum) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = 0;
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = LOADTIME + driveTime[i + portNum][0];
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						transTime[i][j] = LOADTIME
								* 2
								+ driveTime[i + portNum][j + portNum - IENum
										- OENum];
						// transTime[i][j] = LOADTIME * 2 + driveTime[i +
						// portNum][j + portNum];
					}
					// j属于 IF U IE
					else {
						transTime[i][j] = driveTime[i + portNum][0];
					}
				}
			}
			// i 属于 IE
			else if (i >= stockNum && i < stockNum + IENum) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = LOADTIME + driveTime[0][j + portNum];
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = 0;
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						transTime[i][j] = LOADTIME
								+ driveTime[0][j + portNum - IENum - OENum];
						// transTime[i][j] = LOADTIME + driveTime[0][j + portNum
						// ];
					}
					// j属于 IF U IE
					else {
						transTime[i][j] = LOADTIME + drive0[0] * 2;
					}
				}
			}
			// i 属于 IF
			else if (i >= stockNum + IENum + OENum
					&& i < stockNum + IENum + OENum + IFNum) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = LOADTIME
								* 2
								+ driveTime[i + portNum - IENum - OENum][j
										+ portNum];
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = LOADTIME
								+ driveTime[i + portNum - IENum - OENum][0];
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						if (driveTime[i + portNum - IENum - OENum][j + portNum
								- IENum - OENum] > 0) {
							transTime[i][j] = LOADTIME
									* 2
									+ driveTime[i + portNum - IENum - OENum][j
											+ portNum - IENum - OENum];
						} else {
							transTime[i][j] = 0;
						}
					}
					// j属于 IF U IE
					else {
						double[] ds = Arrays.copyOfRange(driveTime[i + portNum
								- IENum - OENum], portNum, stockNum + portNum);
						Arrays.sort(ds);
						transTime[i][j] = LOADTIME * 2 + ds[0] + drive0[0];
					}
				}

			}
			// i 属于 OF U OE
			else {
				for (int j = 0; j < taskNum + stockNum; j++) {

					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = driveTime[0][j + portNum];
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = drive0[0] * 2 + LOADTIME;
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						double[] ds = Arrays.copyOfRange(driveTime[j + portNum
								- IENum - OENum], portNum, stockNum + portNum);
						Arrays.sort(ds);
						transTime[i][j] = LOADTIME * 2 + ds[0] + drive0[0];
					}
					// j属于 IF U IE
					else {
						transTime[i][j] = 0;
					}
				}

			}
		}

	}

	// 保留小数点位数方法
	public static double myRound(double v, int scale) {
		String temp = "#0.";
		for (int i = 0; i < scale; i++) {
			temp += "0";
		}
		return Double.valueOf(new java.text.DecimalFormat(temp).format(v))
				.doubleValue();
	}

	// 计算距离函数
	public static double distance(double x1, double y1, double x2, double y2) {
		double truckspeed = 1; // /定义卡车行驶速度
		double dis = Math
				.sqrt((Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2)))
				/ truckspeed;
		return dis;
	}

}
