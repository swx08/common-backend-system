package com.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.SystemException;
import com.common.mapper.DictTypeMapper;
import com.common.model.dto.DictDataAddDto;
import com.common.model.dto.DictDataSearchDto;
import com.common.model.entity.DictData;
import com.common.mapper.DictDataMapper;
import com.common.model.entity.DictType;
import com.common.model.enums.DictStatusEnum;
import com.common.model.vo.DictDataEchoVo;
import com.common.model.vo.DictDataListVo;
import com.common.model.vo.DictTypeEchoVo;
import com.common.model.vo.DictTypeListVo;
import com.common.response.ResponseCodeEnum;
import com.common.response.ResultData;
import com.common.service.IDictDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典数据表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-07-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements IDictDataService {

    private final DictTypeMapper dictTypeMapper;

    @Override
    public ResultData addDictData(DictDataAddDto dto) throws SystemException {
        //校验同一个字段类型下是否有重复的字典标签
        verifyLabel(dto);
        DictData dictData = new DictData();
        BeanUtils.copyProperties(dto, dictData);
        if(baseMapper.insert(dictData) > 0){
            log.info("新增字典数据成功！");
            return ResultData.success();
        }else{
            log.error("新增字典数据失败！");
            return ResultData.fail(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public Map<String, Object> queryDictDataList(Integer pageNo, Integer pageSize, DictDataSearchDto dto) {
        Page<DictData> pageInfo = new Page<>(pageNo, pageSize);
        QueryWrapper<DictData> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getLabel()), "label", dto.getLabel().trim());
        wrapper.like(StringUtils.isNotBlank(dto.getType()), "type", dto.getType().trim());
        wrapper.eq(dto.getStatus() != null, "status", dto.getStatus());
        wrapper.orderByDesc("create_time");

        Page<DictData> dictDataPage = baseMapper.selectPage(pageInfo, wrapper);
        List<DictData> dictDataList = dictDataPage.getRecords();

        if (!CollectionUtils.isEmpty(dictDataList)) {
            List<DictDataListVo> list = convertDictDataList(dictDataList);
            Map<String, Object> map = new HashMap<>();
            map.put("data", list);
            map.put("total", dictDataPage.getTotal());
            return map;
        }
        return null;
    }

    @Override
    public ResultData updateDictStatus(Integer id) {
        DictData dictData = baseMapper.selectById(id);
        if (dictData == null) {
            log.error("字典数据不存在，无法修改状态");
            return ResultData.fail(1029, "字典类型不存在！");
        }
        log.info("正在修改字典类型{}的状态...",dictData.getType());

        DictStatusEnum newStatus;
        if (dictData.getStatus().intValue() == DictStatusEnum.OPEN.getCode()) {
            newStatus = DictStatusEnum.CLOSE;
            log.info("修改状态为：{}", newStatus.getStatus());
        } else {
            newStatus = DictStatusEnum.OPEN;
            log.info("修改状态为：{}", newStatus.getStatus());
        }
        dictData.setStatus(newStatus.getCode());

        if (baseMapper.updateById(dictData) > 0) {
            log.info("字典类型{}的状态修改成功", dictData.getType());
            return ResultData.success();
        } else {
            log.error("字典类型{}的状态修改失败", dictData.getType());
            return ResultData.fail(1013, "字典类型状态修改失败！");
        }
    }

    @Override
    public ResultData deleteDict(Integer id) {
        DictData dictData = baseMapper.selectById(id);
        log.info("正在删除字典类型：{}",dictData.getType());
        //删除字典类型
        if(baseMapper.deleteById(id) > 0) {
            log.info("id为{}的字典类型已被彻底删除",id);
            return ResultData.success();
        }else{
            log.error("id为{}的字典类型删除失败",id);
            return ResultData.fail(1017,"字典类型删除失败！");
        }
    }

    @Override
    public DictDataEchoVo echoDictById(Integer id) {
        DictData dictData = baseMapper.selectById(id);
        if(null != dictData){
            DictDataEchoVo dictDataEchoVo = new DictDataEchoVo();
            BeanUtils.copyProperties(dictData, dictDataEchoVo);
            return dictDataEchoVo;
        }
        return null;
    }

    @Override
    public ResultData updateDict(DictDataEchoVo dictDataEchoVo) throws SystemException {
        //校验字典类型和字典名称是否唯一
        verifyDictDataWithEdit(dictDataEchoVo);
        //修改字典类型
        DictData dictData = baseMapper.selectById(dictDataEchoVo.getId());
        BeanUtils.copyProperties(dictDataEchoVo, dictData);
        if (this.updateById(dictData)) {
            return ResultData.success();
        }
        return ResultData.fail();
    }

    @Override
    public List<Map<String,Object>> queryDictLabelList(String type) {
        log.info("正在查询字典类型为{}的数据...",type);
        //查询字典类型的状态
        QueryWrapper<DictType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type);
        DictType dictType = dictTypeMapper.selectOne(queryWrapper);
        if(dictType == null){
            log.error("字典类型{}不存在",type);
            return Collections.emptyList();
        }
        if(!dictType.getStatus().equals(DictStatusEnum.OPEN.getCode())){
            log.error("字典类型{}状态为关闭，无法查询",type);
            return Collections.emptyList();
        }

        List<Map<String,Object>> list = new ArrayList<>();
        QueryWrapper<DictData> wrapper = new QueryWrapper<>();
        wrapper.eq("type",type);
        wrapper.eq("status",DictStatusEnum.OPEN.getCode());
        List<DictData> dictData = baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(dictData)){
            dictData.forEach(item -> {
                Map<String,Object> map = new HashMap<>();
                map.put("label",item.getLabel());
                map.put("value",item.getValue());
                list.add(map);
            });
            return list;
        }
        return Collections.emptyList();
    }

    private void verifyDictDataWithEdit(DictDataEchoVo dataEchoVo) throws SystemException {
        QueryWrapper<DictData> wrapper = new QueryWrapper<>();
        wrapper.eq("type",dataEchoVo.getType());
        List<DictData> dictData = baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(dictData)){
            for (DictData item : dictData) {
                if((item.getLabel().equalsIgnoreCase(dataEchoVo.getLabel())) && (item.getId().intValue() != dataEchoVo.getId())){
                    throw new SystemException(ResponseCodeEnum.DICT_DATA_LABEL_REPEAT);
                }
            }
        }
    }

    private List<DictDataListVo> convertDictDataList(List<DictData> dictDataList) {
        List<DictDataListVo> list = new ArrayList<>();
        dictDataList.forEach(item -> {
            DictDataListVo itemVo = new DictDataListVo();
            BeanUtils.copyProperties(item, itemVo);
            itemVo.setChecked(item.getStatus().intValue() == DictStatusEnum.OPEN.getCode());
            list.add(itemVo);
        });
        return list;
    }

    private void verifyLabel(DictDataAddDto dto) throws SystemException {
        QueryWrapper<DictData> wrapper = new QueryWrapper<>();
        wrapper.eq("type",dto.getType());
        List<DictData> dictData = baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(dictData)){
            for (DictData item : dictData) {
                if(item.getLabel().equalsIgnoreCase(dto.getLabel())){
                    throw new SystemException(ResponseCodeEnum.DICT_DATA_LABEL_REPEAT);
                }
                if(item.getValue().equals(dto.getValue())){
                    throw new SystemException(ResponseCodeEnum.DICT_DATA_VALUE_REPEAT);
                }
            }
        }
    }
}
