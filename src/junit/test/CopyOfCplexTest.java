package junit.test;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

/**
 * 数学模型《集装箱调运中的建模与优化》5.2节 // 单港口，多堆场，包含空箱运输
 * 
 * 顺序——》 港口，堆场，客户点 顺序——》IE，OE，IF，OF
 * 
 * @author Leon
 * 
 */
public class CopyOfCplexTest {

	@Test
	public void model() {

		// the number of ports
		int[][] tw1 =  { {  0, 0, 0, 0, 0 },
				{  1000, 1000, 1000, 1000, 1000 } };
		int[][] tw2 ={ {  0, 0, 0, 0, 0 },
				{  1000, 1000, 1000, 1000, 1000 } };
		int[][] driveTime ={ { 0, 61, 18, 82, 21, 50, 40 },
				{ 61, 0, 54, 81, 80, 48, 70 },
				{ 18, 54, 0, 64, 38, 58, 25},
				{ 82, 81, 64, 0, 101, 114, 45 },
				{ 21, 80, 38, 101, 0, 57, 57 },
				{ 50, 48, 58, 114, 57, 0, 83 },
				{ 40, 70, 25, 45, 57, 83, 0 },
				 };
//		int[] loadTime = { 0, 106, 66, 111, 6, 63, 118 };
		int[] loadTime = { 106, 66, 111, 63, 118 };

		// System.out.println("--------------------------tw1");
		// for (int i = 0; i < tw1[0].length; i++) {
		//
		// System.out.print(tw1[0][i] + "," + tw1[1][i] + "   ");
		// }
		// System.out.println("--------------------------tw1");
		// System.out.println("--------------------------tw2");
		//
		// for (int i = 0; i < tw2[0].length; i++) {
		//
		// System.out.print(tw2[0][i] + "," + tw2[1][i] + "   ");
		// }
		//
		// System.out.println("--------------------------tw2");
		// System.out.println("--------------------------driveTime");
		//
		// for (int i = 0; i < driveTime[0].length; i++) {
		// for (int j = 0; j < driveTime[0].length; j++) {
		// System.out.print(driveTime[i][j] + "\t");
		// }
		// System.out.println();
		// }
		// System.out.println("--------------------------driveTime");
		//
		// System.out.println("--------------------------loadTime");
		// for (int i = 0; i < loadTime.length; i++) {
		//
		// System.out.print(loadTime[i] + "\t");
		// }
		// System.out.println("--------------------------loadTime");

		// --------------------------------------------------------------------------------------------
		// int taskNum = 7; // 任务数<=客户点
		int IENum = 0;
				int OENum = 0;
		int IFNum = 2;
		int OFNum =3;
		final int portNum = 1;
		int stockNum = 1;
		int[] truckNum = { 4 };
		final int LOADTIME = 5;
		int taskNum = 5;

		// 写工具getLoad
		// 每一个任务都要有装卸时间
		// int[] loadTime = { 10, 7, 13, 7, 5, 9, 5 };

		// timewindow1[0][1]与timewindow1[1][1] 是一对时间窗，表示第2个任务的
		// 后面的一维是固定的,是"第几个任务"的时间窗
		// 第一时间窗有taksNum个
		// int[][] tw1 = { { 67, 3, 23, 236, 0, 195, 106 },
		// { 213, 175, 116, 282, 10, 344, 130 } };
		// // 第二时间窗的顺序是IF，OF，有IF+OF个
		// int[][] tw2 = { { 62, 247, 31, 235, 149 }, { 209, 513, 217, 493, 376
		// } };

		// 只有这里与地理位置有关。顺序：港口，堆场，客户点
		// 写工具getDis
		// int[][] driveTime = {
		// { 0, 57, 66, 19, 53, 68, 72, 67, 91 },
		// { 57, 0, 23, 51, 80, 16, 51, 77, 77 },
		// { 66, 23, 0, 66, 71, 36, 29, 61, 54 },
		// { 19, 51, 66, 0, 71, 58, 80, 83, 102 },
		// { 53, 80, 71, 71, 0, 96, 54, 23, 57 },
		// { 68, 16, 36, 58, 96, 0, 66, 93, 91 },
		// { 72, 51, 29, 80, 54, 66, 0, 37, 25 },
		// { 67, 77, 61, 83, 23, 93, 37, 0, 34 },
		// { 91, 77, 54, 102, 57, 91, 25, 34, 0 } };

		// 服务时间
		double[] serviceTime = new double[taskNum];
		// 顶点活动时间
		double[][] nodeTW = new double[2][taskNum];
		// 顶点转换时间
		double[][] transTime = new double[taskNum + stockNum][taskNum
				+ stockNum];

		/**
		 * 条件
		 */
		condition(taskNum, IENum, OENum, IFNum, portNum, stockNum, LOADTIME,
				loadTime, tw1, tw2, driveTime, serviceTime, nodeTW, transTime);

		/**
		 * 数学模型
		 */
		try {
			IloCplex cplex = new IloCplex();
			// 决策变量Xij
			IloNumVar[][] X = new IloNumVar[stockNum + taskNum][];
			for (int i = 0; i < stockNum + taskNum; i++) {
				// 定义决策变量的范围
				// 每一个X[i]都有client+1个数
				X[i] = cplex.boolVarArray(stockNum + taskNum);
			}

			// 决策变量Yij
			IloNumVar[] Y = cplex.numVarArray(taskNum, Double.MIN_VALUE,
					Double.MAX_VALUE);

			// 目标函数
			IloNumExpr[][] expr0 = new IloNumExpr[stockNum + taskNum][stockNum
					+ taskNum];
			IloNumExpr[] expr1 = new IloNumExpr[stockNum + taskNum];
			for (int i = 0; i < stockNum + taskNum; i++) {
				for (int j = 0; j < stockNum + taskNum; j++) {
					// if (i != j)
					expr0[i][j] = cplex.prod(transTime[i][j], X[i][j]);
				}
			}

			for (int i = 0; i < stockNum + taskNum; i++) {
				expr1[i] = cplex.sum(expr0[i]);
			}

			IloNumExpr exprObj = cplex.sum(expr1);

			// 约束1
			IloNumExpr[][] temp1 = new IloNumExpr[stockNum][taskNum];
			IloNumExpr[] expr3 = new IloNumExpr[stockNum];
			for (int i = 0; i < stockNum; i++) {
				for (int j = 0; j < taskNum; j++) {
					// i属于堆场，j属于任务点
					temp1[i][j] = X[i][j + stockNum];
				}
			}
			for (int i = 0; i < stockNum; i++) {
				expr3[i] = cplex.sum(temp1[i]);
				cplex.addLe(expr3[i], truckNum[i]);
			}

			// 约束2
			IloNumExpr[][] expr4 = new IloNumExpr[taskNum][taskNum + stockNum];
			IloNumExpr[] expr5 = new IloNumExpr[taskNum];
			IloNumExpr[] expr5_2 = new IloNumExpr[taskNum];
			for (int i = 0; i < taskNum; i++) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					expr4[i][j] = X[j][i + stockNum];
				}
			}
			for (int i = 0; i < taskNum; i++) {
				expr5[i] = cplex.sum(expr4[i]);
				expr5_2[i] = cplex.sum(X[i + stockNum]);
			}

//			for (int i = 0; i < taskNum; i++) {
//				IloNumExpr e1 = cplex.sum(X[i + stockNum]);
//				IloNumExpr e2 = cplex.sum(expr5);
//				cplex.addEq(e1, e2);
//				cplex.addEq(1, e1);
//			}
			for (int i = 0; i < taskNum; i++) {
				cplex.addEq(expr5[i], expr5_2[i]);
				cplex.addEq(1, expr5[i]);
				cplex.addEq(1, expr5_2[i]);
			}

			
			
