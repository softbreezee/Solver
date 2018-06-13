package twk5.algorithm;

/**
 * 边
 * 值
 * @author Leon
 * @param <Weight>
 *
 */
public class Edge<Weight extends Number & Comparable> implements Comparable<Edge> {
	private int a,b;//边的两个端点
	private Weight weight;// 边的权重
	
	public Edge(int a,int b,Weight weight){
		this.a = a;
		this.b = b;
		this.weight = weight;
	}   
	public Edge(Edge<Weight> e){
		//用于替换
        this.a = e.a;
        this.b = e.b;
        this.weight = e.weight;
	}
	public int v(){return a;}//返回第一个点
	public int w(){return b;}//返回第二个点
	public Weight wt(){return weight;}//返回权重

	//给定一个点，返回另一个点
	public int other(int x){
		assert x==a || x==b;
		return x == a ? b : a;
	}
	
	//输出边的信息
	public String toString(){
		
		return ""+a+"-"+b+":"+weight;
		
	}
	@Override
	public int compareTo(Edge that) {
		if(weight.compareTo(that.wt())<0){
			return -1;
		}else if(weight.compareTo(that.wt())>0){
			return 1;
		}else{
			return 0;
		}
	}
	
	

}
