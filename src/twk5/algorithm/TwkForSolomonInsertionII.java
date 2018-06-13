package twk5.algorithm;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import twk5.Generator;

public class TwkForSolomonInsertionII{
	int stocks=0,tasks=0,tMax=8,fromTo=-1,to=-1,T =10;
	int ifNum,ofNum;
	double rho1=3,rho2=2;
	double[][] t_ij = new double[stocks+tasks][stocks+tasks];//同一个任务的第一阶段与第二阶段之间的转换时间不为0
	Vector<List<Integer>> routeSet = new Vector();//容器A用于放路径
	LinkedList<Integer> eleSet = new LinkedList();//容器B用于存放元素
	double[] begin,p;
	double[][] tw;
	
	
	public TwkForSolomonInsertionII(){
		String name = "2";
		Generator g = new Generator();
		Reader reader;
		try {
			reader = new FileReader(new File("C:/Users/Administrator/Desktop/data/datafile"+name+".txt"));
			g.fileInput(reader);
			//初始化B容器,先放入第一阶段的点，当第一阶段的点完成访问后，在加入第二阶段的点，是个更新的过程
			ifNum = g.ifNum;
			ofNum = g.ofNum;
			tasks = (g.ifNum+g.ofNum)<<1;
			stocks = g.stockNum;
			for(int i = stocks;i<(tasks>>1)+stocks;i++){
				eleSet.add(i);			
			}
			t_ij = new double[tasks+stocks][tasks+stocks];
//			= new double[tasks>>1];
			p = g.packageTime;
//			System.out.println(p.length);
			
			//对tij处理，转换为t_ij;
			double[][] tij = g.tij;
			for(int i = 0;i<tasks+stocks;i++){
				for(int j = 0;j<tasks+stocks;j++){
					t_ij[i][j] = tij[i][j];
				}
			}
			
			//对节点开始时间初始化
			begin = new double[tasks+stocks];
			for(int i = 0;i<tasks+stocks;i++){
				begin[i] = -1;
			}
			
			//节点的时间窗
			tw = new double[2][tasks];
			for(int i = 0;i<2;i++){
				for(int j = 0;j<tasks;j++){
					
					if(j<(tasks>>1)){
						tw[0][j] = t_ij[0][j];
						tw[1][j] = T - t_ij[j+(tasks>>1)][0]-p[j];
					}else{
						tw[0][j] = t_ij[0][j-(tasks>>1)] + p[j-(tasks>>1)];
						tw[1][j] = T - t_ij[j][0]-p[j-((tasks)>>1)];
						
					}
					
					
				}
			}
						
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("文件读取有误");
		}
		
	}
	
	
	public static void main(String[] args) {
		TwkForSolomonInsertionII instant = new TwkForSolomonInsertionII();
		try {
			instant.operation();
			
			for(List<Integer> l:instant.routeSet){
				for(int i:l){
					System.out.print(i+"-");
				}
				System.out.println();
			}
			for(List<Integer> l:instant.routeSet){
				for(int i:l){
					System.out.print(instant.begin[i]+"-");
				}
				System.out.println();
			}
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @throws Exception
	 */
	public void operation() throws Exception{
		int curRouteIndex = 0;
		while(eleSet.size()!=0){
			/**
			 * 从B中取出某点c，构建初始路径
			 */
			LinkedList<Integer> route = new LinkedList<Integer>();
			//选择一个堆场初始化，以后要进行更新
			route.addFirst(0);
			begin[0] = 0;
			route.addLast(0);
			ss:while(//R没有满载 && 
					eleSet.size()!=0){
	
				/**
				 * 如果B容器中的全部点都不可行，那么重新创建一个路径
				 */
				if(infeasibleAll(route,eleSet))
					break ss;
				LinkedList<Integer> subEleSet = selectFeasible(route,eleSet);
				
				
				/**
				 * 对B容器中的每一个点，计算最合适的插入位置
				 */
				Integer[] eleInsertion =  bestInsertionPlace(route,eleSet);
	
				/**
				 * 此时B容器中的每个节点都有一个最合适的插入位置，根据这些节点及其插入位置找出一个最合适的节点，
				 * 要判断是否可行，1、第一二阶段任务的前后顺序，2、T限制，3、tMax限制
				 */
				Integer bestEle = selectBestEle(eleInsertion,eleSet,route);
				if(bestEle == -1){
					throw new Exception("没有最合适的点");
				}

				/**
				 * 将节点插入到插入位置，同时要更新节点开始时间
				 */
				boolean ff = insert(route,bestEle,eleInsertion,eleSet);
				if(!ff)
					throw new Exception("插入不成功");
				
				/**
				 * 移除插入节点，如果被移除的节点是第一阶段的节点，同时更新容器B，更新返回的堆场
				 */
				eleSet.remove(bestEle);
//				System.out.println(w);
				if(bestEle<stocks+(tasks>>1))
					eleSet.add(bestEle+(tasks>>1));
			}
			
			/**
			 * 将R放进A中
			 */
			routeSet.add(curRouteIndex, route);
			curRouteIndex++;
			System.out.println(begin[0]);
		}
		
	}
	
	
	/**
	 * 挑选出可行的解，作为以后操作的代替B容器的容器
	 * @param route 当前的路径
	 * @param eleSet2 B容器
	 * @return B容器的子集
	 */
	private LinkedList<Integer> selectFeasible(LinkedList<Integer> route, LinkedList<Integer> eleSet2) {
		LinkedList<Integer> subList = new LinkedList<Integer>();
		int count = 0;
		
		//对当前路径的每个可以插入的位置，进行所有B容器中所有点插入操作的判断
		for(int k = 0 ; k < route.size();k++){
		
			if(k>0){
				double[] beginII = new double[stocks+tasks];
				for(int i = 0;i<eleSet2.size();i++){
					//C1
					if(eleSet2.get(i)<stocks+(tasks>>1)){
					//begin的角标是从0开始的，eleSet里面的元素是从stocks开始的	
						beginII[eleSet2.get(i)] = begin[route.get(k-1)] + t_ij[route.get(k-1)][eleSet2.get(i)];
					}
					//C2
					if(eleSet.get(i)>=stocks+(tasks>>1) && eleSet.get(i)<stocks+tasks){
						double a = begin[route.get(k-1)]+t_ij[route.get(k-1)][eleSet2.get(i)];
//						System.out.println(begin[eleSet2.get(i)-(tasks>>1)]);
//						System.out.println(p[eleSet2.get(i)-(tasks>>1)-stocks]);
						double b = begin[eleSet2.get(i)-(tasks>>1)]+p[eleSet2.get(i)-(tasks>>1)-stocks];
						
						beginII[eleSet2.get(i)] = a>b?a:b;  
					}
					if(beginII[eleSet2.get(i)]<tw[0][eleSet2.get(i)-stocks] && beginII[eleSet2.get(i)]>tw[1][eleSet2.get(i)-stocks])
						count++;
				}
			}
		
		}	
		
		return subList;
	}


	/**
	 * 插入动作，更新路径，
	 * @param route 待插入的路径
	 * @param bestEle 带插入的节点
	 * @param indexOfEleInsertion B容器中所有节点的最好的插入位置,这个数组的长度是B容器的容量
	 * @param eleSet2 B容器
	 */
	private boolean insert(LinkedList<Integer> route, Integer bestEle, Integer[] eleInsertion, LinkedList<Integer> eleSet2) {
		// TODO Auto-generated method stub
		int indexOfBestEle = eleSet2.indexOf(bestEle);
		Integer bestElePlace = eleInsertion[indexOfBestEle];
		int indexOfbestElePlace = route.indexOf(bestElePlace)+1;//得到最好的位置的脚标+1，插入它之后
//		System.out.println(""+bestEle+bestElePlace);
//		System.out.println(indexOfbestElePlace);
		//这里会不会出现插入的位置不正确
		route.add(indexOfbestElePlace, bestEle);//插入到当前的路径	
		
		
		//更新节点的开始时间,对插入节点及以后的节点进行更新
		for(int i = indexOfbestElePlace;i<route.size()-1;i++){
//			System.out.println(route.get(i));
			
			if(route.get(i)<stocks+(tasks>>1)){
				//begin的角标是从0开始的，eleSet里面的元素是从stocks开始的
				begin[route.get(i)] = begin[route.get(i-1)] + t_ij[route.get(i-1)][route.get(i)];
			}
			
			//c2
			if(route.get(i)>=stocks+(tasks>>1) && route.get(i)<stocks+tasks){
				double a = begin[route.get(i-1)]+t_ij[route.get(i-1)][route.get(i)];
				double b = begin[route.get(i)-(tasks>>1)]+p[route.get(i)-(tasks>>1)-stocks];
				begin[route.get(i)] = a>b?a:b;  
			}
		}
		
		/**
		 * 更新返回的堆场
		 */
		return true;
		
	}

	
//	@Test
//	public void test(){
//		LinkedList<Integer> list = new LinkedList<Integer>();
//		for(int i = 0;i<10;i++){
//			list.add(i+1);
//		}
//		
//		//3和4之间插入11
//		int i = list.indexOf(3);
//		list.add(i, 11);
//		for(Integer j:list){
//			System.out.println(j);
//		}
//		
//	}
	

	/**
	 * 选择最合适的插入元素
	 * @param indexOfEleInsertion B容器中每个元素对于当前路径最好的插入位置，脚标与B容器中的元素一一对应
	 * @param eleSet2 B容器，里面装的待插入的元素
	 * @param route 
	 * @return 返回该插入元素
	 */
	private Integer selectBestEle(Integer[] eleInsertion, LinkedList<Integer> eleSet2, LinkedList<Integer> route) {
		double alph1 = 0.8;
		double alph2 = 0.2;
		double c = 0,min = 100;
		int index = -1;
		for(int i = 0;i<eleSet2.size();i++){
			Integer nextEle = route.get(route.indexOf(eleInsertion[i])+1);
			double c1 = caculateC1(eleSet2.get(i),eleInsertion[i],nextEle);
			double c2 = caculateC2(eleSet2.get(i),eleInsertion[i],nextEle);
			c = alph1*c1+alph2*c2;
			if(min>c){
				min = c;
				index = i;
			}
		}	
		if(index == -1){
			return -1;
		}
		return eleSet2.get(index);
	}


	/**
	 * 计算出B容器中每个点的最合适的插入位置
	 * @param route 当前路径
	 * @param eleSet2 B容器
	 * @return p(u) ，返回的数组，角标与B容器的脚标一一对应，数组的内容是route路径里面的元素
	 */
	private Integer[] bestInsertionPlace(LinkedList<Integer> route, LinkedList<Integer> eleSet2) {
		double alph1 = 0.8;
		double alph2 = 0.2;
		Integer[] bestPlace = new Integer[eleSet2.size()];//这个数组里依照eleSet2的次序放着每个元素的该插入的位置p(u)
		for(Integer i:bestPlace){
			i = -1;
		}
		
		for(int i = 0;i<eleSet2.size();i++){
			//对于当前点，当前路径的每个位置的代价
			double[] benifit = new double[route.size()-1];
			for(double b:benifit){
				b = 100;
			}
			//拿每个点对路径进行遍历
			for(int j = 0;j < route.size();j++){
				if(j>0){
					//可行有benifit，不可行的benifit的为100
					if(feasible(eleSet2.get(i),route.get(j-1),route.get(j))){
						double c1 = caculateC1(eleSet2.get(i),route.get(j-1),route.get(j));
						double c2 = caculateC2(eleSet2.get(i),route.get(j-1),route.get(j));
//						System.out.println(c1+"--"+c2);
						benifit[j-1] = alph1*c1 + alph2*c2;
					}
				}
			}
			
			int index = findMinBenifit(benifit,route);//返回代价最小的点的角标
			bestPlace[i] = route.get(index);
			
		}
		
		return bestPlace;
	}

	
	
	/**
	 * 寻找最小的返回值
	 * @param benifit 长度=前路径长度-1
	 * @param route
	 * @return
	 */
	private int findMinBenifit(double[] benifit, LinkedList<Integer> route) {
		assert benifit.length == route.size()-1;
		double min=100;
		int index = -1;

		for(int i = 0;i<benifit.length;i++){
			if(min>benifit[i]){
				min = benifit[i];
				index = i;
			}
		}
		
//		return route.get(index);
		return index;
	}

	

	/**
	 * 计算c2，代表着插入节点u的急迫性
	 * @param u
	 * @param i
	 * @param j
	 * @return
	 */
	private double caculateC2(Integer u, Integer i, Integer j) {
		double beginU  = 0,c2 = 0;;//u插入ij中，最早的开始时间
		//pick--of
		//delivery--if
		if(u<stocks+(tasks>>1)){
		//begin的角标是从0开始的，eleSet里面的元素是从stocks开始的	
			beginU = begin[i] + t_ij[i][u];
		}
		//c2
		if(u>=stocks+(tasks>>1) && u<stocks+tasks){
			double a = begin[i]+t_ij[i][u];
			double b = begin[u-(tasks>>1)]+p[u-(tasks>>1)-stocks];
			beginU = a>b?a:b;  
		}
		//u在p2
		if(u>=ifNum+ofNum+stocks && u<ifNum+ofNum+ofNum+stocks){
			c2 = tw[1][u-stocks] - beginU;
		}
		//u在d1
		else if(u>=ifNum+ofNum+stocks && u<ifNum+ofNum+ofNum+stocks){
			c2 = beginU - tw[0][u-stocks];
		}else{
			
			c2 = (tw[1][u-stocks] - tw[0][u - stocks])/4;
		}
		return c2;
	}

	

	/**
	 * 代表目标的改变量
	 * @param u 被插入的点
	 * @param i	插入点之前的点，在路径中
	 * @param j 之后的点
	 * @return
	 */
	private double caculateC1(Integer u, Integer i, Integer j) {
		
		
		
//		double mBefore = //插入u之前的持续时间
//		double mAfter = //插入u之后的持续时间
//		double c1 = rho1*((mAfter-mBefore)/tMax) + rho2*(t_ij[i][u] + t_ij[u][j] - t_ij[i][j]);
		double c1 =  rho2*(t_ij[i][u] + t_ij[u][j] - t_ij[i][j]);
		
		
		return c1;
	}

	

	/**
	 * 判断i,u,j中u是否可行
	 * @param u 待插入的点
	 * @param i	之前的点
	 * @param j	之后的点
	 * @return
	 */
	private boolean feasible(Integer u, Integer i, Integer j) {
		assert eleSet.contains(u);
		double beginU = 0;
		if(u<stocks+(tasks>>1)){
		//begin的角标是从0开始的，eleSet里面的元素是从stocks开始的	
			beginU = begin[i] + t_ij[i][u];
		}
		//c2
		if(u>=stocks+(tasks>>1) && u<stocks+tasks){
			double a = begin[i]+t_ij[i][u];
			double b = begin[u-(tasks>>1)]+p[u-(tasks>>1)-stocks];
			beginU = a>b?a:b;  
		}
		if(beginU<tw[0][u-stocks] && beginU>tw[1][u-stocks])//tw是从0开始的,u是eleSet中的点，是从stocks开始的
			return false;
		else
			return true;
	}


	/**
	 * 判断全部的点是不是都不可行
	 * @param route 当前路径
	 * @param eleSet2 B容器
	 * @return 返回true表示不可行
	 */
	private boolean infeasibleAll(LinkedList<Integer> route, LinkedList<Integer> eleSet2) {
		int count = 0;
		
		//对当前路径的每个可以插入的位置，进行所有B容器中所有点插入操作的判断
		for(int k = 0 ; k < route.size();k++){
		
			if(k>0){
				double[] beginII = new double[stocks+tasks];
				for(int i = 0;i<eleSet2.size();i++){
					//C1
					if(eleSet2.get(i)<stocks+(tasks>>1)){
					//begin的角标是从0开始的，eleSet里面的元素是从stocks开始的	
						beginII[eleSet2.get(i)] = begin[route.get(k-1)] + t_ij[route.get(k-1)][eleSet2.get(i)];
					}
					//C2
					if(eleSet.get(i)>=stocks+(tasks>>1) && eleSet.get(i)<stocks+tasks){
						double a = begin[route.get(k-1)]+t_ij[route.get(k-1)][eleSet2.get(i)];
//						System.out.println(begin[eleSet2.get(i)-(tasks>>1)]);
//						System.out.println(p[eleSet2.get(i)-(tasks>>1)-stocks]);
						double b = begin[eleSet2.get(i)-(tasks>>1)]+p[eleSet2.get(i)-(tasks>>1)-stocks];
						
						beginII[eleSet2.get(i)] = a>b?a:b;  
					}
					if(beginII[eleSet2.get(i)]<tw[0][eleSet2.get(i)-stocks] && beginII[eleSet2.get(i)]>tw[1][eleSet2.get(i)-stocks])
						count++;
				}
			}
		
		}	
		return count==(route.size()-1)*eleSet2.size()?true:false;
	}

	
	/**
	 * 保证了任务的第二阶段执行要在第一阶段的后面
	 * @param v
	 * @param route
	 * @param from
	 * @return
	 */
	private boolean feasibleIandII(Integer v, LinkedList<Integer> route,Integer from) {
		if(route.contains(v-(tasks>>1)) && route.indexOf(from) < route.indexOf(v-(tasks>>1))){
			return false;
		}else{
			return true;
		}
		
	}

	/**
	 * 判断一个路径route上v点插入i,j之间，是否超过了每车的限定时间
	 * @param route 当前路径
	 * @param v 插入点
	 * @param i 
	 * @param j
	 * @return	没有超过返回ture;否则返回false
	 */
	private boolean feasible(LinkedList<Integer> route, Integer v, Integer i,Integer j) {
		int[] from = new int[stocks+tasks];
		double totalRouteTime = 0;
		//计算出没插入这个点之前的总持续时间
//		System.out.println(route.size());
//		for(Integer x:route){
//			System.out.println(x);
//		}
		for(int s = 0;s<route.size();s++){
			if(s>0){
//				System.out.println("form"+route.get(s-1)+"to"+route.get(s));
				totalRouteTime+= t_ij[route.get(s-1)][route.get(s)];
			}
		}
		//计算插入这个点之后的总持续时间
		totalRouteTime = totalRouteTime - t_ij[i][j] + t_ij[i][v] + t_ij[v][j];

//		System.out.println(totalRouteTime);
		return totalRouteTime <= tMax;
	}

	/**
	 *	距离最近堆场的距离
	 * @param integer B容器中的点
	 * @return 返回堆场到该点之间的距离（时间）
	 */
	private double shortestFromStocksTo(Integer v) {
		double temp = -1;
		for(int i = 0;i<stocks;i++){
			if(temp<t_ij[i][v])
				temp = t_ij[i][v];//t_ij为顶点时间矩阵
			
		}
		return temp;
	}
	
	
	private int shortestFromStocksTo(Integer v,boolean o) {
		double temp = -1;
		int x = -1;
		for(int i = 0;i<stocks;i++){
			if(temp<t_ij[i][v])
				temp = t_ij[i][v];//t_ij为顶点时间矩阵
			
		}
		return x;
	}
	
	
	/**
	 * 找到利益最大的点
	 * @param benifit B容器中的所有的点
	 * @return  最大利益的点的角标
	 */
	private int findMaxBenifit(double[] benifit) {
		double temp = -100;
		int index = -1;
		for(int i = 0;i<benifit.length;i++){
			if(benifit[i]>=temp){
				temp = benifit[i];
				index = i;
			}			
			System.out.println("findMaxBen"+benifit[i]);
		}
		return index;
	}
	
	
	/**
	 * 
	 * @param route
	 * @param w
	 * @param fromTo2
	 * @param to2
	 */
	private void insert(LinkedList<Integer> route, int w, int fromTo2, int to2) {
		int vIndex = -1;
		if(to2<stocks){
			vIndex = route.indexOf(fromTo2)+1;
		}
		else{
			vIndex = route.indexOf(to2);
		}
//		System.out.println(""+fromTo2+to2);
		route.add(vIndex,Integer.valueOf(w));//插在to2的前面
//		for(Integer i:route){
//			System.out.print(i);
//		}
		//如果to2这个点是最后一个点，那么要重新选择一个距离w最近的堆场
		if(to2<stocks){
			int to3 = shortestFromStocksTo(w,true);
			if(to3!=-1){
				route.removeLast();
				route.addLast(to3);
			}
		}
		
	}

	

}