			// 约束3
			for (int i = 0; i < taskNum; i++) {
				cplex.addLe(nodeTW[0][i], Y[i]);
				cplex.addLe(Y[i], nodeTW[1][i]);
			}

			// 约束4
			 int M = Integer.MAX_VALUE;
//			 double M = Double.MAX_VALUE;
			for (int i = 0; i < taskNum; i++) {
				for (int j = 0; j < taskNum; j++) {
					// if (i != j)
					// model.add(TB[i]+ET[i]+Tran_time[i][j]-TB[j]<=(1-X[i][j])*M);
					cplex.addLe(
							cplex.diff(cplex.sum(Y[i], serviceTime[i] + transTime[i + stockNum][j + stockNum]), Y[j]),
							cplex.prod( M, cplex.diff(1.0, X[i + stockNum][j + stockNum])));
				}
			}

			/**
			 * 模型求解
			 */
			// 最小化目标函数
			cplex.addMaximize(exprObj);
			if (cplex.solve()) {
				System.out.println("-----------1、求解状态----------");
				System.out.println("Solution status: " + cplex.getStatus()
						+ "|||" + cplex.getAlgorithm());

				Double[][] x = new Double[stockNum + taskNum][stockNum
						+ taskNum];
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						if (i != j)
							x[i][j] = (Double) cplex.getValue(X[i][j]);
					}
				}
