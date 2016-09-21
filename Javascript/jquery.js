var index = 0;
var activeMarkerId = 0;
var formEntity;
var dragIndex = -1;

var mactive = {
    url: '/map/img/markers/marker-blue.png',
    size: new google.maps.Size(24, 46),
    origin: new google.maps.Point(0,0),
    anchor: new google.maps.Point(12,40)
};

var mpip = {
    url: '/map/img/markers/measle-blue.png',
    size: new google.maps.Size(12,12),
    origin: new google.maps.Point(0,0),
    anchor: new google.maps.Point(6,6)
};

function mapClick(location) {
    /** hide search tab if open **/
    if ($(".side-search").css("display") != "none") {
        dismissSearchPane();
    }
    inactivateOldMarker();      /** set old marker (if any) icon to measle **/
    activeMarkerId = index;     /** set activeMarkerId to new index **/
    
    addFormData(formEntity, location);        /** add form entity for new marker **/
    this.addMarker(location);       /** add new marker to map at location **/
    this.modifyAddress(location.lat() + ", " + location.lng());     /** assign placeholder address **/

    /* if path is empty then remove info page */
    var path = poly.getPath();
    if (path.getLength() == 0) {
        $(".body-container").removeClass("info");
        $(".side-info").hide();
    }
    path.push(location);
    
    index++;        /** lastly update index for new entries **/
}


function addMarker(location) {
    var marker = new google.maps.Marker({
        position: location,
        map: map,
        draggable: true,
        markerId: index,
        icon: mactive 
    });
    
    nodes.push(marker);         /** add new marker to list of markers for ref **/

    google.maps.event.addListener(marker, 'click', function(event) {
        setActiveMarker(this.markerId);
        changeFormFocus(this.markerId);
    });
    google.maps.event.addListener(marker, 'dragstart', function(event) {
        dragIndex = getMarkerIndex(this.markerId);
    });
    google.maps.event.addListener(marker, 'drag', function(event) {
        poly.getPath().setAt(dragIndex, event.latLng);
    });
    google.maps.event.addListener(marker, 'dragend', function(event) {
        setActiveMarker(this.markerId);
        poly.getPath().setAt(dragIndex, event.latLng);
        changeMarkerGeo(this.markerId, event.latLng);
        dragIndex = -1;
    });

}

function getMarkerIndex(id) {
    for (i = 0; i < nodes.length; i++) {
        if(nodes[i].markerId == id) {
            return i;
        }
    }
    return i;
}

function deleteNode(id) {
    var i;
    for (i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.markerId == id) {
            node.setMap(null);                  /** remove marker from map **/
            nodes.splice(i,1);                  /** remove marker from array **/
            $("#"+node.markerId).remove();      /** delete form entity **/
            poly.getPath().removeAt(i);
            return;
        }
    }
    
}

/** returns 0 if no next node found, returns 1 if successful **/
function nextNode() {
    var id = activeMarkerId;
    var node;
    for (i = 0; i < nodes.length;) {
        node = nodes[i];
        i++;
        if (node.markerId == id && i != nodes.length) {
            node = nodes[i];
            setActiveMarker(node.markerId);
            break;
        }
    }
    changeFormFocus(node.markerId);
    panMap(node.position);
    if (id == node.markerId) {
        return 0;
    }
    else return 1;
}

/** return 0 if no prev, return 1 if prev was found **/
function prevNode() {
    var id = activeMarkerId;
    var prev = nodes[0];
    for (i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.markerId == id) {
            break;
        }
        prev = nodes[i];
    }
    setActiveMarker(prev.markerId);
    changeFormFocus(prev.markerId);
    panMap(prev.position);
    if (prev.markerId == id) {
        return 0;
    }
    else return 1;
}

function inactivateOldMarker() {
    var oldMarker = getMarker(activeMarkerId);
    if (oldMarker != null) {
        oldMarker.setIcon(mpip);
    }
}

