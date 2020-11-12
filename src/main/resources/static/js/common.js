var c_init_api = "init";
var $;
var form;
layui.use(['form', 'layer'], function () {
    $ = layui.jquery;
    form = layui.form;
    $(function () {
        $.ajax({
            url: c_init_api,
            type: 'GET',
            cache: false,
            success: function (d) {
                (!d || (d.code && d.code !== 200)) && (layer.msg("请求数据异常!<br>" + d.msg && "", {time: 5000, icon: 2}));
                (!d.data) && (layer.msg("下拉框初始化失败,没有数据!", {time: 5000, icon: 7}));
                enumData = d.data;
                setEnumData(enumData);
            },
            error: function (e) {
                var error = e.statusText;
                (e.status && e.status === 0) && (error = '登陆可能超时，3s后跳转到登陆页面！');
                layer.msg("请求失败,网络异常!<br>" + error, {time: 10000, icon: 2});
                (e.status && e.status === 0) && (setTimeout("window.location.href = 'logout'", 3000));
            }
        });

        function setEnumData(data) {
            $(".data-select").each(function () {
                var enumName = $(this).context.name;
                if (enumName && data[enumName]) {
                    var html = '<option value="" selected>请选择</option>';
                    var dataVal = data[enumName];
                    for (key in dataVal) {
                        (key && key !== 'all')
                        && (html += ' <option value="' + key + '">' + key + '</option>');
                    }
                    $(this).html(html);
                    $(this).trigger("chosen:updated");
                    form.render();
                }
            });
        }
    });
});