package com.common.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.common.model.enums.DictStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典类型表
 * </p>
 *
 * @author author
 * @since 2024-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="DictType对象", description="字典类型表")
public class DictTypeSearchDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典名称")
    private String name;

    @ApiModelProperty(value = "字典类型")
    private String type;

    @ApiModelProperty(value = "字典状态：0：停用；1：正常")
    private Integer status;
}
