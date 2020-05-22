function getParameterByName(target) {
    let url = window.location.href;

    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    let movieInfo = jQuery("#movie_info");

    movieInfo.append(
        "<p class='head' id='" + resultData[0]["movieId"] + "'style='font-family:verdana;font-weight:bold'>" + resultData[0]["movieTitle"] + "</p>" +
        "<p style='font-size:80%'>Release Year: " + resultData[0]["movieYear"] + "</p>" +
        "<p style='color:navy ;font-size:60%;font-weight:bold'>Director: " + resultData[0]["movieDirector"] + "</p>");


    console.log("handleResult: populating movie details table from resultData");

    let movieDetailsTable = jQuery("#movieDetails_table_body");

    let row = "<th>";
    for (let j = 0; j < resultData[0]["movieGenres"].length; j++) {
        row += "<p class='genre' id='" + resultData[0]["movieGenres"][j]["id"] + "'>"  + resultData[0]["movieGenres"][j]["name"]   + "</p>";
    }
    row += "</th><th>";
    for (let i = 0; i < resultData[0]["movieStars"].length; i++) {
        row += '<a href="single-star.html?id=' + resultData[0]['movieStars'][i]["id"] + '">' +
                resultData[0]['movieStars'][i]["name"] +  '</a>' + ", ";
    }
    row = row.substring(0,row.length-2);
    row += "</th>" +
           "<th>"  + resultData[0]["movieRating"]   + "</th>" +
           "</tr>";

    movieDetailsTable.append(row);
    $(".genre").each(function() {
        $(this).on("click", () => {
            sessionStorage.setItem("browseGenre", $(this).attr("id"));
            window.location.replace("index.html");
        })
    })
}



let movieId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});


$(".title").on("click", () => {
    window.location.replace("index.html");
})

$(".cartbtn").on("click", () => {
    window.location.replace("cart.html");
})

$("#logout").on("click", (e) => {
    e.preventDefault();
    sessionStorage.clear();
    $.ajax({
        method: "GET",
        url: "api/logout",
        success: function(){console.log("successfully logout")}
    });
    window.location.replace("login.html");
});

$(".add-to-cart").on("click", addToCart);



function addToCart() {
    let head = $(".head");
    $.ajax("api/cart", {
        method: "POST",
        data: {"id" : head.attr("id"), "title" : head.text()},
        success: console.log("successfully added in to cart")
    });
}