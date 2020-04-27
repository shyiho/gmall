package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.MemberException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Autowired
    private MemberDao memberDao;

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        switch (type){
            case 1 :
                wrapper.eq("username",data);
                break;
            case 2 :
                wrapper.eq("mobile",data);
                break;
            case 3 :
                wrapper.eq("email",data);
                break;
            default:
                return null;
        }
        return this.memberDao.selectCount(wrapper)==0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        //生成盐
        String salt = UUID.randomUUID().toString().substring(0,6);
        memberEntity.setSalt(salt);
        //对密码加密
        String password = DigestUtils.md5Hex(salt + memberEntity.getPassword());
        memberEntity.setPassword(password);
        //设置创建时间
        memberEntity.setCreateTime(new Date());
        //新增用户
        memberEntity.setGrowth(0);
        memberEntity.setIntegration(0);
        memberEntity.setLevelId(0l);
        memberEntity.setStatus(1);
        this.save(memberEntity);
    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        //查询
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));
        //校验用户名
        if (memberEntity==null) {
           throw new MemberException("用户名错误");
        }
        //校验密码
        if(!StringUtils.equals(DigestUtils.md5Hex(memberEntity.getSalt()+password),memberEntity.getPassword())){
           throw new MemberException("密码错误");
        }
        return memberEntity;
    }

}