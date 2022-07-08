package com.tao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.dto.DishDto;
import com.tao.dto.SetmealDto;
import com.tao.pojo.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //新曾套餐
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐
    public void removeWithDish(List<Long> ids);

    //套餐数据回显示
    SetmealDto getData(Long id);

    //修改套餐
    public void SetMealUpdateWithDish(SetmealDto setmealDto);
}
