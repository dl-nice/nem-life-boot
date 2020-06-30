package com.nem.life.framework.boot.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    @ApiModelProperty(value = "第几页")
    private Integer page;

    @ApiModelProperty(value = "每页的大小")
    private Integer pageSize;

    @ApiModelProperty(value = "总数")
    private Integer total;

    @ApiModelProperty(value = "总页数")
    private Integer pageNumber;

    @ApiModelProperty(value = "分页数据")
    private List<T> records;
}
