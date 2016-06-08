package it.blogspot.geoframe.SewerPipeDimensioning;

import it.blogspot.geoframe.utils.GEOconstants;

public class SewerPipeDimensioning {

	/** Definition of first node terrain elevation */
	private static final double startElevation = 7.76;

	/** Definition of second node terrain elevation */
	private static final double endElevation = 4.65;

	/** Definition of pipe horizontal lenght */
	private static final double pipeLenght = 10.0;
	
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
	
	/** 
	 * Evaluation of theta from G=0.5*(1-cos(theta/2)
	 */
	private double computeFillAngle() {
		return 2*Math.acos(1-2*fillCoefficient);
	}
	
	/** 
	 * Evaluation of G (fill coefficient) from G=0.5*(1-cos(theta/2)
	 */
	private double computeFillCoefficient(double fillAngle) {
		return 0.5 *(1-Math.cos(fillAngle/2));
	}
	
	/** 
	 * Evaluation of Rh (hydraulic radius) from Rh=D/4*(1-sin(theta)/theta)
	 */
	private double computeHydraulicRadius(double fillAngle) {
		return diameter/4*(1-Math.sin(fillAngle)/fillAngle);
	}
	
	/** 
	 * Evaluation of slope from slope=tau/(WSPECIFICWEIGHT*Rh)
	 */
	private double computeSlope() {
		return shearStress/(GEOconstants.WSPECIFICWEIGHT*hydraulicRadius);
	}
	
	/** 
	 * Evaluation of natural slope
	 */
	private double evaluateNaturalSlope() {
		return (startElevation-endElevation)/pipeLenght;
	}

	/** 
	 * Evaluation of minimum slope
	 */
	private double evaluateMinSlope() {
		double fillAngle = computeFillAngle();
		diameter = computeFixedDiameter(fillAngle);
		/*! 
		 * /TODO evaluation of commercial diameters
		 */
		hydraulicRadius = computeHydraulicRadius(fillAngle);
		return computeSlope();
	}
	
	/** 
	 * Evaluation of diameter due to auto-cleaning
	 */
	private double computeFixedDiameter(double fillAngle) {
		final double pow1 = 13.0/6;
		double numerator =  Math.pow(4, pow1);
		final double pow2 = 7.0/6;
		double denominator = fillAngle*Math.pow(1-Math.sin(fillAngle)/fillAngle, pow2)*gaucklerStricklerCoefficient*Math.pow(shearStress/GEOconstants.WSPECIFICWEIGHT, 0.5);
		final double pow3 = 6.0/13;
		
		return Math.pow(numerator/denominator, pow3);
	}

	/** 
	 * Evaluation of diameter due to natural slope
	 */
	private double computeDiameter(double slope) {
		
		double fillAngle = computeFillAngle();
		final double pow1 = 3.0/8;
		double numerator = Math.pow( (discharge*fillAngle)/(gaucklerStricklerCoefficient*Math.pow(slope,0.5)), pow1);
		final double pow2 = 5.0/8;
		double denominator = Math.pow(1-Math.sin(fillAngle)/fillAngle, pow2);
		final double pow3 = -9.0/8;
		
		return numerator/denominator*Math.pow(10,pow3);
	}
	
	public static void main(String [ ] args)
	{
		SewerPipeDimensioning test = new SewerPipeDimensioning();
		double minSlope = test.evaluateMinSlope();
		double naturalSlope = test.evaluateNaturalSlope();
		double slope;
		
		if  (naturalSlope >= minSlope) {
			slope = naturalSlope;
		} else {
			slope = minSlope;
		}
		diameter = test.computeDiameter(slope);
	}
}