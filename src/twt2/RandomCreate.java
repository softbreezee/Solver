package twt2;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ref.Data;

/**
 * 根据condition对象创建初始的数据，是否需要保存在数据库中！
 * 方案一
 * 不保存，则不能查看这些数据，或者说算例不能二次创建
 * 
 * 方案二
 * 保存，可以查看修改这些数据，可是创建二次算例
 * 
 * 2017、8、6号，开发停滞在第二时间窗的处理，要求行驶时间。第二时间窗是按照任务的顺序来生成。
 * 但是行驶时间矩阵是按照港口、堆场、客户的顺序生成的。
 * 客户不一定与任务的个数相等！
 * 客户点一定等于重箱任务的的个数！客户点只会在生成的时候出现重合的可能！
 * 但是客户点不一定是按照IE OE IF OF顺序排列的！！
 * 
 * 解决办法：暂时查看别人的生成初始信息的程序改进，完成后台方案一二！
 * @author Leon
 */
public class RandomCreate {
	private static RandomCreate create;
	
	int ieNum;
	int oeNum;
	int ifNum;
	int ofNum;
	int taskNum;
	int portNum;
	int stockNum;
	
	int LOADTIME = 5;
	static int[][] clientP;
	static int[][] portP ;
	static int[][] stockP ;
	static double[][] distance ;
	double[][] driveTime ;
	
	int[][] tw1;
	int[][] tw2 ;
	int[] loadTime ;
	// 顶点活动时间
	double[] serviceTime;
	// 任务顶点活动时间
	double[][] nodeTW ;
	// 弧转换时间
	double[][] transTime;
	

	static {
		create = new RandomCreate();
	}

	
	
	/**
	 * 静态方法，提供一个map集合 封装信息： 
	 * 客户位置
	 * 港口位置
	 * 堆场位置
	 * 距离矩阵
	 * 时间矩阵
	 * 第一时间窗（taskNum） 
	 * 第二时间窗(IF+OF)
	 * 装卸货时间
	 * 距离矩阵(portNum+stockNum+taskNum)
	 * 
	 * @param ep
	 * @param ep
	 * @return
	 */
	public static Data getCondition(int IE,int OE,int IF,int OF,int portNum,int stockNum,int rangeX,int rangeY) {
		Data d =new Data();
		d.stockNum = stockNum;
		d.portNum = portNum;
		d.IE = IE;
		d.OE = OE;
		d.IF = IF;
		d.OF = OF;
		d.taskNum = IE+OE+IF+OF;
		d.loadCar = 5;
		clientP = create.getPosition(IF+OF,rangeX,rangeY);
		portP = create.getPosition(portNum,rangeX, rangeY);
		stockP = create.getPosition(stockNum,rangeX, rangeY);
		distance = create.getDis(clientP, stockP, portP);
		d.driveTime = create.getDriveTime(distance);
		Map<String, Object> tw = create.getTW(IE,OE,IF,OF,portNum,stockNum, distance);
		d.tw1 = (int[][]) tw.get("tw1");
		d.tw2 = (int[][]) tw.get("tw2");
		d.loadTime = create.getLoadTime(IF,OF);
		return d;
	}


	//行驶时间矩阵
	private double[][] getDriveTime(double[][] distance) {
		double[][] driveTime = new double[distance[0].length][distance[0].length];
		for (int i = 0; i < distance[0].length; i++) {
			for (int j = i; j < distance[0].length; j++) {
				driveTime[i][j] = myRound(distance[i][j]/300,2);
				driveTime[j][i] = myRound(driveTime[i][j],2);
			}
		}
		
		return driveTime;
	}


	/**
	 * 时间窗问题：1、范围2、行驶的时间
	 * 
	 * @param IE
	 * @param OE
	 * @param IF
	 * @param OF
	 * @param rangeOfClient
	 * @param rangeOfstock
	 */
	private Map<String, Object> getTW(int IENum,int OENum,int IFNum,int OFNum,int portNum,int stockNum,double[][] distance) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		// 第一时间窗
		int taskNum = IENum+ IFNum + OENum + OFNum;
		int[][] tw1 = new int[2][taskNum];
		// 注意：：随机数应该乘的是范围！！
		for (int i = 0; i < taskNum; i++) {
			//第一时间窗是8：00 - 12：00，宽度是0-3小时
			tw1[0][i] = (int) (Math.random() * 240);
			tw1[1][i] = tw1[0][i] + (int) (Math.random() * 180);
		}
		map.put("tw1", tw1);

