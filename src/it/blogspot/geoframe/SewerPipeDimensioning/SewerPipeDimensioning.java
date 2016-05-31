package it.blogspot.geoframe.SewerPipeDimensioning;

public class SewerPipeDimensioning {

	/** Definition of specific weight of water [N/m^3] */
	private static final double WSPECIFICWEIGHT = 9800;

	/** Definition of first node terrain elevation */
	private static final double startElevation = 7.76;

	/** Definition of second node terrain elevation */
	private static final double endElevation = 4.65;

	/** Definition of pipe horizontal lenght */
	private static final double pipeLenght = 10.0;

	private static boolean flag = false;
	
	/** Definition of diameter */
	public static double D;
	
	/** Definition of fill coefficient */
	public static double G = 0.8;
	
	/** Definition of shear stress [Pa] */
	public static double tau = 2.0;
	
	/** Definition of discharge [l/s] */
	public static double Q = 2.0;
	
	/** Definition of Gaucker-Strickler coefficient */
	public static double Ks = 0.03;
	
	/** Definition of hydraulic radius */
	public static double Rh;
	
	/** Evaluation of theta from G=0.5*(1-cos(theta/2) */
	private double getTheta(double G) {
		return 2*Math.acos(1-2*G);
	}
	
	/** Evaluation of G (fill coefficient) from G=0.5*(1-cos(theta/2) */
	private double getG(double theta) {
		return 0.5 *(1-Math.cos(theta/2));
	}
	
	/** Evaluation of Rh (hydraulic radius) from Rh=D/4*(1-sin(theta)/theta) */
	private double getRh(double D, double theta) {
		return D/4*(1-Math.sin(theta)/theta);
	}
	
	/** Evaluation of slope from slope=tau/(WSPECIFICWEIGHT*Rh) */
	private double getSlope(double tau, double Rh) {
		return tau/(WSPECIFICWEIGHT*Rh);
	}
	
	/** Check if the minimum Slope is greater/smaller than natural Slope */
	private double checkSlope(double minSlope, double nSlope) {
		if  (nSlope >= minSlope) {
			return nSlope;
		}
		else {
			flag = true;
			return minSlope;
		}
	}

	/** Evaluation of natural slope */
	private double naturalSlope(double y1, double y2, double lenght) {
		return (y1-y2)/lenght;
	}

	/** Evaluation of minimum slope */
	private double minSlope() {
		double theta = getTheta(G);
		D = getFixedD(Q,theta,tau,Ks);
		/*! /TODO evaluation of commercial diameters */
		Rh = getRh(D,theta);
		return getSlope(tau, Rh);
	}
	
	/** Evaluation of diameter due to autocleaning */
	private double getFixedD(double Q, double theta, double tau, double Ks) {
		double num = Math.pow(4, 13/6);
		double den = theta*Math.pow(1-Math.sin(theta)/theta,7/6)*Ks*Math.pow(tau/WSPECIFICWEIGHT,0.5);
		return Math.pow(num/den,6/13);
	}

	/** Evaluation of diameter due to natural slope */
	private double getDiameter(double slope) {
		double theta = getTheta(G);
		double num = Math.pow( (Q*theta)/(Ks*Math.pow(slope,0.5)), 3.0/8 );
		// double num = (Q*Math.pow(2, 13.0/3))/(Ks*Math.pow(slope, 0.5));
		double den = Math.pow(1-Math.sin(theta)/theta, 5.0/8);
		// double den = Math.pow(1-Math.sin(theta)/theta,2.0/3)*(theta - Math.sin(theta));
		return num/den*Math.pow(10,-9.0/8);
	}
	
	public static void main(String [ ] args)
	{
		SewerPipeDimensioning test = new SewerPipeDimensioning();
		
		double minSlope = test.minSlope();
		double nSlope = test.naturalSlope(y1, y2, L);
		
		double slope = test.checkSlope(minSlope, nSlope);
		  
		if (flag == false) {
			System.out.println(slope);
	    	 D = test.getDiameter(slope);
	     } 
		System.out.println(slope);
		System.out.println(G);
		System.out.println(test.getTheta(slope));
		System.out.println(D*100);
		// System.out.println(test.getSlope(tau, Rh));
	}
	
}