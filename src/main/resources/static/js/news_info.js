//JavaScript代码区域
var $;
var layer;
var table;
var element;
var laydate;
layui.use(["layer", "element", "table", "form","laydate"], function () {
    //$即是jquery,layui自动内嵌了jquery，layui.$相当于获取了jquery对象
    $ = layui.jquery;
    table = layui.table;
    layer = layui.layer;
    var form = layui.form;
    element = layui.element;
    laydate = layui.laydate;
    //初始化表格
    var tableIns = table.render({
        elem: "#news-info",
        url: '/crawler/manage/news_info/show_news_info_es',
        page: true,
        height: "full-50",
        method: 'POST',
        id:"news-info",
        contentType:"application/json;charset=utf-8",
        cols: [[
            {field: 'title', title: '标题', align: 'center'},
            {field: 'content', title: '内容', align: 'center'},
            {field: 'url', title: 'url', align: 'center',templet:proofPicture},
            {field: 'time', title: '发布时间', align: 'center',templet:'#dateFormat'},
            {field: 'source', title: '来源', align: 'center'},
            {field: 'keyword', title: '关键词', align: 'center'},
        ]]
    });

    $('#qry_news_info').click(function () {
        table.reload("news-info",{
            method : 'post',
            where:{
                keyword:$('#keyword').val(),
                title:$('#title').val(),
                source:$('#source').val(),
                createTimeStart:$('#startDate').val(),
                createTimeEnd:$('#endDate').val()
            },
            page : {
                curr : 1
            }
        });
        return false;
    });

    function proofPicture(d) {
        var link = d.url;
        if ('' == link || null == link || undefined == link) {
            return '';
        }
        if (link.length > 0) {
            return '<a class="layui-table-link" href='+'"'+link + '"'+' lay-event="link">'+ link +'</a>'
        }

    }

    laydate.render({
        elem: '#startDate',
        type: 'datetime',
        format:'yyyy-MM-dd HH:mm:ss'
    });
    laydate.render({
        elem: '#endDate',
        type: 'datetime',
        format:'yyyy-MM-dd HH:mm:ss'
    });


    $('#export_news_info').click(function () {
        var keyword = $('#keyword').val();
        var title = $('#title').val();
        var source = $('#source').val();
        var createTimeStart = $('#startDate').val();
        var createTimeEnd = $('#endDate').val();
        // 模拟从后端接口读取需要导出的数据
        $.ajax({
            url: '/crawler/manage/news_info/export_news_info',
            type: "post",
            contentType: "application/json;charset=utf-8",
            data: "{\"keyword\":\"" + keyword + "\",\"title\":\"" + title + "\",\"source\":\"" + source + "\",\"createTimeStart\":\"" + createTimeStart +"\",\"createTimeEnd\":\"" + createTimeEnd +"\"}"
            ,dataType: 'json'
            ,success:function (res) {
                if (res.code == 0) {
                    var data = res.data;
                    // 重点！！！如果后端给的数据顺序和映射关系不对，请执行梳理函数后导出
                    data = LAY_EXCEL.filterExportData(data, [
                        'title'
                        , 'content'
                        , 'url'
                        , 'time'
                        , 'source'
                        , 'keyword'
                    ]);
                    // 重点2！！！一般都需要加一个表头，表头的键名顺序需要与最终导出的数据一致
                    data.unshift({title: "标题", content: "内容", url: 'url', time: '时间', source: '来源', keyword: '关键词'});

                    var timestart = Date.now();
                    LAY_EXCEL.exportExcel(data, '导出数据.xlsx', 'xlsx');
                    var timeend = Date.now();

                    var spent = (timeend - timestart) / 1000;
                    layer.alert('导出耗时 ' + spent + ' s');
                }else{
                    layer.msg("导出失败", {icon: 5, time: 1000});
                }
            }
        });
    });

    // //监听头工具栏事件
    // table.on('tool(news-info)', function(obj) {
    //     debugger;
    //     var checkStatus = table.checkStatus(obj.config.id),
    //         data = checkStatus.data; //获取选中的数据
    //     switch (obj.event) {
    //         case 'table_export':
    //             exportFile('news-info')
    //             break;
    //     };
    // });
    //
    // /**
    //  * by yutons
    //  * Array.from() 非常方便的将一个类数组的集合 ==》 数组，直接使用数组身上的方法。例如：常用的map，foreach…
    //  * 但是，问题来了，IE不识别Array.from这个方法。所以写了它兼容IE的写法。
    //  */
    // if (!Array.from) {
    //     Array.from = function(el) {
    //         return Array.apply(this, el);
    //     }
    // }
    // //表格导出
    // function exportFile(id) {
    //     debugger;
    //     //根据传入tableID获取表头
    //     var headers = $("div[lay-id=" + id + "] .layui-table-box table").get(0);
    //     var htrs = Array.from(headers.querySelectorAll('tr'));
    //     var titles = {};
    //     for (var j = 0; j < htrs.length; j++) {
    //         var hths = Array.from(htrs[j].querySelectorAll("th"));
    //         for (var i = 0; i < hths.length; i++) {
    //             var clazz = hths[i].getAttributeNode('class').value;
    //             if (clazz != ' layui-table-col-special' && clazz != 'layui-hide') {
    //                 //排除居左、具有、隐藏字段
    //                 //修改:默认字段data-field+i,兼容部分数据表格中不存在data-field值的问题
    //                 titles['data-field' + i] = hths[i].innerText;
    //             }
    //         }
    //     }
    //     //根据传入tableID获取table内容
    //     var bodys = $("div[lay-id=" + id + "] .layui-table-box table").get(1);
    //     var btrs = Array.from(bodys.querySelectorAll("tr"))
    //     var bodysArr = new Array();
    //     for (var j = 0; j < btrs.length; j++) {
    //         var contents = {};
    //         var btds = Array.from(btrs[j].querySelectorAll("td"));
    //         for (var i = 0; i < btds.length; i++) {
    //             for (var key in titles) {
    //                 //修改:默认字段data-field+i,兼容部分数据表格中不存在data-field值的问题
    //                 var field = 'data-field' + i;
    //                 if (field === key) {
    //                     //根据表头字段获取table内容字段
    //                     contents[field] = btds[i].innerText;
    //                 }
    //             }
    //         }
    //         bodysArr.push(contents)
    //     }
    //     //将标题行置顶添加到数组
    //     bodysArr.unshift(titles);
    //     //导出excel
    //     excel.exportExcel({
    //         sheet1: bodysArr
    //     }, '新闻表' + new Date().toLocaleString() + '.xlsx', 'xlsx');
    // }

});

