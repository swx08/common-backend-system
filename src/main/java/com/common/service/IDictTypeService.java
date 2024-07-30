package com.common.service;

import com.common.exception.SystemException;
import com.common.model.dto.DictTypeAddDto;
import com.common.model.dto.DictTypeSearchDto;
import com.common.model.entity.DictType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.common.model.vo.DictTypeEchoVo;
import com.common.response.ResultData;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典类型表 服务类
 * </p>
 *
 * @author author
 * @since 2024-07-10
 */
public interface IDictTypeService extends IService<DictType> {

    /**
     * 新增字典
     * @param dto
     * @return
     */
    ResultData addDictType(DictTypeAddDto dto) throws SystemException;

    /**
     * 分页查询字典类型数据
     * @param pageNo
     * @param pageSize
     * @param dto
     * @return
     */
    Map<String, Object> queryDictList(Integer pageNo, Integer pageSize, DictTypeSearchDto dto);

    /**
     * 查询字典类型数据
     * @return
     */
    List<String> queryAllTypeData();

    /**
     * 修改字典状态
     * @param id
     * @return
     */
    ResultData updateDictStatus(Integer id);

    /**
     * 删除字典
     * @param id
     * @return
     */
    ResultData deleteDict(Integer id);

    /**
     * 字典类型数据回显
     * @param id
     * @return
     */
    DictTypeEchoVo echoDictById(Integer id);

    /**
     * 修改字典类型
     * @param dictTypeEchoVo
     * @return
     */
    ResultData updateDict(DictTypeEchoVo dictTypeEchoVo) throws SystemException;
}
