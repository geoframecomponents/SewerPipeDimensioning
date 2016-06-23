/*
 * GNU GPL v3 License
 *
 * Copyright 2015 AboutHydrology (Riccardo Rigon)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.blogspot.geoframe.sewerPipeDimensioning;

import it.blogspot.geoframe.utils.GEOconstants;
import it.blogspot.geoframe.utils.GEOgeometry;
import it.blogspot.geoframe.hydroGeoEntities.line.*;

/**
 * @brief Computation of sewer pipe dimension.
 * 
 * @description This class compute the pipe diameter of a sewer with the respect
 *              of minimum slope and minimum excavation.
 * 
 * @author ftt01, dallatorre.daniele@gmail.com
 * @author
 * @version 0.1
 * @date June 13, 2016
 * @copyright GNU Public License v3 GWH-2b4
 */
public class SewerPipeDimensioning {

	private static double gaucklerStricklerCoefficient;
	private static double fillCoefficient;
	private double fillAngle;
	private static double discharge;
	private double hydraulicRadius;
	private double minSlope;
	private double pipeSlope;
	private double diameter;
	private double elevationEndPoint;

	private Pipe pipe;

	/**
	 * @brief Default constructor.
	 */
	public SewerPipeDimensioning() {
	}

	/**
	 * @brief Setter of the class fields.
	 *
	 * @description This method set the fields of the class and the parameters
	 *              of the local <strong>pipe</strong> object calling
	 *              <strong>setFields</strong> method. Furthermore it sets first
	 *              attempt values calling
	 *              <strong>setFirstAttemptValues</strong> method evaluating the
	 *              remaining field values.
	 * 
	 * @param[in] pipe <strong>Pipe</strong> object that contains all the
	 *            necessary information about the considered HydroGeoEntity.
	 */
	private void setPipe(final Pipe pipe) {
		this.pipe = pipe;
		setFields();
		setFirstAttemptValues();
	}

	/**
	 * @brief Setter of the pipe class fields.
	 */
	private void setFields() {
		gaucklerStricklerCoefficient = pipe.getGaucklerStricklerCoefficient();
		fillCoefficient = pipe.getFillCoefficient();
		discharge = pipe.getDischarge();
	}

	/**
	 * @brief Setter for the first attempt values.
	 */
	private void setFirstAttemptValues() {
		fillAngle = computeFillAngle();
		elevationEndPoint = pipe.getEndPoint().getTerrainElevation()
				- GEOconstants.MINIMUMEXCAVATION;
		pipeSlope = computePipeSlope();
		minSlope = computeMinSlope();
	}

	/**
	 * @brief Computation of <strong>fillAngle</strong>.
	 * 
	 * @description Computation of <strong>fillAngle</strong> from \f[
	 *              G=\frac{1-cos(\theta/2)}{2} \f] where \f$ G \f$ is the fill
	 *              coefficient, \f$ \theta \f$ is the fill angle related to the
	 *              fill coefficient.
	 */
	private double computeFillAngle() {
		return 2 * Math.acos(1 - 2 * fillCoefficient);
	}

	/**
	 * @brief Computation of minimum slope.
	 * 
	 * @description Evaluation of minimum slope due to fixed shear stress at the
	 *              base of the pipe with the relation \f[
	 *              i_{min}=\frac{\tau}{\gamma*R_h} \f] where the \f$\tau\f$ is
	 *              the shear stress, \f$\gamma\f$ is the specific weight of
	 *              water and \f$R_h\f$ the hydraulic radius.
	 * 
	 * @todo Build a method to use commercial pipe dimensions.
	 */
	private double computeMinSlope() {
		diameter = computeFixedDiameter(fillAngle);
		hydraulicRadius = computeHydraulicRadius(fillAngle);

		return GEOconstants.SHEARSTRESS
				/ (GEOconstants.WSPECIFICWEIGHT * hydraulicRadius);
	}

	/**
	 * @brief Computation of diameter due to auto-cleaning.
	 * 
	 * @description The diameter is related to minimum slope and is evaluated by
	 *              the relation \f[ D = {\left[ \frac{4^{^{13}/_6} Q}{\theta
	 *              {(1-\frac{sin(\theta)}{\theta})}^{^7/_6} K_s
	 *              \sqrt{^\tau/_\gamma}} \right]}^{^6/_{13}} \f]
	 */
	private double computeFixedDiameter(double fillAngle) {
		final double pow1 = 13.0 / 6;
		double numerator = Math.pow(4, pow1);
		final double pow2 = 7.0 / 6;
		double denominator = fillAngle
				* Math.pow(1 - Math.sin(fillAngle) / fillAngle, pow2)
				* gaucklerStricklerCoefficient
				* Math.pow(GEOconstants.SHEARSTRESS
						/ GEOconstants.WSPECIFICWEIGHT, 0.5);
		final double pow3 = 6.0 / 13;

		return Math.pow(numerator / denominator, pow3);
	}

	/**
	 * @brief Computation of the hydraulic radius.
	 * 
	 * @description Computation of the hydraulic radius from \f[ R_h =
	 *              D\frac{1-sin(\theta)/\theta)}{4} \f] where the \f$ \theta
	 *              \f$ is the fill angle.
	 */
	private double computeHydraulicRadius(double fillAngle) {
		return diameter / 4 * (1 - Math.sin(fillAngle) / fillAngle);
	}

	/**
	 * @brief Computation of <strong>pipeSlope</strong>
	 * 
	 * @description Evaluation of the slope of the pipe with the end elevation
	 *              point set by class field <strong>elevationEndPoint</strong>.
	 */
	private double computePipeSlope() {
		return GEOgeometry.computeSlope(pipe.getStartPoint().getX(), pipe
				.getStartPoint().getY(), pipe.getStartPoint().getElevation(),
				pipe.getEndPoint().getX(), pipe.getEndPoint().getY(),
				elevationEndPoint);
	}

	/**
	 * @brief Evaluation of diameter related to fixed slope.
	 */
	private double computeDiameter(double slope) {
		double fillAngle = computeFillAngle();
		final double pow1 = 3.0 / 8;
		double numerator = Math.pow((discharge * fillAngle)
				/ (gaucklerStricklerCoefficient * Math.pow(slope, 0.5)), pow1);
		final double pow2 = 5.0 / 8;
		double denominator = Math
				.pow(1 - Math.sin(fillAngle) / fillAngle, pow2);
		final double pow3 = -9.0 / 8;

		return numerator / denominator * Math.pow(10, pow3);
	}

	/**
	 * Evaluation of elevation of end point due to a defined slope
	 */
	private double computeElevationEndPoint(double slope) {
		return pipe.getStartPoint().getElevation()
				- slope
				* GEOgeometry.horizontalProjection(pipe.getStartPoint().getX(),
						pipe.getStartPoint().getY(), pipe.getEndPoint().getX(),
						pipe.getEndPoint().getY());
	}

	/**
	 * Evaluation of velocity
	 */
	private double computeVelocity() {
		double numerator = discharge * 8;
		double denominator = diameter * diameter * (fillAngle - Math.sin(fillAngle));
		return numerator / denominator;
	}

	public Pipe run(final Pipe pipe) {
		setPipe(pipe);
		if (pipeSlope >= minSlope) {
			this.pipe.buildPipe(elevationEndPoint, computeDiameter(pipeSlope),
					fillCoefficient, computeVelocity());
		} else {
			this.pipe.buildPipe(computeElevationEndPoint(minSlope), diameter,
					fillCoefficient, computeVelocity());
		}

		return this.pipe;
	}
}
