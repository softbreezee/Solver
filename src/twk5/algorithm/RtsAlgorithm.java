package twk5.algorithm;

import java.util.Arrays;
import java.util.Vector;

public class RtsAlgorithm {

	// 全邻域搜索
	/**
	 * 
	 * @param v 领域映射后的解的集合
	 * @param x 初始解
	 * @param costbest 每个解对应的目标值
	 * @param firstcity 起始城市的角标
	 */
	void localsearchWNS(Vector<Vector<Integer>> v, Vector<Integer> x, int costbest, int firstcity) {
		swap(x, 0, firstcity); // 将起始城市置于最开始
		costbest = countDis(x, v);// countDis用于计算花费，代码在此不再贴出
		Vector<Integer> tempx = x;
		int num = x.size();
		while (true) {
			int min = costbest;
			//邻域映射，对换两个城市
			for (int i = 1; i < num - 1; i++) {
				for (int j = i + 1; j < num; j++) {
					swap(x[i], x[j]);//邻域映射的策略
					int temp = countDis(x, v);//每次对换后，都进行计算
					if (temp < min) {
						min = temp;
						tempx = x;
					}
					swap(x[i], x[j]);
				}
			}
			if (min == costbest) {
				break;
			}
			costbest = min;
			x = tempx;
		}
	}

	/**
	 * vector元素互换
	 * 对换角标i,j位置的元素
	 * 
	 * @param v
	 * @param i
	 * @param j
	 */
	private void swap(Vector<Integer> v, int i, int j) {
		// TODO Auto-generated method stub
		Integer elei = v.get(i);
		v.add(i, v.get(j));
		v.remove(i + 1);
		v.add(j, elei);
		v.remove(j + 1);

	}

	public static void main(String[] args) {
		Vector<Integer> v = new Vector<Integer>();
		// 12345
		v.add(1);
		v.add(2);
		v.add(3);
		v.add(4);
		v.add(5);
		for (Integer i : v) {
			System.out.println(i);
		}
		new RtsAlgorithm().swap(v, 1, 2);// 交换2/3

		for (Integer i : v) {
			System.out.println(i);
		}

	}

}
