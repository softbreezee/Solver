package twt2;

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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * 基本接驳运输问题，xijk模型
 * 对车辆的约束不能线性化
 * @author Leon
 * 
 */
public class TWTSolver_2 {

	/**
	 * 数据处理
	 */
	public double[] loadTime;//装卸货的时间
	public int IE;
	public int OE;
	public int IF;
	public int OF;
	public int taskNum;
	public int portNum;//港口数量
	public int stockNum;//堆场数量
	public int[] truckNum;//堆场的卡车数
	public int trucks;
	public double loadCar = 0;//装卸车的时间为0
	public double tMax;//卡车外出的工作时间
	
	double[] serviceTime;// 任务顶点活动时间
	double[][] nodeTW;//顶点时间窗
	double[][] transTime;// 弧转换时间
	
	
	
	public TWTSolver_2() throws Exception{
		//初始化
		Generator g = new Generator();
		//读文件
		int str =11;
		File file = new File("D:/算例2可用数据/两堆场/datafile" + str + ".txt");
		FileReader r = new FileReader(file);
		g.fileInput(r);
		
		//写文件
//		g.generate(5,10, 0 ,5, 1,1);
//		g.fileOutput();
		IE = g.ieNum;
		OE = g.oeNum;
		IF = g.ifNum;
		OF = g.ofNum;
		taskNum = g.numofTasks;
		portNum = g.numofPort;
		stockNum = g.numofStock;
		serviceTime = g.serviceTime;
		nodeTW = g.nodeTW;
		transTime = g.tij;
		loadCar = g.loadCar;
		loadTime = g.loadTime;
	}

	public static void main(String[] args) throws Exception {
		TWTSolver_2 sovler = new TWTSolver_2();
		int[] arr = {3,6};
		sovler.truckNum = arr;
		sovler.tMax =8;
		sovler.trucks = arr[0]+arr[1];
		sovler.computing();
//		System.out.println(sovler.IE);
//		System.out.println(sovler.OE);
//		System.out.println(sovler.IF);
//		System.out.println(sovler.OF);
//		System.out.println(sovler.taskNum);
//		System.out.println(sovler.portNum);
//		System.out.println(sovler.stockNum);
//		System.out.println(sovler.serviceTime);
//		System.out.println(sovler.loadCar);
		
	}
	

