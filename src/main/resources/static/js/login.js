var $;
var layer;
var form;
//是否自动登录
var autoLogin = localStorage.getItem("autoLogin") || "false";
layui.use(['form', 'layer'], function () {
    $ = layui.jquery;
    layer = layui.layer;
    form = layui.form;
    //如果是自动登录
    if (autoLogin === "true") {
        var name = localStorage.getItem("name") || "";
        var password = localStorage.getItem("password") || "";
        $("#name").val(name);
        $("#password").val(password);
        $("#rememberMe").attr("checked", "checked");
    }
});
//自动登录
// var $;
layui.use(['form', 'layer'], function () {
    form = layui.form;
    $ = layui.$;
    form.on("checkbox(autoLogin)", function (data) {
        var autoLogin = localStorage.getItem("autoLogin") || "false";
        if (autoLogin === "false") {
            localStorage.setItem("autoLogin", "true");
            localStorage.setItem("name", $("#name").val());
            localStorage.setItem("password", $("#password").val());
        } else {
            localStorage.setItem("autoLogin", "false");
            localStorage.setItem("name", "");
            localStorage.setItem("password", "");
        }
    });
});

layui.use(['form', 'layer'], function () {
    form = layui.form,
        layer = parent.layer === undefined ? layui.layer : parent.layer,
        $ = layui.jquery;
    //登录按钮事件
    form.on("submit(login)", function (data) {
        var autoLogin = localStorage.getItem("autoLogin") || "false";
        if (autoLogin === "true") {
            localStorage.setItem("name", $("#name").val());
            localStorage.setItem("password", $("#password").val());
        } else {
            localStorage.setItem("name", "");
            localStorage.setItem("password", "");
        }

        $.ajax({
            type: "post",
            url: "/crawler/login",
            data: {
                username: data.field.name,
                password: data.field.password,
                code: data.field.code
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 0) {//登录成功
                    var manage = '/crawler/manage/center';
                    layer.msg(result.msg,
                        {
                            time: 1200,
                            icon: 1
                        }, function () {
                            parent.location.href = manage + '?token=' + result.data.token;
                        });
                } else {
                    layer.msg(result.msg,
                        {
                            time: 1000,
                            icon: 5
                        });
                }
            }
        });
        return false;
    });
});

/*点击看不清，刷新验证码*/
function reloadCodeImage() {
    var timestamp = new Date().valueOf();
    var url = /*[[@{/code/getCodeImage}]]*/"/crawler/code/getCodeImage";
    $('#randCode').attr('src', url + '?time=' + timestamp);
    $('#code').val('');
}

//转到注册页面
function register() {
    var url = /*[[@{/register}]]*/"/crawler/register";
    window.location.href = url;
}
