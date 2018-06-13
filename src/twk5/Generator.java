package twk5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import org.junit.Test;

/**
 * ��ɶ����ݴ���ĸ�д�����ڿ��Դ����ѳ������
 * @author Leon
 *
 */
public class Generator {

	public int ofNum = 0;//
	public int ifNum = 0;//
	public int numofCustomers = 0;
	public int numofTasks = 0;
	public int stockNum = 0;
	public double timePeriod = 0;

	public double c1 = 0;
	public double c2 = 0;
	public double c3 = 0;

	public double[] packageTime;
	public double[][] tij;

	public double[][] coordinateofTerminal;
	public double[][] coordinateofCustomer;

	@Test
	public void testOut() throws IOException {
		Generator g = new Generator();
		// of if stock
		g.generate(8,7,1);
		g.fileOutput();

	}
	@Test
	public void testIn() throws IOException {
		Generator g = new Generator();
		// of if stock
		String name = "2-5-4";
		FileReader reader = new FileReader(new File("D:/twkdata/datafile"+name+".txt"));
		g.fileInput(reader);
		
	}
	
	@Test
	public void teatLinked(){
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		//��2-3֮�����
		int indexOf = list.indexOf(3);
		list.add(indexOf, 5);
		for(Integer i:list){
			System.out.print(i);
		}
		
	}

	
	
	private double getMinTerminalAndCustormer(double x2, double y2, double x3, double y3,double[][] coordinateofTerminal) {
		double[] arr = new double[coordinateofTerminal.length];
		for(int i = 0;i<coordinateofTerminal.length;i++){
			arr[i] = disUseTime(x2, y2,coordinateofTerminal[i][0],coordinateofTerminal[i][1] ) + disUseTime(x3,y3,coordinateofTerminal[i][0],coordinateofTerminal[i][1]);
		}
		Arrays.sort(arr);
		return arr[0];
	}

	
	
	
	public void fileOutput() throws IOException {
		// ///////// д���Զ����ʽ�����ļ�
		// String outputFilePath = "/datafile" + ofNum + "-" + ifNum + ".txt";
		String outputFilePath = "D:/twkdata/datafile" + ofNum + "-" + ifNum
				+ "-" + stockNum + ".txt";
		FileWriter fw = new FileWriter(outputFilePath);

		// �����������
		fw.write("# of pickup customers\r\n" + ofNum + "\r\n");
		fw.write("# of delivery customers\r\n" + ifNum + "\r\n");
		fw.write("# of customers\r\n" + numofCustomers + "\r\n");
		fw.write("# of tasks\r\n" + numofTasks + "\r\n");
		fw.write("# of stockNum\r\n" + stockNum + "\r\n");
		fw.write("c1\r\n" + c1 + "\r\n");
		fw.write("c2\r\n" + c2 + "\r\n");
		fw.write("c3\r\n" + c3 + "\r\n");
		fw.write("# of timeperiod\r\n" + timePeriod + "\r\n");

		// ���package time;
		fw.write("# of package time\r\n");
		for (int i = 0; i < numofCustomers; i++) {
			fw.write(packageTime[i] + "\r\n");
		}

		// ���coordinates of terminal;
		fw.write("terminal coordinates\r\n");
		for (int i = 0; i < coordinateofTerminal[0].length; i++) {
			for (int j = 0; j < coordinateofTerminal.length; j++) {
				fw.write(coordinateofTerminal[j][i] + "\r\n");
			}
		}

		// ���coordinate of customers; ���ж���
		fw.write("customer coordinate\r\n");
		for (int i = 0; i < coordinateofCustomer[0].length; i++) {
			for (int j = 0; j < coordinateofCustomer.length; j++) {
				fw.write(coordinateofCustomer[j][i] + "\r\n");
			}
		}

		// ���distance matrix;
		fw.write("distance matrix\r\n");
		for (int i = 0; i < tij.length; i++) {
			for (int j = 0; j < tij[0].length; j++) {
				fw.write(tij[i][j] + "\r\n");
			}
		}

		fw.write("ENDDATA\r\n");
		fw.flush();
		fw.close();
	}

