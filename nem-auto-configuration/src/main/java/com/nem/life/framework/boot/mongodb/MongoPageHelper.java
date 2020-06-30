package com.nem.life.framework.boot.mongodb;

import com.nem.life.framework.boot.common.PageParameter;
import com.nem.life.framework.boot.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ConditionalOnProperty(prefix = "spring.data.mongodb", value = "uri")
@AutoConfigureAfter(MongoTemplate.class)
@Configuration
@RequiredArgsConstructor
public class MongoPageHelper {
    public static final Integer FIRST_PAGE_NUMBER = 1;
    private static final String ID = "_id";

    private final MongoTemplate mongoTemplate;

    /**
     * 分页查询
     *
     * @param query         分页条件
     * @param entityClass   查询实体类
     * @param pageParameter 查询分页参数
     * @param mapper        转换参数
     * @param lastId        最后一个id
     * @param <T>           请求参数
     * @param <R>           响应参数
     * @return 分页参数
     */
    public <T, R> PageResult<R> pageQuery(Query query, Class<T> entityClass,
                                          PageParameter<R> pageParameter, Function<T, R> mapper, String lastId) {
        //分页逻辑
        int total = (int) mongoTemplate.count(query, entityClass);
        final Integer pages = (int) Math.ceil(total / (double) pageParameter.getPageSize());
        if (pageParameter.getPage() <= 0 || pageParameter.getPage() > pages) {
            pageParameter.setPage(FIRST_PAGE_NUMBER);
        }
        final Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(lastId)) {
            if (pageParameter.getPage() != FIRST_PAGE_NUMBER) {
                criteria.and(ID).gt(new ObjectId(lastId));
            }
            query.limit(pageParameter.getPageSize());
        } else {
            int skip = pageParameter.getPageSize() * (pageParameter.getPage() - 1);
            query.skip(skip).limit(pageParameter.getPageSize());
        }

        final List<T> entityList = mongoTemplate
                .find(query.addCriteria(criteria)
                                //  .with(new Sort(Collections.singletonList(new Sort.Order(Sort.Direction.ASC, ID)))),
                                .with(Sort.by(
                                        Sort.Order.asc(ID)
                                )),
                        entityClass);
        final PageResult<R> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setPageNumber(pages);
        pageResult.setPageSize(pageParameter.getPageSize());
        pageResult.setPage(pageParameter.getPage());
        pageResult.setRecords(entityList.stream().map(mapper).collect(Collectors.toList()));
        return pageResult;
    }


    /**
     * 分页查询[无lastId]
     *
     * @param query         分页条件
     * @param entityClass   查询实体类
     * @param pageParameter 查询分页参数
     * @param mapper        转换参数
     * @param <T>           请求参数
     * @param <R>           响应参数
     * @return 分页参数
     */
    public <T, R> PageResult<R> pageQuery(Query query, Class<T> entityClass,
                                          PageParameter<R> pageParameter, Function<T, R> mapper) {
        return pageQuery(query, entityClass, pageParameter, mapper, null);
    }

}
