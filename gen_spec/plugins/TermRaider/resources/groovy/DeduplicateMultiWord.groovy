// Warning: this has to alter the inputAS; the outputAS is ignored.
// $Id: DeduplicateMultiWord.groovy 19035 2015-12-14 12:05:27Z markagreenwood $

// Delete extra MultiWord annotations with exactly the same span
// (keep one of each group)

List<Annotation> mwList = new ArrayList<Annotation>(inputAS.get("MultiWord"));
Collections.sort(mwList, new OffsetComparator());

for (int i=0 ; i < mwList.size() - 1 ; i++) {
  Annotation mwi = mwList.get(i);
  
  for (int j=i+1 ; j < mwList.size() ; j++) {
    Annotation mwj = mwList.get(j);
    
    if (mwj.getStartNode().getOffset() > mwi.getStartNode().getOffset()) {
       //if we've moved past the start offset of the outer annotation then
       //because the annotations are sorted we know we'll never find a matching
       //one so we can safely stop looking.
       break;
    }

    if (mwj.getStartNode().getOffset().equals(mwi.getStartNode().getOffset())
        && mwj.getEndNode().getOffset().equals(mwi.getEndNode().getOffset()) ) {
      inputAS.remove(mwi);
    }
  }
}

// Delete SelectedToken and MultiWord annotations that span
// or subspan NEs and Addresses.
// Also, copy language feature from Sentence down to term candidates.

Set<String> termTypes = new HashSet<String>();
termTypes.add("SingleWord");
termTypes.add("MultiWord");

Set<String> exclusionTypes = new HashSet<String>();
exclusionTypes.add("Person");
exclusionTypes.add("Organization");
exclusionTypes.add("Location");
exclusionTypes.add("Date");
exclusionTypes.add("Money");
exclusionTypes.add("Percent");
exclusionTypes.add("Address");
exclusionTypes.add("UserID");
exclusionTypes.add("Number");

AnnotationSet candidates = inputAS.get(termTypes);

AnnotationSet excluded = inputAS.get(exclusionTypes);
AnnotationSet strongStop = inputAS.get("StrongStop");

for (Annotation candidate : candidates) {
  // delete unwanted term candidates
  if (! gate.Utils.getCoveringAnnotations(excluded, candidate).isEmpty()) {
    FeatureMap newf = Factory.newFeatureMap();
    newf.putAll(candidate.getFeatures());
    String newType = "deleted_NE_" + candidate.getType();
    inputAS.add(candidate.getStartNode(), candidate.getEndNode(), newType, newf);
    inputAS.remove(candidate);  
  }
  
  else if (! gate.Utils.getContainedAnnotations(strongStop, candidate).isEmpty()) {
    FeatureMap newf = Factory.newFeatureMap();
    newf.putAll(candidate.getFeatures());
    String newType = "deleted_SS_" + candidate.getType();
    inputAS.add(candidate.getStartNode(), candidate.getEndNode(), newType, newf);
    inputAS.remove(candidate);  
  }
  
}
