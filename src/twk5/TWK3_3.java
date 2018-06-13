package twk5;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

import org.junit.Test;

/**
 * xijk模型
 * 对车辆的约束不能线性化
 * @author Leon
 * 
 */
public class TWK3_3 {

	/**
	 * 数据处理
	 */
	double[][] tij;
	// c1 = 10;
	// c2 = 1;
	// c3 = 0;
	double timePeriod = 16;
	int tasks ;
	int stocks ;
	int trucks ;
	int[] stockTrucks;
	double[] loadTime ;
	double tMax;
	
	
	
	public static void main(String[] args) throws Exception {
		String name = "17";
//		String name = "3-4-2";
//		FileReader reader = new FileReader(new File("D:/twkdata/datafile"+name+".txt"));
		FileReader reader = new FileReader(new File("C:/Users/Administrator/Desktop/data/datafile"+name+".txt"));
		Generator g = new Generator();
		g.fileInput(reader);
		
		TWK3_3 t = new TWK3_3();
		t.tasks = g.numofTasks;
		t.timePeriod = g.timePeriod;
		t.stocks = g.stockNum;
		t.loadTime = g.packageTime;
		t.tij = g.tij;
		//车辆的设置必须使得全部都用到
		int[]  arr ={8,2};
		t.trucks = arr[0];
		t.stockTrucks =arr; 
//		t.tMax =3.48 ;
//		t.tMax =8 ;
//		System.out.println(t.timePeriod);

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
			// Build cplex
			IloCplex cplex = new IloCplex();

			IloIntVar[][][] X = new IloIntVar[tasks + stocks][tasks + stocks][trucks];
			IloNumVar[][][] Z = new IloNumVar[tasks + stocks][tasks + stocks][trucks];
			IloNumVar[][] Y = new IloNumVar[tasks][trucks];
			IloNumVar s = cplex.numVar(0, Integer.MAX_VALUE);
			// 定义决策变量
			for (int i = 0; i < X.length; i++) {
				for (int j = 0; j < X[0].length; j++) {
					for (int k = 0; k < X[0][0].length; k++) {
						X[i][j][k] = cplex.boolVar("X" + i + j + k);
						Z[i][j][k] = cplex.numVar(0,Integer.MAX_VALUE);
					}
				}
			}
			
			for(int k = 0;k<trucks;k++){
				for (int i = 0; i < tasks; i++) {
					Y[i][k] = cplex.numVar(0, timePeriod);
				}
			}
			
			for(int k = 0;k<trucks;k++){
				for(int i = 0;i < tasks/2;i++){
					for(int j = 0;j<stocks;j++){
						cplex.addGe(Y[i][k], tij[j][i+stocks]);
					}
				}
				for(int i = tasks/2;i < tasks;i++){
					for(int j = 0;j<stocks;j++){
						
						cplex.addLe(Y[i][k], timePeriod - tij[i+stocks][j]);
					}
				}
			}
			
			// 定义约束
			// 堆场车辆约束
			for (int i = 0; i < stocks; i++) {
				IloLinearNumExpr exprCar0 = cplex.linearNumExpr();// 创建一个约束表达式
				for(int k = 0;k<trucks;k++){
					for (int j = stocks; j < tasks + stocks; j++) {
						if (i != j) {
							exprCar0.addTerm(1.0, X[i][j][k]);
						}
					}
				}
				cplex.addLe(exprCar0, stockTrucks[i]);// 添加约束表达式
			}
			
			//一辆车只能从一个堆场出来，只能返回一个堆场
			for(int k = 0;k<trucks;k++){
				IloLinearNumExpr oneCarGo = cplex.linearNumExpr();
				IloLinearNumExpr oneCarBack = cplex.linearNumExpr();
				for(int i = 0;i<stocks;i++){
					for(int j = stocks;j<tasks + stocks;j++){
						oneCarGo.addTerm( 1.0, X[i][j][k]);
						oneCarBack.addTerm(1.0, X[j][i][k]);
					}
				}
//				cplex.addLe(oneCarGo, 1);
				cplex.addLe(oneCarGo, 1);
				cplex.addEq(oneCarGo, oneCarBack);
			}
			//一个顶点只能被访问一次
			System.out.println(stocks+","+tasks+stocks);
			for(int i = stocks;i<tasks+stocks;i++){
				IloLinearNumExpr oneNodeVist = cplex.linearNumExpr();
//				IloLinearNumExpr oneNodeVisted = cplex.linearNumExpr();
				for(int k = 0;k<trucks;k++){
					for(int j = 0;j < tasks + stocks;j++ ){
						if (i != j) {
						oneNodeVist.addTerm(1.0, X[i][j][k]);
//						oneNodeVisted.addTerm(1.0, X[j][i][k]);
						}
					}
				}
				cplex.addEq(1, oneNodeVist);
//				cplex.addEq(oneNodeVist, oneNodeVisted); 
			}
			
			//流平衡约束
			for(int j = stocks;j < stocks+tasks;j++){
				for(int k = 0;k<trucks;k++){
					IloLinearNumExpr balance = cplex.linearNumExpr();
					for(int i = 0;i < stocks+tasks;i++){
						if (i != j) {
							balance.addTerm(1.0, X[i][j][k]);
							balance.addTerm(-1.0, X[j][i][k]);
						}
					}
					cplex.addEq(0, balance);
				}
			}
			
			
			// 约束3 sequence constraint连续任务的节点时间顺序
			// by logical constraints
			// Yi - Yj + tij <= M(1-X[i][j])
			for(int k = 0;k<trucks;k++){
				for (int i = stocks; i < stocks + tasks; i++) {
					for (int j = 0; j < stocks + tasks; j++) {
						if (i != j 	) {
							IloLinearNumExpr con3 = cplex.linearNumExpr();
							con3.addTerm(1.0, Y[i - stocks][k]);
							con3.addTerm(-1.0, Z[i][j][k]);
							// ifThen(con1,con2):Returns a constraint that if con1
							// is true, then con2 must also be true.
							// con3 = Yj - Yi
							// Yj-Yi >=tij
							// if Xij被选中，那么Yj - Yi >= tij
							// tij用处理么？不用处理，因为刚好与之一一对应
							cplex.add(cplex.ifThen(cplex.eq(X[i][j][k], 1),cplex.le(con3,0)));
						}
					}
				}
				for (int i = 0; i < stocks + tasks; i++) {
					for (int j = stocks; j < stocks + tasks; j++) {
						if (i != j 	) {
							IloLinearNumExpr con3 = cplex.linearNumExpr();
							con3.addTerm(-1.0, Y[j - stocks][k]);
							con3.addTerm(1.0, Z[i][j][k]);
							cplex.add(cplex.ifThen(cplex.eq(X[i][j][k], 1),cplex.le(con3,-tij[i][j])));
						}
					}
				}
			}
			
			
			// 约束4 temporal constraint
			// d.taskNum>>1 是客户数量
			for(int k = 0;k < trucks;k++){
				for (int i = 0; i < (tasks >> 1); i++) {
					IloLinearNumExpr con4 = cplex.linearNumExpr();
					con4.addTerm(1.0, Y[(tasks >> 1) + i][k]);
					con4.addTerm(-1.0, Y[i][k]);
					cplex.addGe(con4, loadTime[i]);
				}
			}
			
			
//			//车辆行驶时间的约束
//			for(int k = 0;k < trucks;k++){
//				IloLinearNumExpr drivePersistence = cplex.linearNumExpr();
//				for(int i = 0;i < stocks+tasks;i++){
//					for(int j = 0;j<stocks+tasks;j++){				
//						if(i!=j)
//							drivePersistence.addTerm(tij[i][j], X[i][j][k]);						
//					}
//				}
//				cplex.addLe(drivePersistence, tMax);
//			}
			
			//车辆行驶时间的约束
			for(int k = 0;k < trucks;k++){
				IloLinearNumExpr drivePersistence = cplex.linearNumExpr();
				for (int i = stocks; i < tasks + stocks; i++) {
					for (int j = 0; j <stocks; j++) {
						if (j != i) {
							drivePersistence.addTerm(tij[i][j], X[i][j][k]);
							drivePersistence.addTerm(1.0, Z[i][j][k]);
						}
					}
				}
				for (int i = 0; i < stocks; i++) {
					for (int j = stocks; j <stocks+tasks; j++) {
						if (j != 0) {
							drivePersistence.addTerm(-1.0, Z[i][j][k]);
						}
					}
				}
				cplex.addLe(drivePersistence, s);
//				cplex.addLe(drivePersistence, tMax);
			}
			int M = 1000;
			// 对zij的约束
			for(int k = 0;k<trucks;k++){
				for (int i = 0; i < stocks; i++) {
					for (int j = 0; j < tasks; j++) {
						cplex.addLe(Z[i][j + stocks][k], cplex.prod(M, X[i][j + stocks][k]));
						cplex.addLe(cplex.prod(-1, Z[j + stocks][i][k]), cplex.prod(M, X[j + stocks][i][k]));
					}
				}
			}
			
			
			

			// 定义目标函数
			IloLinearNumExpr exprObj = cplex.linearNumExpr(); //

			for(int k = 0;k < trucks;k++){
//				IloLinearNumExpr drivePersistence = cplex.linearNumExpr();
				for (int i = stocks; i < tasks + stocks; i++) {
					for (int j = 0; j <stocks; j++) {
						if (j != i) {
							exprObj.addTerm(-tij[i][j], X[i][j][k]);
							exprObj.addTerm(-1.0, Z[i][j][k]);
						}
					}
				}
				for (int i = 0; i < stocks; i++) {
					for (int j = stocks; j <stocks+tasks; j++) {
						if (j != i) {
							exprObj.addTerm(1.0, Z[i][j][k]);
						}
					}
				}
//				cplex.addLe(drivePersistence, s);
//				cplex.addLe(drivePersistence, tMax);
				exprObj.addTerm(1.0,s);
			}

			cplex.addMinimize(exprObj);

			// cplex.exportcplex("D:/out.lp");

			// Solve cplex
			double starttime, endtime;
			starttime = System.currentTimeMillis();
			boolean hasSolve = cplex.solve();
			System.out.println("the question has best solution?"+hasSolve);
			if (hasSolve) {

				endtime = System.currentTimeMillis();
				double finalObj = cplex.getObjValue();
				double cpuTime = (endtime - starttime) / 1000;
				System.out.println("Solution status = " + cplex.getStatus());
				System.out.println("final objctive = " + myRound(finalObj,2));
				System.out.println("CPU time:	" + cpuTime);

				int[][][] sxij = new int[X.length][X[0].length][X[0][0].length];
				double[][][] szij = new double[X.length][X[0].length][X[0][0].length];
				for (int j = 0; j < sxij.length; j++) {
					for (int k = 0; k < sxij[0].length; k++) {
						for(int i = 0;i<sxij[0][0].length;i++){
							if (j != k) {
								
								
								if(j<stocks&& k<stocks){
									sxij[j][k][i] = 0;
								} else{
									if (cplex.getValue(X[j][k][i]) < 0.8) {
										sxij[j][k][i] = 0;
									} else {
										sxij[j][k][i] = 1;
										szij[j][k][i] = myRound(cplex.getValue(Z[j][k][i]),2);
									}
								}
								
								
								
							} else {
								sxij[j][k][i] = 0;
							}
						}
					}
				}
				
				System.out.println("用车最长时间");
				System.out.println(cplex.getValue(s));
				
				double[] syi = new double[Y.length];
				for(int k = 0;k<trucks;k++){
					for(int i = 0;i<Y.length;i++){
						syi[i] = cplex.getValue(Y[i][k]);
						
					}
				}
				
				// output fleet size
				int fleetSize = 0;
				for (int i = 0; i < stocks; i++) {
					for (int j = stocks; j < tasks + stocks; j++) {
						for(int k = 0; k < trucks;k++){
							fleetSize += sxij[i][j][k];
						}
					}
				}
				System.out.println("fleet size:	" + fleetSize);

				// output total traveling time
				double totalTravelTime = 0;
				for(int k = 0;k < sxij[0][0].length;k++){
					for (int i = 0; i < sxij.length; i++) {
						for (int j = 0; j < sxij[0].length; j++) {
							totalTravelTime += sxij[i][j][k] * tij[i][j];
						}
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
				for(int k = 0;k<sxij[0][0].length;k++){
					for (int i = stocks; i < tasks + stocks; i++) {
						for (int ss = 0; ss < stocks; ss++) {
							if (sxij[ss][i][k] == 1) {
								int temp = i;
								System.out.print("第"+k+"辆车:"+ss + "---" + temp+"("+tij[ss][temp]+")");
								do {
									for (int j = 0; j < tasks + stocks; j++) {
										if (sxij[temp][j][k] == 1) {
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
				}
				// output xij
				System.out.print("\rxij:\r");
				for(int k = 0;k<sxij[0][0].length;k++){
					System.out.println("第"+k+"辆车");
					for (int i = 0; i < sxij.length; i++) {
						for (int j = 0; j < sxij[0].length; j++) {
							System.out.print(sxij[i][j][k] + " ");
						}
						System.out.print("\r");
					}
				}
				System.out.print("\rzij:\r");
				for(int k = 0;k<sxij[0][0].length;k++){
					System.out.println("第"+k+"辆车");
					for (int i = 0; i < sxij.length; i++) {
						for (int j = 0; j < sxij[0].length; j++) {
							System.out.print(szij[i][j][k] + "\t");
						}
						System.out.print("\r");
					}
				}
				
				System.out.print("\rservice beginning time:\r");
				for(int k = 0; k < trucks;k++){
					for (int i = 0; i < tasks; i++) {
						System.out.print("y"+(i+stocks)+","+myRound(cplex.getValue(Y[i][k]),2) // i + 1 + ": " +
								+"\r");
					}
				}
				
				for(int i = 0 ; i < tasks/2; i++){
					
					System.out.println("loadTime"+(i+stocks)+loadTime[i]);
				}
					
				
			}
			cplex.end();
		} catch (IloException ex) {
			System.out.println("Concert Error: " + ex);
		}
	}

	public static double myRound(double v, int scale) {
		String temp = "#0.";
		for (int i = 0; i < scale; i++) {
			temp += "0";
		}
		return Double.valueOf(new java.text.DecimalFormat(temp).format(v))
				.doubleValue();
	}
}
