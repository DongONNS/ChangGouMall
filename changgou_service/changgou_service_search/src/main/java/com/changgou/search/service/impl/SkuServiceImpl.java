package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    // ElasticSearchTemplate : 可以实现索引库的增删改查[高级搜索]
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 多条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) throws Exception {
        // 构建查询条件
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);

        // 集合信息搜索
        HashMap<String, Object> resultMap = searchList(builder);

//        // 当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
//        // 分类 --> searchMap --> category
//        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))){
//            // 分类信息搜索
//            List<String> categoryList = searchCategoryList(builder);
//            resultMap.put("categoryList",categoryList);
//        }
//
//        // 当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
//        // 品牌 --> searchMap --> brand
//        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))){
//            // 查询品牌集合
//            List<String> brandList = searchBrandList(builder);
//            resultMap.put("brandList",brandList);
//        }
//
//        // 查询规格参数集合
//        Map<String, Set<String>> specList = searchSpecList(builder);
//        resultMap.put("specList",specList);

        // 分组搜索实现 包括category brand以及spec数据
        Map<String, Object> groupMap = searchGroupList(builder, searchMap);
        resultMap.putAll(groupMap);

        return resultMap;
    }

    // 查询条件构建
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        // NativeSearchQueryBuilder : 搜索条件构建对象，用于封装各种搜索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        // must、must_not、should
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size() > 0){
            // 根据关键词搜索
            String keywords = searchMap.get("keywords");
            // 如果关键词不为空，则搜索关键词数据
            if (!StringUtils.isEmpty(keywords)){
                // builder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            // 输入了分类 --> category(将其他分类过滤掉，起筛选该分类的数据)
            String category = searchMap.get("category");
            if (!StringUtils.isEmpty(category)){
                // name为域的名称,value 为域相应的值
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",category));
            }

            // 输入了品牌 --> brand(将其他品牌过滤掉，只筛选该品牌的数据)
            String brand = searchMap.get("brand");
            if (!StringUtils.isEmpty(brand)){
                // name为域的名称,value 为域相应的值
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",brand));
            }

            // 规格过滤实现:在数据前面加上spec_,也可以是其他前缀
            // eg spec_网络 = 联通3G&&spec_颜色=红
            for(Map.Entry<String,String> entry : searchMap.entrySet()){
                String key = entry.getKey();
                // 如果key以spec_开始,则表示规格筛选查询
                if (key.startsWith("spec_")){
                    // 规格条件的值
                    String value = entry.getValue();
                    // eg spec_网络,spec_前5个值要去掉
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword",value));
                }
            }

            // price 0-500元 500-1000元 1000-1500元 1500-2000元 2000-3000元 3000元以上
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)){
                // 1.去掉中文元和以上
                price = price.replace("元", "").replace("以上", "");

                // 2.根据 - 分割 [0,500] [500,1000] ...... [3000]
                String[] prices = price.split("-");

                // x一定不为空,y有可能为null
                if (prices != null && prices.length >= 1){
                    // price >= prices[0]
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));

                    if (prices.length == 2){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

            //排序的实现
            String sortField = searchMap.get("sortField");
            String sortRule  = searchMap.get("sortRule");

            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
                builder.withSort(
                            new FieldSortBuilder(sortField)         //指定排序的域
                            .order(SortOrder.valueOf(sortRule)));   //指定排序的规则
            }
        }

        // 分页 如果用户不不传分页参数，则默认第一页
        Integer pageNum = covertPage(searchMap); // 默认第一页
        Integer size = 20;    // 默认查询的数据条数
        builder.withPageable((PageRequest.of(pageNum - 1,size)));

        //将BoolQueryBuilder的实例填充给NativeSearchQueryBuilder
        builder.withQuery(boolQueryBuilder);
        return builder;
    }

    /**
     * 接收前端传入的分页参数
     * @param searchMap
     * @return
     */
    public Integer covertPage(Map<String,String> searchMap){
        // 如果searchMap不为空，获取pageNum的值
        if (searchMap != null){
            String pageNum = searchMap.get("pageNum");
            try{
                return Integer.parseInt(pageNum);
            }catch (NumberFormatException e){
            }
        }
        return 1;
    }

    /**
     * 执行搜索返回响应结果
     * 1) 搜索条件封装对象
     * 2) 搜索的结果集（集合数据）需要转换的类型
     * 3) AggregatedPage<SkuInfo> : 搜索结果集的封装
     */
    private HashMap<String, Object> searchList(NativeSearchQueryBuilder builder) {

        // 高亮配置
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");    // 指定高亮域
        // 前缀   <em style="color:red">
        field.preTags("<em style=\"color:red\">");
        // 后缀   </em>
        field.postTags("</em>");
        // 碎片长度 也就是当我们的name(指定高亮的区域)过长时，我们进行高亮展示的区域
        field.fragmentSize(100);
        // 添加高亮
        builder.withHighlightFields(field);

//        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(),  //搜索条件封装
                SkuInfo.class,      // 数据集合要转换的类型的字节码
                // 执行搜索后，将数据结果集封装到该对象中
                new SearchResultMapper() {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                        List<T> list = new ArrayList<T>();

                        // 执行查询，获取所有数据 -->结果集[非高亮数据 | 高亮数据]
                        SearchHits searchHits = searchResponse.getHits();
                        for (SearchHit hit : searchHits){
                            // 分析结果集数据，获取[非高亮]数据
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

                            // 分析结果集数据，获取高亮数据 --> 只有某个域的高亮数据
                            HighlightField highlightField = hit.getHighlightFields().get("name");

                            if (highlightField != null && highlightField.getFragments() != null){
                                Text[] fragments = highlightField.getFragments();

                                StringBuffer buffer = new StringBuffer();
                                for (Text fragment : fragments){
                                    buffer.append(fragment);
                                }
                                // 非高亮数据中指定的域替换成高亮数据
                                skuInfo.setName(buffer.toString());
                            }
                            // 将高亮数据添加到集合中
                            list.add((T)skuInfo);

                        }
                        // 将数据返回
                        /**
                         * 1) 搜索的集合数据：(携带高亮)List<T> content
                         * 2) 分页对象信息 ： Pageable pageable
                         * 3) 搜索记录的总条数 ： long total
                         */
                        return new AggregatedPageImpl<T>(list,pageable,searchResponse.getHits().getTotalHits());
                    }
                });



        // 分页参数--总记录数
        long totalElements = page.getTotalElements();

        // 总的页数
        int totalPages = page.getTotalPages();

        // 获取数据结果集
        List<SkuInfo> contents = page.getContent();

        // 封装一个map存储所有数据
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows",contents);

        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);

        // 获取搜索的封装信息
        NativeSearchQuery query = builder.build();
        Pageable pageable = query.getPageable();
        int pageSize = pageable.getPageSize();        // 页面大小
        int pageNumber = pageable.getPageNumber() + 1;    // 当前页码

        resultMap.put("pageSize",pageSize);
        resultMap.put("pageNumber",pageNumber);

        return resultMap;
    }

    /**
     * 实现分组查询规格的集合
     * @param builder
     * @return
     */
    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder builder) {
        /**
         * .addAggregation -->添加一个聚合操作
         * 1) .terms() : 取别名
         * 2) .field() : 表示根据哪个域进行分组查询
         * 3) .size()  : 表示查询的数据量
         * @return
         */
        // 在原来根据关键词查询的基础上进行分组查询,所以用的是原来的builder
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(1000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(),SkuInfo.class);

        /**
         * 获取分组数据
         * aggregatedPage.getAggregations() 获取的是集合，可以根据多个域进行分组
         * .get("skuSpec") : 获取指定域的集合 [{"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"},
         *                                  {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"170"}]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String specName = bucket.getKeyAsString();
            specList.add(specName);
        }
        Map<String, Set<String>> allSpec = putAllSpec(specList);

        return allSpec;
    }

    /**
     * 将specList 封装到一个Map<String, Set<String>>中
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        // 合并后的spec
        Map<String, Set<String>> allSpec = new HashMap<>();

        // 1.循环specList
        for(String spec : specList){
            // 2.将每个json字符串转成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);



            // 3.将每个Map对象合并成一个Map<String,Set<String>>
            // 4.合并流程,循环所有Map
            for (Map.Entry<String,String> entry : specMap.entrySet()) {
                // 4.2取出当前Map,并且获取对应的Key以及对应的value
                String key = entry.getKey();
                String value = entry.getValue();

                // 4.3将当前循环的数据合并到一个Map<String,Set<String>>中
                // 从allSpec中获取当前规格对应的Set集合

                Set<String> specSet = allSpec.get(key);
                if (specSet == null) {
                    // 如果之前的allSpec中没有该规格数据，那么创建新的specSet
                    specSet = new HashSet<>();
                }
                specSet.add(value);

                allSpec.put(key, specSet);
            }
        }
        return allSpec;
    }

    /**
     * 实现分组查询品牌的集合
     * .addAggregation -->添加一个聚合操作
     * 1) 取别名
     * 2) 表示根据哪个域进行分组查询
     */
    private List<String> searchBrandList(NativeSearchQueryBuilder builder) {

        // 在原来根据关键词查询的基础上进行分组查询,所以用的是原来的builder
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(),SkuInfo.class);

        /**
         * 获取分组数据
         * aggregatedPage.getAggregations() 获取的是集合，可以根据多个域进行分组
         * .get("skuBrand") : 获取指定域的集合数 [华为、中兴、小米]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuBrand");
        List<String> brandList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String brandName = bucket.getKeyAsString();
            brandList.add(brandName);
        }
        return brandList;
    }

    /**
     * 实现分组查询分类的集合
     * .addAggregation -->添加一个聚合操作
     * 1) 取别名
     * 2) 表示根据哪个域进行分组查询
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder builder) {

        // 在原来根据关键词查询的基础上进行分组查询,所以用的是原来的builder
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(),SkuInfo.class);

        /**
         * 获取分组数据
         * aggregatedPage.getAggregations() 获取的是集合，可以根据多个域进行分组
         * .get("skuCategory") : 获取指定域的集合数 [手机、家用电器、手机配件]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String categoryName = bucket.getKeyAsString();
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /**
     * 实现分组查询->分类分组、品牌分组、规格分组
     * .addAggregation -->添加一个聚合操作
     * 1) 取别名
     * 2) 表示根据哪个域进行分组查询
     */
    private Map<String,Object> searchGroupList(NativeSearchQueryBuilder builder,Map<String,String> searchMap) {

        // 在原来根据关键词查询的基础上进行分组查询,所以用的是原来的builder
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))){
            builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))){
            builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }

        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));

        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(),SkuInfo.class);

        /**
         * 获取分组数据
         * aggregatedPage.getAggregations() 获取的是集合，可以根据多个域进行分组
         * .get("skuCategory") : 获取指定域的集合数 [手机、家用电器、手机配件]
         */
        // 定义一个Map,存储所有分组结果
        Map<String,Object> groupMapResult = new HashMap<>();

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))){
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            // 获取分类分组集合数据
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList",categoryList);
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))){
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            // 获取品牌分组集合数据
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList",brandList);
        }

        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");

        // 获取规格分组集合数据
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specList",specMap);

        return groupMapResult;
    }

    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms){
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String fieldName = bucket.getKeyAsString();
            groupList.add(fieldName);
        }
         return groupList;
    }


    /**
     * 导入数据到es索引库
     */
    @Override
    public void importData() {
        // 1.feign调用查询List<SkuInfo>
        Result<List<Sku>> skuResult = skuFeign.findAll();

        // 2.将List<Sku>转成List<SkuInfo>
        /**
         * 将list<Sku> 转成List<SkuInfo>
         * List<Sku> -> [{skuJSON}] -> List<SkuInfo>
         */
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);

        // 循环当前SkuInfoList
        for(SkuInfo skuInfo : skuInfoList){
            // 获取spec --> Map(String) --> Map类型
            // {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            // 如果需要生成动态的域，只需要将该域存入到一个Map<String,Object>对象中即可，该Map<String,Object>的key会生成一个域，域的名字为该Map的key
            // 当前Map<String,Object>后面的Object的值会作为当前Sku对象该域（key）对应的值
            skuInfo.setSpecMap(specMap);
        }

        // 3.调用Dao实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);
    }
}

//        JDBC搜索的执行过程
//        // 查询条件
//        // 执行sql = select * from table ======> ResultSet
//        // 循环ResultSet --> JavaBean --> List<JavaBean>
//
//        // 搜索条件封装
//        String sql = "select * from tb_sku where name like '%华为%'";
//
//        // 预编译sql语句，执行sql
//        Connection connection = null;
//        PreparedStatement pst = null;
//        ResultSet resultSet = null;
//
//        try {
//            pst = connection.prepareStatement(sql);
//            resultSet = pst.executeQuery();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        // 数据解析
//        List<SkuInfo> skuInfos = new ArrayList<>();
//        while (resultSet.next()){
//            SkuInfo skuInfo = new SkuInfo();
//            skuInfo.setName(resultSet.getString("name"));
//            skuInfo.setCategoryName(resultSet.getString("categoryName"));
//            skuInfos.add(skuInfo);
//        }