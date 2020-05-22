function browseByGenre() {
    console.log(this.id)
    $.ajax({
        dataType: "json",
        method: "GET",
        data: {"browseBy" : "genre", "browseKey" : this.id, "orderBy" : $(".sort-by").val(),
               "limit" : $(".num-movies").val(), "ascOptR" : $(".asc-opt-rating").val(), "ascOptT" : $(".asc-opt-title").val(),
               "offset": 1},
        url: "api/movies",
        success: (resultData) => handleMovieResult(resultData)
    })
}

function browseByTitle() {
    console.log($(this).text());
    $.ajax({
        dataType: "json",
        method: "GET",
        data: {"browseBy" : "title", "browseKey" : $(this).text(), "orderBy" : $(".sort-by").val(),
               "limit" : $(".num-movies").val(), "ascOptR" : $(".asc-opt-rating").val(), "ascOptT" : $(".asc-opt-title").val(),
               "offset": 1},
        url: "api/movies",
        success: (resultData) => handleMovieResult(resultData)
    })
}

function jumpToPage() {
    console.log("now jump to page: " + sessionStorage.getItem("currentPage"));
    $.ajax(
        "api/movies", {
        method: "GET",
            data: search_form.serialize() + "&orderBy=" + $(".sort-by").val()
        + "&limit=" + $(".num-movies").val() + "&ascOptR=" + $(".asc-opt-rating").val() + "&ascOptT=" + $(".asc-opt-title").val()
        + "&offset=" + sessionStorage.getItem("currentPage"),
            success: (resultData) => handleMovieResult(resultData)
    });
}

function insertBrowseGenres(resultData) {
    let browseGenreTag = $(".browse-genre");
    browseGenreTag.empty();
    browseGenreTag.append("<p class='browse-type-title'>Browse By Genre</p>");
    for (let i = 0; i < resultData.length; i++) {
        let row = "";
        let id = resultData[i]["genreId"];
        row += "<button class='browse-genre-submit' id='" +
                     id + "'>" +
                    resultData[i]["genreName"] +
                "</button>";
        browseGenreTag.append(row);
    }
}


function handleMovieResult (resultData) {
    // result data format: [0]userinfo, [1]all genres, [2]total movie num, [3-inf]movie items
    console.log("handleStarResult: populating star table from resultData");

    let fullName = resultData[0]["fullName"];
    $("#user_welcome_message").text("Welcome back " + fullName);

    insertBrowseGenres(resultData[1]);
    console.log(resultData[2]["totalNum"]);

    let prevTotal = sessionStorage.getItem("totalPage");
    let newTotal = Math.ceil(parseInt(resultData[2]["totalNum"]) / parseInt($(".num-movies").val())).toString();
    if ((prevTotal === null) || (newTotal !== prevTotal)) {
        sessionStorage.setItem("totalPage", newTotal);
        sessionStorage.setItem("currentPage", 1);
        sessionStorage.setItem("startPage", 1);
        loadPageNum();
    }


    resultData = resultData.slice(3, resultData.length);
    let movieTableBody = $(".list");
    movieTableBody.empty();
    for (let i = 0; i < resultData.length; i++) {
        let row = "";
        row +=
            "<div class='movieItem'>" +
            "<a class='movieTitle' href='single-movie.html?id=" + resultData[i]['movieId'] + "' id='" +
            resultData[i]['movieId'] + "'>" +
            resultData[i]["movieTitle"] + "</a><div>" +
            "<p class='rating'>" + resultData[i]["movieRating"] + "</p>" +
            "<button class='add-to-cart' id='" + resultData[i]['movieId'] + "'>+ <img src='cart.png'></button></div>" +
            "<p>" + resultData[i]["movieYear"] +
            ", Director: " + resultData[i]["movieDirector"] + "</p>" +
            "<p> Genres: ";

        let genres = resultData[i]["movieGenres"];
        for (let x = 0; x < genres.length; x++) {
            row += "<a class='browse-genre-submit' id=" + genres[x]["genreId"] + " style='color: gold; cursor: pointer; padding:0;'>" +
                genres[x]["genreName"] + "</a>";
            if (x < genres.length-1) {
                row += ", "
            }
        }

        row += "</p><p> Stars: ";

        let stars = resultData[i]["movieStars"];
        for (let j = 0; j < stars.length; j++) {
            row += '<a href="single-star.html?id=' + stars[j]["id"] + '">' +
                stars[j]["name"] + '</a>'
            if (j < stars.length - 1)
                row += ", ";
        }
        row += "</p></div>";
        movieTableBody.append(row);
    }

    if (resultData.length == 0) {
        movieTableBody.append("<th>Seems like there is nothing to show..</th>")
    }

    $(".browse-genre-submit").each(function() {
        $(this).on("click", browseByGenre);
        $(this).hover(function() {
            $(this).css("text-decoration", "underline");
        }, function() {
            $(this).css("text-decoration", "none");
        });
    });

    $(".add-to-cart").each(function() {
        $(this).on("click", addToCart);
    })

}

