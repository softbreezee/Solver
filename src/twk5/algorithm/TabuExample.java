package twk5.algorithm;

import java.util.Random;

public class TabuExample {

	/**
	 * 主动禁忌搜索算法需要的参数 1、迭代次数 2、每次搜索邻居的个数 3、禁忌长度 4、城市数量？？节点个数 5、距离矩阵 6、最佳出现代数？？？
	 * 7、初始路径编码？？？ 8、当代最好的编码 9、存放临时编码 10、禁忌表 11、当前代数
	 */

	/**
	 * 
	 * @param n
	 *            编码长度
	 * @param g
	 *            迭代次数
	 * @param c
	 *            每次搜索邻居个数
	 * @param m
	 *            禁忌长度
	 */
	public TabuExample(int n, int g, int c, int m) {

	}

	/**
	 * 主动禁忌搜索算法需要的参数 1、迭代次数 2、每次搜索邻居的个数 3、禁忌长度 4、城市数量？？节点个数 5、距离矩阵 6、最佳出现代数？？？
	 * 7、初始路径编码？？？ 8、当代最好的编码 9、存放临时编码 10、禁忌表 11、当前代数
	 */

	/**
	 * 
	 * @param n
	 *            编码长度
	 * @param g
	 *            迭代次数
	 * @param c
	 *            每次搜索邻居个数
	 * @param m
	 *            禁忌长度
	 */
	public TabuExample(int n, int g, int c, int m) {

	}

	/**
	 * 初始化Tabu算法类 ，设置对应的参数
	 * 
	 * @param filename
	 *            数据文件名，该文件存储所有城市节点坐标数据
	 * @throws IOException
	 */
	private void init(String filename) throws IOException {
		// 读取数据
		// 计算距离矩阵

		Ghh = new int[cityNum]; // 初始路径编码，数组长度是城市个数？？？节点的个数？？
		bestGh = new int[cityNum]; // 最好路径的编码，
		bestEvaluation = Integer.MAX_VALUE; // 对解的评价，对那个解的评价？？？
		LocalGhh = new int[cityNum]; // 当前最好的编码，
		localEvaluation = Integer.MAX_VALUE; // 对当前所求到的最好的路径的编码的评价
		tempGhh = new int[cityNum]; // 存放临时的编码
		tempEvaluation = Integer.MAX_VALUE; // 对临时编码的评价

		jinji = new int[ll][cityNum]; // 晋级表，ll为禁忌表的长度，cityNum为编码的长度
		bestT = 0; // 最佳编码出现的代数，迭代的次数从0开始么？？？？？
		t = 0; // 当前代数

		random = new Random(System.currentTimeMillis());
		/*
		 * for(int i=0;i<cityNum;i++) { for(int j=0;j<cityNum;j++) {
		 * System.out.print(distance[i][j]+","); } System.out.println(); }
		 */

	}

	/**
     * 初始化编码Ghh
     * 对每个节点都进行编码，每个节点都是不相同的随机数
     * 方法只有一种么？
     */
    void initGroup(){
    	int i, j;
    	Ghh[0] = //该数组的长度是城市的个数
    	for(i=1;i<cityNum;){//注意角标是从1开始
    		Ghh[i] = random.nextInt(65535)%cityNum;
    		
    		for(j = 0;j<i;j++){//对第i个编码判断，i前面的编码不能与第i个编码相同
                if (Ghh[i] == Ghh[j]) {  
                    break;  
                }
    		}
    		
    		if(j == i){ //当i前面的节点编码完成之后，对i+1进行编码
    			i++;
    		}
    		
    	}
    	
    }

	// 复制编码体，复制编码Gha到Ghb
	public void copyGh(int[] Gha, int[] Ghb) {
		for (int i = 0; i < cityNum; i++) {
			Ghb[i] = Gha[i];
		}
	}

	/**
	 * 计算路线的总距离
	 * 
	 * @param chr
	 *            参数是编码Ghh,也就是一个解
	 * @return 返回路线的总距离
	 */
	public int evaluate(int[] chr) {
		// 0123
		int len = 0;
		// 编码，起始城市,城市1,城市2...城市n
		// 从起始城市到城市1，城市2一直到城市n，0-1-2-3-...-n
		for (int i = 1; i < cityNum; i++) {
			len += distance[chr[i - 1]][chr[i]];
		}
		// 城市n到起始城市 ,n-0
		len += distance[chr[cityNum - 1]][chr[0]];
		return len;
	}

