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
        elem: "#keyword_table",
        url: '/crawler/manage/keyword/show_keyword',
        page: true,
        height: "full-50",
        method: 'POST',
        contentType:"application/json;charset=utf-8",
        id:"grdContent",
        cols: [[
            {type: 'checkbox', fixed: 'left'},
            {field: 'keyword', title: '关键词', align: 'center'},
            {field: 'source', title: '采集源', align: 'center'},
            {field: 'isDelete', title: '是否可用', align: 'center',templet: '#isDeleteEnum'},
            {field: 'createTime', title: '创建时间', align: 'center',templet:'#dateFormat'},
            {field:'right', title: '操作', width:250,toolbar:"#bar"}
        ]]
    });
    $('#startFactoryOnce').click(function () {
        layer.confirm('确定启动吗？', {title: '启动'}, function (index) {
            //向服务端发送删除指令og
            $.getJSON("/crawler/manage/keyword/start_factory_once", {}, function (ret) {
                layer.close(index);//关闭弹窗
                if(ret.code != 0){
                    layer.msg(ret.msg, {icon: 5, time: 1000});
                }else{
                    layer.msg("启动成功", function () {
                    });
                }
            });
            layer.close(index);
        });
    });

    $('#btnAddKeyword').click(function () {
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
                var keyword = $("#keyword").val();
                if (keyword !== '') {
                    var arr_box = [];
                    $('input[type=checkbox]:checked').each(function() {
                        arr_box.push($(this).val());
                    });
                    if(arr_box.length == 0){
                        layer.msg("请至少选择一个采集源", {icon: 5, time: 1000});
                    }else {
                        var source = "[";
                        for(i=0;i<arr_box.length;i++){
                            if(i == arr_box.length-1){
                                source += "\"" + arr_box[i] + "\"]"
                            }else {
                                source += "\"" + arr_box[i] + "\",";
                            }
                        }
                        console.log(source);
                        $.getJSON("/crawler/manage/keyword/add_keyword", {
                            keyword: keyword,
                            source:source
                        }, function (result) {
                            //根据后台返回的参数，来进行判断
                            if (result.code == 0) {
                                layer.closeAll();
                                layer.msg("添加成功", function () {
                                });
                            } else {
                                layer.msg("关键词添加失败！", {icon: 5, time: 1000});
                            }
                            //layui 表格重载
                            table.reload("grdContent", {
                                method: 'POST',
                                page:{
                                    curr: 1 //重新从第 1 页开始
                                }
                            });
                        });
                    }
                } else {
                    layer.msg("请输入关键词", {icon: 5, time: 1000});
                }
            }
        })
    });
    //获取按钮
    table.on('tool(keyword_table)', function(obj) {
        var data = obj.data; //获得当前行数据
        var tr = obj.tr//活动当前行tr 的  DOM对象
        if (obj.event === 'del') { //删除
            layer.confirm('确定删除吗？', {title: '删除'}, function (index) {
                //向服务端发送删除指令og
                $.getJSON("/crawler/manage/keyword/delete_keyword", {id: data.id}, function (ret) {
                    layer.close(index);//关闭弹窗
                    table.reload('grdContent', {//重载表格
                        page: {
                            curr: 1
                            // 重新从第 1 页开始
                        }
                    })
                });
                layer.close(index);
            });
        }
    });

});

