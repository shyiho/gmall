package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.GroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidPage(Long cid, QueryCondition condition) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if(cid!=null){
            wrapper.eq("catelog_id", cid);
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                wrapper
        );
        return new PageVo(page);
    }

    @Override
    public GroupVO queryGroupWithAttrsByGid(Long gid) {
        GroupVO groupVO = new GroupVO();
        //查询group
        AttrGroupEntity groupEntity = this.getById(gid);
        BeanUtils.copyProperties(groupEntity,groupVO);
        //根据gid查询关联关系，并获取attrIds
        List<AttrAttrgroupRelationEntity> relations = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        if(CollectionUtils.isEmpty(relations)){
            return groupVO;
        }
        groupVO.setRelations(relations);
        //根据attrIds查询，所有的规格参数
        List<Long> attrIds = relations.stream().map(relationEntity -> relationEntity.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrs = this.attrDao.selectBatchIds(attrIds);
        groupVO.setAttrEntities(attrs);
        return groupVO;
    }

    @Override
    public List<GroupVO> queryGroupAttrsByCid(Long cid) {
        List<AttrGroupEntity> groupEntities= this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        return groupEntities.stream().map(t->this.queryGroupWithAttrsByGid(t.getAttrGroupId())).collect(Collectors.toList());
    }

}