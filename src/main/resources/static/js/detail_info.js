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
    var form = layui.form;
    element = layui.element;
    //初始化表格
    var tableIns = table.render({
        elem: "#detail-info",
        url: '/crawler/manage/show_hotel_detail',
        page: true,
        height: "full-190",
        method: 'POST',
        id:"detail-info",
        contentType:"application/json;charset=utf-8",
        cols: [[
            {field: 'name', title: '酒店名', align: 'center'},
            {field: 'url', title: '链接', align: 'center'},
            {field: 'openTime', title: '开业时间', align: 'center'},
            {field: 'decorateTime', title: '装修时间', align: 'center'},
            {field: 'roomNum', title: '房间数', align: 'center'},
            {field: 'roomInfo', title: '客房信息', align: 'center'},
            {field: 'createTime', title: '创建时间', align: 'center',templet:'#dateFormat'}
        ]]
    });

    $('#qry-detail-info').click(function () {
        var search = {};
        debugger;
        $.each($(".layui-input-inline :input").serializeArray(),function (index,item) {
            (item.name) && (search[item.name] = (item.value) ? item.value : null);
            search[item.name] || (delete search[item.name]);
        });
        if (!search || $.isEmptyObject(search)) {
            layer.msg("请至少填写一个查询条件!", {time: 3000, icon: 7});
            return false;

        }
        search = JSON.stringify(search);
        table.reload("detail-info",{
            method : 'post',
            where:{
                hotelName:$('#hotelName').val()
            },
            page : {
                curr : 1
            }
        });
        return false;
    });

});

