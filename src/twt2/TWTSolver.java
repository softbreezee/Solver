package twt2;


import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import org.junit.Test;





/**
 * 计算工具,cplex
 * 
 * 任务的排列：IE OE IF OF ie0 ie1 ie2 oe0 oe1 if0 if1 of0 of1 of2 ...
 * OV图上的顶点排列顺序是出发/返回顶点、IE、OE、IF、OF
 * 
 * 实际中的顶点排列顺序是港口、堆场、客户
 * 重箱任务的数量要等于客户的数量！！
 * @author Leon
 * 
 */
public class TWTSolver {
//	public double[][] driveTime;//实际的两个点之间的行驶时间
	public double[] loadTime;//装卸货的时间
	public int IE;
	public int OE;
	public int IF;
	public int OF;
	public int taskNum;
	public int portNum;//港口数量
	public int stockNum;//堆场数量
	public int[] truckNum;//堆场的卡车数
	public double loadCar = 0;//装卸车的时间为0
	public double tMax;//卡车外出的工作时间
	
	double[] serviceTime;// 任务顶点活动时间
	double[][] nodeTW;//顶点时间窗
	double[][] transTime;// 弧转换时间
	
	public TWTSolver() throws Exception{
		//初始化
		Generator g = new Generator();
		//读文件
		int str =8;
		File file = new File("D:/算例2可用数据/两堆场/datafile" + str + ".txt");
		FileReader r = new FileReader(file);
		g.fileInput(r);
		
		//写文件
		//of if ie oe
//		g.generate(2,3, 0,4, 1,2);
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
		TWTSolver sovler = new TWTSolver();
		int[] arr = {2,1,20};
		sovler.truckNum = arr;
		sovler.tMax =8;
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

	@Test
	public void computing() {
		try {

			IloCplex cplex = new IloCplex();
			/* 此处删去对cplex的设置及cplex输出为一个cvs文件 */

			IloIntVar[][] X = new IloIntVar[taskNum + stockNum][taskNum + stockNum];
			// IloNumVar[][] xij = new IloNumVar[numofTasks + 1][numofTasks +
			// 1]
			IloNumVar[][] Z = new IloNumVar[taskNum + stockNum][taskNum + stockNum];
			IloNumVar[] Y = new IloNumVar[taskNum];

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
//			for (int i = 0; i < Y.length; i++) {
//				Y[i] = cplex.numVar(0, tMax, "y" + (i + 1));
//			}

			// 定义约束
			// 堆场车辆约束
			for (int i = 0; i < stockNum; i++) {
				IloLinearNumExpr exprCar0 = cplex.linearNumExpr();// 创建一个约束表达式
				for (int j = stockNum; j < taskNum + stockNum; j++) {
					if (i != j) {
						exprCar0.addTerm(1.0, X[i][j]);
					}
				}
				cplex.addLe(exprCar0, truckNum[i]);// 添加约束表达式
			}

			// 流平衡约束
			for (int j = stockNum; j < stockNum + taskNum; j++) {
				IloLinearNumExpr exprBal0 = cplex.linearNumExpr();
				IloLinearNumExpr exprBal1 = cplex.linearNumExpr();
				for (int i = 0; i < stockNum + taskNum; i++) {
					if (i != j) {
						exprBal0.addTerm(1.0, X[i][j]);
						exprBal1.addTerm(1.0, X[j][i]);
					}
				}
				cplex.addEq(exprBal0, 1);
				cplex.addEq(exprBal0, exprBal1);
			}
			// 时间窗约束
			for (int i = 0; i < taskNum; i++) {
				cplex.addLe(nodeTW[0][i], Y[i]);
				cplex.addLe(Y[i], nodeTW[1][i]);
			}


			// 约束45 sequence constraint连续任务的节点时间顺序
			// by logical constraints
			for (int i = stockNum; i < stockNum + taskNum; i++) {
				for (int j = 0; j < stockNum + taskNum; j++) {
					if (i != j // && (i + numofCustomers) != j
								// && (i - numofCustomers) != j
					) {
						IloLinearNumExpr con3 = cplex.linearNumExpr();
						con3.addTerm(-1.0, Y[i - stockNum]);
						con3.addTerm(1.0, Z[i][j]);
						cplex.add(cplex.ifThen(cplex.eq(X[i][j], 1),cplex.ge(con3, serviceTime[i-stockNum])));
					}
				}
			}
			for (int i = 0; i < stockNum + taskNum; i++) {
				for (int j = stockNum; j < stockNum + taskNum; j++) {
					if (i != j // && (i + numofCustomers) != j
					// && (i - numofCustomers) != j
					) {
						IloLinearNumExpr con3 = cplex.linearNumExpr();
						con3.addTerm(1.0, Y[j - stockNum]);
						con3.addTerm(-1.0, Z[i][j]);
						cplex.add(cplex.ifThen(cplex.eq(X[i][j], 1), cplex.ge(con3, transTime[i][j])));

					}
				}
			}


			int M = 1000;
			// 对于司机的工作时间约束
//			int[] arrC = new int[taskNum];
//			for (int i = 0; i < taskNum; i++) {
//				arrC[i] = i;
//			}
//			Set<Set<Integer>> subSet = getSubSet(arrC);// 包含空集
//			for (Set<Integer> s : subSet) {
//				if (s.size() != 0) {
//					// 几个集合几个约束
//					IloLinearNumExpr expr0 = cplex.linearNumExpr();
//					IloLinearNumExpr expr1 = cplex.linearNumExpr();
//					for (Integer indexInS : s) {
//						for (int i = 0; i < stockNum; i++) {
//							expr0.addTerm(1.0,Z[indexInS+stockNum][i]);
//							expr0.addTerm(transTime[indexInS + stockNum][i], X[indexInS+ stockNum][i]);
//						}
//						for (int i = 0; i < stockNum; i++) {
//							expr0.addTerm(-1.0,Z[i][indexInS+stockNum]);
//						}
//						for (int inInS : s) {
//							expr1.addTerm(M, X[indexInS + stockNum][inInS + stockNum]);
//						}
//					}
//					cplex.ifThen(cplex.eq(s.size()-1, expr1), cplex.le(expr0, tMax));
////					cplex.addLe(expr, tMax+M*(s.size()-1));
//				}
//				// 添加
//			}
			
			double sstartTiem = System.currentTimeMillis();
			int[] arrC = new int[taskNum];
			for (int i = 0; i < taskNum; i++) {
				arrC[i] = i;
			}
			Set<Set<Integer>> subSet = getSubSet(arrC);// 包含空集
			double eendTime = System.currentTimeMillis();
			double ssTime = (eendTime - sstartTiem) / 1000;
			
			
			for (Set<Integer> s : subSet) {
				if (s.size() != 0) {
					// 几个集合几个约束
					IloLinearNumExpr expr = cplex.linearNumExpr();
					for (Integer indexInS : s) {
						for (int i = 0; i < stockNum; i++) {
							expr.addTerm(1.0,Z[indexInS+stockNum][i]);
							expr.addTerm(transTime[indexInS + stockNum][i], X[indexInS+ stockNum][i]);
						}
						for (int i = 0; i < stockNum; i++) {
							expr.addTerm(-1.0,Z[i][indexInS+stockNum]);
						}
						for (int inInS : s) {
							expr.addTerm(M, X[indexInS + stockNum][inInS + stockNum]);
						}
					}
					
					cplex.addLe(expr, tMax+M*(s.size()-1));
				}
				// 添加
			}

			
			
//			// 对于司机的工作时间约束
//			int[] arrC = new int[taskNum];
//			for (int i = 0; i < taskNum; i++) {
//				arrC[i] = i;
//			}
//			Set<Set<Integer>> subSet = getSubSet(arrC);// 包含空集
//			for (Set<Integer> s : subSet) {
//				if (s.size() != 0) {
//					// 几个集合几个约束
//					IloNumExpr[] z00 = new IloNumExpr[s.size()];
//					IloNumExpr[] z11 = new IloNumExpr[s.size()];
//					IloNumExpr[] z22 = new IloNumExpr[s.size()];
//					int sIndex = 0;
//					for (int indexInS : s) {
//						IloNumExpr[] z0 = new IloNumExpr[stockNum];
//						IloNumExpr[] z1 = new IloNumExpr[stockNum];
//						for (int i = 0; i < stockNum; i++) {
//							z0[i] = cplex.sum(Z[indexInS + stockNum][i], cplex
//									.prod(transTime[indexInS + stockNum][i], X[indexInS
//									                                           + stockNum][i]));
//							
//							z1[i] = Z[i][indexInS + stockNum];
//							
//						}
//						IloNumExpr[] z2 = new IloNumExpr[s.size()];
//						int index = 0;
//						for (int inInS : s) {
//							z2[index] = X[indexInS + stockNum][inInS + stockNum];
//							index++;
//						}
//						
//						z00[sIndex] = cplex.sum(z0);
//						z11[sIndex] = cplex.sum(z1);
//						z22[sIndex] = cplex.sum(z2);
//						sIndex++;
//						
//					}
//					
//					cplex.addLe(
//							cplex.diff(cplex.sum(z00), cplex.sum(z11)),
//							cplex.sum(
//									tMax,
//									cplex.prod(
//											Integer.MAX_VALUE,
//											cplex.diff(s.size() - 1, cplex.sum(z11))
//											)
//									)
//							);
//				}
//				// 添加
//			}


			// 对zij的约束
			for (int i = 0; i < stockNum; i++) {
				for (int j = 0; j < taskNum; j++) {
					cplex.addLe(Z[i][j + stockNum], cplex.prod(M, X[i][j + stockNum]));
					cplex.addLe(cplex.prod(-1, Z[j + stockNum][i]), cplex.prod(M, X[j + stockNum][i]));
				}
			}

			// 定义目标函数
			IloLinearNumExpr exprObj = cplex.linearNumExpr(); //
			for (int i = stockNum; i < taskNum + stockNum; i++) {
				for (int j = 0; j <stockNum; j++) {
					if (j != i) {
						exprObj.addTerm(1.0, Z[i][j]);
						exprObj.addTerm(transTime[i][j],X[i][j]);
					}
				}
			}
			for (int i = 0; i < stockNum; i++) {
				for (int j = stockNum; j <stockNum+taskNum; j++) {
					if (j != i) {
						exprObj.addTerm(-1.0, Z[i][j]);
					}
				}
			}
			// 最小化目标函数
			cplex.addMinimize(exprObj);



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
				System.out.println("-----------1、求解状态----------");
				System.out.println("Solution status = " + cplex.getStatus());
				System.out.println("final objctive = " + myRound(finalObj,2));
				System.out.println("CPU time:	" + cpuTime);
				System.out.println("找集合的时间 time:	" + ssTime);
				
				

				
				//堆场之间的X没有用到
				int[][] sxij = new int[X.length][X[0].length];
				double[][] szij = new double[X.length][X[0].length];
				for (int i = 0; i < sxij.length; i++) {
					for (int j = 0; j < sxij[0].length; j++) {
						if (i != j) {
							
							if(i<stockNum && j<stockNum){
								sxij[i][j] = 0;
							} else{
								if (cplex.getValue(X[i][j]) < 0.8) {
									sxij[i][j] = 0;
								} else {
									sxij[i][j] = 1;
									szij[i][j] = cplex.getValue(Z[i][j]);
								}
							}
							
							
						} else {
							sxij[i][j] = 0;
						}
					}
				}

				// output fleet size
				int fleetSize = 0;
				for(int i = 0;i<stockNum;i++ ){
					for (int j = stockNum; j < taskNum + stockNum; j++) {
						fleetSize += sxij[i][j];
					}
				}
				System.out.println("fleet size:	" + fleetSize);

				// output total traveling time
				double totalTravelTime = 0;
				for (int i = 0; i < sxij.length; i++) {
					for (int j = 0; j < sxij[0].length; j++) {
						totalTravelTime += sxij[i][j] * transTime[i][j];
					}
				}
				System.out.println("Total Travel time:	" + totalTravelTime);

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

				// output xij
				 System.out.print("\rxij:\r");
				 for (int i = 0; i < sxij.length; i++) {
					 for (int j = 0; j < sxij[0].length; j++) {
						 System.out.print(sxij[i][j] + " ");
					 }
					 System.out.print("\r");
				 }

			
				 System.out.print("\rservice beginning time:\r");
				 for (int i = 0; i < taskNum; i++) {
					 System.out.print("y"+(i+stockNum)+"="+myRound(cplex.getValue(Y[i]), 2) //i + 1 + ": " +
				 + "\r");
				 }

				double[][] x = new double[stockNum + taskNum][stockNum + taskNum];
				double[][] z = new double[stockNum + taskNum][stockNum + taskNum];
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						if (i != j) {
							
							if(i<stockNum && j<stockNum){
								sxij[i][j] = 0;
							} else{
								x[i][j] = cplex.getValue(X[i][j]);

								// System.out.println(cplex.getValue(X[i][j]));
								z[i][j] = cplex.getValue(Z[i][j]);
							}
						}
					}
				}
				System.out.println("-----------2、求解时间----------");
				System.out.println("The total time is " + cplex.getObjValue());
				//
				// 0是堆场。其余是客户。
				System.out.println("-------------3、路径-------------");
				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
						if ((i != j) && (x[i][j] == 1)) {
							// if ((i != j) ) {
							System.out.println(i + "--" + j+"("+myRound(z[i][j],2)+")");
						}
					}
				}

				System.out.println("-------------4、解-------------");

				for (int i = 0; i < stockNum + taskNum; i++) {
					for (int j = 0; j < stockNum + taskNum; j++) {
//						System.out.println("x[" + i + "]" + "[" + j + "]" + "=" + x[i][j]);
//						System.out.println("z[" + i + "]" + "[" + j + "]" + "=" + z[i][j]);
						System.out.print(myRound(z[i][j],2)+"\t");
					}
					System.out.println();
				}
				
				
				
				
				
			}
			

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
