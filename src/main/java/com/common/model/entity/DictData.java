package com.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 字典数据表
 * </p>
 *
 * @author author
 * @since 2024-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("dict_data")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="DictData对象", description="字典数据表")
public class DictData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "字典标签")
    private String label;

    @ApiModelProperty(value = "字典键值")
    private Integer value;

    @ApiModelProperty(value = "字典类型")
    private String type;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "字典数据状态：0：停用；1：正常")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