function addToCart() {
    let id = this.id;
    let title = $("a#"+id).text();
    $.ajax("api/cart", {
        method: "POST",
        data: {"id" : id, "title" : title},
        success: console.log("successfully added in to cart")
    });
}

function loadPageNum() {
    let totalPage = parseInt(sessionStorage.getItem("totalPage"));
    let pageNum = $(".page-num");
    pageNum.empty();
    for (var i = 1; i <= totalPage; i++) {
        pageNum.append("<button class='single-page-num' id=" + i + " style='width: 8%;'>"  + i + "</button>");
    }

    $(".single-page-num").each(function (){
        let maxPage = parseInt(sessionStorage.getItem("startPage"));
        let currentPage = parseInt(sessionStorage.getItem(("currentPage")));
        maxPage += 10;
        if (parseInt($(this).text()) == currentPage) {
            $(this).css("color", "red");
        }
        if (parseInt($(this).text()) > maxPage) {
            $(this).hide();
        }

        $(this).on("click", function(){
            let prevPage = sessionStorage.getItem("currentPage");
            let currentPage = $(this).text();
            $("#" + prevPage + ".single-page-num").css("color", "gold");
            $("#" + currentPage + ".single-page-num").css("color", "red");
            sessionStorage.setItem("currentPage", currentPage);
            jumpToPage();
        });
    });

    $("#prev-page").unbind("click").bind("click", function () {
        let prevPage = parseInt(sessionStorage.getItem("currentPage"));
        let currentPage = prevPage - 1;
        let startPage = parseInt(sessionStorage.getItem("startPage"));
        if (currentPage >= 1) {
            sessionStorage.setItem("currentPage", currentPage);
            $("#" + prevPage + ".single-page-num").css("color", "gold");
            $("#" + currentPage + ".single-page-num").css("color", "red");

            if (currentPage < startPage) {
                $("#" + (startPage+10) + ".single-page-num").hide();
                $("#" + currentPage + ".single-page-num").show();
                sessionStorage.setItem("startPage", startPage-1);
            }
            jumpToPage();
        }

    })

    $("#next-page").unbind("click").bind("click", function () {
        console.log("jump")
        let totalPage = parseInt(sessionStorage.getItem("totalPage"));
        console.log(totalPage);
        let prevPage = parseInt(sessionStorage.getItem("currentPage"));
        let currentPage = prevPage + 1;
        let startPage = parseInt(sessionStorage.getItem("startPage"));
        if (currentPage <= totalPage) {
            sessionStorage.setItem("currentPage", currentPage);
            $("#" + prevPage + ".single-page-num").css("color", "gold");
            $("#" + currentPage + ".single-page-num").css("color", "red");

            if (currentPage > startPage+10) {
                $("#" + startPage + ".single-page-num").hide();
                $("#" + currentPage + ".single-page-num").show();
                sessionStorage.setItem("startPage", startPage+1);
            }
            jumpToPage();
        }
    })
}






function submitSearchForm() {
    $.ajax(
        "api/movies", {
            method: "GET",
            data: { "title" : $("input[name='title']").val(), "director" : $("input[name='director']").val(),
                    "year" : $("input[name='year']").val(), "actor" : $("input[name='actor']").val(),
                    "orderBy" : $(".sort-by").val(), "limit" : $(".num-movies").val(),
                    "ascOptR" : $(".asc-opt-rating").val(), "ascOptT" : $(".asc-opt-title").val(),
                    "offset": 1},
            success: (resultData) => handleMovieResult(resultData)
        }
    );
    $(".searchTerm").val("");
}




window.onload = function() {
    setUpPage();
    let orderItem = JSON.parse(sessionStorage.getItem("orders"));

    console.log(orderItem);
    if (orderItem !== null) {
        $(".sort-by").val(orderItem["orderBy"]);
        $(".num-movies").val(orderItem["limit"]);
        $(".asc-opt-rating").val(orderItem["ascOptR"]);
        $(".asc-opt-title").val(orderItem["ascOptT"]);
    }
    else {
        $(".sort-by").val("rating");
        $(".num-movies").val(10);
        $(".asc-opt-rating").val("desc");
        $(".asc-opt-title").val("asc");
    }
    storeOrderSession();

    let browseBy = sessionStorage.getItem("browseGenre");
    if (browseBy !== null) {
        sessionStorage.removeItem("browseGenre");
        console.log("browseByGenre: " + browseBy);
        $.ajax({
            dataType: "json",
            method: "GET",
            data: {"browseBy" : "genre", "browseKey" : browseBy, "orderBy" : $(".sort-by").val(),
                "limit" : $(".num-movies").val(), "ascOptR" : $(".asc-opt-rating").val(), "ascOptT" : $(".asc-opt-title").val(),
                "offset": 1},
            url: "api/movies",
            success: (resultData) => handleMovieResult(resultData)
        })
    } else {
        $.ajax({
            dataType: "json",
            method: "GET",
            data: {"orderBy" : $(".sort-by").val(), "limit" : $(".num-movies").val(),
                "ascOptR" : $(".asc-opt-rating").val(), "ascOptT" : $(".asc-opt-title").val(), "offset" : sessionStorage.getItem("currentPage")},
            url: "api/movies",
            success: (resultData) => handleMovieResult(resultData)
        });
    }


}