	public void generate(int ofNum, int ifNum, int numofStock) {
		this.ofNum = ofNum;
		this.ifNum = ifNum;

		numofCustomers = ifNum + ofNum;
		numofTasks = 2 * numofCustomers;
		this.stockNum = numofStock;

		c1 = 10;
		c2 = 1;
		c3 = 0;

		timePeriod = 16;
		packageTime = new double[numofCustomers];
		tij = new double[numofTasks + stockNum][numofTasks
				+ stockNum];

		coordinateofTerminal = new double[stockNum][2];
		coordinateofCustomer = new double[numofCustomers][2];

		// ��������
		double areaParameter = 200; // �����С����
		//ԭ���ѳ���ʱ��ֵȡ���ĵ�
//		coordinateofTerminal[0][0] = areaParameter / 2;
//		coordinateofTerminal[0][1] = areaParameter / 2;
		for(int i = 0;i<stockNum;i++){
			coordinateofTerminal[i][0] = myRound(Math.random() * areaParameter,2);
			coordinateofTerminal[i][1] = myRound(Math.random() * areaParameter,2);
			
		}
		

		// ����customer����
		for (int i = 0; i < numofCustomers; i++) {
			coordinateofCustomer[i][0] = myRound(Math.random() * areaParameter,
					2);
			coordinateofCustomer[i][1] = myRound(Math.random() * areaParameter,
					2);
		}

		
		//��������жѳ���ͻ���֮��ľ���
		
		
		// ֱ������tij
		// i����D
		for (int i = 0; i < stockNum; i++) {
			// j����D
			for (int j = 0; j < stockNum; j++) {
				tij[i][j] = 0;
			}

			// j����tasks
			for (int j = 0; j < numofCustomers; j++) {
				double x1, y1, x2, y2;
				x1 = coordinateofTerminal[i][0];
				y1 = coordinateofTerminal[i][1];
				x2 = coordinateofCustomer[j][0];
				y2 = coordinateofCustomer[j][1];
				tij[i][stockNum + j] = tij[i][stockNum + numofCustomers + j] = disUseTime(x1, y1, x2, y2);
				tij[stockNum + j][i] = tij[stockNum + numofCustomers + j][i] = disUseTime(x1, y1, x2, y2);
			}
		}
		//i����tasks
		//����ȥ�ѳ��Ĳ��ֶ���������
		for (int i = 0; i < numofCustomers; i++) {
			for (int j = 0; j < numofCustomers; j++) {
				double x2,y2,x3,y3;
				x2 = coordinateofCustomer[i][0];
				y2 = coordinateofCustomer[i][1];
				x3 = coordinateofCustomer[j][0];
				y3 = coordinateofCustomer[j][1];
				
				tij[i + stockNum][j + stockNum] //i,j����o1i1
						= tij[stockNum+ numofCustomers + i][j + stockNum] //i����02i2,j����o1i1
						= tij[stockNum+ numofCustomers + i][stockNum + numofCustomers + j] //i����o2i2,j����o2i2
						= getMinTerminalAndCustormer(x2,y2,x3,y3,coordinateofTerminal);
				//i����o1i1,j����o2i2
				tij[i + stockNum][stockNum + numofCustomers + j] = disUseTime(x2,y2,x3,y3);
			}
		}
		for (int i = 0; i < ifNum; i++) {
			for (int j = 0; j < ofNum; j++) {
				//i����i2,j����o1
				tij[stockNum + numofCustomers + ofNum + i][stockNum + j] 
						= disUseTime(coordinateofCustomer[ofNum + i][0],coordinateofCustomer[ofNum + i][1], coordinateofCustomer[j][0], coordinateofCustomer[j][1]);
			}
		}

		for (int i = 0; i < tij.length; i++) {
			for (int j = 0; j < tij[0].length; j++) {
				tij[i][j] = tij[i][j] / 80;
			}
		}
		// ��������Ŷ���ʹ���粻�Գ�
		// for (int i = 0; i < tij.length; i++) {
		// for (int j = 0; j < tij.length; j++) {
		// tij[i][j] = tij[i][j]
		// * (0.9 + 0.2 * Math.random());
		// }
		// }

		for (int i = 0; i < numofCustomers; i++) {
			tij[stockNum + numofCustomers + i][stockNum + i] = 1000;
		}

		for (int i = 0; i < tij.length; i++) {
			for (int j = 0; j < tij[0].length; j++) {
				tij[i][j] = myRound(tij[i][j], 3);
			}
		}

		for (int i = 0; i < packageTime.length; i++) {
			packageTime[i] = 3 + 2 * Math.random();
			packageTime[i] = myRound(packageTime[i], 3);
		}

	}


	// ����(��ʻʱ��)����
	public double disUseTime(double x1, double y1, double x2, double y2) {
		double truckspeed = 1; // 1����/Сʱ
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

	// ���ļ�
	public void fileInput(Reader io) throws IOException {

		BufferedReader br = new BufferedReader(io);

		// �����������
		br.readLine();
		String str = br.readLine();
		StringTokenizer st = new StringTokenizer(str);
		ofNum = (int) Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		ifNum = (int) Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		numofCustomers = (int) Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		numofTasks = (int) Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		stockNum = (int) Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		// st = new StringTokenizer(str);
		// c1 = Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		// st = new StringTokenizer(str);
		// c2 = Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		// st = new StringTokenizer(str);
		// c3 = Double.parseDouble(st.nextToken());

		br.readLine();
		str = br.readLine();
		st = new StringTokenizer(str);
		timePeriod = Double.parseDouble(st.nextToken());
		System.out.println();

		packageTime = new double[numofCustomers];
		tij = new double[numofTasks + stockNum][numofTasks + stockNum];

		coordinateofTerminal = new double[stockNum][2];
		coordinateofCustomer = new double[numofCustomers][2];

		// ����package time;
		br.readLine();
		for (int i = 0; i < numofCustomers; i++) {
			str = br.readLine();
			st = new StringTokenizer(str);
			packageTime[i] = Double.parseDouble(st.nextToken());
		}

		// ����terminal coordinate
		br.readLine();
		for (int i = 0; i < coordinateofTerminal[0].length; i++) {
			for (int j = 0; j < coordinateofTerminal.length; j++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				coordinateofTerminal[j][i] = Double.parseDouble(st.nextToken());
			}
		}


		// ����customers coordinate
		br.readLine();
		for (int i = 0; i < coordinateofCustomer[0].length; i++) {
			for (int j = 0; j < coordinateofCustomer.length; j++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				coordinateofCustomer[j][i] = Double.parseDouble(st.nextToken());
			}
		}

		// ����distance Matrix;
		br.readLine();
		for (int i = 0; i < tij.length; i++) {
			for (int j = 0; j < tij[0].length; j++) {
				str = br.readLine();
				st = new StringTokenizer(str);
				tij[i][j] = Double.parseDouble(st.nextToken());
//				System.out.println(tij[i][j]);
			}
		}
		br.close();
		
		for (int i = 0; i < stockNum + numofTasks; i++) {
			for (int j = 0; j < stockNum + numofTasks; j++) {
				System.out.print(myRound(tij[i][j],2)+"\t");
			}
			System.out.println();
		}
		
		
	}

}