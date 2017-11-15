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



var min = $("#min").val();
var max = $("#max").val();

$("[id=min]").keypress(function (event) {
    if (event.which == 13) {
        event.preventDefault();

        min = $("#min").val();
        refresh(min,max);
    }
});

$("[id=max]").keypress(function (event) {
    if (event.which == 13) {
        event.preventDefault();

        max = $("#max").val();
        refresh(min,max);
    }
});

function refresh(minprice, maxprice) {
    var path = window.location.pathname + "&filter=minprice:" + minprice + ";maxprice:" + maxprice + "";

    window.location.replace(path);
    window.location.href = path;
}