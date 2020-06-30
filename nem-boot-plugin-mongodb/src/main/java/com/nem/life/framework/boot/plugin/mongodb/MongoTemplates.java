package com.nem.life.framework.boot.plugin.mongodb;

import com.mongodb.MongoException;
import com.nem.life.framework.boot.entity.PageParameter;
import com.nem.life.framework.boot.entity.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@ConditionalOnProperty(prefix = "spring.data.mongodb", value = "uri")
@AutoConfigureAfter(MongoTemplate.class)
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties
public class MongoTemplates {
    private final MongoPageHelper mongoPageHelper;
    private final MongoTemplate mongoTemplate;

    /**
     * 批量插入数据
     *
     * @param classList 数据list
     */
    public <T> void insertList(List<T> classList) {
        mongoTemplate.insertAll(classList);
    }

    /**
     * 查询一条数据
     */
    public <T> void insertOne(T t) {
        mongoTemplate.insert(t);
    }

    /**
     * 根据更新条件更新id集合中的数据
     *
     * @param update 更新条件
     * @param idList id集合
     * @param clazz  要更新的实体类
     */
    public void updateListByIdIn(Update update, List<String> idList, Class<?> clazz) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                getObjectName(clazz));
        for (String e : idList) {
            Query query = new Query(Criteria.where("_id").is(e));
            ops.updateOne(query, update);
        }
        ops.execute();
    }


    /**
     * 根据更新条件更新全部数据
     *
     * @param update 更新条件
     * @param query  更新范围限定
     * @param clazz  要更新的实体类
     */
    public void update(Update update, Query query, Class<?> clazz) {
        BulkOperations ops = mongoTemplate
                .bulkOps(BulkOperations.BulkMode.UNORDERED, getObjectName(clazz))
                .updateOne(query, update);
        ops.execute();
    }

    /**
     * 查询单个类
     *
     * @param query 查询条件
     * @param clazz 要查询的实体类
     */
    public <T> T selectOne(Query query, Class<T> clazz) {
        return mongoTemplate.findOne(query, clazz);
    }

    /**
     * 查询多个类
     *
     * @param query 查询条件
     * @param clazz 要查询的实体类
     */
    public <T> List<T> selectList(Query query, Class<T> clazz) {
        return mongoTemplate.find(query, clazz);
    }

    /***
     * 根据id集合查询
     * @param idList id集合
     * @param clazz 要查询的实体类
     */
    public <T> List<T> SelectByIdIn(List<String> idList, Class<T> clazz) {
        List<Object> resultList = new ArrayList<>();
        for (String e : idList) {
            Query query = new Query(Criteria.where("_id").is(e));
            resultList.add(mongoTemplate.findOne(query, clazz));
        }
        return (List<T>) resultList;
    }

    /**
     * 分页查询
     *
     * @param query         查询条件
     * @param entityClass   要返回的结果
     * @param pageParameter 分页参数
     * @return 分页数据
     */
    public <T> PageResult<T> pageQuery(Query query, Class<T> entityClass, PageParameter pageParameter) {
        return mongoPageHelper.pageQuery(query, entityClass, pageParameter, Function.identity(), null);
    }


    /**
     * 联表查询
     *
     * @param mainTable   主表
     * @param slaveTable  从表
     * @param mainMatch   主条件
     * @param slaveMatch  从条件
     * @param resultClass 返回结果
     */
    public <M, S, R> List<R> select(@NotNull Class<M> mainTable, @NotNull Class<S> slaveTable,
                                    AggregationOperation mainMatch,
                                    AggregationOperation slaveMatch,
                                    Class<R> resultClass
    ) {
        //一对多关系中存放子表
        String foreignFieldName = mongoChildName(mainTable);

        //表关联ID
        String mainTableId = getMongoIdName(mainTable);
        String slaveTableId = getMongoIdName(slaveTable);

        //表名字
        String mainTableName = getObjectName(mainTable);
        String slaveTableName = getObjectName(slaveTable);

        //复杂查询方式
        LookupOperation lookupOperation = LookupOperation
                .newLookup()
                .from(slaveTableName)
                .localField(mainTableId)
                .foreignField(slaveTableId).as(foreignFieldName);

        List<AggregationOperation> operations = new ArrayList<>();
        //先排序，否则会排序混乱
        try {
            operations.add(Aggregation.sort(Sort.by(Sort.Order.desc(getMongoIdName(mainTable)))));
        } catch (Exception iae) {
            operations.add(Aggregation.sort(Sort.by(Sort.Order.desc(getMongoIdName(mainTable)))));
        }

        List<AggregationOperation> counts = new ArrayList<>();

        //连表条件
        operations.add(lookupOperation);
        counts.add(lookupOperation);

        //查询子表不为空
        operations.add(Aggregation.match(where(foreignFieldName).not().size(0)));
        counts.add(Aggregation.match(where(foreignFieldName).not().size(0)));
        //主表条件
        if (mainMatch != null) {
            operations.add(mainMatch);
            counts.add(mainMatch);
        }
        //子表条件
        if (slaveMatch != null) {
            operations.add(slaveMatch);
            counts.add(slaveMatch);
        }

        Object object;
        try {
            object = Class.forName(resultClass.getName()).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            System.err.println("如果返回类型有误，则使用主表做返回类型");
            object = mainTable;
        }


        System.err.println("-------------------------------");
        System.err.println(new Date().toString());
        System.err.println("-------------------------------");
        System.err.println("主表名称:" + mainTableName);
        System.err.println("子表名称:" + slaveTableName);
        System.err.println("-------------------------------");
        System.err.println("主表关联id:" + mainTableId);
        System.err.println("子表关联id:" + slaveTableId);
        System.err.println("-------------------------------");
        System.err.println("子表列名:" + foreignFieldName);
        System.err.println("返回类:" + object.getClass());
        System.err.println("-------------------------------");


        List<?> mappedResults = mongoTemplate.aggregate(newAggregation(operations), mainTableName, object.getClass()).getMappedResults();
        return (List<R>) mappedResults;
    }


    /**
     * @Description 获取对象名称[驼峰命名]
     * @Param 实体类.class
     * @Return 对象名字[小写]
     * @Author 南有乔木
     * @Date 2019/07/16 下午 08:39
     */
    private static String getObjectName(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();

        //com.wx.saas.rtb.data.pojo.bo.CollectorDto -> CollectorDto
        String className = clazz.getName().split("\\.")[clazz.getName().split("\\.").length - 1];

        //CollectorDto -> collectorDto
        stringBuilder.append(String.valueOf(className.charAt(0)).toLowerCase());
        stringBuilder.append(className.substring(1));
        return stringBuilder.toString();
    }

    /**
     * @Description 联合查询的时候子表是哪个list
     * @Param 主表实体类.class
     * @Return 子表名
     * @Author 南有乔木
     * @Date 2019/08/01 上午 10:45
     */
    public static <T> String mongoChildName(Class<? super T> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType().getName().equals("java.util.List") || field.getType().getName().equals("java.util.Set")) {
                MongoChild mongoChild = field.getAnnotation(MongoChild.class);
                if (mongoChild != null) {
                    return field.getName();
                }
            }
        }
        throw new MongoException("多表关联中没有取到子类集合名字,请加上 @MongoChild");
    }

    /**
     * @Description 获得标注了MongoId的字段
     * @Param 实体类.class
     * @Return 标注了MongoId的字段
     * @Author 南有乔木
     * @Date 2019/08/01 上午 10:44
     */
    private static <T> String getMongoIdName(Class<? super T> clazz) {
        List<Field> fieldList = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        while (clazz != null && !clazz.getName().toLowerCase().equals("java.lang.object")) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        for (Field e : fieldList) {
            if (e.getAnnotation(Id.class) != null) {
                return e.getName();
            }
        }
        throw new MongoException("不能不设置关联ID");
    }

    /**
     * @Description 如果一个实体类有多个MongoId
     * @Param 实体类.class
     * @Return 标注了MongoId的字段集合
     * @Author 南有乔木
     * @Date 2019/08/01 上午 10:44
     */
    private static <T> List<String> getMongoIdNameList(Class<? super T> clazz) {
        List<String> nameList = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        while (clazz != null && !clazz.getName().toLowerCase().equals("java.lang.object")) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        for (Field e : fieldList) {
            if (e.getAnnotation(Id.class) != null) {
                nameList.add(e.getName());
            }
        }
        if (nameList.size() == 0) throw new MongoException("不能不设置查询主键");
        return nameList;
    }

    /**
     * @Description 通过注解获得排序字段
     * @Param 实体类.class
     * @Return 标注了排序的字段名称
     * @Author 南有乔木
     * @Date 2019/08/01 上午 10:43
     */
    private static <T> String getMongoOrderName(Class<? super T> clazz) {
        List<Field> fieldList = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        while (clazz != null && !clazz.getName().toLowerCase().equals("java.lang.object")) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        for (Field e : fieldList) {
            if (e.getAnnotation(MongoOrder.class) != null) {
                return e.getName();
            }
        }
        return null;
    }

}