function setActiveMarker(id) {
    inactivateOldMarker();
    var newMarker = getMarker(id);
    newMarker.setIcon(mactive);
    activeMarkerId = id;
}

function getMarker(id) {
    for (i = 0; i < nodes.length; i++) {
        if (nodes[i].markerId == id) {
            return nodes[i];
        }
    }
    return null;
}

function search(place) {
    var location;
    if (place.geometry != null) {
        location = place.geometry.location;
        /** this code will add a marker at location of search address**/
        mapClick(location);
        
        /** this moves the map to the searched locaton **/
        panMap(location);
    }
    else {
        searchAddress(place.name);
    }
}

function panMap(location) {
    var zoom = map.getZoom();
    /** this code zooms in the map to a close up of location **/
    /**
    if (zoom < 16) {
        map.setZoom(17);
    }
    **/
    map.panTo(location);
    map.panBy(240,0);

}

function searchAddress(address) {
    console.log("searchaddress: " + address);
    geocoder.geocode( {"address" : address}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
            /** this code will add a marker at location of search address**/
            mapClick(results[0].geometry.location);
            /** this moves the map to the searched locaton **/
            panMap(results[0].geometry.location);
        }
        else {
            console.log('Geocode was not successfull: ' + status);
        }
    });
}


function changeMarkerGeo(id, location) {
    $("#"+id).find(".lat").val(location.lat());
    $("#"+id).find(".lng").val(location.lng());
    getFormattedAddress(location);
    
}

function changeFormFocus(id) {
    $(".map-form-entity").hide();
    $("#"+id.toString()).show();
    $("#"+id.toString()).parent().animate({scrollTop: 0}, "fast");
    modifyAddress($("#"+id.toString()).find(".revgeo").val());
}

function addFormData(template, location) {
    var data = template.replace(/blank/g, activeMarkerId);
    $(".form-content").prepend(data);                                        /** add form entity for new entry **/
    $("#blank").first().attr("id", activeMarkerId);                     /** assign marker id to new entity **/
    
    activateFormEvents();                                                  /** enable js-functions **/
    changeMarkerGeo(activeMarkerId, location);                          /** fill in form geo-code **/
    changeFormFocus(activeMarkerId);                                    /** hide other entities **/
}

function modifyAddress(address) {
    $(".marker-address").attr("title", address);
    $(".marker-address").text(address);
    $("#"+ this.activeMarkerId.toString()).find(".revgeo").val(address);
}

function getFormattedAddress(location) {
    var address;
    geocoder.geocode({'latLng': location}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
            if (results[0]) {
                address = results[0].formatted_address;
            } else {
            address = 'No results found';
            } 
        } else {
            address = 'Geocoder failed due to: ' + status;
        }
        this.modifyAddress(address);
    });

}

/** form js effects **/
function activateFormEvents() {
    $(".map-form-entity").first().find(".btn-path").click(function() {
        var type = $(this).data("type");
        $(this).parent().find(".type").val(type);
        $(this).parent().find(".btn-path").removeClass("active");
        $(this).addClass("active");
    });

    $('.map-form-entity').first().find('.noUi-slider').noUiSlider({
        start:  [3],
        step:   1,
        connect:    "lower",
        range:  {
            'min':  [1],
            'max':  [5]
        }
    });

    $('.map-form-entity').first().find('.noUi-slider').on({
        slide:  function() {
            var val = $(this).val();
            $(this).parent().find('.rate-input').val(val);
            var set = $(this).parent().find(".rate-value").data("set");
            var label = rateLabels[parseInt(set)][parseInt(val)-1];
            $(this).parent().find(".rate-value").children().text(label);
        }
    });

    $(".map-form-entity").first().find(".section-comment").find("textarea").keyup(function() {
        if($(this).val().length >= 1000) {
            $(this).parent().addClass('warning-show');
        }
        else {
            $(this).parent().removeClass('warning-show');
        }
    });


}

