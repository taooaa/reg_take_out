package com.tao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tao.mapper.CategoryMapper;
import com.tao.pojo.Category;
import com.tao.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
}
