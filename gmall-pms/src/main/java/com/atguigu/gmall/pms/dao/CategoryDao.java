package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author houshiyin
 * @email 615734289@qq.com
 * @date 2019-12-12 10:00:03
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
