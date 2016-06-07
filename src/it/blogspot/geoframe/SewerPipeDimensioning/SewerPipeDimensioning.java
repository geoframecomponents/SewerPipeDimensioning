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
	public static double diameter;
	
	/** Definition of fill coefficient */
	public static double fillCoefficient= 0.8;
	
	/** Definition of shear stress [Pa] */
	public static double shearStress = 2.0;
	
	/** Definition of discharge [l/s] */
	public static double discharge = 2.0;
	
	/** Definition of Gaucker-Strickler coefficient */
	public static double gaucklerStricklerCoefficient = 0.03;
	
	/** Definition of hydraulic radius */
	public static double hydraulicRadius;
	
	/** Evaluation of theta from G=0.5*(1-cos(theta/2) */
	private double getFillAngle(double fillCoefficient) {
		return 2*Math.acos(1-2*fillCoefficient);
	}
	
	/** Evaluation of G (fill coefficient) from G=0.5*(1-cos(theta/2) */
	private double getFillCoefficient(double fillAngle) {
		return 0.5 *(1-Math.cos(fillAngle/2));
	}
	
	/** Evaluation of Rh (hydraulic radius) from Rh=D/4*(1-sin(theta)/theta) */
	private double getHydraulicRadius(double diameter, double fillAngle) {
		return diameter/4*(1-Math.sin(fillAngle)/fillAngle);
	}
	
	/** Evaluation of slope from slope=tau/(WSPECIFICWEIGHT*Rh) */
	private double getSlope(double shearStress, double hydraulicRadius) {
		return shearStress/(WSPECIFICWEIGHT*hydraulicRadius);
	}
	
	/** Check if the minimum Slope is greater/smaller than natural Slope */
	private double checkSlope(double minSlope, double naturalSlope) {
		if  (naturalSlope >= minSlope) {
			return naturalSlope;
		} else {
			flag = true;
			return minSlope;
		}
	}

	/** Evaluation of natural slope */
	private double naturalSlope(double startElevation, double endElevation, double pipeLenght) {
		return (startElevation-endElevation)/pipeLenght;
	}

	/** Evaluation of minimum slope */
	private double minSlope() {
		double fillAngle = getFillAngle(fillCoefficient);
		diameter = getFixedDiameter(discharge,fillAngle,shearStress,gaucklerStricklerCoefficient);
		/*! /TODO evaluation of commercial diameters */
		hydraulicRadius = getHydraulicRadius(diameter,fillAngle);
		return getSlope(shearStress, hydraulicRadius);
	}
	
	/** Evaluation of diameter due to autocleaning */
	private double getFixedDiameter(double discharge, double fillAngle, double shearStress, double gaucklerStricklerCoefficient) {
		double numerator =  Math.pow(4, 13.0/6);
		double denominator = fillAngle*Math.pow(1-Math.sin(fillAngle)/fillAngle, 7.0/6)*gaucklerStricklerCoefficient*Math.pow(shearStress/WSPECIFICWEIGHT, 0.5);
		return Math.pow(numerator/denominator, 6.0/13);
	}

	/** Evaluation of diameter due to natural slope */
	private double getDiameter(double slope) {
		double fillAngle = getFillAngle(fillCoefficient);
		double numerator = Math.pow( (discharge*fillAngle)/(gaucklerStricklerCoefficient*Math.pow(slope,0.5)), 3.0/8);
		// double num = (Q*Math.pow(2, 13.0/3))/(Ks*Math.pow(slope, 0.5));
		double denominator = Math.pow(1-Math.sin(fillAngle)/fillAngle, 5.0/8);
		// double den = Math.pow(1-Math.sin(theta)/theta,2.0/3)*(theta - Math.sin(theta));
		return numerator/denominator*Math.pow(10,-9.0/8);
	}
	
	public static void main(String [ ] args)
	{
		SewerPipeDimensioning test = new SewerPipeDimensioning();
		
		double minSlope = test.minSlope();
		double naturalSlope = test.naturalSlope(startElevation, endElevation, pipeLenght);
		
		double slope = test.checkSlope(minSlope, naturalSlope);
		  
		if (flag == false) {
			System.out.println(slope);
	    	 diameter = test.getDiameter(slope);
	     } 
		System.out.println(slope);
		System.out.println(fillCoefficient);
		System.out.println(test.getFillAngle(slope));
		System.out.println(diameter*100);
		// System.out.println(test.getSlope(tau, Rh));
	}
	
}