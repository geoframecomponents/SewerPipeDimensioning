package it.blogspot.geoframe.SewerPipeDimensioning;

import it.blogspot.geoframe.utils.GEOconstants;
import it.blogspot.geoframe.utils.GEOgeometry;
import it.blogspot.geoframe.hydroGeoEntities.line.*;

public class SewerPipeDimensioning {
	
    private static double gaucklerStricklerCoefficient;
    private static double fillCoefficient;
    private double fillAngle;
    private static double discharge;
    private double hydraulicRadius;
    private double minSlope;
    private double pipeSlope;
    public double diameter;

    private Pipe pipe;

    public SewerPipeDimensioning(final Pipe pipe) {
    	this.pipe = pipe;
		setFields();
	}

	private void setFields() {
		gaucklerStricklerCoefficient = pipe.getGaucklerStricklerCoefficient();
		fillCoefficient = pipe.getFillCoefficient();
		discharge = pipe.getDischarge();

		fillAngle = computeFillAngle();

		pipe.setElevationEndPoint(pipe.getEndPoint().getTerrainElevation()-GEOconstants.MINIMUMEXCAVATION);
		pipeSlope = computePipeSlope();
		
		minSlope = computeMinSlope();
	}

	/** 
	 * Evaluation of theta from G=0.5*(1-cos(theta/2)
	 */
	private double computeFillAngle() {
		return 2*Math.acos(1-2*fillCoefficient);
	}
	
	/**
	 * Evaluation of Rh (hydraulic radius) from Rh=D/4*(1-sin(theta)/theta)
	 */
	private double computeHydraulicRadius(double fillAngle) {
		return diameter/4*(1-Math.sin(fillAngle)/fillAngle);
	}
	
	/** 
	 * Evaluation of slope due to fixed shear stress from slope=tau/(WSPECIFICWEIGHT*Rh)
	 */
	private double computeMinSlope() {
		diameter = computeFixedDiameter(fillAngle);
		hydraulicRadius = computeHydraulicRadius(fillAngle);
		return GEOconstants.SHEARSTRESS/(GEOconstants.WSPECIFICWEIGHT*hydraulicRadius);
	}
	
	/**
	 * Evaluation of diameter due to auto-cleaning
	 */
	private double computeFixedDiameter(double fillAngle) {
		final double pow1 = 13.0/6;
		double numerator =  Math.pow(4, pow1);
		final double pow2 = 7.0/6;
		double denominator = fillAngle*Math.pow(1-Math.sin(fillAngle)/fillAngle, pow2)*gaucklerStricklerCoefficient*
								Math.pow(GEOconstants.SHEARSTRESS/GEOconstants.WSPECIFICWEIGHT, 0.5);
		final double pow3 = 6.0/13;

		return Math.pow(numerator/denominator, pow3);
	}

	/**
	 * Evaluation of pipeSlope with minimum excavation
	 */
	private double computePipeSlope() {
		return GEOgeometry.computeSlope(pipe.getStartPoint().getX(), pipe.getStartPoint().getY(), pipe.getStartPoint().getElevation(),
										pipe.getEndPoint().getX(), pipe.getEndPoint().getY(), pipe.getEndPoint().getElevation());
	}

	/**
	 * Evaluation of diameter
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

	/**
	 * Evaluation of elevation of end point due to a defined slope
	 */
	private double computeElevationEndPoint(double slope) {
		return pipe.getStartPoint().getElevation()-slope*GEOgeometry.horizontalProjection(pipe.getStartPoint().getX(),
				pipe.getStartPoint().getY(), pipe.getEndPoint().getX(), pipe.getEndPoint().getY());
	}

	/**
	 * Evaluation of velocity
	 */
	private double computeVelocity() {
		double numerator = discharge*8;
		double denominator = diameter*(fillAngle-Math.sin(fillAngle));
		return numerator/denominator;
	}
	
	public Pipe run() {
		if (pipeSlope>=minSlope) {
			pipe.buildPipe(pipe.getEndPoint().getElevation(), computeDiameter(pipeSlope), fillCoefficient, computeVelocity());
		} else {
			pipe.buildPipe(computeElevationEndPoint(minSlope), diameter, fillCoefficient, computeVelocity());
		}

		return pipe;
	}
}
