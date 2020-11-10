var $;
var form;
var layer;
layui.use(['form', 'layer'], function () {
    form = layui.form,
        layer = parent.layer === undefined ? layui.layer : parent.layer,
        $ = layui.jquery;
    //注册账号
    form.on("submit(register)", function (data) {
        $.ajax({
            type: "post",
            url: "/crawler/register",
            data: data.field,
            dataType: "json",
            success: function (result) {
                if (result.code == 0) {// 注册成功
                    layer.confirm("注册成功,立即登录", function () {
                        showLogin();
                    });
                } else {
                    layer.msg(result.msg, {icon: 5, time: 1000});
                }
            }
        });
        return false;
    });

    //qq登录
    $(document).on("click", ".quick-login i.qqLogin", function () {
        // layer.msg("功能正在开发中，即将开放哦!",{time: 1500});
        // var access_page =/*[[#{qqLogin.redirectUrl}]]*/"";
        var access_page =/*[[#{login.accessPage}]]*/"";
        $.post(
            '/crawler/qq/oauth',
            {'state': access_page},
            function (result) {
                if (result.code == 0) {
                    //成功,则跳转到微信二维码扫一扫页面
                    window.location.href = result.data.oauthUrl;
                } else {
                    layer.msg(result.msg,
                        {
                            time: 1000,
                            icon: 5
                        });
                }
            }
        );
    });

    // 微信登录
    $(document).on("click", ".quick-login i.wxLogin", function () {
        //TODO:这里注意部署的时候修改一下 url,扫码成功后跳转的页面
        // var access_page = "http://xiaomo.mynatapp.cc/crawler/manage/center";
        var access_page =/*[[#{login.accessPage}]]*/"";
        $.ajax({
            type: "post",
            url: "/crawler/weChat/login_url",
            data: {
                "access_page": access_page
            },
            dataType: "json",
            success: function (result) {
                if (result.code == 0) {//登录成功
                    //成功,则跳转到微信二维码扫一扫页面
                    window.location.href = result.data.qrCodeUrl;
                } else {
                    layer.msg(result.msg,
                        {
                            time: 1000,
                            icon: 5
                        });
                }
            }
        });
    });
});

//跳转登录页面
function showLogin() {
    window.location.href = /*[[@{/}]]*/"/crawler";
}