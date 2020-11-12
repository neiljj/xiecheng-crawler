//JavaScript代码区域
var $;
var layer;
var table;
layui.use(["layer", "element", "table", "form","laydate"], function () {
    //$即是jquery,layui自动内嵌了jquery，layui.$相当于获取了jquery对象
    $ = layui.jquery;
    table = layui.table;
    layer = layui.layer;
    //初始化表格
    var tableIns = table.render({
        elem: "#crawler-task",
        url: '/crawler/manage/show_crawler_task',
        page: true,
        height: "full-190",
        method: 'POST',
        id:"grdContent",
        cols: [[
            {type: 'checkbox', fixed: 'left'},
            {field: 'city', title: '城市', align: 'center'},
            {field: 'type', title: '类型', align: 'center'},
            {field: 'brand', title: '品牌', align: 'center'},
            {field: 'status', title: '状态', align: 'center',templet: '#statusEnum'},
            {field: 'createTime', title: '创建时间', align: 'center',templet:'#dateFormat'}
        ]]
    });
    $('#btnAddTask').click(function () {
        debugger;
        var city = $('#city').val();
        if(city === ''){
            layer.msg("城市为必填项", {icon: 5, time: 1000});
        }else {
            var type = $('#type').val();
            var brand = $("#brand").val();
            $.ajax({
                type: "post",
                url: "/crawler/manage/add_crawler_task",
                contentType:"application/json;charset=utf-8",
                data: "{\"city\":\""+city+"\",\"type\":\""+type+"\",\"brand\":\""+brand+"\"}",
                dataType: "json",
                success: function (result) {
                    if (result.code == 0) {
                        layer.msg("任务添加成功", function () {

                        });
                    } else {
                        layer.msg("该任务已存在", {icon: 5, time: 1000});
                    }
                }
            });
            //layui 表格重载
            table.reload("grdContent", {
                method: 'POST',
                page:{
                    curr: 1 //重新从第 1 页开始
                }
            });
        }
    });
});

//基本信息
function customerInfo() {

}
