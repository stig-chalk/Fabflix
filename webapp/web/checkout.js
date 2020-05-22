function setUpPage() {
    $("#logout").on("click", (e) => {
        e.preventDefault();
        $.ajax({
            method: "GET",
            url: "api/logout",
            success: function(){console.log("successfully logout")}
        });
        window.location.replace("login.html");
    });
    $(".backbtn").on("click", () => {
        window.location.replace("index.html");
    })

    $(".cartbtn").on("click", () => {
        window.location.replace("cart.html");
    })


}

window.onload = function() {
    setUpPage();
}

let checkout = $(".payment");

function handleSessionData(resultData) {
    let total = $(".total");
    total.empty();
    let cartDetails_table_body= $("#cartDetails_table");
    cartDetails_table_body.empty();
    let totalp = 0;
    for (let i = 0; i < resultData.length; i++) {

        let row = "<div class='cartDetails_single'><a href='single-movie.html?id=" + resultData[i]["id"] + "'>" +
            resultData[i]["name"] +  "</a>";
        row +=  "<p class='unitPrice'>" + resultData[i]["price"] + "$ ea</p>" +
            "<div><p>Quantity: " + resultData[i]["quantity"].toString()+"</p></div></div>";
        cartDetails_table_body.append(row);
        totalp += resultData[i]["price"]*resultData[i]["quantity"];
    }

    total.append("<p class=\"total-price\">"+"Total Price: " + totalp.toFixed(2) +"$</p>");
}


function placeOrder(event) {
    console.log("submit checkout form");
    event.preventDefault();
    $.ajax("api/checkout", {
        method: "POST",
        data: checkout.serialize(),
        success: function() {
            window.location.replace("confirmPage.html")
        },
        error: function () {
            alert("Error: Invalid payment info, please try again.");
        }
    });
}

checkout.submit(placeOrder);

$.ajax("api/checkout", {
    method: "GET",
    success: handleSessionData
});
