package com.changgou.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.entity.Result;
import com.changgou.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalDateEventListener {

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private PageFeign pageFeign;

    //字符串
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 监听插入数据
     * @InsertListenPoint 只有增加后的数据
     * rowData.getAfterColumnsList():增加 修改
     * rowData.getBeforeColumnsList():删除 修改
     * @param eventType 事件类型 增加数据
     * @param rowData   发生变更的一行数据
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By-Annotation: " + c.getName() + ":" + c.getValue()));
    }

    /**
     * 监听修改数据
     * @UpdateListenPoint 只有修改后的数据
     * rowData.getAfterColumnsList():增加 修改
     * rowData.getBeforeColumnsList():删除 修改
     * @param rowData   发生变更的一行数据
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.RowData rowData){
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("变更前的列名:" + column.getName() + "-----变更前的数据：" + column.getValue());
        }
        System.out.println("==============================");
        for (CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("变更后的列名:" + column.getName() + "-----变更后的数据:" + column.getValue());
        }
    }

    /**
     * 监听删除数据
     * @UpdateListenPoint 只有修改后的数据
     * rowData.getAfterColumnsList():增加 修改
     * rowData.getBeforeColumnsList():删除 修改
     * @param eventType 事件类型 修改数据
     * @param rowData   发生变更的一行数据
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("删除前的列名:" + column.getName()+ "----删除前的值:" + column.getValue());
        }
    }

    // 自定义数据库的 操作来监听
    // destination = "example"
    @ListenPoint(destination = "example",
            schema = "changgou_content",
            table = {"tb_content", "tb_content_category"},
            eventType = {
                    CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.DELETE,
                    CanalEntry.EventType.INSERT})
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //1.获取列名 为category_id的值
        String categoryId = getColumnValue(eventType, rowData);

        //2.调用feign 获取该分类下的所有的广告集合
        Result<List<Content>> categoryResult = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> data = categoryResult.getData();

        //3.使用redisTemplate存储到redis中
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(data));
        System.out.println("更新数据已缓存到redis");
    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        String categoryId = "";
        //判断 如果是删除  则获取beforlist
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        } else {
            //判断 如果是添加 或者是更新 获取afterlist
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }
    @ListenPoint(destination = "example",
            schema = "changgou_goods",
            table = {"tb_spu"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String spuId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();//spuid
                    break;
                }
            }
            //todo 删除静态页

        }else{
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String spuId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }
    }
}












//    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        String categoryId = "";
//
//        //判断 如果是删除  则获取beforeList
//        if (eventType == CanalEntry.EventType.DELETE) {
//            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
//                if (column.getName().equalsIgnoreCase("category_id")) {
//                    categoryId = column.getValue();
//                    return categoryId;
//                }
//            }
//        } else {
//            //判断 如果是添加 或者是更新 获取afterList
//            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
//                if (column.getName().equalsIgnoreCase("category_id")) {
//                    categoryId = column.getValue();
//                    return categoryId;
//                }
//            }
//        }
//        return categoryId;
//    }
//
