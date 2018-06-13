package tw1;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import org.junit.Test;

import ref.ComputeUtil;

/**
 * 计算工具,cplex
 * 
 * 任务的排列：IE OE IF OF ie0 ie1 ie2 oe0 oe1 if0 if1 of0 of1 of2 ...
 * DAOV图上的顶点排列顺序是出发/返回顶点、IE、OE、IF、OF
 * 
 * 实际中的顶点排列顺序是港口、堆场、客户
 * 
 * 重箱任务的数量要等于客户的数量！！
 * 
 * @author Leon
 * 
 */
public class TWSolver {

	@Test
	public void computing() {

		/**
		 * 1、接受已知的参数 int IE = (Integer) cd.get("IE"); int OE = (Integer)
		 * cd.get("OE"); int IF = (Integer) cd.get("IF"); int OF = (Integer)
		 * cd.get("OF"); int taskNum = (Integer) cd.get("taskNum"); int portNum
		 * = (Integer) cd.get("portNum"); int stockNum = (Integer)
		 * cd.get("stockNum"); int loadCar = (Integer) cd.get("loadCar"); int[]
		 * loadTime = (int[]) cd.get("loadTime"); int[][] tw1 = (int[][])
		 * cd.get("tw1"); int[][] tw2 = (int[][]) cd.get("tw2"); double[][]
		 * driveTime = (double[][]) cd.get("driveTime"); //注意，不要丢掉 int[]
		 * truckNum = (int[]) cd.get("truckNum");
		 */
		int[][] tw1 = { { 0, 0, 0, 0, 0 }, { 1000, 1000, 1000, 1000, 1000 } };
		int[][] tw2 = { { 0, 0, 0, 0, 0 }, { 1000, 1000, 1000, 1000, 1000 } };
		double[][] driveTime = { { 0, 61, 18, 82, 21, 50, 40, 100 },
				{ 61, 0, 54, 81, 80, 48, 70, 45 },
				{ 18, 54, 0, 64, 38, 58, 25, 87 },
				{ 82, 81, 64, 0, 101, 114, 45, 78 },
				{ 21, 80, 38, 101, 0, 57, 57, 121 },
				{ 50, 48, 58, 114, 57, 0, 83, 92 },
				{ 40, 70, 25, 45, 57, 83, 0, 91 },
				{ 100, 45, 87, 78, 121, 92, 91, 0 } };
		int[] loadTime = { 0, 106, 66, 111, 6, 63, 118 };
		int IE = 0;
		int OE = 0;
		int IF = 2;
		int OF = 3;
		final int portNum = 1;
		int stockNum = 1;
		int[] truckNum = { 4 };
		int loadCar = 5;
		int taskNum = 5;

		/** 顶点分为任务顶点和出发/返回顶点！ ***/
		// 任务顶点服务时间
		double[] serviceTime = new double[taskNum];
		// 任务顶点活动时间
		double[][] nodeTW = new double[2][taskNum];
		// 弧转换时间
		double[][] transTime = new double[taskNum + stockNum][taskNum
				+ stockNum];

		/**
		 * 2、条件，用来确定服务时间、顶点活动时间、顶点转换时间的值
		 */
		ComputeUtil.condition(taskNum, IE, OE, IF, portNum, stockNum, loadCar,
				loadTime, tw1, tw2, driveTime, serviceTime, nodeTW, transTime);

		/**
		 * 3、 数学模型
		 */

		try {
			IloCplex cplex = new IloCplex();

			// 决策变量Xij
			IloNumVar[][] X = new IloNumVar[stockNum + taskNum][];
			for (int i = 0; i < stockNum + taskNum; i++) {
				// 定义决策变量的范围
				// 每一个X[i]都有stockNum + taskNum个数
				X[i] = cplex.boolVarArray(stockNum + taskNum);
			}
			// 决策变量Yij
			IloNumVar[] Y = cplex.numVarArray(taskNum, Double.MIN_VALUE,
					Double.MAX_VALUE);

			// 目标函数
			IloNumExpr[][] expr0 = new IloNumExpr[taskNum][stockNum];
			IloNumExpr[][] expr1 = new IloNumExpr[stockNum][taskNum];
			for (int i = 0; i < taskNum; i++) {
				for (int j = 0; j < stockNum; j++) {
					expr0[i][j] = cplex.prod(
							cplex.sum(Y[i], transTime[i + stockNum][j]
									+ serviceTime[i]), X[i][j]);
				}
			}
			for (int i = 0; i < stockNum; i++) {
				for (int j = 0; j < taskNum; j++) {
					// if (i != j)
					expr1[i][j] = cplex.prod(
							cplex.diff(Y[j], transTime[i][j + stockNum]),
							X[i][j]);
				}
			}
			IloNumExpr[] expr2 = new IloNumExpr[taskNum];
			IloNumExpr[] expr3 = new IloNumExpr[stockNum];
			for (int i = 0; i < taskNum; i++) {
				expr2[i] = cplex.sum(expr0[i]);
			}
			for (int i = 0; i < stockNum; i++) {
				expr3[i] = cplex.sum(expr1[i]);
			}
			IloNumExpr exprObj = cplex.diff(cplex.sum(expr2), cplex.sum(expr3));

			// IloNumExpr[][] expr0 = new IloNumExpr[stockNum +
			// taskNum][stockNum
			// + taskNum];
			// IloNumExpr[] expr1 = new IloNumExpr[stockNum + taskNum];
			// for (int i = 0; i < stockNum + taskNum; i++) {
			// for (int j = 0; j < stockNum + taskNum; j++) {
			// // if (i != j)
			// expr0[i][j] = cplex.prod(transTime[i][j], X[i][j]);
			// }
			// }
			//
			// for (int i = 0; i < stockNum + taskNum; i++) {
			// expr1[i] = cplex.sum(expr0[i]);
			// }
			//
			// IloNumExpr exprObj = cplex.sum(expr1);

			// 车辆数约束
			IloNumExpr[][] tempCar0 = new IloNumExpr[stockNum][taskNum];
			for (int i = 0; i < stockNum; i++) {
				for (int j = 0; j < taskNum; j++) {
					tempCar0[i][j] = X[i][j + stockNum];
				}
			}
			IloNumExpr[] tempCar1 = new IloNumExpr[stockNum];
			for (int i = 0; i < stockNum; i++) {
				tempCar1[i] = cplex.sum(tempCar0[i]);
				cplex.addLe(tempCar1[i], truckNum[i]);
			}

			// 流平衡约束
			IloNumExpr[][] tempBal0 = new IloNumExpr[taskNum][taskNum
					+ stockNum];
			IloNumExpr[] tempBal1 = new IloNumExpr[taskNum];
			IloNumExpr[] tempBal2 = new IloNumExpr[taskNum];
			for (int i = 0; i < taskNum; i++) {
				for (int j = 0; j < taskNum + stockNum; j++) {
					// a01 = x10
					// a02 = x20
					// a03 = x30
					tempBal0[i][j] = X[j][i + stockNum];
				}
			}
			for (int i = 0; i < taskNum; i++) {
				tempBal1[i] = cplex.sum(tempBal0[i]);
				tempBal2[i] = cplex.sum(X[i + stockNum]);
			}
			for (int i = 0; i < taskNum; i++) {
				cplex.addEq(tempBal1[i], tempBal2[i]);
				cplex.addEq(1, tempBal1[i]);
				cplex.addEq(1, tempBal2[i]);
			}

			// 时间窗约束
			for (int i = 0; i < taskNum; i++) {
				cplex.addLe(nodeTW[0][i], Y[i]);
				cplex.addLe(Y[i], nodeTW[1][i]);
			}

			// 时间连续性约束，serviceTimes只用taskNum个
			int M = Integer.MAX_VALUE;
			for (int i = 0; i < taskNum; i++) {
				for (int j = 0; j < taskNum; j++) {
					// if (i != j)
					// model.add(TB[i]+ET[i]+Tran_time[i][j]-TB[j]<=(1-X[i][j])*M);
					cplex.addLe(
							cplex.diff(
									cplex.sum(Y[i], serviceTime[i]
											+ transTime[i + stockNum][j
													+ stockNum]),//
									Y[j]),
							cplex.prod(
									M,
									cplex.diff(1.0, X[i + stockNum][j
											+ stockNum])));
				}
			}

			/**
			 * 模型求解
			 */
			// 最小化目标函数
			cplex.addMaximize(exprObj);
			// 获取求解时间
			long startTime = System.currentTimeMillis();
			Boolean solveSuccess = cplex.solve();
			long endTime = System.currentTimeMillis();
			double time = endTime - startTime;

			if (solveSuccess) {
				System.out.println("-----------1、求解状态----------");
				System.out.println("Solution status: " + cplex.getStatus()
						+ "|||" + cplex.getAlgorithm());

				// 将解从模型中取出
				Double[][] x = new Double[stockNum + taskNum][stockNum
						+ taskNum];
				Double[] y = new Double[taskNum];
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						// if (i != j)
						x[i][j] = (Double) cplex.getValue(X[i][j]);
					}
				}
				for (int i = 0; i < taskNum; i++) {
					y[i] = cplex.getValue(Y[i]);
				}
				System.out.println("-----------2、求解时间----------");
				System.out.println("The total time is " + cplex.getObjValue());

				// 0是堆场。其余是客户。
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
						System.out.println("x[" + i + "]" + "[" + j + "]" + "="
								+ x[i][j]);
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
			}

			cplex.end();
		} catch (IloException e) {

			e.printStackTrace();

		}

		System.out.println("cplexUtil执行");

	}

}
