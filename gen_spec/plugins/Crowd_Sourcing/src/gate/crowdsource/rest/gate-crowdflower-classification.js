require(['jquery-noconflict'], function($) {
 //Ensure MooTools is where it must be
  Window.implement('$', function(el, nc){
    return document.id(el, nc, this.document);
  });

  var $ = window.jQuery;
  //jQuery goes here

  $(".radios").each(function() {
    var def = $(this).data("default");
    $(this).find("[value=\""+def+"\"]").attr("checked", "checked");
    }
  );
});
