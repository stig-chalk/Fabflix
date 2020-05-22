function handleResult(result) {
    console.log("handle result");
    let moviesBody = $(".movies");
    moviesBody.empty();
    let totalPrice = result[result.length - 1];
    result = result.slice(0, result.length-1);
    for (let i = 0; i < result.length; i++) {

        moviesBody.append(  "<p>"+ result[i]["saleId"] + " " + result[i]["title"] + " * " + result[i]["quantity"] + "</p>");
    }
    moviesBody.append("<h3>" + totalPrice.toFixed(2) + "$</h3>");
}

console.log("right");

$.ajax("api/confirm", {
    type: "GET",
    dataType: "json",
    success: handleResult
});

$("button").on("click", () => {
    $.ajax("api/confirm", {
        type: "GET",
        data: {"type": "back"} ,
        success: function() {
            window.location.replace("index.html");
        }
    });
});