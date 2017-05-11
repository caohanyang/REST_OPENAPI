/*
 *  Copyright (c) 2010--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: TermbankTableModel.java 18976 2015-10-29 15:16:00Z adamfunk $
 */
package gate.termraider.gui;

import gate.termraider.bank.AbstractTermbank;
import gate.termraider.util.ScoreType;
import gate.termraider.util.Term;
import gate.termraider.util.TermComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;


public class TermbankTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 5178553454244139001L;

  private List<Term> terms;
  private AbstractTermbank termbank;
  private List<ScoreType> scoreTypes;

  
  public TermbankTableModel(AbstractTermbank termbank) {
    this.termbank = termbank;
    this.terms = new ArrayList<Term>(termbank.getTerms());
    // Is this necessary?  The table will have autosort wedges
    Collections.sort(this.terms, new TermComparator());
    this.scoreTypes = termbank.getScoreTypes();
  }


  public int getColumnCount() {
    // 1 column for the Term itself
    return this.scoreTypes.size() + 1;
  }

  
  public int getRowCount() {
    return this.terms.size();
  }

  
  public Object getValueAt(int row, int col) {
    Term term = this.terms.get(row); 
    if (col == 0) {
      return term.toString();
    }
    
    // Implied else: look the score up;
    // remember that the scoreType index is off by 1 from the column
    ScoreType type = scoreTypes.get(col - 1);
    return this.termbank.getScore(type, term);
  }


  public Class<?> getColumnClass(int col) {
    if (col == 0) {
      return String.class;
    }
    // implied else
    return Number.class;
  }

  
  public String getColumnName(int col) {
    if (col == 0) {
      return "term";
    }
    
    return this.scoreTypes.get(col - 1).toString();
  }

  
  public void setComparators(TableRowSorter<TermbankTableModel> sorter) {
    Comparator<Number> numberComparator = new NumberComparator();
    for (int i = 1 ; i < this.getColumnCount() ; i++ ) {
      sorter.setComparator(i, numberComparator);
    }
  }
  
  
}


class NumberComparator implements Comparator<Number>  {

  @Override
  public int compare(Number o1, Number o2) {
    if (o1.doubleValue() == o2.doubleValue()) {
      return 0;
    }
    if (o1.doubleValue() < o2.doubleValue()) {
      return -1;
    }
    return 1;
  }

}
