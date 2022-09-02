package include;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class Maths {

	private static Random r = new Random();
	
	private static char[] lettersAndNumbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	public static String ranString(int length) {
		char[] buf = new char[length];
		for(int i=0; i<length; i++)
			buf[i] = lettersAndNumbers[r.nextInt(lettersAndNumbers.length)];
		return new String(buf);
	}
	
	public static int ranInt(int b1, int b2) {
		if(b1 == b2)
			return b1;
		if(b1 > b2) {
			int t = b1;
			b1 = b2;
			b2 = t;
		}
		return r.nextInt(b1, b2+1);
	}
	
	public static Color getReadableColor(Color background) {
		return 0.2126*background.getRed()
					+ 0.7152*background.getGreen()
					+ 0.0722*background.getBlue() 
				< 140 ? Color.white : Color.black;
	}
	
	public static class PointN {
		
		private double[] cords;
		
		public PointN(double... cords) {
			this.cords = cords;
		}
		
		public double[] getCords() {
			return cords;
		}
		
		public int dimensions() {
			return cords.length;
		}
		
		public static class DifferentDimensionsException extends RuntimeException {
			private static final long serialVersionUID = -5448383695288621547L;
			public DifferentDimensionsException(int start, int end) {
				super(start + " <-> " + end);
			}
		}
		
		public double dis(PointN end) {
			int dims = dimensions();
			if(dims != end.dimensions())
				throw new DifferentDimensionsException(dimensions(), end.dimensions());
			
			double result = 0;
			
			double[][] vals = new double[][] {getCords(), end.getCords()};
			
			for(int i=0; i<dims; i++)
				result += (vals[0][i] - vals[1][i]) * (vals[0][i] - vals[1][i]);
			
			return Math.sqrt(result);
		}
		
	}
	
	public static class Scaler {
		
		int dp = -1;
		
		public Scaler setDecPl(int places) {
			dp = places + 1;
			return this;
		}
		
		double s, e, c;
		
		public Scaler(double smin, double smax, double emin, double emax) {
			s = smin;
			e = emin;
			c = (emax - emin) / (smax - smin);
		}
		
		public double scale(double in) {
			double res = (in - s) * c + e;
			return dp <= 0 ? res : new BigDecimal(res).setScale(0, RoundingMode.HALF_UP).doubleValue();
		}
		
	}
	
	
}
