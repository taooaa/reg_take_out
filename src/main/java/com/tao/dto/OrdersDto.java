package com.tao.dto;

import com.tao.pojo.OrderDetail;
import com.tao.pojo.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private List<OrderDetail> orderDetails;
}
