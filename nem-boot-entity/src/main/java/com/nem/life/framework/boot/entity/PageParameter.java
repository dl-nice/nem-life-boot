package com.nem.life.framework.boot.entity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageParameter<T> {
    @ApiModelProperty(name = "page", value = "第几页[默认1]", required = false, example = "1")
    private Integer page;

    @ApiModelProperty(name = "pageSize", value = "页大小[默认20]", required = false, example = "10")
    private Integer pageSize;

    @ApiModelProperty(name = "lastId", value = "最后一个id", required = false, example = "10")
    private Long lastId;

    public Integer getPage() {
        return page == null ? 1 : page;
    }

    public Integer getPageSize() {
        return pageSize == null ? 20 : pageSize;
    }

    public static PageParameter fullPage() {
        return new PageParameter(1, 999999999, 0L);
    }

    public Page<T> convertMpPage() {
        Page<T> page = new Page<>();
        page.setCurrent(this.getPage());
        page.setSize(this.getPageSize());
        return page;
    }
}
