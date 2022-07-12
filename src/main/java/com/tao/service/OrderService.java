package com.tao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.pojo.OrderDetail;
import com.tao.pojo.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {

    //用户下单
    public void submit(Orders orders);

}
