package twk5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * 
 * 含zij模型 验证正确
 * 
 * @author Leon
 * 
 */
public class TWKSovler2 {

	

	/**
	 * 数据处理
	 */
	double[][] tij;
	double timePeriod = 24;
	int tasks ;
	int stocks ;
	int[] stockTrucks;
	double[] loadTime ;
	double tMax;
	
	public static void main(String[] args) throws Exception {
		String name = "2";
//		String name = "1-2-1";
//		String name = "1-2-1";
//		FileReader reader = new FileReader(new File("D:/twkdata/datafile"+name+".txt"));
		FileReader reader = new FileReader(new File("C:/Users/Administrator/Desktop/data/datafile"+name+".txt"));
		Generator g = new Generator();
		g.fileInput(reader);
		
		TWKSovler2 t = new TWKSovler2();
		t.tasks = g.numofTasks;
//		t.timePeriod = g.timePeriod;
		t.stocks = g.stockNum;
		t.loadTime = g.packageTime;
		t.tij = g.tij;
		//
		int[]  arr ={5,2};
		t.stockTrucks =arr; 
		t.tMax =g.timePeriod;

		/**
		 * xue的数据顺序是出口，进口
		 * ofif
		 */
		t.maincplex();
		
		
	}

	/**
	 * 1、从d中取出的任务应该是子任务的数量 2、d.driveTime没有处理，应该是定义的弧转换时间
	 * 3、假设4个客户任务（第一阶段任务与第二阶段任务）的对应关系是 0->4 1->5 2->6 3->7 4、
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void maincplex() {

		try {
			/**
			 * 模型建立
			 */
			// Build cplex
			IloCplex cplex = new IloCplex();

			/* 此处删去对cplex的设置及cplex输出为一个cvs文件 */

			// 和文章中tij表格格式一致,这里的任务数量应该是【子】任务数量
			IloIntVar[][] X = new IloIntVar[tasks + stocks][tasks + stocks];
			// IloNumVar[][] xij = new IloNumVar[numofTasks + 1][numofTasks +
			// 1];
			IloNumVar[][] Z = new IloNumVar[tasks + stocks][tasks + stocks];
			IloNumVar[] Y = new IloNumVar[tasks];

			// 定义决策变量
			for (int i = 0; i < X.length; i++) {
				for (int j = 0; j < X[0].length; j++) {
					// boolVar里面的String参数是标识？
					X[i][j] = cplex.boolVar("X" + i + j);
					Z[i][j] = cplex.numVar(0, Integer.MAX_VALUE, "z" + i + j);

				}
			}

			for (int i = 0; i < Y.length; i++) {
				Y[i] = cplex.numVar(0, Integer.MAX_VALUE, "y" + (i + 1));
			}

			// 定义约束
			// 堆场车辆约束
			for (int i = 0; i < stocks; i++) {
				IloLinearNumExpr exprCar0 = cplex.linearNumExpr();// 创建一个约束表达式
				for (int j = stocks; j < tasks + stocks; j++) {
					if (i != j) {
						exprCar0.addTerm(1.0, X[i][j]);
					}
				}
				cplex.addLe(exprCar0, stockTrucks[i]);// 添加约束表达式
			}

			// 流平衡约束
			for (int j = stocks; j < stocks + tasks; j++) {
				IloLinearNumExpr exprBal0 = cplex.linearNumExpr();
				IloLinearNumExpr exprBal1 = cplex.linearNumExpr();
				for (int i = 0; i < stocks + tasks; i++) {
					if (i != j) {
						exprBal0.addTerm(1.0, X[i][j]);
						exprBal1.addTerm(1.0, X[j][i]);
					}
				}
				cplex.addEq(exprBal0, 1);
				cplex.addEq(exprBal0, exprBal1);
			}

			// 约束3 temporal constraint
			// tasks>>1 是客户数量
			for (int i = 0; i < (tasks >> 1); i++) {
				IloLinearNumExpr con4 = cplex.linearNumExpr();
				con4.addTerm(1.0, Y[(tasks >> 1) + i]);
				con4.addTerm(-1.0, Y[i]);
				cplex.addGe(con4, loadTime[i]);
			}

