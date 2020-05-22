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

    console.log("handleResult: populating star info from resultData");

    let movieInfo = jQuery("#Star_info");

    movieInfo.append(
        "<p class='head' style='font-family:verdana;font-weight:bold'>" + resultData[0]["starName"] + "</p>" +
        "<p style='font-size:80%'>Year of Birth: " + resultData[0]["starYear"] + "</p>");


    console.log("handleResult: populating movie details table from resultData");

    let starDetailsTable = jQuery("#starDetails_table_body");

    let movies =  "<th>";
    for (let i = 0; i < resultData[0]["starMovies"].length; i++) {
        movies += '<a href="single-movie.html?id=' + resultData[0]['starMovies'][i]["id"] + '">' +
            resultData[0]['starMovies'][i]["name"] +  '</a>' + ", ";
    }

    movies = movies.substring(0,movies.length-2);
    movies += "</th>" ;

    starDetailsTable.append(movies);
}



let starId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-star?id=" + starId,
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