function dismissSubmitPane() {
    if ($(".body-container").hasClass("submit-complete")) {
        return;
    }
    $(".side-confirm").fadeOut("slow");
    $(".map-overlay").fadeOut("slow");
    $(".main-menu").removeClass("submit-active");
    $(".body-container").removeClass("submit-active");
}

function dismissSearchPane() {
    $(".side-search").fadeOut("slow");
    $(".body-container").removeClass("search");
}

function resetMap() {
    if ($(".body-container").hasClass("submit-active")) {
        return;
    }

    dismissSearchPane();
    $(".body-container").addClass("info");
    $(".side-info").fadeIn("fast");
    $(".form-content").empty();
    poly.getPath().clear();
    for (i = 0; i < nodes.length; i++) {
        nodes[i].setMap(null);
    }
    index = 0;
    nodes = [];
}

function showHelp() {
    if ($(".body-container").hasClass("submit-complete")) {
        return;
    }
    $(".help").fadeIn("fast");
    $(".help-container").find(".inner").animate({scrollTop: 0}, "fast");
    $(".side-dimen").addClass("hidden");
}

function hideHelp() {
    $(".help").fadeOut("fast");
    $(".side-dimen").removeClass("hidden");
}

function submitComplete() {
    $(".body-container").addClass("submit-complete");
    $(".thanks").fadeIn("fast");
    $(".inputs").hide();

}

/* init */
$(document).ready(function() {
    $.ajax({
        url: formEntityUrl,
            success: function(data) {
                formEntity = data;
            }
    });

    $('#createroute').submit(function(event) {
        event.preventDefault();

        /** check route name is not zero **/
        if ($("#name").val().length == 0) {
           $(".route-name").first().addClass("required-show");
           $(".route-name").parent().animate({scrollTop: 0}, "fast");
           return;
        }
        
        var url = $(this).attr('action');
        var data = $(this).serialize();

        $.ajax({
            type:   "POST",
            url:    url,
            data:   data,
            success: function() {
                submitComplete();
            }
        });
    });

    $(".btn-submit").click(function() {
        if ($(".body-container").hasClass("submit-complete")) {
            return;
        }
        $(".side-confirm").fadeIn("fast");
        $(".map-overlay").fadeIn("fast");
        $(".body-container").addClass("submit-active");
        $(".main-menu").addClass("submit-active");
        dismissSearchPane();
    });

    /** SIDE HEADER FUNCTIONS **/
    $(".btn-delete").first().click(function() {
        if (nodes.length == 1) {
            resetMap();
        }
        else {
            var id = activeMarkerId;
            if (prevNode() == 0) {
                nextNode();
            }
            deleteNode(id);
        }
    });

    $(".btn-prev").first().click(function() {
        prevNode();
    });

    
    $(".btn-next").first().click(function() {
        nextNode();
    });
    /***/
    
    /** submit boundry functions **/
    $("#name").keyup(function() {
        if($(this).val().length >= 50) {
            $(".route-name").first().addClass('warning-show');
        }
        else {
            $(".route-name").first().removeClass('required-show');
            $(".route-name").first().removeClass('warning-show');
        }
    });

    $("#desc").keyup(function() {
        if($(this).val().length >= 1000) {
            $(".route-desc").first().addClass('warning-show');
        }
        else {
            $(".route-desc").first().removeClass('warning-show');
        }
    });

    /***/


    $(".dismiss-submit").click(function() {
        dismissSubmitPane();
    });

    $(".dismiss-search").click(function() {
        dismissSearchPane();
    });

    $(".btn-search").click(function() {
        if ($(".body-container").hasClass("submit-active")) {
            return;
        }
        $(".body-container").addClass("search");
        $(".side-search").fadeIn("fast");
        $("#pac-input").val('').select();
    });

    $("#search").click(function() {
        var address = $("#pac-input").val();
        searchAddress(address);
    });

    $(".btn-reset").click(function() {
        resetMap();
    });

    $(".btn-help").click(function() {
        showHelp();
    });

    $(".btn-dismiss-help").click(function() {
        hideHelp();
    });

});
