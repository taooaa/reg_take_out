package com.tao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tao.pojo.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    public void clean();
}
