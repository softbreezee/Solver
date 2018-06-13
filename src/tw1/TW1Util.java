package tw1;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ref.ComputeUtil;
import ref.Data;



public class TW1Util {
	
	/**
	 * 功能：对一个数组，对其中某一段进行求和
	 * @param arr
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int partSum(int[] arr,int begin,int end){
		int ret = 0;
		for(int i = begin;i<end;i++){
			ret = ret + arr[i];
		}
		
		return ret;
	}
	
	
	
	
	@Test	
	public void test(){
		Data d = new Data(1);
		System.out.println(d.IE);
		for(int x:d.loadTime){
			System.out.println(x);
		}
		
	}
	
	
	
	@Test
	public void  testNewNodeTW(){
		
		/*
		 * 1、接受已知参数
		 */
		int[][] tw1 = { { 225, 97, 221, 205, 75, 82 },
				{ 310, 237, 339, 315, 138, 120 } };
		int[][] tw2 = { { 226, 107, 107 }, 
				{ 446, 241, 390 } };
		double[][] driveTime = { 
				{ 0, 47, 35, 54, 42 }, 
				{ 47, 0, 13, 98, 55 },
				{ 35, 13, 0, 88, 43 },
				{ 54, 98, 88, 0, 89 },
				{ 42, 55, 43, 89, 0 } };
		int[] loadTime = { 13, 16, 13 };
		int IE = 1;
		int OE = 2;
		int IF = 1;
		int OF = 2;
		final int portNum = 1;
		int stockNum = 1;
		int[] truckNum = { 4 };
		int loadCar = 5;
		int taskNum = 6;

		/** 顶点分为任务顶点和出发/返回顶点！ ***/
		// 任务顶点服务时间
		double[] serviceTime = new double[taskNum];
		// 任务顶点时间窗
		double[][] nodeTW = new double[2][taskNum];
		// 弧转换时间
		double[][] transTime = new double[taskNum + stockNum][taskNum
				+ stockNum];

		/**
		 * 2、条件，用来确定服务时间、顶点活动时间、顶点转换时间的值
		 */
		ComputeUtil.condition(taskNum, IE, OE, IF, portNum, stockNum, loadCar,
				loadTime, tw1, tw2, driveTime, serviceTime, nodeTW, transTime);
		
		
		//1、设置最小宽度，得到最大的子任务数，共有w个
		int d = 5;
		int w = 0;
		int[] zi = new int[taskNum];
		for(int i = 0;i<taskNum;i++){
			//每个任务被分成了几个子任务
			int n = 0;
			n = (int) Math.floor((nodeTW[1][i] - nodeTW[0][i])/d);
			w = w + n;
			zi[i] = n;
			System.out.println(nodeTW[0][i]+"----"+nodeTW[1][i]);
		}
		System.out.println(taskNum);
		System.out.println(w);
		//2、重新设置顶点的时间窗
		int tempW = 0;
		double[][] nodeTWNew = new double[2][w];
		double[] serviceTimeNew = new double[w];
		double[][] transTimeNew = new double[w + stockNum][w + stockNum];
		for(int i = 0;i<taskNum;i++){
			for(int j = 0;j<zi[i];j++){
				nodeTWNew[0][tempW] = nodeTW[0][i]+d*j;
				nodeTWNew[1][tempW] = nodeTW[0][i]+d*(j+1);
				serviceTimeNew[tempW] = serviceTime[i] + d*j;	
				//对弧转换时间的赋值！
				
				tempW++;
			}
		}
		
		//对弧转换时间的定义
		for(int i = 0;i<stockNum;i++){
			for(int j = 0;j<stockNum;j++){
				transTimeNew[i][j] = transTime[i][j];
			}
		}
		
		//定义一个二维数组存放原来第i个任务的子任务的开始位置
		int[] w_index = new int[zi.length];
		int w_begin = 0;
		for(int i = 0; i<zi.length;i++){
			w_index[i] = w_begin;
			w_begin += zi[i];
		}
		