			// 约束45 sequence constraint连续任务的节点时间顺序
			// by logical constraints
			for (int i = stocks; i < stocks + tasks; i++) {
				for (int j = 0; j < stocks + tasks; j++) {
					if (i != j // && (i + numofCustomers) != j
								// && (i - numofCustomers) != j
					) {
						IloLinearNumExpr con3 = cplex.linearNumExpr();
						con3.addTerm(1.0, Z[i][j]);
						con3.addTerm(-1.0, Y[i - stocks]);
						cplex.add(cplex.ifThen(cplex.eq(X[i][j], 1),
								cplex.ge(con3, 0)));

					}
				}
			}
			for (int i = 0; i < stocks + tasks; i++) {
				for (int j = stocks; j < stocks + tasks; j++) {
					if (i != j // && (i + numofCustomers) != j
					// && (i - numofCustomers) != j
					) {
						IloLinearNumExpr con3 = cplex.linearNumExpr();
						con3.addTerm(1.0, Y[j - stocks]);
						con3.addTerm(-1.0, Z[i][j]);
						cplex.add(cplex.ifThen(cplex.eq(X[i][j], 1),
								cplex.ge(con3, tij[i][j])));

					}
				}
			}

			// 约束yi>=tji
			for (int i = 0; i < (tasks >> 1); i++) {
				for (int j = 0; j < stocks; j++) {
					cplex.addGe(Y[i], tij[j][i + stocks]);

				}
			}
			// 约束yi+tij <= T
			for (int i = (tasks >> 1); i < tasks; i++) {
				for (int j = 0; j < stocks; j++) {

					cplex.addLe(Y[i], timePeriod - tij[i + stocks][j]);
				}
			}

			int M = 1000;
			// 对于司机的工作时间约束
			int[] arrC = new int[stocks];
			for (int i = 0; i < stocks; i++) {
				arrC[i] = i;
			}
			Set<Set<Integer>> subSet = getSubSet(arrC);// 包含空集
			for (Set<Integer> s : subSet) {
				if (s.size() != 0) {
					// 几个集合几个约束
					IloLinearNumExpr expr0 = cplex.linearNumExpr();
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					for (Integer indexInS : s) {
						for (int i = 0; i < stocks; i++) {
							expr0.addTerm(1.0,Z[indexInS+stocks][i]);
							expr0.addTerm(tij[indexInS + stocks][i], X[indexInS+ stocks][i]);
						}
						for (int i = 0; i < stocks; i++) {
							expr0.addTerm(-1.0,Z[i][indexInS+stocks]);
						}
						for (int inInS : s) {
							expr1.addTerm(M, X[indexInS + stocks][inInS + stocks]);
						}
					}
					cplex.ifThen(cplex.eq(s.size()-1, expr1), cplex.le(expr0, tMax));
//					cplex.addLe(expr, tMax+M*(s.size()-1));
				}
				// 添加
			}

			// 对zij的约束
			for (int i = 0; i < stocks; i++) {
				for (int j = 0; j < tasks; j++) {
					cplex.addLe(Z[i][j + stocks], cplex.prod(M, X[i][j + stocks]));
					cplex.addLe(cplex.prod(-1, Z[j + stocks][i]), cplex.prod(M, X[j + stocks][i]));

				}
			}

			// 定义目标函数
			IloLinearNumExpr exprObj = cplex.linearNumExpr(); //
			for (int i = 0; i < tasks + stocks; i++) {
				for (int j = 0; j < tasks + stocks; j++) {
					if (j != i) {
						exprObj.addTerm(tij[i][j], X[i][j]);
						
					}
				}
			}
//			for (int i = stocks; i < tasks+stocks; i++) {
//				for (int j = 0; j <stocks; j++) {
//					if (j != i) {
//						
//						exprObj.addTerm(1.0, Z[i][j]);
//						exprObj.addTerm(tij[i][j],X[i][j]);
//					}
//				}
//			}
//			for (int i = 0; i < stocks; i++) {
//				for (int j = stocks; j <stocks+tasks; j++) {
//					if (j != i) {
//						
//						exprObj.addTerm(-1.0, Z[i][j]);
//					}
//				}
//			}


