package com.tao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.common.CustomException;
import com.tao.mapper.CategoryMapper;
import com.tao.pojo.Category;
import com.tao.pojo.Dish;
import com.tao.pojo.Setmeal;
import com.tao.service.CategoryService;
import com.tao.service.DishService;
import com.tao.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    //根据id删除
    @Override
    public void remove(Long ids) {
        //查询是否关联套餐，如果已经关联，抛出异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count = dishService.count(dishLambdaQueryWrapper);

        if(count>0){
            //已经关联菜品，抛出异常
            throw new CustomException("当前分类下已经关联菜品，不能能删除");

        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0){
            //关联了套餐，抛出异常
            throw new CustomException("当前分类下已经关联套餐，不能能删除");
        }
        super.removeById(ids);
    }
}
