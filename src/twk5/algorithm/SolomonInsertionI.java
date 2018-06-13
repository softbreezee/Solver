package twk5.algorithm;

import java.io.* ;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class SolomonInsertionI {
	//coded by 杨龙
	
	//定义全局变量物流公司和客户参数
	static int vehicleNum;//车辆数
	static int capacity;//车容量
	static int[] demand = new int[101];//每个客户点的需求
	static double[][] timeWindow = new double[101][2];//时间窗
	static double[] serviceTime = new double[101];//每个客户点的服务时间
	static double[][] coordinate = new double[101][2];//每个客户点的坐标
	static boolean[] routeState =new boolean[101];//客户的状态，初始化为true，表示尚未加入路径
	static int[][] route =new int[30][102];//????????什么意思，最多有30个路径？？
	static double distance[][] = new double[101][101];//距离矩阵
	static double[] c1 = new double[101];//?????
	static int pStar[]=new int[101];//?????????路径数？
	static int routedUsers =1;
	static double[][] transferedCoordinate = new double[101][2];
	//static int routej=0;
	// VRPTW主函数 
	public static void main(String[] args) {
		//pStar[0]=-1;
		double totalDistance=0;
		int vehiclesUsed=0;
		int points=0;
		long t = System.currentTimeMillis();		
		String file = "C:/Users/young/Desktop/GIS大作业/Solomon 100/In/RC101.txt " ;
		String content = readFile(file);
		System.out.println(content);
		getData(file);
		//得到距离矩阵
		distanceMatrix(101,101);
		//得到路径
		vehicleRoutes();
		//得到路径总长，打印路径
		for(int i=0;i<vehicleNum;i++){
			points=0;
			if(route[i][1]!=0) {
				vehiclesUsed++;
				printRoutei(i);				
				while(route[i][points]!=200){
					points++;
				}
				for(int j=1;j<points;j++){
					totalDistance= totalDistance+distance[j][j-1];
				}
			}			
		}
		t=System.currentTimeMillis()-t;
		System.out.println("Computing Time Used = " +t+"ms");
		System.out.println("Vehicles Used = "+ vehiclesUsed);
		System.out.println("Total Distance = "+ totalDistance);
		System.out.println();
		//transferCoordinate();
		//printMap();
		
	}
	
	//读取文件	
	public static String readFile(String path) {
		File f = new File(path);
		//分配新的直接字节缓冲区
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10240);
		StringBuffer stringBuffer = new StringBuffer(186140);
		Charset charset = Charset.forName("GBK");
		try {
			FileInputStream fileInputStream = new FileInputStream(f);
			//用于读取、写入、映射和操作文件的通道
			FileChannel fileChannel = fileInputStream.getChannel();
			while (fileChannel.read(byteBuffer)!= -1){
				//反转此缓冲区
				byteBuffer.flip();
				CharBuffer charBuffer = charset.decode(byteBuffer);
				stringBuffer.append(charBuffer.toString());
				byteBuffer.clear();
			}
			 fileInputStream.close();  
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return stringBuffer.toString();  
	}
	
	//获取文件数据	
	public  static void getData(String path){

		String file = path;
		String content = readFile(file);
		
		//将内容以换行符转成数组
		String[] rowsContents = content.split("\r\n");
		//内容总共行数
		int rowCount = rowsContents.length;
		//集合、存储数据
		int j = 0;
		for(int i = 0; i<rowCount; i++){
			
			String rowContent = rowsContents[i];
			//以一个或多个空格分割字符串
			String[] rowArgs = rowContent.split("[' ']+");
			
			if(i==4) {
				//vehicles
				vehicleNum = Integer.parseInt(rowArgs[1]);
				//capacity
				capacity = Integer.parseInt(rowArgs[2]);
				System.out.println(vehicleNum);
				System.out.println(capacity);
			}
			
			else if(i>8) {
				//customer status
				routeState[j] = true;
				//X cood.
				coordinate[j][0] =Double.parseDouble(rowArgs[2]);
				//Y
				coordinate[j][1] = Integer.parseInt(rowArgs[3]);
				//demand
				demand[j] = Integer.parseInt(rowArgs[4]);
				//ready time
				timeWindow[j][0] = Double.parseDouble(rowArgs[5]);
				//due date
				timeWindow[j][1] = Double.parseDouble(rowArgs[6]);
				//service time				
				serviceTime[j] = Double.parseDouble(rowArgs[7]);
				System.out.println(j + "\t"+ routeState[j]+ "\t" +coordinate[j][0] +"\t"+ coordinate[j][1] +"\t"+ demand[j] +"\t"+ timeWindow[j][0] +"\t" + timeWindow[j][1] +"\t"+ serviceTime[j]);
				j++;
			}
		}				
	}
	
	//行车路径方案
	public static void vehicleRoutes(){
		//初始化
		int[] totalWeight = new int[30];		
		int u=0;int p=0;int points= 0;
		routeState[0] = false;
		//首先将路径参数定义为一个不可能值，表示该schedule点尚未有user
		for(int i=0;i<vehicleNum;i++){
			for(int j=0;j<102;j++){				
				route[i][j] = 200;
			}
		}		
		//构建路径
		for(int i=0;i<vehicleNum;i++){
		//int i=0;
			//if(u==-2) break;
			route[i][0]=0;//路径开始为0			
			route[i][1]=maxDistance(0);	//从一个点开始
			//route[i][1]=minDueTime();	
			routeState[route[i][1]]=false;//该点设置被访问过
			totalWeight[i] = demand[route[i][1]];//更新路径总容量
			routedUsers++;//路径+1,
			route[i][2]=0;
			for(int j=0;j<101;j++){
				u = bestInsertUser(i);//得到要插入第i条路径的点
				if(u==-1){
					break;
				}
				else if(u==-2){
					break;
				}
				else/* if(u>0)*/{
					u = bestInsertUser(i);p=pStar[u];
					//得到有意义的schedule点数					
					while(route[0][points] !=200){
						points++;
					}
					for(int k=points;k>p;k--)
						route[i][k]=route[i][k-1];
					//if(i==2){System.out.println("InsertedU&p="+u+"	"+p);}
						route[i][p]=u;
						routeState[u]=false;														
				}
				
			}
		}
		
		int a=0;
	for(int l=0;l<101;l++){
		if(routeState[l]) a++;}
	 System.out.println("unrouted="+a+ routeState[2]);
	}
	
	//距离矩阵
	public static void distanceMatrix(int i, int j){
		double square;
		for(i=0;i<101;i++){
			for(j=0;j<101;j++){
			 square = Math.pow(coordinate[i][0] - coordinate[j][0],2) + Math.pow(coordinate[i][1] - coordinate[j][1],2);
			distance[i][j]=Math.pow(square, 0.5);
			}
		}
		
	}
		
	//求距离j最远的unrouted客户
	public static int  maxDistance(int j){
		double distance1;
		double temp = 0;
		int t = 0;
		for(int i=1;i<101;i++){
		distance1 = distance[i][j] ;
		if(distance1 > temp && routeState[i] ){
				temp = distance1;
				t = i;
			}
		}
		return t;
		
	}
	
	//求最近关门时间的unrouted客户
	public static int minDueTime(){
		double time;
		double temp = 10000;
		int j=0;		
		for(int i=1;i<101;i++){
		time = timeWindow[i][1];
		if(time < temp && routeState[i] ){
				temp = time;
				j = i;
			}
		}
		return j;
	}
	
	//求最大值
	public static double max(double a,double b){
		return a > b ? a : b ;
	}
	
	//针对点u求最佳插入位置
	public static void bestInsertPlace(int routei, int u){
		//标准参数
		double alpha1=1;
		double alpha2=0;
		double miu=1;
		
		int pStar1 =-1;
		int[] tempRoute = new int[102];
		double[] beginTime = new double[102];
		double[] newBeginTime = new double[2];
		double[] waitingTime= new double[101];		
		double[] pushForward = new double[101];
		int totalWeight=0 ;
		
		double[] c11 = new double[101];
		double[] c12 = new double[101];
		boolean feasibility = true;
		boolean insertble = false;
		int points= 0;
		double t;
		double temp=0;
		//int p1;
		//负值表示该插入处不可行
		for(int i=0;i<101;i++){
			c11[i]=-1;
			c12[i]=-1;
			c1[i]=-1;
		}
		//当前路径的总需求和路径上的点数
		
		while(route[routei][points] !=200)
			totalWeight =totalWeight +demand[route[routei][points++]];				
		//System.out.println(totalWeight);
				
		//求路径上各个点的beginTime和waitingTime
		beginTime[route[routei][0]]=0;
		for(int i=1;i<points-1;i++){
			//即前一点的beginTime加serviceTime和distance		
		t=beginTime[route[routei][i-1]]+serviceTime[route[routei][i-1]]+distance[route[routei][i-1]][route[routei][i]];
		beginTime[route[routei][i]]=max(t,timeWindow[route[routei][i]][0]);
		waitingTime[route[routei][i]]=beginTime[route[routei][i]]-timeWindow[route[routei][i]][0];
		}
				
		if(totalWeight+demand[u] > capacity)
			pStar[u]=-1;
		else{
		//针对某一点u寻找可行且最合适的插入位置(每次插入在p前)
		for(int p=1;p<points;p++){
			feasibility=true;
		//把当前路径复制给临时路径变量
		for(int i=0;i<102;i++)
			tempRoute[i]=route[routei][i];
		//插入
						
		//先得到新路径		
			for(int i=points;i>p;i--)						
			tempRoute[i]=tempRoute[i-1];
			
			tempRoute[p]=u;
			t= beginTime[tempRoute[p-1]]+serviceTime[tempRoute[p-1]]+distance[tempRoute[p-1]][tempRoute[p]];
			newBeginTime[0]=max(t,timeWindow[u][0]);
			//System.out.println(newBeginTime[0]+"\t'"+t+"\t"+ timeWindow[u][0]);
			
			t = newBeginTime[0]+serviceTime[tempRoute[p]]+distance[tempRoute[p]][tempRoute[p+1]];
			//System.out.println(t);
			newBeginTime[1]=max(t,timeWindow[tempRoute[p+1]][0]);
			
		//PF时间
		pushForward[tempRoute[p+1]] = newBeginTime[1]-beginTime[tempRoute[p+1]];
		//System.out.println(pushForward[tempRoute[p+1]]);
		if(p<points-1){	
		for(int i=p+2;i<points+1;i++)
			pushForward[tempRoute[i]]= max(0,pushForward[tempRoute[i-1]]-waitingTime[tempRoute[i]]);		
		}
		
		//检验可行性
		
		if (newBeginTime[0] > timeWindow[u][1]){
			//System.out.println(newBeginTime[0]+"\t"+timeWindow[u][1]);
				feasibility =false;	}			
		else for(int i=p+1;i<points+1;i++){
			
			if(p==points-1) {
			if(newBeginTime[1]>timeWindow[0][1]) feasibility =false;
				
//System.out.println(points+ "\t"+ p+"\t"+u+"\t"+tempRoute[i-2]+"\t"+beginTime[i-2]+"\t"+pushForward[tempRoute[i-2]]+"\t"+timeWindow[tempRoute[i]][1]+"\t");
			}
			else {
			if((t= beginTime[tempRoute[i]]+pushForward[tempRoute[i]]) >timeWindow[tempRoute[i]][1]){
				feasibility =false;				
				break;
				}
			 }
			
		if(feasibility) {
			
			c11[p]=distance[u][route[routei][p-1]] + distance[u][route[routei][p]] - miu*distance[route[routei][p-1]][route[routei][p]];
			c12[p]=newBeginTime[1] - beginTime[route[routei][p]];
			c1[p] =alpha1*c11[p] + alpha2*c12[p];
			insertble=true;
		}
		}				
		}
		//比较得到最优的c1
		if(insertble)
			for(int p=1;p<points;p++){
				if(c1[p] > 0){
				temp=c1[p];
				pStar1=p;
				//System.out.println("temp ="+p);
				break;
				}		
			}
			for(int p=1;p<points;p++){
				if(c1[p] > 0&&c1[p] < temp){				
					temp=c1[p];
				//System.out.println(p);
				pStar1 = p;
				}
			}
			pStar[u]=pStar1;
		}			
				
	}
	
	//针对每个可插入点计算c2并求最优插入点
	public static int bestInsertUser(int routei){
		double[] c2 =new double[101];
		
		//表示全部已插入完毕
		//表示存在某些点未插入但本路径无可再插入点
		boolean insertble=false;
		//boolean tempBoolean=false;
		int uStar=-1;
		int points=0;
		double temp=0;
		double lamda=1;
		String str="";
				
		int a=0;
		for(int l=0;l<101;l++){
			if(!routeState[l]) a++;}
		if(a==101){
			System.out.println("schedule complete!");
			uStar =-1;
		}		
		else for(int i=1;i<101;i++){
			if(routeState[i]){
				bestInsertPlace(routei,i);
				if(pStar[i]>0) {
					//tempBoolean=true;}
					//insertble= insertble||tempBoolean;
					insertble = true;
					break;}
			}			
		}
		
		if(!insertble) {
			System.out.println("route"+routei+"complete!");
			//若本路径无再可插入点则返回-2并打印该路径
			while(route[routei][points]!=200){
				str=str+route[routei][points] +"-";
				points++;
				}
			System.out.println(str);
			uStar=-2;
		} 
		else {
		for(int i=1;i<101;i++){
			if(routeState[i]){		
			bestInsertPlace(routei,i);
			 if(pStar[i] >0)
				c2[i] =lamda*distance[0][i] - c1[pStar[i]];          
				temp = c2[i];				
				break;
				}
			}
		
		for(int i=1;i<101;i++){
			if(routeState[i]){
			bestInsertPlace(routei,i);
			if(pStar[i] >0){
				c2[i] =lamda*distance[0][i] - c1[pStar[i]];
				if(temp <= c2[i]){
					temp = c2[i];
					uStar=i;
					}
				}
			}
		}
		}
		return uStar;			
	}
		
	//打印路径
	public static void printRoutei(int routei){
		String str ="";
		int points=0;
		while(route[routei][points]!=200){
				str=str+route[routei][points] +"-";
				points++;
				}
			System.out.println("route"+routei+"="+str);			
		}

		

}