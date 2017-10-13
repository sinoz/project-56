$("[name=range]").on("change", function() {
    $("[for=range]").val("€" + this.value +".00");

    return false;
}).trigger("change");

// I.A: Say thanks to my buddy Collin Haines
$("[name=range]").mouseup(function(event) {
    var path = window.location.pathname + "&filter=maxprice:" + this.value + "";

    window.location.replace(path);
    window.location.href = path;
});