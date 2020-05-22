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
    }
    )


}

window.onload = function() {
    setUpPage();
}

let cart = $("#cart");

function handleSessionData(resultData) {
    let cartDetails_table_body= $("#cartDetails_table");
    cartDetails_table_body.empty();

    for (let i = 0; i < resultData.length; i++) {

        let row = "<div class='cartDetails_single'><a href='single-movie.html?id=" + resultData[i]["id"] + "'>" +
            resultData[i]["name"] +  "</a>";
        row +=  "<p class='unitPrice'>" + resultData[i]["price"] + "$ EA</p>" +
            "<div class='quantity-div'><p class='quantity-p'>Quantity: </p><input id='" + resultData[i]["id"] + "' name='quantity' value='" + resultData[i]["quantity"].toString() +
            "'type='number' min='0' max='10'></div></div>";
        cartDetails_table_body.append(row);
    }

    $("input").each(function () {
        $(this).change(updateCart);
    })

}


$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});

function updateCart() {
    console.log($(this).attr("id"));
    $.ajax("api/cart", {
        method: "GET",
        data: {"quantity" : $(this).val(), "id" : $(this).attr("id")},
        success: (data) => handleSessionData(data)
    })
}
