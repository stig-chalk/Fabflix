let login_form = $("#login_form");

$(".button").hide();

$('input[value="customers"]').prop('checked', true);

$('input[type="checkbox"]').on('change', function() {
    $('input[type="checkbox"]').not(this).prop('checked', false);
});





function handleLoginResult(resultData) {
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultData["status"] === "success") {
        let targetPage = "index.html";
        if (resultData["directTo"] === "employees") {
            targetPage = "_dashboard.html"
        }
        window.location.replace(targetPage);
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        $("#login_failed_message").text(resultData["message"]);
        grecaptcha.reset();
        $(".button").hide();
    }
}

function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");

    formSubmitEvent.preventDefault();


    $.ajax(
        "api/login", {
            method: "POST",
            data: login_form.serialize(),
            success: (result) => handleLoginResult(result)
        }
    );
}

function reCaptchaVerify(response) {
    if (response.length > 0) {
        $(".button").show();
    }
}

const reCaptchaCallback = function () {
    grecaptcha.render(
        "recaptcha-element", {
            'sitekey': "6Lc9kvMUAAAAAE2GO6amBm5g2eEg15fMhW4ajJ9H",
            'theme': "dark",
            'callback': reCaptchaVerify
        });
};

window.reCaptchaCallback = reCaptchaCallback;
login_form.submit(submitLoginForm);


