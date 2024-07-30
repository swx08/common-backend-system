package com.common.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.common.exception.SystemException;
import com.common.model.dto.DictDataAddDto;
import com.common.model.dto.DictDataSearchDto;
import com.common.model.dto.DictTypeAddDto;
import com.common.model.dto.DictTypeSearchDto;
import com.common.model.vo.DictDataEchoVo;
import com.common.model.vo.DictTypeEchoVo;
import com.common.response.ResultData;
import com.common.service.IDictDataService;
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
 * 字典数据表 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-07-13
 */
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@Api(tags = "字典数据模块")
@RestController
@RequestMapping("/dict/data")
public class DictDataController {

    private final IDictDataService dictDataService;

    /**
     * 分页查询字典数据
     * @param
     * @return
     */
    @GetMapping("/list/{pageNo}/{pageSize}")
    @ApiOperation(value = "分页查询字典数据")
    @SaCheckPermission(value = "system:dict_data:list",orRole = {"admin","common","test"})
    public ResultData queryDictDataList(@PathVariable("pageNo") Integer pageNo,
                                    @PathVariable("pageSize") Integer pageSize,
                                    DictDataSearchDto dto) {
        Map<String,Object> map = dictDataService.queryDictDataList(pageNo,pageSize,dto);
        return ResultData.success(map);
    }

    /**
     * 新增字典数据
     * @param
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增字典数据")
    @SaCheckPermission(value = "system:dict_data:add",orRole = {"admin"})
    public ResultData addDictData(@RequestBody @Validated DictDataAddDto dto) throws SystemException {
        return dictDataService.addDictData(dto);
    }

    /**
     * 查询字典标签数据
     * @param
     * @return
     */
    @GetMapping("/query/label")
    @ApiOperation(value = "查询字典标签数据")
    public ResultData queryDictLabelList(@RequestParam("type") String type) {
        List<Map<String,Object>> result = dictDataService.queryDictLabelList(type);
        return ResultData.success(result);
    }

    /**
     * 修改字典数据
     * @param
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改字典数据")
    @SaCheckPermission(value = "system:dict_data:update",orRole = {"admin"})
    public ResultData updateDict(@RequestBody @Valid DictDataEchoVo dictDataEchoVo) throws SystemException {
        return dictDataService.updateDict(dictDataEchoVo);
    }

    /**
     * 修改字典数据状态
     * @param
     * @return
     */
    @PutMapping("/update/status/{id}")
    @ApiOperation(value = "修改字典数据状态")
    @SaCheckPermission(value = "system:dict_data:update",orRole = {"admin"})
    public ResultData updateDictStatus(@PathVariable("id") Integer id) throws SystemException {
        return dictDataService.updateDictStatus(id);
    }

    /**
     * 字典数据回显
     * @param
     * @return
     */
    @GetMapping("/echo/{id}")
    @ApiOperation(value = "字典数据回显")
    public ResultData echoDictById(@PathVariable("id") Integer id) {
        DictDataEchoVo dictDataEchoVo = dictDataService.echoDictById(id);
        return ResultData.success(dictDataEchoVo);
    }

    /**
     * 删除字典数据
     * @param
     * @return
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除字典数据")
    @SaCheckPermission(value = "system:dict_data:delete",orRole = {"admin"})
    public ResultData deleteDict(@PathVariable("id") Integer id) throws SystemException {
        return dictDataService.deleteDict(id);
    }

    /**
     * 批量删除字典数据
     * @param
     * @return
     */
    @DeleteMapping("/batch/delete/{ids}")
    @ApiOperation(value = "批量删除字典数据")
    @SaCheckPermission(value = "system:dict_data:delete",orRole = {"admin"})
    public ResultData deleteDict(@PathVariable("ids") List<Integer> ids) throws SystemException {
        for (Integer id : ids) {
            dictDataService.deleteDict(id);
        }
        return ResultData.success();
    }
}
