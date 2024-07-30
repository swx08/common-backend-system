package com.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.SystemException;
import com.common.mapper.DictDataMapper;
import com.common.model.dto.DictTypeAddDto;
import com.common.model.dto.DictTypeSearchDto;
import com.common.model.entity.DictData;
import com.common.model.entity.DictType;
import com.common.mapper.DictTypeMapper;
import com.common.model.entity.User;
import com.common.model.enums.DictStatusEnum;
import com.common.model.enums.UserStatusEnum;
import com.common.model.vo.DictTypeEchoVo;
import com.common.model.vo.DictTypeListVo;
import com.common.model.vo.EchoUserVo;
import com.common.model.vo.UserListVo;
import com.common.response.ResponseCodeEnum;
import com.common.response.ResultData;
import com.common.service.IDictDataService;
import com.common.service.IDictTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典类型表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-07-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements IDictTypeService {

    private final DictDataMapper dictDataMapper;

    private final IDictDataService dictDataService;

    @Override
    public ResultData addDictType(DictTypeAddDto dto) throws SystemException {
        //校验字典类型和字典名称是否唯一
        verifyDictType(dto);
        //保存字典类型
        DictType dictType = new DictType();
        dictType.setName(dto.getName())
                .setRemark(dto.getRemark())
                .setType(dto.getType());
        if (this.save(dictType)) {
            return ResultData.success();
        }
        return ResultData.fail();
    }

    @Override
    public Map<String, Object> queryDictList(Integer pageNo, Integer pageSize, DictTypeSearchDto dto) {
        Page<DictType> pageInfo = new Page<>(pageNo, pageSize);
        QueryWrapper<DictType> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getName()), "name", dto.getName().trim());
        wrapper.like(StringUtils.isNotBlank(dto.getType()), "type", dto.getType().trim());
        wrapper.eq(dto.getStatus() != null, "status", dto.getStatus());
        wrapper.orderByDesc("create_time");

        Page<DictType> dictTypePage = baseMapper.selectPage(pageInfo, wrapper);
        List<DictType> dictTypeList = dictTypePage.getRecords();

        if (!CollectionUtils.isEmpty(dictTypeList)) {
            List<DictTypeListVo> list = convertDictTypeList(dictTypeList);
            Map<String, Object> map = new HashMap<>();
            map.put("data", list);
            map.put("total", dictTypePage.getTotal());
            return map;
        }
        return null;
    }

    @Override
    public List<String> queryAllTypeData() {
        List<DictType> dictTypes = baseMapper.selectList(null);
        if(!CollectionUtils.isEmpty(dictTypes)) {
            return dictTypes.stream().map(DictType::getType).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public ResultData updateDictStatus(Integer id) {
        DictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            log.error("字典数据不存在，无法修改状态");
            return ResultData.fail(1029, "字典类型不存在！");
        }
        log.info("正在修改字典类型{}的状态...",dictType.getType());

        DictStatusEnum newStatus;
        if (dictType.getStatus().intValue() == DictStatusEnum.OPEN.getCode()) {
            newStatus = DictStatusEnum.CLOSE;
            log.info("修改状态为：{}", newStatus.getStatus());
        } else {
            newStatus = DictStatusEnum.OPEN;
            log.info("修改状态为：{}", newStatus.getStatus());
        }
        dictType.setStatus(newStatus.getCode());

        if (baseMapper.updateById(dictType) > 0) {
            log.info("字典类型{}的状态修改成功", dictType.getType());
            return ResultData.success();
        } else {
            log.error("字典类型{}的状态修改失败", dictType.getType());
            return ResultData.fail(1013, "字典类型状态修改失败！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultData deleteDict(Integer id) {
        DictType dictType = baseMapper.selectById(id);
        log.info("正在删除字典类型：{}",dictType.getType());
        //删除字典数据和类型关联表的数据
        removeDictDataById(id);
        //删除字典类型
        if(baseMapper.deleteById(id) > 0) {
            log.info("id为{}的字典类型已被彻底删除",id);
            return ResultData.success();
        }else{
            log.error("id为{}的字典类型删除失败",id);
            return ResultData.fail(1017,"字典类型删除失败！");
        }
    }

    private void removeDictDataById(Integer id) {
        log.info("正在删除字典数据和类型关联表的数据...");
        DictType dictType = baseMapper.selectById(id);
        if(null != dictType){
            List<DictData> dictDataList = dictDataMapper.selectList(new QueryWrapper<DictData>().eq("type", dictType.getType()));
            if(!CollectionUtils.isEmpty(dictDataList)){
                dictDataService.removeBatchByIds(dictDataList);
            }
        }
    }

    @Override
    public DictTypeEchoVo echoDictById(Integer id) {
        DictType dictType = baseMapper.selectById(id);
        if(null != dictType){
            DictTypeEchoVo dictTypeEchoVo = new DictTypeEchoVo();
            BeanUtils.copyProperties(dictType, dictTypeEchoVo);
            return dictTypeEchoVo;
        }
        return null;
    }

    @Override
    public ResultData updateDict(DictTypeEchoVo dictTypeEchoVo) throws SystemException {
        log.info("正在修改字典类型：{}",dictTypeEchoVo.getType());
        //校验字典类型和字典名称是否唯一
        verifyDictTypeWithEdit(dictTypeEchoVo);
        //修改字典类型
        DictType dictType = baseMapper.selectById(dictTypeEchoVo.getId());
        //同时修改dict_data表中的type字段
        if(!dictType.getType().equalsIgnoreCase(dictTypeEchoVo.getType())){
            updateDictDataBatchId(dictType, dictTypeEchoVo);
        }
        BeanUtils.copyProperties(dictTypeEchoVo, dictType);
        if (this.updateById(dictType)) {
            log.info("字典类型修改成功");
            return ResultData.success();
        }
        log.error("字典类型修改失败");
        return ResultData.fail();
    }

    private void updateDictDataBatchId(DictType dictType, DictTypeEchoVo dictTypeEchoVo) {
        QueryWrapper<DictData> wrapper = new QueryWrapper<>();
        wrapper.eq("type", dictType.getType());
        List<DictData> dictData = dictDataMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(dictData)){
            dictData.forEach(item -> {
                item.setType(dictTypeEchoVo.getType());
            });
            //批量修改
            dictDataService.updateBatchById(dictData);
        }
    }

    private void verifyDictTypeWithEdit(DictTypeEchoVo dictTypeEchoVo) throws SystemException {
        //查询字典类型是否存在
        QueryWrapper<DictType> wrapper = new QueryWrapper<>();
        wrapper.eq("type", dictTypeEchoVo.getType());
        DictType dictType1 = baseMapper.selectOne(wrapper);
        if (dictType1 != null && !dictType1.getId().equals(dictTypeEchoVo.getId())) {
            log.error("字典类型已存在");
            throw new SystemException(ResponseCodeEnum.FAIL_DICT_TYPE_EXIST);
        }
        //查询字典名称是否存在
        wrapper.clear();
        wrapper.eq("name", dictTypeEchoVo.getName());
        DictType dictType2 = baseMapper.selectOne(wrapper);
        if (dictType2 != null && !dictType2.getId().equals(dictTypeEchoVo.getId())) {
            log.error("字典名称已存在");
            throw new SystemException(ResponseCodeEnum.FAIL_DICT_NAME_EXIST);
        }
    }

    private List<DictTypeListVo> convertDictTypeList(List<DictType> dictTypes) {
        List<DictTypeListVo> list = new ArrayList<>();
        dictTypes.forEach(item -> {
            DictTypeListVo itemVo = new DictTypeListVo();
            BeanUtils.copyProperties(item, itemVo);
            itemVo.setChecked(item.getStatus().intValue() == DictStatusEnum.OPEN.getCode());
            list.add(itemVo);
        });
        return list;
    }

    private void verifyDictType(DictTypeAddDto dto) throws SystemException {
        //查询字典类型是否存在
        QueryWrapper<DictType> wrapper = new QueryWrapper<>();
        wrapper.eq("type", dto.getType());
        if (this.count(wrapper) > 0) {
            log.error("字典类型已存在");
            throw new SystemException(ResponseCodeEnum.FAIL_DICT_TYPE_EXIST);
        }
        //查询字典名称是否存在
        wrapper.clear();
        wrapper.eq("name", dto.getName());
        if (this.count(wrapper) > 0) {
            log.error("字典名称已存在");
            throw new SystemException(ResponseCodeEnum.FAIL_DICT_NAME_EXIST);
        }
    }
}
