package twt2;
/////////////////////////////////////////////////////////////////////////////
//                                                       
//            
//     Created:               110118 Xue Zhaojie
//     1st version Finished:  110118 Xue Zhaojie
//     Modified:              110221 Xue Zhaojie
//			   
//  
//
//////////////////////////////////////////////////////////////////////////////

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import org.junit.Test;

import com.sun.jmx.snmp.tasks.TaskServer;

/**
 * ������������
 * û������ʵ�ʵľ�����󣬶���ͨ������õ�
 * @author Leon
 *
 */
public class Generator {

	int ofNum = 0;//
	int ifNum = 0;//
	int ieNum = 0;//
	int oeNum = 0;//
	int numofCustomers = 0;//�ͻ�����
	int numofTasks = 0;//����������
	int numofStock = 0;//�ѳ�����
	int numofPort = 0;//�ۿ�����
	double tMAx = 0;//������ʻ��ʱ��
	double loadCar = 0.083;//����װ��ʱ��
//	double driveTime[][];//ʵ�ʵ���֮�����ʻʱ��
	double[][] tw1;//��һʱ�䴰
	double[][] tw2;//�ڶ�ʱ�䴰


	double[] loadTime;//װж����ʱ��
	double[][] tij;//��ת��ʱ��
	double[] serviceTime;//�������ʱ��
	double[][] nodeTW;//����ʱ�䴰
	
	double[][] coordinate;//����

	@Test
	public void test() throws Exception{
		Generator g = new Generator();
//		g.generate(2, 3, 2);
		g.generate(4, 5, 2, 2, 1, 2);
		g.fileOutput();
//		int str = 7;
//		File file = new File("D:/datafile" + str + ".txt");
//		FileReader r = new FileReader(file);
//		
//		g.fileInput(r);
		
//		System.out.println(g.ofNum);
//		System.out.println(g.ifNum);
//		System.out.println(g.ieNum);
//		System.out.println(g.oeNum);
//		System.out.println(g.numofCustomers);
//		System.out.println(g.numofPort);
//		System.out.println(g.numofStock);
//		System.out.println(g.numofTasks);
//		System.out.println(g.loadCar);
//		System.out.println(g.loadTime[1]);
//		System.out.println(g.tij[2][2]);
//		System.out.println(g.serviceTime[2]);
//		System.out.println(g.nodeTW[1][2]);
//		System.out.println(g.coordinate[6][1]);
		
		
	}
	
	@Test
	public void testMaxVeichal() throws Exception{
		//��ʼ��
		Generator g = new Generator();
		//���ļ�
		int str =8;
		File file = new File("D:/����2��������/���ѳ�/datafile" + str + ".txt");
		FileReader r = new FileReader(file);
		g.fileInput(r);
		
		double veichal = g.getMaxVeichal();
		System.out.println(veichal/8);
		
	}
	
	
	
	
	/**
	 * �õ������ó���
	 * @return
	 */
	public double getMaxVeichal(){
		double totalTime = 0;
		for(int i = 0;i<serviceTime.length;i++){
			totalTime += serviceTime[i];
		}
		double[] mindis1 = new double[numofTasks];
		double[] mindis2 = new double[numofTasks];
		for(int i = 0;i<numofTasks;i++){
			mindis1[i] = 100;
			mindis2[i] = 100;
		}
		for(int i = 0;i<numofStock;i++){
			for(int j = numofStock;j<numofStock+numofTasks;j++){
				if(mindis1[j-numofStock]>tij[i][j]){
					mindis1[j-numofStock] = tij[i][j];
				}
			}
		}
		for(int i = numofStock;i< numofTasks+numofStock;i++){
			for(int j = 0;j< numofStock;j++){
				if(mindis2[i-numofStock]>tij[i][j]){
					mindis2[i-numofStock] = tij[i][j];
				}	
			}
		}
		for(int i = 0;i<numofTasks;i++){
			totalTime += mindis1[i];
		}
		for(int i = 0;i<numofTasks;i++){
			totalTime += mindis2[i];
		}
		
		
		return totalTime;
	}
	
	
	
