package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.HomeSubjectSpuEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 专题商品
 *
 * @author houshiyin
 * @email 615734289@qq.com
 * @date 2019-12-02 19:31:24
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageVo queryPage(QueryCondition params);
}

