$(document).ready(function () {
    // Hàm Ẩn Hiện sidedropdown khi ấn vào 1 nút bên side dropdown
    $(".sidedropdown-btn").click(function () {
        // Mở hoặc đóng sidedropdown hiện tại khi click
        $(this).toggleClass("active");
        var sidedropdownContent = $(this).next();
        sidedropdownContent.slideToggle();

        // Đóng tất cả các sidedropdown khác khi mở 1 sidedropdown hiện tại
        $(".sidedropdown-btn").not($(this).removeClass("active"));
        $(".sidedropdown-container").not($(this).next()).slideUp();

        $("#logoutLink").on("click", function (e) {
            e.preventDefault();
            document.logoutForm.submit();//<form th:action="@{/logout}" method="post" name="logoutForm"/>
        });
    });

    // $(".dropup-btn").on("click", function(){
    //     $(this).toggleClass("active");
    //     $(".dropup-menu").slideToggle("fast");
    // });

    // Ẩn tất cả các sidedropdown-container khi click chuột ra ngoài
    // $(document).click(function (event) {
    //     var target = $(event.target);
    //     if (!target.hasClass("sidedropdown-btn")) {
    //         $(".sidedropdown-btn").removeClass("active");
    //         $(".sidedropdown-container").slideUp();
    //     }
    // });
});
