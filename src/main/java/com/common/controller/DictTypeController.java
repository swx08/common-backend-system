package com.common.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.common.exception.SystemException;
import com.common.model.dto.AddMenuDto;
import com.common.model.dto.DictTypeAddDto;
import com.common.model.dto.DictTypeSearchDto;
import com.common.model.dto.SearchUserDto;
import com.common.model.vo.DictTypeEchoVo;
import com.common.model.vo.EchoUserVo;
import com.common.response.ResultData;
import com.common.service.IDictTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典类型表 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-07-10
 */
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@Api(tags = "字典类型模块")
@RestController
@RequestMapping("/dict/type")
public class DictTypeController {

    private final IDictTypeService dictTypeService;

    /**
     * 分页查询字典类型数据
     * @param
     * @return
     */
    @GetMapping("/list/{pageNo}/{pageSize}")
    @ApiOperation(value = "分页查询字典类型数据")
    @SaCheckPermission(value = "system:dict:list",orRole = {"admin","common","test"})
    public ResultData queryDictList(@PathVariable("pageNo") Integer pageNo,
                                    @PathVariable("pageSize") Integer pageSize,
                                    DictTypeSearchDto dto) {
        Map<String,Object> map = dictTypeService.queryDictList(pageNo,pageSize,dto);
        return ResultData.success(map);
    }

    /**
     * 新增字典
     * @param
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增字典")
    @SaCheckPermission(value = "system:dict:add",orRole = {"admin"})
    public ResultData addDictType(@RequestBody @Validated DictTypeAddDto dto) throws SystemException {
        return dictTypeService.addDictType(dto);
    }

    /**
     * 查询字典类型数据
     * @param
     * @return
     */
    @GetMapping("/query/type")
    @ApiOperation(value = "查询字典类型数据")
    public ResultData queryAllTypeData() {
        List<String> typeList = dictTypeService.queryAllTypeData();
        return ResultData.success(typeList);
    }

    /**
     * 修改字典类型
     * @param
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改字典类型")
    @SaCheckPermission(value = "system:dict:update",orRole = {"admin"})
    public ResultData updateDict(@RequestBody @Valid DictTypeEchoVo dictTypeEchoVo) throws SystemException {
        return dictTypeService.updateDict(dictTypeEchoVo);
    }

    /**
     * 修改字典状态
     * @param
     * @return
     */
    @PutMapping("/update/status/{id}")
    @ApiOperation(value = "修改字典状态")
    @SaCheckPermission(value = "system:dict:update",orRole = {"admin"})
    public ResultData updateDictStatus(@PathVariable("id") Integer id) throws SystemException {
        return dictTypeService.updateDictStatus(id);
    }

    /**
     * 字典类型数据回显
     * @param
     * @return
     */
    @GetMapping("/echo/{id}")
    @ApiOperation(value = "字典类型数据回显")
    public ResultData echoDictById(@PathVariable("id") Integer id) {
        DictTypeEchoVo dictTypeEchoVo = dictTypeService.echoDictById(id);
        return ResultData.success(dictTypeEchoVo);
    }

    /**
     * 删除字典
     * @param
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除字典")
    @SaCheckPermission(value = "system:dict:delete",orRole = {"admin"})
    public ResultData deleteDict(@PathVariable("id") Integer id) throws SystemException {
        return dictTypeService.deleteDict(id);
    }

    /**
     * 批量删除字典
     * @param
     * @return
     */
    @DeleteMapping("/batch/delete/{ids}")
    @ApiOperation(value = "批量删除字典")
    @SaCheckPermission(value = "system:dict:delete",orRole = {"admin"})
    public ResultData batchDeleteDict(@PathVariable("ids") List<Integer> ids) throws SystemException {
        for (Integer id : ids) {
            dictTypeService.deleteDict(id);
        }
        return ResultData.success();
    }
}
