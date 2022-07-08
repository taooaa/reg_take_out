package com.tao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.dto.SetmealDto;
import com.tao.pojo.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    //新曾套餐
    public void saveWithDish(SetmealDto setmealDto);
}