//				Double[] y = new Double[taskNum];
				for(int i = 0;i<taskNum;i++){
					System.out.println(cplex.getValue(Y[i]));
				}

				System.out.println("-----------2、求解时间----------");
				System.out.println("The total time is " + cplex.getObjValue());

				System.out.println("-------------3、路径-------------");
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						if ((i != j) && (x[i][j] == 1)) {
							// if ((i != j) ) {
							System.out.println(i + "--" + j);
						}
					}
				}

				System.out.println("-------------4、解-------------");
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						{
							System.out.println("x[" + i + "]" + "[" + j + "]"
									+ "=" + x[i][j]);
						}
					}
				}

			}
			System.out.println("-------------5、转换时间-------------");
			for (int i = 0; i < stockNum + taskNum; i++) {
				for (int j = 0; j < stockNum + taskNum; j++) {
					System.out.println("transtime[" + i + "][" + j + "]" + "="
							+ transTime[i][j]);
				}
			}
			System.out.println("-------------6、节点活动时间-------------");
			for (int j = 0; j < taskNum; j++) {
				for (int i = 0; i < 2; i++) {
					System.out.print("[" + i + "][" + j + "]" + "="
							+ nodeTW[i][j] + "\t");
				}
				System.out.println();
			}
			System.out.println("-------------7、转换时间-------------");
			for (int i = 0; i < stockNum + taskNum; i++) {
				for (int j = 0; j < stockNum + taskNum; j++) {
					System.out.println("transtime[" + i + "][" + j + "]" + "="
							+ transTime[i][j]);
				}
			}

			cplex.end();

		} catch (IloException e) {

			e.printStackTrace();

		}

		//

	}

	/**
	 * 条件的判断
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
	 */
	private static void condition(int taskNum, int IENum, int OENum, int IFNum,
			int portNum, int stockNum, final int LOADTIME, int[] loadTime2,
			int[][] tw1, int[][] tw2, int[][] driveTime, double[] serviceTime,
			double[][] nodeTW, double[][] transTime) {
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
				serviceTime[i] = (
						Math.max(
							(tw2[0][i - IENum - OENum] - tw1[1][i]),
							//第一个客户就是if0对应的
							(LOADTIME + driveTime[0][i + stockNum + portNum - IENum - OENum])
						) +
						loadTime2[i - IENum - OENum] +LOADTIME);
			}
			// i属于OF
			if (i >= (IFNum + IENum + OENum) && i < taskNum) {
				serviceTime[i] = (
						Math.max(
							(tw2[0][i - IENum - OENum] - tw1[1][i]),
							(loadTime2[i - IENum - OENum] +LOADTIME + driveTime[i + stockNum + portNum - IENum - OENum][0])
						) +
						LOADTIME);
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
				nodeTW[0][i] = 
						Math.min(
								Math.max(
										tw1[0][i], 
										tw2[0][i - IENum - OENum] - LOADTIME - driveTime[0][i -IENum-OENum+ portNum + stockNum]
										),
								tw1[1][i]
								);

				nodeTW[1][i] = 
						Math.min(
								tw1[1][i], 
								tw2[1][i - IENum - OENum]- LOADTIME - driveTime[0][i -IENum-OENum+ portNum + stockNum]
								);
			}
			// i属于OF
			if (i >= (IFNum + IENum + OENum) && i < taskNum) {

				nodeTW[0][i] = 
						Math.min(
							Math.max(
									tw1[0][i], 
									tw2[0][i - IENum - OENum] - LOADTIME - driveTime[i + portNum + stockNum - IENum - OENum][0] - loadTime2[i - IENum - OENum]
									), 
							tw1[1][i]
							);

				nodeTW[1][i] = 
						Math.min(
								tw1[1][i], 
								tw2[1][i - IENum - OENum] - LOADTIME - driveTime[i + portNum + stockNum - IENum - OENum][0] - loadTime2[i - IENum - OENum]
								);
			}
		}

		/**
		 * 装换时间Tij 从零开始先堆场，后任务
		 */
		int[] drive0 = Arrays.copyOfRange(driveTime[0], portNum, stockNum
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
					else if (j >= stockNum + IENum && j < stockNum + IENum + OENum) {
						transTime[i][j] = LOADTIME + driveTime[i + portNum][0];
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum && j < taskNum + stockNum) {
						transTime[i][j] = LOADTIME * 2 + driveTime[i + portNum][j + portNum - IENum - OENum];
//						transTime[i][j] = LOADTIME * 2 + driveTime[i + portNum][j + portNum];
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
					else if (j >= stockNum + IENum && j < stockNum + IENum + OENum) {
						transTime[i][j] = 0;
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum && j < taskNum + stockNum) {
						transTime[i][j] = LOADTIME + driveTime[0][j + portNum - IENum - OENum];
//						transTime[i][j] = LOADTIME + driveTime[0][j + portNum ];
					}
					// j属于 IF U IE
					else {
						transTime[i][j] = LOADTIME + drive0[0] * 2;
					}
				}
			}
			// i 属于 IF
			else if (i >= stockNum + IENum + OENum && i < stockNum + IENum + OENum + IFNum) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// j属于 堆场
					if (j < stockNum) {
						transTime[i][j] = LOADTIME * 2 + driveTime[i + portNum - IENum - OENum][j + portNum];
					}
					// j属于 OE
					else if (j >= stockNum + IENum && j < stockNum + IENum + OENum) {
						transTime[i][j] = LOADTIME + driveTime[i + portNum - IENum - OENum][0];
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum && j < taskNum + stockNum) { 
						if (driveTime[i + portNum - IENum - OENum  ][j + portNum - IENum - OENum ] > 0) {
							transTime[i][j] = LOADTIME * 2  + driveTime[i + portNum - IENum - OENum  ][j + portNum - IENum - OENum ];
						} else {
							transTime[i][j] = 0;
						}
					}
					// j属于 IF U IE
					else {
						int[] ds = Arrays.copyOfRange(  driveTime[i + portNum - IENum - OENum], portNum, stockNum + portNum);
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
					else if (j >= stockNum + IENum && j < stockNum + IENum + OENum) {
						transTime[i][j] = drive0[0] * 2 + LOADTIME;
					}
					// j属于 OF
					else if (j >= stockNum + IENum + OENum + IFNum && j < taskNum + stockNum) {
						int[] ds = Arrays.copyOfRange( driveTime[j + portNum - IENum - OENum], portNum, stockNum + portNum);
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
}
