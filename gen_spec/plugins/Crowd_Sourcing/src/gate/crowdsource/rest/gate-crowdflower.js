// CrowdFlower uses MooTools JS library
$$('.gate-snippet').addEvent('change:relay(input)', function(e, target) {
  // when a checkbox inside an annotation snippet is checked or unchecked, toggle
  // the "selected" class on its containing label to match
  var parent = $$(target).getParents('label')[0];
  parent.toggleClass('selected', target.checked);
});

$$(".gate-snippet .cml_field").each(function(target) {
  Array.each(target.data("default").split(","), function(index) {
  	var checkbox = target.getElement("input.answer[value=\""+index+"\"]")
	var parent = $$(checkbox).getParents('label')[0];
  	checkbox.checked = true;
  	parent.toggleClass('selected', true);
   });
});