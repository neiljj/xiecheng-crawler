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
});

//跳转登录页面
function showLogin() {
    window.location.href = /*[[@{/}]]*/"/crawler";
}