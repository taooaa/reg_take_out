package com.tao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tao.common.BaseContext;
import com.tao.common.R;
import com.tao.dto.OrdersDto;
import com.tao.pojo.OrderDetail;
import com.tao.pojo.Orders;
import com.tao.pojo.ShoppingCart;
import com.tao.service.OrderDetailService;
import com.tao.service.OrderService;
import com.tao.service.ShoppingCartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    //用户下单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody  Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    //后台订单展示
    @GetMapping("/page")

    public R<Page> page(int page, int pageSize, String number,String beginTime,String endTime){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件  动态sql  字符串使用StringUtils.isNotEmpty这个方法来判断
        //这里使用了范围查询的动态SQL
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    //前台订单展示
    @GetMapping("/userPage")
        public R<Page> getOrders(int page, int pageSize){
            Page<Orders> pageInfo = new Page<>(page, pageSize);
            Page<OrdersDto> dtoPage = new Page<>();
            Long currentId = BaseContext.getCurrentId();
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            // 根据用户id分页查询出该用户的订单信息
            queryWrapper.eq(currentId != null, Orders::getUserId, currentId)
                    .orderByDesc(Orders::getOrderTime);
            orderService.page(pageInfo, queryWrapper);
            // 复制page
            BeanUtils.copyProperties(pageInfo, dtoPage, "records");
            // 将查出来的订单信息 根据orderid查出订单对应的菜品信息 从新转成list
            List<OrdersDto> ordersDtos = pageInfo.getRecords().stream().map((item) -> {
                OrdersDto dto = new OrdersDto();
                BeanUtils.copyProperties(item, dto);
                LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(item.getId() != null, OrderDetail::getOrderId, item.getId());
                List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
                dto.setOrderDetails(orderDetails);
                return dto;
            }).collect(Collectors.toList());
            // 从新返回结果集
            dtoPage.setRecords(ordersDtos);
            return R.success(dtoPage);
        }

    //修改订单状态
    @PutMapping
    public R<String> update(@RequestBody Orders orders){
        orderService.updateById(orders);
        return R.success("订单状态修改成功");
    }

    //再来一单功能
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,String> map){
        String ids = map.get("id");

        long id = Long.parseLong(ids);

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        shoppingCartService.clean();

        //获取用户id
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }
}