	/**
	 * 对两个解的编码进行邻域交换，得到邻居？？？
	 * 
	 * @param Gh
	 *            当前的编码
	 * @param tempGh
	 *            邻域交换之后的临时编码
	 */
	public void neighbor(int[] Gh, int[] tempGh) {
		int i, temp;
		int ran1, ran2;

		//
		for (i = 0; i < cityNum; i++) {
			tempGh[i] = Gh[i];
		}
		ran1 = random.nextInt(65535) % cityNum;
		ran2 = random.nextInt(65535) % cityNum;
		while (ran1 == ran2) {
			ran2 = random.nextInt(65535) % cityNum;
		}

		// 对临时的编码，两个位置的编码对换
		// 注意：初始化编码的时候，编码的数组里面放的是随机的城市编码，节点编号，这个随机数是能和角标对应的
		// 例如：
		// 0 1 2 3 4 5角标
		// 4 2 3 5 1 0数组存放的节点
		// 这里随机取了两个位置的元素（节点）进行了对换
		temp = tempGh[ran1];
		tempGh[ran1] = tempGh[ran2];
		tempGh[ran2] = temp;
	}

	/**
	 * 判断编码是否在禁忌表中
	 * 
	 * @param tempGh
	 *            当前的编码
	 * @return
	 */
	public boolean panduan(int[] tempGh) {
		int i, temp;
		int ran1, ran2;

		for (i = 0; i < cityNum; i++) {
			tempGh[i] = Gh[i];
		}
		ran1 = random.nextInt(65535) % cityNum;
		ran2 = random.nextInt(65535) % cityNum;
		while (ran1 == ran2) {
			ran2 = random.nextInt(65535) % cityNum;
		}
		temp = tempGh[ran1];
		tempGh[ran1] = tempGh[ran2];
		tempGh[ran2] = temp;
	}

	/**
	 * 判断编码是否在禁忌表中
	 * 
	 * @param tempGh
	 * @return
	 */
	public int isInTabu(int[] tempGh) {
		int i, j;
		int flag = 0;
		for (i = 0; i < ll; i++) { // ll为禁忌表的长度
			flag = 0;
			for (j = 0; j < cityNum; j++) {
				if (tempGh[j] != jinji[i][j]) { // 因为禁忌表中存放的数组地址可能不一样，只能判断其中的元素
					flag = 1;// 不相同
					break;
				}
			}
			if (flag == 0)// 相同，返回存在相同
			{
				// return 1;
				break;
			}
		}
		if (i == ll)// 不等
		{
			return 0;// 不存在
		} else {
			return 1;// 存在
		}

	}

	/**
	 * 更新禁忌表 移除表中的第一个解，添加当前解至表的最后 解禁忌与加入禁忌
	 * 
	 * @param tempGh
	 *            待添加至禁忌表的解
	 */
	public void addSolution(int[] tempGh) {
		int i, j, k;
		// 删除禁忌表第一个编码，后面编码往前挪动
		for (i = 0; i < ll - 1; i++) {
			for (j = 0; j < cityNum; j++) {
				jinji[i][j] = jinji[i + 1][j];
			}
		}

		// 新的编码加入禁忌表
		for (k = 0; k < cityNum; k++) {
			jinji[ll - 1][k] = tempGh[k];
		}

	}

	/**
	 * 求解
	 */
	public void solve() {
		int nn;
		/*
		 * 初始化编码Ghh
		 */
		initGroup(); // 可以使用别的算法求解的一个初始解
		copyGh(Ghh, bestGh);// 复制当前编码Ghh到最好编码bestGh
		/*
		 * 评价解
		 */
		bestEvaluation = evaluate(Ghh); // 对解的评价，评价标准就是路径的长度

		/*
		 * 进行迭代
		 */
		while (t < MAX_GEN) { // 迭代
			nn = 0;
			localEvaluation = Integer.MAX_VALUE;
			while (nn < N) {
				/*
				 * 邻域交换
				 */
				neighbor(Ghh, tempGhh);// 得到当前编码Ghh的邻域编码tempGhh
				if (isInTabu(tempGhh) == 0)// 判断编码是否在禁忌表中
				{
					// 不在
					tempEvaluation = evaluate(tempGhh);
					if (tempEvaluation < localEvaluation) {
						copyGh(tempGhh, LocalGhh);
						localEvaluation = tempEvaluation;
					}
					nn++;
				}
			}
			if (localEvaluation < bestEvaluation) {
				bestT = t;
				copyGh(LocalGhh, bestGh);
				bestEvaluation = localEvaluation;
			}
			copyGh(LocalGhh, Ghh);

			// 解禁忌表，LocalGhh加入禁忌表
			addSolution(LocalGhh);
			t++;
		}

		System.out.println("最佳长度出现代数：");
		System.out.println(bestT);
		System.out.println("最佳长度");
		System.out.println(bestEvaluation);
		System.out.println("最佳路径：");
		for (int i = 0; i < cityNum; i++) {
			System.out.print(bestGh[i] + ",");
		}
	}

	public static void main(String[] args) {
		System.out.println(new Random(System.currentTimeMillis()).nextInt(65535) % 48);
	}

}