	/**
	 * �������񡢶ѳ��������ۿڸ�������Ϣ����������������
	 * @param ofNum
	 * @param ifNum
	 * @param ieNum
	 * @param oeNum
	 * @param numofPort
	 * @param numofStock
	 * @throws Exception 
	 */
	public void generate(int ofNum, int ifNum,int ieNum,int oeNum,int numofPort,int numofStock) throws Exception{
		this.ofNum = ofNum;
		this.ifNum = ifNum;
		this.ieNum = ieNum;
		this.oeNum = oeNum;

		numofCustomers = ifNum + ofNum;
		numofTasks = ifNum+ofNum+ieNum+oeNum;
		this.numofStock = numofStock;
		this.numofPort = numofPort;

		loadTime = new double[numofCustomers];
		tij = new double[numofTasks + numofStock][numofTasks + numofStock];
		//������������ɰ��ոۿڡ��ѳ����ͻ����˳������
		coordinate = new double[numofPort + numofStock + numofTasks][2];

		// ��������
		double areaParameter = 200; // �����С����,����
		
		//�����ۿ�/�ѳ�/�ͻ�������꣬ieoeifof
			
			coordinate[0][0] = myRound(Math.random() * areaParameter, 2);
			coordinate[0][1] = myRound(Math.random() * areaParameter, 2);
		if(numofStock==1){
			coordinate[1][0] = areaParameter/2;
			coordinate[1][1] = areaParameter/2;
		}else if(numofStock==2){
			coordinate[1][0] = areaParameter/4;
			coordinate[1][1] = areaParameter/4;
			coordinate[2][0] = areaParameter/4+areaParameter/2;
			coordinate[2][1] = areaParameter/4+areaParameter/2;
		}else if(numofStock==3){
			coordinate[1][0] = myRound(areaParameter/2,2);
			coordinate[1][1] = myRound(areaParameter*2/3, 2);
			coordinate[2][0] = myRound(areaParameter/3, 2);
			coordinate[2][1] = myRound(areaParameter/3, 2);
			coordinate[3][0] = myRound(areaParameter*2/3, 2);
			coordinate[3][1] = myRound(areaParameter/3, 2);
		}else if(numofStock==4){
			coordinate[1][0] = areaParameter/4;
			coordinate[1][1] = areaParameter/4;
			coordinate[2][0] = areaParameter*3/4;
			coordinate[2][1] = areaParameter/4;
			coordinate[3][0] = areaParameter/4;
			coordinate[3][1] = areaParameter*3/4;
			coordinate[4][0] = areaParameter*3/4;
			coordinate[4][1] = areaParameter*3/4;
		}else{
			for (int i = numofPort ; i < numofPort + numofStock; i++) {
				coordinate[i][0] = myRound(Math.random() * areaParameter, 2);
				coordinate[i][1] = myRound(Math.random() * areaParameter, 2);
			}
		}
		
		for (int i = numofPort + numofStock ; i < numofPort + numofStock + numofTasks; i++) {
			
			coordinate[i][0] = myRound(Math.random() * areaParameter, 2);
			coordinate[i][1] = myRound(Math.random() * areaParameter, 2);
		}

		
		//��ת��ʱ��
//		double[] drive0 = Arrays.copyOfRange(tij[0], numofPort, numofPort + numofStock);
		double[] drive0 = new double[numofStock];
		for(int k = 0;k<numofStock;k++){
			drive0[k] = distance(0, k+numofPort);
		}
		Arrays.sort(drive0);

		for (int i = 0; i < numofTasks + numofStock; i++) {
			// i ���� �ѳ�
			if (i < numofStock) {
				for (int j = 0; j < numofTasks + numofStock; j++) {
					// j���� �ѳ�
					if (j < numofStock) {
						tij[i][j] = 0;
					}
					// j���� OE
					else if (j >= numofStock + ieNum && j < numofStock + ieNum + oeNum) {
						tij[i][j] = loadCar + distance(i + numofPort,0);
					}
					// j���� OF
					else if (j >= numofStock + ieNum + oeNum + ifNum && j < numofTasks + numofStock) {
						tij[i][j] = myRound(loadCar
								* 2
								+ distance(i + numofPort,j + numofPort - ieNum - oeNum),2);
						// tij[i][j] = LOADTIME * 2 + driveTime[i +
						// numofPort][j + numofPort];
					}
					// j���� IF U IE
					else {
						tij[i][j] = myRound(distance(i + numofPort,0),2);
					}
				}
			}
			// i ���� IE
			else if (i >= numofStock && i < numofStock + ieNum) {
				for (int j = 0; j < numofTasks + numofStock; j++) {
					// j���� �ѳ�
					if (j < numofStock) {
						tij[i][j] = myRound(loadCar + distance(0,j + numofPort),2);
					}
					// j���� OE
					else if (j >= numofStock + ieNum
							&& j < numofStock + ieNum + oeNum) {
						tij[i][j] = 0;
					}
					// j���� OF
					else if (j >= numofStock + ieNum + oeNum + ifNum
							&& j < numofTasks + numofStock) {
						tij[i][j] = myRound(loadCar
								+ distance(0,j + numofPort - ieNum - oeNum),2);
						// tij[i][j] = LOADTIME + driveTime[0][j + numofPort
						// ];
					}
					// j���� IF U IE
					else {
						tij[i][j] = myRound(loadCar + drive0[0] * 2,2);
					}
				}
			}
			// i ���� IF
			else if (i >= numofStock + ieNum + oeNum
					&& i < numofStock + ieNum + oeNum + ifNum) {
				for (int j = 0; j < numofTasks + numofStock; j++) {
					// j���� �ѳ�
					if (j < numofStock) {
						tij[i][j] = myRound(loadCar
								* 2
								+ distance(i + numofPort - ieNum - oeNum,j
										+ numofPort),2);
					}
					// j���� OE
					else if (j >= numofStock + ieNum
							&& j < numofStock + ieNum + oeNum) {
						tij[i][j] = myRound(loadCar
								+ distance(i + numofPort - ieNum - oeNum,0),2);
					}
					// j���� OF
					else if (j >= numofStock + ieNum + oeNum + ifNum
							&& j < numofTasks + numofStock) {
						if (distance(i + numofPort - ieNum - oeNum,j + numofPort
								- ieNum - oeNum) > 0) {
							tij[i][j] = myRound(loadCar
									* 2
									+ distance(i + numofPort - ieNum - oeNum,j
											+ numofPort - ieNum - oeNum),2);
						} else {
							tij[i][j] = 0;
						}
					}
					// j���� IF U IE
					else {
						//�����˵�i�У���
//						double[] ds = Arrays.copyOfRange(driveTime[i + numofPort - ieNum - oeNum], numofPort, numofStock + numofPort);
						double ds[] = new double[numofStock];
						double drive0_[] = new double[numofStock];
						double min = 100;
						for(int k = 0;k<numofStock;k++){
							drive0_[k] = distance(0, k+numofPort);
							ds[k] = distance(i + numofPort - ieNum - oeNum, k+numofPort);
							double temp = drive0_[k]+ds[k];
							if(min>temp){
								min = temp;
							}
						}
						if(min == 100){
							throw new Exception("ת��ʱ����㲻��ȷ");
						}
						Arrays.sort(ds);
						tij[i][j] = myRound(loadCar * 2 + min,2);
					}
				}

			}
			// i ���� OF U OE
			else {
				for (int j = 0; j < numofTasks + numofStock; j++) {

					// j���� �ѳ�
					if (j < numofStock) {
						tij[i][j] = myRound(distance(0,j + numofPort),2);
					}
					// j���� OE
					else if (j >= numofStock + ieNum
							&& j < numofStock + ieNum + oeNum) {
						tij[i][j] = myRound(drive0[0] * 2 + loadCar,2);
					}
					// j���� OF
					else if (j >= numofStock + ieNum + oeNum + ifNum
							&& j < numofTasks + numofStock) {
//						double[] ds = Arrays.copyOfRange(distance[j + numofPort	- ieNum - oeNum], numofPort, numofStock + numofPort);
						double ds[] = new double[numofStock];
						double drive0_[] = new double[numofStock];
						double min = 100;
						for(int k = 0;k<numofStock;k++){
							drive0_[k] = distance(0, k+numofPort);
							ds[k] = distance(j + numofPort - ieNum - oeNum - ifNum, k+numofPort);
							double temp = drive0_[k]+ds[k];
							if(min>temp){
								min = temp;
							}
						}
						if(min == 100){
							throw new Exception("ת��ʱ����㲻��ȷ");
						}
						tij[i][j] = myRound(loadCar * 2 +min,2);
					}
					// j���� IF U IE
					else {
						tij[i][j] = 0;
					}
				}

			}
		}


		//��װжʱ������ɣ���5min~60min֮��
		for (int i = 0; i < loadTime.length; i++) {
			loadTime[i] = 0.083 + 0.917 * Math.random();
			loadTime[i] = myRound(loadTime[i], 3);
		}
		
		
		// ��һʱ�䴰
		tw1 = new double[2][numofTasks];
		// ע�⣺�������Ӧ�ó˵��Ƿ�Χ����
		for (int i = 0; i < numofTasks; i++) {
			//��һʱ�䴰��8��00 - 12��00�������0-3Сʱ
			tw1[0][i] = myRound(Math.random() * 4,2);
			tw1[1][i] = myRound(tw1[0][i] + Math.random() * 3,2);
		}
		// �ڶ�ʱ�䴰,ֻ�����������еڶ�ʱ�䴰
		int num = ifNum + ofNum;
		tw2 = new double[2][num];
		// ע�⣺�������Ӧ�ó˵��Ƿ�Χ����
		for (int i = 0; i < num; i++) {
			// �ڶ�ʱ�䴰�Ŀ�ʼʱ��Ϊ��һʱ�䴰�Ŀ�ʼʱ��+�ͻ�����ۿ�֮�����ʻ����
			tw2[0][i] = myRound(tw1[0][i + numofTasks - num] +distance(0,i+numofPort+numofStock),2);
			//�����2-5Сʱ
			tw2[1][i] = myRound(tw2[0][i] + (Math.random() * 3+2),2);
		}
		
		//�������ʱ��
		/**
		 * ����ʱ��
		 * ie��oe��if,of
		 */
		serviceTime = new double[numofTasks];
		for (int i = 0; i < numofTasks; i++) {
			// i����IE+OE
			if (i < (ieNum + oeNum)) {
				serviceTime[i] = loadCar;
			}
			// i����IF
			if (i >= (ieNum + oeNum) && i < (ifNum + ieNum + oeNum)) {
				serviceTime[i] = myRound((Math.max(
						(tw2[0][i - ieNum - oeNum] - tw1[1][i]),
						// ��һ���ͻ�����if0��Ӧ��
						(loadCar + distance(0,i + numofStock + numofPort - ieNum - oeNum))) 
						+ loadTime[i - ieNum - oeNum] + loadCar),2);
			}
			// i����OF
			if (i >= (ifNum + ieNum + oeNum) && i < numofTasks) {
				serviceTime[i] = myRound((Math.max(
						(tw2[0][i - ieNum - oeNum] - tw1[1][i]), (loadTime[i
								- ieNum - oeNum]
								+ loadCar + distance(i + numofStock + numofPort
								- ieNum - oeNum,0))) + loadCar),2);
			}
		}
		
		
		nodeTW = new double[2][numofTasks];
		//����ʱ�䴰
		for (int i = 0; i < numofTasks; i++) {
			// i����IE+OE
			if (i < (ieNum + oeNum)) {
				nodeTW[0][i] = tw1[0][i];
				nodeTW[1][i] = tw1[1][i];
			}
			// i����IF
			if (i >= (ieNum + oeNum) && i < (ifNum + ieNum + oeNum)) {
				nodeTW[0][i] = myRound(Math.min(
						Math.max(tw1[0][i], 
								tw2[0][i - ieNum - oeNum]- loadCar- distance(0,i - ieNum - oeNum + numofPort+ numofStock)
								)
								, tw1[1][i]),
								2);

				nodeTW[1][i] = myRound(Math.min(tw1[1][i], tw2[1][i - ieNum - oeNum]
						- loadCar
						- distance(0,i - ieNum - oeNum + numofPort + numofStock)),2);
			}
			// i����OF
			if (i >= (ifNum + ieNum + oeNum) && i < numofTasks) {
				double ta = Math.max(tw1[0][i], tw2[0][i - ieNum - oeNum]- loadCar- distance(i + numofPort + numofStock - ieNum	- oeNum,0)
						- loadTime[i - ieNum - oeNum]);

				nodeTW[0][i] = myRound(Math.min(ta, tw1[1][i]),2);

				nodeTW[1][i] = myRound(Math.min(tw1[1][i], tw2[1][i - ieNum - oeNum]
						- loadCar
						- distance(i + numofPort + numofStock - ieNum - oeNum,0)
						- loadTime[i - ieNum - oeNum]),2);
			}
		}
		
		
	}
	

