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
        elem: "#hotel-info",
        url: '/crawler/manage/show_hotel_info',
        page: true,
        height: "full-190",
        method: 'POST',
        id:"hotel-info",
        contentType:"application/json;charset=utf-8",
        cols: [[
            {field: 'city', title: '城市', align: 'center'},
            {field: 'type', title: '类型', align: 'center'},
            {field: 'brand', title: '品牌', align: 'center'},
            {field: 'hotelName', title: '酒店名', align: 'center'},
            {field: 'url', title: '链接', align: 'center'},
            {field: 'address', title: '地址', align: 'center'},
            {field: 'star', title: '星级', align: 'center'},
            {field: 'price', title: '价格', align: 'center'},
            {field: 'createTime', title: '创建时间', align: 'center',templet:'#dateFormat'}
        ]]
    });

    $('#qry-hotel-info').click(function () {
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
        table.reload("hotel-info",{
            method : 'post',
            where:{
                city:$('#city').val(),
                type:$('#type').val(),
                brand:$('#brand').val(),
                hotelName:$('#hotelName').val()
            },
            page : {
                curr : 1
            }
        });
        return false;
    });

});

