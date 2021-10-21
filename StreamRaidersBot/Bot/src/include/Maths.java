package include;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Maths {

	
	private static String[] lettersAndNumbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split("");
	public static String ranString(int length) {
		StringBuilder ran = new StringBuilder();
		for(int i=0; i<length; i++)
			ran.append(lettersAndNumbers[ranInt(0, lettersAndNumbers.length-1)]);
		
		return ran.toString();
	}
	
	public static int ranInt(int min, int max) {
		return (int) new Scaler(0, 1, min, max).setDecPl(0).scale(Math.random());
	}
	
	public static Color getReadableColor(Color background) {
        
		double lum = 0.2126*background.getRed() + 0.7152*background.getGreen() + 0.0722*background.getBlue();
		
		if(lum < 140)
			return Color.white;
		else
			return Color.black;
		
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
