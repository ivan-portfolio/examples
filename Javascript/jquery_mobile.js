console.log('loaded index.js');
var transition = false;

function makeListFocusable() {
    console.log('makefocusable');

    $(".rule").children(".body").children("ol").find("ol").children("li").click(function() {
        if ($(this).hasClass("active")) {
            $(this).removeClass("active");
        }
        else {
        $(".rule").children(".body").children("ol").find("ol").children("li").removeClass("active");
        $(this).addClass("active");   
        }
    });
}

$(document).on("mobileinit", function() {
    console.log('mobileinit');

});

$(document).on("pagecreate", function() {
    console.log("pageinit");

    $(document).on("pagecontainertransition", function() {
        console.log("pagecontainer:transition");
        transition = false;
    });

    $(".page").on("swiperight", function() {
        console.log('swiped');
        if (!transition) {
            transition = true;
            $.mobile.back();
        }
    });

    //set default focus to remove old :focus remainers;
    $(".page").focus();

    if($(".page").hasClass("field")) {
        console.log("init fieldpage");
        $(".options").find("li").click(function() {
            var id = $(this).attr("id");
            $(".options").find("li").removeClass("active");
            $(this).addClass("active");
            var img = "./img/" + id + ".png";
            $(".figure").children("img").attr("src", img);
            $(".content").hide();
            $("."+id).fadeIn("slow");
        });
    }
});


var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        $(".main").fadeIn("fast");
        console.log('Received Event: ' + id);
    }
};

