package com.common.service;

import com.common.exception.SystemException;
import com.common.model.dto.DictDataAddDto;
import com.common.model.dto.DictDataSearchDto;
import com.common.model.entity.DictData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.model.vo.DictDataEchoVo;
import com.common.response.ResultData;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典数据表 服务类
 * </p>
 *
 * @author author
 * @since 2024-07-13
 */
public interface IDictDataService extends IService<DictData> {

    /**
     * 新增字典数据
     * @param dto
     * @return
     */
    ResultData addDictData(DictDataAddDto dto) throws SystemException;

    /**
     * 分页查询字典数据
     */
    Map<String, Object> queryDictDataList(Integer pageNo, Integer pageSize, DictDataSearchDto dto);

    /**
     * 修改字典数据状态
     * @param id
     * @return
     */
    ResultData updateDictStatus(Integer id);

    /**
     * 删除字典数据
     * @param id
     * @return
     */
    ResultData deleteDict(Integer id);

    /**
     * 字典数据回显
     * @param id
     * @return
     */
    DictDataEchoVo echoDictById(Integer id);

    /**
     * 修改字典数据
     * @param dictDataEchoVo
     * @return
     */
    ResultData updateDict(DictDataEchoVo dictDataEchoVo) throws SystemException;

    /**
     * 查询字典标签数据
     * @param type
     * @return
     */
    List<Map<String,Object>> queryDictLabelList(String type);
}
