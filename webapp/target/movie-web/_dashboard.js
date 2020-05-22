function handleResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    let fullName = resultData[0]["fullName"];
    $("#user_welcome_message").text("Welcome back " + fullName);

    resultData = resultData.slice(1, resultData.length);
    let tableDetails = $(".table-details");
    tableDetails.empty();
    for (var i = 0; i < resultData.length; i++) {
        let singleTable = resultData[i];
        let row = "<div class='single-table'><h5>" + singleTable["tableName"] + "</h5>";
        let singleAttrs = singleTable["columns"];
        for (var j = 0; j < singleAttrs.length; j++) {
            row += "<p>" + singleAttrs[j]["field"] + ": " + singleAttrs[j]["type"] + "</p>";
        }
        row += "</div>"
        tableDetails.append(row);
    }
}

let addStar_form = $(".insert-star");
let addMovie_form = $(".insert-movie");

function insertStar(formSubmitEvent) {
    console.log("submit add star form");

    formSubmitEvent.preventDefault();
    if (!checkInputValid(".insert-star")) {
        alert("Add Star Error: Please fill in all the required attributes");
    } else {
        $.ajax({
            dataType: "json",
            data: addStar_form.serialize() + "&insertType=star",
            method: "GET",
            url: "api/dashbord",
            success: function(result) {
                alert("star_id: " + result['star_id']);
            }
        });
        addStar_form[0].reset();
    }
}

function insertMovie(formSubmitEvent) {
    console.log("submit add movie form");

    formSubmitEvent.preventDefault();
    if (!checkInputValid(".insert-movie")) {
        alert("Add Movie Error: Please fill in all the required attributes");
    } else {
        $.ajax({
            dataType: "json",
            data: addMovie_form.serialize() + "&insertType=movie",
            method: "GET",
            url: "api/dashbord",
            success: function(result) {
                alert("Movie id: " + result['movie_id'] + "  Star id:" + result['star_id'] + "  Genre id: " + result['genre_id']);
            },
            error: function(jqXHR, textStatus, errorThrown){
                alert("Duplicated Movie");
            }
        });
        addMovie_form[0].reset()
    }
}


function checkInputValid(className) {
    var result = true;
    $(className + " .req").each(function() {
        if ($(this).val() === '') {
            result = false
        }
    })
    return result;
}
$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/dashbord",
    success: handleResult
});

$(".insertbtn").on("click", function() {
    let dropdown = $(".dropdown");
    if (dropdown.css("display") == "none") {
        $(".dropdown").css("display", "flex");
        $(this).empty();
        $(this).append('<img class="dropbtn_image" src="up.png" height="20px" width="20px">')
    } else {
        $(".dropdown").css("display", "none");
        $(this).empty();
        $(this).append("Insert")
    }
});

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

addStar_form.submit(insertStar);
addMovie_form.submit(insertMovie);