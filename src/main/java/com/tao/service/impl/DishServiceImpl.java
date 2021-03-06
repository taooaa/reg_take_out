package com.tao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.common.CustomException;
import com.tao.dto.DishDto;
import com.tao.mapper.DishMapper;
import com.tao.pojo.Dish;
import com.tao.pojo.DishFlavor;
import com.tao.pojo.Setmeal;
import com.tao.pojo.SetmealDish;
import com.tao.service.DishFlavorService;
import com.tao.service.DishService;
import com.tao.service.SetmealDishService;
import javafx.css.Styleable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealDishService setmealDishService;

    //新增菜品，同时保存对应的口味数据
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        Long dishDtoId = dishDto.getId();
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味表
        dishFlavorService.saveBatch(flavors);
    }

    //根据id查询菜品和口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品接不能信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //从dish_flavor查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    //更新菜品
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //插入新数据
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    //删除菜品和关系表数据
    @Override
    @Transactional
    public void deleteWithFlavor(List<Long> ids) {
        //判断是否可以删除
        //查询是否关联套餐，如果已经关联，抛出异常
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(setmealDishLambdaQueryWrapper);

        if(count1>0){
            //已经关联套餐，抛出异常
            throw new CustomException("当前菜品已经关联套餐，不能能删除");

        }
        //如果没有关联，判断状态是否停售
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);

        int count = this.count(queryWrapper);
        if (count>0){
            throw new CustomException("菜品正在售卖，不可删除");
        }

        this.removeByIds(ids);

        //删除关系表中数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);

        dishFlavorService.remove(lambdaQueryWrapper);
    }

}