	// ///////// д���Զ����ʽ�����ļ�
	public void fileOutput() throws IOException {
//		String outputFilePath = "/datafile" + ofNum + "-" + ifNum + ".txt";
		String outputFilePath = "D:/datafile" + numofTasks + ".txt";
		FileWriter fw = new FileWriter(outputFilePath);

		// �����������
		fw.write("# of ieNum\r\n" + ieNum + "\r\n");
		fw.write("# of ifNum\r\n" + ifNum + "\r\n");
		fw.write("# of oeNum\r\n" + oeNum + "\r\n");
		fw.write("# of ofNum\r\n" + ofNum + "\r\n");
		fw.write("# of customers\r\n" + numofCustomers + "\r\n");
		fw.write("# of tasks\r\n" + numofTasks + "\r\n");
		fw.write("# of stocks\r\n" + numofStock + "\r\n");
		fw.write("# of ports\r\n" + numofPort + "\r\n");
		
		// ���package time;
		fw.write("# of loadTime\r\n");
		for (int i = 0; i < loadTime.length; i++) {
			fw.write(loadTime[i] + "\r\n");
		}

//		System.out.println(tij[0][0]);
//		System.out.println(nodeTW);
		//�������ʱ�䴰
		fw.write("node timeWindow(xyxyxy)\r\n");
		for (int j = 0; j < nodeTW[0].length; j++) {
			for (int i = 0; i < 2; i++) {
				fw.write(nodeTW[i][j] + "\r\n");
			}
		}
		//����������ʱ��
		fw.write("node serviceTime\r\n");
		for (int j = 0; j < serviceTime.length; j++) {
				fw.write(serviceTime[j] + "\r\n");
		}
		// ���tij��ת��ʱ�䣬һ��һ�����
		fw.write("tij matrix\r\n");
		for (int i = 0; i < tij.length; i++) {
			for (int j = 0; j < tij[0].length; j++) {
				fw.write(tij[i][j] + "\r\n");
			}
		}
		// ���coordinate of port; ���ж���xxxx,yyyy
		fw.write("# of port coordinate(xxx,yyy)\r\n");
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < numofPort; i++) {
				fw.write(coordinate[i][j] + "\r\n");
			}
		}
		// ���coordinate of stock; ���ж���,xxxx,yyyy
		fw.write("# of stocks coordinate(xxxyyy)\r\n");
		for (int j = 0; j < 2 ; j++) {
			for (int i = numofPort; i < numofPort + numofStock; i++) {
				fw.write(coordinate[i][j] + "\r\n");
			}
		}
		// ���coordinate of customer; ���ж���
		fw.write("# of customer coordinate(xxxyyy)\r\n");
		for (int j = 0; j < 2 ; j++) {
			for (int i = numofPort+numofStock; i < numofPort + numofStock + numofCustomers; i++) {
				fw.write(coordinate[i][j] + "\r\n");
			}
		}
		//�����һʱ�䴰xyxyxy
		fw.write("first timeWindow(xyxyxy)\r\n");
		for (int j = 0; j < tw1[0].length; j++) {
			for (int i = 0; i < 2; i++) {
				fw.write(tw1[i][j] + "\r\n");
			}
		}
		//����ڶ�ʱ�䴰
		fw.write("second timeWindow(xyxyxy)\r\n");
		for (int j = 0; j < tw2[0].length; j++) {
			for (int i = 0; i < 2; i++) {
				fw.write(tw2[i][j] + "\r\n");
			}
		}
		fw.write("ENDDATA\r\n");
		fw.flush();
		fw.close();
	}

	
	// ������뺯��
	public double distance(int i,int j){
		double x1 = coordinate[i][0];
		double y1 = coordinate[i][1];
		double x2 = coordinate[j][0];
		double y2 = coordinate[j][1];
		return distance( x1,  y1,  x2,  y2);
	}
	// ������뺯��
	public double distance(double x1, double y1, double x2, double y2) {
		double truckspeed = 60; // /���忨����ʻ�ٶ�60����ÿСʱ��
		double dis = Math
				.sqrt((Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2)))
				/ truckspeed;
		return dis;
	}

	// ����С����λ������
	public static double myRound(double v, int scale) {
		String temp = "#0.";
		for (int i = 0; i < scale; i++) {
			temp += "0";
		}
		return Double.valueOf(new java.text.DecimalFormat(temp).format(v))
				.doubleValue();
	}
	public void fileInput(Reader r) throws IOException {

		BufferedReader br = new BufferedReader(r);

		// ����ie
		br.readLine();
		String str = br.readLine();
		StringTokenizer st = new StringTokenizer(str);
		ieNum = (int) Double.parseDouble(st.nextToken());
		// ����if
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		ifNum = (int) Double.parseDouble(st.nextToken());
		// ����oe
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		oeNum = (int) Double.parseDouble(st.nextToken());
		// ����of
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		ofNum = (int) Double.parseDouble(st.nextToken());
		// ����customers
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		numofCustomers = (int) Double.parseDouble(st.nextToken());
		// ����tasks
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		numofTasks = (int) Double.parseDouble(st.nextToken());
		// ����stocks
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		numofStock = (int) Double.parseDouble(st.nextToken());
		// ����ports
		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		numofPort = (int) Double.parseDouble(st.nextToken());

		loadTime = new double[numofCustomers];
		tij = new double[numofTasks + numofStock][numofTasks + numofStock];
		//������������ɰ��ոۿڡ��ѳ����ͻ����˳������
		coordinate = new double[numofPort + numofStock + numofTasks][2];

		// ����package time;
		br.readLine();
		for (int i = 0; i < numofCustomers; i++) {
			str = br.readLine();
			st = new StringTokenizer(str);
			loadTime[i] = Double.parseDouble(st.nextToken());
		}
				
		nodeTW = new double[2][numofTasks];
		// ����ڵ�ʱ�䴰
		br.readLine();
		for (int j = 0; j < nodeTW[0].length; j++) {
			for (int i = 0; i < 2; i++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				nodeTW[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		
		serviceTime = new double[numofTasks];
		// ���붥�����ʱ��
		br.readLine();
		for (int j = 0; j < serviceTime.length; j++) {
			str = br.readLine();
			st = new StringTokenizer(str);
			serviceTime[j] = Double.parseDouble(st.nextToken());
		}		
		
		// ���tij��ת��ʱ�䣬һ��һ�����
		br.readLine();
		for (int i = 0; i < tij.length; i++) {
			for (int j = 0; j < tij[0].length; j++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				tij[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		
		// ���coordinate of port; ���ж���xxxx,yyyy
		br.readLine();
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < numofPort; i++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				coordinate[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		// ���coordinate of stock; ���ж���,xxxx,yyyy
		br.readLine();
		for (int j = 0; j < 2 ; j++) {
			for (int i = numofPort; i < numofPort + numofStock; i++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				coordinate[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		// ���coordinate of customer; ���ж���
		br.readLine();
		for (int j = 0; j < 2 ; j++) {
			for (int i = numofPort+numofStock; i < numofPort + numofStock + numofCustomers; i++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				coordinate[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		
		tw1 = new double[2][numofTasks];
		tw2 = new double[2][numofCustomers];
		//�����һʱ�䴰xyxyxy
		br.readLine();
		for (int j = 0; j < tw1[0].length; j++) {
			for (int i = 0; i < 2; i++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				tw1[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		//����ڶ�ʱ�䴰
		br.readLine();
		for (int j = 0; j < tw2[0].length; j++) {
			for (int i = 0; i < 2; i++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				tw2[i][j] = Double.parseDouble(st.nextToken());
			}
		}

		
		// �����Ϊ��file���޸ĵ�����꣬����ͨ���������tij
		// for (int i = 0; i < numofStock; i++) {
		// for (int j = 0; j < numofStock; j++) {
		// tij[i][j] = 0;
		// }
		// for (int j = 0; j < numofCustomers; j++) {
		// tij[i][numofStock + j] =
		// tij[i][numofStock
		// + numofCustomers + j] = distance(
		// coordinateofTerminal[0][0], coordinateofTerminal[0][1],
		// coordinateofCustomer[j][0], coordinateofCustomer[j][1]);
		// tij[numofStock + j][i] = tij[numofStock
		// + numofCustomers + j][i] = distance(
		// coordinateofTerminal[0][0], coordinateofTerminal[0][1],
		// coordinateofCustomer[j][0], coordinateofCustomer[j][1]);
		// }
		// }
		//
		// for (int i = 0; i < numofCustomers; i++) {
		// for (int j = 0; j < numofCustomers; j++) {
		// tij[i + numofStock][j + numofStock] =
		// tij[numofStock
		// + numofCustomers + i][j + numofStock] =
		// tij[numofStock
		// + numofCustomers + i][numofStock + numofCustomers
		// + j] = distance(coordinateofTerminal[0][0],
		// coordinateofTerminal[0][1], coordinateofCustomer[i][0],
		// coordinateofCustomer[i][1])
		// + distance(coordinateofTerminal[0][0],
		// coordinateofTerminal[0][1],
		// coordinateofCustomer[j][0],
		// coordinateofCustomer[j][1]);
		// tij[i + numofStock][numofStock
		// + numofCustomers + j] = distance(
		// coordinateofCustomer[i][0], coordinateofCustomer[i][1],
		// coordinateofCustomer[j][0], coordinateofCustomer[j][1]);
		// }
		// }
		//
		// for (int i = 0; i < ifNum; i++) {
		// for (int j = 0; j < ofNum; j++) {
		// tij[numofStock + numofCustomers + ofNum +
		// i][numofStock
		// + j] = distance(
		// coordinateofCustomer[ofNum + i][0],
		// coordinateofCustomer[ofNum + i][1],
		// coordinateofCustomer[j][0], coordinateofCustomer[j][1]);
		// }
		// }
		//
		// for (int i = 0; i < tij.length; i++) {
		// for (int j = 0; j < tij[0].length; j++) {
		// tij[i][j] = tij[i][j] / 80;
		// }
		// }
		//
		// for (int i = 0; i < numofCustomers; i++) {
		// tij[numofStock + numofCustomers + i][numofStock
		// + i] = 1000;
		// }
		//
		// for (int i = 0; i < tij.length; i++) {
		// for (int j = 0; j < tij[0].length; j++) {
		// tij[i][j] = myRound(tij[i][j], 1);
		// }
		// }
		
//		System.out.println(ofNum);
//		System.out.println(ifNum);
//		System.out.println(ieNum);
//		System.out.println(oeNum);
//		System.out.println(numofCustomers);
//		System.out.println(numofPort);
//		System.out.println(numofStock);
//		System.out.println(numofTasks);
//		System.out.println(loadCar);
//		System.out.println(loadTime.length);
//		System.out.println(tij[2][2]);
//		System.out.println(serviceTime[2]);
//		System.out.println(nodeTW[1][2]);
//		System.out.println(coordinate[6][1]);
//		System.out.println("----------");
		br.close();
	}
	
	
}
