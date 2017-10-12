
$("[name=range]").on("change", function() {
    $("[for=range]").val("€" + this.value +".00  " );
}).trigger("change");
