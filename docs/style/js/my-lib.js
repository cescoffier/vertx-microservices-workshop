$(document).ready(function () {
    $(".assignment").each(function (i, element) {
        var id = "assignment-" + i;
        $(element)
            .before($("<a class='btn btn-primary' " +
                "data-toggle='collapse' href='#" + id + "'" +
                ">Show me the code</a>"))
            .toggleClass("collapse").attr("id", id);
    });
});
