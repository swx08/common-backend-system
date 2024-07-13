package com.common.model.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
public class DictDataEchoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "字典标签")
    @NotBlank(message = "字典标签不能为空")
    private String label;

    @ApiModelProperty(value = "字典键值")
    @NotNull(message = "字典键值不能为空")
    private Integer value;

    @ApiModelProperty(value = "字典类型")
    private String type;

    @ApiModelProperty(value = "备注")
    @NotBlank(message = "备注不能为空")
    private String remark;
}
