package com.atguigu.gmall.pms.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SkuSaleVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfo(QueryCondition condition, Long catId) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //判断分类是否为0
        if (catId!=0){
            wrapper.eq("catalog_id",catId);
        }
        //判断关键字是否为空
        String key = condition.getKey();
        if(StringUtils.isNotBlank(key)){
            wrapper.and(t->t.eq("id",key).or().like("spu_name",key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition), wrapper
        );
        return new PageVo(page);
    }

    @Autowired
    private SpuInfoDescDao descDao;
    @Autowired
    private ProductAttrValueService attrValueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Override
    @GlobalTransactional
    public void BigSave(SpuInfoVO spuInfoVO) {
        //1保存spu相关的3张表
        //1.1保存pms_spu_info信息
        Long spuId = saveSpuInfo(spuInfoVO);
        //1.2保存pms_spu_info_desc
        //this.spuInfoDescService.saveSpuInfoDesc(spuInfoVO, spuId);有实体类设置idType.INPUT无法添加id报错
        //1.3保存pms_product_attr_value
        saveBaseAttrValue(spuInfoVO, spuId);
        //2保存sku相关的3张表
        saveSkuAndSale(spuInfoVO, spuId);
    }

    private void saveSkuAndSale(SpuInfoVO spuInfoVO, Long spuId) {
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return ;
        }
        skus.forEach(skuInfoVO -> {
            //2.1保存pms_sku_info
            skuInfoVO.setSpuId(spuId);
            skuInfoVO.setSkuCode(UUID.randomUUID().toString());
            skuInfoVO.setBrandId(spuInfoVO.getBrandId());
            skuInfoVO.setCatalogId(spuInfoVO.getCatalogId());
            List<String> images = skuInfoVO.getImages();
            if(!CollectionUtils.isEmpty(images)){
                skuInfoVO.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoVO.getSkuDefaultImg())?skuInfoVO.getSkuDefaultImg():images.get(0));
            }
            this.skuInfoDao.insert(skuInfoVO);
            Long skuId = skuInfoVO.getSkuId();
            //2.2保存pms_sku_images
            if(!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoVO.getSkuDefaultImg(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }
            //2.3保存pms_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            if(!CollectionUtils.isEmpty(saleAttrs)){
                //设置skuid
                saleAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                //批量保存销售属性
                this.saleAttrValueService.saveBatch(saleAttrs);
            }
            //3保存营销信息的3张表（feign远程调用sms保存）
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            BeanUtils.copyProperties(skuInfoVO,skuSaleVO);
            skuSaleVO.setSkuId(skuId);
            this.gmallSmsClient.saveSale(skuSaleVO);
        });
    }

    private void saveBaseAttrValue(SpuInfoVO spuInfoVO, Long spuId) {
        List<BaseAttrVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(t -> {
                ProductAttrValueEntity attrValueEntity = t;
                attrValueEntity.setSpuId(spuId);
                return attrValueEntity;
            }).collect(Collectors.toList());
            this.attrValueService.saveBatch(collect);
        }
    }

    private Long saveSpuInfo(SpuInfoVO spuInfoVO) {
            spuInfoVO.setCreateTime(new Date());
            spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
            this.save(spuInfoVO);
            return spuInfoVO.getId();
    }

}