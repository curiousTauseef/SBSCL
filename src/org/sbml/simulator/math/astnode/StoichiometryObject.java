/*
 * $Id: StoichiometryObject.java 15:12:31 keller $ $URL:
 * StoichiometryObject.java $
 * --------------------------------------------------------------------- This
 * file is part of SBMLsimulator, a Java-based simulator for models of
 * biochemical processes encoded in the modeling language SBML.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.sbml.simulator.math.astnode;

import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.simulator.math.EfficientASTNodeInterpreter;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class StoichiometryObject {
  private double stoichiometry;
  private double time;
  private int speciesIndex;
  private int speciesRefIndex;
  private boolean constant;
  private boolean isSetStoichiometryMath;
  private ASTNode math;
  private String id;
  private SpeciesReference sr;
  private Map<String, Double> stoichiometricCoefHash;
  private double[] Y;
  private EfficientASTNodeInterpreter nodeInterpreter;
  private int reactionIndex;
  private boolean isReactant;
  
  /**
   * 
   * @param sr
   * @param speciesIndex
   * @param speciesRefIndex
   * @param stoichiometricCoefHash
   * @param Y
   * @param nodeInterpreter
   * @param kineticLawObject
   * @param isReactant
   */
  public StoichiometryObject(SpeciesReference sr, int speciesIndex,
    int speciesRefIndex, Map<String, Double> stoichiometricCoefHash,
    double[] Y, EfficientASTNodeInterpreter nodeInterpreter,
    int reactionIndex, boolean isReactant) {
    this.isSetStoichiometryMath = sr.isSetStoichiometryMath();
    if (isSetStoichiometryMath) {
      math = sr.getStoichiometryMath().getMath();
    }
    this.sr = sr;
    this.reactionIndex=reactionIndex;
    this.id = sr.getId();
    this.speciesIndex = speciesIndex;
    this.speciesRefIndex = speciesRefIndex;
    this.constant = sr.getConstant();
    if ((!sr.isSetId()) && (!isSetStoichiometryMath)) {
      constant=true;
    }
    this.stoichiometricCoefHash = stoichiometricCoefHash;
    this.Y = Y;
    this.nodeInterpreter = nodeInterpreter;
    this.time = Double.NaN;
    this.isReactant = isReactant;
    computeStoichiometricValue();
  }
  
  public void computeChange(double currentTime, double[] changeRate, double[] v) {
    if(constant==false) {
      compileDouble(currentTime);
    }
    if (isReactant) {
      changeRate[speciesIndex]-= stoichiometry * v[reactionIndex];
    } else {
      changeRate[speciesIndex]+= stoichiometry * v[reactionIndex];
    }
  }
  
  /**
   * 
   * @param time
   * @return
   */
  private double compileDouble(double time) {
    if (this.time != time) {
      this.time = time;
      if (!constant || time <= 0d) {
        computeStoichiometricValue();
      }
    }
    return stoichiometry;
  }
  
  /**
   * 
   */
  private void computeStoichiometricValue() {
    if (speciesRefIndex >= 0) {
      stoichiometry = Y[speciesRefIndex];
      stoichiometricCoefHash.put(id, stoichiometry);
    } else if (stoichiometricCoefHash != null
        && stoichiometricCoefHash.containsKey(id)) {
      stoichiometry = stoichiometricCoefHash.get(id);
    } else {
      if (isSetStoichiometryMath) {
        stoichiometry = nodeInterpreter.compileDouble(math);
      } else {
        stoichiometry = sr.getCalculatedStoichiometry();
      }
    }
  }
  
  /**
   * 
   * @return
   */
  public int getSpeciesIndex() {
    return speciesIndex;
  }
  
  
  /**
   * 
   * @return
   */
  public boolean isReactant() {
    return isReactant;
  }
  
}