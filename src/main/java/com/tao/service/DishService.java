package com.tao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.dto.DishDto;
import com.tao.pojo.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品，操作两张表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询口味
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品
    public void updateWithFlavor(DishDto dishDto);

    //删除菜品
    public void deleteWithFlavor(List<Long> ids);

}