		for(int i = 0;i<taskNum;i++){
			for(int j = 0;j<taskNum;j++){
				for(int x = w_index[i] ;x<w_index[i]+zi[i];x++){
					for(int y = w_index[i] ;y<w_index[i]+zi[i];y++){
						transTimeNew[x+stockNum][y+stockNum] = transTime[i+stockNum][j+stockNum];

					}
				}
			}
		}
		
		for(int i = 0; i<stockNum;i++){
			for(int j = 0;j<taskNum;j++){
				for(int y = w_index[i] ;y<w_index[i]+zi[i];y++){
					transTimeNew[i][y+stockNum] = transTime[i][j+stockNum];
					transTimeNew[y+stockNum][i] = transTime[j+stockNum][i];
					
				}
			}
		}
		
		
		
	}









	public static Map<String,Object> newNodeTimeData(int taskNum, int iE, int oE, int iF,
			int portNum, int stockNum, int loadCar, int[] loadTime,
			int[][] tw1, int[][] tw2, double[][] driveTime,
			double[] serviceTime, double[][] nodeTW, double[][] transTime,int d) {
		
		/**
		 * 2、条件，用来确定服务时间、顶点活动时间、顶点转换时间的值
		 */
		ComputeUtil.condition(taskNum, iE, oE, iF, portNum, stockNum, loadCar,
				loadTime, tw1, tw2, driveTime, serviceTime, nodeTW, transTime);
		
		
		//1、设置最小宽度，得到最大的子任务数，共有w个
		int w = 0;
		int[] zi = new int[taskNum];
		for(int i = 0;i<taskNum;i++){
			//每个任务被分成了几个子任务
			int n = 0;
			n = (int) Math.floor((nodeTW[1][i] - nodeTW[0][i])/d);
			w = w + n;
			zi[i] = n;
			System.out.println(nodeTW[0][i]+"----"+nodeTW[1][i]);
		}
		System.out.println(taskNum);
		System.out.println(w);
		//2、重新设置顶点的时间窗
		int tempW = 0;
		double[][] nodeTWNew = new double[2][w];
		double[] serviceTimeNew = new double[w];
		double[][] transTimeNew = new double[w + stockNum][w + stockNum];
		for(int i = 0;i<taskNum;i++){
			for(int j = 0;j<zi[i];j++){
				nodeTWNew[0][tempW] = nodeTW[0][i]+d*j;
				nodeTWNew[1][tempW] = nodeTW[0][i]+d*(j+1);
				serviceTimeNew[tempW] = serviceTime[i] + d*j;	
				//对弧转换时间的赋值！
				
				tempW++;
			}
		}
		
		//对弧转换时间的定义
		for(int i = 0;i<stockNum;i++){
			for(int j = 0;j<stockNum;j++){
				transTimeNew[i][j] = transTime[i][j];
			}
		}
		
		//定义一个二维数组存放原来第i个任务的子任务的开始位置
		int[] w_index = new int[zi.length];
		int w_begin = 0;
		for(int i = 0; i<zi.length;i++){
			w_index[i] = w_begin;
			w_begin += zi[i];
		}
		
		for(int i = 0;i<taskNum;i++){
			for(int j = 0;j<taskNum;j++){
				for(int x = w_index[i] ;x<w_index[i]+zi[i];x++){
					for(int y = w_index[i] ;y<w_index[i]+zi[i];y++){
						transTimeNew[x+stockNum][y+stockNum] = transTime[i+stockNum][j+stockNum];

					}
				}
			}
		}
		
		for(int i = 0; i<stockNum;i++){
			for(int j = 0;j<taskNum;j++){
				for(int y = w_index[i] ;y<w_index[i]+zi[i];y++){
					transTimeNew[i][y+stockNum] = transTime[i][j+stockNum];
					transTimeNew[y+stockNum][i] = transTime[j+stockNum][i];
					
				}
			}
		}	
		
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("nodeTWNew", nodeTWNew);
		map.put("serviceTimeNew", serviceTimeNew);
		map.put("transTimeNew", transTimeNew);
		map.put("w", w);
		System.out.println(w);
		map.put("w_index", w_index);
		map.put("zi", zi);
		return map;
	}
	
	
	
	
	
	
	
	
	
	

}
