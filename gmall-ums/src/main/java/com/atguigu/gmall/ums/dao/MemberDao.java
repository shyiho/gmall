package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author houshiyin
 * @email 615734289@qq.com
 * @date 2019-12-02 19:33:39
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
