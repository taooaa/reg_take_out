package com.tao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.dto.SetmealDto;
import com.tao.mapper.SetmealMapper;
import com.tao.pojo.Setmeal;
import com.tao.pojo.SetmealDish;
import com.tao.service.SetmealDishService;
import com.tao.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    //新增套餐
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存菜品基本信息
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保证关联关系
        setmealDishService.saveBatch(setmealDishes);

    }
}