function storeOrderSession() {
    let orderItem = {"orderBy" : $(".sort-by").val(), "limit" : $(".num-movies").val(),
        "ascOptR" : $(".asc-opt-rating").val(), "ascOptT" : $(".asc-opt-title").val(), "offset": sessionStorage.getItem("currentPage")};
    sessionStorage.setItem("orders", JSON.stringify(orderItem));
    return orderItem;
}




function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")
    query = query.trim();
    // TODO: if you want to check past query results first, you can do it here
    let querys = JSON.parse(sessionStorage.getItem("querys"));

    if (querys[query]) {
        console.log("using cached result");
        handleLookupAjaxSuccess(querys[query], query, doneCallback);

    } else {
        jQuery.ajax({
            method: "GET",
            dataType: "json",
            data: {"autocp" : query},
            url: "api/movies",
            success: function(data) {
                handleLookupAjaxSuccess(data, query, doneCallback);
            },
            error: function(errorData) {
                console.log("lookup ajax error");
                console.log(errorData);
            }
        })
    }
}


function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON

    // console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here
    let querys = JSON.parse(sessionStorage.getItem("querys"));
    if (!querys[query]) {
        console.log("add new query result");
        querys[query] = data;
        sessionStorage.setItem("querys", JSON.stringify(querys));
    }

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}


function handleSelectSuggestion(suggestion) {
    window.location.replace("single-movie.html?id=" + suggestion["data"]["movieId"]);
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"]);
}


function setUpPage() {
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

    $("select").on("change", (e) => {
        let orderItem = storeOrderSession();
        $.ajax({
            dataType: "json",
            method: "GET",
            data: orderItem,
            url: "api/movies",
            success: (resultData) => handleMovieResult(resultData)
        })
    })

    $(".dropbtn").on("click", () => {
        let dropdown = $(".dropdown");
        if (dropdown.css("display") == "none") {
            $(".dropdown").css("display", "block");
            $(".dropbtn_image").attr("src", "up.png");
        } else {
            $(".dropdown").css("display", "none");
            $(".dropbtn_image").attr("src", "browse.png");
        }

    });

    $(".filterbtn").on("click", ()=> {
        let pos = $(".filterbtn").offset();
        let filterDetail = $(".filterdetail");
        if (filterDetail.css("display") == "none") {
            filterDetail.css("display", "block");
            filterDetail.css("left", pos.left);
            $(".filterbtn>img").attr("src", "up.png");
        } else {
            filterDetail.css("display", "none");
            $(".filterbtn>img").attr("src", "filter.png");
        }
    })

    $(".browse-title").append("<button class='browse-title-submit'>*</button>");
    for (var i = 48; i <= 90; i++) {
        if (i == 58)
            i = 65;
        $(".browse-title").append("<button class='browse-title-submit'>" + String.fromCharCode(i) + "</button>");
    }
    $("button.browse-title-submit").each(function () {
        $(this).on("click", browseByTitle);
    });

    $(".title").on("click", () => {
        window.location.replace("index.html");
    });

    $(".cartbtn").on("click", () => {
        window.location.replace("cart.html");
    });

    $(".titlesearchbtn").on("click", () => {
        console.log("doing normal search");
        submitSearchForm();
    });

    $(".deletebtn").on("click", () => {
        $("input").each(function() {
            $(this).val('');
        })
    });

    if (sessionStorage.getItem("currentPage") === null) {
        sessionStorage.setItem("startPage", 1);
        sessionStorage.setItem("currentPage", 1);
    }

    $(".searchTerm").autocomplete({
        // documentation of the lookup function can be found under the "Custom lookup function" section
        lookup: function(query, doneCallback) {
            handleLookup(query, doneCallback);
        },
        onSelect: function(suggestion) {
            handleSelectSuggestion(suggestion);
        },
        // set delay time
        deferRequestBy: 300,
        // there are some other parameters that you might want to use to satisfy all the requirements
        minChars: 3,
    });

    $('.searchTerm').keypress(function(event) {
        if (event.keyCode === 13) {
            console.log("doing normal search");
            submitSearchForm();
        }
    });

    sessionStorage.setItem("querys", JSON.stringify(new Object()));

    loadPageNum();

}


