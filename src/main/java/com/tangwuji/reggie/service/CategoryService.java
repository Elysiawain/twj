package com.tangwuji.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tangwuji.reggie.commons.R;
import com.tangwuji.reggie.pojo.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 按照菜品的id进行删除
     * @param ids
     */
    R<Object> deleteById(Long ids);
}