		// 第二时间窗,只有重箱任务有第二时间窗
		int num = OFNum + IFNum;
		int[][] tw2 = new int[2][num];
		// 注意：：随机数应该乘的是范围！！
		for (int i = 0; i < num; i++) {
			// 第二时间窗的开始时刻为第一时间窗的开始时刻+客户点与港口之间的行驶距离
			tw2[0][i] = tw1[0][i + taskNum - num] +(int)(distance[0][i+portNum+stockNum]/500) ;
			//宽度是2-5小时
			tw2[1][i] = tw2[0][i] + (int) (Math.random() * 180+120);
		}
		map.put("tw2", tw2);

		return map;
	}
	
	
	/**
	 * 生成初始位置
	 */
	private int[][] getPosition(int Num, int RangeOfX, int RangeOfY) {
		int[][] arr = new int[2][Num];
		for (int i = 0; i < Num; i++) {
			arr[0][i] = (int) (Math.random() * RangeOfX);
			arr[1][i] = (int) (Math.random() * RangeOfY);
		}
		return arr;
	}
	
	
	/**
	 * 生成距离矩阵
	 */
	private double[][] getDis(int[][] client, int[][] stock, int[][] port) {

		int clientNum = 0;
		if (client != null) {
			clientNum = client[0].length;
		}

		int num = clientNum + stock[0].length + port[0].length;
		double[][] dis = new double[num][num];
		// 港口、堆场、客户点
		// 生成距离矩阵
		for (int i = 0; i < num; i++) {
			for (int j = i; j < num; j++) {
				if (i < port[0].length) {
					if (j < port[0].length) {
						double x = Math.pow(port[0][i] - port[0][j], 2);
						double y = Math.pow(port[1][i] - port[1][j], 2);
						dis[i][j] = myRound(Math.pow(x + y, 0.5),2);
					}
					if (j >= port[0].length
							&& j < stock[0].length + port[0].length) {
						double x = Math.pow(port[0][i] - stock[0][j - port[0].length], 2);
						double y = Math.pow(port[1][i] - stock[1][j - port[0].length], 2);
						dis[i][j] = myRound(Math.pow(x + y, 0.5),2);
					}
					if (j >= stock[0].length + port[0].length) {
						double x = Math.pow(port[0][i] - client[0][j - stock[0].length - port[0].length], 2);
						double y = Math.pow(port[1][i] - client[1][j - stock[0].length - port[0].length], 2);
						dis[i][j] = myRound(Math.pow(x + y, 0.5),2);
					}

				}

				if (i >= port[0].length && i < stock[0].length + port[0].length) {
					if (j >= port[0].length && j < stock[0].length + port[0].length) {
						double x = Math.pow(stock[0][i - port[0].length] - stock[0][j - port[0].length], 2);
						double y = Math.pow(stock[1][i - port[0].length]- stock[1][j - port[0].length], 2);
						dis[i][j] = myRound(Math.pow(x + y, 0.5),2);
					}
					if (j >= stock[0].length + port[0].length) {
						double x = Math.pow(stock[0][i - port[0].length] - client[0][j - stock[0].length - port[0].length], 2);
						double y = Math.pow(stock[1][i - port[0].length] - client[1][j - stock[0].length - port[0].length], 2);
						dis[i][j] = myRound(Math.pow(x + y, 0.5),2);
					}

				}

				if (i >= stock[0].length + port[0].length
						&& j >= stock[0].length + port[0].length) {
					double x = Math.pow(client[0][i - stock[0].length - port[0].length] - client[0][j - stock[0].length - port[0].length],2);
					double y = Math.pow(client[1][i - stock[0].length - port[0].length] - client[1][j - stock[0].length - port[0].length],2);
					dis[i][j] = myRound(Math.pow(x + y, 0.5),2);
				}
				dis[j][i] = dis[i][j];
			}
		}

		return dis;

	}

	/**
	 * 生成装卸时间
	 *  --只有重箱任务有装卸货时间（第一次修改）
	 */
	private int[] getLoadTime(int IFNum,int OFNum) {
		int num =IFNum+ OFNum;
//		int num =ep.getIFNum() +ep.getIENum()+ ep.getOFNum()+ep.getOENum();
		int[] loadTime = new int[num];
		for (int i = 0; i < num; i++) {
			loadTime[i] = (int) (Math.random() * 15 + 5);
		}
		return loadTime;
	}
	
	public static void condition(int taskNum, int IENum, int OENum, int IFNum,
			int portNum, int stockNum, final int LOADTIME, int[] loadTime2,
			int[][] tw1, int[][] tw2, double[][] driveTime,
			double[] serviceTime, double[][] nodeTW, double[][] transTime) {
		/**
		 * 服务时间
		 * ie，oe，if,of
		 */
		for (int i = 0; i < taskNum; i++) {
			// i属于IE+OE
			if (i < (IENum + OENum)) {
				serviceTime[i] = LOADTIME;
			}
			// i属于IF
			if (i >= (IENum + OENum) && i < (IFNum + IENum + OENum)) {
				serviceTime[i] = myRound((Math.max(
						(tw2[0][i - IENum - OENum] - tw1[1][i]),
						// 第一个客户就是if0对应的
						(LOADTIME + driveTime[0][i + stockNum + portNum - IENum
								- OENum]))
						+ loadTime2[i - IENum - OENum] + LOADTIME),2);
			}
			// i属于OF
			if (i >= (IFNum + IENum + OENum) && i < taskNum) {
				serviceTime[i] = myRound((Math.max(
						(tw2[0][i - IENum - OENum] - tw1[1][i]), (loadTime2[i
								- IENum - OENum]
								+ LOADTIME + driveTime[i + stockNum + portNum
								- IENum - OENum][0])) + LOADTIME),2);
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
						transTime[i][j] = myRound(LOADTIME
								* 2
								+ driveTime[i + portNum][j + portNum - IENum
										- OENum],2);
						// transTime[i][j] = LOADTIME * 2 + driveTime[i +
						// portNum][j + portNum];
					}
					// j属于 IF U IE
					else {
						transTime[i][j] = myRound(driveTime[i + portNum][0],2);
					}
				}
			}
			// i 属于 IE
			else if (i >= stockNum && i < stockNum + IENum) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = myRound(LOADTIME + driveTime[0][j + portNum],2);
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = 0;
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						transTime[i][j] = myRound(LOADTIME
								+ driveTime[0][j + portNum - IENum - OENum],2);
						// transTime[i][j] = LOADTIME + driveTime[0][j + portNum
						// ];
					}
					// j属于 IF U IE
					else {
						transTime[i][j] = myRound(LOADTIME + drive0[0] * 2,2);
					}
				}
			}
			// i 属于 IF
			else if (i >= stockNum + IENum + OENum
					&& i < stockNum + IENum + OENum + IFNum) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = myRound(LOADTIME
								* 2
								+ driveTime[i + portNum - IENum - OENum][j
										+ portNum],2);
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = myRound(LOADTIME
								+ driveTime[i + portNum - IENum - OENum][0],2);
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						if (driveTime[i + portNum - IENum - OENum][j + portNum
								- IENum - OENum] > 0) {
							transTime[i][j] = myRound(LOADTIME
									* 2
									+ driveTime[i + portNum - IENum - OENum][j
											+ portNum - IENum - OENum],2);
						} else {
							transTime[i][j] = 0;
						}
					}
					// j属于 IF U IE
					else {
						double[] ds = Arrays.copyOfRange(driveTime[i + portNum
								- IENum - OENum], portNum, stockNum + portNum);
						Arrays.sort(ds);
						transTime[i][j] = myRound(LOADTIME * 2 + ds[0] + drive0[0],2);
					}
				}

			}
			// i 属于 OF U OE
			else {
				for (int j = 0; j < taskNum + stockNum; j++) {

					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = myRound(driveTime[0][j + portNum],2);
					}
					// j属于 OE
					else if (j >= stockNum + IENum
							&& j < stockNum + IENum + OENum) {
						transTime[i][j] = myRound(drive0[0] * 2 + LOADTIME,2);
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum
							&& j < taskNum + stockNum) {
						double[] ds = Arrays.copyOfRange(driveTime[j + portNum
								- IENum - OENum], portNum, stockNum + portNum);
						Arrays.sort(ds);
						transTime[i][j] = myRound(LOADTIME * 2 + ds[0] + drive0[0],2);
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
	
	// ///////// 写出自定义格式算例文件
	public void fileOutput() throws IOException {
//		String outputFilePath = "/datafile" + numofPickup + "-" + numofDelivery + ".txt";
		String outputFilePath = "D:/twtdata/datafile" + taskNum +"-"+stockNum+ ".txt";
		FileWriter fw = new FileWriter(outputFilePath);

		// 输出基本参数
		fw.write("# of ie customers\r\n" + ieNum + "\r\n");
		fw.write("# of oe customers\r\n" + oeNum + "\r\n");
		fw.write("# of if customers\r\n" + ifNum + "\r\n");
		fw.write("# of of customers\r\n" + ofNum + "\r\n");
		fw.write("# of customers\r\n" + (ifNum+ofNum) + "\r\n");
		fw.write("# of tasks\r\n" + (taskNum) + "\r\n");
		fw.write("# of stockNum\r\n" + (stockNum) + "\r\n");


//		// 输出堆场车辆;
//		fw.write("package time\r\n");
//		for (int i = 0; i < stockNum; i++) {
//			fw.write(packageTime[i] + "\r\n");
//		}
		// 输出package time;
		fw.write("package time\r\n");
		for (int i = 0; i < loadTime.length; i++) {
			fw.write(loadTime[i] + "\r\n");
		}

		fw.write("serviceTime\r\n");
		for (int i = 0; i < serviceTime.length; i++) {
			fw.write(serviceTime[i] + "\r\n");
		}
		fw.write("nodeTW\r\n");
		for (int i = 0; i < nodeTW.length; i++) {
			for (int j = 0; j < nodeTW[0].length; j++) {
				
				fw.write(nodeTW[i][j] + "\r\n");
			}
		}

		// 输出distance matrix;
		fw.write("transTime\r\n");
		for (int i = 0; i < transTime.length; i++) {
			for (int j = 0; j < transTime[0].length; j++) {
				fw.write(transTime[i][j] + "\r\n");
			}
		}

		fw.write("ENDDATA\r\n");
		fw.flush();
		fw.close();
	}

	
//	//读文件
//	public void fileInput(Reader io) throws IOException {
//
////		String inputFilePath = "data/data/datafile" + dataindex + ".txt";
////		String inputFilePath = "C:/Users/Administrator/Desktop/data/datafile" + dataindex + ".txt";
//		// 读入cluster数据文件
//		// String inputFilePath = "data/clusterdata/datafile" + pickup + "-"
//		// + delivery + ".txt";
//		// 读入某确定文件，进行不同rho比例计算
//		// String inputFilePath = "data/rho ratio data/datafile4-4-4.5.txt";
//		BufferedReader br = new BufferedReader(io);
//
//		// 输入基本参数
//		br.readLine();
//		String str = br.readLine();
//		StringTokenizer st = new StringTokenizer(str);
//		ieNum = (int) Double.parseDouble(st.nextToken());
//
//		br.readLine();
//		str = br.readLine();
//		st = new StringTokenizer(str);
//		oeNum = (int) Double.parseDouble(st.nextToken());
//
//		br.readLine();
//		str = br.readLine();
//		st = new StringTokenizer(str);
//		ifNum = (int) Double.parseDouble(st.nextToken());
//
//		br.readLine();
//		str = br.readLine();
//		st = new StringTokenizer(str);
//		ofNum = (int) Double.parseDouble(st.nextToken());
//		
//		br.readLine();
//		str = br.readLine();
//		st = new StringTokenizer(str);
//		clientNum = (int) Double.parseDouble(st.nextToken());
//		
//		br.readLine();
//		str = br.readLine();
//		st = new StringTokenizer(str);
//		taskNum = (int) Double.parseDouble(st.nextToken());
//
//		br.readLine();
//		str = br.readLine();
//		st = new StringTokenizer(str);
//		stockNum = (int) Double.parseDouble(st.nextToken());
//
//		loadTime = new int[clientNum];
//		serviceTime = new double[taskNum];
//		// 任务顶点活动时间
//		nodeTW = new double[2][taskNum];
//		// 弧转换时间
//		transTime = new double[taskNum + stockNum][taskNum + stockNum];
//
//		// 输入package time;
//		br.readLine();
//		for (int i = 0; i < clientNum; i++) {
//			str = br.readLine();
//			st = new StringTokenizer(str);
//			loadTime[i] = Integer.parseInt(st.nextToken());
//		}
//		
//		
//		
//		br.readLine();
//		for (int i = 0; i < serviceTime.length; i++) {
//			str = br.readLine();
//			st = new StringTokenizer(str);
//			serviceTime[i] = Double.parseDouble(st.nextToken());
//		}
//		br.readLine();
//		for (int i = 0; i < nodeTW.length; i++) {
//			for (int j = 0; j < nodeTW[0].length; j++) {
//				
//				str = br.readLine();
//				st = new StringTokenizer(str);
//				nodeTW[i][j] = Double.parseDouble(st.nextToken());
//			}
//		}
//
//
//		// 输入transTime;
//		br.readLine();
//		for (int i = 0; i < transTime.length; i++) {
//			for (int j = 0; j < transTime[0].length; j++) {
//				str = br.readLine();
//				st = new StringTokenizer(str);
//				transTime[i][j] = Double.parseDouble(st.nextToken());
//			}
//		}
//
//
//		br.close();
//	}

	
	

}