	/**
	 * 1、从d中取出的任务应该是子任务的数量 2、d.driveTime没有处理，应该是定义的弧转换时间
	 * 3、假设4个客户任务（第一阶段任务与第二阶段任务）的对应关系是 0->4 1->5 2->6 3->7 4、
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void computing() {

		try {
			// Build cplex
			IloCplex cplex = new IloCplex();

			
//			 	cplex.setOut(os);
//				model.setOut(null);
//				model.setOut(new TimestampOutput());
				
//				model.use(new TimestampOutput((double)System.currentTimeMillis()));

			 	cplex.setParam(IloCplex.IntParam.MIPEmphasis, 0);

			 	cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
			 	cplex.setParam(IloCplex.IntParam.MIPInterval, 10000);

			 	cplex.setParam(IloCplex.DoubleParam.TiLim, 3600);

				// out of memory
			 	cplex.setParam(IloCplex.DoubleParam.WorkMem, 4096);
				// model.setParam(IloCplex.DoubleParam.TreLim, 3);
			 	cplex.setParam(IloCplex.IntParam.NodeFileInd, 3);
			 	cplex.setParam(IloCplex.StringParam.WorkDir, "data/result/out of memory");

				// Set the maximum number of threads to 1.
//				model.setParam(IloCplex.IntParam.Threads, 1);

				// model.setParam(IloCplex.DoubleParam.EpGap, 0);

				// no preprocessing
//				model.setParam(IloCplex.BooleanParam.PreInd, false);

				// Turn on traditional search for use with control callbacks
//				 model.setParam(IloCplex.IntParam.MIPSearch,
//				 IloCplex.MIPSearch.Traditional);
				//
				// model.setParam(IloCplex.IntParam.VarSel, 3);
			
			
			
			
			IloIntVar[][][] X = new IloIntVar[taskNum + stockNum][taskNum + stockNum][trucks];
			IloNumVar[][][] Z = new IloNumVar[taskNum + stockNum][taskNum + stockNum][trucks];
//			IloNumVar[][] Y = new IloNumVar[taskNum][trucks];
			IloNumVar[] Y = new IloNumVar[taskNum];
			// 定义决策变量
			for (int i = 0; i < X.length; i++) {
				for (int j = 0; j < X[0].length; j++) {
					for (int k = 0; k < X[0][0].length; k++) {
						X[i][j][k] = cplex.boolVar("X" + i + j + k);
					}
				}
			}
			
//			for(int k = 0;k<trucks;k++){
				for (int i = 0; i < taskNum; i++) {
//					Y[i][k] = cplex.numVar(0, Integer.MAX_VALUE, "y" + (i + 1));
					Y[i] = cplex.numVar(0, Integer.MAX_VALUE, "y" + (i + 1));
				}
//			}
			
			//定义z
			for (int i = 0; i < Z.length; i++) {
				for (int j = 0; j < Z[0].length; j++) {
					for (int k = 0; k < Z[0][0].length; k++) {
						Z[i][j][k] = cplex.numVar(0, Integer.MAX_VALUE);
					}
				}
			}
			
			
			// 定义约束
			// 堆场车辆约束
			for (int i = 0; i < stockNum; i++) {
				IloLinearNumExpr exprCar0 = cplex.linearNumExpr();// 创建一个约束表达式
				for(int k = 0;k<trucks;k++){
					for (int j = stockNum; j < taskNum + stockNum; j++) {
						if (i != j) {
							exprCar0.addTerm(1.0, X[i][j][k]);
						}
					}
				}
				cplex.addLe(exprCar0, truckNum[i]);// 添加约束表达式
			}
			
			//一辆车只能从一个堆场出来，只能返回一个堆场
			for(int k = 0;k<trucks;k++){
				IloLinearNumExpr oneCarGo = cplex.linearNumExpr();
				IloLinearNumExpr oneCarBack = cplex.linearNumExpr();
				for(int i = 0;i<stockNum;i++){
					for(int j = stockNum;j<taskNum + stockNum;j++){
						oneCarGo.addTerm( 1.0, X[i][j][k]);
						oneCarBack.addTerm(1.0, X[j][i][k]);
					}
				}
//				cplex.addLe(oneCarGo, 1);
				cplex.addLe(oneCarGo, 1);
				cplex.addEq(oneCarGo, oneCarBack);
			}
			//一个顶点只能被访问一次
			System.out.println(stockNum+","+taskNum+stockNum);
			for(int i = stockNum;i<taskNum+stockNum;i++){
				IloLinearNumExpr oneNodeVist = cplex.linearNumExpr();
//				IloLinearNumExpr oneNodeVisted = cplex.linearNumExpr();
				for(int k = 0;k<trucks;k++){
					for(int j = 0;j < taskNum + stockNum;j++ ){
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
			for(int j = stockNum;j < stockNum+taskNum;j++){
				for(int k = 0;k<trucks;k++){
					IloLinearNumExpr balance = cplex.linearNumExpr();
					for(int i = 0;i < stockNum+taskNum;i++){
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
				for (int i = stockNum; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						if (i != j 	) {
							IloLinearNumExpr con3 = cplex.linearNumExpr();
							con3.addTerm(1.0, Y[i - stockNum]);
							con3.addTerm(-1.0, Z[i][j][k]);
							// ifThen(con1,con2):Returns a constraint that if con1
							// is true, then con2 must also be true.
							// con3 = Yj - Yi
							// Yj-Yi >=tij
							// if Xij被选中，那么Yj - Yi >= tij
							// tij用处理么？不用处理，因为刚好与之一一对应
							cplex.add(cplex.ifThen(cplex.eq(X[i][j][k], 1),cplex.le(con3,-serviceTime[i-stockNum])));
						}
					}
				}
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = stockNum; j < stockNum + taskNum; j++) {
						if (i != j 	) {
							IloLinearNumExpr con3 = cplex.linearNumExpr();
							con3.addTerm(-1.0, Y[j - stockNum]);
							con3.addTerm(1.0, Z[i][j][k]);
							cplex.add(cplex.ifThen(cplex.eq(X[i][j][k], 1),cplex.le(con3,-transTime[i][j])));
						}
					}
				}
			}
			
			// 约束4 时间窗约束
//			for(int k = 0;k<trucks;k++){
				for (int i = 0; i < taskNum; i++) {
					cplex.addLe(nodeTW[0][i], Y[i]);
					cplex.addLe(Y[i], nodeTW[1][i]);
				}
//			}
			
			
			
			//车辆行驶时间的约束 
			for(int k = 0;k < trucks;k++){
				IloLinearNumExpr drivePersistence = cplex.linearNumExpr();
				for (int i = stockNum; i < taskNum + stockNum; i++) {
					for (int j = 0; j <stockNum; j++) {
						if (j != i) {
							drivePersistence.addTerm(transTime[i][j], X[i][j][k]);
							drivePersistence.addTerm(1.0, Z[i][j][k]);
						}
					}
				}
				for (int i = 0; i < stockNum; i++) {
					for (int j = stockNum; j <stockNum+taskNum; j++) {
						if (j != 0) {
							drivePersistence.addTerm(-1.0, Z[i][j][k]);
						}
					}
				}
				cplex.addLe(drivePersistence, tMax);
			}
			
			
			
			int M = 1000;
			// 对zij的约束
			for(int k = 0;k<trucks;k++){
				for (int i = 0; i < stockNum; i++) {
					for (int j = 0; j < taskNum; j++) {
						cplex.addLe(Z[i][j + stockNum][k], cplex.prod(M, X[i][j + stockNum][k]));
						cplex.addLe(cplex.prod(-1, Z[j + stockNum][i][k]), cplex.prod(M, X[j + stockNum][i][k]));
					}
				}
			}
				
			// 定义目标函数
			IloLinearNumExpr exprObj = cplex.linearNumExpr(); //

			for(int k = 0;k<trucks;k++){
				for (int i = stockNum; i < taskNum + stockNum; i++) {
					for (int j = 0; j <stockNum; j++) {
						if (j != i) {
							exprObj.addTerm(transTime[i][j], X[i][j][k]);
							exprObj.addTerm(1.0, Z[i][j][k]);
						}
					}
				}
			}
			for(int k = 0;k<trucks;k++){
				for (int i = 0; i < stockNum; i++) {
					for (int j = stockNum; j <stockNum+taskNum; j++) {
						if (j != 0) {
							exprObj.addTerm(-1.0, Z[i][j][k]);
						}
					}
				}
			}

			cplex.addMinimize(exprObj);

			//cplex.exportcplex("D:/out.lp");

			/**
			 * 模型求解
			 */
			// 获取求解时间
			double starttime, enime;
			starttime = System.currentTimeMillis();
			boolean yes = cplex.solve();
			System.out.println(yes);
			System.out.println(tMax);
			System.out.println(truckNum[0]);
			
			
			if (yes) {
				System.out.println("Solution status: " + cplex.getStatus() + "|||" + cplex.getAlgorithm());
//				System.out.println("serviceTime[10]"+serviceTime[9]);

				enime = System.currentTimeMillis();
				double finalObj = cplex.getObjValue();
				double cpuTime = (enime - starttime) / 1000;
				System.out.println("Solution status = " + cplex.getStatus());
				System.out.println("final objctive = " + myRound(finalObj,2));
				System.out.println("CPU time:	" + cpuTime);
				
				
				
				

				int[][][] sxij = new int[X.length][X[0].length][X[0][0].length];
				double[][][] szij = new double[X.length][X[0].length][X[0][0].length];
				for (int j = 0; j < sxij.length; j++) {
					for (int k = 0; k < sxij[0].length; k++) {
						for(int i = 0;i<sxij[0][0].length;i++){
							if (j != k) {
								
								
								if(j<stockNum && k<stockNum){
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
				
				
				// output fleet size
				int fleetSize = 0;
				for(int k = 0;k<trucks;k++){
					for(int i = 0;i<stockNum;i++ ){
						for (int j = stockNum; j < taskNum + stockNum; j++) {
							fleetSize += sxij[i][j][k];
						}
					}
				}
				System.out.println("fleet size:	" + fleetSize);

				// output total traveling time
				double totalTravelTime = 0;
				for(int k = 0;k<trucks;k++){
					for (int i = 0; i < sxij.length; i++) {
						for (int j = 0; j < sxij[0].length; j++) {
							totalTravelTime += sxij[i][j][k] * transTime[i][j];
						}
					}
				}
				System.out.println("Total Travel time:	" + totalTravelTime);
				
				
				// output xijk
				
				for(int k = 0;k< X[0][0].length;k++){
					System.out.println("第"+k+"各路径");
					for (int i = 0; i < X.length; i++) {
						System.out.print(i+"\t");
						for (int j = 0; j < X[0].length; j++) {
							if(i!=j)
//							System.out.print("X"+i+j+k+"="+cplex.getValue(X[i][j][k]));
//							System.out.print("X"+i+j+k+"="+cplex.getValue(X[i][j][k])+"("+myRound(cplex.getValue(Z[i][j][k]),2)+")"+"\t");
							System.out.print("X"+i+j+k+"="+sxij[i][j][k]+"("+szij[i][j][k]+")"+"\t");
							else
								System.out.print("X"+i+j+k+"="+0.0+"("+0.0+")"+"\t");
						}
						System.out.println();
					}
					System.out.println();
				}
				// output yik
				System.out.print("\rservice beginning time:\r");
//				for(int k = 0;k< Y[0].length;k++){
//					System.out.println("第"+k+"各路径");
					for (int i = 0; i < Y.length; i++) {
//						System.out.print("Y"+(i+stockNum)+"="+myRound(cplex.getValue(Y[i][k]),2)+"\t");
						System.out.println("Y"+(i+stockNum)+"="+myRound(cplex.getValue(Y[i]),2)+"\t");
					}
//					System.out.println();
//				}
				
				



//				Integer[] arr = new Integer[stockNum];
//				for(int i = 0;i<arr.length ;i++){
//					arr[i] = new Integer(i);
//				}	
//				 System.out.print("\rroutes:\r");
//				 for (int i = stockNum; i < taskNum + stockNum; i++) {
//					 for(int k = 0;k < stockNum;k++){
//						 if (sxij[k][i] == 1) {
//							 int temp = i;
//							 System.out.print(k + "---" + temp+"("+szij[k][temp]+")");
//							  {
//								 for (int j = 0; j < taskNum + stockNum; j++) {
//									 if (sxij[temp][j] == 1) {
//										 System.out.print("---" + j+"("+szij[temp][j]+")");
//										 temp = j;
//										 break;
//									 }
//								 }
////							 } while (temp != k);
//							 } while (!Arrays.asList(arr).contains(temp));
//							 System.out.print("\r");
//						 }
//					 }
//				 }
				
				
				

				
				

//				// output xij
//				 System.out.print("\rxij:\r");
//				 for (int i = 0; i < sxij.length; i++) {
//					 for (int j = 0; j < sxij[0].length; j++) {
//						 System.out.print(sxij[i][j] + " ");
//					 }
//					 System.out.print("\r");
//				 }
			

			System.out.println("-------------5、转换时间-------------");

			for (int i = 0; i < stockNum + taskNum; i++) {
				for (int j = 0; j < stockNum + taskNum; j++) {
					System.out.print(myRound(transTime[i][j],2)+"\t");
				}
				System.out.println();
			}

			System.out.println("-------------6、节点活动时间-------------");

			for (int j = 0; j < taskNum; j++) {

				for (int i = 0; i < 2; i++) {
					System.out.print("[" + i + "][" + j + "]" + "=" + nodeTW[i][j] + "\t");
				}
			}
				
				
	
			cplex.end();
			}
		} catch (Exception e) {

			e.printStackTrace();

		}

		System.out.println("cplexUtil执行");

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
	
	@Test
	public void test2(){
		int[] arr = {1,2,3,4};
		Set<Set<Integer>> set = getSubSet(arr);
		for(Set<Integer> s:set){
			for(Integer i:s){
				System.out.print(i+"-");
			}
			System.out.println();
		}
	}

}
