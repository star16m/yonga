$(document).ready(function() {
	// loader setup
    $(document).ajaxSend(function(event, jqXHR, settings) {
    	$("div.loader").fadeIn();
    });

    $(document).ajaxComplete(function(event, jqXHR, settings) {
    	$("div.loader").fadeOut();
    });
    // random icon setup
    var iconArray = new Array("glass", "music", "search", "envelope-o", "heart", "star", "star-o", "user", "film", "th-large", "th", "th-list", "check", "remove", "close", "times", "search-plus", "search-minus", "power-off", "signal", "gear", "cog", "trash-o", "home", "file-o", "clock-o", "road", "download", "arrow-circle-o-down", "arrow-circle-o-up", "inbox", "play-circle-o", "rotate-right", "repeat", "refresh", "list-alt", "lock", "flag", "headphones", "volume-off", "volume-down", "volume-up", "qrcode", "barcode", "tag", "tags", "book", "bookmark", "print", "camera", "font", "bold", "italic", "text-height", "text-width", "align-left", "align-center", "align-right", "align-justify", "list", "dedent", "outdent", "indent", "video-camera", "photo", "image", "picture-o", "pencil", "map-marker", "adjust", "tint", "edit", "pencil-square-o", "share-square-o", "check-square-o", "arrows", "step-backward", "fast-backward", "backward", "play", "pause", "stop", "forward", "fast-forward", "step-forward", "eject", "chevron-left", "chevron-right", "plus-circle", "minus-circle", "times-circle", "check-circle", "question-circle", "info-circle", "crosshairs", "times-circle-o", "check-circle-o", "ban", "arrow-left", "arrow-right", "arrow-up", "arrow-down", "mail-forward", "share", "expand", "compress", "plus", "minus", "asterisk", "exclamation-circle", "gift", "leaf", "fire", "eye", "eye-slash", "warning", "exclamation-triangle", "plane", "calendar", "random", "comment", "magnet", "chevron-up", "chevron-down", "retweet", "shopping-cart", "folder", "folder-open", "arrows-v", "arrows-h", "bar-chart-o", "bar-chart", "twitter-square", "facebook-square", "camera-retro", "key", "gears", "cogs", "comments", "thumbs-o-up", "thumbs-o-down", "star-half", "heart-o", "sign-out", "linkedin-square", "thumb-tack", "external-link", "sign-in", "trophy", "github-square", "upload", "lemon-o", "phone", "square-o", "bookmark-o", "phone-square", "twitter", "facebook-f", "facebook", "github", "unlock", "credit-card", "rss", "hdd-o", "bullhorn", "bell", "certificate", "hand-o-right", "hand-o-left", "hand-o-up", "hand-o-down", "arrow-circle-left", "arrow-circle-right", "arrow-circle-up", "arrow-circle-down", "globe", "wrench", "tasks", "filter", "briefcase", "arrows-alt", "group", "users", "chain", "link", "cloud", "flask", "cut", "scissors", "copy", "files-o", "paperclip", "save", "floppy-o", "square", "navicon", "reorder", "bars", "list-ul", "list-ol", "strikethrough", "underline", "table", "magic", "truck", "pinterest", "pinterest-square", "google-plus-square", "google-plus", "money", "caret-down", "caret-up", "caret-left", "caret-right", "columns", "unsorted", "sort", "sort-down", "sort-desc", "sort-up", "sort-asc", "envelope", "linkedin", "rotate-left", "undo", "legal", "gavel", "dashboard", "tachometer", "comment-o", "comments-o", "flash", "bolt", "sitemap", "umbrella", "paste", "clipboard", "lightbulb-o", "exchange", "cloud-download", "cloud-upload", "user-md", "stethoscope", "suitcase", "bell-o", "coffee", "cutlery", "file-text-o", "building-o", "hospital-o", "ambulance", "medkit", "fighter-jet", "beer", "h-square", "plus-square", "angle-double-left", "angle-double-right", "angle-double-up", "angle-double-down", "angle-left", "angle-right", "angle-up", "angle-down", "desktop", "laptop", "tablet", "mobile-phone", "mobile", "circle-o", "quote-left", "quote-right", "spinner", "circle", "mail-reply", "reply", "github-alt", "folder-o", "folder-open-o", "smile-o", "frown-o", "meh-o", "gamepad", "keyboard-o", "flag-o", "flag-checkered", "terminal", "code", "mail-reply-all", "reply-all", "star-half-empty", "star-half-full", "star-half-o", "location-arrow", "crop", "code-fork", "unlink", "chain-broken", "question", "info", "exclamation", "superscript", "subscript", "eraser", "puzzle-piece", "microphone", "microphone-slash", "shield", "calendar-o", "fire-extinguisher", "rocket", "maxcdn", "chevron-circle-left", "chevron-circle-right", "chevron-circle-up", "chevron-circle-down", "html5", "css3", "anchor", "unlock-alt", "bullseye", "ellipsis-h", "ellipsis-v", "rss-square", "play-circle", "ticket", "minus-square", "minus-square-o", "level-up", "level-down", "check-square", "pencil-square", "external-link-square", "share-square", "compass", "toggle-down", "caret-square-o-down", "toggle-up", "caret-square-o-up", "toggle-right", "caret-square-o-right", "euro", "eur", "gbp", "dollar", "usd", "rupee", "inr", "cny", "rmb", "yen", "jpy", "ruble", "rouble", "rub", "won", "krw", "bitcoin", "btc", "file", "file-text", "sort-alpha-asc", "sort-alpha-desc", "sort-amount-asc", "sort-amount-desc", "sort-numeric-asc", "sort-numeric-desc", "thumbs-up", "thumbs-down", "youtube-square", "youtube", "xing", "xing-square", "youtube-play", "dropbox", "stack-overflow", "instagram", "flickr", "adn", "bitbucket", "bitbucket-square", "tumblr", "tumblr-square", "long-arrow-down", "long-arrow-up", "long-arrow-left", "long-arrow-right", "apple", "windows", "android", "linux", "dribbble", "skype", "foursquare", "trello", "female", "male", "gittip", "gratipay", "sun-o", "moon-o", "archive", "bug", "vk", "weibo", "renren", "pagelines", "stack-exchange", "arrow-circle-o-right", "arrow-circle-o-left", "toggle-left", "caret-square-o-left", "dot-circle-o", "wheelchair", "vimeo-square", "turkish-lira", "try", "plus-square-o", "space-shuttle", "slack", "envelope-square", "wordpress", "openid", "institution", "bank", "university", "mortar-board", "graduation-cap", "yahoo", "google", "reddit", "reddit-square", "stumbleupon-circle", "stumbleupon", "delicious", "digg", "pied-piper", "pied-piper-alt", "drupal", "joomla", "language", "fax", "building", "child", "paw", "spoon", "cube", "cubes", "behance", "behance-square", "steam", "steam-square", "recycle", "automobile", "car", "cab", "taxi", "tree", "spotify", "deviantart", "soundcloud", "database", "file-pdf-o", "file-word-o", "file-excel-o", "file-powerpoint-o", "file-photo-o", "file-picture-o", "file-image-o", "file-zip-o", "file-archive-o", "file-sound-o", "file-audio-o", "file-movie-o", "file-video-o", "file-code-o", "vine", "codepen", "jsfiddle", "life-bouy", "life-buoy", "life-saver", "support", "life-ring", "circle-o-notch", "ra", "rebel", "ge", "empire", "git-square", "git", "hacker-news", "tencent-weibo", "qq", "wechat", "weixin", "send", "paper-plane", "send-o", "paper-plane-o", "history", "genderless", "circle-thin", "header", "paragraph", "sliders", "share-alt", "share-alt-square", "bomb", "soccer-ball-o", "futbol-o", "tty", "binoculars", "plug", "slideshare", "twitch", "yelp", "newspaper-o", "wifi", "calculator", "paypal", "google-wallet", "cc-visa", "cc-mastercard", "cc-discover", "cc-amex", "cc-paypal", "cc-stripe", "bell-slash", "bell-slash-o", "trash", "copyright", "at", "eyedropper", "paint-brush", "birthday-cake", "area-chart", "pie-chart", "line-chart", "lastfm", "lastfm-square", "toggle-off", "toggle-on", "bicycle", "bus", "ioxhost", "angellist", "cc", "shekel", "sheqel", "ils", "meanpath", "buysellads", "connectdevelop", "dashcube", "forumbee", "leanpub", "sellsy", "shirtsinbulk", "simplybuilt", "skyatlas", "cart-plus", "cart-arrow-down", "diamond", "ship", "user-secret", "motorcycle", "street-view", "heartbeat", "venus", "mars", "mercury", "transgender", "transgender-alt", "venus-double", "mars-double", "venus-mars", "mars-stroke", "mars-stroke-v", "mars-stroke-h", "neuter", "facebook-official", "pinterest-p", "whatsapp", "server", "user-plus", "user-times", "hotel", "bed", "viacoin", "train", "subway", "medium");
    $(".fa-target").each(function() {$(this).addClass("fa-" + iconArray[Math.floor(Math.random()*(iconArray.length))])});
    
    // disabled a tag
    $("a.disabled").bind('click', false);
    // login button
    $("a.login-btn").on("click", function(e) {
        $("form.login-form").submit();
    });
    // userId 에서 enter key 가 눌려지면 다음 password 로 이동
    $('#userId').keypress(function(event) {
        if (event.keyCode == 13 || event.which == 13) {
            $('#password').focus();
            event.preventDefault();
        }
    });
    // password 에서 enter key 가 눌려지면 로그인
    $('#password').keypress(function(event) {
        if (event.keyCode == 13 || event.which == 13) {
            $("form.login-form").submit();
        }
    });
    // enable selectpicker
    $('#categorySelect,#makerSelect,#brandSelect,#keijoSelect').selectpicker({
    	width: '100%'
    });
    
    // set image gallery
    $('div.product-image').magnificPopup({
    	delegate: 'a',
    	type: 'image',
    	gallery: {
    		enabled:true
    	}
    });
    // focus input
    $('div.form-group div input.form-control:enabled').first().focus().select();
    // select event
    $('#categorySelect').on('changed.bs.select', function(e) {
    	var data = {};
    	data["viewProductImage"] = $('input#checkViewProductImage').is(":checked");
    	var categoryId = $(this).val();
    	$.ajax({
            url: "/selects/options",
            type: "patch",
            contentType: "application/json",
            cache: false,
            data: JSON.stringify(data),
            //dataType: 'json',
            success: function (data,status,xhr) {
            	if (data != "success") {
            		alert(data);
            	} else {
            		window.location.href = "/product/" + categoryId;
            	}
            }
    	});
    });
    $('#makerSelect,#brandSelect,#keijoSelect').on('changed.bs.select', function(e) {
    	$(this).data('targetchanged', true);
    });
    function changeOption() {
    	var categoryId = $('#makerSelect').data('currentCategory');
    	var data = {};
    	data["selectsMaker"] = $('#makerSelect').val();
    	data["selectsKeijo"] = $('#keijoSelect').val();
    	data["selectsBrand"] = $('#brandSelect').val();
    	data["viewProductImage"] = $('input#checkViewProductImage').is(":checked");
    	$.ajax({
            url: "/selects/options",
            type: "patch",
            contentType: "application/json",
            cache: false,
            data: JSON.stringify(data),
            //dataType: 'json',
            success: function (data,status,xhr) {
            	if (data != "success") {
            		alert(data);
            	} else {
            		window.location.href = "/product/" + categoryId;
            	}
            }
    	});
    }
    $('#makerSelect,#keijoSelect,#brandSelect').on('hide.bs.select', function (e) {
    	if (!$(this).data('targetchanged')) {
    		return;
    	}
    	changeOption();
	});
	$('input#checkViewProductImage').change(function(){
	    changeOption();
	});
});
function initialCategory(categoryId, status) {
	var message = '';
	var data = {};
	if (categoryId == null) {
		if ($("#extractCategorySelect").val() == null || $("#extractCategorySelect").val() == "") {
			alert("초기화할 카테고리를 선택해 주세요.");
			return;
		}
		if (!confirm("선택한 [" + $("#extractCategorySelect").val().length + "] 개 카테고리를 초기화 하시겠습니까?")) {
			return false;
		}
		data["categoryIdList"] = $("#extractCategorySelect").val();
		data["extractMode"] = $("input:checkbox[id='initialize']").is(":checked") ? "INITIALIZE" : "EXTRACT"
	    return $.ajax({
	        url: "/category/init",
	        type: "patch",
	        contentType: "application/json",
	        cache: false,
	        data: JSON.stringify(data),
	        //dataType: 'json',
	        success: function (data,status,xhr) {
	        	if (data != "success") {
	        		alert(data);
	        	} else {
	        		location.reload();
	        	}
	        }
	    });
	}
}
function setConfig() {
	var message = '';
	var data = {};
    if (!$("#inputTitle").val() || !$("#inputWelcome").val() || !$("#inputAdminEmail").val() || !$("#inputMailHost").val() || !$("#inputMailPort").val() || !$("#inputMailId").val() || !$("#inputMailPassword").val()) {
        alert("설정값을 입력해 주세요.");
        return;
    }
    data["title"] = $("#inputTitle").val();
    data["welcome"] = $("#inputWelcome").val();
    data["adminEmail"] = $("#inputAdminEmail").val();
    data["mailHost"] = $("#inputMailHost").val();
    data["mailPort"] = $("#inputMailPort").val();
    data["mailId"] = $("#inputMailId").val();
    data["mailPassword"] = $("#inputMailPassword").val();

    return $.ajax({
        url: "/config",
        type: "post",
        contentType: "application/json",
        cache: false,
        data: JSON.stringify(data),
        //dataType: 'json',
        success: function (data,status,xhr) {
            alert('저장되었습니다.');
            location.reload();
        },
        error : function(error) {
            console.log(error.responseText);
            alert('저장에 실패하였습니다. [' + error.responseText + ']');

        }
    });
}
function modifyCustomer() {
    // clear error
    $('.custom-error').remove();
    var isAdmin = $("#is-admin-role").text() == 'true';
    var isModify = $("#ismodify").val() == 'true';
    return $.ajax({
        url: isModify ? "/customer/profile" : "/customer/join",
        type: isModify ? "put" : "post",
        contentType: "application/json",
        cache: false,
        data: JSON.stringify({
            userId: $("#userId").val(),
            name: $("#name").val(),
            password: $("#password").val(),
            tel: $("#tel").val(),
            email: $("#email").val(),
            description: $("#description").val()
        }),
        //dataType: 'json',
        success: function (data,status,xhr) {
            alert(isAdmin || isModify ? '저장되었습니다.' : '저장되었습니다.\n사용자 승인이 완료되기 까지는 로그인이 제한됩니다.');
            window.location.href = isAdmin ? "/customer" : "/";
        },
        error : function(data,status,xhr) {
            var result = eval("(" + data.responseText + ")");
            if (result.error && result.error == 'Bad Request') {
                $.each(result.errors,function(key,value){
                    $('input#'+result.errors[key].field).after('<span class="custom-error">'+result.errors[key].defaultMessage+'<br/></span>');
                });
            } else if (result.result) {
                alert(result.result);
            }
        }
    });
}
function enableCustomer(customerId, enabled) {
    return $.ajax({
        url: "/customer/enabled",
        type: "patch",
        contentType: "application/json",
        cache: false,
        data: JSON.stringify({
            customerId: customerId,
            enabled: !enabled
        }),
        //dataType: 'json',
        success: function (data,status,xhr) {
            alert("저장되었습니다.");
            window.location.href = "/customer";
        },
        error : function(data,status,xhr) {
            alert(data.responseJSON.result);
        }
    });
}
function deleteCustomer(customerId) {
    if (confirm('유저[' + customerId + '] 를 삭제 하시겠습니까?')) {
        return $.ajax({
                url: "/customer/" + customerId,
                type: "delete",
                contentType: "application/json",
                cache: false,
                success: function (data,status,xhr) {
                    window.location.href = "/customer";
                },
                error : function(data,status,xhr) {
                    alert(data.responseJSON.result);
                }
            });
    } else {
        alert('취소');
    }
}