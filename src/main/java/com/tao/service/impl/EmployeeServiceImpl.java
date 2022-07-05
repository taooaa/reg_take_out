package com.tao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.mapper.EmployeeMapper;
import com.tao.pojo.Employee;
import com.tao.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