			cplex.addMinimize(exprObj);

//			cplex.exportcplex("D:/out.lp");

			// Solve cplex
			double starttime, endtime;
			starttime = System.currentTimeMillis();
			boolean yes = cplex.solve();
			System.out.println(yes);
			System.out.println("custormers" + tasks);

			if (yes) {
				System.out.println("Solution status: " + cplex.getStatus()
						+ "|||" + cplex.getAlgorithm());

				endtime = System.currentTimeMillis();
				double finalObj = cplex.getObjValue();
				double cpuTime = (endtime - starttime) / 1000;
				System.out.println("Solution status = " + cplex.getStatus());
				System.out.println("final objctive = " + finalObj);
				System.out.println("CPU time:	" + cpuTime);

				int[][] sxij = new int[X.length][X[0].length];
				double[][] szij = new double[X.length][X[0].length];
				for (int i = 0; i < sxij.length; i++) {
					for (int j = 0; j < sxij[0].length; j++) {
						if (i != j) {
							if (cplex.getValue(X[i][j]) < 0.1) {
								sxij[i][j] = 0;
							} else {
								sxij[i][j] = 1;
								szij[i][j] = myRound(cplex.getValue(Z[i][j]), 2);
							}
						} else {
							sxij[i][j] = 0;
						}
					}
				}

				// output fleet size
				int fleetSize = 0;
				for (int i = 0; i < stocks; i++) {
					for (int j = stocks; j < tasks + stocks; j++) {
						fleetSize += sxij[i][j];
					}
				}
				System.out.println("fleet size:	" + fleetSize);

				// output total traveling time
				double totalTravelTime = 0;
				for (int i = 0; i < sxij.length; i++) {
					for (int j = 0; j < sxij[0].length; j++) {
						totalTravelTime += sxij[i][j] * tij[i][j];
					}
				}
				System.out.println("Total Travel time:	" + totalTravelTime);

				// output saving containers
				// int savingContainers = 0;
				// for (int i = 0; i < numofDelivery; i++) {
				// for (int j = 0; j < numofPickup; j++) {
				// savingContainers += sxij[1 + numofCustomers
				// + numofPickup + i][1 + j];
				// }
				// }
				// System.out.print("\rsaving containers: " + savingContainers
				// + "\r");
				Integer[] arr = new Integer[stocks];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = new Integer(i);
				}
				// output routes

				System.out.print("\rroutes:\r");
				for (int i = stocks; i < tasks + stocks; i++) {
					for (int k = 0; k < stocks; k++) {
						if (sxij[k][i] == 1) {
							int temp = i;
							System.out.print(k + "---" + temp +"("+tij[k][temp]+")");
							do {
								for (int j = 0; j < tasks + stocks; j++) {
									if (sxij[temp][j] == 1) {
										System.out.print("---" + j+"("+tij[temp][j]+")");
										temp = j;
										break;
									}
								}
								// } while (temp != k);
							} while (!Arrays.asList(arr).contains(temp));
							System.out.print("\r");
						}
					}
				}

				// output xij
				System.out.print("\rxij:\r");
				for (int i = 0; i < sxij.length; i++) {
					for (int j = 0; j < sxij[0].length; j++) {
						System.out.print(sxij[i][j] + " ");
					}
					System.out.print("\r");
				}

				System.out.print("\rservice beginning time:\r");
				for (int i = 0; i < tasks; i++) {
					System.out.print(cplex.getValue(Y[i]) // i + 1 + ": " +
							+ "\r");
				}

			}
			cplex.end();
		} catch (IloException ex) {
			System.out.println("Concert Error: " + ex);
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

	// 求子集的方法
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
	
}
