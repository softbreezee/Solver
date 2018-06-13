package twk5.algorithm;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import twk5.Generator;

public class TwkForSolomonInsertion{
	int stocks=0,tasks=0,tMax=8,fromTo=-1,to=-1;
	double[][] t_ij = new double[stocks+tasks][stocks+tasks];//同一个任务的第一阶段与第二阶段之间的转换时间不为0
	Vector<List<Integer>> routeSet = new Vector();//容器A用于放路径
	LinkedList<Integer> eleSet = new LinkedList();//容器B用于存放元素
	double[] begin;
	
	public static void main(String[] args) {
		TwkForSolomonInsertion instant = new TwkForSolomonInsertion();
		try {
			instant.operation();
			
			for(List<Integer> l:instant.routeSet){
				for(int i:l){
					System.out.print(i+"-");
				}
				System.out.println();
			}
			for(List<Integer> route:instant.routeSet){
				double totalTime = 0;
				for(int s = 0;s<route.size();s++){
					if(s>0){
//						System.out.println("form"+route.get(s-1)+"to"+route.get(s));
						totalTime+= instant.t_ij[route.get(s-1)][route.get(s)];
					}
				}
				System.out.println(totalTime);
			}
			
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public TwkForSolomonInsertion(){
		
		String name = "2";
		Generator g = new Generator();
		Reader reader;
		try {
			reader = new FileReader(new File("C:/Users/Administrator/Desktop/data/datafile"+name+".txt"));
			g.fileInput(reader);
			//初始化B容器,先放入第一阶段的点，当第一阶段的点完成访问后，在加入第二阶段的点，是个更新的过程
			tasks = (g.ifNum+g.ofNum)<<1;
			stocks = g.stockNum;
			for(int i = stocks;i<(tasks>>1)+stocks;i++){
				eleSet.add(i);			
			}
			t_ij = new double[tasks+stocks][tasks+stocks];
			double[] p; 
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
//			System.out.println((tasks>>1));
			for(int i = stocks;i<stocks+(tasks>>1);i++){
				t_ij[i][i+(tasks>>1)] = p[i-stocks];
			}
			
			//对节点开始时间初始化
			begin = new double[eleSet.size()];
			for(int i = 0;i<eleSet.size();i++){
				begin[i] = -1;
			}
						
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("文件读取有误");
		}
		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void operation() throws Exception{
		int curRouteIndex = 0;
		while(eleSet.size()!=0){
			
			//如何保证不同路径的时候，第二阶段的开始时间是在第一阶段的开始时间之后
			//什么时候确定每个点的开始时间
			//插入的时候就要确定每个点的开始时间
			//同时插入点的之后的开始时间都要发生改变
			
			/**
			 * 从B中取出某点c，构建初始路径
			 */
			//先在B中取得一个点,完成更新
			int v = eleSet.removeFirst();
			if(v<(tasks>>1)+stocks){
				eleSet.addLast(v+(tasks>>1));
			}
			//创建路径，里层循环一定会将合适的点都插入完成，
			//这时候当前路径没有合适点插入的时候，在外层重新创建一个路径
			LinkedList<Integer> route = new LinkedList<Integer>();
			//选择一个堆场初始化，以后要进行更新
			route.addFirst(0);
			route.add(v);
			route.addLast(0);
			while(//R没有满载 && 
					eleSet.size()!=0){
				/**
				 * 从B中挑出一个点e出来
				 */
				int w = pick(route,eleSet);
				if(w==-1)
					throw new Exception("选择的点有问题");//说明B中所有的点对于当前路径都不可行，应该重新创建一个空路径
				
				/**
				 * 将e点插入到R的某个中间位置
				 */
//				 boolean f = insert(route,w);
				if(fromTo!=-1&&to!=-1&&route.contains(fromTo)&&route.contains(to)&&feasible(route,w,fromTo,to))
					insert(route,w,fromTo,to);
				else
//					throw new Exception("from="+fromTo+"和to有问题"+to);
					break;
				//插入w点，同时更新B容器

				eleSet.remove(Integer.valueOf(w));
//				System.out.println(w);
				if(w<stocks+(tasks>>1))
					eleSet.add(w+(tasks>>1));
			}
			/**
			 * 将R放进A中
			 */
			routeSet.add(curRouteIndex, route);
			curRouteIndex++;
		}
		
	}
	
	

	/**
	 * 选择合适的点
	 * 在选择的同时就保证了这个点的可行性
	 * 没有合适的点则返回-1
	 * @param route
	 * @param eleSet2
	 * @return 返回合适的点
	 * @throws Exception 
	 */
	private int pick(LinkedList<Integer> route, LinkedList<Integer> eleSet2) throws Exception {
		//定义插入和构建新路径的代价，benifit =c1-c2，选择利益最大的点作为被选择的插入点
		double[] c1 = new double[eleSet2.size()];//插入到当前路径的代价
		double[] c2 = new double[eleSet2.size()];//构建新路径的代价
		double[] benifit = new double[eleSet2.size()];
		//对benifit初始化
		for(int i = 0;i<eleSet2.size();i++){
			benifit[i] = -100;
		}
		
		int v = -1;
		vv:for(int i = 0;i<eleSet2.size();i++){
			//计算c1，遍历当前路径中的点，c1是引起路径的持续时间变化的最小的改变量
			//在计算c1的时候 ，要不要排出明显不对的点？
			c1[i] = caculateC1(eleSet2.get(i),route);
			if(c1[i] == 100)
//				throw new Exception("c1计算不正确");//c1=100，说明出现了不可行的点，应该跳过，进入下个循环，对下个点进行判断
				continue vv;//跳过当前点的同时，也将当前点的benifit置为-100, 不会影响后面寻找最大的benifit
			//计算c2,c2为这个点距离最近的堆场的距离
			c2[i] = shortestFromStocksTo(eleSet2.get(i));
			if(c2[i] == -1)
				throw new Exception("c2计算不正确");
			benifit[i] = c2[i] - c1[i];			
//			benifit[i] = c1[i] - c2[i];			
//			System.out.println("c1"+c1[i]+"c2"+c2[i]);
		}
		int vIndex = findMaxBenifit(benifit);
		if(vIndex!=-1)
			 v = eleSet2.get(vIndex);
		else{
			throw new Exception("没找到最大的Benifit");
		}
		//对v点进行判断，是否满足插入的条件：是，移除该点并返回；否，返回-1
		//是否达到车辆工作时间的限制
		
//		System.out.println(route.get(route.size()-1));
		return v;
		
	}

	
	/**
	 * 计算点插入当前路径的最小的代价，这个代价是导致该路径持续时间变化量
	 * @param integer  B容器中的点
	 * @param route  当前路径
	 * @return 返回所有可插入的位置中，导致路径持续时间所有变化量中最小的是变化量
	 * @throws Exception 
	 */
	private double caculateC1(Integer v, LinkedList<Integer> route) throws Exception {
		//计算插入每两个点之间的价值
		double minDisparity = 100,currDisparity = 0;//插入该点到某个位置的改变量
		
		//计算出了插入这个路径所有位置的最小的改变量 
		for(int i = 0;i<route.size();i++){
			//记录上一个点从什么地方开始
			if(i>0){	
				
				//这里都是对同一条路径的是否可以插入某一点进行判断
				//插入的是from[route.get(i)],route.get(i)之间的改变量
				//保证插入点是可行的前提下，计算插入后的改变量
				//第一个条件是插入该点后保证当前路径不会超过车辆的最大工作时间
				//第二个条件是保证当前路径不会出现第二阶段的节点出现第一阶段节点之前，在同一个路径中
				//第三个条件是保证当当前路径的是在计划期内的！
				//这条是对不同路径的时间顺序进行判断
				//第四个条件是保证插入节点后，当前路径第二阶段的节点的开始时间要在第一阶段节点的是开始时间之后
				if(feasible(route,v,route.get(i-1),route.get(i))&& feasibleIandII(v,route,route.get(i-1)) && feasiblePeiord(begin,v,route.get(i-1),route.get(i),route)){
					
					currDisparity = t_ij[route.get(i-1)][v] + t_ij[v][route.get(i)]- t_ij[route.get(i-1)][route.get(i)];//???????这里会不会出现负值？？？？
					//这里出现了负值，说明这个改变量不仅没有导致路径的总时间增加，反而使得总时间减少了，可以被选择
//					System.out.println("t"+from[route.get(i)]+v+":"+ t_ij[from[route.get(i)]][v]+"  t"+v+route.get(i)+":"+t_ij[v][route.get(i)]+"  t"+from[route.get(i)]+route.get(i)+":"+t_ij[from[route.get(i)]][route.get(i)]);
					
//					if(currDisparity<0)
//						throw new Exception("改变量为负值");
					if(minDisparity>currDisparity ){//初始化minDisparity为100
						minDisparity = currDisparity;
						fromTo =  route.get(i-1);
						to = route.get(i);
					}
				}
			}
		}
//		System.out.println(minDisparity);
		return minDisparity;
	}

	/**
	 * 判断该路径不能超过计划期
	 * @param begin2
	 * @param v
	 * @param integer
	 * @param integer2
	 * @param route
	 * @return
	 */
	private boolean feasiblePeiord(double[] begin2, Integer v, Integer integer,
			Integer integer2, LinkedList<Integer> route) {
		
		return false;
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
	 * 完成插入动作，同时也要满足
	 * @param route
	 * @param w
	 */
	private boolean insert(LinkedList<Integer> route, int w) {
		//插入到合适的位置
		//如果插入到对场前或者堆场之后，需要更新路径中的堆场
		return true;
		
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
