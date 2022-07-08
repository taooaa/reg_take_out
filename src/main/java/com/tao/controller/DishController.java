package com.tao.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tao.common.R;
import com.tao.dto.DishDto;
import com.tao.pojo.Category;
import com.tao.pojo.Dish;
import com.tao.pojo.Employee;
import com.tao.service.CategoryService;
import com.tao.service.DishFlavorService;
import com.tao.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody  DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    //分页查询菜品
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();


        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //过滤条件
        queryWrapper.like(StringUtils.hasText(name),Dish::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if(category!= null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody  DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    //删除菜品
//    @DeleteMapping
//    public R<String> delete(Long ids){
//        dishService.deleteWithFlavor(ids);
//        return R.success("删除菜品成功");
//    }


    //根据条件查询对应菜品数据
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());

        //只查询状态为1的，即起售状态的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }
}
