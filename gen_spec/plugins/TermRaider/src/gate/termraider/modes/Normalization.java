/*
 *  Copyright (c) 2013--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: Normalization.java 18978 2015-10-29 15:55:39Z adamfunk $
 */
package gate.termraider.modes;

public enum Normalization {
  None,
  Hundred,
  Sigmoid,
  SigmoidOld;
  
  
  // Old scale was mushing too many terms together where rounding made them
  // all appear tied for 100.0.
  // The old scale is available as a non-default option for backward compatibility
  private static double xScaleDefault = 80;
  private static double xScaleOld = 4.8;

  
  
  public static double calculate(Normalization mode, Number raw) {
    if (mode == None) {
      return raw.doubleValue();
    }
    
    if (mode == Hundred) {
      return 100.0 * raw.doubleValue();
    }
    
    if (mode == SigmoidOld) {
      return normalizeScore(raw.doubleValue(), xScaleOld);
    }
    
    
    // must be sigmoid
    return normalizeScore(raw.doubleValue(), xScaleDefault);
  }
  
  
  /**
   * The following produces the right half of a sigmoid 
   * curve adjusted so that
   * f(0) = 0; f(inf) = 100; f(x>0) > 0
   * @param score from 0 to inf 
   * @return score from 0 to 100
   */
  private static double normalizeScore(double score, double xScale) {
    double norm = 2.0 / (1.0 + Math.exp(-score / xScale)) - 1.0;
    return (double) (100.0F * norm);
  }
  
  /* Note: Normalization mode does not apply to the AnnotationTermbank, since it
   * is derived from (presumably) already normalized tf.idf values.
   */

  
}
