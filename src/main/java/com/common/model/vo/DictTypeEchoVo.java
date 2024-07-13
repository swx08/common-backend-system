package com.common.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
public class DictTypeEchoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典id")
    private Long id;

    @ApiModelProperty(value = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    private String name;

    @ApiModelProperty(value = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    private String type;

    @ApiModelProperty(value = "备注")
    @NotBlank(message = "字典备注不能为空")
    private String remark;
}
