package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author houshiyin
 * @email 615734289@qq.com
 * @date 2019-12-02 19:28:51
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
