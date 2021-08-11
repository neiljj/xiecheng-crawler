//JavaScript代码区域
var $;
var layer;
var table;
var element;
layui.use(["layer", "element", "table", "form","laydate"], function () {
    //$即是jquery,layui自动内嵌了jquery，layui.$相当于获取了jquery对象
    $ = layui.jquery;
    table = layui.table;
    layer = layui.layer;
    element = layui.element;
    //初始化表格
    var tableIns = table.render({
        elem: "#cookie_table",
        url: '/crawler/manage/cookie/show_cookie',
        page: true,
        height: "full-50",
        method: 'POST',
        contentType:"application/json;charset=utf-8",
        id:"grdContent",
        cols: [[
            {type: 'checkbox', fixed: 'left'},
            {field: 'cookie', title: 'cookie', align: 'center'},
            {field: 'type', title: '类型', align: 'center',templet: '#cookieTypeEnum'},
            {field: 'updateTime', title: '更新时间', align: 'center',templet:'#dateFormat'},
            {field:'right', title: '操作', width:250,toolbar:"#bar"}
        ]]
    });
    //获取按钮
    table.on('tool(cookie_table)', function(obj) {
        debugger;
        var data = obj.data; //获得当前行数据
        var tr = obj.tr ;//活动当前行tr 的  DOM对象
        if (obj.event === 'update') { //删除
            layer.open({
                type:1,
                title:"添加关键词",
                area: ['50%','50%'],
                maxmin: true,
                content: $("#window"),
                btn: ['提交', '关闭'],
                id: 'add-keyword',
                moveType: 1 ,//拖拽模式，0或者1
                yes:function (layero) {
                    debugger;
                    var cookie = $("#cookie").val();
                    if (cookie !== '') {
                        $.getJSON("/crawler/manage/cookie/update_cookie", {
                            id:data.id,
                            cookie:cookie,
                            type:data.type
                        }, function (result) {
                            //根据后台返回的参数，来进行判断
                            if (result.code == 0) {
                                layer.closeAll();
                                layer.msg("更新成功", function () {
                                });
                            } else {
                                layer.msg("更新失败！", {icon: 5, time: 1000});
                            }
                            //layui 表格重载
                            table.reload("grdContent", {
                                method: 'POST',
                                page:{
                                    curr: 1 //重新从第 1 页开始
                                }
                            });
                        });
                    } else {
                        layer.msg("请输入cookie", {icon: 5, time: 1000});
                    }
                }
            })
        }
    });
});

