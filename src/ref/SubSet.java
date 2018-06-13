package ref;
//作者：超越

//链接：https://www.zhihu.com/question/29985661/answer/46501393
//来源：知乎
//著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

import java.util.HashSet;
import java.util.Set;

public class SubSet {

	public static void main(String[] args) {
		int[] set = new int[]{0,1,2};
		Set<Set<Integer>> result = getSubSet(set);	//调用方法
		
		//输出结果
		for(Set<Integer> subSet: result){
			
			if(subSet.size()!=0){
				for(Integer num: subSet){
					System.out.print(num);
				}
				System.out.println("");
			}
		}
	}

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